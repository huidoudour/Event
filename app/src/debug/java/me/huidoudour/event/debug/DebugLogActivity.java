package me.huidoudour.event.debug;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.DocumentsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import me.huidoudour.event.R;
import me.huidoudour.event.utils.BaseActivity;

public class DebugLogActivity extends BaseActivity {

    private static final String PREFS_NAME = "debug_log_prefs";
    private static final String KEY_SAVE_FOLDER_URI = "save_folder_uri";

    private DebugLogViewModel viewModel;
    private RecyclerView recyclerView;
    private TextView emptyView;
    private TextView tvLogCount;
    private TextView tvSelectedPath;
    private Button btnSelectFolder, btnExportLogs;
    private ImageButton btnRefresh, btnClear;
    private LogAdapter adapter;

    private Uri savedFolderUri;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    // 文件夹选择器
    private final ActivityResultLauncher<Uri> folderPickerLauncher =
        registerForActivityResult(
            new ActivityResultContracts.OpenDocumentTree(),
            uri -> {
                if (uri != null) {
                    // 获取持久化读写权限
                    final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                                        | Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
                    getContentResolver().takePersistableUriPermission(uri, takeFlags);

                    savedFolderUri = uri;
                    saveFolderUri(uri);
                    updatePathDisplay();
                    Toast.makeText(this, "保存位置已设置", Toast.LENGTH_SHORT).show();
                }
            }
        );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug_log);

        DebugLogInitializer.init(this);

        setupToolbar();
        setupViews();
        setupViewModel();
        loadSavedFolder();
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupViews() {
        recyclerView = findViewById(R.id.recyclerViewLogs);
        emptyView = findViewById(R.id.emptyView);
        tvLogCount = findViewById(R.id.tvLogCount);
        tvSelectedPath = findViewById(R.id.tvSelectedPath);
        btnSelectFolder = findViewById(R.id.btnSelectFolder);
        btnExportLogs = findViewById(R.id.btnExportLogs);
        btnRefresh = findViewById(R.id.btnRefreshLogs);
        btnClear = findViewById(R.id.btnClearLogs);

        adapter = new LogAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        btnSelectFolder.setOnClickListener(v -> openFolderPicker());
        btnExportLogs.setOnClickListener(v -> exportLogsToFolder());
        btnRefresh.setOnClickListener(v -> {
            if (viewModel != null) viewModel.refresh();
        });
        btnClear.setOnClickListener(v -> clearAllLogs());
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this, new DebugLogViewModel.Factory(getApplication()))
                .get(DebugLogViewModel.class);

        viewModel.getAllLogs().observe(this, logs -> {
            adapter.submitList(logs);
            if (logs == null || logs.isEmpty()) {
                recyclerView.setVisibility(View.GONE);
                emptyView.setVisibility(View.VISIBLE);
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                emptyView.setVisibility(View.GONE);
            }
        });

        viewModel.getLogCount().observe(this, count -> {
            tvLogCount.setText("日志数：" + (count != null ? count : 0));
        });
    }

    // ── 文件夹选择与保存 ──

    private void openFolderPicker() {
        // 若有已保存的目录，以其作为初始目录，否则 null
        folderPickerLauncher.launch(savedFolderUri);
    }

    private void loadSavedFolder() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String uriStr = prefs.getString(KEY_SAVE_FOLDER_URI, "");
        if (!uriStr.isEmpty()) {
            savedFolderUri = Uri.parse(uriStr);
            updatePathDisplay();
        }
    }

    private void saveFolderUri(Uri uri) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putString(KEY_SAVE_FOLDER_URI, uri.toString()).apply();
    }

    private void updatePathDisplay() {
        if (savedFolderUri != null) {
            // 显示 URI 的最后一段路径，更友好
            String path = savedFolderUri.getLastPathSegment();
            tvSelectedPath.setText(path != null ? path : savedFolderUri.toString());
            tvSelectedPath.setTextColor(getColor(android.R.color.darker_gray));
        } else {
            tvSelectedPath.setText("未选择");
            tvSelectedPath.setTextColor(
                getResources().getColor(android.R.color.darker_gray));
        }
    }

    // ── 导出日志 ──

    /**
     * 将全部日志导出为 JSON 文件，保存到用户通过 SAF 选择的目录中。
     */
    private void exportLogsToFolder() {
        if (savedFolderUri == null) {
            Toast.makeText(this, "请先选择保存文件夹", Toast.LENGTH_SHORT).show();
            return;
        }

        executor.execute(() -> {
            try {
                List<DebugLogEntry> logs = viewModel.getAllLogsSync();
                if (logs == null || logs.isEmpty()) {
                    mainHandler.post(() -> Toast.makeText(this, "暂无日志可导出", Toast.LENGTH_SHORT).show());
                    return;
                }

                // 构建 JSON
                JSONArray jsonArray = new JSONArray();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault());
                for (DebugLogEntry entry : logs) {
                    JSONObject obj = new JSONObject();
                    obj.put("id", entry.getId());
                    obj.put("timestamp", sdf.format(new Date(entry.getTimestamp())));
                    obj.put("operation", entry.getOperation());
                    obj.put("detail", entry.getDetail());
                    obj.put("entityId", entry.getEntityId());
                    jsonArray.put(obj);
                }

                // 文件名：debug_logs_20260705_183000.json
                SimpleDateFormat fileSdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
                String fileName = "debug_logs_" + fileSdf.format(new Date()) + ".json";

                // tree URI 转 document URI，再调用 createDocument
                String treeDocId = DocumentsContract.getTreeDocumentId(savedFolderUri);
                Uri dirDocUri = DocumentsContract.buildDocumentUriUsingTree(savedFolderUri, treeDocId);
                Uri fileUri = DocumentsContract.createDocument(
                        getContentResolver(), dirDocUri, "application/json", fileName);

                boolean success;
                try (OutputStream os = getContentResolver().openOutputStream(fileUri)) {
                    if (os != null) {
                        os.write(jsonArray.toString(2).getBytes("UTF-8"));
                        success = true;
                    } else {
                        success = false;
                    }
                }

                boolean finalSuccess = success;
                mainHandler.post(() -> {
                    if (finalSuccess) {
                        Toast.makeText(this, "日志已导出为 " + fileName, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "导出失败，请检查目录权限", Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                mainHandler.post(() -> Toast.makeText(this, "导出出错：" + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void clearAllLogs() {
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle("清空日志")
                .setMessage("确定要清空所有调试操作日志吗？")
                .setPositiveButton("清空", (dialog, which) -> {
                    executor.execute(() -> viewModel.deleteAll());
                    Toast.makeText(this, "日志已清空", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    // ── RecyclerView 适配器 ──

    private static class LogAdapter extends RecyclerView.Adapter<LogAdapter.LogViewHolder> {
        private List<DebugLogEntry> logs;

        public void submitList(List<DebugLogEntry> list) {
            this.logs = list;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_2, parent, false);
            return new LogViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
            holder.bind(logs.get(position));
        }

        @Override
        public int getItemCount() {
            return logs == null ? 0 : logs.size();
        }

        static class LogViewHolder extends RecyclerView.ViewHolder {
            private final TextView text1;
            private final TextView text2;

            LogViewHolder(View itemView) {
                super(itemView);
                text1 = itemView.findViewById(android.R.id.text1);
                text2 = itemView.findViewById(android.R.id.text2);
            }

            void bind(DebugLogEntry entry) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                String timestamp = sdf.format(new Date(entry.getTimestamp()));
                String entityInfo = entry.getEntityId() > 0 ? " [ID:" + entry.getEntityId() + "]" : "";
                text1.setText("[" + entry.getOperation() + "]" + entityInfo + "  " + timestamp);
                text2.setText(entry.getDetail());
            }
        }
    }
}
