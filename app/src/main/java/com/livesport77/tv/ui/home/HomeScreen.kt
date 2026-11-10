package com.livesport77.tv.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.livesport77.tv.model.Match
import com.livesport77.tv.ui.components.LeagueHeaderCard
import com.livesport77.tv.ui.components.MatchItemCard
import com.livesport77.tv.ui.components.MatchListControlBar
import com.livesport77.tv.ui.components.TvHeaderBar
import com.livesport77.tv.ui.detail.MatchDetailView
import com.livesport77.tv.ui.theme.*
import com.livesport77.tv.viewmodel.SportsViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Main Master-Detail Home Screen for LiveSport77 Android TV.
 * Benchmarked strictly on LiveScore.com UI:
 * - Grouped by League
 * - [LIVE] filter toggle
 * - Date Navigation < Today >
 * - Direct LiveScore.com API data with no local cache
 */
@Composable
fun HomeScreen(
    viewModel: SportsViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var currentTime by remember {
        mutableStateOf(SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()))
    }

    // Handle Android TV Back Button
    BackHandler(enabled = uiState.isFullscreen) {
        viewModel.setFullscreen(false)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TvBackground)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ZONE 1: TOP HEADER & SPORT SELECTOR
            TvHeaderBar(
                selectedSport = uiState.selectedSport,
                onSportSelected = { viewModel.selectSport(it) },
                currentTime = currentTime
            )

            // ZONE 2 & 3: MASTER-DETAIL SPLIT (45% Rail vs 55% Detail)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // ZONE 2: MATCH LIST RAIL
                Column(
                    modifier = Modifier
                        .weight(0.52f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(18.dp))
                        .background(TvSurface)
                        .padding(12.dp)
                ) {
                    // Header Status & Count
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (uiState.isLiveOnly) "● LIVE SCORES" else "ALL MATCHES",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (uiState.isLiveOnly) TvLiveRed else TvTextMuted,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        )
                        Text(
                            text = "${uiState.filteredMatches.size} matches",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TvFocusGold,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp
                            )
                        )
                    }

                    // LIVESCORE CONTROL BAR: [LIVE] < Today > [Calendar]
                    MatchListControlBar(
                        isLiveOnly = uiState.isLiveOnly,
                        onToggleLive = { viewModel.toggleLiveFilter() },
                        dateDisplay = uiState.dateDisplay,
                        dayOfMonth = uiState.dateDayOfMonth,
                        onPrevDate = { viewModel.prevDate() },
                        onNextDate = { viewModel.nextDate() },
                        onTodayClick = { viewModel.resetToToday() },
                        modifier = Modifier.padding(bottom = 10.dp)
                    )

                    // CONTENT LIST
                    if (uiState.isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "⚡ Đang tải trực tiếp từ LiveScore.com...",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = TvFocusGold,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = "Không lưu cache dữ liệu",
                                    style = MaterialTheme.typography.labelSmall.copy(color = TvTextMuted)
                                )
                            }
                        }
                    } else if (uiState.groupedLeagues.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (uiState.isLiveOnly) {
                                    "Hiện không có trận đấu nào đang LIVE.\nBấm nút [LIVE] để xem tất cả trận đấu."
                                } else {
                                    "Không có dữ liệu trận đấu cho ngày này."
                                },
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = TvTextMuted,
                                    textAlign = TextAlign.Center
                                )
                            )
                        }
                    } else {
                        // LAZYCOLUMN GROUPED BY LEAGUE
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            uiState.groupedLeagues.forEach { league ->
                                // League Section Header
                                item(key = "league_${league.id}") {
                                    LeagueHeaderCard(league = league)
                                }

                                // Matches belonging to this League
                                items(
                                    items = league.matches,
                                    key = { it.id }
                                ) { match ->
                                    MatchItemCard(
                                        match = match,
                                        isSelected = uiState.activeMatch?.id == match.id,
                                        isFavorite = uiState.favorites.contains(match.id),
                                        onFocused = { viewModel.setActiveMatch(match) },
                                        onSelect = { viewModel.setFullscreen(true) },
                                        onFavoriteClick = { viewModel.toggleFavorite(match.id) }
                                    )
                                }
                            }
                        }
                    }
                }

                // ZONE 3: MATCH DETAIL PANEL
                MatchDetailView(
                    match = uiState.activeMatch,
                    onFullscreenClick = { viewModel.setFullscreen(true) },
                    modifier = Modifier.weight(0.48f)
                )
            }
        }

        // FULLSCREEN BIG SCOREBOARD MODAL
        if (uiState.isFullscreen && uiState.activeMatch != null) {
            FullscreenScoreboardModal(
                match = uiState.activeMatch!!,
                onClose = { viewModel.setFullscreen(false) }
            )
        }
    }
}

/**
 * Big Scoreboard Fullscreen Modal for Living Room / Sports Cafe view.
 */
@Composable
fun FullscreenScoreboardModal(
    match: Match,
    onClose: () -> Unit
) {
    Dialog(onDismissRequest = onClose) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xE6070A0F))
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(TvSurface)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // League & Status
                Text(
                    text = "${match.leagueFlag} ${match.leagueName}",
                    style = MaterialTheme.typography.titleLarge.copy(color = TvFocusGold, fontWeight = FontWeight.Bold)
                )

                // Big Scoreboard
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = match.homeTeam.iconEmoji, fontSize = 56.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = match.homeTeam.name,
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = TvTextPrimary
                            )
                        )
                    }

                    Text(
                        text = "${match.score.home}  -  ${match.score.away}",
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = 64.sp,
                            fontWeight = FontWeight.Black,
                            color = TvFocusGold,
                            letterSpacing = 4.sp
                        )
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = match.awayTeam.iconEmoji, fontSize = 56.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = match.awayTeam.name,
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = TvTextPrimary
                            )
                        )
                    }
                }

                // Match Status Pill
                Text(
                    text = match.statusDisplay,
                    style = MaterialTheme.typography.titleMedium.copy(color = TvLiveRed, fontWeight = FontWeight.Bold)
                )

                // Back / Close Button
                Button(
                    onClick = onClose,
                    colors = ButtonDefaults.colors(
                        containerColor = TvFocusGold,
                        contentColor = Color.Black
                    ),
                    shape = ButtonDefaults.shape(RoundedCornerShape(12.dp))
                ) {
                    Text(text = "Đóng [ESC / Back]", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
