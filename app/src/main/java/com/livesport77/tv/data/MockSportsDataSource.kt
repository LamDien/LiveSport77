package com.livesport77.tv.data

import com.livesport77.tv.model.*

/**
 * In-Memory Mock Data Source providing LiveScore-quality match details
 * with zero network overhead.
 */
object MockSportsDataSource {

    private val footballMatches = listOf(
        Match(
            id = "fb_01",
            sport = SportType.FOOTBALL,
            leagueId = "epl",
            leagueName = "Premier League - Vòng 11",
            leagueFlag = "🏴󠁧󠁢󠁥󠁮󠁧󠁿",
            startTime = "19:30",
            status = MatchStatus.LIVE,
            statusDisplay = "78' LIVE",
            homeTeam = Team("ars", "Arsenal", "ARS", "🔴"),
            awayTeam = Team("che", "Chelsea", "CHE", "🔵"),
            score = MatchScore(2, 1, "78'"),
            events = listOf(
                MatchEvent("e1", "23'", EventType.GOAL, "ars", "Bukayo Saka", "Kiến tạo: Martin Ødegaard", "1 - 0"),
                MatchEvent("e2", "55'", EventType.GOAL, "ars", "Martin Ødegaard", "Sút xa từ ngoài vòng cấm", "2 - 0"),
                MatchEvent("e3", "68'", EventType.PENALTY_GOAL, "che", "Cole Palmer", "Đá phạt đền thành công", "2 - 1"),
                MatchEvent("e4", "72'", EventType.YELLOW_CARD, "che", "Moisés Caicedo", "Phạm lỗi nguy hiểm", "Foul")
            ),
            stats = MatchStats(
                possessionHome = 58,
                possessionAway = 42,
                shotsOnTargetHome = 6,
                shotsOnTargetAway = 3,
                totalShotsHome = 14,
                totalShotsAway = 8,
                cornersHome = 5,
                cornersAway = 2,
                foulsHome = 9,
                foulsAway = 12
            ),
            lineup = MatchLineup(
                homeFormation = "4-3-3",
                awayFormation = "4-2-3-1",
                homeStarting = listOf("Raya", "White", "Saliba", "Gabriel", "Timber", "Partey", "Rice", "Ødegaard", "Saka", "Havertz", "Martinelli"),
                awayStarting = listOf("Sánchez", "Gusto", "Fofana", "Colwill", "Cucurella", "Caicedo", "Lavia", "Madueke", "Palmer", "Neto", "Jackson")
            )
        ),
        Match(
            id = "fb_02",
            sport = SportType.FOOTBALL,
            leagueId = "laliga",
            leagueName = "La Liga - El Clásico",
            leagueFlag = "🇪🇸",
            startTime = "16:00",
            status = MatchStatus.FINISHED,
            statusDisplay = "FT (Kết thúc)",
            homeTeam = Team("rma", "Real Madrid", "RMA", "⚪"),
            awayTeam = Team("bar", "Barcelona", "BAR", "🔴🔵"),
            score = MatchScore(1, 0, "FT"),
            events = listOf(
                MatchEvent("e21", "34'", EventType.GOAL, "rma", "Jude Bellingham", "Đánh đầu cận thành", "1 - 0"),
                MatchEvent("e22", "61'", EventType.YELLOW_CARD, "bar", "Gavi", "Tranh cãi trọng tài", "Card"),
                MatchEvent("e23", "89'", EventType.SUBSTITUTION, "rma", "Luka Modrić", "Vào sân thay Vinicius", "Sub")
            ),
            stats = MatchStats(
                possessionHome = 46,
                possessionAway = 54,
                shotsOnTargetHome = 4,
                shotsOnTargetAway = 5,
                totalShotsHome = 11,
                totalShotsAway = 13,
                cornersHome = 3,
                cornersAway = 6,
                foulsHome = 14,
                foulsAway = 10
            )
        ),
        Match(
            id = "fb_03",
            sport = SportType.FOOTBALL,
            leagueId = "epl",
            leagueName = "Premier League - Super Sunday",
            leagueFlag = "🏴󠁧󠁢󠁥󠁮󠁧󠁿",
            startTime = "22:30",
            status = MatchStatus.SCHEDULED,
            statusDisplay = "22:30 Hôm nay",
            homeTeam = Team("mci", "Manchester City", "MCI", "🩵"),
            awayTeam = Team("liv", "Liverpool", "LIV", "🔴"),
            score = MatchScore(0, 0, "Chưa đá"),
            events = emptyList(),
            stats = MatchStats()
        ),
        Match(
            id = "fb_04",
            sport = SportType.FOOTBALL,
            leagueId = "ucl",
            leagueName = "Champions League - Bảng F",
            leagueFlag = "⭐",
            startTime = "02:45",
            status = MatchStatus.SCHEDULED,
            statusDisplay = "02:45 Rạng sáng",
            homeTeam = Team("bay", "Bayern Munich", "BAY", "🔴⚪"),
            awayTeam = Team("psg", "Paris Saint-Germain", "PSG", "🔵🔴"),
            score = MatchScore(0, 0, "Chưa đá"),
            events = emptyList(),
            stats = MatchStats()
        )
    )

    private val basketballMatches = listOf(
        Match(
            id = "bb_01",
            sport = SportType.BASKETBALL,
            leagueId = "nba",
            leagueName = "NBA - Regular Season",
            leagueFlag = "🇺🇸",
            startTime = "08:30",
            status = MatchStatus.LIVE,
            statusDisplay = "Q4 02:15 LIVE",
            homeTeam = Team("lal", "Los Angeles Lakers", "LAL", "🟡🟣"),
            awayTeam = Team("gsw", "Golden State Warriors", "GSW", "🔵🟡"),
            score = MatchScore(104, 99, "Q4 02:15"),
            events = listOf(
                MatchEvent("b1", "Q4 02:15", EventType.POINT, "lal", "LeBron James", "3-pt Stepback Jump Shot", "104 - 99"),
                MatchEvent("b2", "Q4 02:45", EventType.POINT, "gsw", "Stephen Curry", "Pullup 3-pt", "101 - 99"),
                MatchEvent("b3", "Q4 03:10", EventType.POINT, "lal", "Anthony Davis", "Alley Oop Dunk", "101 - 96")
            ),
            stats = MatchStats(
                possessionHome = 52,
                possessionAway = 48,
                shotsOnTargetHome = 44, // FG%
                shotsOnTargetAway = 41,
                cornersHome = 12, // 3-pt made
                cornersAway = 14
            )
        ),
        Match(
            id = "bb_02",
            sport = SportType.BASKETBALL,
            leagueId = "nba",
            leagueName = "NBA - Eastern Conference",
            leagueFlag = "🇺🇸",
            startTime = "10:00",
            status = MatchStatus.SCHEDULED,
            statusDisplay = "10:00 Hôm nay",
            homeTeam = Team("bos", "Boston Celtics", "BOS", "🟢⚪"),
            awayTeam = Team("mil", "Milwaukee Bucks", "MIL", "🦌"),
            score = MatchScore(0, 0, "Chưa đá"),
            events = emptyList(),
            stats = MatchStats()
        )
    )

    private val tennisMatches = listOf(
        Match(
            id = "tn_01",
            sport = SportType.TENNIS,
            leagueId = "atp",
            leagueName = "ATP Masters 1000 - Chung Kết",
            leagueFlag = "🎾",
            startTime = "14:00",
            status = MatchStatus.LIVE,
            statusDisplay = "Set 3 (4-3) LIVE",
            homeTeam = Team("nov", "Novak Djokovic", "N. DJO", "🇷🇸"),
            awayTeam = Team("car", "Carlos Alcaraz", "C. ALC", "🇪🇸"),
            score = MatchScore(2, 1, "Set 3 (4-3)"),
            events = listOf(
                MatchEvent("t1", "Set 1", EventType.POINT, "nov", "Djokovic thắng Set 1 (6-4)", "First Set", "6 - 4"),
                MatchEvent("t2", "Set 2", EventType.POINT, "car", "Alcaraz thắng Set 2 (7-5)", "Second Set", "5 - 7"),
                MatchEvent("t3", "Set 3", EventType.POINT, "nov", "Djokovic Break Game (4-3)", "Break Point", "4 - 3")
            ),
            stats = MatchStats(
                possessionHome = 68, // 1st serve %
                possessionAway = 64,
                shotsOnTargetHome = 8, // Aces
                shotsOnTargetAway = 11,
                cornersHome = 2, // Double faults
                cornersAway = 4
            )
        )
    )

    fun getMatchesBySport(sport: SportType): List<Match> {
        return when (sport) {
            SportType.FOOTBALL -> footballMatches
            SportType.BASKETBALL -> basketballMatches
            SportType.TENNIS -> tennisMatches
        }
    }
}

