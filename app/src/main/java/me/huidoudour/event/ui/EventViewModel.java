package me.huidoudour.event.ui;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import me.huidoudour.event.data.Event;
import me.huidoudour.event.data.EventDao;
import me.huidoudour.event.data.EventDatabase;
import me.huidoudour.event.data.EventRepository;

public class EventViewModel extends AndroidViewModel {
    private final EventRepository repository;
    private final LiveData<List<Event>> allEvents;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public EventViewModel(@NonNull Application application) {
        super(application);
        EventDatabase database = EventDatabase.getDatabase(application);
        EventDao eventDao = database.eventDao();
        repository = new EventRepository(eventDao);
        allEvents = repository.allEvents;
    }

    public LiveData<List<Event>> getAllEvents() {
        return allEvents;
    }

    public void addEvent(String title, String description, long eventTime) {
        executor.execute(() -> {
            Event event = new Event(title, description, eventTime);
            repository.insert(event);
        });
    }

    public void updateEvent(Event event) {
        executor.execute(() -> {
            repository.update(event);
        });
    }

    public void deleteEvent(Event event) {
        executor.execute(() -> {
            repository.delete(event);
        });
    }

    public void deleteAllEvents() {
        executor.execute(() -> {
            repository.deleteAll();
        });
    }
    
    public EventRepository getRepository() {
        return repository;
    }

    public static class Factory implements ViewModelProvider.Factory {
        private final Application application;

        public Factory(Application application) {
            this.application = application;
        }

        @NonNull
        @Override
        @SuppressWarnings("unchecked")
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(EventViewModel.class)) {
                return (T) new EventViewModel(application);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
        
        public EventRepository createRepository() {
            EventDatabase database = EventDatabase.getDatabase(application);
            return new EventRepository(database.eventDao());
        }
    }
}
