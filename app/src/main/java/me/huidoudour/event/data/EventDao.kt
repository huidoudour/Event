package me.huidoudour.event.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface EventDao {
    @Query("SELECT * FROM events ORDER BY updatedAt DESC")
    fun getAllEvents(): LiveData<List<Event>>

    /** 同步查询所有事件（用于导出，必须在后台线程调用） */
    @Query("SELECT * FROM events ORDER BY updatedAt DESC")
    fun getAllEventsSync(): List<Event>

    @Query("SELECT * FROM events ORDER BY eventTime ASC, id ASC")
    fun getEventsByTimeAscending(): LiveData<List<Event>>

    @Query("SELECT * FROM events ORDER BY eventTime DESC, id DESC")
    fun getEventsByTimeDescending(): LiveData<List<Event>>

    /** 按关键词搜索（标题或描述包含），按时间正序 */
    @Query(
        "SELECT * FROM events WHERE title LIKE '%' || :query || '%' ESCAPE '\\' " +
            "OR description LIKE '%' || :query || '%' ESCAPE '\\' " +
            "ORDER BY eventTime ASC, id ASC"
    )
    fun searchEventsByTimeAscending(query: String): LiveData<List<Event>>

    /** 按关键词搜索（标题或描述包含），按时间倒序 */
    @Query(
        "SELECT * FROM events WHERE title LIKE '%' || :query || '%' ESCAPE '\\' " +
            "OR description LIKE '%' || :query || '%' ESCAPE '\\' " +
            "ORDER BY eventTime DESC, id DESC"
    )
    fun searchEventsByTimeDescending(query: String): LiveData<List<Event>>

    @Insert
    fun insert(event: Event): Long

    /** 批量插入（用于导入等场景），Room 会在单个事务中完成 */
    @Insert
    fun insertAll(events: List<Event>)

    @Update
    fun update(event: Event)

    @Delete
    fun delete(event: Event)

    @Query("DELETE FROM events")
    fun deleteAll()

    /** 重置自增ID计数器（清空数据后调用） */
    @Query("DELETE FROM sqlite_sequence WHERE name='events'")
    fun resetAutoIncrement()

    @Query("DELETE FROM events WHERE id IN (:ids)")
    fun deleteByIds(ids: List<Long>)
}
