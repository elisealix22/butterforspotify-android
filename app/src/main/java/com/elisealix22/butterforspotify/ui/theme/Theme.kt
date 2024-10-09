package com.elisealix22.butterforspotify.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = ThemeColor.AlmostBlack,
    onPrimary = ThemeColor.White,
    background = ThemeColor.White,
    surface = ThemeColor.White,
    surfaceContainer = ThemeColor.BottomNavBackgroundLight,
    secondaryContainer = ThemeColor.BottomNavSelectedLight,
    onSecondaryContainer = ThemeColor.AlmostBlack
)

private val DarkColorScheme = darkColorScheme(
    primary = ThemeColor.White,
    onPrimary = ThemeColor.AlmostBlack,
    background = ThemeColor.AlmostBlack,
    surface = ThemeColor.AlmostBlack,
    surfaceContainer = ThemeColor.BottomNavBackgroundDark,
    secondaryContainer = ThemeColor.BottomNavSelectedDark,
    onSecondaryContainer = ThemeColor.White
)

@Composable
fun ButterForSpotifyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
