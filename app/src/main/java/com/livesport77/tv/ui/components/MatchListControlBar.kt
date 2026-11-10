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
            .background(Color(0xFF141822))
            .padding(horizontal = 10.dp, vertical = 6.dp),
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
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TvArrowButton(text = "‹", onClick = onPrevDate)

            Text(
                text = dateDisplay,
                style = MaterialTheme.typography.titleMedium.copy(
                    color = TvTextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
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
        isActive -> TvLiveRed
        else -> Color(0xFF202738)
    }

    val textColor = when {
        isFocused -> Color.Black
        isActive -> Color.White
        else -> TvTextMuted
    }

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .border(
                width = if (isFocused) 2.dp else 1.dp,
                color = if (isFocused) TvFocusGold else if (isActive) TvLiveRed else Color(0xFF333E56),
                shape = RoundedCornerShape(14.dp)
            )
            .focusable(interactionSource = interactionSource)
            .clickable(interactionSource = interactionSource, indication = null) { onClick() }
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (showDot) {
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(if (isFocused) Color.Black else Color.White)
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
            .size(24.dp)
            .clip(CircleShape)
            .background(if (isFocused) TvFocusGold else Color(0xFF202738))
            .focusable(interactionSource = interactionSource)
            .clickable(interactionSource = interactionSource, indication = null) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium.copy(
                color = if (isFocused) Color.Black else TvTextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
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
            .size(26.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (isFocused) TvFocusGold else Color(0xFF202738))
            .border(
                width = 1.dp,
                color = if (isFocused) TvFocusGold else Color(0xFF333E56),
                shape = RoundedCornerShape(6.dp)
            )
            .focusable(interactionSource = interactionSource)
            .clickable(interactionSource = interactionSource, indication = null) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "📅 $day",
            style = MaterialTheme.typography.labelSmall.copy(
                color = if (isFocused) Color.Black else TvTextMuted,
                fontWeight = FontWeight.Bold,
                fontSize = 9.sp
            )
        )
    }
}

