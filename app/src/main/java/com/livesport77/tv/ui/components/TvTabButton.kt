package com.livesport77.tv.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
 * TV Tab Button for Sports Selection (Football, Basketball, Tennis).
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

    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.05f else 1.0f,
        animationSpec = tween(120),
        label = "tv_tab_scale"
    )

    val shape = RoundedCornerShape(12.dp)

    Box(
        modifier = modifier
            .scale(scale)
            .padding(horizontal = 4.dp)
            .clip(shape)
            .background(
                when {
                    isFocused -> TvFocusGold
                    isSelected -> TvSurfaceFocused
                    else -> Color.Transparent
                }
            )
            .border(
                width = if (isFocused) 2.dp else 1.dp,
                color = when {
                    isFocused -> TvFocusGold
                    isSelected -> TvBorderFocused
                    else -> TvBorderSubtle
                },
                shape = shape
            )
            .focusable(interactionSource = interactionSource)
            .clickable(interactionSource = interactionSource, indication = null) { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = "$icon  $title",
            style = MaterialTheme.typography.titleLarge.copy(
                fontSize = 15.sp,
                fontWeight = if (isSelected || isFocused) FontWeight.Bold else FontWeight.Medium,
                color = when {
                    isFocused -> Color.Black
                    isSelected -> TvFocusGold
                    else -> TvTextPrimary
                }
            )
        )
    }
}
