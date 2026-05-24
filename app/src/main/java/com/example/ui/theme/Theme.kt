package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.graphics.Color

private val WarpColorScheme = lightColorScheme(
    primary = GeoPrimary,
    secondary = GeoSecondary,
    tertiary = GeoTextSecondary,
    background = GeoBackground,
    surface = GeoSurface,
    onPrimary = GeoOnPrimary,
    onSecondary = GeoOnSecondary,
    onBackground = GeoTextPrimary,
    onSurface = GeoTextPrimary,
    surfaceVariant = GeoBorderLight,
    outline = GeoBorderMedium
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = false, // Set false to default to Geometric Balance Light Theme
  dynamicColor: Boolean = false, // Disable dynamic colors to preserve premium brand aesthetics
  content: @Composable () -> Unit,
) {
  val colorScheme = WarpColorScheme

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
