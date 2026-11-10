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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.livesport77.tv.model.Match
import com.livesport77.tv.ui.components.MatchItemCard
import com.livesport77.tv.ui.components.TvHeaderBar
import com.livesport77.tv.ui.detail.MatchDetailView
import com.livesport77.tv.ui.theme.*
import com.livesport77.tv.viewmodel.SportsViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Main Master-Detail Home Screen for LiveSport77 Android TV.
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
            .padding(18.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ZONE 1: TOP HEADER & SPORT SELECTOR
            TvHeaderBar(
                selectedSport = uiState.selectedSport,
                onSportSelected = { viewModel.selectSport(it) },
                currentTime = currentTime
            )

            // ZONE 2 & 3: MASTER-DETAIL SPLIT (36% Rail vs 64% Detail)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // ZONE 2: MATCH LIST RAIL (LazyColumn with View Recycling for RAM < 60MB)
                Column(
                    modifier = Modifier
                        .weight(0.36f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(18.dp))
                        .background(TvSurface)
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "DANH SÁCH TRẬN ĐẤU",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TvTextMuted,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        )
                        Text(
                            text = "${uiState.matches.size} trận",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TvFocusGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        )
                    }

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(
                            items = uiState.matches,
                            key = { it.id } // Preserves node identity, avoids focus flicker on data update
                        ) { match ->
                            MatchItemCard(
                                match = match,
                                isSelected = uiState.activeMatch?.id == match.id,
                                onFocused = { viewModel.setActiveMatch(match) },
                                onSelect = { viewModel.setFullscreen(true) }
                            )
                        }
                    }
                }

                // ZONE 3: MATCH DETAIL PANEL
                MatchDetailView(
                    match = uiState.activeMatch,
                    onFullscreenClick = { viewModel.setFullscreen(true) },
                    modifier = Modifier.weight(0.64f)
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

                Text(
                    text = "Trạng thái: ${match.statusDisplay}",
                    style = MaterialTheme.typography.titleLarge.copy(color = TvNeonGreen, fontWeight = FontWeight.SemiBold)
                )

                Button(
                    onClick = onClose,
                    colors = ButtonDefaults.colors(
                        containerColor = TvSurfaceFocused,
                        contentColor = TvTextPrimary
                    ),
                    shape = ButtonDefaults.shape(RoundedCornerShape(12.dp))
                ) {
                    Text(
                        text = "Bấm [Back / Esc] để quay lại",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            }
        }
    }
}
