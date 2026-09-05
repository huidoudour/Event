package me.huidoudour.event.util

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.edit

object IconColorHelper {
    private const val PREFS_NAME = "icon_color_prefs"
    private const val KEY_ICON_COLOR = "icon_color"

    // 图标颜色常量
    const val COLOR_DEFAULT = 0     // 默认(绿色)
    const val COLOR_COLORFUL = 1    // 彩色

    private const val DEFAULT_ACTIVITY_ALIAS = "me.huidou.event.MainActivityAliasDefault"
    private const val COLORFUL_ACTIVITY_ALIAS = "me.huidou.event.MainActivityAliasColorful"

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
        prefs.edit { putInt(KEY_ICON_COLOR, color) }
    }

    /**
     * 启动时同步图标状态（仅在状态不一致时才执行 IPC）
     */
    fun ensureIconColor(context: Context) {
        val color = getIconColor(context)
        val targetAlias = getAliasForColor(color) ?: return
        val pm = context.packageManager

        // 检查目标 alias 是否已启用，已启用则跳过
        val state = pm.getComponentEnabledSetting(ComponentName(context, targetAlias))
        if (state == PackageManager.COMPONENT_ENABLED_STATE_ENABLED ||
            state == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT && isDefaultEnabled(color)) {
            return
        }
        // 状态不一致，后台修复
        applyIconColor(context, color)
    }

    /**
     * 应用图标颜色更改（仅在用户主动切换时调用）
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

    private fun isDefaultEnabled(color: Int): Boolean = color == COLOR_DEFAULT

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
