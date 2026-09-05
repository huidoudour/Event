package me.huidoudour.event.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamicColorScheme

/**
 * CompositionLocal：直接传递 darkTheme 布尔值，供 isDarkColorScheme() 等函数使用。
 * 比 background.luminance() 判断更可靠，不依赖生成的 ColorScheme 具体亮度。
 */
val LocalDarkTheme = compositionLocalOf { false }

/**
 * 根据主题色索引构建完整 MD3 ColorScheme。
 * - 索引 0：跟随系统壁纸动态取色 (Material You)，API<31 回退到蓝色
 * - 索引 1~6：使用 materialkolor 从种子色自动生成完整 ColorScheme
 *
 * @param darkTheme 是否深色主题
 * @param themeColor [me.huidoudour.event.util.ThemeHelper] 主题色常量 (0=系统色, 1~6=手动色)
 */
@Composable
fun eventColorScheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    themeColor: Int = 0
): androidx.compose.material3.ColorScheme {
    // 索引 0：跟随系统壁纸取色
    if (themeColor == 0 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val context = LocalContext.current
        return if (darkTheme) dynamicDarkColorScheme(context)
        else dynamicLightColorScheme(context)
    }
    // 手工主题色：materialkolor 根据种子色自动生成完整 ColorScheme（含 surface 细微底调）
    val seed = themeSeedColor(themeColor)
    return dynamicColorScheme(
        primary = seed,
        isDark = darkTheme,
        isAmoled = false,
        style = PaletteStyle.Neutral,
        contrastLevel = -0.15,
        modifyColorScheme = { cs ->
            if (darkTheme) {
                // 深色模式：显式覆盖背景/前景色，避免 materialkolor 低对比度参数导致
                // 背景偏亮 + 文字/图标偏暗
                cs.copy(
                    primary = lerp(Color(0xFF1C1B1F), cs.primary, 0.55f),
                    primaryContainer = lerp(Color(0xFF1C1B1F), cs.primaryContainer, 0.45f),
                    // 背景色 — 标准 Material3 深色层级
                    background = Color(0xFF121212),
                    surface = Color(0xFF1E1E1E),
                    surfaceContainerLowest = Color(0xFF0D0D0D),
                    surfaceContainerLow = Color(0xFF1B1B1B),
                    surfaceContainer = Color(0xFF202020),
                    surfaceContainerHigh = Color(0xFF282828),
                    surfaceContainerHighest = Color(0xFF303030),
                    // 前景色 — 确保深色背景下文字/图标明亮可读
                    onBackground = Color(0xFFE1E3E6),
                    onSurface = Color(0xFFE1E3E6),
                    onSurfaceVariant = Color(0xFFC0C7CD),
                    onPrimary = Color.White,
                    onPrimaryContainer = Color(0xFFC5E7FF),
                    onSecondaryContainer = Color(0xFFD0E6F3),
                    onTertiaryContainer = Color(0xFFE5DEFF),
                    inverseSurface = Color(0xFFE1E3E6),
                    inverseOnSurface = Color(0xFF2E3133),
                )
            } else {
                // 亮色模式也需显式覆盖前景色，避免 contrastLevel=-0.15 导致文字偏灰
                cs.copy(
                    primary = lerp(Color.White, cs.primary, 0.55f),
                    primaryContainer = lerp(Color.White, cs.primaryContainer, 0.45f),
                    // 表面色 — 淡蓝底（对话框、卡片等共用），覆盖所有 surface 层级
                    background = BlogBgBlue,
                    surface = BlogBgPaleBlue,
                    surfaceContainerLowest = BlogBgBlue,
                    surfaceContainerLow = BlogBgPaleBlue,
                    surfaceContainer = BlogBgPaleBlue,
                    surfaceContainerHigh = BlogBgPaleBlue,
                    surfaceContainerHighest = BlogCardBlue,
                    // 前景色 — 亮色下统一纯黑
                    onBackground = Color.Black,
                    onSurface = Color.Black,
                    onSurfaceVariant = Color.Black,
                    onPrimary = Color.White,
                    onPrimaryContainer = Color.Black,
                    onSecondaryContainer = Color.Black,
                    onTertiaryContainer = Color.Black,
                )
            }
        }
    )
}

@Composable
fun EventTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    themeColor: Int = 0,
    content: @Composable () -> Unit
) {
    val colorScheme = eventColorScheme(darkTheme = darkTheme, themeColor = themeColor)

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    // 通过 CompositionLocal 传递 darkTheme，替代 background.luminance() 判断
    CompositionLocalProvider(LocalDarkTheme provides darkTheme) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = EventTypography,
            content = content
        )
    }
}

/** 当前是否深色模式（直接读取 CompositionLocal，不再依赖背景亮度猜测） */
@Composable
fun isDarkColorScheme(): Boolean = LocalDarkTheme.current

/**
 * 博客风格淡蓝→淡粉渐变背景（参考 huidoudour.github.io body 渐变），
 * 仅亮色模式生效；深色模式返回 null，由调用方回退到默认背景色。
 */
@Composable
fun blogBackgroundBrush(): Brush? {
    if (isDarkColorScheme()) return null
    return Brush.linearGradient(
        colors = listOf(BlogBgBlue, BlogBgPaleBlue, BlogBgPaleBlue2),
        start = Offset.Zero,
        end = Offset.Infinite
    )
}

/**
 * 博客风格渐变背景 Modifier：亮色为淡蓝→淡粉渐变，深色回退默认背景色。
 * 封装 [blogBackgroundBrush] 与回退色，避免调用方处理 Brush?/Color 类型分支。
 */
@Composable
fun Modifier.blogBackground(): Modifier {
    val brush = blogBackgroundBrush()
    return if (brush != null) background(brush)
    else background(MaterialTheme.colorScheme.background)
}

/** 卡片边框色：亮色纯黑低 alpha（高对比），深色回退 outline */
@Composable
fun cardBorderColor(): Color =
    if (isDarkColorScheme()) MaterialTheme.colorScheme.outline
    else Color.Black.copy(alpha = 0.18f)

/** 卡片副文本色：亮色纯黑，深色回退 onSurfaceVariant */
@Composable
fun cardSubTextColor(): Color =
    if (isDarkColorScheme()) MaterialTheme.colorScheme.onSurfaceVariant
    else Color.Black

/** 工具栏图标按钮底色：亮色为淡蓝圆底，深色回退到 surfaceContainerHighest */
@Composable
fun softIconButtonBg(): Color =
    if (isDarkColorScheme()) MaterialTheme.colorScheme.surfaceContainerHighest
    else BlogBtnBlue.copy(alpha = 0.55f)

/** TopAppBar 配色：亮色淡蓝半透（透出渐变），深色跟随 surface */
@Composable
fun topAppBarColors(): TopAppBarColors =
    TopAppBarDefaults.topAppBarColors(
        containerColor = if (isDarkColorScheme()) MaterialTheme.colorScheme.surface else BlogCardBlue.copy(alpha = 0.85f),
        titleContentColor = if (isDarkColorScheme()) MaterialTheme.colorScheme.onSurface else Color.Black,
        actionIconContentColor = if (isDarkColorScheme()) MaterialTheme.colorScheme.onSurface else Color.Black
    )

/** 取消/次要按钮（OutlinedButton）底色：始终透明，仅保留边框 */
@Composable
fun softOutlinedButtonColors(): ButtonColors =
    ButtonDefaults.outlinedButtonColors(
        containerColor = Color.Transparent,
        contentColor = if (isDarkColorScheme()) MaterialTheme.colorScheme.onSurface else Color.Black
    )

/** 取消按钮边框：亮色可见黑线，深色跟随主题 outline */
@Composable
fun cancelButtonBorder(): BorderStroke =
    BorderStroke(1.dp, if (isDarkColorScheme()) MaterialTheme.colorScheme.outline else Color.Black.copy(alpha = 0.25f))

/** 确认按钮（filled Button）配色：亮色淡粉底+黑字，深色 primaryContainer */
@Composable
fun confirmButtonColors(): ButtonColors =
    ButtonDefaults.buttonColors(
        containerColor = if (isDarkColorScheme()) MaterialTheme.colorScheme.primaryContainer else BlogBtnPink.copy(alpha = 0.45f),
        contentColor = if (isDarkColorScheme()) MaterialTheme.colorScheme.onPrimaryContainer else Color.Black
    )

/** FAB 按钮配色：亮色淡粉底+黑字，深色 primaryContainer */
@Composable
fun fabContainerColor(): Color =
    if (isDarkColorScheme()) MaterialTheme.colorScheme.primaryContainer else BlogBtnPink.copy(alpha = 0.55f)

@Composable
fun fabContentColor(): Color =
    if (isDarkColorScheme()) MaterialTheme.colorScheme.onPrimaryContainer else Color.Black

/** 多选-全选 FAB：亮色淡蓝底，深色 secondaryContainer */
@Composable
fun selectAllFabContainerColor(): Color =
    if (isDarkColorScheme()) MaterialTheme.colorScheme.secondaryContainer else BlogBtnBlue.copy(alpha = 0.55f)

@Composable
fun selectAllFabContentColor(): Color =
    if (isDarkColorScheme()) MaterialTheme.colorScheme.onSecondaryContainer else Color.Black

/** 多选-删除 FAB：亮色淡红底，深色 errorContainer */
@Composable
fun deleteFabContainerColor(): Color =
    if (isDarkColorScheme()) MaterialTheme.colorScheme.errorContainer else Color(0xFFFFCDD2).copy(alpha = 0.55f)

@Composable
fun deleteFabContentColor(): Color =
    if (isDarkColorScheme()) MaterialTheme.colorScheme.onErrorContainer else Color.Black
