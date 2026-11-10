package com.livesport77.tv.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
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
 * Reusable Focusable Card for Android TV.
 * Scales by 1.03x on D-pad Focus, renders a rich gold glow border (#FFD700).
 */
@Composable
fun TvFocusCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 14.dp,
    onClick: () -> Unit = {},
    content: @Composable ColumnScope.(isFocused: Boolean) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.03f else 1.0f,
        animationSpec = tween(durationMillis = 150),
        label = "tv_card_scale"
    )

    val shape = RoundedCornerShape(cornerRadius)

    Box(
        modifier = modifier
            .scale(scale)
            .shadow(
                elevation = if (isFocused) 16.dp else 2.dp,
                shape = shape,
                ambientColor = if (isFocused) TvFocusGold else Color.Black,
                spotColor = if (isFocused) TvFocusGold else Color.Black
            )
            .clip(shape)
            .background(if (isFocused) TvSurfaceFocused else TvSurface)
            .border(
                width = if (isFocused) 2.5.dp else 1.dp,
                color = if (isFocused) TvBorderFocused else TvBorderSubtle,
                shape = shape
            )
            .focusable(interactionSource = interactionSource)
            .clickable(interactionSource = interactionSource, indication = null) { onClick() }
            .padding(14.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            content(isFocused)
        }
    }
}
