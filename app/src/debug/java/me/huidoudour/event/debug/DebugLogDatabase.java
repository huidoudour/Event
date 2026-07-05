package me.huidoudour.event.debug;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {DebugLogEntry.class}, version = 1, exportSchema = false)
public abstract class DebugLogDatabase extends RoomDatabase {

    public abstract DebugLogDao debugLogDao();

    private static volatile DebugLogDatabase INSTANCE;

    public static DebugLogDatabase getDatabase(Context context) {
        if (INSTANCE == null) {
            synchronized (DebugLogDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            DebugLogDatabase.class,
                            "debug_log_database"
                    ).build();
                }
            }
        }
        return INSTANCE;
    }
}
