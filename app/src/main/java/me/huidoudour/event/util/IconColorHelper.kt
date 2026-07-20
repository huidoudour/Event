package me.huidoudour.event.util

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager

object IconColorHelper {
    private const val PREFS_NAME = "icon_color_prefs"
    private const val KEY_ICON_COLOR = "icon_color"

    // 图标颜色常量
    const val COLOR_DEFAULT = 0     // 默认(绿色)
    const val COLOR_COLORFUL = 1    // 彩色

    private const val MAIN_ACTIVITY_CLASS = "me.huidoudour.event.MainActivity"
    private const val DEFAULT_ACTIVITY_ALIAS = "me.huidoudour.event.MainActivityAliasDefault"
    private const val COLORFUL_ACTIVITY_ALIAS = "me.huidoudour.event.MainActivityAliasColorful"

    /**
     * 获取当前图标颜色
     */
    fun getIconColor(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_ICON_COLOR, COLOR_DEFAULT)
    }

    /**
     * 设置图标颜色
     */
    fun setIconColor(context: Context, color: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_ICON_COLOR, color).apply()
    }

    /**
     * 应用图标颜色更改
     */
    fun applyIconColor(context: Context, color: Int) {
        val pm = context.packageManager

        // 禁用所有图标别名
        disableAllAliases(pm, context)

        // 启用选中的图标别名
        val aliasToEnable = getAliasForColor(color)
        if (aliasToEnable != null) {
            pm.setComponentEnabledSetting(
                ComponentName(context, aliasToEnable),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            )
        }
    }

    private fun getAliasForColor(color: Int): String? {
        return when (color) {
            COLOR_DEFAULT -> DEFAULT_ACTIVITY_ALIAS
            COLOR_COLORFUL -> COLORFUL_ACTIVITY_ALIAS
            else -> DEFAULT_ACTIVITY_ALIAS
        }
    }

    private fun disableAllAliases(pm: PackageManager, context: Context) {
        val aliases = arrayOf(
            DEFAULT_ACTIVITY_ALIAS,
            COLORFUL_ACTIVITY_ALIAS
        )
        for (alias in aliases) {
            pm.setComponentEnabledSetting(
                ComponentName(context, alias),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
            )
        }
    }
}
