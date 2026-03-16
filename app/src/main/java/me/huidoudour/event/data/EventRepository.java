package me.huidoudour.event.data;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import java.util.List;

public class EventRepository {
    private final EventDao eventDao;
    public final LiveData<List<Event>> allEvents;
    private final MediatorLiveData<List<Event>> sortedEvents = new MediatorLiveData<>();
    private boolean isAscending = false; // false 表示倒序，true 表示正序
    private LiveData<List<Event>> currentSource;

    public EventRepository(EventDao eventDao) {
        this.eventDao = eventDao;
        this.allEvents = eventDao.getAllEvents();
        // 默认倒序
        setSortOrder(false);
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
        event.setUpdatedAt(System.currentTimeMillis());
        eventDao.update(event);
    }

    public void delete(Event event) {
        eventDao.delete(event);
    }

    public void deleteAll() {
        eventDao.deleteAll();
    }

    public LiveData<List<Event>> getSortedEvents() {
        return sortedEvents;
    }

    public boolean isAscending() {
        return isAscending;
    }

    public void toggleSortOrder() {
        isAscending = !isAscending;
        setSortOrder(isAscending);
    }
}
