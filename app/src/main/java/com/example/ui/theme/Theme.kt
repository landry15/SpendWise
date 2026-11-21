package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.data.preferences.ThemeMode

private val SpendWiseLightColorScheme = lightColorScheme(
    primary = EmeraldPrimary,
    onPrimary = EmeraldOnPrimary,
    primaryContainer = EmeraldPrimaryContainer,
    onPrimaryContainer = EmeraldOnPrimaryContainer,
    secondary = GoldSecondary,
    onSecondary = GoldOnSecondary,
    secondaryContainer = GoldSecondaryContainer,
    onSecondaryContainer = GoldOnSecondaryContainer,
    tertiary = SlateTertiary,
    onTertiary = SlateOnTertiary,
    tertiaryContainer = SlateTertiaryContainer,
    onTertiaryContainer = SlateOnTertiaryContainer,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    surfaceContainerLow = LightSurfaceContainerLow,
    surfaceContainerHigh = LightSurfaceContainerHigh,
    outline = LightOutline,
    outlineVariant = LightOutlineVariant,
    error = ErrorRed,
    onError = Color.White,
    errorContainer = ErrorContainerLight,
    onErrorContainer = OnErrorContainerLight
)

private val SpendWiseDarkColorScheme = darkColorScheme(
    primary = DarkEmeraldPrimary,
    onPrimary = DarkEmeraldOnPrimary,
    primaryContainer = DarkEmeraldPrimaryContainer,
    onPrimaryContainer = DarkEmeraldOnPrimaryContainer,
    secondary = DarkGoldSecondary,
    onSecondary = DarkGoldOnSecondary,
    secondaryContainer = DarkGoldSecondaryContainer,
    onSecondaryContainer = DarkGoldOnSecondaryContainer,
    tertiary = DarkSlateTertiary,
    onTertiary = DarkSlateOnTertiary,
    tertiaryContainer = DarkSlateTertiaryContainer,
    onTertiaryContainer = DarkSlateOnTertiaryContainer,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    surfaceContainerLow = DarkSurfaceContainerLow,
    surfaceContainerHigh = DarkSurfaceContainerHigh,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,
    error = DarkErrorRed,
    onError = Color(0xFF690005),
    errorContainer = ErrorContainerDark,
    onErrorContainer = OnErrorContainerDark
)

val SpendWiseShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun SpendWiseTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val colorScheme = if (isDark) SpendWiseDarkColorScheme else SpendWiseLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = SpendWiseTypography,
        shapes = SpendWiseShapes,
        content = content
    )
}
