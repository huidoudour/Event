package me.huidoudour.event.debug;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface DebugLogDao {

    @Query("SELECT * FROM debug_logs ORDER BY timestamp DESC")
    LiveData<List<DebugLogEntry>> getAllLogs();

    @Query("SELECT * FROM debug_logs ORDER BY timestamp DESC")
    List<DebugLogEntry> getAllLogsSync();

    @Insert
    void insert(DebugLogEntry entry);

    @Query("DELETE FROM debug_logs")
    void deleteAll();

    @Query("SELECT COUNT(*) FROM debug_logs")
    LiveData<Integer> getLogCount();
}
