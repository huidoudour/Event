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
    val updatedAt: Long = System.currentTimeMillis()
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
