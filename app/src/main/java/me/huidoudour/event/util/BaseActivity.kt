package me.huidoudour.event.util

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate

/**
 * 基类 Activity，统一处理语言、主题模式和主题色的初始化与实时同步。
 *
 * 所有使用传统 View 系统的 Activity 可继承此类。
 * Compose Activity 应直接使用 BaseComposeActivity。
 */
abstract class BaseActivity : AppCompatActivity() {

    // 记录当前Activity创建时的设置值，用于onResume检测变化
    private var currentLanguage: String = ""
    private var currentTheme: Int = -1
    private var currentThemeColor: Int = 0
    protected var isRestarting = false

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.applyLanguage(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // 在super.onCreate前设置本地夜间模式（delegate实例方法，确保对当前Activity生效）
        val themeMode = ThemeHelper.getTheme(this)
        delegate.localNightMode = when (themeMode) {
            ThemeHelper.THEME_LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            ThemeHelper.THEME_DARK -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        super.onCreate(savedInstanceState)
        // 在super.onCreate后应用主题色overlay（必须在setContentView前）
        ThemeHelper.applyThemeColor(this)

        // 记录当前设置值，用于onResume检测变化
        currentLanguage = LocaleHelper.getLanguage(this)
        currentTheme = ThemeHelper.getTheme(this)
        currentThemeColor = ThemeHelper.getThemeColor(this)
    }

    override fun onResume() {
        super.onResume()
        if (isRestarting) return
        // 检测语言、主题模式或主题色是否在外部被修改
        val newLanguage = LocaleHelper.getLanguage(this)
        val newTheme = ThemeHelper.getTheme(this)
        val newThemeColor = ThemeHelper.getThemeColor(this)

        if (newLanguage != currentLanguage || newTheme != currentTheme || newThemeColor != currentThemeColor) {
            isRestarting = true
            restartSelf()
        }
    }

    /**
     * 重启当前 Activity 以应用新的设置
     */
    protected fun restartSelf() {
        val intent = intent
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        finish()
        startActivity(intent)
        @Suppress("DEPRECATION")
        overridePendingTransition(0, 0)
    }

}
