package com.livesport77.tv.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
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
        // TOP SCOREBOARD HEADER
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(TvSurfaceVariant)
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
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Home Team
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = match.homeTeam.iconEmoji, fontSize = 32.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = match.homeTeam.name,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TvTextPrimary
                        )
                    )
                    Text(text = "Chủ nhà", style = MaterialTheme.typography.labelSmall)
                }

                // Big Score Display
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (match.status == MatchStatus.SCHEDULED) "- vs -" else "${match.score.home} - ${match.score.away}",
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontSize = 42.sp,
                            fontWeight = FontWeight.Black,
                            color = TvFocusGold,
                            letterSpacing = 2.sp
                        )
                    )
                    Text(
                        text = match.score.periodInfo,
                        style = MaterialTheme.typography.labelSmall.copy(color = TvTextMuted)
                    )
                }

                // Away Team
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = match.awayTeam.iconEmoji, fontSize = 32.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = match.awayTeam.name,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TvTextPrimary
                        )
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
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "$icon ${event.minute}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = TvFocusGold,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Text(
                                        text = event.playerName,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = TvTextPrimary,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 13.sp
                                        )
                                    )
                                }
                                event.scoreAtEvent?.let {
                                    Text(
                                        text = it,
                                        style = MaterialTheme.typography.labelSmall.copy(color = TvTextMuted)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // RIGHT HALF: STATS BARS
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

                val stats = match.stats ?: MatchStats()

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatBar(title = "Kiểm soát bóng", homeVal = "${stats.possessionHome}%", awayVal = "${stats.possessionAway}%", homePercent = stats.possessionHome / 100f)
                    StatBar(title = "Sút trúng đích", homeVal = "${stats.shotsOnTargetHome}", awayVal = "${stats.shotsOnTargetAway}", homePercent = 0.65f)
                    StatBar(title = "Phạt góc", homeVal = "${stats.cornersHome}", awayVal = "${stats.cornersAway}", homePercent = 0.70f)
                }

                // Lineup & Fullscreen Action Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "👥 Sơ đồ: ${match.lineup?.homeFormation ?: "4-3-3"} vs ${match.lineup?.awayFormation ?: "4-2-3-1"}",
                        style = MaterialTheme.typography.labelSmall.copy(color = TvTextMuted, fontSize = 10.sp),
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    Button(
                        onClick = onFullscreenClick,
                        colors = ButtonDefaults.colors(
                            containerColor = TvFocusGold,
                            contentColor = Color.Black
                        ),
                        shape = ButtonDefaults.shape(RoundedCornerShape(8.dp)),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Bấm [OK] Phóng To",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        )
                    }
                }
            }
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

