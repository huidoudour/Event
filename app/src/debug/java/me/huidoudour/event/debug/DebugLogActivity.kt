package me.huidoudour.event.debug

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.DocumentsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import me.huidoudour.event.R
import me.huidoudour.event.util.ActionMonitor
import me.huidoudour.event.util.BaseActivity
import java.util.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors

/**
 * 调试日志页面。
 * 仅在 Debug 构建中存在，通过反射由 SettingsActivity 启动。
 */
class DebugLogActivity : BaseActivity() {

    private lateinit var viewModel: DebugLogViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: TextView
    private lateinit var tvLogCount: TextView
    private lateinit var tvSelectedPath: TextView
    private lateinit var btnSelectFolder: Button
    private lateinit var btnExportLogs: Button
    private lateinit var btnRefresh: ImageButton
    private lateinit var btnClear: ImageButton
    private lateinit var adapter: LogAdapter

    private var savedFolderUri: Uri? = null
    private val executor = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())

    // 文件夹选择器
    private val folderPickerLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            contentResolver.takePersistableUriPermission(uri, takeFlags)

            savedFolderUri = uri
            saveFolderUri(uri)
            updatePathDisplay()
            Toast.makeText(this, "保存位置已设置", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_debug_log)

        DebugLogInitializer.init(this)

        setupToolbar()
        setupViews()
        setupViewModel()
        loadSavedFolder()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            ActionMonitor.log("BTN_CLICK", "调试日志页返回", 0)
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupViews() {
        recyclerView = findViewById(R.id.recyclerViewLogs)
        emptyView = findViewById(R.id.emptyView)
        tvLogCount = findViewById(R.id.tvLogCount)
        tvSelectedPath = findViewById(R.id.tvSelectedPath)
        btnSelectFolder = findViewById(R.id.btnSelectFolder)
        btnExportLogs = findViewById(R.id.btnExportLogs)
        btnRefresh = findViewById(R.id.btnRefreshLogs)
        btnClear = findViewById(R.id.btnClearLogs)

        adapter = LogAdapter()
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        btnSelectFolder.setOnClickListener {
            ActionMonitor.log("BTN_CLICK", "调试日志页选择保存文件夹", 0)
            openFolderPicker()
        }
        btnExportLogs.setOnClickListener {
            ActionMonitor.log("BTN_CLICK", "调试日志页导出日志", 0)
            exportLogsToFolder()
        }
        btnRefresh.setOnClickListener {
            ActionMonitor.log("BTN_CLICK", "调试日志页刷新", 0)
            if (::viewModel.isInitialized) viewModel.refresh()
        }
        btnClear.setOnClickListener {
            ActionMonitor.log("BTN_CLICK", "调试日志页打开清空确认", 0)
            clearAllLogs()
        }
    }

    private fun setupViewModel() {
        viewModel = androidx.lifecycle.ViewModelProvider(
            this,
            DebugLogViewModel.Factory(application)
        )[DebugLogViewModel::class.java]

        viewModel.allLogs.observe(this) { logs ->
            adapter.submitList(logs)
            if (logs.isNullOrEmpty()) {
                recyclerView.visibility = View.GONE
                emptyView.visibility = View.VISIBLE
            } else {
                recyclerView.visibility = View.VISIBLE
                emptyView.visibility = View.GONE
            }
        }

        viewModel.logCount.observe(this) { count ->
            tvLogCount.text = "日志数：${count ?: 0}"
        }
    }

    // ── 文件夹选择与保存 ──

    private fun openFolderPicker() {
        folderPickerLauncher.launch(savedFolderUri)
    }

    private fun loadSavedFolder() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val uriStr = prefs.getString(KEY_SAVE_FOLDER_URI, "") ?: ""
        if (uriStr.isNotEmpty()) {
            savedFolderUri = Uri.parse(uriStr)
            updatePathDisplay()
        }
    }

    private fun saveFolderUri(uri: Uri) {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        prefs.edit().putString(KEY_SAVE_FOLDER_URI, uri.toString()).apply()
    }

    private fun updatePathDisplay() {
        val uri = savedFolderUri
        if (uri != null) {
            val path = uri.lastPathSegment
            tvSelectedPath.text = path ?: uri.toString()
            @Suppress("DEPRECATION")
            tvSelectedPath.setTextColor(resources.getColor(android.R.color.darker_gray))
        } else {
            tvSelectedPath.text = "未选择"
            @Suppress("DEPRECATION")
            tvSelectedPath.setTextColor(resources.getColor(android.R.color.darker_gray))
        }
    }

    // ── 导出日志 ──

    private fun exportLogsToFolder() {
        if (savedFolderUri == null) {
            Toast.makeText(this, "请先选择保存文件夹", Toast.LENGTH_SHORT).show()
            return
        }

        val uri = savedFolderUri ?: return
        executor.execute {
            try {
                val logs = viewModel.getAllLogsSync()
                if (logs.isEmpty()) {
                    mainHandler.post {
                        Toast.makeText(this, "暂无日志可导出", Toast.LENGTH_SHORT).show()
                    }
                    return@execute
                }

                // 构建 JSON
                val jsonArray = JSONArray()
                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
                for (entry in logs) {
                    val obj = JSONObject()
                    obj.put("id", entry.id)
                    obj.put("timestamp", sdf.format(Date(entry.timestamp)))
                    obj.put("operation", entry.operation)
                    obj.put("detail", entry.detail)
                    obj.put("entityId", entry.entityId)
                    jsonArray.put(obj)
                }

                // 文件名：debug_logs_20260705_183000.json
                val fileSdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                val fileName = "debug_logs_${fileSdf.format(Date())}.json"

                // tree URI 转 document URI
                val treeDocId = DocumentsContract.getTreeDocumentId(uri)
                val dirDocUri = DocumentsContract.buildDocumentUriUsingTree(uri, treeDocId)
                val fileUri = DocumentsContract.createDocument(
                    contentResolver, dirDocUri, "application/json", fileName
                )

                val success = contentResolver.openOutputStream(fileUri!!)?.use { os ->
                    os.write(jsonArray.toString(2).toByteArray(Charsets.UTF_8))
                    true
                } ?: false

                mainHandler.post {
                    if (success) {
                        Toast.makeText(this, "日志已导出为 $fileName", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this, "导出失败，请检查目录权限", Toast.LENGTH_SHORT).show()
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
                mainHandler.post {
                    Toast.makeText(this, "导出出错：${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun clearAllLogs() {
        AlertDialog.Builder(this)
            .setTitle("清空日志")
            .setMessage("确定要清空所有调试操作日志吗？")
            .setPositiveButton("清空") { _, _ ->
                ActionMonitor.log("DIALOG_CONFIRM", "确认清空调试日志", 0)
                executor.execute { viewModel.deleteAll() }
                Toast.makeText(this, "日志已清空", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("取消") { _, _ ->
                ActionMonitor.log("DIALOG_CANCEL", "取消清空调试日志", 0)
            }
            .show()
    }

    // ── RecyclerView 适配器 ──

    private class LogAdapter : RecyclerView.Adapter<LogAdapter.LogViewHolder>() {
        private var logs: List<DebugLogEntry> = emptyList()

        fun submitList(list: List<DebugLogEntry>?) {
            this.logs = list ?: emptyList()
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(android.R.layout.simple_list_item_2, parent, false)
            return LogViewHolder(view)
        }

        override fun onBindViewHolder(holder: LogViewHolder, position: Int) {
            holder.bind(logs[position])
        }

        override fun getItemCount(): Int = logs.size

        class LogViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val text1: TextView = itemView.findViewById(android.R.id.text1)
            private val text2: TextView = itemView.findViewById(android.R.id.text2)

            fun bind(entry: DebugLogEntry) {
                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val timestamp = sdf.format(Date(entry.timestamp))
                val entityInfo = if (entry.entityId > 0) " [ID:${entry.entityId}]" else ""
                text1.text = "[${entry.operation}]$entityInfo  $timestamp"
                text2.text = entry.detail
            }
        }
    }

    companion object {
        private const val PREFS_NAME = "debug_log_prefs"
        private const val KEY_SAVE_FOLDER_URI = "save_folder_uri"
    }
}
