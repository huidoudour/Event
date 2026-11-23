package me.huidoudour.event.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Objects

@Entity(tableName = "events")
data class Event(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String?,
    val eventTime: Long,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    /** 处理方式：未标记 / 已完成 / 他人完成 / 未计划 / 不相干 */
    val status: EventStatus = EventStatus.NONE,
    /** 处理方式的设置时间戳；未标记时为 null */
    val statusAt: Long? = null,
    /** 修改时间记录：每编辑一次事件（标题/描述/事件时间）就追加一条时间戳 */
    val editHistory: List<Long> = emptyList(),
    /** 处理方式变更记录：每切换一次状态就追加被替换下来的旧状态 */
    val statusHistory: List<StatusChange> = emptyList()
) {
    constructor(title: String, description: String?, eventTime: Long) : this(
        title = title,
        description = description,
        eventTime = eventTime,
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val event = other as Event
        return id == event.id &&
                eventTime == event.eventTime &&
                createdAt == event.createdAt &&
                updatedAt == event.updatedAt &&
                title == event.title &&
                description == event.description &&
                status == event.status &&
                statusAt == event.statusAt &&
                editHistory == event.editHistory &&
                statusHistory == event.statusHistory
    }

    override fun hashCode(): Int {
        return Objects.hash(
            id, title, description, eventTime, createdAt, updatedAt,
            status, statusAt, editHistory, statusHistory
        )
    }
}
