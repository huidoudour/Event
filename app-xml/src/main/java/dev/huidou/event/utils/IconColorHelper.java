package dev.huidou.event.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

public class IconColorHelper {
    private static final String PREFS_NAME = "icon_color_prefs";
    private static final String KEY_ICON_COLOR = "icon_color";
    
    // 图标颜色常量
    public static final int COLOR_DEFAULT = 0;        // 默认(绿色)
    public static final int COLOR_COLORFUL = 1;       // 彩色
    public static final int COLOR_RED = 2;            // 红色
    public static final int COLOR_BLUE = 3;           // 蓝色
    public static final int COLOR_YELLOW = 4;         // 黄色
    public static final int COLOR_PURPLE = 5;         // 紫色
    public static final int COLOR_ORANGE = 6;         // 橙色
    public static final int COLOR_CYAN = 7;           // 青色
    public static final int COLOR_PINK = 8;           // 粉色
    
    private static final String MAIN_ACTIVITY_CLASS = "dev.huidou.event.MainActivity";
    private static final String DEFAULT_ACTIVITY_ALIAS = "dev.huidou.event.MainActivityAliasDefault";
    private static final String COLORFUL_ACTIVITY_ALIAS = "dev.huidou.event.MainActivityAliasColorful";
    private static final String RED_ACTIVITY_ALIAS = "dev.huidou.event.MainActivityAliasRed";
    private static final String BLUE_ACTIVITY_ALIAS = "dev.huidou.event.MainActivityAliasBlue";
    private static final String YELLOW_ACTIVITY_ALIAS = "dev.huidou.event.MainActivityAliasYellow";
    private static final String PURPLE_ACTIVITY_ALIAS = "dev.huidou.event.MainActivityAliasPurple";
    private static final String ORANGE_ACTIVITY_ALIAS = "dev.huidou.event.MainActivityAliasOrange";
    private static final String CYAN_ACTIVITY_ALIAS = "dev.huidou.event.MainActivityAliasCyan";
    private static final String PINK_ACTIVITY_ALIAS = "dev.huidou.event.MainActivityAliasPink";
    
    /**
     * 获取当前图标颜色
     */
    public static int getIconColor(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_ICON_COLOR, COLOR_DEFAULT);
    }
    
    /**
     * 设置图标颜色
     */
    public static void setIconColor(Context context, int color) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt(KEY_ICON_COLOR, color).apply();
    }
    
    /**
     * 应用图标颜色更改
     */
    public static void applyIconColor(Context context, int color) {
        PackageManager pm = context.getPackageManager();
        
        // 禁用所有图标别名
        disableAllAliases(pm, context);
        
        // 启用选中的图标别名
        String aliasToEnable = getAliasForColor(color);
        if (aliasToEnable != null) {
            pm.setComponentEnabledSetting(
                new ComponentName(context, aliasToEnable),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            );
        }
    }
    
    /**
     * 根据颜色获取对应的activity-alias类名
     */
    private static String getAliasForColor(int color) {
        switch (color) {
            case COLOR_DEFAULT:
                return DEFAULT_ACTIVITY_ALIAS;
            case COLOR_COLORFUL:
                return COLORFUL_ACTIVITY_ALIAS;
            case COLOR_RED:
                return RED_ACTIVITY_ALIAS;
            case COLOR_BLUE:
                return BLUE_ACTIVITY_ALIAS;
            case COLOR_YELLOW:
                return YELLOW_ACTIVITY_ALIAS;
            case COLOR_PURPLE:
                return PURPLE_ACTIVITY_ALIAS;
            case COLOR_ORANGE:
                return ORANGE_ACTIVITY_ALIAS;
            case COLOR_CYAN:
                return CYAN_ACTIVITY_ALIAS;
            case COLOR_PINK:
                return PINK_ACTIVITY_ALIAS;
            default:
                return DEFAULT_ACTIVITY_ALIAS;
        }
    }
    
    /**
     * 禁用所有图标别名
     */
    private static void disableAllAliases(PackageManager pm, Context context) {
        String[] aliases = {
            DEFAULT_ACTIVITY_ALIAS,
            COLORFUL_ACTIVITY_ALIAS,
            RED_ACTIVITY_ALIAS,
            BLUE_ACTIVITY_ALIAS,
            YELLOW_ACTIVITY_ALIAS,
            PURPLE_ACTIVITY_ALIAS,
            ORANGE_ACTIVITY_ALIAS,
            CYAN_ACTIVITY_ALIAS,
            PINK_ACTIVITY_ALIAS
        };
        
        for (String alias : aliases) {
            pm.setComponentEnabledSetting(
                new ComponentName(context, alias),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
            );
        }
    }
}
