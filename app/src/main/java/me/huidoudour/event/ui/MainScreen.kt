package me.huidoudour.event.ui

import android.widget.Toast
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.huidoudour.event.R
import me.huidoudour.event.data.Event
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 主界面 Compose 组件：对齐原 XML 布局 activity_main.xml
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
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
                    // 清空按钮（长按触发）— 对齐XML：btnClearAll（Clear All Button Long Press）
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .combinedClickable(
                                onClick = {
                                    Toast.makeText(context, R.string.long_press_to_clear, Toast.LENGTH_SHORT).show()
                                },
                                onLongClick = onClearAll
                            ),
                        contentAlignment = Alignment.Center
                    ) {
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
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Icon(painterResource(R.drawable.ic_add), contentDescription = context.getString(R.string.add_event), tint = Color.Unspecified)
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
                    // ── 表格视图 ── 横向可滑动，按数据内容统一列宽
                    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }
                    val colWidths = remember(events) { calculateColumnWidths(events, dateFormat) }
                    val horizontalScrollState = rememberScrollState()
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                            .horizontalScroll(horizontalScrollState)
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxHeight()
                                .wrapContentWidth(),
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(12.dp))
                            ) {
                                // 表头 — primary 背景，跟随主题色
                                TableHeaderRow(
                                    isMultiSelectMode = isMultiSelectMode,
                                    isAllSelected = selectedIds.size == events.size && events.isNotEmpty(),
                                    onToggleSelectAll = { onSelectAll() },
                                    colWidths = colWidths
                                )
                                // 数据行 — 交替背景
                                LazyColumn(modifier = Modifier.fillMaxHeight()) {
                                    items(events, key = { it.id }) { event ->
                                        val index = events.indexOf(event)
                                        TableRow(
                                            event = event,
                                            isMultiSelectMode = isMultiSelectMode,
                                            isSelected = selectedIds.contains(event.id),
                                            isEvenRow = index % 2 == 0,
                                            onClick = {
                                                if (isMultiSelectMode) onToggleSelection(event.id)
                                                else onEventClick(event)
                                            },
                                            onLongClick = { onEventLongClick(event) },
                                            colWidths = colWidths,
                                            dateFormat = dateFormat
                                        )
                                    }
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
// 表格表头 — 使用 primary 背景 + 统一列宽
// ═══════════════════════════════════════════════════

@Composable
private fun TableHeaderRow(
    isMultiSelectMode: Boolean,
    isAllSelected: Boolean,
    onToggleSelectAll: () -> Unit,
    colWidths: TableColumnWidths
) {
    val headerBg = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
    val headerFg = MaterialTheme.colorScheme.onPrimaryContainer
    Surface(
        color = headerBg,
        modifier = Modifier.wrapContentWidth()
    ) {
        Row(
            modifier = Modifier.padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isMultiSelectMode) {
                Checkbox(
                    checked = isAllSelected,
                    onCheckedChange = { onToggleSelectAll() },
                    modifier = Modifier.size(18.dp).padding(start = 12.dp, end = 8.dp),
                    colors = CheckboxDefaults.colors(
                        checkmarkColor = headerFg,
                        uncheckedColor = headerFg.copy(alpha = 0.7f)
                    )
                )
            }
            // ID
            Text(
                text = stringResource(R.string.table_id),
                modifier = Modifier
                    .width(colWidths.id)
                    .padding(start = 12.dp, end = 12.dp),
                textAlign = TextAlign.Center,
                color = headerFg,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
            // 标题
            Text(
                text = stringResource(R.string.event_title),
                modifier = Modifier
                    .width(colWidths.title)
                    .padding(start = 12.dp, end = 12.dp),
                textAlign = TextAlign.Center,
                color = headerFg,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
            // 描述
            Text(
                text = stringResource(R.string.event_description),
                modifier = Modifier
                    .width(colWidths.desc)
                    .padding(start = 12.dp, end = 12.dp),
                textAlign = TextAlign.Center,
                color = headerFg,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
            // 时间
            Text(
                text = stringResource(R.string.event_time),
                modifier = Modifier
                    .width(colWidths.time)
                    .padding(start = 12.dp, end = 12.dp),
                textAlign = TextAlign.Center,
                color = headerFg,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
        }
    }
}

// ═══════════════════════════════════════════════════
// 表格行 — 统一列宽，分号内容换行
// ═══════════════════════════════════════════════════

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TableRow(
    event: Event,
    isMultiSelectMode: Boolean,
    isSelected: Boolean,
    isEvenRow: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    colWidths: TableColumnWidths,
    dateFormat: SimpleDateFormat
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val rowBackground = when {
        isSelected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        isPressed -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
        isEvenRow -> MaterialTheme.colorScheme.surface
        else -> MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f)
    }

    Surface(
        modifier = Modifier
            .wrapContentWidth()
            .combinedClickable(
                interactionSource = interactionSource,
                indication = ripple(),
                onClick = onClick,
                onLongClick = onLongClick
            ),
        color = rowBackground
    ) {
        Row(
            modifier = Modifier
                .wrapContentWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isMultiSelectMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onClick() },
                    modifier = Modifier.size(18.dp).padding(start = 8.dp, end = 8.dp)
                )
            }
            // ID — 居中对齐
            Text(
                text = "${event.id}",
                modifier = Modifier
                    .width(colWidths.id)
                    .padding(start = 12.dp, end = 12.dp),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
            // 标题 — 含"；"则允许换行
            EventTableCell(
                text = event.title,
                modifier = Modifier
                    .width(colWidths.title)
                    .padding(start = 12.dp, end = 12.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            // 描述 — 含"；"则允许换行
            EventTableCell(
                text = event.description?.takeIf { it.isNotBlank() } ?: "-",
                modifier = Modifier
                    .width(colWidths.desc)
                    .padding(start = 12.dp, end = 12.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            // 时间 — 居中对齐
            Text(
                text = dateFormat.format(Date(event.eventTime)),
                modifier = Modifier
                    .width(colWidths.time)
                    .padding(start = 12.dp, end = 12.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

/** 表格数据单元格：含中文分号"；"则允许多行换行，否则限制 2 行省略 */
@Composable
private fun EventTableCell(
    text: String,
    modifier: Modifier,
    style: TextStyle,
    color: Color,
) {
    val hasSemicolon = text.contains("；")
    Text(
        text = text,
        modifier = modifier,
        style = style,
        color = color,
        maxLines = if (hasSemicolon) Int.MAX_VALUE else 2,
        overflow = if (hasSemicolon) TextOverflow.Clip else TextOverflow.Ellipsis,
    )
}

/** 表格统一列宽 */
data class TableColumnWidths(
    val id: Dp,
    val title: Dp,
    val desc: Dp,
    val time: Dp,
)

/** 根据数据内容计算各列宽度（每字符 ≈8dp，取 min 下限 + 数据最长值） */
private fun calculateColumnWidths(events: List<Event>, dateFormat: SimpleDateFormat): TableColumnWidths {
    fun maxLen(selector: (Event) -> String): Int =
        if (events.isEmpty()) 1 else events.maxOf { selector(it).length }
    fun toDp(n: Int): Dp = (n * 8).coerceAtLeast(0).dp

    return TableColumnWidths(
        id    = maxOf(60.dp,  toDp(maxLen { "${it.id}" })),
        title = maxOf(140.dp, toDp(maxLen { it.title })),
        desc  = maxOf(120.dp, toDp(maxLen { it.description?.takeIf { d -> d.isNotBlank() } ?: "-" })),
        time  = maxOf(150.dp, toDp(maxLen { dateFormat.format(Date(it.eventTime)) })),
    )
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
