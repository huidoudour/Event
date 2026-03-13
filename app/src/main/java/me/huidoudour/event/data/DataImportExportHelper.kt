package me.huidoudour.event.data

import android.content.Context
import android.net.Uri
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.*

/**
 * 数据导入导出助手类
 * 负责将 Event 数据转换为 JSON 格式以及从 JSON 恢复
 */
class DataImportExportHelper(private val context: Context) {
    
    companion object {
        private const val FILE_NAME = "events_backup.json"
        private const val MIME_TYPE = "application/json"
    }
    
    /**
     * 导出数据为 JSON 格式
     * @param repository EventRepository 实例
     * @return 如果导出成功返回 true，否则返回 false
     */
    fun exportData(repository: EventRepository): Boolean {
        return try {
            val events = repository.allEvents.value ?: emptyList()
            
            if (events.isEmpty()) {
                return false // 没有数据可导出
            }
            
            val jsonArray = JSONArray()
            
            for (event in events) {
                val jsonObject = JSONObject().apply {
                    put("id", event.id)
                    put("title", event.title)
                    put("description", event.description ?: "")
                    put("eventTime", event.eventTime)
                    put("createdAt", event.createdAt)
                }
                jsonArray.put(jsonObject)
            }
            
            // 保存到 app 私有目录
            val jsonString = jsonArray.toString(2) // 格式化 JSON
            context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE).use { outputStream ->
                outputStream.write(jsonString.toByteArray())
            }
            
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * 导出数据到用户选择的文件
     * @param repository EventRepository 实例
     * @param uri 目标文件的 URI
     * @return 如果导出成功返回 true，否则返回 false
     */
    fun exportDataToUri(repository: EventRepository, uri: Uri): Boolean {
        return try {
            val events = repository.allEvents.value ?: emptyList()
            
            if (events.isEmpty()) {
                return false // 没有数据可导出
            }
            
            val jsonArray = JSONArray()
            
            for (event in events) {
                val jsonObject = JSONObject().apply {
                    put("id", event.id)
                    put("title", event.title)
                    put("description", event.description ?: "")
                    put("eventTime", event.eventTime)
                    put("createdAt", event.createdAt)
                }
                jsonArray.put(jsonObject)
            }
            
            // 写入到指定 URI
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                val jsonString = jsonArray.toString(2)
                outputStream.write(jsonString.toByteArray())
            }
            
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * 从 JSON 文件导入数据
     * @param repository EventRepository 实例
     * @param clearExisting 是否清空现有数据
     * @return 如果导入成功返回 true，否则返回 false
     */
    fun importData(repository: EventRepository, clearExisting: Boolean = true): Boolean {
        return try {
            // 从 app 私有目录读取
            val fileInputStream = context.openFileInput(FILE_NAME)
            val jsonString = fileInputStream.bufferedReader().use { it.readText() }
            
            parseAndSaveJsonData(jsonString, repository, clearExisting)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * 从指定的 URI 导入数据
     * @param repository EventRepository 实例
     * @param uri 源文件的 URI
     * @param clearExisting 是否清空现有数据
     * @return 如果导入成功返回 true，否则返回 false
     */
    fun importDataFromUri(repository: EventRepository, uri: Uri, clearExisting: Boolean = true): Boolean {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val jsonString = inputStream.bufferedReader().use { it.readText() }
                parseAndSaveJsonData(jsonString, repository, clearExisting)
            } ?: false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * 解析 JSON 数据并保存到数据库
     */
    private fun parseAndSaveJsonData(
        jsonString: String,
        repository: EventRepository,
        clearExisting: Boolean
    ): Boolean {
        return try {
            val jsonArray = JSONArray(jsonString)
            
            // 如果需要清空现有数据
            if (clearExisting) {
                repository.deleteAll()
            }
            
            // 解析并保存每个事件
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                
                val event = Event(
                    jsonObject.getString("title"),
                    jsonObject.getString("description"),
                    jsonObject.getLong("eventTime")
                ).apply {
                    id = jsonObject.getLong("id")
                    createdAt = jsonObject.getLong("createdAt")
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
     * 检查是否有可导入的数据
     */
    fun hasBackupData(): Boolean {
        return try {
            val file = context.getFileStreamPath(FILE_NAME)
            file.exists() && file.length() > 0
        } catch (e: Exception) {
            false
        }
    }
}
