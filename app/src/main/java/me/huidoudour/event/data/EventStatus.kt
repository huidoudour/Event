package me.huidoudour.event.data

/**
 * 事件处理方式（状态）。
 *
 * 取代旧版的 isDone 布尔标记，让事件除了「已完成」之外还能标记为
 * 他人完成、未计划、不相干等处理方式。每个事件同一时间只有一种状态，
 * [NONE] 表示尚未标记。
 *
 * 数据库与导入导出文件都使用枚举名（大写英文）持久化，避免随界面语言变化而失效。
 */
enum class EventStatus {
    /** 未标记（默认状态） */
    NONE,

    /** 已完成 */
    DONE,

    /** 他人完成：事情已经了结，但不是自己做的 */
    DONE_BY_OTHERS,

    /** 未计划：暂时没有计划处理 */
    UNPLANNED,

    /** 不相干：与当前关注的事情无关 */
    IRRELEVANT;

    companion object {
        /** 从持久化值还原状态；未知或空值回退到 [NONE]，保证旧数据/新版本数据都能读取 */
        fun fromStorage(value: String?): EventStatus =
            entries.firstOrNull { it.name == value?.trim() } ?: NONE
    }
}

/**
 * 处理方式的一次历史记录：被替换下来的旧状态及其设置时间。
 *
 * 事件状态每变更一次就向 [Event.statusHistory] 追加一条，详情页以删除线展示，
 * 让用户能看到「曾经标记为已完成、随后又被取消」这类变化轨迹。
 */
data class StatusChange(
    val status: EventStatus,
    val at: Long
)
