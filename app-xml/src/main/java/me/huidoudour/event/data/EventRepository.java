package me.huidoudour.event.data;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import java.util.List;

public class EventRepository {
    private static final String PREFS_NAME = "sort_prefs";
    private static final String KEY_SORT_ASCENDING = "sort_ascending";
    
    private final EventDao eventDao;
    private final SharedPreferences preferences;
    public final LiveData<List<Event>> allEvents;
    private final MediatorLiveData<List<Event>> sortedEvents = new MediatorLiveData<>();
    private boolean isAscending;
    private LiveData<List<Event>> currentSource;

    public EventRepository(Context context, EventDao eventDao) {
        this.eventDao = eventDao;
        this.preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.allEvents = eventDao.getAllEvents();
        // 从SharedPreferences读取排序状态，默认为倒序
        this.isAscending = preferences.getBoolean(KEY_SORT_ASCENDING, false);
        setSortOrder(isAscending);
    }

    private void setSortOrder(boolean ascending) {
        LiveData<List<Event>> newSource = ascending ? 
            eventDao.getEventsByTimeAscending() : 
            eventDao.getEventsByTimeDescending();
        
        if (currentSource != null) {
            sortedEvents.removeSource(currentSource);
        }
        currentSource = newSource;
        sortedEvents.addSource(newSource, sortedEvents::postValue);
    }

    public long insert(Event event) {
        return eventDao.insert(event);
    }

    public void update(Event event) {
        // 只在内容变化时更新时间戳
        event.setUpdatedAt(System.currentTimeMillis());
        eventDao.update(event);
    }

    public void delete(Event event) {
        eventDao.delete(event);
    }

    public void deleteAll() {
        eventDao.deleteAll();
        eventDao.resetAutoIncrement();
    }

    public void deleteByIds(java.util.List<Long> ids) {
        eventDao.deleteByIds(ids);
    }

    /** 同步查询所有事件，必须在后台线程调用 */
    public List<Event> getAllEventsSync() {
        return eventDao.getAllEventsSync();
    }

    public LiveData<List<Event>> getSortedEvents() {
        return sortedEvents;
    }

    public boolean isAscending() {
        return isAscending;
    }

    public void toggleSortOrder() {
        isAscending = !isAscending;
        // 保存排序状态到SharedPreferences
        preferences.edit().putBoolean(KEY_SORT_ASCENDING, isAscending).apply();
        setSortOrder(isAscending);
    }
    
    /** 同步排序状态（从SharedPreferences读取） */
    public void syncSortOrder() {
        boolean savedAscending = preferences.getBoolean(KEY_SORT_ASCENDING, false);
        if (savedAscending != isAscending) {
            isAscending = savedAscending;
            setSortOrder(isAscending);
        }
    }
}
