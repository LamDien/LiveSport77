package com.livesport77.tv.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.livesport77.tv.ui.theme.*

/**
 * Control Bar above Match List strictly benchmarked on LiveScore.com UI:
 * [ LIVE Pill ]  <  Today  >  [ Calendar ]
 */
@Composable
fun MatchListControlBar(
    isLiveOnly: Boolean,
    onToggleLive: () -> Unit,
    dateDisplay: String,
    dayOfMonth: String,
    onPrevDate: () -> Unit,
    onNextDate: () -> Unit,
    onTodayClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF13161F))
            .border(1.dp, Color(0xFF232A3B), RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // 1. [LIVE] TOGGLE PILL
        TvFilterPill(
            text = "LIVE",
            isActive = isLiveOnly,
            showDot = true,
            onClick = onToggleLive
        )

        // 2. DATE NAVIGATION: < Today >
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TvArrowButton(text = "‹", onClick = onPrevDate)

            Text(
                text = dateDisplay,
                style = MaterialTheme.typography.titleMedium.copy(
                    color = TvTextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            )

            TvArrowButton(text = "›", onClick = onNextDate)
        }

        // 3. CALENDAR BADGE [10]
        TvCalendarBadge(day = dayOfMonth, onClick = onTodayClick)
    }
}

@Composable
private fun TvFilterPill(
    text: String,
    isActive: Boolean,
    showDot: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val bg = when {
        isFocused -> TvFocusGold
        isActive -> Color(0xFFE11D48)
        else -> Color(0xFF1E2330)
    }

    val textColor = when {
        isFocused -> Color.Black
        isActive -> Color.White
        else -> Color(0xFFE2E8F0)
    }

    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(bg)
            .border(
                width = if (isFocused) 2.dp else 1.dp,
                color = if (isFocused) TvFocusGold else if (isActive) Color(0xFFE11D48) else Color(0xFF2E384D),
                shape = CircleShape
            )
            .focusable(interactionSource = interactionSource)
            .clickable(interactionSource = interactionSource, indication = null) { onClick() }
            .padding(horizontal = 12.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        if (showDot) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(if (isFocused) Color.Black else if (isActive) Color.White else Color(0xFFE11D48))
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                color = textColor,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
        )
    }
}

@Composable
private fun TvArrowButton(
    text: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(if (isFocused) TvFocusGold else Color(0xFF1E2330))
            .border(
                width = if (isFocused) 2.dp else 1.dp,
                color = if (isFocused) TvFocusGold else Color(0xFF2A3345),
                shape = CircleShape
            )
            .focusable(interactionSource = interactionSource)
            .clickable(interactionSource = interactionSource, indication = null) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium.copy(
                color = if (isFocused) Color.Black else TvTextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp
            )
        )
    }
}

@Composable
private fun TvCalendarBadge(
    day: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(RoundedCornerShape(7.dp))
            .background(if (isFocused) TvFocusGold else Color(0xFF1E2330))
            .border(
                width = if (isFocused) 2.dp else 1.dp,
                color = if (isFocused) TvFocusGold else Color(0xFF333E56),
                shape = RoundedCornerShape(7.dp)
            )
            .focusable(interactionSource = interactionSource)
            .clickable(interactionSource = interactionSource, indication = null) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = day,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = if (isFocused) Color.Black else TvTextPrimary,
                    fontWeight = FontWeight.Black,
                    fontSize = 11.sp
                )
            )
        }
    }
}
