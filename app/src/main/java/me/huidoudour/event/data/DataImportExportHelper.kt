package me.huidoudour.event.data

import android.content.Context
import android.net.Uri
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 数据导入导出助手类
 * JSON 格式：{ "事件标题": "...", "事件详情": "...", "事件时间": "yyyy-MM-dd HH:mm:ss" }
 * 导入时兼容旧版仅到分钟的 "yyyy-MM-dd HH:mm" 格式。
 */
class DataImportExportHelper(private val context: Context) {

    private fun buildJsonString(events: List<Event>): String? {
        if (events.isEmpty()) return null
        val array = JSONArray()
        for ((_, title, description, eventTime, createdAt, updatedAt, isDone, doneAt, editHistory) in events) {
            val obj = JSONObject()
            try {
                obj.put(KEY_TITLE, title)
                obj.put(KEY_DETAIL, description ?: "")
                obj.put(KEY_TIME, DATE_FORMAT.get()!!.format(Date(eventTime)))
                // 保存 createdAt 和 updatedAt 以保持原始时间戳
                obj.put("createdAt", createdAt)
                obj.put("updatedAt", updatedAt)
                // 完成标记；未完成时不写入完成时间，避免空值语义不明确
                obj.put(KEY_DONE, isDone)
                doneAt?.let { obj.put(KEY_DONE_AT, DATE_FORMAT.get()!!.format(Date(it))) }
                // 修改记录：按时间顺序保存每次编辑的毫秒时间戳
                if (editHistory.isNotEmpty()) {
                    obj.put(KEY_EDIT_HISTORY, JSONArray(editHistory))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            array.put(obj)
        }
        return try {
            array.toString(2)
        } catch (_: Exception) {
            null
        }
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
    @Suppress("unused")
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

    /**
     * 解析 JSON 字符串并写入数据库。
     * 同时兼容旧格式（英文字段 id/title/description/eventTime/createdAt）
     * 和新格式（中文字段 事件标题/事件详情/事件时间）。
     * 导入时会清空数据库并重新生成连续的ID。
     */
    private fun parseAndSaveJsonData(
        jsonString: String,
        repository: EventRepository,
        clearExisting: Boolean
    ): Boolean {
        return try {
            val array = JSONArray(jsonString)
            if (clearExisting) repository.deleteAll()

            // 解析为 Event 列表，按时间升序排序后一次批量插入
            // （Room 的 List @Insert 会在单个事务中完成，避免逐条 INSERT 的性能损耗）
            val eventsToImport = mutableListOf<Event>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val title: String
                val detail: String?
                val eventTime: Long
                val isDone: Boolean
                val doneAt: Long?

                if (obj.has(KEY_TITLE)) {
                    // ── 新格式（中文字段）──
                    title = obj.optString(KEY_TITLE, "")
                    detail = obj.optString(KEY_DETAIL, "")
                    eventTime = parseTime(obj.optString(KEY_TIME, ""))
                    isDone = obj.optBoolean(KEY_DONE, false)
                    doneAt = parseOptionalTime(obj, KEY_DONE_AT)
                } else {
                    // ── 旧格式（英文字段）──
                    title = obj.optString("title", "")
                    detail = obj.optString("description", "")
                    eventTime = obj.optLong("eventTime", System.currentTimeMillis())
                    isDone = obj.optBoolean("isDone", false)
                    doneAt = parseOptionalTime(obj, "doneAt")
                }
                // 优先还原备份中的创建/修改时间；旧备份无这些字段时回退到事件时间，
                // 避免「创建于/修改于」在恢复后被改写成导入时刻
                val createdAt = obj.optLong("createdAt", 0L).takeIf { it > 0 } ?: eventTime
                val updatedAt = obj.optLong("updatedAt", 0L).takeIf { it > 0 } ?: createdAt
                // 旧备份没有修改记录列表：若修改时间晚于创建时间，补一条记录保留该信息
                val editHistory = parseEditHistory(obj).ifEmpty {
                    if (updatedAt > createdAt) listOf(updatedAt) else emptyList()
                }
                eventsToImport.add(
                    Event(
                        title = title,
                        description = detail,
                        eventTime = eventTime,
                        createdAt = createdAt,
                        updatedAt = updatedAt,
                        isDone = isDone,
                        doneAt = doneAt,
                        editHistory = editHistory
                    )
                )
            }

            // 按时间升序排序（最旧的在前），ID会自动从1开始递增
            eventsToImport.sortBy { it.eventTime }
            repository.insertAll(eventsToImport)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * 解析时间字符串，支持：
     * - "yyyy-MM-dd HH:mm:ss"（全角或半角冒号/空格均可）
     * - 旧版 "yyyy-MM-dd HH:mm"（秒默认为 00）
     * - 毫秒时间戳字符串
     */
    private fun parseTime(raw: String?): Long {
        if (raw.isNullOrBlank()) return System.currentTimeMillis()
        // 将全角冒号 "：" 替换为半角 ":"，全角空格替换为半角空格
        val normalized = raw.trim()
            .replace('：', ':')
            .replace('\u3000', ' ')
        // 新格式（含秒）优先；旧备份只到分钟，依次回退兼容
        for (format in listOf(DATE_FORMAT.get()!!, LEGACY_DATE_FORMAT.get()!!)) {
            try {
                format.parse(normalized)?.let { return it.time }
            } catch (_: ParseException) {
                // 当前格式不匹配，尝试下一种
            }
        }
        // 尝试当作毫秒时间戳
        return try {
            normalized.toLong()
        } catch (_: NumberFormatException) {
            System.currentTimeMillis()
        }
    }

    /**
     * 解析修改记录数组（毫秒时间戳）；字段缺失或格式不合法时返回空列表。
     */
    private fun parseEditHistory(obj: JSONObject): List<Long> {
        val array = obj.optJSONArray(KEY_EDIT_HISTORY)
            ?: obj.optJSONArray("editHistory")
            ?: return emptyList()
        return (0 until array.length()).mapNotNull { i ->
            array.optLong(i, 0L).takeIf { it > 0 }
        }
    }

    /**
     * 解析可选的完成时间：字段缺失、为 null 或空串时返回 null。
     * 兼容日期字符串（新格式）与毫秒时间戳（旧格式）。
     */
    private fun parseOptionalTime(obj: JSONObject, key: String): Long? {
        if (!obj.has(key) || obj.isNull(key)) return null
        val normalized = obj.optString(key).trim()
            .replace('：', ':')
            .replace('\u3000', ' ')
        if (normalized.isEmpty()) return null
        for (format in listOf(DATE_FORMAT.get()!!, LEGACY_DATE_FORMAT.get()!!)) {
            try {
                format.parse(normalized)?.let { return it.time }
            } catch (_: ParseException) {
                // 当前格式不匹配，尝试下一种
            }
        }
        return normalized.toLongOrNull()?.takeIf { it > 0 }
    }

    /**
     * 从用户通过 SAF 选择的文件 URI 导入数据。
     */
    fun importDataFromUri(
        repository: EventRepository,
        uri: Uri,
        clearExisting: Boolean
    ): Boolean {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val reader = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8))
                val sb = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    if (sb.isNotEmpty()) sb.append('\n')
                    sb.append(line)
                }
                reader.close()
                parseAndSaveJsonData(sb.toString(), repository, clearExisting)
            } ?: false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * 从应用私有目录导入数据。
     */
    @Suppress("unused")
    fun importData(repository: EventRepository, clearExisting: Boolean): Boolean {
        return try {
            context.openFileInput(FILE_NAME).use { inputStream ->
                val reader = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8))
                val sb = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    if (sb.isNotEmpty()) sb.append('\n')
                    sb.append(line)
                }
                reader.close()
                parseAndSaveJsonData(sb.toString(), repository, clearExisting)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * 检查是否存在备份数据。
     */
    @Suppress("unused")
    fun hasBackupData(): Boolean {
        return try {
            val file = context.getFileStreamPath(FILE_NAME)
            file.exists() && file.length() > 0
        } catch (_: Exception) {
            false
        }
    }

    companion object {
        private const val FILE_NAME = "events_backup.json"
        private const val KEY_TITLE = "事件标题"
        private const val KEY_DETAIL = "事件详情"
        private const val KEY_TIME = "事件时间"
        private const val KEY_DONE = "已完成"
        private const val KEY_DONE_AT = "完成时间"
        private const val KEY_EDIT_HISTORY = "修改记录"
        // 数据交换格式固定用 Locale.US，避免随系统语言变化导致导出/导入不一致。
        // 精确到秒，保证备份/恢复与库内毫秒级 eventTime 的秒信息一致。
        // SimpleDateFormat 非线程安全，使用 ThreadLocal 避免并发导入/导出时共享状态。
        private val DATE_FORMAT = ThreadLocal.withInitial {
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        }
        // 旧版备份仅有到分钟的时间，解析时作为回退格式
        private val LEGACY_DATE_FORMAT = ThreadLocal.withInitial {
            SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
        }
    }
}
