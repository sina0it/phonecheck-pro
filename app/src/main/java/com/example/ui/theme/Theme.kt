package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

enum class AccentStyle(val title: String, val color: Color, val glow: Color) {
    CYAN("Cyan Sapphire", CyanAccent, CyanAccentGlow),
    EMERALD("Emerald Green", EmeraldAccent, Color(0x6600E676)),
    GOLD("Imperial Gold", GoldAccent, Color(0x66FFD700)),
    VIOLET("Electric Violet", VioletAccent, Color(0x66B388FF))
}

val LocalAccentColor = compositionLocalOf { CyanAccent }
val LocalAccentGlow = compositionLocalOf { CyanAccentGlow }

private val LuxuryDarkColorScheme = darkColorScheme(
    primary = CyanAccent,
    onPrimary = ObsidianBlack,
    primaryContainer = DarkSurfaceElevated,
    onPrimaryContainer = PureWhite,
    secondary = EmeraldAccent,
    onSecondary = ObsidianBlack,
    tertiary = GoldAccent,
    background = ObsidianBlack,
    onBackground = TextPrimary,
    surface = DeepMidnight,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurface,
    onSurfaceVariant = TextSecondary,
    outline = DarkCardBorder,
    outlineVariant = DarkCardBorderGlow,
    error = StatusCriticalRed,
    onError = PureWhite
)

private val LuxuryLightColorScheme = lightColorScheme(
    primary = Color(0xFF0284C7),
    onPrimary = PureWhite,
    primaryContainer = Color(0xFFE0F2FE),
    onPrimaryContainer = Color(0xFF0369A1),
    secondary = Color(0xFF059669),
    onSecondary = PureWhite,
    tertiary = Color(0xFFD97706),
    background = LightBackground,
    onBackground = Color(0xFF0F172A),
    surface = LightSurface,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = LightSurfaceElevated,
    onSurfaceVariant = Color(0xFF475569),
    outline = LightCardBorder,
    outlineVariant = Color(0xFFCBD5E1),
    error = StatusCriticalRed,
    onError = PureWhite
)

@Composable
fun PhoneCheckProTheme(
    darkTheme: Boolean = true,
    accent: AccentStyle = AccentStyle.CYAN,
    content: @Composable () -> Unit
) {
    val baseScheme = if (darkTheme) LuxuryDarkColorScheme else LuxuryLightColorScheme
    val colorScheme = baseScheme.copy(
        primary = accent.color,
        primaryContainer = if (darkTheme) DarkSurfaceElevated else Color(0xFFF1F5F9)
    )

    CompositionLocalProvider(
        LocalAccentColor provides accent.color,
        LocalAccentGlow provides accent.glow
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
