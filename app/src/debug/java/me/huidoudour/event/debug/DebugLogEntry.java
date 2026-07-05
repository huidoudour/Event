package me.huidoudour.event.debug;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Debug 操作日志条目实体。
 * 记录用户的每一次关键操作，仅存在于 Debug 构建中。
 */
@Entity(tableName = "debug_logs")
public class DebugLogEntry {

    @PrimaryKey(autoGenerate = true)
    private long id;

    /** 操作时间戳（毫秒） */
    private long timestamp;

    /** 操作类型：CREATE / UPDATE / DELETE / DELETE_ALL / DELETE_BATCH / IMPORT / EXPORT / SORT / MODE_CHANGE / SETTINGS_CHANGE 等 */
    private String operation;

    /** 操作详情描述 */
    private String detail;

    /** 操作涉及的事件 ID（0 表示不涉及具体事件） */
    private long entityId;

    public DebugLogEntry(long timestamp, String operation, String detail, long entityId) {
        this.timestamp = timestamp;
        this.operation = operation;
        this.detail = detail;
        this.entityId = entityId;
    }

    // ── Getters & Setters ──

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getOperation() { return operation; }
    public void setOperation(String operation) { this.operation = operation; }

    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }

    public long getEntityId() { return entityId; }
    public void setEntityId(long entityId) { this.entityId = entityId; }
}
