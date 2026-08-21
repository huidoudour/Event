package me.huidoudour.event.util

import android.content.Context
import android.content.res.Configuration
import androidx.core.content.edit
import java.util.Locale

/**
 * 语言切换辅助类
 * 用于管理和切换应用的语言设置
 */
object LocaleHelper {
    private const val PREFS_NAME = "locale_prefs"
    private const val KEY_LANGUAGE = "language"

    // 支持的语言代码（标准国家+地区格式）
    const val LANG_SYSTEM = "system"           // 跟随系统
    const val LANG_CHINESE = "zh-rCN"          // 简体中文
    const val LANG_TRADITIONAL_CHINESE = "zh-rTW"  // 繁体中文
    const val LANG_ENGLISH = "en-rUS"          // 英文
    const val LANG_RUSSIAN = "ru-rRU"          // 俄语
    const val LANG_JAPANESE = "ja-rJP"         // 日语

    /**
     * 保存语言设置
     */
    fun setLanguage(context: Context, language: String) {
        val normalized = normalizeLanguage(language)
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { putString(KEY_LANGUAGE, normalized) }
        // 立即应用语言设置
        applyLanguage(context, normalized)
    }

    /**
     * 获取当前保存的语言设置
     */
    fun getLanguage(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val language = prefs.getString(KEY_LANGUAGE, LANG_SYSTEM) ?: LANG_SYSTEM
        val normalized = normalizeLanguage(language)
        if (language != normalized) {
            prefs.edit { putString(KEY_LANGUAGE, normalized) }
        }
        return normalized
    }

    /**
     * 应用语言设置到 Context
     */
    fun applyLanguage(context: Context): Context {
        val language = getLanguage(context)
        return applyLanguage(context, language)
    }

    private fun applyLanguage(context: Context, language: String): Context {
        val locale = when {
            LANG_SYSTEM == language -> Locale.getDefault()
            else -> parseLocale(language)
        }

        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)

        return context.createConfigurationContext(config)
    }

    /**
     * 将旧的语言代码标准化为新格式
     */
    private fun normalizeLanguage(language: String): String {
        if (language.isEmpty()) return LANG_SYSTEM
        if (language.contains("-r")) return language
        return when (language) {
            "zh" -> LANG_CHINESE
            "zh-TW" -> LANG_TRADITIONAL_CHINESE
            "en" -> LANG_ENGLISH
            "ru" -> LANG_RUSSIAN
            "ja" -> LANG_JAPANESE
            "system" -> LANG_SYSTEM
            else -> language
        }
    }

    /**
     * 将语言代码字符串解析为 Locale 对象
     */
    private fun parseLocale(language: String): Locale {
        if (language.isEmpty()) return Locale.getDefault()

        // 检查是否包含 "-r" 分隔符（新格式）
        val index = language.indexOf("-r")
        if (index > 0) {
            val lang = language.substring(0, index)
            val region = language.substring(index + 2)
            return Locale.Builder().setLanguage(lang).setRegion(region).build()
        }

        // 检查旧格式 "zh-TW"
        val parts = language.split("-")
        return if (parts.size == 2) {
            Locale.Builder().setLanguage(parts[0]).setRegion(parts[1]).build()
        } else {
            Locale.Builder().setLanguage(language).build()
        }
    }

    /**
     * 获取语言显示名称
     */
    fun getLanguageDisplayName(context: Context, language: String): String {
        val normalized = normalizeLanguage(language)
        return when (normalized) {
            LANG_SYSTEM -> context.getString(me.huidoudour.event.R.string.follow_system)
            LANG_CHINESE -> context.getString(me.huidoudour.event.R.string.chinese)
            LANG_TRADITIONAL_CHINESE -> context.getString(me.huidoudour.event.R.string.traditional_chinese)
            LANG_ENGLISH -> context.getString(me.huidoudour.event.R.string.english)
            LANG_RUSSIAN -> context.getString(me.huidoudour.event.R.string.russian)
            LANG_JAPANESE -> context.getString(me.huidoudour.event.R.string.japanese)
            else -> context.getString(me.huidoudour.event.R.string.follow_system)
        }
    }

    /**
     * 获取所有支持的语言代码列表
     */
    fun getSupportedLanguages(): Array<String> = arrayOf(
        LANG_SYSTEM, LANG_CHINESE, LANG_TRADITIONAL_CHINESE,
        LANG_ENGLISH, LANG_RUSSIAN, LANG_JAPANESE
    )
}
