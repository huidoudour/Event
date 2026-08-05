package me.huidoudour.event

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import me.huidoudour.event.ui.MeScreenContent
import me.huidoudour.event.ui.theme.EventTheme
import me.huidoudour.event.util.LocaleHelper
import me.huidoudour.event.util.ThemeHelper

class MeActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ThemeHelper.applyNightMode(LocaleHelper.applyLanguage(newBase)))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeHelper.initTheme(this)
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            EventTheme(
                themeColor = ThemeHelper.getThemeColor(this),
                darkTheme = ThemeHelper.getTheme(this) == ThemeHelper.THEME_DARK ||
                        (ThemeHelper.getTheme(this) == ThemeHelper.THEME_SYSTEM &&
                         isNightMode())
            ) {
                MeScreenContent()
            }
        }
    }

    private fun isNightMode(): Boolean {
        val mode = resources.configuration.uiMode and
                android.content.res.Configuration.UI_MODE_NIGHT_MASK
        return mode == android.content.res.Configuration.UI_MODE_NIGHT_YES
    }
}
