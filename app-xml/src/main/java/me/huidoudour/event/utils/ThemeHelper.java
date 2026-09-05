package me.huidoudour.event.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

/**
 * 主题切换辅助类
 * 用于管理和切换应用的主题模式
 */
public class ThemeHelper {
    
    private static final String PREFS_NAME = "theme_prefs";
    private static final String KEY_THEME = "theme_mode";
    
    // 支持的主题模式
    public static final int THEME_SYSTEM = -1;     // 跟随系统
    public static final int THEME_LIGHT = 1;       // 浅色主题
    public static final int THEME_DARK = 2;        // 深色主题
    
    /**
     * 保存主题设置并立即应用
     */
    public static void setTheme(Context context, int themeMode) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt(KEY_THEME, themeMode).apply();
        
        // 立即应用主题设置
        applyTheme(themeMode);
    }
    
    /**
     * 获取当前保存的主题设置
     */
    public static int getTheme(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_THEME, THEME_SYSTEM);
    }
    
    /**
     * 应用主题设置
     */
    public static void applyTheme(int themeMode) {
        switch (themeMode) {
            case THEME_LIGHT:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case THEME_DARK:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case THEME_SYSTEM:
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
    }
    
    /**
     * 在应用启动时初始化主题设置
     */
    public static void initTheme(Context context) {
        int themeMode = getTheme(context);
        applyTheme(themeMode);
    }
    
    /**
     * 获取主题显示名称
     */
    public static String getThemeDisplayName(Context context, int themeMode) {
        switch (themeMode) {
            case THEME_SYSTEM:
                return context.getString(me.huidoudour.event.R.string.system_theme);
            case THEME_LIGHT:
                return context.getString(me.huidoudour.event.R.string.light_theme);
            case THEME_DARK:
                return context.getString(me.huidoudour.event.R.string.dark_theme);
            default:
                return context.getString(me.huidoudour.event.R.string.system_theme);
        }
    }
    
    /**
     * 获取所有支持的主题模式列表
     */
    public static int[] getSupportedThemes() {
        return new int[]{
            THEME_SYSTEM,
            THEME_LIGHT,
            THEME_DARK
        };
    }
}
