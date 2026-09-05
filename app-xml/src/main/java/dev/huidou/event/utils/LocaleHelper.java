package dev.huidou.event.utils;

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
    
    // 支持的语言代码（标准国家+地区格式）
    public static final String LANG_SYSTEM = "system";  // 跟随系统
    public static final String LANG_CHINESE = "zh-rCN";     // 简体中文
    public static final String LANG_TRADITIONAL_CHINESE = "zh-rTW";  // 繁体中文
    public static final String LANG_ENGLISH = "en-rUS";     // 英文
    public static final String LANG_RUSSIAN = "ru-rRU";     // 俄语
    public static final String LANG_JAPANESE = "ja-rJP";    // 日语
    
    /**
     * 保存语言设置
     */
    public static void setLanguage(Context context, String language) {
        // 标准化语言代码
        String normalized = normalizeLanguage(language);
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_LANGUAGE, normalized).apply();
        
        // 立即应用语言设置
        applyLanguage(context, normalized);
    }
    
    /**
     * 获取当前保存的语言设置
     */
    public static String getLanguage(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String language = prefs.getString(KEY_LANGUAGE, LANG_SYSTEM);
        // 将旧的语言代码标准化为新格式
        String normalized = normalizeLanguage(language);
        if (!language.equals(normalized)) {
            // 保存标准化后的值
            prefs.edit().putString(KEY_LANGUAGE, normalized).apply();
        }
        return normalized;
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
        } else {
            // 解析语言代码（支持新格式如 "zh-rCN" 和旧格式如 "zh"）
            locale = parseLocale(language);
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
     * 将旧的语言代码标准化为新格式
     * 例如 "zh" -> "zh-rCN", "zh-TW" -> "zh-rTW", "en" -> "en-rUS"
     */
    private static String normalizeLanguage(String language) {
        if (language == null || language.isEmpty()) {
            return LANG_SYSTEM;
        }
        // 已经是新格式
        if (language.contains("-r")) {
            return language;
        }
        // 映射旧格式
        switch (language) {
            case "zh":
                return LANG_CHINESE;
            case "zh-TW":
                return LANG_TRADITIONAL_CHINESE;
            case "en":
                return LANG_ENGLISH;
            case "ru":
                return LANG_RUSSIAN;
            case "ja":
                return LANG_JAPANESE;
            default:
                return language;
        }
    }
    
    /**
     * 将语言代码字符串解析为 Locale 对象
     * 支持新格式 "zh-rCN" 和旧格式 "zh"、"zh-TW"
     */
    private static Locale parseLocale(String language) {
        if (language == null || language.isEmpty()) {
            return Locale.getDefault();
        }
        
        // 检查是否包含 "-r" 分隔符（新格式）
        int index = language.indexOf("-r");
        if (index > 0) {
            String lang = language.substring(0, index);
            String region = language.substring(index + 2);
            return new Locale(lang, region);
        }
        
        // 检查旧格式 "zh-TW"
        String[] parts = language.split("-");
        if (parts.length == 2) {
            return new Locale(parts[0], parts[1]);
        }
        
        // 纯语言代码
        return new Locale(language);
    }
    
    /**
     * 获取语言显示名称
     */
    public static String getLanguageDisplayName(Context context, String language) {
        // 标准化语言代码以匹配常量
        String normalized = normalizeLanguage(language);
        switch (normalized) {
            case LANG_SYSTEM:
                return "跟随系统";
            case LANG_CHINESE:
                return context.getString(dev.huidou.event.R.string.chinese);
            case LANG_TRADITIONAL_CHINESE:
                return context.getString(dev.huidou.event.R.string.traditional_chinese);
            case LANG_ENGLISH:
                return context.getString(dev.huidou.event.R.string.english);
            case LANG_RUSSIAN:
                return context.getString(dev.huidou.event.R.string.russian);
            case LANG_JAPANESE:
                return context.getString(dev.huidou.event.R.string.japanese);
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
