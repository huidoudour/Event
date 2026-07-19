package me.huidoudour.event.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * 根据主题色索引构建 Material3 ColorScheme
 * @param themeColor 对应 ThemeHelper 的主题色常量
 */
@Composable
fun eventColorScheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    themeColor: Int = 0
) = if (darkTheme) {
    darkColorScheme(
        primary = when (themeColor) {
            0 -> DarkPrimaryPurple
            1 -> DarkPrimaryPink
            2 -> DarkPrimaryBlue
            3 -> DarkPrimaryGreen
            4 -> DarkPrimaryOrange
            5 -> DarkPrimaryRed
            6 -> DarkPrimaryCyan
            else -> DarkPrimaryPurple
        },
        onPrimary = DarkOnPrimaryPurple,
        primaryContainer = when (themeColor) {
            0 -> DarkPrimaryContainerPurple
            1 -> DarkPrimaryContainerPink
            2 -> DarkPrimaryContainerBlue
            3 -> DarkPrimaryContainerGreen
            4 -> DarkPrimaryContainerOrange
            5 -> DarkPrimaryContainerRed
            6 -> DarkPrimaryContainerCyan
            else -> DarkPrimaryContainerPurple
        },
        onPrimaryContainer = DarkOnPrimaryPurple
    )
} else {
    lightColorScheme(
        primary = when (themeColor) {
            0 -> LightPrimaryPurple
            1 -> LightPrimaryPink
            2 -> LightPrimaryBlue
            3 -> LightPrimaryGreen
            4 -> LightPrimaryOrange
            5 -> LightPrimaryRed
            6 -> LightPrimaryCyan
            else -> LightPrimaryPurple
        },
        onPrimary = LightOnPrimaryPurple,
        primaryContainer = when (themeColor) {
            0 -> LightPrimaryContainerPurple
            1 -> LightPrimaryContainerPink
            2 -> LightPrimaryContainerBlue
            3 -> LightPrimaryContainerGreen
            4 -> LightPrimaryContainerOrange
            5 -> LightPrimaryContainerRed
            6 -> LightPrimaryContainerCyan
            else -> LightPrimaryContainerPurple
        },
        onPrimaryContainer = LightOnPrimaryPurple
    )
}

@Composable
fun EventTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    themeColor: Int = 0,
    content: @Composable () -> Unit
) {
    val colorScheme = eventColorScheme(darkTheme = darkTheme, themeColor = themeColor)

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = EventTypography,
        content = content
    )
}
