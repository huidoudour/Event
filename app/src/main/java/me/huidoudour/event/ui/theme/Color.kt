package me.huidoudour.event.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * MD3 种子色数组，索引与 [me.huidoudour.event.util.ThemeHelper] 主题色常量一一对应：
 * 0=系统动态色(Material You) 1=粉色 2=蓝色 3=绿色 4=橙色 5=红色 6=青色
 * 注：索引 0 在 Theme.kt 中由 dynamicColorScheme 接管，此处仅作 API<31 回退（蓝色）
 */
val ThemeSeedColors = listOf(
    Color(0xFF3F6EF5), // 0 - 蓝色（API<31 回退色）
    Color(0xFFDE5C9D), // 1 - 粉色
    Color(0xFF3F6EF5), // 2 - 蓝色
    Color(0xFF2E7D32), // 3 - 绿色
    Color(0xFFFF6D00), // 4 - 橙色
    Color(0xFFD32F2F), // 5 - 红色
    Color(0xFF00838F), // 6 - 青色
)

/** 根据主题色索引获取对应的种子色 */
fun themeSeedColor(index: Int): Color =
    ThemeSeedColors.getOrElse(index) { ThemeSeedColors[0] }

/** 一个主题的完整 primary + secondary + tertiary 三组颜色 token */
data class ThemeTokens(
    val primary: Color, val onPrimary: Color,
    val primaryContainer: Color, val onPrimaryContainer: Color,
    val secondary: Color, val onSecondary: Color,
    val secondaryContainer: Color, val onSecondaryContainer: Color,
    val tertiary: Color, val onTertiary: Color,
    val tertiaryContainer: Color, val onTertiaryContainer: Color,
)

// ── Light 完整 token ──

private val LightTokens = listOf(
    // 0 - 蓝色（API<31 回退色）
    ThemeTokens(
        Color(0xFF3459D1), Color(0xFFFFFFFF), Color(0xFFDBE1FF), Color(0xFF00164F),
        Color(0xFF585E71), Color(0xFFFFFFFF), Color(0xFFDDE1F9), Color(0xFF151B2C),
        Color(0xFF735572), Color(0xFFFFFFFF), Color(0xFFFFD7FA), Color(0xFF2C122C),
    ),
    // 1 - 粉色
    ThemeTokens(
        Color(0xFFB0306A), Color(0xFFFFFFFF), Color(0xFFFFD9E2), Color(0xFF3E0020),
        Color(0xFF74565F), Color(0xFFFFFFFF), Color(0xFFFFD9E2), Color(0xFF2A151D),
        Color(0xFF7C5634), Color(0xFFFFFFFF), Color(0xFFFFDCC2), Color(0xFF2F1500),
    ),
    // 2 - 蓝色
    ThemeTokens(
        Color(0xFF3459D1), Color(0xFFFFFFFF), Color(0xFFDBE1FF), Color(0xFF00164F),
        Color(0xFF585E71), Color(0xFFFFFFFF), Color(0xFFDDE1F9), Color(0xFF151B2C),
        Color(0xFF735572), Color(0xFFFFFFFF), Color(0xFFFFD7FA), Color(0xFF2C122C),
    ),
    // 3 - 绿色
    ThemeTokens(
        Color(0xFF2E7D32), Color(0xFFFFFFFF), Color(0xFFA5D6A7), Color(0xFF002106),
        Color(0xFF52664F), Color(0xFFFFFFFF), Color(0xFFD5ECC9), Color(0xFF11210D),
        Color(0xFF6C5A3B), Color(0xFFFFFFFF), Color(0xFFFFDDBB), Color(0xFF261900),
    ),
    // 4 - 橙色
    ThemeTokens(
        Color(0xFFE65100), Color(0xFFFFFFFF), Color(0xFFFFDCC2), Color(0xFF2C1500),
        Color(0xFF755845), Color(0xFFFFFFFF), Color(0xFFFFDCC2), Color(0xFF2C1600),
        Color(0xFF6B5E2F), Color(0xFFFFFFFF), Color(0xFFFFE08D), Color(0xFF221B00),
    ),
    // 5 - 红色
    ThemeTokens(
        Color(0xFFC62828), Color(0xFFFFFFFF), Color(0xFFFFDAD4), Color(0xFF410002),
        Color(0xFF755452), Color(0xFFFFFFFF), Color(0xFFFFDAD4), Color(0xFF2C1513),
        Color(0xFF745A31), Color(0xFFFFFFFF), Color(0xFFFFDEA9), Color(0xFF271900),
    ),
    // 6 - 青色
    ThemeTokens(
        Color(0xFF006874), Color(0xFFFFFFFF), Color(0xFF97F0FF), Color(0xFF001F24),
        Color(0xFF4B6269), Color(0xFFFFFFFF), Color(0xFFC2E8EE), Color(0xFF051F24),
        Color(0xFF6A5C3F), Color(0xFFFFFFFF), Color(0xFFF3DFB1), Color(0xFF231B00),
    ),
)

// ── Dark 完整 token ──

private val DarkTokens = listOf(
    // 0 - 蓝色（API<31 回退色）
    ThemeTokens(
        Color(0xFFB1C5FF), Color(0xFF002A7E), Color(0xFF1A41B8), Color(0xFFDBE1FF),
        Color(0xFFC1C6DD), Color(0xFF2B3042), Color(0xFF414659), Color(0xFFDDE1F9),
        Color(0xFFE2BAD9), Color(0xFF43263F), Color(0xFF5C3B56), Color(0xFFFFD7FA),
    ),
    // 1 - 粉色
    ThemeTokens(
        Color(0xFFFFB1C8), Color(0xFF5E1136), Color(0xFF8E2652), Color(0xFFFFD9E2),
        Color(0xFFE2BDC8), Color(0xFF422931), Color(0xFF5A3F47), Color(0xFFFFD9E2),
        Color(0xFFEFBD94), Color(0xFF4A2A0B), Color(0xFF623E1D), Color(0xFFFFDCC2),
    ),
    // 2 - 蓝色
    ThemeTokens(
        Color(0xFFB1C5FF), Color(0xFF002A7E), Color(0xFF1A41B8), Color(0xFFDBE1FF),
        Color(0xFFC1C6DD), Color(0xFF2B3042), Color(0xFF414659), Color(0xFFDDE1F9),
        Color(0xFFE2BAD9), Color(0xFF43263F), Color(0xFF5C3B56), Color(0xFFFFD7FA),
    ),
    // 3 - 绿色
    ThemeTokens(
        Color(0xFF81C784), Color(0xFF003910), Color(0xFF2E7D32), Color(0xFFA5D6A7),
        Color(0xFFB9CFAE), Color(0xFF24371F), Color(0xFF3A4D33), Color(0xFFD5ECC9),
        Color(0xFFE6C186), Color(0xFF3D2C07), Color(0xFF56421B), Color(0xFFFFDDBB),
    ),
    // 4 - 橙色
    ThemeTokens(
        Color(0xFFFFB780), Color(0xFF492800), Color(0xFF723B00), Color(0xFFFFDCC2),
        Color(0xFFE1C1A8), Color(0xFF402C1A), Color(0xFF59422F), Color(0xFFFFDCC2),
        Color(0xFFD6CB5E), Color(0xFF373000), Color(0xFF4F4700), Color(0xFFFFE08D),
    ),
    // 5 - 红色
    ThemeTokens(
        Color(0xFFFFB4A9), Color(0xFF690005), Color(0xFF93000A), Color(0xFFFFDAD4),
        Color(0xFFE6BDBA), Color(0xFF442927), Color(0xFF5D3F3C), Color(0xFFFFDAD4),
        Color(0xFFE4C27A), Color(0xFF3E2C04), Color(0xFF564219), Color(0xFFFFDEA9),
    ),
    // 6 - 青色
    ThemeTokens(
        Color(0xFF4FD8EB), Color(0xFF00363E), Color(0xFF004F58), Color(0xFF97F0FF),
        Color(0xFFB1CCD2), Color(0xFF1C3438), Color(0xFF324B4F), Color(0xFFC2E8EE),
        Color(0xFFD7C48D), Color(0xFF392E06), Color(0xFF51441C), Color(0xFFF3DFB1),
    ),
)

/** 根据主题色索引与深浅模式获取完整 12 色 token 集合 */
fun themeTokens(themeColor: Int, darkTheme: Boolean): ThemeTokens {
    val idx = themeColor.coerceIn(0, 6)
    return if (darkTheme) DarkTokens[idx] else LightTokens[idx]
}
