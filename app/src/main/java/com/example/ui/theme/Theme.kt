package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
  primary = RenaultAmber,
  onPrimary = TextDarkOnLight,
  primaryContainer = RenaultAmberDark,
  onPrimaryContainer = TextHighContrast,
  secondary = FormelDBlueLight,
  onSecondary = TextHighContrast,
  secondaryContainer = IndustrialCard,
  onSecondaryContainer = TextHighContrast,
  background = IndustrialNavyDark,
  onBackground = TextHighContrast,
  surface = IndustrialSurface,
  onSurface = TextHighContrast,
  surfaceVariant = IndustrialCard,
  onSurfaceVariant = TextMediumContrast,
  error = QcErrorRed,
  onError = TextHighContrast,
  errorContainer = QcErrorBg,
  onErrorContainer = TextHighContrast,
  outline = IndustrialCardBorder
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Rugged handheld default high-contrast dark
  dynamicColor: Boolean = false, // Keep consistent industrial palette
  content: @Composable () -> Unit
) {
  MaterialTheme(
    colorScheme = DarkColorScheme,
    typography = Typography,
    content = content
  )
}
