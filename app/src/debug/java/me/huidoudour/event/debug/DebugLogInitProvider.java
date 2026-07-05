package me.huidoudour.event.debug;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

/**
 * 自动初始化调试日志系统的 ContentProvider。
 * 仅在 Debug 构建中存在（在 debug AndroidManifest 中注册），
 * 会在 Application.onCreate() 之前自动调用 onCreate()，
 * 确保 OperationLogRecorder 早早注入真正的日志写入器。
 */
public class DebugLogInitProvider extends ContentProvider {

    @Override
    public boolean onCreate() {
        DebugLogInitializer.init(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return null;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
