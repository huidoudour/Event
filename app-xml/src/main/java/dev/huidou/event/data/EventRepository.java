package dev.huidou.event.data;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

/**
 * 事件仓库。
 * 底层已切换为本地 org.sqlite 原生库（手写 SQL），不再依赖 Room。
 * 由于没有 Room 的自动变更通知，这里在每次写操作后手动重新查询并经由 LiveData 刷新。
 */
public class EventRepository {
    private static final String PREFS_NAME = "sort_prefs";
    private static final String KEY_SORT_ASCENDING = "sort_ascending";

    private final EventDao eventDao;
    private final SharedPreferences preferences;

    /** 按更新时间倒序的实时列表（供导出等读取） */
    public final MutableLiveData<List<Event>> allEvents = new MutableLiveData<>();
    /** 按事件时间排序的实时列表（UI 订阅） */
    private final MutableLiveData<List<Event>> sortedEvents = new MutableLiveData<>();

    private boolean isAscending;

    public EventRepository(Context context, EventDao eventDao) {
        this.eventDao = eventDao;
        this.preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        // 从 SharedPreferences 读取排序状态，默认为倒序
        this.isAscending = preferences.getBoolean(KEY_SORT_ASCENDING, false);
        reloadData();
    }

    /** 写操作后重新查询并刷新两个 LiveData */
    private void reloadData() {
        allEvents.postValue(eventDao.queryByUpdatedAtDesc());
        sortedEvents.postValue(eventDao.queryByTime(isAscending));
    }

    public long insert(Event event) {
        long id = eventDao.insert(event);
        reloadData();
        return id;
    }

    public void update(Event event) {
        // 只在内容变化时更新时间戳
        event.setUpdatedAt(System.currentTimeMillis());
        eventDao.update(event);
        reloadData();
    }

    public void delete(Event event) {
        eventDao.delete(event);
        reloadData();
    }

    public void deleteAll() {
        eventDao.deleteAll();
        eventDao.resetAutoIncrement();
        reloadData();
    }

    public void deleteByIds(java.util.List<Long> ids) {
        eventDao.deleteByIds(ids);
        reloadData();
    }

    /** 同步查询所有事件（用于导出，必须在后台线程调用） */
    public List<Event> getAllEventsSync() {
        return eventDao.queryByUpdatedAtDesc();
    }

    public LiveData<List<Event>> getSortedEvents() {
        return sortedEvents;
    }

    public boolean isAscending() {
        return isAscending;
    }

    public void toggleSortOrder() {
        isAscending = !isAscending;
        // 保存排序状态到 SharedPreferences
        preferences.edit().putBoolean(KEY_SORT_ASCENDING, isAscending).apply();
        reloadData();
    }

    /** 同步排序状态（从 SharedPreferences 读取） */
    public void syncSortOrder() {
        boolean savedAscending = preferences.getBoolean(KEY_SORT_ASCENDING, false);
        if (savedAscending != isAscending) {
            isAscending = savedAscending;
            reloadData();
        }
    }
}
