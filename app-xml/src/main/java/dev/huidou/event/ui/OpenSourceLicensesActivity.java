package dev.huidou.event.ui;

import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dev.huidou.event.R;
import dev.huidou.event.utils.LocaleHelper;
import dev.huidou.event.utils.ThemeHelper;

/**
 * 开源许可页面（Java + XML 实现）：
 * 展示本项目与使用的第三方开源依赖、构建工具的许可证信息。
 * 点击条目展开协议全文，行尾外链按钮跳转项目主页。
 */
public class OpenSourceLicensesActivity extends AppCompatActivity {

    // ── 项目主页链接 ──
    private static final String URL_EVENT = "https://github.com/huidoudour/Event";
    private static final String URL_APPCOMPAT = "https://developer.android.com/jetpack/androidx/releases/appcompat";
    private static final String URL_FRAGMENT = "https://developer.android.com/jetpack/androidx/releases/fragment";
    private static final String URL_LIFECYCLE = "https://developer.android.com/jetpack/androidx/releases/lifecycle";
    private static final String URL_MATERIAL = "https://github.com/material-components/material-components-android";
    private static final String URL_SQLITE = "https://github.com/sqlite/sqlite";
    private static final String URL_MTFILES = "https://github.com/L-JINBIN/MTDataFilesProvider";
    private static final String URL_KOTLIN = "https://github.com/JetBrains/kotlin";
    private static final String URL_AGP = "https://developer.android.com/build/releases/gradle-plugin";

    // ── assets/licenses/ 下的协议全文文件 ──
    private static final String LIC_APACHE = "apache-2.0.txt";
    private static final String LIC_MIT_EVENT = "mit-event.txt";
    private static final String LIC_SQLITE_PD = "sqlite-public-domain.txt";

    private LinearLayout container;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    /** 单个开源项目条目 */
    private static final class LicenseItem {
        final String name;
        final String version;
        final String license;
        final String url;
        final String licenseFile;

        LicenseItem(String name, String version, String license, String url, String licenseFile) {
            this.name = name;
            this.version = version;
            this.license = license;
            this.url = url;
            this.licenseFile = licenseFile;
        }
    }

    @Override
    protected void attachBaseContext(android.content.Context newBase) {
        // 应用默认回退语言（简体中文）
        super.attachBaseContext(LocaleHelper.applyLanguage(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 在 onCreate 开始时初始化主题（固定浅色）
        ThemeHelper.initTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_source_licenses);

        container = findViewById(R.id.licensesContainer);
        setupToolbar();
        renderLicenses();
    }

    private void setupToolbar() {
        com.google.android.material.appbar.MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    /** 渲染三个分组：本项目 / 第三方开源依赖 / 构建工具 */
    private void renderLicenses() {
        String appVersion = getAppVersion();

        addGroup(R.string.licenses_this_app, Arrays.asList(
            new LicenseItem(getString(R.string.app_name), "v" + appVersion,
                "MIT License", URL_EVENT, LIC_MIT_EVENT)
        ));

        addGroup(R.string.licenses_third_party, Arrays.asList(
            new LicenseItem("AndroidX AppCompat", "1.8.0", "Apache License 2.0", URL_APPCOMPAT, LIC_APACHE),
            new LicenseItem("AndroidX Fragment KTX", "1.9.0", "Apache License 2.0", URL_FRAGMENT, LIC_APACHE),
            new LicenseItem("AndroidX Lifecycle (ViewModel / LiveData)", "2.11.0", "Apache License 2.0", URL_LIFECYCLE, LIC_APACHE),
            new LicenseItem("Material Components for Android", "1.14.0", "Apache License 2.0", URL_MATERIAL, LIC_APACHE),
            new LicenseItem("SQLite (org.sqlite 原生库)", "3.49.0", "Public Domain", URL_SQLITE, LIC_SQLITE_PD),
            new LicenseItem("MTDataFilesProvider", "v1.0.0",
                getString(R.string.license_not_provided), URL_MTFILES, null)
        ));

        addGroup(R.string.licenses_build_tools, Arrays.asList(
            new LicenseItem("Kotlin", "2.4.10", "Apache License 2.0", URL_KOTLIN, LIC_APACHE),
            new LicenseItem("Android Gradle Plugin", "9.3.1", "Apache License 2.0", URL_AGP, LIC_APACHE)
        ));
    }

    /** 添加一个分组：标题 + 条目列表 */
    private void addGroup(int titleRes, List<LicenseItem> items) {
        TextView header = new TextView(this);
        header.setText(titleRes);
        header.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        header.setTypeface(null, Typeface.BOLD);
        header.setTextColor(getColor(R.color.settings_section_title));
        LinearLayout.LayoutParams hp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        hp.setMargins(dp(16), dp(8), dp(16), dp(8));
        header.setLayoutParams(hp);
        container.addView(header);

        for (LicenseItem item : items) {
            View v = LayoutInflater.from(this).inflate(R.layout.item_license, container, false);
            bindLicItem(v, item);
            container.addView(v);
        }
    }

    /** 绑定单个许可条目 */
    private void bindLicItem(View v, LicenseItem item) {
        TextView name = v.findViewById(R.id.licenseName);
        TextView version = v.findViewById(R.id.licenseVersion);
        TextView tag = v.findViewById(R.id.licenseTag);
        ImageView iconExpand = v.findViewById(R.id.iconExpand);
        LinearLayout rowMain = v.findViewById(R.id.rowMain);
        LinearLayout body = v.findViewById(R.id.licenseBody);
        TextView text = v.findViewById(R.id.licenseText);
        MaterialCardView btnLink = v.findViewById(R.id.linkButton);

        name.setText(item.name);
        version.setText(item.version);
        tag.setText(item.license);

        // 未提供协议全文的条目不可展开
        boolean expandable = item.licenseFile != null;
        iconExpand.setVisibility(expandable ? View.VISIBLE : View.GONE);
        rowMain.setEnabled(expandable);

        final boolean[] expanded = {false};
        rowMain.setOnClickListener(view -> {
            if (!expandable) return;
            expanded[0] = !expanded[0];
            body.setVisibility(expanded[0] ? View.VISIBLE : View.GONE);
            iconExpand.setRotation(expanded[0] ? 180f : 0f);
            if (expanded[0] && text.length() == 0) {
                loadLicenseText(item.licenseFile, text);
            }
        });

        btnLink.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(item.url));
            startActivity(intent);
        });
    }

    /** 后台读取协议全文，避免阻塞主线程 */
    private void loadLicenseText(String fileName, TextView target) {
        executor.execute(() -> {
            String txt = "";
            try (java.io.BufferedReader reader =
                     new java.io.BufferedReader(
                         new java.io.InputStreamReader(
                             getAssets().open("licenses/" + fileName), "UTF-8"))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append('\n');
                }
                txt = sb.toString();
            } catch (Exception e) {
                txt = "";
            }
            final String s = txt;
            mainHandler.post(() -> target.setText(
                s.isEmpty() ? getString(R.string.license_not_provided) : s));
        });
    }

    private String getAppVersion() {
        try {
            return getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (Exception e) {
            return "";
        }
    }

    private int dp(int value) {
        return (int) (getResources().getDisplayMetrics().density * value);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
