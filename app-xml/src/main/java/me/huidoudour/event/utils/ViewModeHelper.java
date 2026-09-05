package me.huidoudour.event.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * 视图模式管理器
 * 用于保存和获取用户选择的视图模式（条目视图或列表视图）
 */
public class ViewModeHelper {
    
    private static final String PREFS_NAME = "view_mode_prefs";
    private static final String KEY_VIEW_MODE = "view_mode";
    
    // 视图模式常量
    public static final int VIEW_MODE_CARD = 0;  // 条目视图（默认）
    public static final int VIEW_MODE_LIST = 1;  // 列表视图
    
    /**
     * 获取当前视图模式
     * @param context 上下文
     * @return 视图模式（VIEW_MODE_CARD 或 VIEW_MODE_LIST）
     */
    public static int getViewMode(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_VIEW_MODE, VIEW_MODE_CARD);
    }
    
    /**
     * 设置视图模式
     * @param context 上下文
     * @param mode 视图模式（VIEW_MODE_CARD 或 VIEW_MODE_LIST）
     */
    public static void setViewMode(Context context, int mode) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt(KEY_VIEW_MODE, mode).apply();
    }
    
    /**
     * 获取视图模式的显示名称
     * @param context 上下文
     * @param mode 视图模式
     * @return 显示名称
     */
    public static String getViewModeDisplayName(Context context, int mode) {
        switch (mode) {
            case VIEW_MODE_CARD:
                return context.getString(me.huidoudour.event.R.string.card_view);
            case VIEW_MODE_LIST:
                return context.getString(me.huidoudour.event.R.string.list_view);
            default:
                return context.getString(me.huidoudour.event.R.string.card_view);
        }
    }
}
