package me.huidoudour.event.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * 根据主题色索引构建完整 MD3 ColorScheme。
 * - 索引 0：跟随系统壁纸动态取色 (Material You)，API<31 回退到紫色
 * - 索引 1~6：使用预定义种子色构建 primary / secondary / tertiary 三组 token
 * surface / background / outline 等中性 token 使用 lightColorScheme/darkColorScheme 默认值
 *
 * @param darkTheme 是否深色主题
 * @param themeColor [me.huidoudour.event.util.ThemeHelper] 主题色常量 (0=系统色, 1~6=手动色)
 */
@Composable
fun eventColorScheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    themeColor: Int = 0
): androidx.compose.material3.ColorScheme {
    // 索引 0：跟随系统壁纸取色
    if (themeColor == 0 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val context = LocalContext.current
        return if (darkTheme) dynamicDarkColorScheme(context)
        else dynamicLightColorScheme(context)
    }
    // 手工主题色（含 API<31 的回退）
    val tokens = themeTokens(themeColor, darkTheme)
    val seed = themeSeedColor(themeColor)
    return if (darkTheme) {
        darkColorScheme(
            primary = tokens.primary,
            onPrimary = tokens.onPrimary,
            primaryContainer = tokens.primaryContainer,
            onPrimaryContainer = tokens.onPrimaryContainer,
            secondary = tokens.secondary,
            onSecondary = tokens.onSecondary,
            secondaryContainer = tokens.secondaryContainer,
            onSecondaryContainer = tokens.onSecondaryContainer,
            tertiary = tokens.tertiary,
            onTertiary = tokens.onTertiary,
            tertiaryContainer = tokens.tertiaryContainer,
            onTertiaryContainer = tokens.onTertiaryContainer,
            surface = lerp(Color(0xFF1C1B1F), seed, 0.08f),
            surfaceContainerLowest = lerp(Color(0xFF1C1B1F), seed, 0.06f),
            surfaceContainerLow = lerp(Color(0xFF1C1B1F), seed, 0.12f),
            surfaceContainer = lerp(Color(0xFF1C1B1F), seed, 0.15f),
            surfaceContainerHigh = lerp(Color(0xFF1C1B1F), seed, 0.18f),
            surfaceContainerHighest = lerp(Color(0xFF1C1B1F), seed, 0.22f),
            background = lerp(Color(0xFF1C1B1F), seed, 0.06f),
        )
    } else {
        lightColorScheme(
            primary = tokens.primary,
            onPrimary = tokens.onPrimary,
            primaryContainer = tokens.primaryContainer,
            onPrimaryContainer = tokens.onPrimaryContainer,
            secondary = tokens.secondary,
            onSecondary = tokens.onSecondary,
            secondaryContainer = tokens.secondaryContainer,
            onSecondaryContainer = tokens.onSecondaryContainer,
            tertiary = tokens.tertiary,
            onTertiary = tokens.onTertiary,
            tertiaryContainer = tokens.tertiaryContainer,
            onTertiaryContainer = tokens.onTertiaryContainer,
            surface = lerp(Color.White, seed, 0.10f),
            surfaceContainerLowest = lerp(Color.White, seed, 0.08f),
            surfaceContainerLow = lerp(Color.White, seed, 0.15f),
            surfaceContainer = lerp(Color.White, seed, 0.18f),
            surfaceContainerHigh = lerp(Color.White, seed, 0.22f),
            surfaceContainerHighest = lerp(Color.White, seed, 0.26f),
            background = lerp(Color.White, seed, 0.08f),
        )
    }
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
