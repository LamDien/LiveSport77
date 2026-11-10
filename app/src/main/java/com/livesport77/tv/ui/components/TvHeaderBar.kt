package com.livesport77.tv.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.livesport77.tv.model.SportType
import com.livesport77.tv.ui.theme.*

/**
 * Top Navigation Bar for Android TV.
 * Displays LiveSport77 logo, LiveScore.com live connection pill, Sport Selector Tabs, and Clock.
 */
@Composable
fun TvHeaderBar(
    selectedSport: SportType,
    onSportSelected: (SportType) -> Unit,
    currentTime: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(TvSurface)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Logo & LiveScore Connection Badge
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row {
                Text(
                    text = "LIVE",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = TvFocusGold,
                        fontWeight = FontWeight.Black,
                        fontSize = 22.sp
                    )
                )
                Text(
                    text = "SPORT",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = TvTextPrimary,
                        fontWeight = FontWeight.Black,
                        fontSize = 22.sp
                    )
                )
                Text(
                    text = "77",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = TvNeonGreen,
                        fontWeight = FontWeight.Black,
                        fontSize = 22.sp
                    )
                )
            }

            // LiveScore.com Live Connected Pill
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF0F231D))
                    .padding(horizontal = 7.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(TvNeonGreen)
                )
                Text(
                    text = "LIVESCORE.COM LIVE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TvNeonGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                )
            }
        }

        // Sports Selector Tabs
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SportType.values().forEach { sport ->
                TvTabButton(
                    title = sport.displayName,
                    icon = sport.icon,
                    isSelected = selectedSport == sport,
                    onClick = { onSportSelected(sport) }
                )
            }
        }

        // Right side: Remote Hint Pill & Digital Clock
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF1E2433))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "🎮 Remote",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TvFocusGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Text(
                text = currentTime,
                style = MaterialTheme.typography.titleLarge.copy(
                    color = TvTextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                ),
                maxLines = 1,
                softWrap = false
            )
        }
    }
}
