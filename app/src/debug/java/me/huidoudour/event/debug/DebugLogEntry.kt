package me.huidoudour.event.debug

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Debug 操作日志条目实体。
 * 记录用户的每一次关键操作，仅存在于 Debug 构建中。
 */
@Entity(tableName = "debug_logs")
data class DebugLogEntry(
    /** 操作时间戳（毫秒） */
    val timestamp: Long,
    /** 操作类型：CREATE / UPDATE / DELETE / DELETE_ALL / DELETE_BATCH / IMPORT / EXPORT / SORT / MODE_CHANGE / SETTINGS_CHANGE 等 */
    val operation: String,
    /** 操作详情描述 */
    val detail: String,
    /** 操作涉及的事件 ID（0 表示不涉及具体事件） */
    val entityId: Long
) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
}
