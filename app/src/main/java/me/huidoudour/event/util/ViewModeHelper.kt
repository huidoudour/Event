package me.huidoudour.event.util

import android.content.Context
import me.huidoudour.event.R

/**
 * 视图模式管理器
 * 用于保存和获取用户选择的视图模式（条目视图或列表视图）
 */
object ViewModeHelper {
    private const val PREFS_NAME = "view_mode_prefs"
    private const val KEY_VIEW_MODE = "view_mode"

    // 视图模式常量
    const val VIEW_MODE_CARD = 0  // 条目视图（默认）
    const val VIEW_MODE_LIST = 1  // 列表视图

    /**
     * 获取当前视图模式
     */
    fun getViewMode(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_VIEW_MODE, VIEW_MODE_CARD)
    }

    /**
     * 设置视图模式
     */
    fun setViewMode(context: Context, mode: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_VIEW_MODE, mode).apply()
    }

    /**
     * 获取视图模式的显示名称
     */
    fun getViewModeDisplayName(context: Context, mode: Int): String {
        return when (mode) {
            VIEW_MODE_CARD -> context.getString(R.string.card_view)
            VIEW_MODE_LIST -> context.getString(R.string.list_view)
            else -> context.getString(R.string.card_view)
        }
    }
}
