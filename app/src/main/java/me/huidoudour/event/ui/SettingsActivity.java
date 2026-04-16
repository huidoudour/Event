package me.huidoudour.event.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import me.huidoudour.event.R;
import me.huidoudour.event.data.DataImportExportHelper;
import me.huidoudour.event.data.EventRepository;
import me.huidoudour.event.utils.LocaleHelper;

public class SettingsActivity extends AppCompatActivity {

    private EventViewModel viewModel;
    private EventRepository repository;
    private DataImportExportHelper dataHelper;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void attachBaseContext(Context newBase) {
        // 应用语言设置
        super.attachBaseContext(LocaleHelper.applyLanguage(newBase));
    }

    // 导出文件选择器
    private final ActivityResultLauncher<String> exportFileLauncher =
        registerForActivityResult(
            new ActivityResultContracts.CreateDocument("application/json"),
            uri -> {
                if (uri != null) {
                    executor.execute(() -> {
                        boolean success = dataHelper.exportDataToUri(repository, uri);
                        mainHandler.post(() -> {
                            if (success) {
                                Toast.makeText(this, R.string.export_success, Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(this, R.string.no_data_to_export, Toast.LENGTH_SHORT).show();
                            }
                        });
                    });
                }
            }
        );

    // 导入文件选择器
    private final ActivityResultLauncher<String> importFileLauncher =
        registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    showImportConfirmDialog(uri);
                }
            }
        );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        viewModel = new ViewModelProvider(this, new EventViewModel.Factory(getApplication()))
                .get(EventViewModel.class);
        repository = viewModel.getRepository();
        dataHelper = new DataImportExportHelper(this);

        setupToolbar();
        setupExportData();
        setupImportData();
        setupLanguageSettings();
        setupThemeSettings();
        setupAboutDeveloper();
    }

    private void setupToolbar() {
        com.google.android.material.appbar.MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    /** 导出数据 */
    private void setupExportData() {
        MaterialCardView cardExportData = findViewById(R.id.card_export_data);
        cardExportData.setOnClickListener(v ->
            exportFileLauncher.launch("events_backup_" + System.currentTimeMillis() + ".json")
        );
    }

    /** 导入数据 */
    private void setupImportData() {
        MaterialCardView cardImportData = findViewById(R.id.card_import_data);
        cardImportData.setOnClickListener(v ->
            importFileLauncher.launch("application/json")
        );
    }

    /** 显示导入确认对话框 */
    private void showImportConfirmDialog(Uri uri) {
        new MaterialAlertDialogBuilder(this)
            .setTitle(R.string.confirm_import)
            .setMessage(R.string.import_warning)
            .setPositiveButton(R.string.ok, (dialog, which) -> {
                executor.execute(() -> {
                    boolean success = dataHelper.importDataFromUri(repository, uri, true);
                    mainHandler.post(() -> {
                        if (success) {
                            Toast.makeText(this, R.string.import_success, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, R.string.import_failed, Toast.LENGTH_SHORT).show();
                        }
                    });
                });
            })
            .setNegativeButton(R.string.cancel, null)
            .show();
    }



    /** 语言设置 */
    private void setupLanguageSettings() {
        MaterialCardView cardLanguage = findViewById(R.id.card_language_settings);
        cardLanguage.setOnClickListener(v -> showLanguageDialog());
    }
    
    /** 显示语言选择对话框 */
    private void showLanguageDialog() {
        String[] languages = LocaleHelper.getSupportedLanguages();
        String[] languageNames = new String[languages.length];
        
        // 获取当前语言
        String currentLanguage = LocaleHelper.getLanguage(this);
        int checkedItem = 0;
        
        // 构建语言名称列表
        for (int i = 0; i < languages.length; i++) {
            languageNames[i] = LocaleHelper.getLanguageDisplayName(this, languages[i]);
            if (languages[i].equals(currentLanguage)) {
                checkedItem = i;
            }
        }
        
        new MaterialAlertDialogBuilder(this)
            .setTitle(R.string.select_language)
            .setSingleChoiceItems(languageNames, checkedItem, (dialog, which) -> {
                String selectedLanguage = languages[which];
                
                // 如果选择的语言和当前语言相同，不做任何操作
                if (selectedLanguage.equals(currentLanguage)) {
                    dialog.dismiss();
                    return;
                }
                
                // 保存并应用语言设置
                LocaleHelper.setLanguage(this, selectedLanguage);
                
                dialog.dismiss();
                
                // 显示Toast提示
                Toast.makeText(this, R.string.language_changed_restart, Toast.LENGTH_LONG).show();
                
                // 延迟重启App以应用新的语言设置
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    restartApp();
                }, 500);
            })
            .setNegativeButton(R.string.cancel, null)
            .show();
    }
    
    /** 重启整个App */
    private void restartApp() {
        // 获取启动Intent
        Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());
        if (intent != null) {
            // 添加标志以清除任务栈
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            // 退出当前Activity
            finish();
            // 退出整个应用进程
            System.exit(0);
        }
    }

    /** 主题设置（UI 预留，待实现） */
    private void setupThemeSettings() {
        MaterialCardView cardTheme = findViewById(R.id.card_theme_settings);
        cardTheme.setOnClickListener(v -> {
            // TODO: 显示主题选择对话框
        });
    }

    /** 关于开发者 */
    private void setupAboutDeveloper() {
        MaterialCardView cardAbout = findViewById(R.id.card_about_developer);
        cardAbout.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, MeActivity.class);
            startActivity(intent);
        });
    }
}
