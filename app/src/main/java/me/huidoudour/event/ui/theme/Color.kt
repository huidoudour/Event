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

// ── 博客风格淡色系（亮色模式，参考 huidoudour.github.io 的淡蓝）──
val BlogBgBlue = Color(0xFFF8FBFF)       // 背景渐变起点 — 淡蓝白
val BlogBgPaleBlue = Color(0xFFEEF6FC)   // 背景渐变中点 — 淡蓝
val BlogBgPaleBlue2 = Color(0xFFE5F0FA)  // 背景渐变终点 — 稍深淡蓝
val BlogBtnBlue = Color(0xFFB9DFF7)      // 按钮淡蓝底
val BlogBtnPink = Color(0xFFF2B3C3)      // 按钮淡粉底
val BlogCardBlue = Color(0xFFE8F3FC)     // 卡片淡蓝底
