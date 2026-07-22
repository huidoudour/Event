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
                        startActivity(Intent(this, MeActivity::class.java))
                    },
                    onExport = {
                        exportFileLauncher.launch("events_backup_${System.currentTimeMillis()}.json")
                    },
                    onImport = {
                        importFileLauncher.launch("application/json")
                    },
                    isAscending = getSortPrefs().getBoolean("sort_ascending", false),
                    onSortOrderChanged = { ascending ->
                        getSortPrefs().edit().putBoolean("sort_ascending", ascending).apply()
                    },
                    onSettingApplied = {
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
        return getSharedPreferences("sort_prefs", Context.MODE_PRIVATE)
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
