package dev.huidou.event.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;

import java.util.Locale;

/**
 * 语言辅助类
 * 多语言已移除，应用固定使用默认回退语言（简体中文）
 */
public class LocaleHelper {

    // 固定语言：简体中文（默认回退语言）
    private static final Locale DEFAULT_LOCALE = Locale.SIMPLIFIED_CHINESE;

    /**
     * 应用固定语言（简体中文）到 Context
     */
    public static Context applyLanguage(Context context) {
        Locale.setDefault(DEFAULT_LOCALE);

        Configuration config = context.getResources().getConfiguration();
        config.setLocale(DEFAULT_LOCALE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return context.createConfigurationContext(config);
        } else {
            context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());
            return context;
        }
    }
}
