package me.huidoudour.event.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import me.huidoudour.event.R
import me.huidoudour.event.data.Event
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    val context = LocalContext.current
    Scaffold(
        topBar = {
            // ── Toolbar ── 对齐 XML：btnMultiSelect, btnClearAll, btnRefresh, btnSettings
            // actions 从左到右排列，顺序：多选 → 清空 → 刷新 → 设置
            TopAppBar(
                title = { Text("Event") },
                actions = {
                    // 多选按钮（最左）— 对齐XML：btnMultiSelect（最后一个ImageButton，最左）
                    IconButton(onClick = {
                        onToggleMultiSelect()
                    }) {
                        Icon(
                            painterResource(R.drawable.ic_multi_select),
                            contentDescription = context.getString(R.string.multi_select)
                        )
                    }
                    // 清空按钮
                    IconButton(onClick = {
                        onClearAll()
                    }) {
                        Icon(painterResource(R.drawable.ic_delete), contentDescription = context.getString(R.string.clear_all))
                    }
                    // 刷新按钮
                    IconButton(onClick = {
                        onRefresh()
                    }) {
                        Icon(painterResource(R.drawable.ic_refresh), contentDescription = context.getString(R.string.refresh))
                    }
                    // 设置按钮（最右）— 对齐XML：btnSettings（第一个ImageButton，最右）
                    IconButton(onClick = {
                        onSettings()
                    }) {
                        Icon(painterResource(R.drawable.ic_settings), contentDescription = context.getString(R.string.settings))
                    }
                }
            )
        },
        floatingActionButton = {
            // ── FAB ──
            if (!isMultiSelectMode) {
                FloatingActionButton(
                    onClick = {
                        onAddEvent()
                    },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Icon(painterResource(R.drawable.ic_add), contentDescription = context.getString(R.string.add_event))
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
                    // ── 表格视图 ── 对齐 fragment_event_table.xml (TableLayout + table_border)
                    val scrollState = rememberScrollState()
                    // 外层 Card：对齐 table_border.xml — 圆角8dp + 1dp colorOutline 边框 + surface 背景
                    Card(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        shape = RoundedCornerShape(8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
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
                            // 表头与数据之间的分隔线 — 对齐 XML: 1dp colorPrimary
                            HorizontalDivider(
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.primary,
                                thickness = 1.dp
                            )
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
                                    // 行间分隔线 — 对齐 XML: showDividers="middle", dividerPadding="4dp"
                                    HorizontalDivider(
                                        modifier = Modifier.padding(horizontal = 4.dp),
                                        color = MaterialTheme.colorScheme.outlineVariant,
                                        thickness = 1.dp
                                    )
                                }
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
                            onSelectAll()
                        },
                        modifier = Modifier.size(56.dp),
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        elevation = FloatingActionButtonDefaults.elevation(0.dp)
                    ) {
                        Icon(
                            painterResource(R.drawable.ic_check),
                            contentDescription = context.getString(R.string.select_all)
                        )
                    }
                    // 删除所选按钮 - 56dp 圆形，错误颜色
                    FloatingActionButton(
                        onClick = {
                            onDeleteSelected()
                        },
                        modifier = Modifier.size(56.dp),
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                        elevation = FloatingActionButtonDefaults.elevation(0.dp)
                    ) {
                        Icon(painterResource(R.drawable.ic_delete), contentDescription = context.getString(R.string.delete_selected))
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
// 表格表头 - 对齐 activity_list_view.xml 表头
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
                    modifier = Modifier.padding(0.dp)
                )
            }
            // ID: 80dp，居中 — 对齐 XML: width="80dp", gravity="center"
            Text(
                text = "ID",
                modifier = Modifier.width(80.dp).padding(8.dp),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            // 标题: 200dp — 对齐 XML: width="200dp", gravity="start|center_vertical"
            Text(
                text = stringResource(R.string.event_title),
                modifier = Modifier.width(200.dp).padding(8.dp),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            // 描述: 250dp — 对齐 XML: width="250dp", gravity="start|center_vertical"
            Text(
                text = stringResource(R.string.event_description),
                modifier = Modifier.width(250.dp).padding(8.dp),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            // 时间: 180dp，居中 — 对齐 XML: width="180dp", gravity="center"
            Text(
                text = stringResource(R.string.event_time),
                modifier = Modifier.width(180.dp).padding(8.dp),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

// ═══════════════════════════════════════════════════
// 表格行 - 对齐 item_list_view.xml
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
    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                interactionSource = interactionSource,
                indication = androidx.compose.material3.ripple(),
                onClick = onClick,
                onLongClick = onLongClick
            ),
        color = when {
            isSelected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            isPressed -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
            else -> MaterialTheme.colorScheme.surface
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isMultiSelectMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onClick() },
                    modifier = Modifier.padding(0.dp)
                )
            }
            // ID: 80dp，居中 — 对齐 XML: width="80dp", gravity="center"
            Text(
                text = "#${event.id}",
                modifier = Modifier.width(80.dp).padding(8.dp),
                style = MaterialTheme.typography.bodySmall,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            // 标题: 200dp — 对齐 XML: width="200dp", maxLines=2, ellipsize=end
            Text(
                text = event.title,
                modifier = Modifier.width(200.dp).padding(8.dp),
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            // 描述: 250dp — 对齐 XML: width="250dp", maxLines=2, ellipsize=end
            Text(
                text = event.description?.takeIf { it.isNotBlank() } ?: "-",
                modifier = Modifier.width(250.dp).padding(8.dp),
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            // 时间: 180dp，居中 — 对齐 XML: width="180dp", gravity="center"
            Text(
                text = dateFormat.format(Date(event.eventTime)),
                modifier = Modifier.width(180.dp).padding(8.dp),
                style = MaterialTheme.typography.bodySmall,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
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
