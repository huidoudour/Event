package me.huidoudour.event.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;

import java.util.Locale;

/**
 * 语言切换辅助类
 * 用于管理和切换应用的语言设置
 */
public class LocaleHelper {
    
    private static final String PREFS_NAME = "locale_prefs";
    private static final String KEY_LANGUAGE = "language";
    
    // 支持的语言代码
    public static final String LANG_SYSTEM = "system";  // 跟随系统
    public static final String LANG_CHINESE = "zh";     // 简体中文
    public static final String LANG_TRADITIONAL_CHINESE = "zh-TW";  // 繁体中文
    public static final String LANG_ENGLISH = "en";     // 英文
    public static final String LANG_RUSSIAN = "ru";     // 俄语
    public static final String LANG_JAPANESE = "ja";    // 日语
    
    /**
     * 保存语言设置
     */
    public static void setLanguage(Context context, String language) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_LANGUAGE, language).apply();
        
        // 立即应用语言设置
        applyLanguage(context, language);
    }
    
    /**
     * 获取当前保存的语言设置
     */
    public static String getLanguage(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_LANGUAGE, LANG_SYSTEM);
    }
    
    /**
     * 应用语言设置到 Context
     */
    public static Context applyLanguage(Context context) {
        String language = getLanguage(context);
        return applyLanguage(context, language);
    }
    
    /**
     * 应用指定的语言设置到 Context
     */
    private static Context applyLanguage(Context context, String language) {
        Locale locale;
        
        if (LANG_SYSTEM.equals(language)) {
            // 跟随系统语言
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                return context;
            } else {
                locale = Locale.getDefault();
            }
        } else if (LANG_TRADITIONAL_CHINESE.equals(language)) {
            // 繁体中文
            locale = new Locale("zh", "TW");
        } else {
            // 其他语言
            locale = new Locale(language);
        }
        
        Locale.setDefault(locale);
        
        Configuration config = context.getResources().getConfiguration();
        config.setLocale(locale);
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return context.createConfigurationContext(config);
        } else {
            context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());
            return context;
        }
    }
    
    /**
     * 获取语言显示名称
     */
    public static String getLanguageDisplayName(Context context, String language) {
        switch (language) {
            case LANG_SYSTEM:
                return "跟随系统";
            case LANG_CHINESE:
                return context.getString(me.huidoudour.event.R.string.chinese);
            case LANG_TRADITIONAL_CHINESE:
                return context.getString(me.huidoudour.event.R.string.traditional_chinese);
            case LANG_ENGLISH:
                return context.getString(me.huidoudour.event.R.string.english);
            case LANG_RUSSIAN:
                return context.getString(me.huidoudour.event.R.string.russian);
            case LANG_JAPANESE:
                return context.getString(me.huidoudour.event.R.string.japanese);
            default:
                return "跟随系统";
        }
    }
    
    /**
     * 获取所有支持的语言代码列表
     */
    public static String[] getSupportedLanguages() {
        return new String[]{
            LANG_SYSTEM,
            LANG_CHINESE,
            LANG_TRADITIONAL_CHINESE,
            LANG_ENGLISH,
            LANG_RUSSIAN,
            LANG_JAPANESE
        };
    }
}
