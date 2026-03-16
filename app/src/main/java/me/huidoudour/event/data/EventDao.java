package me.huidoudour.event.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface EventDao {
    @Query("SELECT * FROM events ORDER BY updatedAt DESC")
    LiveData<List<Event>> getAllEvents();

    @Query("SELECT * FROM events ORDER BY eventTime ASC")
    LiveData<List<Event>> getEventsByTimeAscending();

    @Query("SELECT * FROM events ORDER BY eventTime DESC")
    LiveData<List<Event>> getEventsByTimeDescending();

    @Insert
    long insert(Event event);

    @Update
    void update(Event event);

    @Delete
    void delete(Event event);

    @Query("DELETE FROM events")
    void deleteAll();
}
