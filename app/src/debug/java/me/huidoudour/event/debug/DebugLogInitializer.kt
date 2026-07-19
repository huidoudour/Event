package me.huidoudour.event.debug

import android.content.Context
import me.huidoudour.event.util.ActionMonitor
import java.util.concurrent.Executors

/**
 * Debug 日志初始化器。
 * 在 Debug 构建的应用启动时调用 [init] 注入真正的日志写入器。
 */
object DebugLogInitializer {
    private var initialized = false

    @JvmStatic
    fun init(context: Context) {
        if (initialized) return
        initialized = true

        val appContext = context.applicationContext
        val db = DebugLogDatabase.getDatabase(appContext)
        val dao = db.debugLogDao()
        val executor = Executors.newSingleThreadExecutor()

        // 注入真正写入 Room 数据库的监控实现
        ActionMonitor.setMonitor { operation, detail, entityId ->
            executor.execute {
                val entry = DebugLogEntry(
                    timestamp = System.currentTimeMillis(),
                    operation = operation,
                    detail = detail,
                    entityId = entityId
                )
                dao.insert(entry)
            }
        }
    }
}
