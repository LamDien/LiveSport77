package com.livesport77.tv.ui.theme

import androidx.compose.runtime.Composable
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.darkColorScheme

@OptIn(ExperimentalTvMaterial3Api::class)
private val TvDarkColorScheme = darkColorScheme(
    primary = TvFocusGold,
    secondary = TvNeonGreen,
    background = TvBackground,
    surface = TvSurface,
    surfaceVariant = TvSurfaceVariant,
    onPrimary = TvBackground,
    onSecondary = TvBackground,
    onBackground = TvTextPrimary,
    onSurface = TvTextPrimary,
    onSurfaceVariant = TvTextMuted
)

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun LiveSport77TvTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = TvDarkColorScheme,
        typography = TvTypography,
        content = content
    )
}

