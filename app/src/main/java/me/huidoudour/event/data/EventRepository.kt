package me.huidoudour.event.data

import android.content.Context
import androidx.core.content.edit
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData

class EventRepository(context: Context, private val eventDao: EventDao) {
    private val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val allEvents: LiveData<List<Event>> = eventDao.getAllEvents()

    private val sortedEvents = MediatorLiveData<List<Event>>()
    private var currentSource: LiveData<List<Event>>? = null

    /** 当前搜索关键词（已转义 LIKE 通配符），空串表示不搜索 */
    private var searchQuery: String = ""

    var isAscending: Boolean = preferences.getBoolean(KEY_SORT_ASCENDING, false)
        private set

    init {
        reloadSource()
    }

    /**
     * 根据当前排序方向与搜索关键词重建数据源。
     * 有搜索词时切换为搜索结果查询，否则恢复常规排序查询。
     */
    private fun reloadSource() {
        val newSource = when {
            searchQuery.isNotBlank() -> if (isAscending) {
                eventDao.searchEventsByTimeAscending(searchQuery)
            } else {
                eventDao.searchEventsByTimeDescending(searchQuery)
            }
            isAscending -> eventDao.getEventsByTimeAscending()
            else -> eventDao.getEventsByTimeDescending()
        }
        currentSource?.let { sortedEvents.removeSource(it) }
        currentSource = newSource
        sortedEvents.addSource(newSource, sortedEvents::postValue)
    }

    fun insert(event: Event): Long = eventDao.insert(event)

    /** 批量插入（用于导入等场景），Room 会在单个事务中完成 */
    fun insertAll(events: List<Event>) = eventDao.insertAll(events)

    fun update(event: Event) {
        // 只在内容变化时更新时间戳
        event.updatedAt = System.currentTimeMillis()
        eventDao.update(event)
    }

    fun delete(event: Event) = eventDao.delete(event)

    fun deleteAll() {
        eventDao.deleteAll()
        eventDao.resetAutoIncrement()
    }

    fun deleteByIds(ids: List<Long>) = eventDao.deleteByIds(ids)

    /** 同步查询所有事件，必须在后台线程调用 */
    fun getAllEventsSync(): List<Event> = eventDao.getAllEventsSync()

    fun getSortedEvents(): LiveData<List<Event>> = sortedEvents

    fun setSearchQuery(query: String) {
        val normalized = escapeLike(query.trim())
        if (normalized == searchQuery) return
        searchQuery = normalized
        reloadSource()
    }

    @Suppress("unused")
    fun toggleSortOrder() {
        isAscending = !isAscending
        preferences.edit { putBoolean(KEY_SORT_ASCENDING, isAscending) }
        reloadSource()
    }

    /** 同步排序状态（从SharedPreferences读取） */
    fun syncSortOrder() {
        val savedAscending = preferences.getBoolean(KEY_SORT_ASCENDING, false)
        if (savedAscending != isAscending) {
            isAscending = savedAscending
            reloadSource()
        }
    }

    /** 转义 LIKE 通配符（配合 DAO 的 ESCAPE '\\'，用于执行字面关键词匹配） */
    private fun escapeLike(query: String): String =
        query.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_")

    companion object {
        private const val PREFS_NAME = "sort_prefs"
        private const val KEY_SORT_ASCENDING = "sort_ascending"
    }
}
