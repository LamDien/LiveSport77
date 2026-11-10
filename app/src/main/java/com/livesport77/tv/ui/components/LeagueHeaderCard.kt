package com.livesport77.tv.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.livesport77.tv.model.LeagueGroup
import com.livesport77.tv.ui.theme.TvFocusGold
import com.livesport77.tv.ui.theme.TvTextMuted
import com.livesport77.tv.ui.theme.TvTextPrimary

/**
 * League Section Header matching LiveScore.com UI.
 * Displays League/Country Icon, League Name, Subtitle, and Chevron indicator.
 */
@Composable
fun LeagueHeaderCard(
    league: LeagueGroup,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            // Flag or League Icon
            Text(
                text = league.flag,
                fontSize = 18.sp
            )

            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(
                    text = league.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = TvTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    ),
                    maxLines = 1
                )
                if (league.country.isNotEmpty() && !league.name.equals(league.country, ignoreCase = true)) {
                    Text(
                        text = league.country,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TvTextMuted,
                            fontSize = 11.sp
                        ),
                        maxLines = 1
                    )
                }
            }
        }

        // Chevron Right
        Text(
            text = "›",
            style = MaterialTheme.typography.titleLarge.copy(
                color = TvTextMuted,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        )
    }
}

