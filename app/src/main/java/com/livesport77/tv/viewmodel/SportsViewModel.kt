package com.livesport77.tv.viewmodel

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.livesport77.tv.model.*
import com.livesport77.tv.repository.SportsRepository
import com.livesport77.tv.repository.SportsRepositoryImpl
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Immutable
data class SportsUiState(
    val selectedSport: SportType = SportType.FOOTBALL,
    val dateOffset: Int = 0,
    val dateDisplay: String = "Hôm nay",
    val dateDayOfMonth: String = "10",
    val dateApiStr: String = "",
    val isLiveOnly: Boolean = false,
    val matches: List<Match> = emptyList(),
    val filteredMatches: List<Match> = emptyList(),
    val groupedLeagues: List<LeagueGroup> = emptyList(),
    val activeMatch: Match? = null,
    val isFullscreen: Boolean = false,
    val isLoading: Boolean = true,
    val favorites: Set<String> = emptySet()
)

class SportsViewModel(
    private val repository: SportsRepository = SportsRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(SportsUiState(isLoading = true))
    val uiState: StateFlow<SportsUiState> = _uiState.asStateFlow()

    init {
        // Compute initial today date
        val (apiDate, displayDate, dayOfMonth) = computeDateInfo(0)
        _uiState.update {
            it.copy(
                dateApiStr = apiDate,
                dateDisplay = displayDate,
                dateDayOfMonth = dayOfMonth,
                isLoading = true
            )
        }

        // On launch: Fetch directly from LiveScore.com CDN (NO CACHE)
        loadDataForCurrentSelection()

        // Background auto refresh every 45s for live scores
        startAutoRefresh()
    }

    fun selectSport(sport: SportType) {
        if (_uiState.value.selectedSport == sport) return
        _uiState.update { it.copy(selectedSport = sport, isLoading = true, activeMatch = null) }
        loadDataForCurrentSelection()
    }

    fun toggleLiveFilter() {
        val newLive = !_uiState.value.isLiveOnly
        _uiState.update { current ->
            val filtered = if (newLive) {
                current.matches.filter { it.status == MatchStatus.LIVE }
            } else {
                current.matches
            }
            val grouped = groupMatchesByLeague(filtered)
            val newActive = if (current.activeMatch != null && filtered.any { it.id == current.activeMatch.id }) {
                current.activeMatch
            } else {
                filtered.firstOrNull()
            }
            current.copy(
                isLiveOnly = newLive,
                filteredMatches = filtered,
                groupedLeagues = grouped,
                activeMatch = newActive
            )
        }
    }

    fun prevDate() {
        changeDateOffset(_uiState.value.dateOffset - 1)
    }

    fun nextDate() {
        changeDateOffset(_uiState.value.dateOffset + 1)
    }

    fun resetToToday() {
        if (_uiState.value.dateOffset == 0) return
        changeDateOffset(0)
    }

    private fun changeDateOffset(newOffset: Int) {
        val (apiDate, displayDate, dayOfMonth) = computeDateInfo(newOffset)
        _uiState.update {
            it.copy(
                dateOffset = newOffset,
                dateApiStr = apiDate,
                dateDisplay = displayDate,
                dateDayOfMonth = dayOfMonth,
                isLoading = true,
                activeMatch = null
            )
        }
        loadDataForCurrentSelection()
    }

    private fun loadDataForCurrentSelection() {
        viewModelScope.launch {
            val sport = _uiState.value.selectedSport
            val dateStr = _uiState.value.dateApiStr
            _uiState.update { it.copy(isLoading = true) }

            val rawMatches = repository.refreshSport(sport, dateStr)
            val isLive = _uiState.value.isLiveOnly
            val filtered = if (isLive) {
                rawMatches.filter { it.status == MatchStatus.LIVE }
            } else {
                rawMatches
            }
            val grouped = groupMatchesByLeague(filtered)
            val initialActive = filtered.firstOrNull()

            _uiState.update { current ->
                current.copy(
                    matches = rawMatches,
                    filteredMatches = filtered,
                    groupedLeagues = grouped,
                    activeMatch = initialActive,
                    isLoading = false
                )
            }

            // Fetch incidents for first active match if available
            initialActive?.let { active ->
                fetchIncidentsForMatch(sport, active)
            }
        }
    }

    fun setActiveMatch(match: Match) {
        _uiState.update { it.copy(activeMatch = match) }
        fetchIncidentsForMatch(_uiState.value.selectedSport, match)
    }

    fun toggleFavorite(matchId: String) {
        _uiState.update { current ->
            val favs = current.favorites.toMutableSet()
            if (favs.contains(matchId)) favs.remove(matchId) else favs.add(matchId)
            current.copy(favorites = favs)
        }
    }

    fun setFullscreen(fullscreen: Boolean) {
        _uiState.update { it.copy(isFullscreen = fullscreen) }
    }

    private fun fetchIncidentsForMatch(sport: SportType, match: Match) {
        viewModelScope.launch {
            val incidents = repository.fetchMatchIncidents(sport, match)
            if (incidents.isNotEmpty()) {
                _uiState.update { current ->
                    if (current.activeMatch?.id == match.id) {
                        current.copy(activeMatch = current.activeMatch.copy(events = incidents))
                    } else {
                        current
                    }
                }
            }
        }
    }

    private fun startAutoRefresh() {
        viewModelScope.launch {
            while (true) {
                delay(45_000L) // Poll LiveScore CDN every 45s
                if (_uiState.value.dateOffset == 0) { // Only auto refresh today
                    val sport = _uiState.value.selectedSport
                    val dateStr = _uiState.value.dateApiStr
                    val updatedMatches = repository.refreshSport(sport, dateStr)
                    val isLive = _uiState.value.isLiveOnly
                    val filtered = if (isLive) {
                        updatedMatches.filter { it.status == MatchStatus.LIVE }
                    } else {
                        updatedMatches
                    }
                    val grouped = groupMatchesByLeague(filtered)
                    _uiState.update { current ->
                        current.copy(
                            matches = updatedMatches,
                            filteredMatches = filtered,
                            groupedLeagues = grouped
                        )
                    }
                }
            }
        }
    }

    private fun groupMatchesByLeague(matchesList: List<Match>): List<LeagueGroup> {
        val groupedMap = LinkedHashMap<String, MutableList<Match>>()
        for (m in matchesList) {
            val key = m.leagueId.ifEmpty { m.leagueName }
            groupedMap.getOrPut(key) { mutableListOf() }.add(m)
        }
        return groupedMap.map { (id, list) ->
            val sample = list.first()
            LeagueGroup(
                id = id,
                name = sample.leagueName,
                country = sample.leagueCountry,
                flag = sample.leagueFlag,
                matches = list
            )
        }
    }

    private fun computeDateInfo(offset: Int): Triple<String, String, String> {
        val cal = Calendar.getInstance(TimeZone.getDefault())
        cal.add(Calendar.DAY_OF_YEAR, offset)

        val apiFormat = SimpleDateFormat("yyyyMMdd", Locale.US).apply {
            timeZone = TimeZone.getDefault()
        }
        val apiDate = apiFormat.format(cal.time)

        // Day of month for calendar icon
        val dayOfMonthFormat = SimpleDateFormat("dd", Locale.getDefault())
        val dayOfMonth = dayOfMonthFormat.format(cal.time)

        val displayDate = when (offset) {
            0 -> "Today"
            -1 -> "Yesterday"
            1 -> "Tomorrow"
            else -> {
                val userFormat = SimpleDateFormat("EEE, dd MMM", Locale.US)
                userFormat.format(cal.time)
            }
        }

        return Triple(apiDate, displayDate, dayOfMonth)
    }
}
