package com.livesport77.tv.viewmodel

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.livesport77.tv.model.Match
import com.livesport77.tv.model.SportType
import com.livesport77.tv.repository.SportsRepository
import com.livesport77.tv.repository.SportsRepositoryImpl
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@Immutable
data class SportsUiState(
    val selectedSport: SportType = SportType.FOOTBALL,
    val matches: List<Match> = emptyList(),
    val activeMatch: Match? = null,
    val isFullscreen: Boolean = false,
    val isLoading: Boolean = false,
    val isLiveScoreConnected: Boolean = true
)

@OptIn(ExperimentalCoroutinesApi::class)
class SportsViewModel(
    private val repository: SportsRepository = SportsRepositoryImpl()
) : ViewModel() {

    private val _selectedSport = MutableStateFlow(SportType.FOOTBALL)
    private val _isFullscreen = MutableStateFlow(false)
    private val _isLoading = MutableStateFlow(false)
    private val _selectedMatchId = MutableStateFlow<String?>(null)

    private val _uiState = MutableStateFlow(SportsUiState(isLoading = true))
    val uiState: StateFlow<SportsUiState> = _uiState.asStateFlow()

    init {
        // Collect matches reactively whenever selectedSport or repository state updates
        viewModelScope.launch {
            _selectedSport.flatMapLatest { sport ->
                repository.getMatches(sport).map { matches -> Pair(sport, matches) }
            }.collect { (sport, matchesList) ->
                _uiState.update { current ->
                    val targetId = _selectedMatchId.value
                    val newActive = if (targetId != null) {
                        matchesList.find { it.id == targetId } ?: matchesList.firstOrNull()
                    } else {
                        current.activeMatch?.let { prev -> matchesList.find { it.id == prev.id } }
                            ?: matchesList.firstOrNull()
                    }

                    current.copy(
                        selectedSport = sport,
                        matches = matchesList,
                        activeMatch = newActive,
                        isLoading = false,
                        isLiveScoreConnected = true
                    )
                }

                // If active match has no incidents yet, fetch them from LiveScore scoreboard
                _uiState.value.activeMatch?.let { active ->
                    if (active.events.isEmpty()) {
                        fetchIncidentsForMatch(sport, active)
                    }
                }
            }
        }

        // Periodic background poll every 45s to fetch real-time updates from LiveScore.com
        startAutoRefresh()
    }

    fun selectSport(sport: SportType) {
        if (_selectedSport.value == sport) return
        _selectedSport.value = sport
        _selectedMatchId.value = null
        viewModelScope.launch {
            _uiState.update { it.copy(selectedSport = sport, isLoading = true) }
            repository.refreshSport(sport)
        }
    }

    fun setActiveMatch(match: Match) {
        _selectedMatchId.value = match.id
        _uiState.update { it.copy(activeMatch = match) }
        fetchIncidentsForMatch(_selectedSport.value, match)
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
                delay(45_000L) // 45 seconds polling
                repository.refreshSport(_selectedSport.value)
            }
        }
    }

    fun setFullscreen(fullscreen: Boolean) {
        _uiState.update { it.copy(isFullscreen = fullscreen) }
    }

    fun triggerLiveSimulation() {
        repository.simulateScoreUpdate()
    }
}
