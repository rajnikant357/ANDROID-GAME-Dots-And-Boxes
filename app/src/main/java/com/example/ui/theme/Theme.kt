package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember

private val TabletopLightColorScheme = lightColorScheme(
    primary = DefaultAppTokens.brandHoney,
    onPrimary = DefaultAppTokens.ink,
    primaryContainer = DefaultAppTokens.paperShade,
    onPrimaryContainer = DefaultAppTokens.ink,
    secondary = DefaultAppTokens.brandSky,
    onSecondary = DefaultAppTokens.ink,
    secondaryContainer = DefaultAppTokens.paperShade,
    onSecondaryContainer = DefaultAppTokens.ink,
    tertiary = DefaultAppTokens.brandMint,
    onTertiary = DefaultAppTokens.ink,
    background = DefaultAppTokens.paperCream,
    onBackground = DefaultAppTokens.ink,
    surface = DefaultAppTokens.paperCream,
    onSurface = DefaultAppTokens.ink,
    surfaceVariant = DefaultAppTokens.paperShade,
    onSurfaceVariant = DefaultAppTokens.ink,
    outline = DefaultAppTokens.ink,
    outlineVariant = DefaultAppTokens.paperLine,
    error = DefaultAppTokens.brandBerry,
    onError = DefaultAppTokens.warmHighlight,
)

object StudioTheme {
    val tokens: AppTokens
        @Composable
        @ReadOnlyComposable
        get() = LocalAppTokens.current
}

@Composable
fun DotsAndBoxesTheme(
    dyslexiaFriendly: Boolean = false,
    content: @Composable () -> Unit,
) {
    val typography = remember(dyslexiaFriendly) { buildAppTypography(dyslexiaFriendly) }
    CompositionLocalProvider(LocalAppTokens provides DefaultAppTokens) {
        MaterialTheme(
            colorScheme = TabletopLightColorScheme,
            typography = typography,
            shapes = AppShapes,
            content = content,
        )
    }
}
