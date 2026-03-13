package me.huidoudour.event.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import me.huidoudour.event.R;
import me.huidoudour.event.data.DataImportExportHelper;
import me.huidoudour.event.data.EventRepository;

public class SettingsActivity extends AppCompatActivity {
    
    private EventViewModel viewModel;
    private EventRepository repository;
    private DataImportExportHelper dataHelper;
    
    // 文件选择器
    private final ActivityResultLauncher<String> exportFileLauncher = 
        registerForActivityResult(
            new ActivityResultContracts.CreateDocument("application/json"),
            uri -> {
                if (uri != null) {
                    if (dataHelper.exportDataToUri(repository, uri)) {
                        Toast.makeText(this, R.string.export_success, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, R.string.no_data_to_export, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        );
    
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
        
        // Initialize ViewModel and Repository
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
    
    /**
     * 导出数据功能
     */
    private void setupExportData() {
        MaterialCardView cardExportData = findViewById(R.id.card_export_data);
        cardExportData.setOnClickListener(v -> {
            // 打开文件选择器，让用户选择保存位置
            exportFileLauncher.launch("events_backup_" + System.currentTimeMillis() + ".json");
        });
    }
    
    /**
     * 导入数据功能
     */
    private void setupImportData() {
        MaterialCardView cardImportData = findViewById(R.id.card_import_data);
        cardImportData.setOnClickListener(v -> {
            // 打开文件选择器，让用户选择 JSON 文件
            importFileLauncher.launch("application/json");
        });
    }
    
    /**
     * 显示导入确认对话框
     */
    private void showImportConfirmDialog(Uri uri) {
        new MaterialAlertDialogBuilder(this)
            .setTitle(R.string.confirm_import)
            .setMessage(R.string.import_warning)
            .setPositiveButton(R.string.ok, (dialog, which) -> {
                if (dataHelper.importDataFromUri(repository, uri, true)) {
                    Toast.makeText(this, R.string.import_success, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, R.string.import_failed, Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton(R.string.cancel, null)
            .show();
    }
    
    /**
     * 语言设置功能（仅 UI）
     */
    private void setupLanguageSettings() {
        MaterialCardView cardLanguage = findViewById(R.id.card_language_settings);
        cardLanguage.setOnClickListener(v -> {
            // TODO: 显示语言选择对话框
            // 目前仅展示 UI，功能暂不实现
        });
    }
    
    /**
     * 主题设置功能（仅 UI）
     */
    private void setupThemeSettings() {
        MaterialCardView cardTheme = findViewById(R.id.card_theme_settings);
        cardTheme.setOnClickListener(v -> {
            // TODO: 显示主题选择对话框
            // 目前仅展示 UI，功能暂不实现
        });
    }
    
    /**
     * 关于开发者功能
     */
    private void setupAboutDeveloper() {
        MaterialCardView cardAbout = findViewById(R.id.card_about_developer);
        cardAbout.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, me.huidoudour.event.MeActivity.class);
            startActivity(intent);
        });
    }
}
