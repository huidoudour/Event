package me.huidoudour.event.debug

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [DebugLogEntry::class], version = 2, exportSchema = false)
abstract class DebugLogDatabase : RoomDatabase() {
    abstract fun debugLogDao(): DebugLogDao

    companion object {
        @Volatile
        private var INSTANCE: DebugLogDatabase? = null

        fun getDatabase(context: Context): DebugLogDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    DebugLogDatabase::class.java,
                    "debug_log_database"
                ).fallbackToDestructiveMigration(false).build().also { INSTANCE = it }
            }
        }
    }
}
