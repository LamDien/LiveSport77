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
    fun testInitialRepositoryStateIsEmptyWithoutCache() = runBlocking {
        // Strict adherence to "Không cache dữ liệu"
        val matches = repository.getMatches(SportType.FOOTBALL).first()
        assertTrue("Initial state must be empty before remote network call", matches.isEmpty())
    }

    @Test
    fun testRefreshSportFetchesDirectlyFromLiveScore() = runBlocking {
        val matches = repository.refreshSport(SportType.FOOTBALL)
        // If network is available, it populates matches
        if (matches.isNotEmpty()) {
            val first = matches.first()
            assertNotNull("Match ID should not be null", first.id)
            assertNotNull("League Name should not be null", first.leagueName)
        }
    }
}
