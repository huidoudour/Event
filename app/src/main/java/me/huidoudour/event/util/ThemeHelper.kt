package me.huidoudour.event.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

import me.huidoudour.event.R;

/**
 * 主题切换辅助类
 * 用于管理和切换应用的主题模式（浅色/深色/系统）及主题色
 */
public class ThemeHelper {
    
    private static final String PREFS_NAME = "theme_prefs";
    private static final String KEY_THEME = "theme_mode";
    private static final String KEY_THEME_COLOR = "theme_color";
    
    // 支持的主题模式
    public static final int THEME_SYSTEM = -1;     // 跟随系统
    public static final int THEME_LIGHT = 1;       // 浅色主题
    public static final int THEME_DARK = 2;        // 深色主题
    
    // 支持的主题色
    public static final int COLOR_DEFAULT = 0;     // 默认紫色
    public static final int COLOR_PINK = 1;        // 粉色
    public static final int COLOR_BLUE = 2;        // 蓝色
    public static final int COLOR_GREEN = 3;       // 绿色
    public static final int COLOR_ORANGE = 4;      // 橙色
    public static final int COLOR_RED = 5;         // 红色
    public static final int COLOR_CYAN = 6;        // 青色
    
    // ─────────────────────────────────────────────
    // 主题模式（浅色/深色/系统）
    // ─────────────────────────────────────────────
    
    /**
     * 保存主题设置并立即应用
     */
    public static void setTheme(Context context, int themeMode) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt(KEY_THEME, themeMode).apply();
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
                return context.getString(R.string.system_theme);
            case THEME_LIGHT:
                return context.getString(R.string.light_theme);
            case THEME_DARK:
                return context.getString(R.string.dark_theme);
            default:
                return context.getString(R.string.system_theme);
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
    
    // ─────────────────────────────────────────────
    // 主题色（粉色/蓝/绿/橙/红/青）
    // ─────────────────────────────────────────────
    
    /**
     * 保存主题色设置
     */
    public static void setThemeColor(Context context, int color) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt(KEY_THEME_COLOR, color).apply();
    }
    
    /**
     * 获取当前保存的主题色
     */
    public static int getThemeColor(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_THEME_COLOR, COLOR_DEFAULT);
    }
    
    /**
     * 应用主题色 ThemeOverlay 到 Activity
     * 必须在 super.onCreate() 之后、setContentView() 之前调用
     */
    public static void applyThemeColor(Activity activity) {
        int color = getThemeColor(activity);
        int overlayRes = getOverlayRes(color);
        if (overlayRes != 0) {
            activity.getTheme().applyStyle(overlayRes, true);
        }
    }
    
    /**
     * 获取主题色对应的 ThemeOverlay 资源ID
     */
    private static int getOverlayRes(int color) {
        switch (color) {
            case COLOR_PINK:   return R.style.ThemeOverlay_Event_Pink;
            case COLOR_BLUE:   return R.style.ThemeOverlay_Event_Blue;
            case COLOR_GREEN:  return R.style.ThemeOverlay_Event_Green;
            case COLOR_ORANGE: return R.style.ThemeOverlay_Event_Orange;
            case COLOR_RED:    return R.style.ThemeOverlay_Event_Red;
            case COLOR_CYAN:   return R.style.ThemeOverlay_Event_Cyan;
            case COLOR_DEFAULT:
            default:           return 0; // 默认色无需overlay
        }
    }
    
    /**
     * 获取主题色显示名称
     */
    public static String getThemeColorDisplayName(Context context, int color) {
        switch (color) {
            case COLOR_DEFAULT: return context.getString(R.string.default_color);
            case COLOR_PINK:    return context.getString(R.string.pink_color);
            case COLOR_BLUE:    return context.getString(R.string.blue_color);
            case COLOR_GREEN:   return context.getString(R.string.green_color);
            case COLOR_ORANGE:  return context.getString(R.string.orange_color);
            case COLOR_RED:     return context.getString(R.string.red_color);
            case COLOR_CYAN:    return context.getString(R.string.cyan_color);
            default:            return context.getString(R.string.default_color);
        }
    }
    
    /**
     * 获取所有支持的主题色列表
     */
    public static int[] getSupportedThemeColors() {
        return new int[]{
            COLOR_DEFAULT,
            COLOR_PINK,
            COLOR_BLUE,
            COLOR_GREEN,
            COLOR_ORANGE,
            COLOR_RED,
            COLOR_CYAN
        };
    }
}
