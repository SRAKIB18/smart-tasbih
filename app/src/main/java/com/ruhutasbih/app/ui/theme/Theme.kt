package com.ruhutasbih.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = CosmicPrimaryDark,
    onPrimary = CosmicOnPrimaryDark,
    primaryContainer = CosmicContainerDark,
    onPrimaryContainer = CosmicOnContainerDark,
    secondary = CelestialGoldSecondaryDark,
    onSecondary = CelestialGoldOnSecondaryDark,
    secondaryContainer = CelestialGoldContainerDark,
    onSecondaryContainer = CelestialGoldOnContainerDark,
    background = SurfaceDark,
    onBackground = OnSurfaceDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark
)

private val LightColorScheme = lightColorScheme(
    primary = CosmicPrimaryLight,
    onPrimary = CosmicOnPrimaryLight,
    primaryContainer = CosmicContainerLight,
    onPrimaryContainer = CosmicOnContainerLight,
    secondary = CelestialGoldSecondaryLight,
    onSecondary = CelestialGoldOnSecondaryLight,
    secondaryContainer = CelestialGoldContainerLight,
    onSecondaryContainer = CelestialGoldOnContainerLight,
    background = SurfaceLight,
    onBackground = OnSurfaceLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight
)

@Composable
fun SmartTasbihTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}