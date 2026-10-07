package com.omersusin.mochi.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Warm dawn world: cream ground, one burnt-orange accent. Chosen from the
// use scene (half-asleep mornings, soft light), not from category habit.
private val Cream = Color(0xFFFFF8F1)
private val CreamContainer = Color(0xFFFBEFE3)
private val Ember = Color(0xFF9C4300)
private val EmberContainer = Color(0xFFFFD9C0)
private val Ink = Color(0xFF201A15)
private val InkSoft = Color(0xFF5C4B40)

private val Cocoa = Color(0xFF1A120C)
private val CocoaContainer = Color(0xFF2A1D13)
private val Dawn = Color(0xFFFFB68F)
private val DawnContainer = Color(0xFF5A2E00)

private val LightScheme = lightColorScheme(
    primary = Ember,
    onPrimary = Color.White,
    primaryContainer = EmberContainer,
    onPrimaryContainer = Color(0xFF3A1C00),
    secondary = InkSoft,
    surface = Cream,
    onSurface = Ink,
    onSurfaceVariant = InkSoft,
    surfaceContainer = CreamContainer,
    outline = Color(0xFFD8C4B2),
)

private val DarkScheme = darkColorScheme(
    primary = Dawn,
    onPrimary = Color(0xFF3A1C00),
    primaryContainer = DawnContainer,
    onPrimaryContainer = Color(0xFFFFDCC6),
    surface = Cocoa,
    onSurface = Color(0xFFF5E9DD),
    onSurfaceVariant = Color(0xFFCDBFAC),
    surfaceContainer = CocoaContainer,
    outline = Color(0xFF4A382C),
)

@Composable
fun MochiTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkScheme else LightScheme,
        content = content,
    )
}
