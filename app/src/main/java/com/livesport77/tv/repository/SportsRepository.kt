package com.livesport77.tv.repository

import android.util.Log
import com.livesport77.tv.data.api.LiveScoreApiService
import com.livesport77.tv.model.Match
import com.livesport77.tv.model.MatchEvent
import com.livesport77.tv.model.SportType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

interface SportsRepository {
    fun getMatches(sport: SportType): Flow<List<Match>>
    suspend fun refreshSport(sport: SportType, dateStr: String = ""): List<Match>
    suspend fun fetchMatchIncidents(sport: SportType, match: Match): List<MatchEvent>
    fun getMatchDetail(matchId: String): Flow<Match?>
}

class SportsRepositoryImpl(
    private val apiService: LiveScoreApiService = LiveScoreApiService()
) : SportsRepository {

    // Pure dynamic in-memory state: NO MOCK DATA, NO CACHED STORAGE
    private val _matchesState = MutableStateFlow<Map<SportType, List<Match>>>(emptyMap())

    override fun getMatches(sport: SportType): Flow<List<Match>> {
        return _matchesState.map { it[sport] ?: emptyList() }
    }

    override suspend fun refreshSport(sport: SportType, dateStr: String): List<Match> {
        return try {
            Log.d("SportsRepository", "Fetching fresh data directly from LiveScore.com CDN for $sport (date: $dateStr)...")
            val liveMatches = apiService.fetchMatches(sport, dateStr)
            _matchesState.update { current ->
                current + (sport to liveMatches)
            }
            Log.d("SportsRepository", "Loaded ${liveMatches.size} matches from LiveScore CDN")
            liveMatches
        } catch (e: Exception) {
            Log.e("SportsRepository", "Error fetching from LiveScore API: ${e.message}", e)
            emptyList()
        }
    }

    override suspend fun fetchMatchIncidents(sport: SportType, match: Match): List<MatchEvent> {
        return try {
            val incidents = apiService.fetchScoreboard(sport, match.id, match.homeTeam.id, match.awayTeam.id)
            if (incidents.isNotEmpty()) {
                _matchesState.update { currentMap ->
                    val sportMatches = currentMap[sport]?.toMutableList() ?: return@update currentMap
                    val index = sportMatches.indexOfFirst { it.id == match.id }
                    if (index != -1) {
                        sportMatches[index] = sportMatches[index].copy(events = incidents)
                        currentMap + (sport to sportMatches)
                    } else {
                        currentMap
                    }
                }
            }
            incidents
        } catch (e: Exception) {
            Log.w("SportsRepository", "Failed to fetch incidents for match ${match.id}: ${e.message}")
            emptyList()
        }
    }

    override fun getMatchDetail(matchId: String): Flow<Match?> {
        return _matchesState.map { sportMap ->
            sportMap.values.flatten().find { it.id == matchId }
        }
    }
}
