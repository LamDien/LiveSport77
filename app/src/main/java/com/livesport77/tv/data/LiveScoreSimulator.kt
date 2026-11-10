package com.livesport77.tv.data

import com.livesport77.tv.model.*

/**
 * Live Score Simulator.
 * Simulates real-time match events for Android TV demonstration.
 */
class LiveScoreSimulator {

    fun advanceMatchTime(match: Match): Match {
        if (match.status != MatchStatus.LIVE) return match

        // Extract minute number from "78' LIVE"
        val currentMin = match.statusDisplay.takeWhile { it.isDigit() }.toIntOrNull() ?: 78
        val nextMin = (currentMin + 1).coerceAtMost(90)

        // Potential simulation of goal at minute 82
        val (newScore, newEvents) = if (nextMin == 82 && match.events.none { it.minute == "82'" }) {
            val updatedScore = match.score.copy(
                home = match.score.home + 1,
                periodInfo = "$nextMin'"
            )
            val newGoal = MatchEvent(
                id = "sim_g_${System.currentTimeMillis()}",
                minute = "82'",
                type = EventType.GOAL,
                teamId = match.homeTeam.id,
                playerName = "Kai Havertz",
                detail = "Đệm bóng cận thành từ đường tạt của Saka",
                scoreAtEvent = "${updatedScore.home} - ${updatedScore.away}"
            )
            updatedScore to (match.events + newGoal)
        } else {
            match.score.copy(periodInfo = "$nextMin'") to match.events
        }

        return match.copy(
            statusDisplay = "$nextMin' LIVE",
            score = newScore,
            events = newEvents
        )
    }
}

