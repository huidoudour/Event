package me.huidoudour.event.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import me.huidoudour.event.data.Event
import me.huidoudour.event.data.EventDatabase
import me.huidoudour.event.data.EventRepository
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class EventViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: EventRepository
    val allEvents: LiveData<List<Event>>
    private val executor: ExecutorService = Executors.newSingleThreadExecutor()

    init {
        val database = EventDatabase.getDatabase(application)
        val eventDao = database.eventDao()
        repository = EventRepository(application, eventDao)
        allEvents = repository.allEvents
    }

    fun getSortedEvents(): LiveData<List<Event>> = repository.getSortedEvents()

    fun toggleSortOrder() = repository.toggleSortOrder()

    fun isAscending(): Boolean = repository.isAscending

    fun addEvent(title: String, description: String?, eventTime: Long) {
        executor.execute {
            val event = Event(title = title, description = description, eventTime = eventTime)
            repository.insert(event)
        }
    }

    fun updateEvent(event: Event) {
        executor.execute {
            repository.update(event)
        }
    }

    fun deleteEvent(event: Event) {
        executor.execute {
            repository.delete(event)
        }
    }

    fun deleteAllEvents() {
        executor.execute {
            repository.deleteAll()
        }
    }

    fun deleteEventsByIds(ids: List<Long>) {
        executor.execute {
            repository.deleteByIds(ids)
        }
    }

    fun getRepository(): EventRepository = repository

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(EventViewModel::class.java)) {
                return EventViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }

        fun createRepository(): EventRepository {
            val database = EventDatabase.getDatabase(application)
            return EventRepository(application, database.eventDao())
        }
    }
}
