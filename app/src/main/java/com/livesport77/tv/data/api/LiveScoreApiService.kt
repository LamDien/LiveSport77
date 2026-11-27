package com.livesport77.tv.data.api

import android.util.Log
import com.livesport77.tv.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.*
import javax.net.ssl.HttpsURLConnection

/**
 * LiveScore.com Direct API Service.
 * Fetches real-time sports data directly from LiveScore CDN public endpoints.
 */
class LiveScoreApiService {

    companion object {
        val HANOI_TIMEZONE: TimeZone = TimeZone.getTimeZone("Asia/Ho_Chi_Minh")
        const val HANOI_TZ_OFFSET_PARAM = "7.00"
        private const val MAX_MATCHES_RESPONSE_BYTES = 5 * 1024 * 1024 // 5 MB cap
        private const val MAX_SCOREBOARD_RESPONSE_BYTES = 1 * 1024 * 1024 // 1 MB cap
        private val DATE_PARAM_REGEX = Regex("^\\d{8}$")
        private val MATCH_ID_REGEX = Regex("^[A-Za-z0-9_-]{1,64}$")
        private val SAFE_IMAGE_PATH_REGEX = Regex("^[A-Za-z0-9._/-]{1,256}$")
    }

    private val baseUrl = "https://prod-cdn-public-api.lsmedia1.com/v1/api/app"
    private val cdnImageBase = "https://lsm-static-prod.livescore.com/medium"
    private val userAgent = "Mozilla/5.0 (Linux; Android 14; Android TV) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"

    internal fun isValidDateParam(dateStr: String): Boolean = DATE_PARAM_REGEX.matches(dateStr)

    internal fun isValidMatchId(matchId: String): Boolean = MATCH_ID_REGEX.matches(matchId)

    internal fun buildSafeLogoUrl(rawImg: String): String? {
        val trimmed = rawImg.trim().trimStart('/')
        if (trimmed.isEmpty()) return null
        if (trimmed.contains("..") || trimmed.contains("://") || trimmed.contains("@") || trimmed.contains("\\")) {
            return null
        }
        if (!SAFE_IMAGE_PATH_REGEX.matches(trimmed)) return null
        return "$cdnImageBase/$trimmed"
    }

    internal fun readStreamBounded(inputStream: InputStream, maxBytes: Int): String {
        val buffer = ByteArray(8192)
        val output = ByteArrayOutputStream()
        var totalRead = 0
        inputStream.use { stream ->
            while (true) {
                val read = stream.read(buffer)
                if (read == -1) break
                totalRead += read
                if (totalRead > maxBytes) {
                    throw IOException("Response body exceeded maximum allowed size of $maxBytes bytes")
                }
                output.write(buffer, 0, read)
            }
        }
        return output.toString(StandardCharsets.UTF_8.name())
    }

    /**
     * Fetch all matches for the given sport and date directly from LiveScore.com CDN.
     * Always aligns calendar day and kickoff times to Hanoi, Vietnam (UTC+7).
     * @param dateStr Date in format yyyyMMdd (e.g., 20261127). If empty, today in Hanoi is used.
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
            if (!isValidDateParam(dateStr)) {
                Log.w("LiveScoreApi", "Rejected invalid date parameter")
                return@withContext emptyList()
            }
            dateStr
        } else {
            val hanoiFormat = SimpleDateFormat("yyyyMMdd", Locale.US).apply {
                timeZone = HANOI_TIMEZONE
            }
            hanoiFormat.format(Date())
        }

        val tzOffset = getTimezoneOffsetParam()
        val endpointUrl = "$baseUrl/date/$sportPath/$targetDate/$tzOffset"

        var connection: HttpsURLConnection? = null
        try {
            val url = URL(endpointUrl)
            if (url.protocol != "https") {
                return@withContext emptyList()
            }
            connection = (url.openConnection() as HttpsURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 8000
                readTimeout = 8000
                instanceFollowRedirects = false
                setRequestProperty("User-Agent", userAgent)
                setRequestProperty("Accept", "application/json, text/plain, */*")
            }

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val jsonString = readStreamBounded(connection.inputStream, MAX_MATCHES_RESPONSE_BYTES)
                return@withContext parseLiveScoreJson(jsonString, sport)
            } else {
                Log.w("LiveScoreApi", "Server returned HTTP ${connection.responseCode} for sport=$sportPath")
            }
        } catch (e: Exception) {
            Log.e("LiveScoreApi", "Error fetching LiveScore data: ${e.javaClass.simpleName}")
        } finally {
            connection?.disconnect()
        }

        return@withContext emptyList()
    }

    /**
     * Always return Hanoi, Vietnam timezone offset (+7.00) so LiveScore CDN
     * aligns both the match day and Esd kickoff timestamps to Asia/Ho_Chi_Minh.
     */
    fun getTimezoneOffsetParam(): String = HANOI_TZ_OFFSET_PARAM

    /**
     * Fetch live scoreboard detail & incidents (goals, cards) for a match.
     */
    suspend fun fetchScoreboard(
        sport: SportType,
        matchId: String,
        homeId: String,
        awayId: String
    ): List<MatchEvent> = withContext(Dispatchers.IO) {
        if (!isValidMatchId(matchId)) {
            Log.w("LiveScoreApi", "Rejected invalid matchId parameter")
            return@withContext emptyList()
        }

        val sportPath = when (sport) {
            SportType.FOOTBALL -> "soccer"
            SportType.HOCKEY -> "hockey"
            SportType.BASKETBALL -> "basketball"
            SportType.TENNIS -> "tennis"
            SportType.CRICKET -> "cricket"
        }
        val endpointUrl = "$baseUrl/scoreboard/$sportPath/$matchId"

        var connection: HttpsURLConnection? = null
        try {
            val url = URL(endpointUrl)
            if (url.protocol != "https") {
                return@withContext emptyList()
            }
            connection = (url.openConnection() as HttpsURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 6000
                readTimeout = 6000
                instanceFollowRedirects = false
                setRequestProperty("User-Agent", userAgent)
                setRequestProperty("Accept", "application/json, text/plain, */*")
            }

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val jsonString = readStreamBounded(connection.inputStream, MAX_SCOREBOARD_RESPONSE_BYTES)
                val eventObj = JSONObject(jsonString)
                return@withContext parseIncidents(eventObj, homeId, awayId)
            }
        } catch (e: Exception) {
            Log.w("LiveScoreApi", "Scoreboard fetch error: ${e.javaClass.simpleName}")
        } finally {
            connection?.disconnect()
        }
        emptyList()
    }

    internal fun parseLiveScoreJson(jsonString: String, sport: SportType): List<Match> {
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
                    val homeLogoUrl = buildSafeLogoUrl(homeImg)

                    // Team 2 (Away)
                    val t2Arr = event.optJSONArray("T2")
                    val t2Obj = t2Arr?.optJSONObject(0)
                    val awayName = t2Obj?.optString("Nm", "Away") ?: "Away"
                    val awayAbr = t2Obj?.optString("Abr", awayName.take(3).uppercase()) ?: "AWY"
                    val awayId = t2Obj?.optString("ID", "t2_$eid") ?: "t2_$eid"
                    val awayImg = t2Obj?.optString("Img", "") ?: ""
                    val awayLogoUrl = buildSafeLogoUrl(awayImg)

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

    internal fun parseIncidents(eventObj: JSONObject, homeId: String, awayId: String): List<MatchEvent> {
        val parsed = mutableListOf<Triple<Int, Int, MatchEvent>>()
        val incsObj = eventObj.optJSONObject("Incs-s") ?: return emptyList()

        val keys = incsObj.keys()
        while (keys.hasNext()) {
            val periodKey = keys.next()
            val periodArray = incsObj.optJSONArray(periodKey) ?: continue
            for (k in 0 until periodArray.length()) {
                val inc = periodArray.getJSONObject(k)
                val nestedIncs = inc.optJSONArray("Incs")

                // LiveScore nests assisted goals and substitutions inside a child "Incs" array
                // where the outer object omits "Pn" and "IT".
                val primaryObj: JSONObject
                var assistName: String? = null
                var subOutName: String? = null

                if (nestedIncs != null && nestedIncs.length() > 0) {
                    var chosen: JSONObject? = null
                    for (m in 0 until nestedIncs.length()) {
                        val sub = nestedIncs.optJSONObject(m) ?: continue
                        val subIt = sub.optInt("IT", 0)
                        if (subIt == 63) {
                            assistName = extractPlayerName(sub)
                        } else if (subIt == 41) {
                            subOutName = extractPlayerName(sub)
                            if (chosen == null) chosen = sub
                        } else if (chosen == null) {
                            chosen = sub
                        }
                    }
                    primaryObj = chosen ?: nestedIncs.optJSONObject(0) ?: inc
                } else {
                    primaryObj = inc
                }

                val itType = primaryObj.optInt("IT", inc.optInt("IT", 0))
                if (itType == 63) continue // Skip standalone assist-only records

                val min = primaryObj.optInt("Min", inc.optInt("Min", 0))
                val minEx = primaryObj.optInt("MinEx", inc.optInt("MinEx", 0))
                val minuteDisplay = if (minEx > 0) "$min+$minEx'" else "$min'"

                val teamNum = primaryObj.optInt("Nm", inc.optInt("Nm", 1)) // 1 = Home, 2 = Away
                val eventTeamId = if (teamNum == 1) homeId else awayId

                val scArr = primaryObj.optJSONArray("Sc") ?: inc.optJSONArray("Sc")
                val scoreAt = if (scArr != null && scArr.length() >= 2) {
                    "${scArr.getInt(0)} - ${scArr.getInt(1)}"
                } else null

                val eventType = when (itType) {
                    36, 39 -> EventType.GOAL
                    37, 38 -> EventType.PENALTY_GOAL
                    43 -> EventType.YELLOW_CARD
                    44, 45 -> EventType.RED_CARD
                    40, 41 -> EventType.SUBSTITUTION
                    else -> EventType.GOAL
                }

                val playerName = extractPlayerName(primaryObj)
                    ?: extractPlayerName(inc)
                    ?: "Cầu thủ"

                val baseDetail = when (itType) {
                    36 -> "Bàn thắng"
                    37 -> "Penalty"
                    38 -> "Hỏng penalty"
                    39 -> "Phản lưới nhà"
                    43 -> "Thẻ vàng"
                    44 -> "Thẻ vàng thứ 2"
                    45 -> "Thẻ đỏ"
                    40, 41 -> if (subOutName != null && subOutName != playerName) "Thay $subOutName" else "Thay người"
                    else -> "Sự kiện"
                }

                val detailText = if (!assistName.isNullOrBlank() && (eventType == EventType.GOAL || eventType == EventType.PENALTY_GOAL)) {
                    "$baseDetail · Kiến tạo: $assistName"
                } else {
                    baseDetail
                }

                parsed.add(
                    Triple(
                        min,
                        minEx,
                        MatchEvent(
                            id = "${periodKey}_${k}_${min}_${minEx}_${playerName}",
                            minute = minuteDisplay,
                            type = eventType,
                            teamId = eventTeamId,
                            playerName = playerName,
                            detail = detailText,
                            scoreAtEvent = scoreAt
                        )
                    )
                )
            }
        }
        return parsed
            .sortedWith(compareBy<Triple<Int, Int, MatchEvent>> { it.first }.thenBy { it.second })
            .map { it.third }
    }

    private fun extractPlayerName(obj: JSONObject): String? {
        val pn = obj.optString("Pn", "").trim()
        if (pn.isNotEmpty() && !pn.equals("null", ignoreCase = true)) return pn

        val fn = obj.optString("Fn", "").trim().takeUnless { it.equals("null", ignoreCase = true) } ?: ""
        val ln = obj.optString("Ln", "").trim().takeUnless { it.equals("null", ignoreCase = true) } ?: ""
        val fullName = "$fn $ln".trim()
        if (fullName.isNotEmpty()) return fullName

        val pnm = obj.optString("Pnm", "").trim()
        if (pnm.isNotEmpty() && !pnm.equals("null", ignoreCase = true)) return pnm

        return null
    }

    /**
     * Format Esd timestamp (yyyyMMddHHmmss) already shifted by LiveScore API to Hanoi UTC+7.
     * Parses and formats strictly in Asia/Ho_Chi_Minh to avoid double timezone offset.
     */
    internal fun formatStartTime(esd: Long): String {
        if (esd <= 0L) return "--:--"
        return try {
            val str = esd.toString()
            if (str.length < 12) return "--:--"
            val pattern = if (str.length >= 14) "yyyyMMddHHmmss" else "yyyyMMddHHmm"
            val hanoiParser = SimpleDateFormat(pattern, Locale.US).apply {
                timeZone = HANOI_TIMEZONE
            }
            val date = hanoiParser.parse(str)
            if (date != null) {
                val hanoiOutput = SimpleDateFormat("HH:mm", Locale.US).apply {
                    timeZone = HANOI_TIMEZONE
                }
                return hanoiOutput.format(date)
            }
            "${str.substring(8, 10)}:${str.substring(10, 12)}"
        } catch (e: Exception) {
            "--:--"
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
