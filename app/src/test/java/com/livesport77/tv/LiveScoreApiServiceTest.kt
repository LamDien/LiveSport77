package com.livesport77.tv

import com.livesport77.tv.data.api.LiveScoreApiService
import com.livesport77.tv.model.EventType
import com.livesport77.tv.model.MatchStatus
import com.livesport77.tv.model.SportType
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.IOException
import java.util.TimeZone

class LiveScoreApiServiceTest {

    private lateinit var apiService: LiveScoreApiService
    private lateinit var originalTimeZone: TimeZone

    @Before
    fun setUp() {
        originalTimeZone = TimeZone.getDefault()
        apiService = LiveScoreApiService()
    }

    @After
    fun tearDown() {
        TimeZone.setDefault(originalTimeZone)
    }

    @Test
    fun getTimezoneOffsetParam_alwaysReturnsHanoiUtcPlus7RegardlessOfDeviceTimezone() {
        TimeZone.setDefault(TimeZone.getTimeZone("America/New_York"))
        assertEquals("7.00", apiService.getTimezoneOffsetParam())
    }

    @Test
    fun formatStartTime_doesNotDoubleApplyTimezoneOffsetForHanoiEsd() {
        // LiveScore /date/.../7.00 already returns Esd shifted to UTC+7 (19:15 Hanoi)
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"))
        assertEquals("19:15", apiService.formatStartTime(20261127191500L))
        assertEquals("00:45", apiService.formatStartTime(20261127004500L))
    }

    @Test
    fun parseIncidents_extractsRealPlayerNamesFromNestedIncsAndAvoidsPlayerFallback() {
        val scoreboardJson = """
            {
              "Incs-s": {
                "2": [
                  {
                    "Min": 75,
                    "Nm": 1,
                    "Pn": "J. Olaïtan",
                    "Fn": "Junior",
                    "Ln": "Olaitan",
                    "IT": 36,
                    "Sc": [1, 0]
                  },
                  {
                    "Min": 90,
                    "Nm": 1,
                    "Sc": [2, 0],
                    "Incs": [
                      {
                        "Min": 90,
                        "Nm": 1,
                        "Fn": "Orkun",
                        "Ln": "Kökçü",
                        "Pn": "O. Kökçü",
                        "IT": 36,
                        "Sc": [2, 0]
                      },
                      {
                        "Min": 90,
                        "Nm": 1,
                        "Fn": "Dušan",
                        "Ln": "Vlahović",
                        "Pn": "D. Vlahović",
                        "IT": 63,
                        "Sc": [2, 0]
                      }
                    ]
                  },
                  {
                    "MinEx": 3,
                    "Min": 90,
                    "Nm": 2,
                    "Sc": [2, 1],
                    "Incs": [
                      {
                        "MinEx": 3,
                        "Min": 90,
                        "Nm": 2,
                        "Pn": "J. East",
                        "IT": 36,
                        "Sc": [2, 1]
                      },
                      {
                        "MinEx": 3,
                        "Min": 90,
                        "Nm": 2,
                        "Pn": "João Victor",
                        "IT": 63,
                        "Sc": [2, 1]
                      }
                    ]
                  }
                ]
              }
            }
        """.trimIndent()

        val events = apiService.parseIncidents(JSONObject(scoreboardJson), "home1", "away1")

        assertEquals(3, events.size)
        assertFalse("Must never show generic 'Player'", events.any { it.playerName == "Player" })
        assertEquals("J. Olaïtan", events[0].playerName)
        assertEquals("75'", events[0].minute)
        assertEquals("O. Kökçü", events[1].playerName)
        assertEquals("90'", events[1].minute)
        assertTrue(events[1].detail?.contains("D. Vlahović") == true)
        assertEquals("J. East", events[2].playerName)
        assertEquals("90+3'", events[2].minute)
        assertEquals("away1", events[2].teamId)
    }

    @Test
    fun parseIncidents_fallsBackToFirstAndLastNameWhenPnIsMissing() {
        val scoreboardJson = """
            {
              "Incs-s": {
                "1": [
                  {
                    "Min": 23,
                    "Nm": 2,
                    "Fn": "Bafode",
                    "Ln": "Diakite",
                    "IT": 45
                  }
                ]
              }
            }
        """.trimIndent()

        val events = apiService.parseIncidents(JSONObject(scoreboardJson), "home1", "away1")
        assertEquals(1, events.size)
        assertEquals("Bafode Diakite", events[0].playerName)
        assertEquals(EventType.RED_CARD, events[0].type)
    }

    @Test
    fun parseLiveScoreJson_parsesScheduledMatchWithHanoiKickoffTime() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"))
        val payload = """
            {
              "Stages": [
                {
                  "Cnm": "Saudi Arabia",
                  "Snm": "Saudi Professional League",
                  "Scd": "saudi-pro",
                  "Events": [
                    {
                      "Eid": "1845704",
                      "Esd": 20261127191500,
                      "Eps": "NS",
                      "Esid": 1,
                      "T1": [{"ID": "h1", "Nm": "Al Kholood", "Abr": "KHO", "Img": "k.png"}],
                      "T2": [{"ID": "a1", "Nm": "Al Khaleej", "Abr": "KHA", "Img": "a.png"}]
                    }
                  ]
                }
              ]
            }
        """.trimIndent()

        val matches = apiService.parseLiveScoreJson(payload, SportType.FOOTBALL)
        assertEquals(1, matches.size)
        val match = matches.first()
        assertEquals(MatchStatus.SCHEDULED, match.status)
        assertEquals("19:15", match.startTime)
        assertEquals("19:15", match.statusDisplay)
    }

    @Test
    fun securityValidation_dateAndMatchIdRejectPathTraversalAndInjection() {
        assertTrue(apiService.isValidDateParam("20261127"))
        assertFalse(apiService.isValidDateParam("../live"))
        assertFalse(apiService.isValidDateParam("2026-11-27"))
        assertFalse(apiService.isValidDateParam("20261127/../../admin"))

        assertTrue(apiService.isValidMatchId("1845704"))
        assertTrue(apiService.isValidMatchId("match_abc-123"))
        assertFalse(apiService.isValidMatchId(""))
        assertFalse(apiService.isValidMatchId("../other"))
        assertFalse(apiService.isValidMatchId("1845704?foo=bar"))
    }

    @Test
    fun securityValidation_buildSafeLogoUrlRejectsMaliciousPaths() {
        assertEquals(
            "https://lsm-static-prod.livescore.com/medium/enet/1234.png",
            apiService.buildSafeLogoUrl("enet/1234.png")
        )
        assertNull(apiService.buildSafeLogoUrl(""))
        assertNull(apiService.buildSafeLogoUrl("../../etc/passwd"))
        assertNull(apiService.buildSafeLogoUrl("http://evil.example/a.png"))
        assertNull(apiService.buildSafeLogoUrl("https://evil.example/a.png"))
        assertNull(apiService.buildSafeLogoUrl("user@evil.example/a.png"))
    }

    @Test(expected = IOException::class)
    fun securityValidation_readStreamBoundedRejectsOversizedPayloads() {
        val oversized = ByteArrayInputStream("A".repeat(128).toByteArray())
        apiService.readStreamBounded(oversized, maxBytes = 64)
    }

    @Test
    fun securityValidation_readStreamBoundedReadsWithinLimit() {
        val payload = """{"Stages":[]}"""
        val result = apiService.readStreamBounded(ByteArrayInputStream(payload.toByteArray()), maxBytes = 64)
        assertEquals(payload, result)
    }

    @Test
    fun parseLiveScoreJson_keepsDistinctLeaguesWithSharedScdOrSameCountrySeparate() {
        val payload = """
            {
              "Stages": [
                {
                  "Sid": "26268",
                  "Cnm": "Europa League",
                  "Ccd": "europa-league",
                  "Snm": "League Stage",
                  "Scd": "league-stage",
                  "Events": [
                    {
                      "Eid": "e1",
                      "Esd": 20261127200000,
                      "Eps": "NS",
                      "Esid": 1,
                      "T1": [{"ID": "h1", "Nm": "Roma", "Abr": "ROM"}],
                      "T2": [{"ID": "a1", "Nm": "Porto", "Abr": "POR"}]
                    }
                  ]
                },
                {
                  "Sid": "26269",
                  "Cnm": "Conference League",
                  "Ccd": "conference-league",
                  "Snm": "League Stage",
                  "Scd": "league-stage",
                  "Events": [
                    {
                      "Eid": "e2",
                      "Esd": 20261127200000,
                      "Eps": "NS",
                      "Esid": 1,
                      "T1": [{"ID": "h2", "Nm": "Chelsea", "Abr": "CHE"}],
                      "T2": [{"ID": "a2", "Nm": "Betis", "Abr": "BET"}]
                    }
                  ]
                },
                {
                  "Sid": "25616",
                  "Cnm": "England",
                  "Ccd": "england",
                  "Snm": "League 1",
                  "Scd": "league-1",
                  "Events": [
                    {
                      "Eid": "e3",
                      "Esd": 20261127200000,
                      "Eps": "NS",
                      "Esid": 1,
                      "T1": [{"ID": "h3", "Nm": "Bolton", "Abr": "BOL"}],
                      "T2": [{"ID": "a3", "Nm": "Wigan", "Abr": "WIG"}]
                    }
                  ]
                }
              ]
            }
        """.trimIndent()

        val matches = apiService.parseLiveScoreJson(payload, SportType.FOOTBALL)
        assertEquals(3, matches.size)
        assertEquals("26268", matches[0].leagueId)
        assertEquals("europa-league", matches[0].leagueCategoryCode)
        assertEquals("league-stage", matches[0].leagueStageCode)
        assertEquals("26269", matches[1].leagueId)
        assertEquals("conference-league", matches[1].leagueCategoryCode)
        assertEquals("25616", matches[2].leagueId)
        assertEquals(3, matches.map { it.leagueId }.toSet().size)
    }
}
