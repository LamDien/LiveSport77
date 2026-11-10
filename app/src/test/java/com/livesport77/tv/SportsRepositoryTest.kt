package com.livesport77.tv

import com.livesport77.tv.model.MatchStatus
import com.livesport77.tv.model.SportType
import com.livesport77.tv.repository.SportsRepositoryImpl
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class SportsRepositoryTest {

    private lateinit var repository: SportsRepositoryImpl

    @Before
    fun setUp() {
        repository = SportsRepositoryImpl()
    }

    @Test
    fun testGetFootballMatchesReturnsValidList() = runBlocking {
        val matches = repository.getMatches(SportType.FOOTBALL).first()
        assertTrue("Football matches should not be empty", matches.isNotEmpty())
        
        val liveMatch = matches.find { it.status == MatchStatus.LIVE }
        assertNotNull("Should contain at least one LIVE match", liveMatch)
        assertEquals("Arsenal", liveMatch?.homeTeam?.name)
    }

    @Test
    fun testGetBasketballMatchesReturnsNBA() = runBlocking {
        val matches = repository.getMatches(SportType.BASKETBALL).first()
        assertTrue("Basketball matches should not be empty", matches.isNotEmpty())
        assertTrue("Should contain NBA league", matches.any { it.leagueId == "nba" })
    }

    @Test
    fun testSimulateScoreUpdateIncrementsMinute() = runBlocking {
        val initialMatches = repository.getMatches(SportType.FOOTBALL).first()
        val initialArsenal = initialMatches.find { it.id == "fb_01" }
        assertNotNull(initialArsenal)

        repository.simulateScoreUpdate()

        val updatedMatches = repository.getMatches(SportType.FOOTBALL).first()
        val updatedArsenal = updatedMatches.find { it.id == "fb_01" }
        assertNotNull(updatedArsenal)
        assertEquals("80' LIVE", updatedArsenal?.statusDisplay)
    }
}

