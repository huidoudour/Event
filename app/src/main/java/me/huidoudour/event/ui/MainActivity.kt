package me.huidoudour.event.ui

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.core.content.FileProvider
import androidx.core.content.edit
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.LocalLifecycleOwner
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.schedulers.Schedulers
import me.huidoudour.event.R
import me.huidoudour.event.data.DataImportExportHelper
import me.huidoudour.event.data.Event
import me.huidoudour.event.ui.components.PredictiveBackPage
import me.huidoudour.event.util.BaseActivity
import me.huidoudour.event.util.IconColorHelper
import me.huidoudour.event.util.ThemeHelper
import me.huidoudour.event.util.ViewModeHelper
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sin
import kotlin.math.sqrt

/** 主界面之上叠加的子页面：设置 → 关于 / 开源许可 */
private enum class OverlayPage {
    Settings,
    Me,
    Licenses,
}

/** 视图切换前完整展示加载动画的时长：让 ContainedLoadingIndicator 走完一轮以上的几何旋转变形。 */
private const val VIEW_SWITCH_LOADING_MILLIS = 900L

/** 切换完成后保留加载动画的时长，避免新视图出现瞬间闪断。 */
private const val VIEW_SWITCH_SETTLE_MILLIS = 320L

/** 与参考项目 Installer 相同的 500ms 程序化入场缓动。 */
private val LabEnterEasing = Easing { fraction ->
    val response = 0.8
    val damping = 0.95
    val omega = 2.0 * PI / response
    val stiffness = omega * omega
    val dampingCoefficient = damping * 4.0 * PI / response
    val decayRate = -dampingCoefficient / 2.0
    val dampedFrequency = sqrt(4.0 * stiffness - dampingCoefficient * dampingCoefficient) / 2.0
    val phase = decayRate / dampedFrequency
    val time = fraction.toDouble()
    (exp(decayRate * time) * (-cos(dampedFrequency * time) + phase * sin(dampedFrequency * time)) + 1.0).toFloat()
}

class MainActivity : BaseActivity() {

    private lateinit var viewModel: EventViewModel
    private lateinit var dataHelper: DataImportExportHelper
    // RxKotlin / RxJava 统一管理导入/导出异步任务，onDestroy 时自动释放
    private val disposables = CompositeDisposable()

    /** 导出：系统 SAF（FileManager 不可用时的回退路径） */
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

    override fun onDestroy() {
        disposables.dispose()
        super.onDestroy()
    }

    private fun isNightMode(): Boolean {
        val mode = resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK
        return mode == Configuration.UI_MODE_NIGHT_YES
    }

    override fun onResume() {
        super.onResume()
        if (isRestarting) return
        viewModel.getRepository().syncSortOrder()
    }

    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    private fun MainScreen(viewModel: EventViewModel) {
        val events by viewModel.getSortedEvents().observeAsState(emptyList())
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
        var showUnmarkConfirm by remember { mutableStateOf<Event?>(null) }
        var multiSelect by remember { mutableStateOf(false) }
        var selIds by remember { mutableStateOf(setOf<Long>()) }
        // 新增完成后把该 ID 交给列表：滚动到新条目，并仅在卡片模式播放一次进入动画。
        var newlyAddedEventId by remember { mutableStateOf<Long?>(null) }
        var newlyAddedEventCreatedAt by remember { mutableStateOf<Long?>(null) }

        // 搜索状态
        var isSearchActive by remember { mutableStateOf(false) }
        var searchQuery by remember { mutableStateOf("") }
        // 进入/退出搜索或输入关键词时，实时刷新搜索结果
        LaunchedEffect(isSearchActive, searchQuery) {
            viewModel.setSearchQuery(if (isSearchActive) searchQuery else "")
        }

        // 叠加页面栈：设置 → 关于 / 开源许可。与参考项目 Installer 一致，子页面不再启动
        // 新的 Activity，而是叠在主界面之上，这样系统返回手势可以直接驱动页面退场动画。
        var overlayStack by rememberSaveable { mutableStateOf(emptyList<OverlayPage>()) }
        // 叠加页面入栈：栈顶已是同一页面时直接忽略，避免快速连点把同一页压入多次
        val pushOverlay: (OverlayPage) -> Unit = { page ->
            if (overlayStack.lastOrNull() != page) {
                overlayStack = overlayStack + page
            }
        }
        // 设置页的排序偏好：变更后立即写回并让仓库同步，返回主界面时已是新排序
        var isAscending by remember { mutableStateOf(viewModel.getRepository().isAscending) }
        // 长按设置按钮切换视图的过渡状态：强制播放 ContainedLoadingIndicator，
        // 动画期间吞掉输入，保证每次切换都完整走一遍过渡
        var viewSwitchLoading by remember { mutableStateOf(false) }
        val viewSwitchScope = rememberCoroutineScope()

        // 多选模式下系统返回键先退出批量管理，再次按返回才退出应用；
        // 叠加页面打开时由叠加页面自己接管（后注册的返回回调优先响应）
        BackHandler(enabled = multiSelect) {
            multiSelect = false
            selIds = emptySet()
        }

        Box(modifier = Modifier.fillMaxSize()) {
            MainScreenContent(
                events = events,
                isMultiSelectMode = multiSelect,
                selectedIds = selIds,
                viewMode = viewMode,
                isSearchActive = isSearchActive,
                searchQuery = searchQuery,
                // 过渡期间隐藏卡片/表格，等加载动画走完再显示新视图
                contentVisible = !viewSwitchLoading,
                newlyAddedEventId = newlyAddedEventId,
                newlyAddedEventCreatedAt = newlyAddedEventCreatedAt,
                onNewEventDisplayed = {
                    newlyAddedEventId = null
                    newlyAddedEventCreatedAt = null
                },
                onSearchToggle = {
                    if (isSearchActive) {
                        isSearchActive = false
                        searchQuery = ""
                    } else {
                        // 进入搜索前退出多选，避免选中态与过滤结果冲突
                        multiSelect = false
                        selIds = emptySet()
                        isSearchActive = true
                    }
                },
                onSearchQueryChange = { searchQuery = it },
                onToggleMultiSelect = {
                    multiSelect = !multiSelect
                    selIds = emptySet()
                },
                onSelectAll = {
                    selIds = if (selIds.size == events.size) emptySet()
                    else events.map { it.id }.toSet()
                },
                onCompleteSelected = {
                    // 已完成的事件无需重复标记，仅处理所选中的未完成项
                    val targets = events.filter { it.id in selIds && !it.isDone }
                    when {
                        selIds.isEmpty() ->
                            Toast.makeText(context, R.string.selected_count_0, Toast.LENGTH_SHORT).show()
                        targets.isEmpty() ->
                            Toast.makeText(context, R.string.selected_all_done, Toast.LENGTH_SHORT).show()
                        else -> {
                            viewModel.completeEvents(targets)
                            Toast.makeText(
                                context,
                                getString(R.string.completed_selected_format, targets.size),
                                Toast.LENGTH_SHORT
                            ).show()
                            multiSelect = false
                            selIds = emptySet()
                        }
                    }
                },
                onDeleteSelected = {
                    if (selIds.isEmpty()) {
                        Toast.makeText(context, R.string.selected_count_0, Toast.LENGTH_SHORT).show()
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
                    viewModel.refreshEvents()
                    Toast.makeText(context, R.string.refreshed, Toast.LENGTH_SHORT).show()
                },
                onClearAll = {
                    showClear = true
                },
                onSettings = {
                    pushOverlay(OverlayPage.Settings)
                },
                // 长按设置按钮切换条目/列表视图：Release 版本同样可用。
                // 切换强制经过 ContainedLoadingIndicator 过渡，过渡期间忽略重复触发。
                onSettingsLongClick = {
                    if (viewSwitchLoading) {
                        Log.d(TAG, "onSettingsLongClick: ignored, transition in progress")
                    } else {
                        val currentMode = viewMode
                        val nextMode = if (currentMode == ViewModeHelper.VIEW_MODE_CARD) {
                            ViewModeHelper.VIEW_MODE_LIST
                        } else {
                            ViewModeHelper.VIEW_MODE_CARD
                        }
                        Log.d(TAG, "onSettingsLongClick: currentMode=$currentMode, nextMode=$nextMode")
                        viewSwitchLoading = true
                        viewSwitchScope.launch {
                            // 先完整播放一轮几何图形旋转变形，再在遮罩后完成视图切换
                            delay(VIEW_SWITCH_LOADING_MILLIS)
                            ViewModeHelper.setViewMode(context, nextMode)
                            viewMode = nextMode
                            // 写回后读取实际生效值，切换失败时给出明确提示
                            val appliedMode = ViewModeHelper.getViewMode(context)
                            val modeName = getString(
                                if (appliedMode == ViewModeHelper.VIEW_MODE_CARD) R.string.card_view
                                else R.string.list_view
                            )
                            if (appliedMode == nextMode) {
                                Log.d(TAG, "onSettingsLongClick: applied=$appliedMode")
                                Toast.makeText(
                                    context,
                                    getString(R.string.view_mode_changed_format, modeName),
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                Log.e(
                                    TAG,
                                    "onSettingsLongClick: apply failed, expected=$nextMode actual=$appliedMode"
                                )
                                Toast.makeText(
                                    context,
                                    R.string.view_mode_change_failed,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            delay(VIEW_SWITCH_SETTLE_MILLIS)
                            viewSwitchLoading = false
                        }
                    }
                }
            )

            // 叠加页面：从栈底到栈顶依次绘制，每层独立管理入场进度与压暗下层的遮罩
            overlayStack.forEach { page ->
                key(page) {
                    PredictiveBackLayer(
                        onBack = {
                            overlayStack = overlayStack.dropLast(1)
                            // 设置页可能改了数据展示模式；overlay 关闭不会走 onResume，这里主动同步
                            viewMode = ViewModeHelper.getViewMode(context)
                        }
                    ) { requestBack ->
                        when (page) {
                            OverlayPage.Settings -> SettingsScreenContent(
                                onBack = requestBack,
                                onAboutDeveloper = {
                                    pushOverlay(OverlayPage.Me)
                                },
                                onOpenLicenses = {
                                    pushOverlay(OverlayPage.Licenses)
                                },
                                onExport = { exportData() },
                                onImport = { importData() },
                                isAscending = isAscending,
                                onSortOrderChanged = { ascending ->
                                    isAscending = ascending
                                    getSortPrefs().edit { putBoolean(SORT_PREF_ASCENDING, ascending) }
                                    viewModel.getRepository().syncSortOrder()
                                },
                                onThemeChanged = { theme ->
                                    ThemeHelper.saveTheme(this@MainActivity, theme)
                                    recreate()
                                },
                                onNeedsRecreate = {
                                    Toast.makeText(context, R.string.settings_applied, Toast.LENGTH_SHORT).show()
                                    recreate()
                                }
                            )

                            OverlayPage.Me -> MeScreenContent()

                            OverlayPage.Licenses -> OpenSourceLicensesScreenContent(onBack = requestBack)
                        }
                    }
                }
            }

            // 视图切换过渡层：强制播放 ContainedLoadingIndicator 的几何旋转变形，
            // 遮罩期间吞掉触摸，避免过渡被打断；切换在遮罩后方完成，用户看到的是动画而不是瞬切。
            AnimatedVisibility(
                visible = viewSwitchLoading,
                enter = fadeIn(animationSpec = tween(durationMillis = 120)),
                exit = fadeOut(animationSpec = tween(durationMillis = 220))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.32f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {},
                    contentAlignment = Alignment.Center
                ) {
                    ContainedLoadingIndicator()
                }
            }
        }

        // ── 对话框 ──
        if (showAdd) {
            AddEventDialog(
                onDismiss = { showAdd = false },
                onConfirm = { title, desc ->
                    val createdAt = System.currentTimeMillis()
                    newlyAddedEventCreatedAt = createdAt
                    viewModel.addEvent(title, desc, createdAt, createdAt) { eventId ->
                        newlyAddedEventId = eventId
                    }
                    Toast.makeText(this, R.string.event_saved, Toast.LENGTH_SHORT).show()
                }
            )
        }

        showEdit?.let { event ->
            EditEventDialog(
                event = event,
                onDismiss = { showEdit = null },
                onConfirm = { title, desc ->
                    viewModel.updateEvent(event.copy(title = title, description = desc))
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

        showUnmarkConfirm?.let { event ->
            UnmarkDoneConfirmDialog(
                eventTitle = event.title,
                onDismiss = { showUnmarkConfirm = null },
                onConfirm = {
                    viewModel.setEventDone(event, false)
                    Toast.makeText(this, R.string.event_marked_undone, Toast.LENGTH_SHORT).show()
                }
            )
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
                onToggleDone = {
                    if (event.isDone) {
                        // 取消完成需二次确认，避免长按菜单误触直接取消标记
                        showUnmarkConfirm = event
                    } else {
                        viewModel.setEventDone(event, true)
                        Toast.makeText(this, R.string.event_marked_done, Toast.LENGTH_SHORT).show()
                    }
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
            fun AlertDialog.tintButtons() {
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
                        val updatedEvent = event.copy(eventTime = cal.timeInMillis)
                        viewModel.updateEvent(updatedEvent)
                        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                        Toast.makeText(
                            this,
                            getString(
                                R.string.event_datetime_changed_format,
                                sdf.format(Date(updatedEvent.eventTime))
                            ),
                            Toast.LENGTH_LONG
                        ).show()
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

    // =========================================================================
    //  设置页：导入 / 导出
    // =========================================================================

    private fun getSortPrefs(): SharedPreferences {
        return getSharedPreferences(SORT_PREFS_NAME, MODE_PRIVATE)
    }

    /** 导出：优先写入缓存并交给 FileManager，未安装时回退到系统 SAF */
    private fun exportData() {
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
                            this@MainActivity,
                            R.string.no_data_to_export,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )
                .addTo(disposables)
        } else {
            exportFileLauncher.launch("events_backup_${System.currentTimeMillis()}.json")
        }
    }

    /** 导入：优先拉起 FileManager，未安装时回退到系统 SAF */
    private fun importData() {
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
            Toast.makeText(
                this@MainActivity,
                getString(R.string.cannot_launch_file_picker, e.message),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

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
    private fun buildExportJson(events: List<Event>): String? {
        if (events.isEmpty()) return null
        // 每个导出任务只创建一个日期格式实例，避免在循环中反复 new
        // 精确到秒，与库内 eventTime 的秒信息保持一致
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        val sb = StringBuilder("[\n")
        events.forEachIndexed { index, event ->
            if (index > 0) sb.append(",\n")
            sb.append("  {\n")
            sb.append("    \"事件标题\": \"${event.title.replace("\\", "\\\\").replace("\"", "\\\"")}\",\n")
            sb.append("    \"事件详情\": \"${(event.description ?: "").replace("\\", "\\\\").replace("\"", "\\\"")}\",\n")
            sb.append("    \"事件时间\": \"${dateFormat.format(Date(event.eventTime))}\",\n")
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
            Toast.makeText(
                this@MainActivity,
                getString(R.string.cannot_launch_file_manager, e.message),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

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
        private const val TAG = "MainActivity"
        /** FileManager 文件管理器包名 */
        private const val FILE_MANAGER_PACKAGE = "me.huidoudour.file.manager"
        /** 排序偏好（与 EventRepository 共用同一份 SharedPreferences） */
        private const val SORT_PREFS_NAME = "sort_prefs"
        private const val SORT_PREF_ASCENDING = "sort_ascending"
    }
}

/**
 * 单个叠加页面的容器：负责入场滑入、压暗下层的遮罩，以及把预测性返回交给 [PredictiveBackPage]。
 *
 * 每层维护自己的入场进度与遮罩强度，因此「设置 → 关于」叠两层时，关于页滑入会压暗设置页，
 * 关于页返回后设置页原样露出，与参考项目 Installer 的叠加页面表现一致。
 */
@Composable
private fun PredictiveBackLayer(
    onBack: () -> Unit,
    content: @Composable (requestBack: () -> Unit) -> Unit,
) {
    var scrim by remember { mutableFloatStateOf(1f) }
    val enterProgress = remember { Animatable(0f) }

    // 入场：遮罩先置满，页面从手势边缘滑入；缓动与参考项目保持一致。
    LaunchedEffect(Unit) {
        scrim = 1f
        enterProgress.snapTo(0f)
        enterProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 500, easing = LabEnterEasing)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            // 叠加页面在屏幕上时，所有触摸都归它：容器是整屏的、不随入场位移，
            // 从第一帧起就命中本层，下层主界面（工具栏设置按钮等）不再接收事件。
            // 否则入场滑入的那 500ms 里连点会穿透到下层，把同一页面压入多次。
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        awaitPointerEvent(PointerEventPass.Main).changes.forEach { it.consume() }
                    }
                }
            }
    ) {
        // 遮罩：进场时随 enterProgress 压暗下层；返回时由 PredictiveBackPage 上报——
        // 手势期间恒为 1，确认返回后才淡到 0。
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    alpha = 0.5f * enterProgress.value * scrim.coerceIn(0f, 1f)
                }
                .background(Color.Black)
        )
        PredictiveBackPage(
            enterProgress = enterProgress.value,
            onScrimChange = { scrim = it },
            onBack = onBack,
            content = content
        )
    }
}
