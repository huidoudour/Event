package me.huidoudour.event.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import me.huidoudour.event.R
import me.huidoudour.event.util.*

/**
 * 设置页面 Compose 组件：对齐原 XML 布局 activity_settings.xml
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreenContent(
    onBack: () -> Unit,
    onAboutDeveloper: () -> Unit,
    onExport: () -> Unit,
    onImport: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(painterResource(R.drawable.ic_back), contentDescription = "返回")
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
            val context = LocalContext.current

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
            val modeDisplay = ViewModeHelper.getViewModeDisplayName(context, currentMode)
            SettingsCard(
                icon = { Icon(painterResource(R.drawable.ic_list), contentDescription = null, modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.onSecondaryContainer) },
                titleRes = R.string.data_display_mode,
                subtitle = modeDisplay,
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
            SettingsCard(
                icon = { Icon(painterResource(R.drawable.ic_sort), contentDescription = null, modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.onSecondaryContainer) },
                titleRes = R.string.sort_order_settings,
                onClick = { showSortDialog = true },
                bottomSpacing = 16
            )
            if (showSortDialog) {
                val sortOptions = arrayOf(
                    context.getString(R.string.sort_ascending),
                    context.getString(R.string.sort_descending)
                )
                SingleChoiceDialog(
                    title = context.getString(R.string.sort_order_settings),
                    items = sortOptions,
                    checkedIndex = 1,
                    onDismiss = { showSortDialog = false },
                    onConfirm = { showSortDialog = false }
                )
            }

            // ==================== 设置 ====================
            SectionHeader(titleRes = R.string.settings)

            // 语言设置
            var showLangDialog by remember { mutableStateOf(false) }
            val languages = LocaleHelper.getSupportedLanguages()
            val currentLang = LocaleHelper.getLanguage(context)
            val currentLangName = getLanguageDisplayNameForRes(context, currentLang)
            SettingsCard(
                icon = { Icon(painterResource(R.drawable.ic_language), contentDescription = null, modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.onSecondaryContainer) },
                titleRes = R.string.language_settings,
                subtitle = currentLangName,
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
                subtitle = ThemeHelper.getThemeDisplayName(context, currentTheme),
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
                subtitle = ThemeHelper.getThemeColorDisplayName(context, currentColor),
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
                        }
                        showColorDialog = false
                    }
                )
            }

            // ==================== 关于 ====================
            SectionHeader(titleRes = R.string.about)

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

@Composable
private fun SectionHeader(titleRes: Int) {
    val context = LocalContext.current
    Text(
        text = context.getString(titleRes),
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 32.dp, top = 16.dp, bottom = 8.dp)
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
            .padding(bottom = bottomSpacing.dp)
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 40dp 圆形图标容器
            Surface(
                modifier = Modifier.size(40.dp),
                shape = MaterialTheme.shapes.extraLarge,
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
