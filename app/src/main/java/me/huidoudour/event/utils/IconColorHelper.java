package me.huidoudour.event.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

public class IconColorHelper {
    private static final String PREFS_NAME = "icon_color_prefs";
    private static final String KEY_USE_COLORFUL_ICON = "use_colorful_icon";
    
    private static final String MAIN_ACTIVITY_CLASS = "me.huidoudour.event.ui.MainActivity";
    private static final String COLORFUL_ACTIVITY_ALIAS = "me.huidoudour.event.MainActivityColorful";
    
    /**
     * 获取是否使用彩色图标
     */
    public static boolean useColorfulIcon(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_USE_COLORFUL_ICON, false);
    }
    
    /**
     * 设置是否使用彩色图标
     */
    public static void setColorfulIcon(Context context, boolean useColorful) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_USE_COLORFUL_ICON, useColorful).apply();
    }
    
    /**
     * 切换图标颜色模式并应用
     */
    public static boolean toggleColorfulIcon(Context context) {
        boolean currentValue = useColorfulIcon(context);
        boolean newValue = !currentValue;
        setColorfulIcon(context, newValue);
        applyIconChange(context, newValue);
        return newValue;
    }
    
    /**
     * 应用图标更改
     */
    private static void applyIconChange(Context context, boolean useColorful) {
        PackageManager pm = context.getPackageManager();
        
        // 启用/禁用彩色图标别名
        int colorfulState = useColorful ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
        pm.setComponentEnabledSetting(
            new ComponentName(context, COLORFUL_ACTIVITY_ALIAS),
            colorfulState,
            PackageManager.DONT_KILL_APP
        );
        
        // 启用/禁用默认图标
        int defaultState = useColorful ? PackageManager.COMPONENT_ENABLED_STATE_DISABLED : PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
        pm.setComponentEnabledSetting(
            new ComponentName(context, MAIN_ACTIVITY_CLASS),
            defaultState,
            PackageManager.DONT_KILL_APP
        );
    }
}
