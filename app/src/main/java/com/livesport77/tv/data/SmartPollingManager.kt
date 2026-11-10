package com.livesport77.tv.data

import com.livesport77.tv.model.Match
import com.livesport77.tv.model.MatchStatus
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.StateFlow

/**
 * Smart Polling Manager for LiveSport77 Android TV.
 * Dynamically adjusts poll intervals to save TV CPU & network:
 * - 8 seconds when there is at least one LIVE match.
 * - 60 seconds when matches are scheduled or finished.
 * - 0% CPU consumption when paused/stopped.
 */
class SmartPollingManager(
    private val scope: CoroutineScope,
    private val onPollTrigger: () -> Unit
) {
    private var pollingJob: Job? = null
    private var isPaused = false

    fun startPolling(matchesFlow: StateFlow<List<Match>>) {
        pollingJob?.cancel()
        pollingJob = scope.launch(Dispatchers.Default) {
            while (isActive) {
                if (!isPaused) {
                    val currentMatches = matchesFlow.value
                    val hasLiveMatches = currentMatches.any { it.status == MatchStatus.LIVE }

                    // Trigger data refresh callback
                    onPollTrigger()

                    val intervalMs = if (hasLiveMatches) 8_000L else 60_000L
                    delay(intervalMs)
                } else {
                    delay(1_000L)
                }
            }
        }
    }

    fun pause() {
        isPaused = true
    }

    fun resume() {
        isPaused = false
    }

    fun stop() {
        pollingJob?.cancel()
        pollingJob = null
    }
}

