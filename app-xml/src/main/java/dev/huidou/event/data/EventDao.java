package dev.huidou.event.data;

import android.content.ContentValues;
import android.database.Cursor;

import org.sqlite.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * 事件 DAO（手写 SQL，基于本地 org.sqlite 原生库）。
 * 所有查询均为同步操作，返回 List&lt;Event&gt;；实时刷新由 EventRepository 负责。
 */
public class EventDao {

    private final SQLiteDatabase db;

    public EventDao(SQLiteDatabase db) {
        this.db = db;
    }

    /** 插入事件，返回新行 id */
    public long insert(Event event) {
        ContentValues values = new ContentValues();
        values.put("title", event.getTitle());
        values.put("description", event.getDescription());
        values.put("eventTime", event.getEventTime());
        values.put("createdAt", event.getCreatedAt());
        values.put("updatedAt", event.getUpdatedAt());
        return db.insert("events", null, values);
    }

    /** 更新事件（不含 id/createdAt，仅更新内容与时间戳） */
    public void update(Event event) {
        ContentValues values = new ContentValues();
        values.put("title", event.getTitle());
        values.put("description", event.getDescription());
        values.put("eventTime", event.getEventTime());
        values.put("updatedAt", event.getUpdatedAt());
        db.update("events", values, "id=?", new String[]{String.valueOf(event.getId())});
    }

    /** 删除单条事件 */
    public void delete(Event event) {
        db.delete("events", "id=?", new String[]{String.valueOf(event.getId())});
    }

    /** 清空所有事件 */
    public void deleteAll() {
        db.delete("events", null, null);
    }

    /** 按 id 批量删除 */
    public void deleteByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return;
        StringBuilder placeholders = new StringBuilder();
        String[] args = new String[ids.size()];
        for (int i = 0; i < ids.size(); i++) {
            if (i > 0) placeholders.append(",");
            placeholders.append("?");
            args[i] = String.valueOf(ids.get(i));
        }
        db.delete("events", "id IN (" + placeholders + ")", args);
    }

    /** 重置自增 ID 计数器（清空数据后调用，使 ID 从 1 重新开始） */
    public void resetAutoIncrement() {
        db.execSQL("DELETE FROM sqlite_sequence WHERE name='events'");
    }

    /** 按更新时间倒序查询全部（默认列表用） */
    public List<Event> queryByUpdatedAtDesc() {
        return query("updatedAt DESC");
    }

    /** 按事件时间排序查询全部（ascending=true 正序，false 倒序） */
    public List<Event> queryByTime(boolean ascending) {
        String orderBy = ascending ? "eventTime ASC, id ASC" : "eventTime DESC, id DESC";
        return query(orderBy);
    }

    private List<Event> query(String orderBy) {
        List<Event> events = new ArrayList<>();
        Cursor cursor = db.query("events", null, null, null, null, null, orderBy);
        try {
            if (cursor != null) {
                int idxId = cursor.getColumnIndexOrThrow("id");
                int idxTitle = cursor.getColumnIndexOrThrow("title");
                int idxDescription = cursor.getColumnIndexOrThrow("description");
                int idxEventTime = cursor.getColumnIndexOrThrow("eventTime");
                int idxCreatedAt = cursor.getColumnIndexOrThrow("createdAt");
                int idxUpdatedAt = cursor.getColumnIndexOrThrow("updatedAt");
                while (cursor.moveToNext()) {
                    Event event = new Event(
                            cursor.getString(idxTitle),
                            cursor.getString(idxDescription),
                            cursor.getLong(idxEventTime)
                    );
                    event.setId(cursor.getLong(idxId));
                    event.setCreatedAt(cursor.getLong(idxCreatedAt));
                    event.setUpdatedAt(cursor.getLong(idxUpdatedAt));
                    events.add(event);
                }
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return events;
    }
}
