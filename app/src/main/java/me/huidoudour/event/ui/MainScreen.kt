package me.huidoudour.event.ui

import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SelectAll
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
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.huidoudour.event.R
import me.huidoudour.event.data.Event
import me.huidoudour.event.ui.theme.BlogCardBlue
import me.huidoudour.event.ui.theme.blogBackground
import me.huidoudour.event.ui.theme.cardBorderColor
import me.huidoudour.event.ui.theme.cardSubTextColor
import me.huidoudour.event.ui.theme.deleteFabContainerColor
import me.huidoudour.event.ui.theme.deleteFabContentColor
import me.huidoudour.event.ui.theme.fabContainerColor
import me.huidoudour.event.ui.theme.fabContentColor
import me.huidoudour.event.ui.theme.isDarkColorScheme
import me.huidoudour.event.ui.theme.selectAllFabContainerColor
import me.huidoudour.event.ui.theme.selectAllFabContentColor
import me.huidoudour.event.ui.theme.softIconButtonBg
import me.huidoudour.event.ui.theme.topAppBarColors
import me.huidoudour.event.util.ViewModeHelper
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
    viewMode: Int,
    isSearchActive: Boolean,
    searchQuery: String,
    onSearchToggle: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
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
    // 整个主界面复用一个日期格式实例，避免每个卡片/行都创建 SimpleDateFormat
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }
    Scaffold(
        // 博客风格淡蓝→淡粉渐变背景（深色模式回退默认背景色）
        modifier = Modifier
            .fillMaxSize()
            .blogBackground(),
        containerColor = Color.Transparent,
        topBar = {
            // ── Toolbar ── 对齐 XML：btnMultiSelect, btnClearAll, btnRefresh, btnSettings
            // actions 从左到右排列，顺序：多选 → 清空 → 刷新 → 设置
            TopAppBar(
                title = {
                    if (isSearchActive) {
                        SearchField(
                            query = searchQuery,
                            onQueryChange = onSearchQueryChange
                        )
                    } else {
                        Text(stringResource(R.string.dis_name))
                    }
                },
                colors = topAppBarColors(),
                actions = {
                    // ── Toolbar actions ── 搜索模式下仅显示关闭；普通模式：搜索 → 多选 → 清空 → 刷新 → 设置
                    if (isSearchActive) {
                        // 关闭搜索按钮
                        IconButton(
                            onClick = onSearchToggle,
                            modifier = Modifier.padding(horizontal = 2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(softIconButtonBg(), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painterResource(R.drawable.ic_close),
                                    contentDescription = stringResource(R.string.close_search)
                                )
                            }
                        }
                    } else {
                    // 搜索按钮（最左）— 进入搜索模式
                    IconButton(
                        onClick = onSearchToggle,
                        modifier = Modifier.padding(horizontal = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(softIconButtonBg(), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painterResource(R.drawable.ic_search),
                                contentDescription = stringResource(R.string.search)
                            )
                        }
                    }
                    // 多选按钮（最左）— 对齐XML：btnMultiSelect（最后一个ImageButton，最左）
                    // 底色圆 40dp，触摸区域保持 48dp
                    IconButton(
                        onClick = {
                            onToggleMultiSelect()
                        },
                        modifier = Modifier.padding(horizontal = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(softIconButtonBg(), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painterResource(R.drawable.ic_multi_select),
                                contentDescription = stringResource(R.string.multi_select)
                            )
                        }
                    }
                    // 清空按钮（长按触发）— 对齐XML：btnClearAll（Clear All Button Long Press）
                    // 底色圆 40dp，触摸区域保持 48dp
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 2.dp)
                            .size(48.dp)
                            .clip(CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(softIconButtonBg(), CircleShape)
                                .combinedClickable(
                                    onClick = {
                                        Toast.makeText(context, R.string.long_press_to_clear, Toast.LENGTH_SHORT).show()
                                    },
                                    onLongClick = onClearAll
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(painterResource(R.drawable.ic_delete), contentDescription = stringResource(R.string.clear_all))
                        }
                    }
                    // 刷新按钮 — 底色圆 40dp，触摸区域保持 48dp
                    IconButton(
                        onClick = {
                            onRefresh()
                        },
                        modifier = Modifier.padding(horizontal = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(softIconButtonBg(), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(painterResource(R.drawable.ic_refresh), contentDescription = stringResource(R.string.refresh))
                        }
                    }
                    // 设置按钮（最右）— 对齐XML：btnSettings（第一个ImageButton，最右）
                    // 底色圆 40dp，触摸区域保持 48dp
                    IconButton(
                        onClick = {
                            onSettings()
                        },
                        modifier = Modifier.padding(horizontal = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(softIconButtonBg(), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(painterResource(R.drawable.ic_settings), contentDescription = stringResource(R.string.settings))
                        }
                    }
                    }
                }
            )
        },
        floatingActionButton = {
            // ── FAB ── 多选模式下不隐藏、外观不变，点击提示退出批量管理
            FloatingActionButton(
                onClick = {
                    if (isMultiSelectMode) {
                        Toast.makeText(context, R.string.exit_multi_select_first, Toast.LENGTH_SHORT).show()
                    } else {
                        onAddEvent()
                    }
                },
                containerColor = fabContainerColor(),
                contentColor = fabContentColor(),
                elevation = FloatingActionButtonDefaults.elevation(0.dp)
            ) {
                Icon(painterResource(R.drawable.ic_add), contentDescription = stringResource(R.string.add_event), tint = fabContentColor())
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            // ── 主内容 ──
            if (events.isEmpty()) {
                EmptyView(modifier = Modifier.fillMaxSize(), isSearchActive = isSearchActive)
            } else {
                if (viewMode == ViewModeHelper.VIEW_MODE_CARD) {
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
                                onLongClick = { onEventLongClick(event) },
                                dateFormat = dateFormat
                            )
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                } else {
                    // ── 表格视图 ── 保留卡片边框，左右保留 6dp 边距，横向可滑动查看完整数据
                    val colWidths = remember(events) { calculateColumnWidths(events, dateFormat) }
                    val horizontalScrollState = rememberScrollState()
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(start = 6.dp, end = 6.dp, bottom = 16.dp)
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
                                    itemsIndexed(events, key = { _, event -> event.id }) { index, event ->
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
                        containerColor = selectAllFabContainerColor(),
                        contentColor = selectAllFabContentColor(),
                        elevation = FloatingActionButtonDefaults.elevation(0.dp)
                    ) {
                        Icon(
                            Icons.Outlined.SelectAll,
                            contentDescription = stringResource(R.string.select_all)
                        )
                    }
                    // 删除所选按钮 - 56dp 圆形
                    FloatingActionButton(
                        onClick = {
                            onDeleteSelected()
                        },
                        modifier = Modifier.size(56.dp),
                        containerColor = deleteFabContainerColor(),
                        contentColor = deleteFabContentColor(),
                        elevation = FloatingActionButtonDefaults.elevation(0.dp)
                    ) {
                        Icon(painterResource(R.drawable.ic_delete), contentDescription = stringResource(R.string.delete_selected))
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
    onLongClick: () -> Unit,
    dateFormat: SimpleDateFormat
) {
    // 亮色下用博客风淡蓝卡片底，深色回退默认 surface；
    // 选中时：用不透明的混合色填充（半透明色会透出卡片阴影，在边缘形成一圈深色"粗框"）
    val base = if (isDarkColorScheme()) MaterialTheme.colorScheme.surface else BlogCardBlue
    val containerColor by animateColorAsState(
        targetValue = if (isSelected)
            lerp(base, MaterialTheme.colorScheme.primaryContainer, 0.5f)
        else
            base,
        label = "eventCardColor"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = MaterialTheme.shapes.medium, // 12dp
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = if (isSelected) null else BorderStroke(1.dp, cardBorderColor())
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
                    color = cardSubTextColor(),
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

    // 交替行背景：亮色为中性白/灰条纹（图 2 效果），深色为深灰/稍亮深灰。
    // 使用固定色而非 surface/surfaceContainerHigh，避免受主题色、壁纸影响而各设备不一致。
    val rowBackground = when {
        isSelected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        isPressed -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
        isEvenRow -> if (isDarkColorScheme()) Color(0xFF1C1C1E) else Color(0xFFFFFFFF)
        else -> if (isDarkColorScheme()) Color(0xFF2C2D31) else Color(0xFFECEEF1)
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
// 顶部搜索框 — 搜索模式下占据 TopAppBar 标题区域
// ═══════════════════════════════════════════════════

@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val dark = isDarkColorScheme()
    val textColor = if (dark) MaterialTheme.colorScheme.onSurface else Color.Black
    val hintColor = if (dark) MaterialTheme.colorScheme.onSurfaceVariant else Color.Black.copy(alpha = 0.5f)
    val bgColor = if (dark) MaterialTheme.colorScheme.surfaceContainerHigh else BlogCardBlue
    // 进入搜索即刻唤起输入法
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(bgColor)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_search),
            contentDescription = null,
            tint = hintColor,
            modifier = Modifier.size(20.dp)
        )
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .weight(1f)
                .padding(start = 10.dp),
            singleLine = true,
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            textStyle = TextStyle(color = textColor, fontSize = 16.sp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            decorationBox = { innerTextField ->
                Box(contentAlignment = Alignment.CenterStart) {
                    if (query.isEmpty()) {
                        Text(
                            text = stringResource(R.string.search_hint),
                            style = TextStyle(color = hintColor, fontSize = 16.sp)
                        )
                    }
                    innerTextField()
                }
            }
        )
        if (query.isNotEmpty()) {
            IconButton(
                onClick = { onQueryChange("") },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_close),
                    contentDescription = stringResource(R.string.clear_search),
                    tint = hintColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════
// 空视图
// ═══════════════════════════════════════════════════

@Composable
fun EmptyView(modifier: Modifier = Modifier, isSearchActive: Boolean = false) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Text(
            stringResource(if (isSearchActive) R.string.no_search_results else R.string.no_events),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
