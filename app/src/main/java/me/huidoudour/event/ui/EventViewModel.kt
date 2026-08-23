package me.huidoudour.event.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import me.huidoudour.event.data.Event
import me.huidoudour.event.data.EventDatabase
import me.huidoudour.event.data.EventRepository

class EventViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: EventRepository
    val allEvents: LiveData<List<Event>>
    // RxJava 统一管理异步任务，onCleared 时自动释放
    private val disposables = CompositeDisposable()

    init {
        val database = EventDatabase.getDatabase(application)
        val eventDao = database.eventDao()
        repository = EventRepository(application, eventDao)
        allEvents = repository.allEvents
    }

    fun getSortedEvents(): LiveData<List<Event>> = repository.getSortedEvents()

    @Suppress("unused")
    fun isAscending(): Boolean = repository.isAscending

    fun addEvent(title: String, description: String?, eventTime: Long) {
        runOnIo {
            val event = Event(title = title, description = description, eventTime = eventTime)
            repository.insert(event)
        }
    }

    fun updateEvent(event: Event) {
        runOnIo { repository.update(event) }
    }

    fun deleteEvent(event: Event) {
        runOnIo { repository.delete(event) }
    }

    fun deleteAllEvents() {
        runOnIo { repository.deleteAll() }
    }

    fun deleteEventsByIds(ids: List<Long>) {
        runOnIo { repository.deleteByIds(ids) }
    }

    fun getRepository(): EventRepository = repository

    /** 在 io 线程池执行数据库写操作（替代手写单线程 Executor，线程池可复用） */
    private fun runOnIo(action: () -> Unit) {
        disposables.add(
            Completable.fromAction(action)
                .subscribeOn(Schedulers.io())
                .subscribe(
                    {},
                    { e -> Log.e(TAG, "database operation failed", e) }
                )
        )
    }

    override fun onCleared() {
        disposables.dispose()
        super.onCleared()
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(EventViewModel::class.java)) {
                return EventViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

    companion object {
        private const val TAG = "EventViewModel"
    }
}
