package com.livesport77.tv.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.livesport77.tv.model.SportType
import com.livesport77.tv.ui.theme.*

/**
 * Top Navigation Bar for Android TV and Tablets.
 * Displays LiveSport77 logo, Sport Selector Tabs (no scroll needed), and Hanoi Clock.
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
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Logo (compact so all 5 sport pills fit without scrolling)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "LIVE",
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = TvFocusGold,
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp
                )
            )
            Text(
                text = "SPORT",
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = TvTextPrimary,
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp
                )
            )
            Text(
                text = "77",
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = TvNeonGreen,
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp
                )
            )
        }

        // Sports Selector Tabs (all 5 fit directly without horizontal scroll)
        Row(
            modifier = Modifier.padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
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

        // Right side: Hanoi Digital Clock
        Text(
            text = currentTime,
            style = MaterialTheme.typography.titleLarge.copy(
                color = TvTextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            ),
            maxLines = 1,
            softWrap = false
        )
    }
}
