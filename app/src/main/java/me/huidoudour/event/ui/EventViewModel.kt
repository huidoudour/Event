package me.huidoudour.event.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.kotlin.toCompletable
import io.reactivex.rxjava3.schedulers.Schedulers
import me.huidoudour.event.data.Event
import me.huidoudour.event.data.EventDatabase
import me.huidoudour.event.data.EventRepository

class EventViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: EventRepository
    val allEvents: LiveData<List<Event>>
    // RxKotlin / RxJava 统一管理异步任务，onCleared 时自动释放
    private val disposables = CompositeDisposable()

    init {
        val database = EventDatabase.getDatabase(application)
        val eventDao = database.eventDao()
        repository = EventRepository(application, eventDao)
        allEvents = repository.allEvents
    }

    fun getSortedEvents(): LiveData<List<Event>> = repository.getSortedEvents()

    /** 更新搜索关键词，空串/空白则恢复展示全部事件 */
    fun setSearchQuery(query: String) {
        repository.setSearchQuery(query)
    }

    @Suppress("unused")
    fun isAscending(): Boolean = repository.isAscending

    fun addEvent(
        title: String,
        description: String?,
        eventTime: Long,
        createdAt: Long,
        onInserted: (Long) -> Unit
    ) {
        Single.fromCallable {
            Event(
                title = title,
                description = description,
                eventTime = eventTime,
                createdAt = createdAt,
                updatedAt = createdAt
            ).let(repository::insert)
        }
            .subscribeOn(Schedulers.single())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = onInserted,
                onError = { e -> Log.e(TAG, "adding event failed", e) }
            )
            .addTo(disposables)
    }

    fun updateEvent(event: Event) {
        runOnIo { repository.update(event) }
    }

    fun refreshEvents() {
        repository.refresh()
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

    /** 在后台串行调度器执行数据库写操作，保持用户操作的提交顺序。 */
    private fun runOnIo(action: () -> Unit) {
        action.toCompletable()
            // 同一事件被连续编辑时，必须按提交顺序写入；Schedulers.io() 会并发执行，
            // 可能让较早的写入在较晚的写入之后完成并覆盖最新内容。
            .subscribeOn(Schedulers.single())
            .subscribeBy(
                onError = { e -> Log.e(TAG, "database operation failed", e) }
            )
            .addTo(disposables)
    }

    override fun onCleared() {
        disposables.dispose()
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
