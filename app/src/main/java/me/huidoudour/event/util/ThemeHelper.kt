package me.huidoudour.event.util

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.view.ContextThemeWrapper
import androidx.core.content.edit
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
    const val COLOR_DEFAULT = 0     // 跟随系统色 (Material You)
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
        prefs.edit { putInt(KEY_THEME, themeMode) }
        applyTheme(themeMode)
    }

    /**
     * 仅持久化主题设置（不触发 AppCompatDelegate 导致 Activity recreate），
     * 配合 Compose 层 state 驱动即时切换，适用于设置页的实时预览。
     */
    fun saveTheme(context: Context, themeMode: Int) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit { putInt(KEY_THEME, themeMode) }
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
     * 将保存的主题模式覆盖到 Context 的 uiMode 配置上。
     *
     * AppCompatDelegate.setDefaultNightMode 只对 AppCompatActivity 生效，
     * ComponentActivity 必须在 attachBaseContext 中调用本方法，
     * XML 主题（values-night）、原生日期/时间选择器和 AlertDialog 等
     * 非 Compose 元素才能正确跟随应用内的深色模式设置。
     */
    fun applyNightMode(context: Context): Context {
        val nightFlag = when (getTheme(context)) {
            THEME_LIGHT -> Configuration.UI_MODE_NIGHT_NO
            THEME_DARK -> Configuration.UI_MODE_NIGHT_YES
            else -> return context // 跟随系统，无需覆盖
        }
        val config = Configuration(context.resources.configuration)
        config.uiMode = nightFlag or (config.uiMode and Configuration.UI_MODE_NIGHT_MASK.inv())
        return context.createConfigurationContext(config)
    }

    /**
     * 基于 Activity 创建跟随当前深色模式设置的对话框 Context。
     *
     * 设置页实时切换主题后 Activity 不会重建，此时弹出的原生对话框
     * 需要用本方法包装的 Context 才能显示正确的深浅配色。
     * 使用 ContextThemeWrapper 保留 Activity 的窗口 token，Dialog 可正常弹出。
     */
    fun createNightAwareContext(activity: Activity): Context {
        val nightFlag = when (getTheme(activity)) {
            THEME_LIGHT -> Configuration.UI_MODE_NIGHT_NO
            THEME_DARK -> Configuration.UI_MODE_NIGHT_YES
            else -> return activity
        }
        val config = Configuration(activity.resources.configuration)
        config.uiMode = nightFlag or (config.uiMode and Configuration.UI_MODE_NIGHT_MASK.inv())
        val wrapper = ContextThemeWrapper(activity, R.style.Theme_Event)
        wrapper.applyOverrideConfiguration(config)
        return wrapper
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
        prefs.edit { putInt(KEY_THEME_COLOR, color) }
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
