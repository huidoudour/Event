package me.huidoudour.event.util;

/**
 * 操作行为监控桥接类。
 *
 * - Release 构建：空实现（无操作）
 * - Debug 构建：通过 {@link #setMonitor(Monitor)} 注入真正的日志写入器，
 *   记录用户的创建、修改、删除、导入导出、界面交互等所有操作。
 *
 * 此桥接层存在的必要性：main 源集无法直接引用 debug 源集的类，
 * 但日志调用点又必须写在 main 的 UI 代码中。
 */
public class ActionMonitor {

    public interface Monitor {
        void onAction(String action, String detail, long entityId);
    }

    private static Monitor monitor;

    /**
     * 由 debug 源集在应用启动时调用，注入真正的监控实现。
     */
    public static void setMonitor(Monitor impl) {
        monitor = impl;
    }

    /**
     * 记录一条操作行为。
     *
     * @param action   操作类型（如 "CREATE"、"BTN_CLICK"、"DIALOG_OPEN" 等）
     * @param detail   操作详情描述
     * @param entityId 操作涉及的事件 ID（没有则传 0）
     */
    public static void log(String action, String detail, long entityId) {
        if (monitor != null) {
            monitor.onAction(action, detail, entityId);
        }
    }
}
