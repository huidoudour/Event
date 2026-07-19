package me.huidoudour.event.data;

import android.content.Context;
import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * 数据导入导出助手类
 * JSON 格式：{ "事件标题": "...", "事件详情": "...", "事件时间": "yyyy-MM-dd HH:mm" }
 */
public class DataImportExportHelper {

    private static final String FILE_NAME = "events_backup.json";
    private static final String KEY_TITLE = "事件标题";
    private static final String KEY_DETAIL = "事件详情";
    private static final String KEY_TIME = "事件时间";
    private static final SimpleDateFormat DATE_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    private final Context context;

    public DataImportExportHelper(Context context) {
        this.context = context;
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 导出
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * 将事件列表序列化为格式化 JSON 字符串。
     * 若列表为空则返回 null。
     */
    private String buildJsonString(List<Event> events) {
        if (events == null || events.isEmpty()) return null;
        JSONArray array = new JSONArray();
        for (Event event : events) {
            JSONObject obj = new JSONObject();
            try {
                obj.put(KEY_TITLE, event.getTitle() != null ? event.getTitle() : "");
                obj.put(KEY_DETAIL, event.getDescription() != null ? event.getDescription() : "");
                obj.put(KEY_TIME, DATE_FORMAT.format(new java.util.Date(event.getEventTime())));
                // 保存 createdAt 和 updatedAt 以保持原始时间戳
                obj.put("createdAt", event.getCreatedAt());
                obj.put("updatedAt", event.getUpdatedAt());
            } catch (Exception e) {
                e.printStackTrace();
            }
            array.put(obj);
        }
        try {
            return array.toString(2);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 导出数据到用户通过 SAF 选择的文件 URI。
     * 必须在后台线程调用（直接读取数据库）。
     */
    public boolean exportDataToUri(EventRepository repository, Uri uri) {
        try {
            List<Event> events = repository.getAllEventsSync();
            String json = buildJsonString(events);
            if (json == null) return false;
            OutputStream os = context.getContentResolver().openOutputStream(uri);
            if (os != null) {
                try {
                    os.write(json.getBytes(StandardCharsets.UTF_8));
                } finally {
                    os.close();
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 导出数据到应用私有目录（备用，不直接暴露给 UI）。
     */
    public boolean exportData(EventRepository repository) {
        try {
            List<Event> events = repository.allEvents.getValue();
            if (events == null) events = new ArrayList<>();
            String json = buildJsonString(events);
            if (json == null) return false;
            OutputStream os = context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
            try {
                os.write(json.getBytes(StandardCharsets.UTF_8));
            } finally {
                os.close();
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 导入
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * 解析 JSON 字符串并写入数据库。
     * 同时兼容旧格式（英文字段 id/title/description/eventTime/createdAt）
     * 和新格式（中文字段 事件标题/事件详情/事件时间）。
     * 导入时会清空数据库并重新生成连续的ID。
     */
    private boolean parseAndSaveJsonData(
            String jsonString,
            EventRepository repository,
            boolean clearExisting
    ) {
        try {
            JSONArray array = new JSONArray(jsonString);
            if (clearExisting) repository.deleteAll();

            // 按时间戳排序，确保导入后ID顺序与时间顺序一致（新的在前）
            List<EventData> eventsToImport = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);

                String title, detail;
                long eventTime;

                if (obj.has(KEY_TITLE)) {
                    // ── 新格式（中文字段）──
                    title = obj.optString(KEY_TITLE, "");
                    detail = obj.optString(KEY_DETAIL, "");
                    eventTime = parseTime(obj.optString(KEY_TIME, ""));
                } else {
                    // ── 旧格式（英文字段）──
                    title = obj.optString("title", "");
                    detail = obj.optString("description", "");
                    eventTime = obj.optLong("eventTime", System.currentTimeMillis());
                }

                eventsToImport.add(new EventData(title, detail, eventTime));
            }

            // 按时间升序排序（最旧的在前），这样导入时ID会从1开始递增，且最大的ID对应最新的时间
            Collections.sort(eventsToImport, new Comparator<EventData>() {
                @Override
                public int compare(EventData o1, EventData o2) {
                    return Long.compare(o1.eventTime, o2.eventTime);
                }
            });

            // 按排序后的顺序插入，ID会自动从1开始递增
            for (EventData data : eventsToImport) {
                Event event = new Event(data.title, data.detail, data.eventTime);
                repository.insert(event);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 解析时间字符串，支持：
     *  - "yyyy-MM-dd HH:mm"（全角或半角冒号/空格均可）
     *  - 毫秒时间戳字符串
     */
    private long parseTime(String raw) {
        if (raw == null || raw.trim().isEmpty()) return System.currentTimeMillis();
        // 将全角冒号 "：" 替换为半角 ":"，全角空格替换为半角空格
        String normalized = raw.trim()
                .replace('：', ':')
                .replace('\u3000', ' ');
        try {
            java.util.Date date = DATE_FORMAT.parse(normalized);
            if (date != null) {
                return date.getTime();
            }
            return System.currentTimeMillis();
        } catch (ParseException e) {
            // 尝试当作毫秒时间戳
            try {
                return Long.parseLong(normalized);
            } catch (NumberFormatException ex) {
                return System.currentTimeMillis();
            }
        }
    }

    /**
     * 从用户通过 SAF 选择的文件 URI 导入数据。
     */
    public boolean importDataFromUri(
            EventRepository repository,
            Uri uri,
            boolean clearExisting
    ) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream != null) {
                try {
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (sb.length() > 0) sb.append('\n');
                        sb.append(line);
                    }
                    reader.close();
                    return parseAndSaveJsonData(sb.toString(), repository, clearExisting);
                } finally {
                    inputStream.close();
                }
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 从应用私有目录导入数据。
     */
    public boolean importData(EventRepository repository, boolean clearExisting) {
        try {
            InputStream inputStream = context.openFileInput(FILE_NAME);
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                if (sb.length() > 0) sb.append('\n');
                sb.append(line);
            }
            reader.close();
            return parseAndSaveJsonData(sb.toString(), repository, clearExisting);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 检查是否存在备份数据。
     */
    public boolean hasBackupData() {
        try {
            java.io.File file = context.getFileStreamPath(FILE_NAME);
            return file.exists() && file.length() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 内部数据载体，用于存储解析后的导入数据。
     */
    private static class EventData {
        final String title;
        final String detail;
        final long eventTime;

        EventData(String title, String detail, long eventTime) {
            this.title = title;
            this.detail = detail;
            this.eventTime = eventTime;
        }
    }
}
