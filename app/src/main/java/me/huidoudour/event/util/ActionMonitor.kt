package me.huidoudour.event.util

import me.huidoudour.event.util.ActionMonitor.setMonitor


/**
 * 操作行为监控桥接类。
 *
 * - Release 构建：空实现（无操作）
 * - Debug 构建：通过 [setMonitor] 注入真正的日志写入器，
 *   记录用户的创建、修改、删除、导入导出、界面交互等所有操作。
 *
 * 此桥接层存在的必要性：main 源集无法直接引用 debug 源集的类，
 * 但日志调用点又必须写在 main 的 UI 代码中。
 */
object ActionMonitor {
    fun interface Monitor {
        fun onAction(action: String, detail: String, entityId: Long)
    }

    private var monitor: Monitor? = null

    /**
     * 由 debug 源集在应用启动时调用，注入真正的监控实现。
     */
    @JvmStatic
    fun setMonitor(impl: Monitor) {
        monitor = impl
    }

    /**
     * 记录一条操作行为。
     *
     * @param action   操作类型（如 "CREATE"、"BTN_CLICK"、"DIALOG_OPEN" 等）
     * @param detail   操作详情描述
     * @param entityId 操作涉及的事件 ID（没有则传 0）
     */
    @JvmStatic
    fun log(action: String, detail: String, entityId: Long) {
        monitor?.onAction(action, detail, entityId)
    }
}
