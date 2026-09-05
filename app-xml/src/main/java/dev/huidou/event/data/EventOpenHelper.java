package dev.huidou.event.data;

import android.content.Context;
import android.database.Cursor;

import org.sqlite.database.sqlite.SQLiteDatabase;
import org.sqlite.database.sqlite.SQLiteOpenHelper;

/**
 * 基于本地 libs/android.aar（org.sqlite）的原生 SQLite 数据库辅助类。
 * 数据库文件名为 event_database，表结构沿用迁移前 Room 的 schema（events 表），
 * 以便打开已存在的数据库时数据兼容。
 */
public class EventOpenHelper extends SQLiteOpenHelper {

    static {
        // 提前加载原生 SQLite 库，避免首次打开数据库时 UnsatisfiedLinkError
        System.loadLibrary("sqliteX");
    }

    private static final String DB_NAME = "event_database";
    private static final int DB_VERSION = 2;

    private static volatile EventOpenHelper INSTANCE;
    private EventDao dao;

    /** 获取数据库单例 */
    public static EventOpenHelper getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (EventOpenHelper.class) {
                if (INSTANCE == null) {
                    INSTANCE = new EventOpenHelper(context.getApplicationContext());
                }
            }
        }
        return INSTANCE;
    }

    private EventOpenHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 与 Room 生成的 events 表结构保持一致
        db.execSQL("CREATE TABLE IF NOT EXISTS events (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "title TEXT, " +
                "description TEXT, " +
                "eventTime INTEGER NOT NULL, " +
                "createdAt INTEGER NOT NULL, " +
                "updatedAt INTEGER NOT NULL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // 添加 updatedAt 列并初始化，保证能正常排序
            db.execSQL("ALTER TABLE events ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0");
            db.execSQL("UPDATE events SET updatedAt = " + System.currentTimeMillis());
        }
    }

    /** 获取手写 SQL 的 DAO（懒加载，使用可写数据库） */
    public EventDao getDao() {
        if (dao == null) {
            dao = new EventDao(getWritableDatabase());
        }
        return dao;
    }

    /**
     * 关闭底层数据库写连接（用于释放持有的 dao 实例）。
     */
    public void closeDao() {
        if (dao != null) {
            dao = null;
        }
    }

    /** 仅供 DAO 在需要时执行原始 SQL（预留） */
    Cursor rawQuery(SQLiteDatabase db, String sql, String[] args) {
        return db.rawQuery(sql, args);
    }
}
