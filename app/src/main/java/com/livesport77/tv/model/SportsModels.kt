package com.livesport77.tv.model

import androidx.compose.runtime.Immutable

/**
 * Supported Sports in LiveSport77
 */
enum class SportType(val id: String, val displayName: String, val icon: String) {
    FOOTBALL("football", "Bóng đá", "⚽"),
    BASKETBALL("basketball", "Bóng rổ", "🏀"),
    TENNIS("tennis", "Quần vợt", "🎾")
}

/**
 * Match Status
 */
enum class MatchStatus {
    LIVE,
    SCHEDULED,
    FINISHED,
    POSTPONED
}

/**
 * Event Types during a match (Goal, Card, Substitution, Point)
 */
enum class EventType {
    GOAL,
    PENALTY_GOAL,
    YELLOW_CARD,
    RED_CARD,
    SUBSTITUTION,
    POINT
}

@Immutable
data class Team(
    val id: String,
    val name: String,
    val shortName: String,
    val iconEmoji: String,
    val logoUrl: String? = null
)

@Immutable
data class MatchScore(
    val home: Int,
    val away: Int,
    val periodInfo: String // e.g., "78'", "FT", "22:30", "AET", "HT"
)

@Immutable
data class MatchEvent(
    val id: String,
    val minute: String,
    val type: EventType,
    val teamId: String,
    val playerName: String,
    val detail: String? = null,
    val scoreAtEvent: String? = null
)

@Immutable
data class MatchStats(
    val possessionHome: Int = 50,
    val possessionAway: Int = 50,
    val shotsOnTargetHome: Int = 0,
    val shotsOnTargetAway: Int = 0,
    val totalShotsHome: Int = 0,
    val totalShotsAway: Int = 0,
    val cornersHome: Int = 0,
    val cornersAway: Int = 0,
    val foulsHome: Int = 0,
    val foulsAway: Int = 0
)

@Immutable
data class MatchLineup(
    val homeFormation: String = "4-3-3",
    val awayFormation: String = "4-2-3-1",
    val homeStarting: List<String> = emptyList(),
    val awayStarting: List<String> = emptyList()
)

@Immutable
data class Match(
    val id: String,
    val sport: SportType,
    val leagueId: String,
    val leagueName: String,
    val leagueCountry: String = "",
    val leagueFlag: String,
    val startTime: String,
    val status: MatchStatus,
    val statusDisplay: String,
    val homeTeam: Team,
    val awayTeam: Team,
    val score: MatchScore,
    val events: List<MatchEvent> = emptyList(),
    val stats: MatchStats? = null,
    val lineup: MatchLineup? = null,
    val isFavorite: Boolean = false
)

@Immutable
data class LeagueGroup(
    val id: String,
    val name: String,
    val country: String,
    val flag: String,
    val matches: List<Match>
)
