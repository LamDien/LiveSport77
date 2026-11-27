package com.livesport77.tv

import com.livesport77.tv.model.*
import com.livesport77.tv.repository.SportsRepository
import com.livesport77.tv.viewmodel.SportsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SportsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private class FakeSportsRepository(
        private val matchesBySport: Map<SportType, List<Match>>,
        private val incidentsByMatchId: Map<String, List<MatchEvent>> = emptyMap(),
        private val standingsByKey: Map<String, List<LeagueStandingRow>> = emptyMap()
    ) : SportsRepository {
        val refreshedCalls = mutableListOf<Pair<SportType, String>>()
        val incidentCalls = mutableListOf<String>()

        override fun getMatches(sport: SportType): Flow<List<Match>> =
            flowOf(matchesBySport[sport].orEmpty())

        override suspend fun refreshSport(sport: SportType, dateStr: String): List<Match> {
            refreshedCalls.add(sport to dateStr)
            return matchesBySport[sport].orEmpty()
        }

        override suspend fun fetchMatchIncidents(sport: SportType, match: Match): List<MatchEvent> {
            incidentCalls.add(match.id)
            return incidentsByMatchId[match.id].orEmpty()
        }

        override suspend fun fetchLeagueStandings(
            sport: SportType,
            categoryCode: String,
            stageCode: String
        ): List<LeagueStandingRow> = standingsByKey["$categoryCode/$stageCode"].orEmpty()

        override fun getMatchDetail(matchId: String): Flow<Match?> = flowOf(null)
    }

    private fun sampleMatch(
        id: String,
        sport: SportType = SportType.FOOTBALL,
        leagueId: String = "epl",
        leagueName: String = "Premier League",
        status: MatchStatus = MatchStatus.LIVE
    ): Match = Match(
        id = id,
        sport = sport,
        leagueId = leagueId,
        leagueName = leagueName,
        leagueCountry = "England",
        leagueFlag = "🏴󠁧󠁢󠁥󠁮󠁧󠁿",
        startTime = "20:00",
        status = status,
        statusDisplay = if (status == MatchStatus.LIVE) "65'" else "20:00",
        homeTeam = Team("h_$id", "Home $id", "H$id", "⚽"),
        awayTeam = Team("a_$id", "Away $id", "A$id", "⚽"),
        score = MatchScore(1, 0, "65'")
    )

    @Test
    fun initialLoad_groupsByLeagueAndFetchesIncidentsForFirstMatch() = runTest(testDispatcher) {
        val m1 = sampleMatch("1", leagueId = "epl", leagueName = "Premier League", status = MatchStatus.LIVE)
        val m2 = sampleMatch("2", leagueId = "epl", leagueName = "Premier League", status = MatchStatus.SCHEDULED)
        val m3 = sampleMatch("3", leagueId = "laliga", leagueName = "La Liga", status = MatchStatus.FINISHED)
        val goal = MatchEvent("ev1", "12'", EventType.GOAL, "h_1", "B. Saka", "Bàn thắng", "1 - 0")

        val fakeRepo = FakeSportsRepository(
            matchesBySport = mapOf(SportType.FOOTBALL to listOf(m1, m2, m3)),
            incidentsByMatchId = mapOf("1" to listOf(goal))
        )

        val vm = SportsViewModel(repository = fakeRepo, enableAutoRefresh = false)
        advanceUntilIdle()

        val state = vm.uiState.value
        assertFalse(state.isLoading)
        assertEquals("Today", state.dateDisplay)
        assertEquals(3, state.matches.size)
        assertEquals(2, state.groupedLeagues.size)
        assertEquals("Premier League", state.groupedLeagues[0].name)
        assertEquals(2, state.groupedLeagues[0].matches.size)
        assertEquals("1", state.activeMatch?.id)
        assertEquals(listOf(goal), state.activeMatch?.events)
        assertEquals(listOf("1"), fakeRepo.incidentCalls)
    }

    @Test
    fun toggleLiveFilter_filtersOnlyLiveMatchesAndUpdatesActiveMatch() = runTest(testDispatcher) {
        val scheduled = sampleMatch("s1", status = MatchStatus.SCHEDULED)
        val live = sampleMatch("l1", status = MatchStatus.LIVE)
        val fakeRepo = FakeSportsRepository(mapOf(SportType.FOOTBALL to listOf(scheduled, live)))

        val vm = SportsViewModel(repository = fakeRepo, enableAutoRefresh = false)
        advanceUntilIdle()
        assertEquals("s1", vm.uiState.value.activeMatch?.id)

        vm.toggleLiveFilter()
        val liveState = vm.uiState.value
        assertTrue(liveState.isLiveOnly)
        assertEquals(1, liveState.filteredMatches.size)
        assertEquals("l1", liveState.filteredMatches.first().id)
        assertEquals("l1", liveState.activeMatch?.id)

        vm.toggleLiveFilter()
        assertFalse(vm.uiState.value.isLiveOnly)
        assertEquals(2, vm.uiState.value.filteredMatches.size)
    }

    @Test
    fun dateNavigation_updatesOffsetAndLabelsCorrectly() = runTest(testDispatcher) {
        val fakeRepo = FakeSportsRepository(emptyMap())
        val vm = SportsViewModel(repository = fakeRepo, enableAutoRefresh = false)
        advanceUntilIdle()

        assertEquals(0, vm.uiState.value.dateOffset)
        assertEquals("Today", vm.uiState.value.dateDisplay)

        vm.prevDate()
        advanceUntilIdle()
        assertEquals(-1, vm.uiState.value.dateOffset)
        assertEquals("Yesterday", vm.uiState.value.dateDisplay)

        vm.nextDate()
        vm.nextDate()
        advanceUntilIdle()
        assertEquals(1, vm.uiState.value.dateOffset)
        assertEquals("Tomorrow", vm.uiState.value.dateDisplay)

        vm.resetToToday()
        advanceUntilIdle()
        assertEquals(0, vm.uiState.value.dateOffset)
        assertEquals("Today", vm.uiState.value.dateDisplay)
    }

    @Test
    fun selectSport_switchesSportAndLoadsNewMatches() = runTest(testDispatcher) {
        val bball = sampleMatch("b1", sport = SportType.BASKETBALL, leagueId = "nba", leagueName = "NBA")
        val fakeRepo = FakeSportsRepository(
            mapOf(
                SportType.FOOTBALL to listOf(sampleMatch("f1")),
                SportType.BASKETBALL to listOf(bball)
            )
        )

        val vm = SportsViewModel(repository = fakeRepo, enableAutoRefresh = false)
        advanceUntilIdle()

        vm.selectSport(SportType.BASKETBALL)
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(SportType.BASKETBALL, state.selectedSport)
        assertEquals(1, state.matches.size)
        assertEquals("b1", state.activeMatch?.id)
    }

    @Test
    fun toggleFavorite_andFullscreenAndSetActiveMatch_updateStateCleanly() = runTest(testDispatcher) {
        val m1 = sampleMatch("1")
        val m2 = sampleMatch("2")
        val fakeRepo = FakeSportsRepository(mapOf(SportType.FOOTBALL to listOf(m1, m2)))

        val vm = SportsViewModel(repository = fakeRepo, enableAutoRefresh = false)
        advanceUntilIdle()

        vm.toggleFavorite("2")
        assertTrue(vm.uiState.value.favorites.contains("2"))
        vm.toggleFavorite("2")
        assertFalse(vm.uiState.value.favorites.contains("2"))

        vm.setActiveMatch(m2)
        advanceUntilIdle()
        assertNotNull(vm.uiState.value.activeMatch)
        assertEquals("2", vm.uiState.value.activeMatch?.id)
        assertTrue(fakeRepo.incidentCalls.contains("2"))

        vm.setFullscreen(true)
        assertTrue(vm.uiState.value.isFullscreen)
        vm.setFullscreen(false)
        assertFalse(vm.uiState.value.isFullscreen)
    }

    @Test
    fun openAndCloseLeagueStandings_loadsStandingsTableAndResetsOnClose() = runTest(testDispatcher) {
        val row = LeagueStandingRow(1, "t1", "Arsenal", null, 10, 8, 1, 1, 22, 7, 15, 25)
        val fakeRepo = FakeSportsRepository(
            matchesBySport = emptyMap(),
            standingsByKey = mapOf("england/premier-league" to listOf(row))
        )
        val vm = SportsViewModel(repository = fakeRepo, enableAutoRefresh = false)
        advanceUntilIdle()

        val league = LeagueGroup(
            id = "25600",
            name = "Premier League",
            country = "England",
            flag = "🏴󠁧󠁢󠁥󠁮󠁧󠁿",
            categoryCode = "england",
            stageCode = "premier-league",
            matches = emptyList()
        )
        vm.openLeagueStandings(league)
        advanceUntilIdle()

        assertEquals("25600", vm.uiState.value.selectedStandingsLeague?.id)
        assertFalse(vm.uiState.value.isLoadingStandings)
        assertEquals(listOf(row), vm.uiState.value.leagueStandings)

        vm.closeLeagueStandings()
        assertEquals(null, vm.uiState.value.selectedStandingsLeague)
        assertTrue(vm.uiState.value.leagueStandings.isEmpty())
    }
}
