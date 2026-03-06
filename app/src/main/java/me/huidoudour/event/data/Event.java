package me.huidoudour.event.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "events")
public class Event {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private String title;
    private String description;
    private long eventTime;
    private long createdAt;

    public Event(String title, String description, long eventTime) {
        this.title = title;
        this.description = description;
        this.eventTime = eventTime;
        this.createdAt = System.currentTimeMillis();
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getEventTime() {
        return eventTime;
    }

    public void setEventTime(long eventTime) {
        this.eventTime = eventTime;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Event event = (Event) obj;
        return id == event.id &&
               eventTime == event.eventTime &&
               createdAt == event.createdAt &&
               java.util.Objects.equals(title, event.title) &&
               java.util.Objects.equals(description, event.description);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(id, title, description, eventTime, createdAt);
    }
}
