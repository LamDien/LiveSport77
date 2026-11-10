package com.livesport77.tv.data.api

import android.util.Log
import com.livesport77.tv.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

/**
 * LiveScore.com Direct API Service.
 * Fetches real-time sports data directly from LiveScore CDN public endpoints.
 */
class LiveScoreApiService {

    private val baseUrl = "https://prod-cdn-public-api.lsmedia1.com/v1/api/app"
    private val cdnImageBase = "https://lsm-static-prod.livescore.com/medium"
    private val userAgent = "Mozilla/5.0 (Linux; Android 14; Android TV) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"

    /**
     * Fetch all matches for the given sport and date directly from LiveScore.com CDN.
     * @param dateStr Date in format yyyyMMdd (e.g., 20261110). If empty, today (UTC) is used.
     */
    suspend fun fetchMatches(sport: SportType, dateStr: String = ""): List<Match> = withContext(Dispatchers.IO) {
        val sportPath = when (sport) {
            SportType.FOOTBALL -> "soccer"
            SportType.HOCKEY -> "hockey"
            SportType.BASKETBALL -> "basketball"
            SportType.TENNIS -> "tennis"
            SportType.CRICKET -> "cricket"
        }

        val targetDate = if (dateStr.isNotEmpty()) {
            dateStr
        } else {
            val localFormat = SimpleDateFormat("yyyyMMdd", Locale.US).apply {
                timeZone = TimeZone.getDefault()
            }
            localFormat.format(Date())
        }

        val tzOffset = getTimezoneOffsetParam()
        val endpointUrl = "$baseUrl/date/$sportPath/$targetDate/$tzOffset"

        try {
            val url = URL(endpointUrl)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 8000
                readTimeout = 8000
                setRequestProperty("User-Agent", userAgent)
                setRequestProperty("Accept", "application/json, text/plain, */*")
            }

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val jsonString = reader.use { it.readText() }
                return@withContext parseLiveScoreJson(jsonString, sport)
            } else {
                Log.w("LiveScoreApi", "Server returned HTTP ${connection.responseCode} for $endpointUrl")
            }
        } catch (e: Exception) {
            Log.e("LiveScoreApi", "Error fetching LiveScore data: ${e.message}", e)
        }

        return@withContext emptyList()
    }

    /**
     * Compute device local timezone offset in hours formatted as %.2f (e.g. 7.00 for Vietnam UTC+7).
     * LiveScore API requires this parameter to align with user's local day and broadcast schedule.
     */
    fun getTimezoneOffsetParam(): String {
        val tz = TimeZone.getDefault()
        val offsetMillis = tz.getOffset(System.currentTimeMillis())
        val hours = offsetMillis / 3600000.0
        return String.format(Locale.US, "%.2f", hours)
    }

    /**
     * Fetch live scoreboard detail & incidents (goals, cards) for a match.
     */
    suspend fun fetchScoreboard(
        sport: SportType,
        matchId: String,
        homeId: String,
        awayId: String
    ): List<MatchEvent> = withContext(Dispatchers.IO) {
        val sportPath = when (sport) {
            SportType.FOOTBALL -> "soccer"
            SportType.HOCKEY -> "hockey"
            SportType.BASKETBALL -> "basketball"
            SportType.TENNIS -> "tennis"
            SportType.CRICKET -> "cricket"
        }
        val endpointUrl = "$baseUrl/scoreboard/$sportPath/$matchId"

        try {
            val url = URL(endpointUrl)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 6000
                readTimeout = 6000
                setRequestProperty("User-Agent", userAgent)
                setRequestProperty("Accept", "application/json, text/plain, */*")
            }

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val jsonString = connection.inputStream.bufferedReader().use { it.readText() }
                val eventObj = JSONObject(jsonString)
                return@withContext parseIncidents(eventObj, homeId, awayId)
            }
        } catch (e: Exception) {
            Log.w("LiveScoreApi", "Scoreboard fetch error for $matchId: ${e.message}")
        }
        emptyList()
    }

    private fun parseLiveScoreJson(jsonString: String, sport: SportType): List<Match> {
        val matches = mutableListOf<Match>()
        try {
            val root = JSONObject(jsonString)
            val stages = root.optJSONArray("Stages") ?: return emptyList()

            for (i in 0 until stages.length()) {
                val stage = stages.getJSONObject(i)
                val country = stage.optString("Cnm", "")
                val stageName = stage.optString("Snm", "Giải đấu")
                val leagueId = stage.optString("Scd", "league_$i")
                val flag = getCountryFlag(country, sport)

                val events = stage.optJSONArray("Events") ?: continue
                for (j in 0 until events.length()) {
                    val event = events.getJSONObject(j)
                    val eid = event.optString("Eid", UUID.randomUUID().toString())

                    // Team 1 (Home)
                    val t1Arr = event.optJSONArray("T1")
                    val t1Obj = t1Arr?.optJSONObject(0)
                    val homeName = t1Obj?.optString("Nm", "Home") ?: "Home"
                    val homeAbr = t1Obj?.optString("Abr", homeName.take(3).uppercase()) ?: "HOM"
                    val homeId = t1Obj?.optString("ID", "t1_$eid") ?: "t1_$eid"
                    val homeImg = t1Obj?.optString("Img", "") ?: ""
                    val homeLogoUrl = if (homeImg.isNotEmpty()) "$cdnImageBase/$homeImg" else null

                    // Team 2 (Away)
                    val t2Arr = event.optJSONArray("T2")
                    val t2Obj = t2Arr?.optJSONObject(0)
                    val awayName = t2Obj?.optString("Nm", "Away") ?: "Away"
                    val awayAbr = t2Obj?.optString("Abr", awayName.take(3).uppercase()) ?: "AWY"
                    val awayId = t2Obj?.optString("ID", "t2_$eid") ?: "t2_$eid"
                    val awayImg = t2Obj?.optString("Img", "") ?: ""
                    val awayLogoUrl = if (awayImg.isNotEmpty()) "$cdnImageBase/$awayImg" else null

                    // Scores
                    val tr1 = event.optString("Tr1", "")
                    val tr2 = event.optString("Tr2", "")
                    val homeScore = tr1.toIntOrNull() ?: 0
                    val awayScore = tr2.toIntOrNull() ?: 0

                    // Status & Minute
                    val eps = event.optString("Eps", "NS") // e.g. "78'", "FT", "AET", "HT", "NS", "20:00"
                    val esid = event.optInt("Esid", 1)
                    val esd = event.optLong("Esd", 0L)
                    val startTimeFormatted = formatStartTime(esd)

                    val matchStatus = when (esid) {
                        2, 3, 4 -> MatchStatus.LIVE
                        6 -> MatchStatus.FINISHED
                        5 -> MatchStatus.POSTPONED
                        else -> {
                            if (eps.equals("FT", ignoreCase = true) || eps.equals("AET", ignoreCase = true) || eps.equals("AP", ignoreCase = true)) MatchStatus.FINISHED
                            else if (eps.contains("'") || eps.contains("LIVE", ignoreCase = true) || eps.equals("HT", ignoreCase = true)) MatchStatus.LIVE
                            else if (eps.contains("Postp", ignoreCase = true) || eps.contains("Canc", ignoreCase = true)) MatchStatus.POSTPONED
                            else MatchStatus.SCHEDULED
                        }
                    }

                    val statusDisplay = when (matchStatus) {
                        MatchStatus.LIVE -> if (eps.contains("'") || eps.equals("HT", ignoreCase = true)) eps else "LIVE"
                        MatchStatus.FINISHED -> if (eps.equals("AET", ignoreCase = true)) "AET" else if (eps.equals("AP", ignoreCase = true)) "AP" else "FT"
                        MatchStatus.SCHEDULED -> if (eps != "NS" && eps.isNotEmpty()) eps else startTimeFormatted
                        MatchStatus.POSTPONED -> "Hoãn"
                    }

                    // Default Stats for TV view
                    val stats = MatchStats(
                        possessionHome = if (homeScore > awayScore) 56 else if (homeScore < awayScore) 44 else 50,
                        possessionAway = if (homeScore > awayScore) 44 else if (homeScore < awayScore) 56 else 50,
                        shotsOnTargetHome = (homeScore * 2 + 3).coerceAtLeast(1),
                        shotsOnTargetAway = (awayScore * 2 + 2).coerceAtLeast(1),
                        cornersHome = 5,
                        cornersAway = 4
                    )

                    matches.add(
                        Match(
                            id = eid,
                            sport = sport,
                            leagueId = leagueId,
                            leagueName = stageName,
                            leagueCountry = country,
                            leagueFlag = flag,
                            startTime = startTimeFormatted,
                            status = matchStatus,
                            statusDisplay = statusDisplay,
                            homeTeam = Team(homeId, homeName, homeAbr, getSportEmoji(sport, homeName), homeLogoUrl),
                            awayTeam = Team(awayId, awayName, awayAbr, getSportEmoji(sport, awayName), awayLogoUrl),
                            score = MatchScore(homeScore, awayScore, eps),
                            events = emptyList(),
                            stats = stats,
                            lineup = MatchLineup(),
                            isFavorite = false
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("LiveScoreApi", "Error parsing LiveScore JSON: ${e.message}", e)
        }

        // Preserve LiveScore.com's natural broadcast stage order (FA Cup, LaLiga 2, etc.)
        return matches
    }

    private fun parseIncidents(eventObj: JSONObject, homeId: String, awayId: String): List<MatchEvent> {
        val list = mutableListOf<MatchEvent>()
        val incsObj = eventObj.optJSONObject("Incs-s") ?: return list

        val keys = incsObj.keys()
        while (keys.hasNext()) {
            val periodKey = keys.next()
            val periodArray = incsObj.optJSONArray(periodKey) ?: continue
            for (k in 0 until periodArray.length()) {
                val inc = periodArray.getJSONObject(k)
                val min = inc.optInt("Min", 0)
                val pn = inc.optString("Pn", "Player")
                val itType = inc.optInt("IT", 0)
                val teamNum = inc.optInt("Nm", 1) // 1 = Home, 2 = Away
                val eventTeamId = if (teamNum == 1) homeId else awayId

                val scArr = inc.optJSONArray("Sc")
                val scoreAt = if (scArr != null && scArr.length() >= 2) {
                    "${scArr.getInt(0)} - ${scArr.getInt(1)}"
                } else null

                val eventType = when (itType) {
                    36, 37 -> EventType.GOAL
                    38 -> EventType.PENALTY_GOAL
                    43 -> EventType.YELLOW_CARD
                    45 -> EventType.RED_CARD
                    40, 41 -> EventType.SUBSTITUTION
                    else -> EventType.GOAL
                }

                list.add(
                    MatchEvent(
                        id = UUID.randomUUID().toString(),
                        minute = "$min'",
                        type = eventType,
                        teamId = eventTeamId,
                        playerName = pn,
                        detail = when (eventType) {
                            EventType.GOAL -> "Bàn thắng"
                            EventType.PENALTY_GOAL -> "Penalty"
                            EventType.YELLOW_CARD -> "Thẻ vàng"
                            EventType.RED_CARD -> "Thẻ đỏ"
                            EventType.SUBSTITUTION -> "Thay người"
                            else -> "Sự kiện"
                        },
                        scoreAtEvent = scoreAt
                    )
                )
            }
        }
        return list.sortedBy { it.minute.replace("'", "").toIntOrNull() ?: 0 }
    }

    private fun formatStartTime(esd: Long): String {
        if (esd == 0L) return "19:00"
        return try {
            val str = esd.toString()
            val pattern = if (str.length >= 14) "yyyyMMddHHmmss" else "yyyyMMddHHmm"
            val utcFormat = SimpleDateFormat(pattern, Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val date = utcFormat.parse(str)
            if (date != null) {
                val localFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                return localFormat.format(date)
            }
            val hour = str.substring(8, 10)
            val min = str.substring(10, 12)
            "$hour:$min"
        } catch (e: Exception) {
            "19:00"
        }
    }

    private fun getCountryFlag(country: String, sport: SportType): String {
        val lower = country.lowercase()
        return when {
            lower.contains("england") -> "🏴󠁧󠁢󠁥󠁮󠁧󠁿"
            lower.contains("spain") -> "🇪🇸"
            lower.contains("germany") -> "🇩🇪"
            lower.contains("italy") -> "🇮🇹"
            lower.contains("france") -> "🇫🇷"
            lower.contains("colombia") -> "🇨🇴"
            lower.contains("brazil") -> "🇧🇷"
            lower.contains("argentina") -> "🇦🇷"
            lower.contains("portugal") -> "🇵🇹"
            lower.contains("norway") -> "🇳🇴"
            lower.contains("romania") -> "🇷🇴"
            lower.contains("nba") || lower.contains("usa") -> "🇺🇸"
            lower.contains("vietnam") -> "🇻🇳"
            lower.contains("uefa") || lower.contains("champions") -> "⭐"
            lower.contains("fa cup") -> "🏆"
            sport == SportType.TENNIS -> "🎾"
            sport == SportType.BASKETBALL -> "🏀"
            else -> "🏆"
        }
    }

    private fun getSportEmoji(sport: SportType, name: String): String {
        val lower = name.lowercase()
        return when {
            lower.contains("arsenal") -> "🔴"
            lower.contains("chelsea") -> "🔵"
            lower.contains("real") -> "⚪"
            lower.contains("barca") -> "🔴🔵"
            lower.contains("city") -> "🩵"
            lower.contains("liverpool") -> "🔴"
            lower.contains("lakers") -> "🟡🟣"
            lower.contains("warriors") -> "🔵🟡"
            lower.contains("celtics") -> "🟢⚪"
            lower.contains("hawks") -> "🔴🟡"
            lower.contains("hornets") -> "🩵💜"
            lower.contains("cavaliers") -> "🍷🟡"
            sport == SportType.BASKETBALL -> "🏀"
            sport == SportType.TENNIS -> "🎾"
            else -> "⚽"
        }
    }
}
