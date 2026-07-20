package me.huidoudour.event.util

import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import me.huidoudour.event.R

/**
 * 主题切换辅助类
 * 用于管理和切换应用的主题模式（浅色/深色/系统）及主题色
 */
object ThemeHelper {
    private const val PREFS_NAME = "theme_prefs"
    private const val KEY_THEME = "theme_mode"
    private const val KEY_THEME_COLOR = "theme_color"

    // 支持的主题模式
    const val THEME_SYSTEM = -1     // 跟随系统
    const val THEME_LIGHT = 1       // 浅色主题
    const val THEME_DARK = 2        // 深色主题

    // 支持的主题色
    const val COLOR_DEFAULT = 0     // 默认紫色
    const val COLOR_PINK = 1        // 粉色
    const val COLOR_BLUE = 2        // 蓝色
    const val COLOR_GREEN = 3       // 绿色
    const val COLOR_ORANGE = 4      // 橙色
    const val COLOR_RED = 5         // 红色
    const val COLOR_CYAN = 6        // 青色

    // ── 主题模式（浅色/深色/系统）──

    /**
     * 保存主题设置并立即应用
     */
    fun setTheme(context: Context, themeMode: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_THEME, themeMode).apply()
        applyTheme(themeMode)
    }

    /**
     * 获取当前保存的主题设置
     */
    fun getTheme(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_THEME, THEME_SYSTEM)
    }

    /**
     * 应用主题设置
     */
    fun applyTheme(themeMode: Int) {
        when (themeMode) {
            THEME_LIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            THEME_DARK -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    /**
     * 在应用启动时初始化主题设置
     */
    fun initTheme(context: Context) {
        val themeMode = getTheme(context)
        applyTheme(themeMode)
    }

    /**
     * 获取主题显示名称
     */
    fun getThemeDisplayName(context: Context, themeMode: Int): String {
        return when (themeMode) {
            THEME_SYSTEM -> context.getString(R.string.system_theme)
            THEME_LIGHT -> context.getString(R.string.light_theme)
            THEME_DARK -> context.getString(R.string.dark_theme)
            else -> context.getString(R.string.system_theme)
        }
    }

    /**
     * 获取所有支持的主题模式列表
     */
    fun getSupportedThemes(): IntArray = intArrayOf(THEME_SYSTEM, THEME_LIGHT, THEME_DARK)

    // ── 主题色 ──

    /**
     * 保存主题色设置
     */
    fun setThemeColor(context: Context, color: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_THEME_COLOR, color).apply()
    }

    /**
     * 获取当前保存的主题色
     */
    fun getThemeColor(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_THEME_COLOR, COLOR_DEFAULT)
    }

    /**
     * 应用主题色 ThemeOverlay 到 Activity
     * 必须在 super.onCreate() 之后、setContentView() 之前调用
     */
    fun applyThemeColor(activity: Activity) {
        val color = getThemeColor(activity)
        val overlayRes = getOverlayRes(color)
        if (overlayRes != 0) {
            activity.theme.applyStyle(overlayRes, true)
        }
    }

    private fun getOverlayRes(color: Int): Int {
        return when (color) {
            COLOR_PINK -> R.style.ThemeOverlay_Event_Pink
            COLOR_BLUE -> R.style.ThemeOverlay_Event_Blue
            COLOR_GREEN -> R.style.ThemeOverlay_Event_Green
            COLOR_ORANGE -> R.style.ThemeOverlay_Event_Orange
            COLOR_RED -> R.style.ThemeOverlay_Event_Red
            COLOR_CYAN -> R.style.ThemeOverlay_Event_Cyan
            else -> 0 // 默认色无需overlay
        }
    }

    /**
     * 获取主题色显示名称
     */
    fun getThemeColorDisplayName(context: Context, color: Int): String {
        return when (color) {
            COLOR_DEFAULT -> context.getString(R.string.default_color)
            COLOR_PINK -> context.getString(R.string.pink_color)
            COLOR_BLUE -> context.getString(R.string.blue_color)
            COLOR_GREEN -> context.getString(R.string.green_color)
            COLOR_ORANGE -> context.getString(R.string.orange_color)
            COLOR_RED -> context.getString(R.string.red_color)
            COLOR_CYAN -> context.getString(R.string.cyan_color)
            else -> context.getString(R.string.default_color)
        }
    }

    /**
     * 获取所有支持的主题色列表
     */
    fun getSupportedThemeColors(): IntArray = intArrayOf(
        COLOR_DEFAULT, COLOR_PINK, COLOR_BLUE, COLOR_GREEN,
        COLOR_ORANGE, COLOR_RED, COLOR_CYAN
    )
}
