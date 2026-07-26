package me.huidoudour.event.ui

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.edit
import androidx.lifecycle.ViewModelProvider
import me.huidoudour.event.MeActivity
import me.huidoudour.event.R
import me.huidoudour.event.data.DataImportExportHelper
import me.huidoudour.event.ui.theme.EventTheme
import me.huidoudour.event.util.LocaleHelper
import me.huidoudour.event.util.ThemeHelper
import java.util.concurrent.Executors

class SettingsActivity : ComponentActivity() {

    private lateinit var viewModel: EventViewModel
    private lateinit var dataHelper: DataImportExportHelper
    private val executor = Executors.newSingleThreadExecutor()

    // 使用 mutableStateOf 驱动 Compose 重组，避免不必要的 recreate() 导致 UI 抖动
    private var isDarkTheme by mutableStateOf(false)
    private var themeColor by mutableIntStateOf(ThemeHelper.COLOR_DEFAULT)
    private var isAscending by mutableStateOf(false)

    private val exportFileLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            executor.execute {
                val repo = viewModel.getRepository()
                val success = dataHelper.exportDataToUri(repo, uri)
                runOnUiThread {
                    if (success) {
                        Toast.makeText(this, R.string.export_success, Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, R.string.no_data_to_export, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private val importFileLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            showImportConfirmDialog(uri)
        }
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.applyLanguage(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeHelper.initTheme(this)
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
        themeColor = ThemeHelper.getThemeColor(this)
        isAscending = getSortPrefs().getBoolean("sort_ascending", false)

        setContent {
            EventTheme(
                themeColor = themeColor,
                darkTheme = isDarkTheme
            ) {
                SettingsScreenContent(
                    onBack = { finish() },
                    onAboutDeveloper = {
                        startActivity(Intent(this, MeActivity::class.java))
                    },
                    onExport = {
                        exportFileLauncher.launch("events_backup_${System.currentTimeMillis()}.json")
                    },
                    onImport = {
                        importFileLauncher.launch("application/json")
                    },
                    isAscending = isAscending,
                    onSortOrderChanged = { ascending ->
                        isAscending = ascending
                        getSortPrefs().edit { putBoolean("sort_ascending", ascending) }
                    },
                    onThemeChanged = { theme ->
                        ThemeHelper.setTheme(this@SettingsActivity, theme)
                        isDarkTheme = theme == ThemeHelper.THEME_DARK ||
                                (theme == ThemeHelper.THEME_SYSTEM && isNightMode())
                    },
                    onThemeColorChanged = { color ->
                        ThemeHelper.setThemeColor(this@SettingsActivity, color)
                        themeColor = color
                    },
                    onNeedsRecreate = {
                        Toast.makeText(this, R.string.settings_applied, Toast.LENGTH_SHORT).show()
                        recreate()
                    }
                )
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

    private fun showImportConfirmDialog(uri: Uri) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(R.string.confirm_import)
            .setMessage(R.string.import_warning)
            .setPositiveButton(R.string.ok) { _, _ ->
                executor.execute {
                    val repo = viewModel.getRepository()
                    val success = dataHelper.importDataFromUri(repo, uri, true)
                    runOnUiThread {
                        if (success) {
                            Toast.makeText(this, R.string.import_success, Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, R.string.import_failed, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
}
