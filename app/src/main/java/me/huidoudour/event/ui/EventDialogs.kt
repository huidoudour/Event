package me.huidoudour.event.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.huidoudour.event.data.Event
import java.text.SimpleDateFormat
import me.huidoudour.event.R
import java.util.*

/**
 * 添加/编辑事件对话框 - 对齐 dialog_event.xml (TextInputLayout OutlinedBox)
 */
@Composable
fun AddEventDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, description: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加事件") },
        text = {
            Column(
                modifier = Modifier.padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("事件标题") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("事件描述") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    maxLines = 10
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirm(title, description)
                        onDismiss()
                    }
                },
                enabled = title.isNotBlank()
            ) { Text("保存") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}

@Composable
fun EditEventDialog(
    event: Event,
    onDismiss: () -> Unit,
    onConfirm: (title: String, description: String) -> Unit
) {
    var title by remember { mutableStateOf(event.title) }
    var description by remember { mutableStateOf(event.description ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("编辑事件") },
        text = {
            Column(
                modifier = Modifier.padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("事件标题") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("事件描述") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    maxLines = 10
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirm(title, description)
                        onDismiss()
                    }
                },
                enabled = title.isNotBlank()
            ) { Text("保存") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}

/**
 * 删除确认对话框 - 对齐 dialog_confirm_delete.xml
 */
@Composable
fun DeleteConfirmDialog(
    eventTitle: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    var input by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(MaterialTheme.shapes.extraLarge)
                    .background(MaterialTheme.colorScheme.errorContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painterResource(R.drawable.ic_delete),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        },
        title = { Text("确认删除", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column {
                Text(
                    "删除事件\"$eventTitle\"，请输入 d 确认：",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    label = { Text("请输入 d") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (input == "d") {
                        onConfirm()
                        onDismiss()
                    }
                },
                enabled = input == "d"
            ) { Text("删除") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}

/**
 * 事件详情对话框 - 对齐 dialog_event_detail.xml
 */
@Composable
fun EventDetailDialog(
    event: Event,
    onDismiss: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(event.title) },
        text = {
            Column {
                if (!event.description.isNullOrBlank()) {
                    Text("描述：${event.description}")
                    Spacer(Modifier.height(8.dp))
                }
                Text("时间：${dateFormat.format(Date(event.eventTime))}")
                Spacer(Modifier.height(4.dp))
                Text("创建：${dateFormat.format(Date(event.createdAt))}")
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("关闭") }
        }
    )
}

/**
 * 长按菜单
 */
@Composable
fun EventLongClickMenu(
    event: Event,
    onDismiss: () -> Unit,
    onChangeTime: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(event.title) },
        text = {
            Column {
                TextButton(
                    onClick = { onDismiss(); onChangeTime() },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("修改时间") }
                TextButton(
                    onClick = { onDismiss(); onEdit() },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("编辑") }
                TextButton(
                    onClick = { onDismiss(); onDelete() },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("删除") }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}

/**
 * 清空所有确认对话框 - 对齐 dialog_confirm_clear.xml
 */
@Composable
fun ClearAllConfirmDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    var input by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(MaterialTheme.shapes.extraLarge)
                    .background(MaterialTheme.colorScheme.errorContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painterResource(R.drawable.ic_delete),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        },
        title = {
            Text("清空所有事件", style = MaterialTheme.typography.headlineSmall)
        },
        text = {
            Column {
                Text(
                    "此操作将删除所有事件数据，且不可恢复。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    label = { Text("请输入 clear") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (input == "clear") {
                        onConfirm()
                        onDismiss()
                    }
                },
                enabled = input == "clear"
            ) { Text("清空") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}

@Composable
fun BatchDeleteConfirmDialog(
    count: Int,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("批量删除") },
        text = { Text("确定要删除已选的 $count 个事件吗？") },
        confirmButton = {
            TextButton(onClick = { onConfirm(); onDismiss() }) { Text("删除") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}
