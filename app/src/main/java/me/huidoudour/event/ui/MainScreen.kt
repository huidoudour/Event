package me.huidoudour.event.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import me.huidoudour.event.R
import androidx.compose.ui.unit.dp
import me.huidoudour.event.data.Event
import me.huidoudour.event.util.ActionMonitor
import java.text.SimpleDateFormat
import java.util.*

/**
 * 主界面 Compose 组件：对齐原 XML 布局 activity_main.xml
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenContent(
    events: List<Event>,
    isMultiSelectMode: Boolean,
    selectedIds: Set<Long>,
    isAscending: Boolean,
    viewMode: Int,
    onSettings: () -> Unit,
    onRefresh: () -> Unit,
    onClearAll: () -> Unit,
    onToggleMultiSelect: () -> Unit,
    onSelectAll: () -> Unit,
    onDeleteSelected: () -> Unit,
    onAddEvent: () -> Unit,
    onEventClick: (Event) -> Unit,
    onEventLongClick: (Event) -> Unit,
    onToggleSelection: (Long) -> Unit
) {
    Scaffold(
        topBar = {
            // ── Toolbar ── 对齐 XML：btnSettings, btnRefresh, btnClearAll, btnMultiSelect
            TopAppBar(
                title = { Text("Event") },
                actions = {
                    // 多选按钮（最左）
                    IconButton(onClick = {
                        ActionMonitor.log("BTN_CLICK", "点击多选按钮", 0)
                        onToggleMultiSelect()
                    }) {
                        Icon(
                            painterResource(R.drawable.ic_multi_select),
                            contentDescription = "多选"
                        )
                    }
                    // 清空按钮
                    IconButton(onClick = {
                        ActionMonitor.log("BTN_CLICK", "长按清空按钮", 0)
                        onClearAll()
                    }) {
                        Icon(painterResource(R.drawable.ic_delete), contentDescription = "清空")
                    }
                    // 刷新按钮
                    IconButton(onClick = {
                        ActionMonitor.log("BTN_CLICK", "点击刷新按钮", 0)
                        onRefresh()
                    }) {
                        Icon(painterResource(R.drawable.ic_refresh), contentDescription = "刷新")
                    }
                    // 设置按钮（最右）
                    IconButton(onClick = {
                        ActionMonitor.log("BTN_CLICK", "点击设置按钮", 0)
                        onSettings()
                    }) {
                        Icon(painterResource(R.drawable.ic_settings), contentDescription = "设置")
                    }
                }
            )
        },
        floatingActionButton = {
            // ── FAB ──
            if (!isMultiSelectMode) {
                FloatingActionButton(onClick = {
                    ActionMonitor.log("UI_ACTION", "点击FAB打开添加事件对话框", 0)
                    onAddEvent()
                }) {
                    Icon(painterResource(R.drawable.ic_add), contentDescription = "添加事件")
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            // ── 主内容 ──
            if (events.isEmpty()) {
                EmptyView(modifier = Modifier.fillMaxSize())
            } else {
                if (viewMode == 0) {
                    // ── 卡片视图 ── 对齐 fragment_event_list.xml padding
                    LazyColumn(
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 88.dp),
                        verticalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        items(events, key = { it.id }) { event ->
                            EventCard(
                                event = event,
                                isMultiSelectMode = isMultiSelectMode,
                                isSelected = selectedIds.contains(event.id),
                                onClick = {
                                    if (isMultiSelectMode) onToggleSelection(event.id)
                                    else onEventClick(event)
                                },
                                onLongClick = { onEventLongClick(event) }
                            )
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                } else {
                    // ── 表格视图 ── 对齐 fragment_event_table.xml: ScrollView + HorizontalScrollView
                    val scrollState = rememberScrollState()
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .horizontalScroll(scrollState)
                    ) {
                        // 表头
                        TableHeaderRow(
                            isMultiSelectMode = isMultiSelectMode,
                            isAllSelected = selectedIds.size == events.size && events.isNotEmpty(),
                            onToggleSelectAll = {
                                onSelectAll()
                            }
                        )
                        HorizontalDivider(modifier = Modifier.fillMaxWidth())
                        // 数据行
                        LazyColumn {
                            items(events, key = { it.id }) { event ->
                                TableRow(
                                    event = event,
                                    isMultiSelectMode = isMultiSelectMode,
                                    isSelected = selectedIds.contains(event.id),
                                    onClick = {
                                        if (isMultiSelectMode) onToggleSelection(event.id)
                                        else onEventClick(event)
                                    },
                                    onLongClick = { onEventLongClick(event) }
                                )
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outlineVariant,
                                    thickness = 0.5.dp
                                )
                            }
                        }
                    }
                }
            }

            // ── 批量操作按钮组 ── 对齐 XML: 悬浮在FAB位置上方
            if (isMultiSelectMode) {
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 16.dp, bottom = 88.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 全选按钮 - 56dp 圆形
                    FloatingActionButton(
                        onClick = {
                            ActionMonitor.log("BTN_CLICK", "点击全选按钮", 0)
                            onSelectAll()
                        },
                        modifier = Modifier.size(56.dp),
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        elevation = FloatingActionButtonDefaults.elevation(0.dp)
                    ) {
                        Icon(
                            painterResource(R.drawable.ic_check),
                            contentDescription = "全选/取消"
                        )
                    }
                    // 删除所选按钮 - 56dp 圆形，错误颜色
                    FloatingActionButton(
                        onClick = {
                            ActionMonitor.log("BTN_CLICK", "点击删除选中按钮", 0)
                            onDeleteSelected()
                        },
                        modifier = Modifier.size(56.dp),
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                        elevation = FloatingActionButtonDefaults.elevation(0.dp)
                    ) {
                        Icon(painterResource(R.drawable.ic_delete), contentDescription = "删除选中")
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════
// 事件卡片 - 对齐 item_event.xml
// ═══════════════════════════════════════════════════

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun EventCard(
    event: Event,
    isMultiSelectMode: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = MaterialTheme.shapes.medium, // 12dp
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            width = 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 多选 CheckBox
            if (isMultiSelectMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onClick() },
                    modifier = Modifier
                        .size(24.dp)
                        .padding(end = 12.dp)
                )
            }
            // 事件内容
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val desc = event.description
                if (!desc.isNullOrBlank()) {
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 4.dp),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = dateFormat.format(Date(event.eventTime)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════
// 表格表头 - 对齐 item_list_view.xml + activity_list_view.xml
// ═══════════════════════════════════════════════════

@Composable
private fun TableHeaderRow(
    isMultiSelectMode: Boolean,
    isAllSelected: Boolean,
    onToggleSelectAll: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isMultiSelectMode) {
                Checkbox(
                    checked = isAllSelected,
                    onCheckedChange = { onToggleSelectAll() },
                    modifier = Modifier.width(48.dp)
                )
            }
            // ID: 80dp
            Text("ID", Modifier.width(80.dp),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            // 标题: 200dp
            Text("标题", Modifier.width(200.dp),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
            // 描述: 250dp
            Text("描述", Modifier.width(250.dp),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
            // 时间: 180dp
            Text("时间", Modifier.width(180.dp),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
        }
    }
}

// ═══════════════════════════════════════════════════
// 表格行 - 对齐 item_list_view.xml + item_table_row.xml
// ═══════════════════════════════════════════════════

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TableRow(
    event: Event,
    isMultiSelectMode: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        color = if (isSelected)
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        else
            MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isMultiSelectMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onClick() },
                    modifier = Modifier.width(48.dp)
                )
            }
            // ID: 80dp
            Text("#${event.id}", Modifier.width(80.dp),
                style = MaterialTheme.typography.bodySmall)
            // 标题: 200dp
            Text(event.title, Modifier.width(200.dp),
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2, overflow = TextOverflow.Ellipsis)
            // 描述: 250dp
            Text(
                event.description?.takeIf { it.isNotBlank() } ?: "-",
                Modifier.width(250.dp),
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2, overflow = TextOverflow.Ellipsis
            )
            // 时间: 180dp
            Text(
                dateFormat.format(Date(event.eventTime)),
                Modifier.width(180.dp),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

// ═══════════════════════════════════════════════════
// 空视图
// ═══════════════════════════════════════════════════

@Composable
fun EmptyView(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Text(
            "暂无事件",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
