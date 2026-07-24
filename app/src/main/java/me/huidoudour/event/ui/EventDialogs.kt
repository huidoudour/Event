package me.huidoudour.event.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import me.huidoudour.event.R
import me.huidoudour.event.data.Event
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 添加/编辑事件对话框 - 对齐 dialog_event.xml (TextInputLayout OutlinedBox)
 */
@Composable
fun AddEventDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, description: String) -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(context.getString(R.string.add_event)) },
        text = {
            Column(
                modifier = Modifier.padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(context.getString(R.string.event_title)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(context.getString(R.string.event_description)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 1,
                    maxLines = 10
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirm(title, description)
                        onDismiss()
                    }
                },
                enabled = title.isNotBlank(),
                shape = RoundedCornerShape(12.dp)
            ) { Text(context.getString(R.string.save)) }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) { Text(context.getString(R.string.cancel)) }
        }
    )
}

@Composable
fun EditEventDialog(
    event: Event,
    onDismiss: () -> Unit,
    onConfirm: (title: String, description: String) -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf(event.title) }
    var description by remember { mutableStateOf(event.description ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(context.getString(R.string.edit) + "事件") },
        text = {
            Column(
                modifier = Modifier.padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(context.getString(R.string.event_title)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(context.getString(R.string.event_description)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 1,
                    maxLines = 10
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirm(title, description)
                        onDismiss()
                    }
                },
                enabled = title.isNotBlank(),
                shape = RoundedCornerShape(12.dp)
            ) { Text(context.getString(R.string.save)) }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) { Text(context.getString(R.string.cancel)) }
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
    val context = LocalContext.current
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
        title = { Text(context.getString(R.string.confirm_delete), style = MaterialTheme.typography.headlineSmall) },
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
                    label = { Text(context.getString(R.string.please_type_del)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (input == "d") {
                        onConfirm()
                        onDismiss()
                    }
                },
                enabled = input == "d",
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) { Text(context.getString(R.string.delete)) }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) { Text(context.getString(R.string.cancel)) }
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
    val context = LocalContext.current
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(event.title) },
        text = {
            Column {
                if (!event.description.isNullOrBlank()) {
                    Text("${context.getString(R.string.event_description)}：${event.description}")
                    Spacer(Modifier.height(8.dp))
                }
                Text("${context.getString(R.string.event_time)}：${dateFormat.format(Date(event.eventTime))}")
                Spacer(Modifier.height(4.dp))
                // 创建时间保持硬编码格式，因其含变量拼接
                Text("创建：${dateFormat.format(Date(event.createdAt))}")
            }
        },
        confirmButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) { Text(context.getString(R.string.close)) }
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
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(event.title) },
        text = {
            Column {
                TextButton(
                    onClick = { onDismiss(); onChangeTime() },
                    modifier = Modifier.fillMaxWidth()
                ) { Text(context.getString(R.string.change_datetime)) }
                TextButton(
                    onClick = { onDismiss(); onEdit() },
                    modifier = Modifier.fillMaxWidth()
                ) { Text(context.getString(R.string.edit)) }
                TextButton(
                    onClick = { onDismiss(); onDelete() },
                    modifier = Modifier.fillMaxWidth()
                ) { Text(context.getString(R.string.delete)) }
            }
        },
        confirmButton = {},
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) { Text(context.getString(R.string.cancel)) }
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
    val context = LocalContext.current
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
            Text(context.getString(R.string.confirm_clear), style = MaterialTheme.typography.headlineSmall)
        },
        text = {
            Column {
                Text(
                    context.getString(R.string.confirm_clear_all_message),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    label = { Text(context.getString(R.string.please_type_clear)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (input == "clear") {
                        onConfirm()
                        onDismiss()
                    }
                },
                enabled = input == "clear",
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) { Text(context.getString(R.string.clear)) }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) { Text(context.getString(R.string.cancel)) }
        }
    )
}

@Composable
fun BatchDeleteConfirmDialog(
    count: Int,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(context.getString(R.string.confirm_delete_selected)) },
        text = { Text("${context.getString(R.string.delete_selected)} $count ?") },
        confirmButton = {
            Button(
                onClick = { onConfirm(); onDismiss() },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) { Text(context.getString(R.string.delete)) }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) { Text(context.getString(R.string.cancel)) }
        }
    )
}
