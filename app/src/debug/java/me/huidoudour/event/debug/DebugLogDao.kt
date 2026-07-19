package me.huidoudour.event.debug

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface DebugLogDao {
    @Query("SELECT * FROM debug_logs ORDER BY timestamp DESC")
    fun getAllLogs(): LiveData<List<DebugLogEntry>>

    @Query("SELECT * FROM debug_logs ORDER BY timestamp DESC")
    fun getAllLogsSync(): List<DebugLogEntry>

    @Insert
    fun insert(entry: DebugLogEntry)

    @Query("DELETE FROM debug_logs")
    fun deleteAll()

    @Query("SELECT COUNT(*) FROM debug_logs")
    fun getLogCount(): LiveData<Int>
}
