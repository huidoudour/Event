package me.huidoudour.event

import android.app.Application
import me.huidoudour.event.util.LocaleHelper

/**
 * 应用入口。
 *
 * 在应用启动时执行一次旧版本语言设置迁移：
 * 将历史上保存在 SharedPreferences（locale_prefs）中的语言迁移到
 * AppCompatDelegate 公共 API（Android 13+ 写入系统、低版本写入 autoStoreLocales）。
 */
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        LocaleHelper.migrateLegacyLanguageIfNeeded(this)
    }
}
