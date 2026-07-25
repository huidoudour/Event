package me.huidoudour.event

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.LocalLifecycleOwner
import me.huidoudour.event.data.Event
import me.huidoudour.event.ui.AddEventDialog
import me.huidoudour.event.ui.BatchDeleteConfirmDialog
import me.huidoudour.event.ui.ClearAllConfirmDialog
import me.huidoudour.event.ui.DeleteConfirmDialog
import me.huidoudour.event.ui.EditEventDialog
import me.huidoudour.event.ui.EventDetailDialog
import me.huidoudour.event.ui.EventLongClickMenu
import me.huidoudour.event.ui.EventViewModel
import me.huidoudour.event.ui.MainScreenContent
import me.huidoudour.event.ui.SettingsActivity
import me.huidoudour.event.ui.theme.EventTheme
import me.huidoudour.event.util.IconColorHelper
import me.huidoudour.event.util.LocaleHelper
import me.huidoudour.event.util.ThemeHelper
import me.huidoudour.event.util.ViewModeHelper
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: EventViewModel
    private var lastThemeMode = 0
    private var lastThemeColor = 0

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.applyLanguage(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // 应用主题和语言
        ThemeHelper.initTheme(this)
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(
            this,
            EventViewModel.Factory(application)
        )[EventViewModel::class.java]

        // 记录当前主题状态，用于 onResume 检测变化
        lastThemeMode = ThemeHelper.getTheme(this)
        lastThemeColor = ThemeHelper.getThemeColor(this)

        setContent {
            EventTheme(
                themeColor = ThemeHelper.getThemeColor(this),
                darkTheme = ThemeHelper.getTheme(this) == ThemeHelper.THEME_DARK ||
                        (ThemeHelper.getTheme(this) == ThemeHelper.THEME_SYSTEM &&
                         isNightMode())
            ) {
                MainScreen(viewModel = viewModel)
            }
        }

        // 图标状态同步 — 延迟到首帧后执行，不阻塞启动
        window.decorView.post {
            IconColorHelper.ensureIconColor(this)
        }
    }

    private fun isNightMode(): Boolean {
        val mode = resources.configuration.uiMode and
                android.content.res.Configuration.UI_MODE_NIGHT_MASK
        return mode == android.content.res.Configuration.UI_MODE_NIGHT_YES
    }

    override fun onResume() {
        super.onResume()
        // 检测主题变化，若从设置页回来则重建界面以应用新主题
        val currentMode = ThemeHelper.getTheme(this)
        val currentColor = ThemeHelper.getThemeColor(this)
        if (currentMode != lastThemeMode || currentColor != lastThemeColor) {
            recreate()
            return
        }
        viewModel.getRepository().syncSortOrder()
    }

    @Composable
    private fun MainScreen(viewModel: EventViewModel) {
        val events = remember { mutableStateOf<List<Event>>(emptyList()) }
        LaunchedEffect(viewModel) {
            viewModel.getSortedEvents().observe(this@MainActivity) { list ->
                events.value = list
            }
        }
        val context = this@MainActivity
        var viewMode by remember { mutableIntStateOf(ViewModeHelper.getViewMode(context)) }
        val lifecycleOwner = LocalLifecycleOwner.current
        DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    viewMode = ViewModeHelper.getViewMode(context)
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
        }

        // 对话框状态
        var showAdd by remember { mutableStateOf(false) }
        var showEdit by remember { mutableStateOf<Event?>(null) }
        var showDelete by remember { mutableStateOf<Event?>(null) }
        var showDetail by remember { mutableStateOf<Event?>(null) }
        var showMenu by remember { mutableStateOf<Event?>(null) }
        var showClear by remember { mutableStateOf(false) }
        var showBatchDelete by remember { mutableStateOf(false) }
        var showDateTime by remember { mutableStateOf<Event?>(null) }
        var multiSelect by remember { mutableStateOf(false) }
        var selIds by remember { mutableStateOf(setOf<Long>()) }

        MainScreenContent(
            events = events.value,
            isMultiSelectMode = multiSelect,
            selectedIds = selIds,
            isAscending = viewModel.isAscending(),
            viewMode = viewMode,
            onToggleMultiSelect = {
                multiSelect = !multiSelect
                selIds = emptySet()
            },
            onSelectAll = {
                selIds = if (selIds.size == events.value.size) emptySet()
                else events.value.map { it.id }.toSet()
            },
            onDeleteSelected = {
                if (selIds.isEmpty()) {
                    Toast.makeText(this, R.string.selected_count_0, Toast.LENGTH_SHORT).show()
                } else {
                    showBatchDelete = true
                }
            },
            onAddEvent = {
                showAdd = true
            },
            onEventClick = { event ->
                if (multiSelect) {
                    selIds = if (event.id in selIds) selIds - event.id else selIds + event.id
                } else {
                    showDetail = event
                }
            },
            onEventLongClick = { event ->
                showMenu = event
            },
            onToggleSelection = { id ->
                selIds = if (id in selIds) selIds - id else selIds + id
            },
            onRefresh = {
                Toast.makeText(this, R.string.refreshed, Toast.LENGTH_SHORT).show()
            },
            onClearAll = {
                showClear = true
            },
            onSettings = {
                startActivity(Intent(this, SettingsActivity::class.java))
            }
        )

        // ── 对话框 ──
        if (showAdd) {
            AddEventDialog(
                onDismiss = { showAdd = false },
                onConfirm = { title, desc ->
                    viewModel.addEvent(title, desc, System.currentTimeMillis())
                    Toast.makeText(this, R.string.event_saved, Toast.LENGTH_SHORT).show()
                }
            )
        }

        showEdit?.let { event ->
            EditEventDialog(
                event = event,
                onDismiss = { showEdit = null },
                onConfirm = { title, desc ->
                    event.title = title
                    event.description = desc
                    viewModel.updateEvent(event)
                    Toast.makeText(this, R.string.event_saved, Toast.LENGTH_SHORT).show()
                }
            )
        }

        showDelete?.let { event ->
            DeleteConfirmDialog(
                eventTitle = event.title,
                onDismiss = { showDelete = null },
                onConfirm = {
                    viewModel.deleteEvent(event)
                    Toast.makeText(this, R.string.event_deleted, Toast.LENGTH_SHORT).show()
                }
            )
        }

        showDetail?.let { event ->
            EventDetailDialog(event = event, onDismiss = { showDetail = null })
        }

        showMenu?.let { event ->
            EventLongClickMenu(
                event = event,
                onDismiss = { showMenu = null },
                onChangeTime = {
                    showDateTime = event
                },
                onEdit = {
                    showEdit = event
                },
                onDelete = {
                    showDelete = event
                }
            )
        }

        showDateTime?.let { event ->
            // 系统日期/时间选择器按钮颜色取自 ROM 的 colorAccent，不跟随 Compose 主题，
            // 这里弹出后强制设为当前主题的 primary 色
            val pickerAccent = MaterialTheme.colorScheme.primary.toArgb()
            fun android.app.AlertDialog.tintButtons() {
                getButton(DialogInterface.BUTTON_POSITIVE)?.setTextColor(pickerAccent)
                getButton(DialogInterface.BUTTON_NEGATIVE)?.setTextColor(pickerAccent)
            }
            val cal = Calendar.getInstance()
            cal.timeInMillis = event.eventTime
            val datePicker = DatePickerDialog(
                this,
                { _, y, m, d ->
                    cal.set(Calendar.YEAR, y)
                    cal.set(Calendar.MONTH, m)
                    cal.set(Calendar.DAY_OF_MONTH, d)
                    val timePicker = TimePickerDialog(this, { _, h, mi ->
                        cal.set(Calendar.HOUR_OF_DAY, h)
                        cal.set(Calendar.MINUTE, mi)
                        cal.set(Calendar.SECOND, 0)
                        event.eventTime = cal.timeInMillis
                        viewModel.updateEvent(event)
                        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                        Toast.makeText(this,
                            "${getString(R.string.event_datetime_changed)}: ${
                                sdf.format(Date(event.eventTime))}",
                            Toast.LENGTH_LONG).show()
                    }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true)
                    timePicker.show()
                    timePicker.tintButtons()
                },
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)
            )
            datePicker.setOnDismissListener { showDateTime = null }
            datePicker.show()
            datePicker.tintButtons()
        }

        if (showClear) {
            ClearAllConfirmDialog(
                onDismiss = { showClear = false },
                onConfirm = {
                    viewModel.deleteAllEvents()
                    Toast.makeText(this, R.string.all_events_cleared, Toast.LENGTH_SHORT).show()
                }
            )
        }

        if (showBatchDelete) {
            BatchDeleteConfirmDialog(
                count = selIds.size,
                onDismiss = { showBatchDelete = false },
                onConfirm = {
                    viewModel.deleteEventsByIds(selIds.toList())
                    Toast.makeText(this, R.string.deleted_selected, Toast.LENGTH_SHORT).show()
                    multiSelect = false
                    selIds = emptySet()
                }
            )
        }
    }
}
