package me.huidoudour.event.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import me.huidoudour.event.R
import me.huidoudour.event.ui.theme.blogBackground
import me.huidoudour.event.ui.theme.cardBorderColor
import me.huidoudour.event.ui.theme.isDarkColorScheme
import me.huidoudour.event.ui.theme.topAppBarColors

// ── 项目主页链接 ──
private const val URL_EVENT = "https://github.com/huidoudour/Event"
private const val URL_COMPOSE = "https://developer.android.com/jetpack/compose"
private const val URL_APPCOMPAT = "https://developer.android.com/jetpack/androidx/releases/appcompat"
private const val URL_ACTIVITY = "https://developer.android.com/jetpack/androidx/releases/activity"
private const val URL_FRAGMENT = "https://developer.android.com/jetpack/androidx/releases/fragment"
private const val URL_LIFECYCLE = "https://developer.android.com/jetpack/androidx/releases/lifecycle"
private const val URL_ROOM = "https://developer.android.com/jetpack/androidx/releases/room"
private const val URL_MATERIAL = "https://github.com/material-components/material-components-android"
private const val URL_MATERIALKOLOR = "https://github.com/jordond/materialkolor"
// SQLite 官方仓库（Public Domain）；构建实际经 requery/sqlite-android fork 引入
private const val URL_SQLITE = "https://github.com/sqlite/sqlite"
private const val URL_COMPOSE_MARKDOWN = "https://github.com/jeziellago/compose-markdown"
private const val URL_RXJAVA = "https://github.com/ReactiveX/RxJava"
private const val URL_RXANDROID = "https://github.com/ReactiveX/RxAndroid"
private const val URL_MTFILES = "https://github.com/L-JINBIN/MTDataFilesProvider"
private const val URL_JUNIT = "https://github.com/junit-team/junit4"
private const val URL_TEST = "https://developer.android.com/training/testing"
private const val URL_KOTLIN = "https://github.com/JetBrains/kotlin"
private const val URL_AGP = "https://developer.android.com/build/releases/gradle-plugin"
private const val URL_KSP = "https://github.com/google/ksp"

// ── assets/licenses/ 下的协议全文文件 ──
private const val LIC_APACHE = "apache-2.0.txt"
private const val LIC_MIT_EVENT = "mit-event.txt"
private const val LIC_MIT_MATERIALKOLOR = "mit-materialkolor.txt"
private const val LIC_MIT_COMPOSE_MARKDOWN = "mit-compose-markdown.txt"
private const val LIC_SQLITE_PD = "sqlite-public-domain.txt"
private const val LIC_EPL = "epl-1.0.txt"

/** 单个开源项目条目：url 为项目主页，licenseFile 为内置协议全文（null 表示未提供，不可展开） */
private data class LicenseItem(
    val name: String,
    val version: String,
    val license: String,
    val url: String,
    val licenseFile: String? = null
)

/** 条目卡片圆角：每个条目独立成正常圆角卡片，不做分段连排 */
private val LicItemRadius = 16.dp
// 点击层始终用全圆角：波纹按 shape 裁剪，展开时若切换为顶部圆角会变成直角波纹
private val LicItemShape = RoundedCornerShape(LicItemRadius)

/**
 * 开源许可页面 Compose 组件：
 * 展示本项目与所使用的第三方开源依赖、构建工具的许可证信息。
 * 点击条目展开协议全文，行尾外链按钮跳转项目主页。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OpenSourceLicensesScreenContent(onBack: () -> Unit) {
    val context = LocalContext.current
    val appVersion = remember {
        try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: ""
        } catch (_: Exception) { "" }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .blogBackground(),
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.open_source_licenses)) },
                colors = topAppBarColors(),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(painterResource(R.drawable.ic_back), contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp)
        ) {
            // 顶部说明
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                border = BorderStroke(1.dp, cardBorderColor()),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDarkColorScheme()) MaterialTheme.colorScheme.surfaceContainerLow else Color.Transparent
                )
            ) {
                Text(
                    text = stringResource(R.string.licenses_notice),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp)
                )
            }

            Spacer(Modifier.height(8.dp))

            // ==================== 本项目 ====================
            LicenseGroup(
                titleRes = R.string.licenses_this_app,
                items = listOf(
                    LicenseItem(
                        name = stringResource(R.string.app_name),
                        version = "v$appVersion",
                        license = "MIT License",
                        url = URL_EVENT,
                        licenseFile = LIC_MIT_EVENT
                    )
                )
            )

            Spacer(Modifier.height(16.dp))

            // ==================== 第三方开源依赖 ====================
            LicenseGroup(
                titleRes = R.string.licenses_third_party,
                items = listOf(
                    LicenseItem("Jetpack Compose", "BOM 2026.08.00", "Apache License 2.0", URL_COMPOSE, LIC_APACHE),
                    LicenseItem("AndroidX AppCompat", "1.8.0", "Apache License 2.0", URL_APPCOMPAT, LIC_APACHE),
                    LicenseItem("AndroidX Activity Compose", "1.13.0", "Apache License 2.0", URL_ACTIVITY, LIC_APACHE),
                    LicenseItem("AndroidX Fragment KTX", "1.9.0", "Apache License 2.0", URL_FRAGMENT, LIC_APACHE),
                    LicenseItem("AndroidX Lifecycle (ViewModel / LiveData)", "2.11.0", "Apache License 2.0", URL_LIFECYCLE, LIC_APACHE),
                    LicenseItem("AndroidX Room", "2.8.4", "Apache License 2.0", URL_ROOM, LIC_APACHE),
                    LicenseItem("Material Components for Android", "1.14.0", "Apache License 2.0", URL_MATERIAL, LIC_APACHE),
                    LicenseItem("MaterialKolor", "5.0.0", "MIT License", URL_MATERIALKOLOR, LIC_MIT_MATERIALKOLOR),
                    LicenseItem("SQLite", "3.49.0 (via requery/sqlite-android)", "Public Domain", URL_SQLITE, LIC_SQLITE_PD),
                    LicenseItem("Compose Markdown", "0.7.2", "MIT License", URL_COMPOSE_MARKDOWN, LIC_MIT_COMPOSE_MARKDOWN),
                    LicenseItem("RxJava", "3.1.12", "Apache License 2.0", URL_RXJAVA, LIC_APACHE),
                    LicenseItem("RxAndroid", "3.0.2", "Apache License 2.0", URL_RXANDROID, LIC_APACHE),
                    LicenseItem("MTDataFilesProvider", "v1.0.0", stringResource(R.string.license_not_provided), URL_MTFILES),
                    LicenseItem("JUnit", "4.13.2", "Eclipse Public License 1.0", URL_JUNIT, LIC_EPL),
                    LicenseItem("AndroidX Test (JUnit Ext / Espresso)", "1.3.0 / 3.7.0", "Apache License 2.0", URL_TEST, LIC_APACHE)
                )
            )

            Spacer(Modifier.height(16.dp))

            // ==================== 构建工具 ====================
            LicenseGroup(
                titleRes = R.string.licenses_build_tools,
                items = listOf(
                    LicenseItem("Kotlin", "2.4.10", "Apache License 2.0", URL_KOTLIN, LIC_APACHE),
                    LicenseItem("Android Gradle Plugin", "9.3.1", "Apache License 2.0", URL_AGP, LIC_APACHE),
                    LicenseItem("KSP (Kotlin Symbol Processing)", "2.3.10", "Apache License 2.0", URL_KSP, LIC_APACHE)
                )
            )
        }
    }
}

/** 分组容器：标题 + 独立圆角卡片列表（每个条目一张正常圆角卡片，行间留白） */
@Composable
private fun LicenseGroup(
    titleRes: Int,
    items: List<LicenseItem>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = stringResource(titleRes),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp)
        )
        items.forEachIndexed { index, item ->
            LicenseRow(item = item)
            if (index < items.lastIndex) Spacer(Modifier.height(8.dp))
        }
    }
}

// 许可证标签与外链按钮固定配色（避免动态主题出现紫色）
private val LicTagLight = Color(0xFFE3F2FD)   // 标签底：淡蓝
private val LicTagDark = Color(0xFF364954)    // 标签底：深蓝灰
private val LicBtnLight = Color(0xFFD0E6F3)   // 外链按钮圆底：淡蓝（同设置页）
private val LicBtnDark = Color(0xFF364954)    // 外链按钮圆底：深蓝灰

/** 单个依赖条目：独立圆角卡片，点击行展开/收起协议全文，行尾外链按钮跳转项目主页 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LicenseRow(item: LicenseItem) {
    val context = LocalContext.current
    // 未提供协议全文的条目不可展开
    val expandable = item.licenseFile != null
    var expanded by remember { mutableStateOf(false) }

    // 展开时从 assets 读取协议全文；读取放到 IO 线程，避免阻塞主线程
    var licenseText by remember(item.licenseFile, expanded) { mutableStateOf("") }
    DisposableEffect(item.licenseFile, expanded) {
        if (expanded && item.licenseFile != null) {
            val disposable = Single.fromCallable {
                context.assets.open("licenses/${item.licenseFile}")
                    .bufferedReader()
                    .use { it.readText() }
            }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { text -> licenseText = text },
                    { licenseText = "" }
                )
            onDispose { disposable.dispose() }
        } else {
            licenseText = ""
            onDispose { }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(LicItemShape)
            .background(if (isDarkColorScheme()) MaterialTheme.colorScheme.surfaceContainerLow else Color.Transparent)
    ) {
        Surface(
            onClick = { if (expandable) expanded = !expanded },
            enabled = expandable,
            // 波纹与卡片圆角同心、不进入展开区；行本身透明，底部直角由外层 Column 背景接续
            shape = LicItemShape,
            color = Color.Transparent,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 6.dp, top = 12.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = item.version,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                // 许可证标签（胶囊形）
                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (isDarkColorScheme()) LicTagDark else LicTagLight,
                    contentColor = if (isDarkColorScheme()) Color(0xFFB4CAD6) else Color(0xFF0D47A1)
                ) {
                    Text(
                        text = item.license,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                    )
                }
                // 展开/收起箭头（仅可展开条目）
                if (expandable) {
                    Icon(
                        imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(start = 2.dp, end = 4.dp)
                    )
                }
                // 外链按钮：淡蓝圆底，跳转项目主页（自带圆形波纹，不与其他区域冲突）
                Surface(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(item.url))
                        context.startActivity(intent)
                    },
                    shape = CircleShape,
                    color = if (isDarkColorScheme()) LicBtnDark else LicBtnLight,
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // 展开区：协议全文
        if (expanded) {
            HorizontalDivider(
                thickness = 1.dp,
                color = if (isDarkColorScheme()) MaterialTheme.colorScheme.outline else Color.Black.copy(alpha = 0.12f),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            val text = if (licenseText.isNotEmpty()) licenseText
            else stringResource(R.string.license_not_provided)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isDarkColorScheme()) MaterialTheme.colorScheme.surfaceContainerLow else Color(0xFFF1F6FC))
                    .border(1.dp, cardBorderColor(), RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
