package me.huidoudour.event.data

import androidx.room.TypeConverter

/**
 * Room 类型转换器：修改记录列表与 TEXT 列互转。
 * 数据库中以逗号分隔的毫秒时间戳保存，空串表示没有修改记录。
 *
 * 同时负责 [EventStatus] 与 TEXT 列的互转，库内保存稳定的枚举名。
 */
class EventConverters {
    @TypeConverter
    fun fromEditHistory(value: List<Long>?): String =
        value?.joinToString(separator = ",").orEmpty()

    @TypeConverter
    fun toEditHistory(value: String?): List<Long> =
        value?.split(',')
            ?.mapNotNull { it.trim().toLongOrNull() }
            .orEmpty()

    @TypeConverter
    fun fromEventStatus(status: EventStatus?): String =
        (status ?: EventStatus.NONE).name

    @TypeConverter
    fun toEventStatus(value: String?): EventStatus =
        EventStatus.fromStorage(value)
}
