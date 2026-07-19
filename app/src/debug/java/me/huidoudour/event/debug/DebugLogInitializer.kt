package me.huidoudour.event.debug;

import android.content.Context;

import me.huidoudour.event.util.ActionMonitor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Debug 日志初始化器。
 * 在 Debug 构建的应用启动时调用 {@link #init(Context)} 注入真正的日志写入器。
 */
public class DebugLogInitializer {

    private static boolean initialized = false;

    public static synchronized void init(Context context) {
        if (initialized) return;
        initialized = true;

        Context appContext = context.getApplicationContext();
        DebugLogDatabase db = DebugLogDatabase.getDatabase(appContext);
        DebugLogDao dao = db.debugLogDao();
        ExecutorService executor = Executors.newSingleThreadExecutor();

        // 注入真正写入 Room 数据库的监控实现
        ActionMonitor.setMonitor((operation, detail, entityId) -> {
            executor.execute(() -> {
                DebugLogEntry entry = new DebugLogEntry(
                        System.currentTimeMillis(),
                        operation,
                        detail,
                        entityId
                );
                dao.insert(entry);
            });
        });
    }
}
