package me.huidoudour.event.debug

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri

/**
 * 自动初始化调试日志系统的 ContentProvider。
 * 仅在 Debug 构建中存在（在 debug AndroidManifest 中注册），
 * 会在 Application.onCreate() 之前自动调用 onCreate()，
 * 确保 DebugLogInitializer 早早注入真正的日志写入器。
 */
class DebugLogInitProvider : ContentProvider() {
    override fun onCreate(): Boolean {
        DebugLogInitializer.init(context!!)
        return true
    }

    override fun query(
        uri: Uri, projection: Array<String>?, selection: String?,
        selectionArgs: Array<String>?, sortOrder: String?
    ): Cursor? = null

    override fun getType(uri: Uri): String? = null

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int = 0

    override fun update(
        uri: Uri, values: ContentValues?, selection: String?,
        selectionArgs: Array<String>?
    ): Int = 0
}
