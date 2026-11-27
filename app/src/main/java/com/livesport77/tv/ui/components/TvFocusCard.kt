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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.livesport77.tv.ui.theme.*

/**
 * Reusable Focusable & Touch-Friendly Card for Android TV and Tablets.
 * Highlights smoothly on D-Pad focus, touch press, and active selection.
 */
@Composable
fun TvFocusCard(
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    cornerRadius: Dp = 14.dp,
    contentPadding: PaddingValues = PaddingValues(14.dp),
    onClick: () -> Unit = {},
    content: @Composable ColumnScope.(isFocused: Boolean) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val isPressed by interactionSource.collectIsPressedAsState()
    val isActive = isFocused || isPressed || isSelected

    val scale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.985f
            isFocused -> 1.02f
            else -> 1.0f
        },
        animationSpec = tween(durationMillis = 120),
        label = "tv_card_scale"
    )

    val shape = RoundedCornerShape(cornerRadius)

    Box(
        modifier = modifier
            .scale(scale)
            .shadow(
                elevation = if (isFocused || isPressed) 12.dp else if (isSelected) 6.dp else 2.dp,
                shape = shape,
                ambientColor = if (isActive) TvFocusGold else Color.Black,
                spotColor = if (isActive) TvFocusGold else Color.Black
            )
            .clip(shape)
            .background(if (isActive) TvSurfaceFocused else TvSurface)
            .border(
                width = if (isFocused || isPressed) 2.dp else if (isSelected) 1.5.dp else 1.dp,
                color = when {
                    isFocused || isPressed -> TvBorderFocused
                    isSelected -> TvFocusGold.copy(alpha = 0.65f)
                    else -> TvBorderSubtle
                },
                shape = shape
            )
            .focusable(interactionSource = interactionSource)
            .clickable(interactionSource = interactionSource, indication = null) { onClick() }
            .padding(contentPadding)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            content(isFocused || isPressed)
        }
    }
}
