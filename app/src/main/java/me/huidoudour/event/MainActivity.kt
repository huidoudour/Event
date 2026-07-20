package me.huidoudour.event

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
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
import me.huidoudour.event.util.ActionMonitor
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
    private var isMultiSelectMode = false
    private val selectedIds = mutableSetOf<Long>()

    // 对话框状态
    private var showAddDialog = false
    private var showEditDialog = false
    private var showDeleteDialog = false
    private var showDetailDialog = false
    private var showMenuDialog = false
    private var showClearDialog = false
    private var showBatchDeleteDialog = false
    private var targetEvent: Event? = null

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.applyLanguage(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // 应用主题和语言
        ThemeHelper.initTheme(this)
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        IconColorHelper.applyIconColor(this, IconColorHelper.getIconColor(this))

        viewModel = ViewModelProvider(
            this,
            EventViewModel.Factory(application)
        )[EventViewModel::class.java]

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
    }

    private fun isNightMode(): Boolean {
        val mode = resources.configuration.uiMode and
                android.content.res.Configuration.UI_MODE_NIGHT_MASK
        return mode == android.content.res.Configuration.UI_MODE_NIGHT_YES
    }

    override fun onResume() {
        super.onResume()
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
                if (!multiSelect) selIds = emptySet()
                if (multiSelect) {
                    ActionMonitor.log("MULTI_SELECT", "进入多选模式", 0)
                } else {
                    ActionMonitor.log("MULTI_SELECT", "退出多选模式", 0)
                }
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
                ActionMonitor.log("UI_ACTION", "点击FAB打开添加事件对话框", 0)
                showAdd = true
            },
            onEventClick = { event ->
                if (multiSelect) {
                    selIds = if (event.id in selIds) selIds - event.id else selIds + event.id
                } else {
                    ActionMonitor.log("ITEM_CLICK", "查看事件详情: ${event.title}", event.id)
                    showDetail = event
                }
            },
            onEventLongClick = { event ->
                ActionMonitor.log("ITEM_LONG_CLICK", "长按事件条目: ${event.title}", event.id)
                showMenu = event
            },
            onToggleSelection = { id ->
                selIds = if (id in selIds) selIds - id else selIds + id
            },
            onRefresh = {
                Toast.makeText(this, R.string.refreshed, Toast.LENGTH_SHORT).show()
            },
            onClearAll = {
                ActionMonitor.log("BTN_CLICK", "长按清空按钮", 0)
                showClear = true
            },
            onSettings = {
                ActionMonitor.log("BTN_CLICK", "点击设置按钮", 0)
                startActivity(Intent(this, SettingsActivity::class.java))
            }
        )

        // ── 对话框 ──
        if (showAdd) {
            AddEventDialog(
                onDismiss = { showAdd = false },
                onConfirm = { title, desc ->
                    ActionMonitor.log("DIALOG_CONFIRM", "确认添加事件: $title", 0)
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
                    ActionMonitor.log("DIALOG_CONFIRM", "确认编辑事件: $title", event.id)
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
                    ActionMonitor.log("DIALOG_CONFIRM", "确认删除事件", event.id)
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
            val cal = Calendar.getInstance()
            cal.timeInMillis = event.eventTime
            val datePicker = DatePickerDialog(
                this,
                { _, y, m, d ->
                    cal.set(Calendar.YEAR, y)
                    cal.set(Calendar.MONTH, m)
                    cal.set(Calendar.DAY_OF_MONTH, d)
                    TimePickerDialog(this, { _, h, mi ->
                        cal.set(Calendar.HOUR_OF_DAY, h)
                        cal.set(Calendar.MINUTE, mi)
                        cal.set(Calendar.SECOND, 0)
                        event.eventTime = cal.timeInMillis
                        viewModel.updateEvent(event)
                        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                        ActionMonitor.log("DIALOG_CONFIRM",
                            "确认修改时间: ${sdf.format(Date(event.eventTime))}", event.id)
                        Toast.makeText(this,
                            "${getString(R.string.event_datetime_changed)}: ${
                                sdf.format(Date(event.eventTime))}",
                            Toast.LENGTH_LONG).show()
                    }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
                },
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)
            )
            datePicker.setOnDismissListener { showDateTime = null }
            datePicker.show()
        }

        if (showClear) {
            ClearAllConfirmDialog(
                onDismiss = { showClear = false },
                onConfirm = {
                    ActionMonitor.log("DIALOG_CONFIRM", "确认清空所有事件", 0)
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
                    ActionMonitor.log("DIALOG_CONFIRM", "确认批量删除 ${selIds.size} 条", 0)
                    viewModel.deleteEventsByIds(selIds.toList())
                    Toast.makeText(this, R.string.deleted_selected, Toast.LENGTH_SHORT).show()
                    multiSelect = false
                    selIds = emptySet()
                }
            )
        }
    }
}
