package me.huidoudour.event.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamicColorScheme

/**
 * 根据主题色索引构建完整 MD3 ColorScheme。
 * - 索引 0：跟随系统壁纸动态取色 (Material You)，API<31 回退到蓝色
 * - 索引 1~6：使用 materialkolor 从种子色自动生成完整 ColorScheme
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
    // 手工主题色：materialkolor 根据种子色自动生成完整 ColorScheme（含 surface 细微底调）
    val seed = themeSeedColor(themeColor)
    return dynamicColorScheme(
        primary = seed,
        isDark = darkTheme,
        isAmoled = false,
        style = PaletteStyle.Neutral,
        contrastLevel = -0.15,
        modifyColorScheme = { cs ->
            val bg = if (darkTheme) Color(0xFF1C1B1F) else Color.White
            cs.copy(
                primary = lerp(bg, cs.primary, 0.55f),
                primaryContainer = lerp(bg, cs.primaryContainer, 0.45f)
            )
        }
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
