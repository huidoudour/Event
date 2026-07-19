package me.huidoudour.event.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModelProvider
import me.huidoudour.event.MeActivity
import me.huidoudour.event.R
import me.huidoudour.event.data.DataImportExportHelper
import me.huidoudour.event.util.ActionMonitor
import me.huidoudour.event.util.LocaleHelper
import me.huidoudour.event.util.ThemeHelper
import me.huidoudour.event.ui.theme.EventTheme
import java.util.concurrent.Executors

class SettingsActivity : ComponentActivity() {

    private lateinit var viewModel: EventViewModel
    private lateinit var dataHelper: DataImportExportHelper
    private val executor = Executors.newSingleThreadExecutor()

    private val exportFileLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            executor.execute {
                val repo = viewModel.getRepository()
                val success = dataHelper.exportDataToUri(repo, uri)
                runOnUiThread {
                    if (success) {
                        ActionMonitor.log("EXPORT", "导出数据成功", 0)
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

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeHelper.initTheme(this)
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(
            this,
            EventViewModel.Factory(application)
        )[EventViewModel::class.java]
        dataHelper = DataImportExportHelper(this)

        setContent {
            EventTheme(
                themeColor = ThemeHelper.getThemeColor(this),
                darkTheme = ThemeHelper.getTheme(this) == ThemeHelper.THEME_DARK ||
                        (ThemeHelper.getTheme(this) == ThemeHelper.THEME_SYSTEM &&
                         isNightMode())
            ) {
                SettingsScreenContent(
                    onBack = { finish() },
                    onAboutDeveloper = {
                        ActionMonitor.log("CARD_CLICK", "点击关于开发者卡片", 0)
                        startActivity(Intent(this, MeActivity::class.java))
                    },
                    onExport = {
                        ActionMonitor.log("CARD_CLICK", "点击导出数据卡片", 0)
                        exportFileLauncher.launch("events_backup_${System.currentTimeMillis()}.json")
                    },
                    onImport = {
                        ActionMonitor.log("CARD_CLICK", "点击导入数据卡片", 0)
                        importFileLauncher.launch("application/json")
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
                            ActionMonitor.log("IMPORT", "导入数据成功", 0)
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
