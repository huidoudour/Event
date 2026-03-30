package me.huidoudour.event.data

import android.content.Context
import android.net.Uri
import org.json.JSONArray
import org.json.JSONObject
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * 数据导入导出助手类
 * JSON 格式：{ "事件标题": "...", "事件详情": "...", "事件时间": "yyyy-MM-dd HH:mm" }
 */
class DataImportExportHelper(private val context: Context) {

    companion object {
        private const val FILE_NAME = "events_backup.json"
        private const val KEY_TITLE = "事件标题"
        private const val KEY_DETAIL = "事件详情"
        private const val KEY_TIME = "事件时间"
        private val DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 导出
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * 将事件列表序列化为格式化 JSON 字符串。
     * 若列表为空则返回 null。
     */
    private fun buildJsonString(events: List<Event>): String? {
        if (events.isEmpty()) return null
        val array = JSONArray()
        for (event in events) {
            val obj = JSONObject().apply {
                put(KEY_TITLE, event.title ?: "")
                put(KEY_DETAIL, event.description ?: "")
                put(KEY_TIME, DATE_FORMAT.format(java.util.Date(event.eventTime)))
            }
            array.put(obj)
        }
        return array.toString(2)
    }

    /**
     * 导出数据到用户通过 SAF 选择的文件 URI。
     * 必须在后台线程调用（直接读取数据库）。
     */
    fun exportDataToUri(repository: EventRepository, uri: Uri): Boolean {
        return try {
            val events = repository.getAllEventsSync()
            val json = buildJsonString(events) ?: return false
            context.contentResolver.openOutputStream(uri)?.use { os ->
                os.write(json.toByteArray(Charsets.UTF_8))
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * 导出数据到应用私有目录（备用，不直接暴露给 UI）。
     */
    fun exportData(repository: EventRepository): Boolean {
        return try {
            val events = repository.allEvents.value ?: emptyList()
            val json = buildJsonString(events) ?: return false
            context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE).use { os ->
                os.write(json.toByteArray(Charsets.UTF_8))
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 导入
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * 解析 JSON 字符串并写入数据库。
     * 同时兼容旧格式（英文字段 id/title/description/eventTime/createdAt）
     * 和新格式（中文字段 事件标题/事件详情/事件时间）。
     */
    private fun parseAndSaveJsonData(
        jsonString: String,
        repository: EventRepository,
        clearExisting: Boolean
    ): Boolean {
        return try {
            val array = JSONArray(jsonString)
            if (clearExisting) repository.deleteAll()

            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)

                val event: Event = if (obj.has(KEY_TITLE)) {
                    // ── 新格式（中文字段）──
                    val title = obj.optString(KEY_TITLE, "")
                    val detail = obj.optString(KEY_DETAIL, "")
                    val timeStr = obj.optString(KEY_TIME, "")
                    val eventTime = parseTime(timeStr)
                    Event(title, detail, eventTime)
                } else {
                    // ── 旧格式（英文字段）──
                    val title = obj.optString("title", "")
                    val desc = obj.optString("description", "")
                    val eventTime = obj.optLong("eventTime", System.currentTimeMillis())
                    val event = Event(title, desc, eventTime)
                    if (obj.has("id")) event.id = obj.getLong("id")
                    if (obj.has("createdAt")) event.createdAt = obj.getLong("createdAt")
                    event
                }

                repository.insert(event)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * 解析时间字符串，支持：
     *  - "yyyy-MM-dd HH:mm"（全角或半角冒号/空格均可）
     *  - 毫秒时间戳字符串
     */
    private fun parseTime(raw: String): Long {
        if (raw.isBlank()) return System.currentTimeMillis()
        // 将全角冒号 "：" 替换为半角 ":"，全角空格替换为半角空格
        val normalized = raw.trim()
            .replace('：', ':')
            .replace('\u3000', ' ')
        return try {
            DATE_FORMAT.parse(normalized)?.time ?: System.currentTimeMillis()
        } catch (e: ParseException) {
            // 尝试当作毫秒时间戳
            normalized.toLongOrNull() ?: System.currentTimeMillis()
        }
    }

    /**
     * 从用户通过 SAF 选择的文件 URI 导入数据。
     */
    fun importDataFromUri(
        repository: EventRepository,
        uri: Uri,
        clearExisting: Boolean = true
    ): Boolean {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val jsonString = inputStream.bufferedReader(Charsets.UTF_8).readText()
                parseAndSaveJsonData(jsonString, repository, clearExisting)
            } ?: false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * 从应用私有目录导入数据。
     */
    fun importData(repository: EventRepository, clearExisting: Boolean = true): Boolean {
        return try {
            val jsonString = context.openFileInput(FILE_NAME)
                .bufferedReader(Charsets.UTF_8).use { it.readText() }
            parseAndSaveJsonData(jsonString, repository, clearExisting)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun hasBackupData(): Boolean {
        return try {
            val file = context.getFileStreamPath(FILE_NAME)
            file.exists() && file.length() > 0
        } catch (e: Exception) {
            false
        }
    }
}
