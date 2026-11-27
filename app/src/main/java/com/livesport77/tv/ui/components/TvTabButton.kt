package com.livesport77.tv.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.livesport77.tv.ui.theme.*

/**
 * Sport Selector Tab Button supporting both Android TV D-Pad and Tablet Touch.
 */
@Composable
fun TvTabButton(
    title: String,
    icon: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val isPressed by interactionSource.collectIsPressedAsState()
    val isHighlighted = isFocused || isPressed

    val scale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.96f
            isFocused -> 1.05f
            else -> 1.0f
        },
        animationSpec = tween(120),
        label = "tv_tab_scale"
    )

    val shape = CircleShape

    val bgColor = when {
        isHighlighted && isSelected -> Color.White
        isHighlighted -> Color(0xFF2A3142)
        isSelected -> Color.White
        else -> Color(0xFF1A1E29)
    }

    val textColor = when {
        isSelected -> Color(0xFF0F1115)
        isHighlighted -> TvFocusGold
        else -> Color(0xFFE2E8F0)
    }

    val borderColor = when {
        isHighlighted -> TvFocusGold
        isSelected -> Color.White
        else -> Color(0xFF2E3648)
    }

    Box(
        modifier = modifier
            .scale(scale)
            .padding(horizontal = 3.dp)
            .defaultMinSize(minHeight = 36.dp)
            .clip(shape)
            .background(bgColor)
            .border(
                width = if (isHighlighted) 2.dp else 1.dp,
                color = borderColor,
                shape = shape
            )
            .focusable(interactionSource = interactionSource)
            .clickable(interactionSource = interactionSource, indication = null) { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$icon  $title",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 13.sp,
                    fontWeight = if (isSelected || isHighlighted) FontWeight.Bold else FontWeight.SemiBold,
                    color = textColor
                ),
                maxLines = 1,
                softWrap = false
            )
        }
    }
}
