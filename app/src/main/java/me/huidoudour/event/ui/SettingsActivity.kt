package me.huidoudour.event.ui

import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.FileProvider
import androidx.core.content.edit
import androidx.lifecycle.ViewModelProvider
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.schedulers.Schedulers
import me.huidoudour.event.MeActivity
import me.huidoudour.event.R
import me.huidoudour.event.data.DataImportExportHelper
import me.huidoudour.event.ui.theme.EventTheme
import me.huidoudour.event.util.BaseActivity
import me.huidoudour.event.util.ThemeHelper
import java.io.File

class SettingsActivity : BaseActivity() {

    private lateinit var viewModel: EventViewModel
    private lateinit var dataHelper: DataImportExportHelper
    // RxKotlin / RxJava 统一管理导入/导出异步任务，onDestroy 时自动释放
    private val disposables = CompositeDisposable()

    // 使用 mutableStateOf 驱动 Compose 重组，避免不必要的 recreate() 导致 UI 抖动
    private var isDarkTheme by mutableStateOf(false)
    private var isAscending by mutableStateOf(false)

    private val exportFileLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            Single.fromCallable {
                dataHelper.exportDataToUri(viewModel.getRepository(), uri)
            }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                    onSuccess = { success ->
                        if (success) {
                            Toast.makeText(this, R.string.export_success, Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, R.string.no_data_to_export, Toast.LENGTH_SHORT).show()
                        }
                    },
                    onError = { e -> Log.e(TAG, "exportDataToUri failed", e) }
                )
                .addTo(disposables)
        }
    }

    /** 导入：FileManager ACTION_GET_CONTENT 或系统 SAF */
    private val importFileLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                Log.d(TAG, "importFileLauncher: uri=$uri")
                showImportConfirmDialog(uri)
            }
        }
    }

    /** 导出到 FileManager：ACTION_SEND 结果处理 */
    private val exportToFileManagerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            Toast.makeText(this, R.string.export_success, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // enableEdgeToEdge 必须在 super.onCreate() 之前调用
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(
            this,
            EventViewModel.Factory(application)
        )[EventViewModel::class.java]
        dataHelper = DataImportExportHelper(this)

        // 初始化状态
        isDarkTheme = ThemeHelper.getTheme(this) == ThemeHelper.THEME_DARK ||
                (ThemeHelper.getTheme(this) == ThemeHelper.THEME_SYSTEM && isNightMode())
        isAscending = getSortPrefs().getBoolean("sort_ascending", false)

        applyContent()
    }

    override fun onDestroy() {
        disposables.dispose()
        super.onDestroy()
    }

    /**
     * 设置 Compose 内容。
     * 使用 key(isDarkTheme) 驱动主题切换时 Compose 树的完全重建。
     */
    private fun applyContent() {
        setContent {
            key(isDarkTheme) {
                EventTheme(darkTheme = isDarkTheme) {
                    SettingsScreenContent(
                        onBack = { finish() },
                        onAboutDeveloper = {
                            startActivity(Intent(this, MeActivity::class.java))
                        },
                        onOpenLicenses = {
                            startActivity(Intent(this, OpenSourceLicensesActivity::class.java))
                        },
                        onExport = {
                            val installed = isFileManagerInstalled()
                            Log.d(TAG, "onExport: isFileManagerInstalled=$installed")
                            if (installed) {
                                // 优先使用 FileManager 保存（Maybe：null 表示无数据可导出，走 onComplete）
                                Maybe.fromCallable {
                                    writeExportJsonToCache(viewModel.getRepository())
                                }
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribeBy(
                                        onSuccess = { file -> launchExportToFileManager(file) },
                                        onError = { e -> Log.e(TAG, "writeExportJsonToCache failed", e) },
                                        onComplete = {
                                            Toast.makeText(
                                                this@SettingsActivity,
                                                R.string.no_data_to_export,
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    )
                                    .addTo(disposables)
                            } else {
                                exportFileLauncher.launch("events_backup_${System.currentTimeMillis()}.json")
                            }
                        },
                        onImport = {
                            val installed = isFileManagerInstalled()
                            Log.d(TAG, "onImport: isFileManagerInstalled=$installed")
                            val intent = if (installed) {
                                Intent(Intent.ACTION_GET_CONTENT).apply {
                                    type = "application/json"
                                    setClassName(FILE_MANAGER_PACKAGE, "$FILE_MANAGER_PACKAGE.MainActivity")
                                }
                            } else {
                                Intent(Intent.ACTION_GET_CONTENT).apply {
                                    type = "application/json"
                                    addCategory(Intent.CATEGORY_OPENABLE)
                                }
                            }
                            Log.d(TAG, "onImport: launching intent=$intent")
                            try {
                                importFileLauncher.launch(intent)
                            } catch (e: Exception) {
                                Log.e(TAG, "onImport: launch failed", e)
                                Toast.makeText(this@SettingsActivity, getString(R.string.cannot_launch_file_picker, e.message), Toast.LENGTH_SHORT).show()
                            }
                        },
                        isAscending = isAscending,
                        onSortOrderChanged = { ascending ->
                            isAscending = ascending
                            getSortPrefs().edit { putBoolean("sort_ascending", ascending) }
                        },
                        onThemeChanged = { theme ->
                            ThemeHelper.saveTheme(this@SettingsActivity, theme)
                            recreate()
                        },
                        onNeedsRecreate = {
                            Toast.makeText(this, R.string.settings_applied, Toast.LENGTH_SHORT).show()
                            recreate()
                        }
                    )
                }
            }
        }
    }

    private fun isNightMode(): Boolean {
        val mode = resources.configuration.uiMode and
                android.content.res.Configuration.UI_MODE_NIGHT_MASK
        return mode == android.content.res.Configuration.UI_MODE_NIGHT_YES
    }

    private fun getSortPrefs(): SharedPreferences {
        return getSharedPreferences("sort_prefs", MODE_PRIVATE)
    }

    // =========================================================================
    //  导出：FileManager 优先
    // =========================================================================

    /** 将导出 JSON 写入 cache/shared/ 目录，返回临时文件 */
    private fun writeExportJsonToCache(repository: me.huidoudour.event.data.EventRepository): File? {
        return try {
            val events = repository.getAllEventsSync()
            if (events.isEmpty()) return null
            val json = buildExportJson(events) ?: return null
            val dir = File(cacheDir, "shared")
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, "events_backup_${System.currentTimeMillis()}.json")
            file.writeText(json, Charsets.UTF_8)
            Log.d(TAG, "writeExportJsonToCache: ${file.absolutePath} (${file.length()} bytes)")
            file
        } catch (e: Exception) {
            Log.e(TAG, "writeExportJsonToCache: failed", e)
            null
        }
    }

    /** 构建导出 JSON 字符串（与 DataImportExportHelper 格式一致） */
    private fun buildExportJson(events: List<me.huidoudour.event.data.Event>): String? {
        if (events.isEmpty()) return null
        // 每个导出任务只创建一个日期格式实例，避免在循环中反复 new
        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.US)
        val sb = StringBuilder("[\n")
        events.forEachIndexed { index, event ->
            if (index > 0) sb.append(",\n")
            sb.append("  {\n")
            sb.append("    \"事件标题\": \"${event.title.replace("\\", "\\\\").replace("\"", "\\\"")}\",\n")
            sb.append("    \"事件详情\": \"${(event.description ?: "").replace("\\", "\\\\").replace("\"", "\\\"")}\",\n")
            sb.append("    \"事件时间\": \"${dateFormat.format(java.util.Date(event.eventTime))}\",\n")
            sb.append("    \"createdAt\": ${event.createdAt},\n")
            sb.append("    \"updatedAt\": ${event.updatedAt}\n")
            sb.append("  }")
        }
        sb.append("\n]")
        return sb.toString()
    }

    /** 通过 ACTION_SEND 将临时文件分享给 FileManager */
    private fun launchExportToFileManager(file: File) {
        try {
            val uri = FileProvider.getUriForFile(
                this,
                "${packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/json"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                setClassName(FILE_MANAGER_PACKAGE, "$FILE_MANAGER_PACKAGE.MainActivity")
            }
            Log.d(TAG, "launchExportToFileManager: ACTION_SEND uri=$uri")
            exportToFileManagerLauncher.launch(intent)
        } catch (e: Exception) {
            Log.e(TAG, "launchExportToFileManager: failed", e)
            file.delete()
            Toast.makeText(this, getString(R.string.cannot_launch_file_manager, e.message), Toast.LENGTH_SHORT).show()
        }
    }

    // =========================================================================
    //  导入：FileManager 优先
    // =========================================================================

    private fun isFileManagerInstalled(): Boolean {
        return try {
            packageManager.getPackageInfo(FILE_MANAGER_PACKAGE, 0)
            true
        } catch (_: PackageManager.NameNotFoundException) {
            false
        }
    }

    private fun showImportConfirmDialog(uri: Uri) {
        // 主题实时切换后 Activity 不重建，需要包装 Context 保证对话框深浅配色正确
        androidx.appcompat.app.AlertDialog.Builder(ThemeHelper.createNightAwareContext(this))
            .setTitle(R.string.confirm_import)
            .setMessage(R.string.import_warning)
            .setPositiveButton(R.string.ok) { _, _ ->
                Single.fromCallable {
                    dataHelper.importDataFromUri(viewModel.getRepository(), uri, true)
                }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeBy(
                        onSuccess = { success ->
                            if (success) {
                                Toast.makeText(this, R.string.import_success, Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(this, R.string.import_failed, Toast.LENGTH_SHORT).show()
                            }
                        },
                        onError = { e -> Log.e(TAG, "importDataFromUri failed", e) }
                    )
                    .addTo(disposables)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    companion object {
        private const val TAG = "SettingsActivity"
        /** FileManager 文件管理器包名 */
        private const val FILE_MANAGER_PACKAGE = "me.huidoudour.file.manager"
    }
}
