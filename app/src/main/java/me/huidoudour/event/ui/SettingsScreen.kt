package me.huidoudour.event.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import me.huidoudour.event.R
import me.huidoudour.event.util.IconColorHelper
import me.huidoudour.event.util.LocaleHelper
import me.huidoudour.event.util.ThemeHelper
import me.huidoudour.event.util.ViewModeHelper

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
    onSettingApplied: () -> Unit = {}
) {
    val context = LocalContext.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(context.getString(R.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(painterResource(R.drawable.ic_back), contentDescription = context.getString(R.string.back))
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
            SectionHeader(titleRes = R.string.data_management)

            // 导出数据
            SettingsCard(
                icon = { Icon(painterResource(R.drawable.ic_export), contentDescription = null, modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.onSecondaryContainer) },
                titleRes = R.string.export_data,
                onClick = onExport
            )
            // 导入数据
            SettingsCard(
                icon = { Icon(painterResource(R.drawable.ic_import), contentDescription = null, modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.onSecondaryContainer) },
                titleRes = R.string.import_data,
                onClick = onImport
            )
            // 数据展示模式（XML：在数据分区下，无独立分区标题）
            var showViewModeDialog by remember { mutableStateOf(false) }
            val currentMode = ViewModeHelper.getViewMode(context)
            SettingsCard(
                icon = { Icon(painterResource(R.drawable.ic_list), contentDescription = null, modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.onSecondaryContainer) },
                titleRes = R.string.data_display_mode,
                onClick = { showViewModeDialog = true }
            )
            if (showViewModeDialog) {
                val modeNames = listOf(
                    context.getString(R.string.card_view),
                    context.getString(R.string.list_view)
                ).toTypedArray()
                SingleChoiceDialog(
                    title = context.getString(R.string.data_display_mode),
                    items = modeNames,
                    checkedIndex = currentMode,
                    onDismiss = { showViewModeDialog = false },
                    onConfirm = { idx ->
                        if (idx != currentMode) {
                            ViewModeHelper.setViewMode(context, idx)
                        }
                        showViewModeDialog = false
                    }
                )
            }
            // 排序设置（XML：在数据分区下，marginBottom=16dp）
            var showSortDialog by remember { mutableStateOf(false) }
            val sortOptions = arrayOf(
                context.getString(R.string.sort_ascending),
                context.getString(R.string.sort_descending)
            )
            SettingsCard(
                icon = { Icon(painterResource(R.drawable.ic_sort), contentDescription = null, modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.onSecondaryContainer) },
                titleRes = R.string.sort_order_settings,
                onClick = { showSortDialog = true },
                bottomSpacing = 16
            )
            if (showSortDialog) {
                val checkedIdx = if (isAscending) 0 else 1
                SingleChoiceDialog(
                    title = context.getString(R.string.sort_order_settings),
                    items = sortOptions,
                    checkedIndex = checkedIdx,
                    onDismiss = { showSortDialog = false },
                    onConfirm = { idx ->
                        val newAscending = idx == 0
                        if (newAscending != isAscending) {
                            onSortOrderChanged(newAscending)
                        }
                        showSortDialog = false
                    }
                )
            }

            // ==================== 设置 ====================
            SectionHeader(titleRes = R.string.settings)

            // 语言设置
            var showLangDialog by remember { mutableStateOf(false) }
            val languages = LocaleHelper.getSupportedLanguages()
            val currentLang = LocaleHelper.getLanguage(context)
            SettingsCard(
                icon = { Icon(painterResource(R.drawable.ic_language), contentDescription = null, modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.onSecondaryContainer) },
                titleRes = R.string.language_settings,
                onClick = { showLangDialog = true }
            )
            if (showLangDialog) {
                val langNames = languages.map {
                    LocaleHelper.getLanguageDisplayName(context, it)
                }.toTypedArray()
                val checkedIdx = languages.indexOfFirst { it == currentLang }.coerceAtLeast(0)
                SingleChoiceDialog(
                    title = context.getString(R.string.select_language),
                    items = langNames,
                    checkedIndex = checkedIdx,
                    onDismiss = { showLangDialog = false },
                    onConfirm = { idx ->
                        if (languages[idx] != currentLang) {
                            LocaleHelper.setLanguage(context, languages[idx])
                            onSettingApplied()
                        }
                        showLangDialog = false
                    }
                )
            }

            // 主题设置（XML使用ic_panel_hollow）
            var showThemeDialog by remember { mutableStateOf(false) }
            val themes = ThemeHelper.getSupportedThemes()
            val currentTheme = ThemeHelper.getTheme(context)
            SettingsCard(
                icon = { Icon(painterResource(R.drawable.ic_panel_hollow), contentDescription = null, modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.onSecondaryContainer) },
                titleRes = R.string.theme_settings,
                onClick = { showThemeDialog = true }
            )
            if (showThemeDialog) {
                val themeNames = themes.map { ThemeHelper.getThemeDisplayName(context, it) }.toTypedArray()
                val checkedIdx = themes.indexOfFirst { it == currentTheme }.coerceAtLeast(0)
                SingleChoiceDialog(
                    title = context.getString(R.string.select_theme),
                    items = themeNames,
                    checkedIndex = checkedIdx,
                    onDismiss = { showThemeDialog = false },
                    onConfirm = { idx ->
                        if (themes[idx] != currentTheme) {
                            ThemeHelper.setTheme(context, themes[idx])
                            onSettingApplied()
                        }
                        showThemeDialog = false
                    }
                )
            }

            // 主题色设置（XML使用ic_panel_solid，marginBottom=16dp）
            var showColorDialog by remember { mutableStateOf(false) }
            val colors = ThemeHelper.getSupportedThemeColors()
            val currentColor = ThemeHelper.getThemeColor(context)
            SettingsCard(
                icon = { Icon(painterResource(R.drawable.ic_panel_solid), contentDescription = null, modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.onSecondaryContainer) },
                titleRes = R.string.theme_color_settings,
                onClick = { showColorDialog = true },
                bottomSpacing = 16
            )
            if (showColorDialog) {
                val colorNames = colors.map { ThemeHelper.getThemeColorDisplayName(context, it) }.toTypedArray()
                val checkedIdx = colors.indexOfFirst { it == currentColor }.coerceAtLeast(0)
                SingleChoiceDialog(
                    title = context.getString(R.string.select_theme_color),
                    items = colorNames,
                    checkedIndex = checkedIdx,
                    onDismiss = { showColorDialog = false },
                    onConfirm = { idx ->
                        if (colors[idx] != currentColor) {
                            ThemeHelper.setThemeColor(context, colors[idx])
                            onSettingApplied()
                        }
                        showColorDialog = false
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
                    context.getString(R.string.default_icon),
                    context.getString(R.string.colorful_icon)
                )
                SingleChoiceDialog(
                    title = context.getString(R.string.select_icon_color),
                    items = iconColorNames,
                    checkedIndex = currentIconColor.coerceIn(0, 1),
                    onDismiss = { showIconColorDialog = false },
                    onConfirm = { idx ->
                        if (idx != currentIconColor) {
                            IconColorHelper.setIconColor(context, idx)
                            IconColorHelper.applyIconColor(context, idx)
                            onSettingApplied()
                        }
                        showIconColorDialog = false
                    }
                )
            }

            // 关于应用（XML使用ic_version，文字为about_app）
            SettingsCard(
                icon = { Icon(painterResource(R.drawable.ic_version), contentDescription = null, modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.onSecondaryContainer) },
                titleRes = R.string.about_app,
                onClick = onAboutDeveloper
            )

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
    val context = LocalContext.current
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
        text = context.getString(titleRes),
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier
    )
}

@Composable
private fun SettingsCard(
    icon: @Composable () -> Unit,
    titleRes: Int,
    subtitle: String? = null,
    onClick: () -> Unit,
    bottomSpacing: Int = 4
) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = bottomSpacing.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        // 对齐 XML: selectableItemBackground 放在内层布局上
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clickable(onClick = onClick),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 40dp 圆形图标容器 — 对齐 XML: cardCornerRadius="20dp"
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                icon()
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = context.getString(titleRes),
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

@Composable
private fun SingleChoiceDialog(
    title: String,
    items: Array<String>,
    checkedIndex: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var selected by remember { mutableStateOf(checkedIndex) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                items.forEachIndexed { index, name ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selected = index
                                onConfirm(index)
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = index == selected,
                            onClick = {
                                selected = index
                                onConfirm(index)
                            }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(name)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}

private fun getLanguageDisplayNameForRes(context: android.content.Context, lang: String): String {
    return when (lang) {
        "system" -> "跟随系统"
        "zh-rCN" -> "简体中文"
        "zh-rTW" -> "繁体中文"
        "en-rUS" -> "English"
        "ru-rRU" -> "Русский"
        "ja-rJP" -> "日本語"
        else -> lang
    }
}
