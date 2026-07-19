package me.huidoudour.event.debug

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.util.concurrent.Executors

class DebugLogViewModel(application: Application) : AndroidViewModel(application) {

    private val dao: DebugLogDao
    val allLogs: LiveData<List<DebugLogEntry>>
    val logCount: LiveData<Int>
    private val executor = Executors.newSingleThreadExecutor()

    init {
        val db = DebugLogDatabase.getDatabase(application)
        dao = db.debugLogDao()
        allLogs = dao.getAllLogs()
        logCount = dao.getLogCount()
    }

    fun getAllLogsSync(): List<DebugLogEntry> = dao.getAllLogsSync()

    fun deleteAll() = executor.execute { dao.deleteAll() }

    fun refresh() {
        // LiveData 自动更新，无需手动刷新
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DebugLogViewModel::class.java)) {
                return DebugLogViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
