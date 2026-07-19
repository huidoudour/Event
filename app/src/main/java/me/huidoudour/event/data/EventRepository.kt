package me.huidoudour.event.data

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData

class EventRepository(context: Context, private val eventDao: EventDao) {
    private val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val allEvents: LiveData<List<Event>> = eventDao.getAllEvents()

    private val sortedEvents = MediatorLiveData<List<Event>>()
    private var currentSource: LiveData<List<Event>>? = null

    var isAscending: Boolean = preferences.getBoolean(KEY_SORT_ASCENDING, false)
        private set

    init {
        setSortOrder(isAscending)
    }

    private fun setSortOrder(ascending: Boolean) {
        val newSource = if (ascending) {
            eventDao.getEventsByTimeAscending()
        } else {
            eventDao.getEventsByTimeDescending()
        }
        currentSource?.let { sortedEvents.removeSource(it) }
        currentSource = newSource
        sortedEvents.addSource(newSource, sortedEvents::postValue)
    }

    fun insert(event: Event): Long = eventDao.insert(event)

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

    fun toggleSortOrder() {
        isAscending = !isAscending
        preferences.edit().putBoolean(KEY_SORT_ASCENDING, isAscending).apply()
        setSortOrder(isAscending)
    }

    /** 同步排序状态（从SharedPreferences读取） */
    fun syncSortOrder() {
        val savedAscending = preferences.getBoolean(KEY_SORT_ASCENDING, false)
        if (savedAscending != isAscending) {
            isAscending = savedAscending
            setSortOrder(isAscending)
        }
    }

    companion object {
        private const val PREFS_NAME = "sort_prefs"
        private const val KEY_SORT_ASCENDING = "sort_ascending"
    }
}
