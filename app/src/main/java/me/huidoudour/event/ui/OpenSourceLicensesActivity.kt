package me.huidoudour.event.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import me.huidoudour.event.ui.theme.EventTheme
import me.huidoudour.event.util.BaseActivity
import me.huidoudour.event.util.ThemeHelper

class OpenSourceLicensesActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // enableEdgeToEdge 必须在 super.onCreate() 之前调用
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            EventTheme(
                themeColor = ThemeHelper.getThemeColor(this),
                darkTheme = ThemeHelper.getTheme(this) == ThemeHelper.THEME_DARK ||
                        (ThemeHelper.getTheme(this) == ThemeHelper.THEME_SYSTEM &&
                         isNightMode())
            ) {
                OpenSourceLicensesScreenContent(onBack = { finish() })
            }
        }
    }

    private fun isNightMode(): Boolean {
        val mode = resources.configuration.uiMode and
                android.content.res.Configuration.UI_MODE_NIGHT_MASK
        return mode == android.content.res.Configuration.UI_MODE_NIGHT_YES
    }
}
