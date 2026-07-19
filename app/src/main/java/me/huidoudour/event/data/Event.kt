package me.huidoudour.event.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Objects

@Entity(tableName = "events")
data class Event(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    var title: String,
    var description: String?,
    var eventTime: Long,
    var createdAt: Long = System.currentTimeMillis(),
    var updatedAt: Long = System.currentTimeMillis()
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
                description == event.description
    }

    override fun hashCode(): Int {
        return Objects.hash(id, title, description, eventTime, createdAt, updatedAt)
    }
}
