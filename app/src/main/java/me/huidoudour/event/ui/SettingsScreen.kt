package me.huidoudour.event.ui

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.huidoudour.event.R
import me.huidoudour.event.ui.theme.BlogCardBlue
import me.huidoudour.event.ui.theme.blogBackground
import me.huidoudour.event.ui.theme.cancelButtonBorder
import me.huidoudour.event.ui.theme.cardBorderColor
import me.huidoudour.event.ui.theme.confirmButtonColors
import me.huidoudour.event.ui.theme.isDarkColorScheme
import me.huidoudour.event.ui.theme.softOutlinedButtonColors
import me.huidoudour.event.util.IconColorHelper
import me.huidoudour.event.util.LocaleHelper
import me.huidoudour.event.util.ThemeHelper
import me.huidoudour.event.util.ViewModeHelper

// ═══════════════════════════════════════════════════
// 分段圆角形状 — 参考 Installer 项目
// ═══════════════════════════════════════════════════
private val SegCornerRadius = 16.dp
private val SegConnectionRadius = 5.dp
private val SegTopShape = RoundedCornerShape(SegCornerRadius, SegCornerRadius, SegConnectionRadius, SegConnectionRadius)
private val SegMiddleShape = RoundedCornerShape(SegConnectionRadius)
private val SegBottomShape = RoundedCornerShape(SegConnectionRadius, SegConnectionRadius, SegCornerRadius, SegCornerRadius)
private val SegSingleShape = RoundedCornerShape(SegCornerRadius)
private val SegGap: Dp = 4.dp
private val CardCorner = 28.dp
private val CardShape = RoundedCornerShape(CardCorner)

@Composable
private fun segmentedShape(index: Int, total: Int): androidx.compose.ui.graphics.Shape = when {
    total == 1 -> SegSingleShape
    index == 0 -> SegTopShape
    index == total - 1 -> SegBottomShape
    else -> SegMiddleShape
}

/**
 * 设置页面 Compose 组件：对齐原 XML 布局 activity_settings.xml
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreenContent(
    onBack: () -> Unit,
    onAboutDeveloper: () -> Unit,
    onExport: () -> Unit,
    onImport: () -> Unit,
    isAscending: Boolean = true,
    onSortOrderChanged: (Boolean) -> Unit = {},
    onThemeChanged: (Int) -> Unit = {},
    onNeedsRecreate: () -> Unit = {}
) {
    val context = LocalContext.current
    Scaffold(
        // 博客风格淡蓝→淡粉渐变背景（深色模式回退默认背景色）
        modifier = Modifier
            .fillMaxSize()
            .blogBackground(),
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                // 亮色下用半透明白，让渐变背景透出；深色回退默认 surface
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (isDarkColorScheme()) MaterialTheme.colorScheme.surface
                    else Color.White.copy(alpha = 0.72f),
                    actionIconContentColor = if (isDarkColorScheme()) MaterialTheme.colorScheme.onSurface
                    else Color.Black
                ),
                navigationIcon = {
                    // 返回按钮：无底色
                    IconButton(onClick = onBack) {
                        Icon(painterResource(R.drawable.ic_back), contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { padding ->
        // 对齐 XML activity_settings.xml：
        // 数据管理 → (导出, 导入, 数据展示模式, 排序)
        // 设置 → (语言, 主题-ic_panel_hollow, 主题色-ic_panel_solid)
        // 关于 → 关于应用
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {

            // ==================== 数据管理 ====================
            // 导出数据
            var showExportConfirm by remember { mutableStateOf(false) }
            // 导入数据
            var showImportConfirm by remember { mutableStateOf(false) }
            // 数据展示模式
            var showViewModeDialog by remember { mutableStateOf(false) }
            val currentMode = ViewModeHelper.getViewMode(context)
            // 排序设置
            var showSortDialog by remember { mutableStateOf(false) }
            val sortOptions = arrayOf(
                stringResource(R.string.sort_ascending),
                stringResource(R.string.sort_descending)
            )

            SettingGroup(titleRes = R.string.data_management) {
                SettingsCard(
                    icon = { Icon(painterResource(R.drawable.ic_export), contentDescription = null, modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.onSurface) },
                    titleRes = R.string.export_data,
                    onClick = { showExportConfirm = true },
                    modifier = Modifier.padding(horizontal = 12.dp),
                    shape = segmentedShape(0, 4)
                )
                Spacer(Modifier.height(SegGap))
                SettingsCard(
                    icon = { Icon(painterResource(R.drawable.ic_import), contentDescription = null, modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.onSurface) },
                    titleRes = R.string.import_data,
                    onClick = { showImportConfirm = true },
                    modifier = Modifier.padding(horizontal = 12.dp),
                    shape = segmentedShape(1, 4)
                )
                Spacer(Modifier.height(SegGap))
                SettingsCard(
                    icon = { Icon(painterResource(R.drawable.ic_list), contentDescription = null, modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.onSurface) },
                    titleRes = R.string.data_display_mode,
                    onClick = { showViewModeDialog = true },
                    modifier = Modifier.padding(horizontal = 12.dp),
                    shape = segmentedShape(2, 4)
                )
                Spacer(Modifier.height(SegGap))
                SettingsCard(
                    icon = { Icon(painterResource(R.drawable.ic_sort), contentDescription = null, modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.onSurface) },
                    titleRes = R.string.sort_order_settings,
                    onClick = { showSortDialog = true },
                    modifier = Modifier.padding(horizontal = 12.dp),
                    shape = segmentedShape(3, 4)
                )
            }

            Spacer(Modifier.height(16.dp))

            // ── 数据管理对话框 ──
            if (showExportConfirm) {
                AlertDialog(
                    onDismissRequest = { showExportConfirm = false },
                    title = { Text(stringResource(R.string.confirm_export)) },
                    text = { Text(stringResource(R.string.export_warning)) },
                    confirmButton = {
                        Button(onClick = { showExportConfirm = false; onExport() }, shape = RoundedCornerShape(12.dp), contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp), colors = confirmButtonColors()) {
                            Text(stringResource(R.string.ok), fontSize = 16.sp)
                        }
                    },
                    dismissButton = {
                        OutlinedButton(onClick = { showExportConfirm = false }, shape = RoundedCornerShape(12.dp), contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp), border = cancelButtonBorder(), colors = softOutlinedButtonColors()) {
                            Text(stringResource(R.string.cancel), fontSize = 16.sp)
                        }
                    }
                )
            }
            if (showImportConfirm) {
                AlertDialog(
                    onDismissRequest = { showImportConfirm = false },
                    title = { Text(stringResource(R.string.confirm_import)) },
                    text = { Text(stringResource(R.string.import_warning)) },
                    confirmButton = {
                        Button(onClick = { showImportConfirm = false; onImport() }, shape = RoundedCornerShape(12.dp), contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp), colors = confirmButtonColors()) {
                            Text(stringResource(R.string.ok), fontSize = 16.sp)
                        }
                    },
                    dismissButton = {
                        OutlinedButton(onClick = { showImportConfirm = false }, shape = RoundedCornerShape(12.dp), contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp), border = cancelButtonBorder(), colors = softOutlinedButtonColors()) {
                            Text(stringResource(R.string.cancel), fontSize = 16.sp)
                        }
                    }
                )
            }
            if (showViewModeDialog) {
                val modeNames = listOf(stringResource(R.string.card_view), stringResource(R.string.list_view)).toTypedArray()
                val viewModeChangedText = stringResource(R.string.view_mode_changed)
                SingleChoiceDialog(
                    title = stringResource(R.string.data_display_mode),
                    items = modeNames,
                    checkedIndex = currentMode,
                    onDismiss = { showViewModeDialog = false },
                    onConfirm = { idx ->
                        if (idx != currentMode) {
                            val mode = if (idx == 0) ViewModeHelper.VIEW_MODE_CARD else ViewModeHelper.VIEW_MODE_LIST
                            ViewModeHelper.setViewMode(context, mode)
                            Toast.makeText(context, viewModeChangedText + ": " + modeNames[idx], Toast.LENGTH_SHORT).show()
                        }
                        showViewModeDialog = false
                    }
                )
            }
            if (showSortDialog) {
                val checkedIdx = if (isAscending) 0 else 1
                val sortOrderChangedText = stringResource(R.string.sort_order_changed)
                SingleChoiceDialog(
                    title = stringResource(R.string.sort_order_settings),
                    items = sortOptions,
                    checkedIndex = checkedIdx,
                    onDismiss = { showSortDialog = false },
                    onConfirm = { idx ->
                        val newAscending = idx == 0
                        if (newAscending != isAscending) {
                            onSortOrderChanged(newAscending)
                            Toast.makeText(context, sortOrderChangedText + ": " + sortOptions[idx], Toast.LENGTH_SHORT).show()
                        }
                        showSortDialog = false
                    }
                )
            }

            // ==================== 设置 ====================
            var showLangDialog by remember { mutableStateOf(false) }
            val languages = LocaleHelper.getSupportedLanguages()
            val currentLang = LocaleHelper.getLanguage(context)
            var showThemeDialog by remember { mutableStateOf(false) }
            val themes = ThemeHelper.getSupportedThemes()
            val currentTheme = ThemeHelper.getTheme(context)
            SettingGroup(titleRes = R.string.settings) {
                SettingsCard(
                    icon = { Icon(painterResource(R.drawable.ic_language), contentDescription = null, modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.onSurface) },
                    titleRes = R.string.language_settings,
                    onClick = { showLangDialog = true },
                    modifier = Modifier.padding(horizontal = 12.dp),
                    shape = segmentedShape(0, 2)
                )
                Spacer(Modifier.height(SegGap))
                SettingsCard(
                    icon = { Icon(painterResource(R.drawable.ic_panel_hollow), contentDescription = null, modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.onSurface) },
                    titleRes = R.string.theme_settings,
                    onClick = { showThemeDialog = true },
                    modifier = Modifier.padding(horizontal = 12.dp),
                    shape = segmentedShape(1, 2)
                )
            }

            Spacer(Modifier.height(16.dp))

            // ── 设置对话框 ──
            if (showLangDialog) {
                val langNames = languages.map { LocaleHelper.getLanguageDisplayName(context, it) }.toTypedArray()
                val checkedIdx = languages.indexOfFirst { it == currentLang }.coerceAtLeast(0)
                SingleChoiceDialog(
                    title = stringResource(R.string.select_language),
                    items = langNames,
                    checkedIndex = checkedIdx,
                    onDismiss = { showLangDialog = false },
                    onConfirm = { idx ->
                        if (languages[idx] != currentLang) { LocaleHelper.setLanguage(context, languages[idx]); onNeedsRecreate() }
                        showLangDialog = false
                    }
                )
            }
            if (showThemeDialog) {
                val themeNames = themes.map { ThemeHelper.getThemeDisplayName(context, it) }.toTypedArray()
                val checkedIdx = themes.indexOfFirst { it == currentTheme }.coerceAtLeast(0)
                val themeChangedText = stringResource(R.string.theme_changed)
                SingleChoiceDialog(
                    title = stringResource(R.string.select_theme),
                    items = themeNames,
                    checkedIndex = checkedIdx,
                    onDismiss = { showThemeDialog = false },
                    onConfirm = { idx ->
                        if (themes[idx] != currentTheme) {
                            onThemeChanged(themes[idx])
                            Toast.makeText(context, themeChangedText + ": " + themeNames[idx], Toast.LENGTH_SHORT).show()
                        }
                        showThemeDialog = false
                    }
                )
            }
            // ==================== 关于 ====================
            var showIconColorDialog by remember { mutableStateOf(false) }
            val currentIconColor = IconColorHelper.getIconColor(context)
            SectionHeader(
                titleRes = R.string.about,
                onLongClick = { showIconColorDialog = true }
            )
            if (showIconColorDialog) {
                val iconColorNames = arrayOf(
                    stringResource(R.string.default_icon),
                    stringResource(R.string.colorful_icon)
                )
                SingleChoiceDialog(
                    title = stringResource(R.string.select_icon_color),
                    items = iconColorNames,
                    checkedIndex = currentIconColor.coerceIn(0, 1),
                    onDismiss = { showIconColorDialog = false },
                    onConfirm = { idx ->
                        if (idx != currentIconColor) {
                            IconColorHelper.setIconColor(context, idx)
                            IconColorHelper.applyIconColor(context, idx)
                            onNeedsRecreate()
                        }
                        showIconColorDialog = false
                    }
                )
            }

            SettingGroup(titleRes = R.string.about, showTitle = false) {
                SettingsCard(
                    icon = { Icon(painterResource(R.drawable.ic_version), contentDescription = null, modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.onSurface) },
                    titleRes = R.string.about_app,
                    onClick = onAboutDeveloper,
                    modifier = Modifier.padding(horizontal = 12.dp),
                    shape = segmentedShape(0, 1)
                )
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SectionHeader(
    titleRes: Int,
    onLongClick: (() -> Unit)? = null
) {
    val modifier = if (onLongClick != null) {
        Modifier
            .padding(start = 32.dp, top = 8.dp, bottom = 8.dp)
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {},
                onLongClick = onLongClick
            )
    } else {
        Modifier.padding(start = 32.dp, top = 8.dp, bottom = 8.dp)
    }
    Text(
        text = stringResource(titleRes),
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = modifier
    )
}

@Composable
private fun SettingsCard(
    icon: @Composable () -> Unit,
    titleRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(12.dp)
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .clickable(onClick = onClick),
        shape = shape,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, cardBorderColor()),
        // 亮色淡蓝底，深色保持透明
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkColorScheme()) Color.Transparent else BlogCardBlue
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 图标：卡片已有淡蓝底，不再额外加圆底
            icon()
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(titleRes),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline
            )
        }
    }
}

/**
 * 分段设置组 — 参照 Installer 应用设置区样式
 * 顶部和底部 item 用大圆角，中间 item 用小圆角连接
 */
@Composable
private fun SettingGroup(
    titleRes: Int,
    showTitle: Boolean = true,
    items: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        if (showTitle) {
            Text(
                text = stringResource(titleRes),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp)
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(CardShape)
                // 亮色透明（融入渐变背景），深色保持 surfaceContainerLow
                .background(if (isDarkColorScheme()) MaterialTheme.colorScheme.surfaceContainerLow else Color.Transparent)
                .padding(top = 8.dp, bottom = 12.dp)
        ) {
            items()
        }
    }
}

@Composable
private fun SingleChoiceDialog(
    title: String,
    items: Array<String>,
    checkedIndex: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var selected by remember { mutableIntStateOf(checkedIndex) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                color = Color.Transparent
            ) {
                Column {
                    items.forEachIndexed { index, name ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selected = index }
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val isSelected = selected == index
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                                    .then(
                                        if (!isSelected) Modifier.border(1.5.dp, MaterialTheme.colorScheme.outline, CircleShape)
                                        else Modifier
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.onPrimary)
                                    )
                                }
                            }
                            Spacer(Modifier.width(12.dp))
                            Text(name)
                        }
                        if (index < items.lastIndex) {
                            HorizontalDivider(
                                thickness = 1.dp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selected) },
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                colors = confirmButtonColors()
            ) {
                Text(stringResource(R.string.ok), fontSize = 16.sp)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                border = cancelButtonBorder(),
                colors = softOutlinedButtonColors()
            ) { Text(stringResource(R.string.cancel), fontSize = 16.sp) }
        }
    )
}
