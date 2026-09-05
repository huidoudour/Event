package dev.huidou.event.utils;

import android.content.Context;

import androidx.appcompat.app.AppCompatDelegate;

/**
 * 主题辅助类
 * 深色主题已移除，应用固定使用浅色主题
 */
public class ThemeHelper {

    /**
     * 应用固定浅色主题
     */
    public static void initTheme(Context context) {
        // 固定浅色主题，不跟随系统、不提供深色模式
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
    }
}
