package me.huidoudour.event.data;

import androidx.lifecycle.LiveData;
import java.util.List;

public class EventRepository {
    private final EventDao eventDao;
    public final LiveData<List<Event>> allEvents;

    public EventRepository(EventDao eventDao) {
        this.eventDao = eventDao;
        this.allEvents = eventDao.getAllEvents();
    }

    public long insert(Event event) {
        return eventDao.insert(event);
    }

    public void update(Event event) {
        eventDao.update(event);
    }

    public void delete(Event event) {
        eventDao.delete(event);
    }

    public void deleteAll() {
        eventDao.deleteAll();
    }
}
