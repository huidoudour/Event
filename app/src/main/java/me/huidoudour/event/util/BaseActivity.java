package me.huidoudour.event.util;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import me.huidoudour.event.MainActivity;

/**
 * 基类Activity，统一处理语言、主题模式和主题色的初始化与实时同步
 * 所有Activity应继承此类以确保设置变更后实时生效
 */
public abstract class BaseActivity extends AppCompatActivity {

    // 记录当前Activity创建时的设置值，用于onResume检测变化
    private String currentLanguage;
    private int currentTheme;
    private int currentThemeColor;
    protected boolean isRestarting = false;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.applyLanguage(newBase));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // 在super.onCreate前应用主题模式（夜间模式）
        ThemeHelper.initTheme(this);
        super.onCreate(savedInstanceState);
        // 在super.onCreate后应用主题色overlay（必须在setContentView前）
        ThemeHelper.applyThemeColor(this);

        // 记录当前设置值，用于onResume检测变化
        currentLanguage = LocaleHelper.getLanguage(this);
        currentTheme = ThemeHelper.getTheme(this);
        currentThemeColor = ThemeHelper.getThemeColor(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isRestarting) return;
        // 检测语言、主题模式或主题色是否在外部被修改（如从设置页返回）
        String newLanguage = LocaleHelper.getLanguage(this);
        int newTheme = ThemeHelper.getTheme(this);
        int newThemeColor = ThemeHelper.getThemeColor(this);

        if (!newLanguage.equals(currentLanguage) || newTheme != currentTheme || newThemeColor != currentThemeColor) {
            isRestarting = true;
            restartSelf();
        }
    }

    /**
     * 重启当前Activity以应用新的设置
     */
    protected void restartSelf() {
        Intent intent = getIntent();
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        finish();
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    /**
     * 重启整个应用到主页（用于设置页变更后全局生效）
     */
    protected void restartApp() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finishAffinity();
        overridePendingTransition(0, 0);
    }
}
