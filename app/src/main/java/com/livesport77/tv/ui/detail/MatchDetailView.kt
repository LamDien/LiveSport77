package com.livesport77.tv.ui.detail

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.livesport77.tv.model.*
import com.livesport77.tv.ui.theme.*

/**
 * Right-hand Match Detail Panel for Android TV Master-Detail Layout.
 * Renders high-contrast live stats, timeline, and lineup.
 */
@Composable
fun MatchDetailView(
    match: Match?,
    onFullscreenClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (match == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(18.dp))
                .background(TvSurface)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Chọn một trận đấu để xem chi tiết",
                style = MaterialTheme.typography.bodyLarge.copy(color = TvTextMuted)
            )
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(18.dp))
            .background(TvSurface)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // TOP SCOREBOARD HEADER (tap to open fullscreen on tablets)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(TvSurfaceVariant)
                .clickable { onFullscreenClick() }
                .padding(16.dp)
        ) {
            // League and Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${match.leagueFlag} ${match.leagueName}",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = TvFocusGold,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                )

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (match.status == MatchStatus.LIVE) TvLiveRed else TvSurfaceFocused)
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (match.status == MatchStatus.LIVE) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                        )
                    }
                    Text(
                        text = match.statusDisplay,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Teams & Big Score
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Home Team
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    DetailTeamLogo(
                        logoUrl = match.homeTeam.logoUrl,
                        emoji = match.homeTeam.iconEmoji,
                        name = match.homeTeam.name
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = match.homeTeam.name,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TvTextPrimary
                        ),
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(text = "Chủ nhà", style = MaterialTheme.typography.labelSmall)
                }

                // Big Score Display
                Column(
                    modifier = Modifier.padding(horizontal = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (match.status == MatchStatus.SCHEDULED) "VS" else "${match.score.home} - ${match.score.away}",
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Black,
                            color = TvFocusGold,
                            letterSpacing = 2.sp
                        ),
                        maxLines = 1,
                        softWrap = false
                    )
                    Text(
                        text = if (match.status == MatchStatus.SCHEDULED) "${match.startTime} (HN)" else match.statusDisplay,
                        style = MaterialTheme.typography.labelSmall.copy(color = TvTextMuted),
                        maxLines = 1,
                        softWrap = false
                    )
                }

                // Away Team
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    DetailTeamLogo(
                        logoUrl = match.awayTeam.logoUrl,
                        emoji = match.awayTeam.iconEmoji,
                        name = match.awayTeam.name
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = match.awayTeam.name,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TvTextPrimary
                        ),
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(text = "Đội khách", style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        // MIDDLE CONTENT: TIMELINE & STATS SPLIT
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // LEFT HALF: TIMELINE EVENTS
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(12.dp))
                    .background(TvSurfaceVariant)
                    .padding(14.dp)
            ) {
                Text(
                    text = "⏱️ DIỄN BIẾN TRẬN ĐẤU",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TvFocusGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                if (match.events.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Chưa có sự kiện nào được ghi nhận",
                            style = MaterialTheme.typography.bodyMedium.copy(color = TvTextMuted, fontSize = 12.sp)
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(match.events) { event ->
                            val icon = when (event.type) {
                                EventType.GOAL, EventType.PENALTY_GOAL -> "⚽"
                                EventType.YELLOW_CARD -> "🟨"
                                EventType.RED_CARD -> "🟥"
                                EventType.SUBSTITUTION -> "🔄"
                                EventType.POINT -> "🏀"
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(TvSurface)
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "$icon ${event.minute}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = TvFocusGold,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = event.playerName,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                color = TvTextPrimary,
                                                fontWeight = FontWeight.Medium,
                                                fontSize = 13.sp
                                            ),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        event.detail?.takeIf { it.contains("Kiến tạo") || it.contains("Thay ") }?.let { subDetail ->
                                            val compactDetail = subDetail.substringAfter("· ", subDetail)
                                            Text(
                                                text = compactDetail,
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
                                event.scoreAtEvent?.let {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = it,
                                        style = MaterialTheme.typography.labelSmall.copy(color = TvTextMuted),
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // RIGHT HALF: STATS BARS (only shown for LIVE / FINISHED matches with real stats)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(12.dp))
                    .background(TvSurfaceVariant)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "📊 THỐNG KÊ TRỰC TIẾP",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TvNeonGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                )

                val isNotStarted = match.status == MatchStatus.SCHEDULED || match.status == MatchStatus.POSTPONED
                val stats = if (isNotStarted) null else match.stats

                if (isNotStarted) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = if (match.status == MatchStatus.POSTPONED) "Trận đấu tạm hoãn" else "Trận đấu chưa diễn ra",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    color = TvTextPrimary,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                            )
                            Text(
                                text = "Thống kê trực tiếp sẽ cập nhật khi bóng lăn (${match.startTime} HN).",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TvTextMuted,
                                    fontSize = 11.sp,
                                    textAlign = TextAlign.Center
                                )
                            )
                        }
                    }
                } else if (stats == null) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Chưa có dữ liệu thống kê cho trận đấu này",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = TvTextMuted,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        )
                    }
                } else {
                    val shotsTotal = (stats.shotsOnTargetHome + stats.shotsOnTargetAway).coerceAtLeast(1)
                    val cornersTotal = (stats.cornersHome + stats.cornersAway).coerceAtLeast(1)

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (stats.possessionHome > 0 || stats.possessionAway > 0) {
                            StatBar(
                                title = "Kiểm soát bóng",
                                homeVal = "${stats.possessionHome}%",
                                awayVal = "${stats.possessionAway}%",
                                homePercent = stats.possessionHome / 100f
                            )
                        }
                        StatBar(
                            title = "Sút trúng đích",
                            homeVal = "${stats.shotsOnTargetHome}",
                            awayVal = "${stats.shotsOnTargetAway}",
                            homePercent = stats.shotsOnTargetHome.toFloat() / shotsTotal
                        )
                        StatBar(
                            title = "Phạt góc",
                            homeVal = "${stats.cornersHome}",
                            awayVal = "${stats.cornersAway}",
                            homePercent = stats.cornersHome.toFloat() / cornersTotal
                        )
                    }
                }

                // Lineup hint & Fullscreen Action Row
                val lineupSummary = match.lineup?.let { lu ->
                    if (lu.homeFormation.isNotBlank() && lu.awayFormation.isNotBlank()) {
                        "👥 Sơ đồ: ${lu.homeFormation} vs ${lu.awayFormation}"
                    } else null
                } ?: if (isNotStarted) "👥 Xem đội hình dự kiến →" else "👥 Xem đội hình thi đấu →"

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = lineupSummary,
                        style = MaterialTheme.typography.labelSmall.copy(color = TvTextMuted, fontSize = 10.sp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    val fsInteraction = remember { MutableInteractionSource() }
                    val fsFocused by fsInteraction.collectIsFocusedAsState()
                    val fsPressed by fsInteraction.collectIsPressedAsState()
                    Box(
                        modifier = Modifier
                            .defaultMinSize(minHeight = 36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (fsPressed) TvFocusGold.copy(alpha = 0.8f) else TvFocusGold)
                            .border(
                                width = if (fsFocused) 2.dp else 0.dp,
                                color = if (fsFocused) Color.White else Color.Transparent,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .focusable(interactionSource = fsInteraction)
                            .clickable(interactionSource = fsInteraction, indication = null) { onFullscreenClick() }
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Phóng to",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailTeamLogo(
    logoUrl: String?,
    emoji: String,
    name: String
) {
    Box(
        modifier = Modifier
            .size(46.dp)
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
                    .size(40.dp)
                    .clip(CircleShape)
            )
        } else {
            Text(text = emoji, fontSize = 28.sp)
        }
    }
}

@Composable
private fun StatBar(
    title: String,
    homeVal: String,
    awayVal: String,
    homePercent: Float
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = homeVal, style = MaterialTheme.typography.labelSmall.copy(color = TvTextPrimary, fontWeight = FontWeight.Bold))
            Text(text = title, style = MaterialTheme.typography.labelSmall.copy(color = TvTextMuted, fontSize = 11.sp))
            Text(text = awayVal, style = MaterialTheme.typography.labelSmall.copy(color = TvTextPrimary, fontWeight = FontWeight.Bold))
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(TvSurface)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(homePercent.coerceIn(0.05f, 0.95f))
                    .background(TvLiveRed)
            )
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight((1f - homePercent).coerceIn(0.05f, 0.95f))
                    .background(Color(0xFF3B82F6))
            )
        }
    }
}

