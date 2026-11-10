package com.livesport77.tv.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.livesport77.tv.model.Match
import com.livesport77.tv.model.MatchStatus
import com.livesport77.tv.ui.theme.*

/**
 * Match Item Card for Left Rail in Android TV Master-Detail Layout.
 * Synchronizes with the Detail Panel when focused by remote D-Pad.
 */
@Composable
fun MatchItemCard(
    match: Match,
    isSelected: Boolean,
    onFocused: () -> Unit,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    TvFocusCard(
        onClick = onSelect,
        modifier = modifier.fillMaxWidth()
    ) { isFocused ->
        // When remote D-Pad focuses on this match, notify the ViewModel
        LaunchedEffect(isFocused) {
            if (isFocused) {
                onFocused()
            }
        }

        // Top Row: League & Status Badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${match.leagueFlag} ${match.leagueName}",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = if (isFocused) TvFocusGold else TvTextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                maxLines = 1
            )

            // Status Badge
            val (badgeBg, badgeText) = when (match.status) {
                MatchStatus.LIVE -> TvLiveRed to match.statusDisplay
                MatchStatus.FINISHED -> TvSurfaceFocused to match.statusDisplay
                MatchStatus.SCHEDULED -> Color(0xFF1E3A8A) to match.statusDisplay
                MatchStatus.POSTPONED -> Color(0xFF475569) to "Hoãn"
            }

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(badgeBg)
                    .padding(horizontal = 6.dp, vertical = 2.dp),
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
                    text = badgeText,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Bottom Row: Teams and Scores
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Team Names
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = match.homeTeam.iconEmoji, fontSize = 14.sp)
                    Text(
                        text = match.homeTeam.name,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TvTextPrimary
                        ),
                        maxLines = 1
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = match.awayTeam.iconEmoji, fontSize = 14.sp)
                    Text(
                        text = match.awayTeam.name,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TvTextPrimary
                        ),
                        maxLines = 1
                    )
                }
            }

            // Scores
            if (match.status == MatchStatus.SCHEDULED) {
                Text(
                    text = "- vs -",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TvTextMuted
                    )
                )
            } else {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = match.score.home.toString(),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isFocused) TvFocusGold else TvTextPrimary
                        )
                    )
                    Text(
                        text = match.score.away.toString(),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isFocused) TvFocusGold else TvTextPrimary
                        )
                    )
                }
            }
        }
    }
}

