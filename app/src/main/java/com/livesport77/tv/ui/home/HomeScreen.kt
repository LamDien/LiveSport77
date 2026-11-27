package com.livesport77.tv.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.livesport77.tv.data.api.LiveScoreApiService
import com.livesport77.tv.model.EventType
import com.livesport77.tv.model.LeagueGroup
import com.livesport77.tv.model.LeagueStandingRow
import com.livesport77.tv.model.Match
import com.livesport77.tv.model.MatchStatus
import com.livesport77.tv.ui.components.LeagueHeaderCard
import com.livesport77.tv.ui.components.MatchItemCard
import com.livesport77.tv.ui.components.MatchListControlBar
import com.livesport77.tv.ui.components.TvHeaderBar
import com.livesport77.tv.ui.detail.MatchDetailView
import com.livesport77.tv.ui.theme.*
import com.livesport77.tv.viewmodel.SportsViewModel
import kotlinx.coroutines.delay
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
        mutableStateOf(formatHanoiClock())
    }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = formatHanoiClock()
            delay(30_000L)
        }
    }

    // Handle Back Button for modals
    BackHandler(enabled = uiState.isFullscreen || uiState.selectedStandingsLeague != null) {
        if (uiState.selectedStandingsLeague != null) {
            viewModel.closeLeagueStandings()
        } else {
            viewModel.setFullscreen(false)
        }
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
                                // League Section Header (tap / OK to open standings)
                                item(key = "league_${league.id}") {
                                    LeagueHeaderCard(
                                        league = league,
                                        onClick = { viewModel.openLeagueStandings(league) }
                                    )
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
                                        onSelect = {
                                            viewModel.setActiveMatch(match)
                                            viewModel.setFullscreen(true)
                                        },
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

        // LEAGUE STANDINGS MODAL (Bảng xếp hạng)
        uiState.selectedStandingsLeague?.let { standingsLeague ->
            LeagueStandingsModal(
                league = standingsLeague,
                standings = uiState.leagueStandings,
                isLoading = uiState.isLoadingStandings,
                onClose = { viewModel.closeLeagueStandings() }
            )
        }
    }
}

private fun formatHanoiClock(): String {
    val formatter = SimpleDateFormat("HH:mm", Locale.US).apply {
        timeZone = LiveScoreApiService.HANOI_TIMEZONE
    }
    return "${formatter.format(Date())} HN"
}

/**
 * Big Scoreboard Fullscreen Modal for Living Room / Tablet / Sports Cafe view.
 * Uses usePlatformDefaultWidth = false so the modal renders cleanly without cramped wrapping.
 */
@Composable
fun FullscreenScoreboardModal(
    match: Match,
    onClose: () -> Unit
) {
    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xEB070A0F))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClose
                )
                .padding(horizontal = 24.dp, vertical = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.88f)
                    .widthIn(max = 960.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(TvSurface)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {}
                    )
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 28.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // Header: League + Status Badge + Touch Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = buildString {
                            append(match.leagueFlag)
                            append(" ")
                            if (match.leagueCountry.isNotBlank()) {
                                append(match.leagueCountry)
                                append(" · ")
                            }
                            append(match.leagueName)
                        },
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = TvFocusGold,
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (match.status == MatchStatus.LIVE) TvLiveRed else TvSurfaceVariant)
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = if (match.status == MatchStatus.SCHEDULED) "${match.startTime} (Giờ HN)" else match.statusDisplay,
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            ),
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }

                // Big Scoreboard Row with balanced weights so long team names never break layout
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(TvSurfaceVariant)
                        .padding(horizontal = 20.dp, vertical = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Home Team
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        FullscreenTeamCrest(
                            logoUrl = match.homeTeam.logoUrl,
                            emoji = match.homeTeam.iconEmoji,
                            name = match.homeTeam.name
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = match.homeTeam.name,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = TvTextPrimary
                            ),
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Chủ nhà",
                            style = MaterialTheme.typography.labelSmall.copy(color = TvTextMuted)
                        )
                    }

                    // Center Score / VS
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (match.status == MatchStatus.SCHEDULED) "VS" else "${match.score.home} - ${match.score.away}",
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontSize = if (match.status == MatchStatus.SCHEDULED) 38.sp else 52.sp,
                                fontWeight = FontWeight.Black,
                                color = TvFocusGold,
                                letterSpacing = 2.sp
                            ),
                            maxLines = 1,
                            softWrap = false
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = when (match.status) {
                                MatchStatus.SCHEDULED -> "Khởi tranh ${match.startTime} (HN)"
                                MatchStatus.LIVE -> "Đang diễn ra · ${match.statusDisplay}"
                                MatchStatus.FINISHED -> "Kết thúc (${match.statusDisplay})"
                                MatchStatus.POSTPONED -> "Tạm hoãn"
                            },
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = if (match.status == MatchStatus.LIVE) TvLiveRed else TvTextMuted,
                                fontWeight = FontWeight.SemiBold
                            ),
                            maxLines = 1,
                            softWrap = false
                        )
                    }

                    // Away Team
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        FullscreenTeamCrest(
                            logoUrl = match.awayTeam.logoUrl,
                            emoji = match.awayTeam.iconEmoji,
                            name = match.awayTeam.name
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = match.awayTeam.name,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = TvTextPrimary
                            ),
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Đội khách",
                            style = MaterialTheme.typography.labelSmall.copy(color = TvTextMuted)
                        )
                    }
                }

                // Key Match Events Preview (with real player names)
                if (match.events.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(TvSurfaceVariant)
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "⏱️ DIỄN BIẾN NỔI BẬT",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TvFocusGold,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        match.events.take(5).forEach { event ->
                            val icon = when (event.type) {
                                EventType.GOAL, EventType.PENALTY_GOAL -> "⚽"
                                EventType.YELLOW_CARD -> "🟨"
                                EventType.RED_CARD -> "🟥"
                                EventType.SUBSTITUTION -> "🔄"
                                EventType.POINT -> "🏀"
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "$icon ${event.minute}  ${event.playerName}" +
                                        (event.detail?.takeIf { it.contains("Kiến tạo") }?.let { " ($it)" } ?: ""),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = TvTextPrimary,
                                        fontSize = 13.sp
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                event.scoreAtEvent?.let { scoreText ->
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = scoreText,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = TvFocusGold,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }
                        }
                    }
                }

                // Lineups Section (only for sports that have team lineups, e.g. Football — never Tennis)
                if (match.sport.supportsLineups) {
                    val isPreMatch = match.status == MatchStatus.SCHEDULED || match.status == MatchStatus.POSTPONED
                    val lineupTitle = if (isPreMatch) "👥 ĐỘI HÌNH DỰ KIẾN" else "👥 ĐỘI HÌNH THI ĐẤU"
                    val lineup = match.lineup
                    val hasPlayers = lineup != null && (lineup.homeStarting.isNotEmpty() || lineup.awayStarting.isNotEmpty())

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(TvSurfaceVariant)
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = lineupTitle,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TvNeonGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        )

                        if (!hasPlayers) {
                            Text(
                                text = if (isPreMatch) {
                                    "Chưa công bố đội hình dự kiến. Đội hình ra sân thường được cập nhật trước giờ thi đấu 45–60 phút."
                                } else {
                                    "Chưa có dữ liệu đội hình cho trận đấu này."
                                },
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TvTextMuted,
                                    fontSize = 12.sp
                                )
                            )
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                LineupTeamColumn(
                                    teamName = match.homeTeam.name,
                                    formation = lineup!!.homeFormation,
                                    players = lineup.homeStarting,
                                    coach = lineup.homeCoach,
                                    modifier = Modifier.weight(1f)
                                )
                                LineupTeamColumn(
                                    teamName = match.awayTeam.name,
                                    formation = lineup.awayFormation,
                                    players = lineup.awayStarting,
                                    coach = lineup.awayCoach,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                // Touch + D-Pad OK Close Button (replaces androidx.tv.material3.Button for reliable touch taps)
                val okInteraction = remember { MutableInteractionSource() }
                val okFocused by okInteraction.collectIsFocusedAsState()
                val okPressed by okInteraction.collectIsPressedAsState()
                Box(
                    modifier = Modifier
                        .defaultMinSize(minWidth = 140.dp, minHeight = 44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (okPressed) TvFocusGold.copy(alpha = 0.8f) else TvFocusGold)
                        .border(
                            width = if (okFocused) 2.dp else 0.dp,
                            color = if (okFocused) Color.White else Color.Transparent,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .focusable(interactionSource = okInteraction)
                        .clickable(interactionSource = okInteraction, indication = null) { onClose() }
                        .padding(horizontal = 32.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "OK",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color.Black,
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun LineupTeamColumn(
    teamName: String,
    formation: String,
    players: List<String>,
    coach: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(TvSurface)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = teamName,
                style = MaterialTheme.typography.labelMedium.copy(
                    color = TvFocusGold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            if (formation.isNotBlank()) {
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = formation,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TvNeonGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    ),
                    maxLines = 1,
                    softWrap = false
                )
            }
        }

        if (players.isEmpty()) {
            Text(
                text = "Chưa có danh sách cầu thủ",
                style = MaterialTheme.typography.bodySmall.copy(color = TvTextMuted, fontSize = 11.sp)
            )
        } else {
            players.forEach { player ->
                Text(
                    text = player,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TvTextPrimary,
                        fontSize = 12.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        if (coach.isNotBlank()) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "HLV: $coach",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = TvTextMuted,
                    fontSize = 10.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun FullscreenTeamCrest(
    logoUrl: String?,
    emoji: String,
    name: String
) {
    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(CircleShape)
            .background(Color(0xFF262C3A)),
        contentAlignment = Alignment.Center
    ) {
        if (!logoUrl.isNullOrEmpty()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(logoUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = name,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
            )
        } else {
            Text(text = emoji, fontSize = 36.sp)
        }
    }
}

/**
 * League Standings Modal (Bảng xếp hạng) matching LiveScore.com layout.
 * Works seamlessly with both Tablet touch and Android TV D-Pad.
 */
@Composable
fun LeagueStandingsModal(
    league: LeagueGroup,
    standings: List<LeagueStandingRow>,
    isLoading: Boolean,
    onClose: () -> Unit
) {
    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xEB070A0F))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClose
                )
                .padding(horizontal = 24.dp, vertical = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.90f)
                    .widthIn(max = 920.dp)
                    .fillMaxHeight(0.88f)
                    .clip(RoundedCornerShape(22.dp))
                    .background(TvSurface)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {}
                    )
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = buildString {
                            append(league.flag)
                            append(" ")
                            if (league.country.isNotBlank() && !league.name.equals(league.country, ignoreCase = true)) {
                                append(league.country)
                                append(" · ")
                            }
                            append(league.name)
                        },
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = TvFocusGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "BẢNG XẾP HẠNG",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = TvNeonGreen,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                // Table Column Headers
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(TvSurfaceVariant)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("#", modifier = Modifier.width(30.dp), style = MaterialTheme.typography.labelSmall.copy(color = TvTextMuted, fontWeight = FontWeight.Bold))
                    Text("Đội bóng", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelSmall.copy(color = TvTextMuted, fontWeight = FontWeight.Bold))
                    Text("Tr", modifier = Modifier.width(34.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall.copy(color = TvTextMuted, fontWeight = FontWeight.Bold))
                    Text("T", modifier = Modifier.width(34.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall.copy(color = TvTextMuted, fontWeight = FontWeight.Bold))
                    Text("H", modifier = Modifier.width(34.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall.copy(color = TvTextMuted, fontWeight = FontWeight.Bold))
                    Text("B", modifier = Modifier.width(34.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall.copy(color = TvTextMuted, fontWeight = FontWeight.Bold))
                    Text("HS", modifier = Modifier.width(42.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall.copy(color = TvTextMuted, fontWeight = FontWeight.Bold))
                    Text("Đ", modifier = Modifier.width(42.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall.copy(color = TvFocusGold, fontWeight = FontWeight.Black))
                }

                // Content
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    when {
                        isLoading -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    text = "⚡ Đang tải bảng xếp hạng...",
                                    style = MaterialTheme.typography.titleMedium.copy(color = TvFocusGold, fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                        standings.isEmpty() -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    text = "Giải đấu này chưa có bảng xếp hạng (ví dụ: cúp loại trực tiếp / giao hữu).",
                                    style = MaterialTheme.typography.bodyMedium.copy(color = TvTextMuted, textAlign = TextAlign.Center)
                                )
                            }
                        }
                        else -> {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(items = standings, key = { "${it.rank}_${it.teamId}" }) { row ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (row.rank <= 4) Color(0xFF172230) else TvSurfaceVariant.copy(alpha = 0.65f))
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${row.rank}",
                                            modifier = Modifier.width(30.dp),
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                color = if (row.rank <= 4) TvNeonGreen else TvTextPrimary,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                        Row(
                                            modifier = Modifier.weight(1f),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            if (!row.teamLogoUrl.isNullOrEmpty()) {
                                                AsyncImage(
                                                    model = ImageRequest.Builder(LocalContext.current)
                                                        .data(row.teamLogoUrl)
                                                        .crossfade(true)
                                                        .build(),
                                                    contentDescription = row.teamName,
                                                    contentScale = ContentScale.Fit,
                                                    modifier = Modifier.size(20.dp).clip(CircleShape)
                                                )
                                            }
                                            Text(
                                                text = row.teamName,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    color = TvTextPrimary,
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 13.sp
                                                ),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        Text("${row.played}", modifier = Modifier.width(34.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall.copy(color = TvTextPrimary))
                                        Text("${row.wins}", modifier = Modifier.width(34.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall.copy(color = TvTextMuted))
                                        Text("${row.draws}", modifier = Modifier.width(34.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall.copy(color = TvTextMuted))
                                        Text("${row.losses}", modifier = Modifier.width(34.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall.copy(color = TvTextMuted))
                                        Text(
                                            text = if (row.goalDiff > 0) "+${row.goalDiff}" else "${row.goalDiff}",
                                            modifier = Modifier.width(42.dp),
                                            textAlign = TextAlign.Center,
                                            style = MaterialTheme.typography.bodySmall.copy(color = TvTextMuted)
                                        )
                                        Text(
                                            text = "${row.points}",
                                            modifier = Modifier.width(42.dp),
                                            textAlign = TextAlign.Center,
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                color = TvFocusGold,
                                                fontWeight = FontWeight.Black
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Bottom Touch + D-Pad OK Button
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    val okInteraction = remember { MutableInteractionSource() }
                    val okFocused by okInteraction.collectIsFocusedAsState()
                    val okPressed by okInteraction.collectIsPressedAsState()
                    Box(
                        modifier = Modifier
                            .defaultMinSize(minWidth = 140.dp, minHeight = 44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (okPressed) TvFocusGold.copy(alpha = 0.8f) else TvFocusGold)
                            .border(
                                width = if (okFocused) 2.dp else 0.dp,
                                color = if (okFocused) Color.White else Color.Transparent,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .focusable(interactionSource = okInteraction)
                            .clickable(interactionSource = okInteraction, indication = null) { onClose() }
                            .padding(horizontal = 32.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "OK",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Color.Black,
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp
                            )
                        )
                    }
                }
            }
        }
    }
}
