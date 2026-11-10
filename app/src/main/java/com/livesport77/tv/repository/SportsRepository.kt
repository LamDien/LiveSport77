package com.livesport77.tv.repository

import android.util.Log
import com.livesport77.tv.data.MockSportsDataSource
import com.livesport77.tv.data.api.LiveScoreApiService
import com.livesport77.tv.model.Match
import com.livesport77.tv.model.MatchEvent
import com.livesport77.tv.model.MatchStatus
import com.livesport77.tv.model.SportType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

interface SportsRepository {
    fun getMatches(sport: SportType): Flow<List<Match>>
    suspend fun refreshSport(sport: SportType): Boolean
    suspend fun fetchMatchIncidents(sport: SportType, match: Match): List<MatchEvent>
    fun getMatchDetail(matchId: String): Flow<Match?>
    fun simulateScoreUpdate()
}

class SportsRepositoryImpl(
    private val apiService: LiveScoreApiService = LiveScoreApiService()
) : SportsRepository {

    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    // In-memory cache holding current matches state (initialized with baseline data for 0ms initial render)
    private val _matchesState = MutableStateFlow<Map<SportType, List<Match>>>(
        mapOf(
            SportType.FOOTBALL to MockSportsDataSource.getMatchesBySport(SportType.FOOTBALL),
            SportType.BASKETBALL to MockSportsDataSource.getMatchesBySport(SportType.BASKETBALL),
            SportType.TENNIS to MockSportsDataSource.getMatchesBySport(SportType.TENNIS)
        )
    )

    init {
        // Immediately fetch live matches from LiveScore.com CDN on startup
        repositoryScope.launch {
            Log.d("SportsRepository", "Initial fetch of live data from LiveScore.com CDN...")
            refreshSport(SportType.FOOTBALL)
            refreshSport(SportType.BASKETBALL)
            refreshSport(SportType.TENNIS)
        }
    }

    override fun getMatches(sport: SportType): Flow<List<Match>> {
        return _matchesState.map { it[sport] ?: emptyList() }
    }

    override suspend fun refreshSport(sport: SportType): Boolean {
        return try {
            val liveMatches = apiService.fetchMatches(sport)
            if (liveMatches.isNotEmpty()) {
                Log.d("SportsRepository", "Successfully fetched ${liveMatches.size} live matches for $sport from LiveScore.com")
                _matchesState.update { current ->
                    current + (sport to liveMatches)
                }
                true
            } else {
                Log.w("SportsRepository", "LiveScore API returned 0 matches for $sport, keeping current data")
                false
            }
        } catch (e: Exception) {
            Log.e("SportsRepository", "Error refreshing $sport from LiveScore API: ${e.message}", e)
            false
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

    override fun simulateScoreUpdate() {
        val currentMap = _matchesState.value.toMutableMap()
        val currentFootball = currentMap[SportType.FOOTBALL]?.toMutableList() ?: return

        // Update first live match or first match for demo simulation
        val liveIndex = currentFootball.indexOfFirst { it.status == MatchStatus.LIVE }
        val targetIndex = if (liveIndex != -1) liveIndex else 0
        if (currentFootball.isNotEmpty()) {
            val oldMatch = currentFootball[targetIndex]
            val newMinute = 88
            val updatedMatch = oldMatch.copy(
                status = MatchStatus.LIVE,
                statusDisplay = "${newMinute}' LIVE",
                score = oldMatch.score.copy(
                    home = oldMatch.score.home + 1,
                    periodInfo = "${newMinute}'"
                )
            )
            currentFootball[targetIndex] = updatedMatch
            currentMap[SportType.FOOTBALL] = currentFootball
            _matchesState.value = currentMap
        }
    }
}
