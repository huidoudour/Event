package me.huidoudour.event.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import io.requery.android.database.sqlite.RequerySQLiteOpenHelperFactory

@Database(entities = [Event::class], version = 3, exportSchema = false)
abstract class EventDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao

    companion object {
        @Volatile
        private var INSTANCE: EventDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 添加 updatedAt 列
                db.execSQL("ALTER TABLE events ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
                // 将所有现有事件的 updatedAt 设置为当前时间戳
                val currentTime = System.currentTimeMillis()
                db.execSQL("UPDATE events SET updatedAt = $currentTime")
            }
        }

        fun getDatabase(context: Context): EventDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    EventDatabase::class.java,
                    "event_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration(false)
                    .openHelperFactory(RequerySQLiteOpenHelperFactory())
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
