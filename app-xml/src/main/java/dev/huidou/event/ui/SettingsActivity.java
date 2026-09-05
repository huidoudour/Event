package dev.huidou.event.ui;

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

import dev.huidou.event.MeActivity;
import dev.huidou.event.R;
import dev.huidou.event.data.DataImportExportHelper;
import dev.huidou.event.data.EventRepository;
import dev.huidou.event.utils.LocaleHelper;
import dev.huidou.event.utils.ThemeHelper;
import dev.huidou.event.utils.ViewModeHelper;

public class SettingsActivity extends AppCompatActivity {

    private EventViewModel viewModel;
    private EventRepository repository;
    private DataImportExportHelper dataHelper;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void attachBaseContext(Context newBase) {
        // 应用默认回退语言（简体中文）
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
        // 在 onCreate 开始时初始化主题（固定浅色）
        ThemeHelper.initTheme(this);
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
        setupDataDisplayMode();
        setupSortSettings();
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



    /** 语言设置（多语言已移除，保留入口但禁用） */
    private void setupLanguageSettings() {
        MaterialCardView cardLanguage = findViewById(R.id.card_language_settings);
        // 多语言已移除，语言固定为默认回退语言，禁用切换入口
        cardLanguage.setEnabled(false);
        cardLanguage.setAlpha(0.5f);
    }

    /** 主题设置（深色主题已移除，保留入口但禁用） */
    private void setupThemeSettings() {
        MaterialCardView cardTheme = findViewById(R.id.card_theme_settings);
        // 深色主题已移除，主题固定为浅色，禁用切换入口
        cardTheme.setEnabled(false);
        cardTheme.setAlpha(0.5f);
    }

    /** 数据展示模式设置 */
    private void setupDataDisplayMode() {
        MaterialCardView cardDataDisplayMode = findViewById(R.id.card_data_display_mode);
        
        cardDataDisplayMode.setOnClickListener(v -> showViewModeDialog());
    }
    
    /** 显示视图模式选择对话框 */
    private void showViewModeDialog() {
        int[] modes = {ViewModeHelper.VIEW_MODE_CARD, ViewModeHelper.VIEW_MODE_LIST};
        String[] modeNames = {
            getString(R.string.card_view),
            getString(R.string.list_view)
        };
        
        // 获取当前视图模式
        int currentMode = ViewModeHelper.getViewMode(this);
        int checkedItem = 0;
        
        // 找到当前选中项
        for (int i = 0; i < modes.length; i++) {
            if (modes[i] == currentMode) {
                checkedItem = i;
                break;
            }
        }
        
        new MaterialAlertDialogBuilder(this)
            .setTitle(R.string.data_display_mode)
            .setSingleChoiceItems(modeNames, checkedItem, (dialog, which) -> {
                int selectedMode = modes[which];
                
                // 如果选择的模式和当前模式相同，不做任何操作
                if (selectedMode == currentMode) {
                    dialog.dismiss();
                    return;
                }
                
                // 保存视图模式设置
                ViewModeHelper.setViewMode(this, selectedMode);
                
                dialog.dismiss();
                
                // 显示Toast提示
                android.widget.Toast.makeText(this, R.string.view_mode_changed, android.widget.Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton(R.string.cancel, null)
            .show();
    }

    /** 排序设置 */
    private void setupSortSettings() {
        MaterialCardView cardSortSettings = findViewById(R.id.card_sort_settings);
        cardSortSettings.setOnClickListener(v -> showSortOrderDialog());
    }
    
    /** 显示排序顺序选择对话框 */
    private void showSortOrderDialog() {
        boolean isAscending = viewModel.getRepository().isAscending();
        int checkedItem = isAscending ? 0 : 1;
        
        String[] sortOrderNames = {
            getString(R.string.sort_ascending),
            getString(R.string.sort_descending)
        };
        
        new MaterialAlertDialogBuilder(this)
            .setTitle(R.string.sort_order_settings)
            .setSingleChoiceItems(sortOrderNames, checkedItem, (dialog, which) -> {
                boolean selectedAscending = (which == 0);
                
                // 如果选择的排序和当前相同，不做任何操作
                if (selectedAscending == isAscending) {
                    dialog.dismiss();
                    return;
                }
                
                // 更新排序顺序
                viewModel.toggleSortOrder();
                
                dialog.dismiss();
                
                // 显示Toast提示
                String sortOrder = selectedAscending
                    ? getString(R.string.sort_ascending)
                    : getString(R.string.sort_descending);
                Toast.makeText(this, sortOrder, Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton(R.string.cancel, null)
            .show();
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
