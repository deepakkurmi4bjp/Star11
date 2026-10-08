package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Sacred Devotional Palette (Maa Narmada Holy Festival)
private val SacredLightColorScheme = lightColorScheme(
    primary = SaffronPrimary,
    onPrimary = PureWhite,
    primaryContainer = SaffronContainer,
    onPrimaryContainer = SaffronDark,
    secondary = NarmadaBlue,
    onSecondary = PureWhite,
    secondaryContainer = NarmadaBlueContainer,
    onSecondaryContainer = NarmadaBlue,
    tertiary = SacredGold,
    onTertiary = Color(0xFF1A1A1A),
    tertiaryContainer = SacredGoldLight,
    onTertiaryContainer = SaffronDark,
    background = WarmIvory,
    onBackground = Color(0xFF1A1A1A),
    surface = SurfaceCard,
    onSurface = Color(0xFF1A1A1A),               // Crisp dark text in text fields and surfaces
    surfaceVariant = Color(0xFFF7F1E7),
    onSurfaceVariant = Color(0xFF3E2723),        // Crisp dark brown for secondary labels
    surfaceContainer = PureWhite,
    surfaceContainerHigh = PureWhite,
    surfaceContainerHighest = Color(0xFFF7F1E7),
    surfaceContainerLow = WarmIvory,
    surfaceContainerLowest = PureWhite,
    inverseSurface = Color(0xFF261C14),
    inverseOnSurface = PureWhite,
    outline = Color(0xFFBCAAA4),                 // Visible crisp borders
    outlineVariant = Color(0xFFD7CCC8),
    error = StatusError,
    onError = PureWhite
)

@Composable
fun NarmadaTheme(
    darkTheme: Boolean = false, // Keep authentic sacred saffron/ivory theme consistent across all devices
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = SacredLightColorScheme,
        typography = Typography,
        content = content
    )
}
