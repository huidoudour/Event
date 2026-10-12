package me.huidoudour.event.util

import android.app.LocaleManager
import android.content.Context
import android.os.Build
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import androidx.core.os.LocaleListCompat

/**
 * 语言切换辅助类
 *
 * 统一通过 AppCompatDelegate 公共 API 管理应用语言设置：
 * - Android 13+：由系统 LocaleManager 持久化，与系统设置"应用语言"双向实时同步
 * - Android 12 及以下：由 AppCompat autoStoreLocales 自动持久化（见 AndroidManifest 中
 *   AppLocalesMetadataHolderService 声明），并在界面重建后自动生效
 *
 * 注意：切换语言后 AppCompat/系统会自动重建界面，调用方无需手动 recreate。
 */
object LocaleHelper {
    private const val PREFS_NAME = "locale_prefs"
    private const val KEY_LANGUAGE = "language"

    // 支持的语言代码（标准国家+地区格式，与 res/values-* 资源目录一致）
    const val LANG_SYSTEM = "system"           // 跟随系统
    const val LANG_CHINESE = "zh-rCN"          // 简体中文
    const val LANG_TRADITIONAL_CHINESE = "zh-rTW"  // 繁体中文
    const val LANG_ENGLISH = "en-rUS"          // 英文
    const val LANG_RUSSIAN = "ru-rRU"          // 俄语
    const val LANG_JAPANESE = "ja-rJP"         // 日语

    /**
     * 保存并应用语言设置（需在主线程调用）
     * Android 13+ 会同步到系统"应用语言"设置，低版本由 AppCompat 持久化
     */
    fun setLanguage(language: String) {
        val normalized = normalizeLanguage(language)
        val locales = if (normalized == LANG_SYSTEM) {
            LocaleListCompat.getEmptyLocaleList()
        } else {
            LocaleListCompat.forLanguageTags(toLanguageTag(normalized))
        }
        AppCompatDelegate.setApplicationLocales(locales)
    }

    /**
     * 获取当前应用语言设置（Android 13+ 直接读取系统状态）
     * @return [LANG_SYSTEM] 或语言代码（如 "zh-rCN"）
     */
    fun getLanguage(): String {
        val locales = AppCompatDelegate.getApplicationLocales()
        if (locales.isEmpty) return LANG_SYSTEM
        return normalizeLanguage(locales.toLanguageTags().substringBefore(','))
    }

    /**
     * 迁移旧版本保存在 SharedPreferences 中的语言设置（一次性，可重复调用）
     *
     * 仅在用户尚未通过公共 API / 系统设置过语言时迁移，避免覆盖新设置。
     * Android 13+ 在应用启动早期调用（无界面附着），需直接写入系统 LocaleManager，
     * 此时 AppCompat 静态 API 找不到关联界面而无法写入系统。
     */
    fun migrateLegacyLanguageIfNeeded(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val legacy = prefs.getString(KEY_LANGUAGE, null) ?: return
        val normalized = normalizeLanguage(legacy)
        // 先同步清除旧键：即使后续设置语言触发界面重建或进程重启，迁移也只会执行一次
        prefs.edit(commit = true) { remove(KEY_LANGUAGE) }
        if (normalized == LANG_SYSTEM) return
        if (hasApplicationLocale(context)) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            runCatching {
                context.getSystemService(LocaleManager::class.java)?.applicationLocales =
                    LocaleList.forLanguageTags(toLanguageTag(normalized))
            }
        } else {
            // Android 12 及以下：写入 AppCompat，首个界面创建时自动持久化到 autoStoreLocales
            AppCompatDelegate.setApplicationLocales(
                LocaleListCompat.forLanguageTags(toLanguageTag(normalized))
            )
        }
    }

    /**
     * 是否已存在应用级语言设置（Android 13+ 读系统，低版本读 AppCompat）
     */
    private fun hasApplicationLocale(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.getSystemService(LocaleManager::class.java)?.applicationLocales?.isEmpty == false
        } else {
            !AppCompatDelegate.getApplicationLocales().isEmpty
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

    /**
     * 资源限定符格式（zh-rCN）转换为 BCP-47 标签（zh-CN）
     */
    private fun toLanguageTag(language: String): String = language.replace("-r", "-")

    /**
     * 将 BCP-47 标签或旧的语言代码标准化为资源限定符格式（如 "zh-CN" → "zh-rCN"）
     */
    private fun normalizeLanguage(language: String): String {
        if (language.isEmpty()) return LANG_SYSTEM
        if (language.contains("-r")) return language
        return when (language) {
            "zh", "zh-CN" -> LANG_CHINESE
            "zh-TW" -> LANG_TRADITIONAL_CHINESE
            "en", "en-US" -> LANG_ENGLISH
            "ru", "ru-RU" -> LANG_RUSSIAN
            "ja", "ja-JP" -> LANG_JAPANESE
            "system" -> LANG_SYSTEM
            else -> language
        }
    }
}
