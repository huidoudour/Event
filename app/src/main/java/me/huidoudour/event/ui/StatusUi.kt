package me.huidoudour.event.ui

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import me.huidoudour.event.R
import me.huidoudour.event.data.EventStatus

/**
 * 处理方式选择器的展示顺序：先列出各种处理结果，最后是清除标记（未完成）。
 * 与 [EventStatus] 的声明顺序解耦，避免以后新增状态时打乱界面顺序。
 */
val eventStatusPickerOrder: List<EventStatus> = listOf(
    EventStatus.DONE,
    EventStatus.DONE_BY_OTHERS,
    EventStatus.UNPLANNED,
    EventStatus.IRRELEVANT,
    EventStatus.NONE,
)

/** 处理方式的本地化名称资源 */
@StringRes
fun EventStatus.labelRes(): Int = when (this) {
    EventStatus.NONE -> R.string.status_none
    EventStatus.DONE -> R.string.status_done
    EventStatus.DONE_BY_OTHERS -> R.string.status_done_by_others
    EventStatus.UNPLANNED -> R.string.status_unplanned
    EventStatus.IRRELEVANT -> R.string.status_irrelevant
}

/**
 * 详情页状态行的紧凑名称：[EventStatus.DONE_BY_OTHERS] 使用三字简称，
 * 与「已完成 / 未计划 / 不相干」等宽，使状态时间与创建/修改时间对齐。
 */
@StringRes
fun EventStatus.detailLabelRes(): Int = when (this) {
    EventStatus.DONE_BY_OTHERS -> R.string.status_done_by_others_short
    else -> labelRes()
}

/** 处理方式的图标：勾 / 人 / 日历取消 / 禁止 / 空心圆 */
fun EventStatus.icon(): ImageVector = when (this) {
    EventStatus.NONE -> Icons.Outlined.RadioButtonUnchecked
    EventStatus.DONE -> Icons.Outlined.Check
    EventStatus.DONE_BY_OTHERS -> Icons.Outlined.Person
    EventStatus.UNPLANNED -> Icons.Outlined.EventBusy
    EventStatus.IRRELEVANT -> Icons.Outlined.Block
}

/**
 * 处理方式的强调色：使用固定语义色（而非主题色），
 * 保证同一列表里「已完成 / 他人完成 / 未计划 / 不相干」能在各主题色下互相区分。
 */
@Composable
fun EventStatus.tint(): Color = when (this) {
    EventStatus.NONE -> MaterialTheme.colorScheme.onSurfaceVariant
    EventStatus.DONE -> if (isDarkColorScheme()) Color(0xFF81C784) else Color(0xFF2E7D32)
    EventStatus.DONE_BY_OTHERS -> if (isDarkColorScheme()) Color(0xFF64B5F6) else Color(0xFF1565C0)
    EventStatus.UNPLANNED -> if (isDarkColorScheme()) Color(0xFFFFB74D) else Color(0xFFEF6C00)
    EventStatus.IRRELEVANT -> if (isDarkColorScheme()) Color(0xFFBDBDBD) else Color(0xFF616161)
}

/** 处理方式标记的圆底/底色：强调色的低透明度版本，深浅色模式分别取值 */
@Composable
fun EventStatus.containerTint(): Color =
    tint().copy(alpha = if (isDarkColorScheme()) 0.24f else 0.14f)
