package com.livesport77.tv.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.livesport77.tv.model.LeagueGroup
import com.livesport77.tv.ui.theme.TvFocusGold
import com.livesport77.tv.ui.theme.TvSurfaceFocused
import com.livesport77.tv.ui.theme.TvTextMuted
import com.livesport77.tv.ui.theme.TvTextPrimary

/**
 * Interactive League Section Header matching LiveScore.com UI.
 * Tapping or pressing OK opens the league standings table.
 */
@Composable
fun LeagueHeaderCard(
    league: LeagueGroup,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val isPressed by interactionSource.collectIsPressedAsState()
    val isHighlighted = isFocused || isPressed

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (isHighlighted) TvSurfaceFocused else Color.Transparent)
            .border(
                width = if (isHighlighted) 1.5.dp else 0.dp,
                color = if (isHighlighted) TvFocusGold else Color.Transparent,
                shape = RoundedCornerShape(10.dp)
            )
            .focusable(interactionSource = interactionSource)
            .clickable(interactionSource = interactionSource, indication = null) { onClick() }
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = league.flag,
                fontSize = 18.sp
            )

            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(
                    text = league.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = if (isHighlighted) TvFocusGold else TvTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (league.country.isNotEmpty() && !league.name.equals(league.country, ignoreCase = true)) {
                    Text(
                        text = league.country,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TvTextMuted,
                            fontSize = 11.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // Standings indicator chevron
        Text(
            text = "BXH ›",
            style = MaterialTheme.typography.labelSmall.copy(
                color = if (isHighlighted) TvFocusGold else TvTextMuted,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
        )
    }
}

