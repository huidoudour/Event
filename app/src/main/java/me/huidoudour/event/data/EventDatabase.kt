package me.huidoudour.event.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import io.requery.android.database.sqlite.RequerySQLiteOpenHelperFactory

@Database(entities = [Event::class], version = 7, exportSchema = false)
@TypeConverters(EventConverters::class)
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

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // v3 仅为 Compose 迁移时的版本号提升，表结构未变化，
                // 补齐该空迁移以保证更早安装的 v2 数据库可以逐级升级到新版本
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 添加完成标记列：旧数据默认未完成，完成时间默认为 NULL
                db.execSQL("ALTER TABLE events ADD COLUMN isDone INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE events ADD COLUMN doneAt INTEGER")
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 添加修改记录列：逗号分隔的毫秒时间戳，旧数据默认为空串
                db.execSQL("ALTER TABLE events ADD COLUMN editHistory TEXT NOT NULL DEFAULT ''")
            }
        }

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 完成标记升级为多种处理方式：新增 status（枚举名）与 statusAt（设置时间）
                db.execSQL("ALTER TABLE events ADD COLUMN status TEXT NOT NULL DEFAULT 'NONE'")
                db.execSQL("ALTER TABLE events ADD COLUMN statusAt INTEGER")
                // 旧版已完成的事件迁移为「已完成」，并保留原完成时间
                db.execSQL("UPDATE events SET status = 'DONE' WHERE isDone = 1")
                db.execSQL("UPDATE events SET statusAt = doneAt WHERE isDone = 1")
                // 旧列已由 status/statusAt 取代，删除以保证 Room 的实体结构校验通过
                db.execSQL("ALTER TABLE events DROP COLUMN isDone")
                db.execSQL("ALTER TABLE events DROP COLUMN doneAt")
            }
        }

        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 添加状态变更记录列：逗号分隔的“状态名:时间戳”，旧数据默认为空串
                db.execSQL("ALTER TABLE events ADD COLUMN statusHistory TEXT NOT NULL DEFAULT ''")
            }
        }

        fun getDatabase(context: Context): EventDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    EventDatabase::class.java,
                    "event_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7)
                    .fallbackToDestructiveMigration(false)
                    .openHelperFactory(RequerySQLiteOpenHelperFactory())
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
