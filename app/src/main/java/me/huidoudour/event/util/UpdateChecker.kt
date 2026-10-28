package me.huidoudour.event.util

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import java.net.HttpURLConnection
import java.net.URL

/**
 * 轻量“检测新版本”：查询 GitHub Releases 最新正式版，与本地基础版本号比较。
 * 仅做提示与跳转，不下载、不安装。使用内置 [HttpURLConnection]，不引入额外网络库。
 */
object UpdateChecker {

    /**
     * 走 GitHub 网页版 releases/latest（会 302 跳转到 /releases/tag/<版本>）而非 api.github.com，
     * 规避未认证 API 的 60 次/小时限流（403）。
     */
    private const val LATEST_RELEASE_PAGE =
        "https://github.com/huidoudour/Event/releases/latest"

    const val RELEASES_PAGE_URL = LATEST_RELEASE_PAGE

    /** 与 debug/release 变体清单中的 meta-data 对应的键 */
    private const val KEY_IS_RELEASE = "isRelease"

    data class UpdateInfo(
        val latestVersion: String,
        val releaseUrl: String
    )

    /** 检测结果：区分“有新版 / 已最新 / 失败”，供调用方区分提示 */
    sealed interface UpdateCheckResult {
        data class NewVersion(val info: UpdateInfo) : UpdateCheckResult
        data object UpToDate : UpdateCheckResult
        data class Failed(val reason: String) : UpdateCheckResult
    }

    /**
     * 是否允许检查更新：由 debug/release 变体清单中的 isRelease 决定（未配置视为不允许），
     * 并额外要求应用本身未被标记为 debuggable，避免调试包用自身版本号与正式版比较。
     */
    fun isEnabled(context: Context): Boolean {
        val appInfo = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getApplicationInfo(
                    context.packageName,
                    PackageManager.ApplicationInfoFlags.of(PackageManager.GET_META_DATA.toLong())
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getApplicationInfo(
                    context.packageName,
                    PackageManager.GET_META_DATA
                )
            }
        } catch (_: PackageManager.NameNotFoundException) {
            return false
        }
        val isRelease = when (val value = appInfo.metaData?.get(KEY_IS_RELEASE)) {
            is Boolean -> value
            is String -> value.toBoolean()
            is Int -> value != 0
            else -> false
        }
        return isRelease && (appInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) == 0
    }

    /**
     * 同步发起检测（调用方需自行置于 IO 线程）。
     *
     * @param localBaseVersion 本地基础版本号，如 "1.7-beta1"（对应构建时注入的 base_version_name 资源）
     */
    fun check(localBaseVersion: String): UpdateCheckResult {
        var conn: HttpURLConnection? = null
        return try {
            conn = (URL(LATEST_RELEASE_PAGE).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 10_000
                readTimeout = 10_000
                // 不自动跟随重定向，手动捕获 302 的 Location 解析 /releases/tag/<版本>
                instanceFollowRedirects = false
                setRequestProperty("User-Agent", "Event-UpdateCheck")
            }
            val code = conn.responseCode
            if (code == HttpURLConnection.HTTP_NOT_FOUND) {
                // 尚无任何已发布的正式版
                return UpdateCheckResult.Failed("HTTP 404 - no published release")
            }
            val location = conn.getHeaderField("Location")
            if (code in 300..399 && location != null) {
                // releases/latest 会排除草稿/预发布，Location 指向最新正式版
                val tag = location.substringAfterLast("/tag/", "")
                    .substringBefore('?')
                    .removePrefix("v")
                if (tag.isEmpty()) {
                    return UpdateCheckResult.Failed("no tag in redirect: $location")
                }
                compareTag(localBaseVersion, tag)
            } else {
                UpdateCheckResult.Failed("HTTP $code")
            }
        } catch (e: Exception) {
            UpdateCheckResult.Failed(e.message ?: e::class.java.simpleName)
        } finally {
            conn?.disconnect()
        }
    }

    /** 统一比较：远端 tag 大于本地版本则新版，否则已最新 */
    private fun compareTag(localBaseVersion: String, tag: String): UpdateCheckResult {
        val info = UpdateInfo(latestVersion = tag, releaseUrl = RELEASES_PAGE_URL)
        return if (compareVersions(tag, localBaseVersion) > 0) {
            UpdateCheckResult.NewVersion(info)
        } else {
            UpdateCheckResult.UpToDate
        }
    }

    /**
     * 按点分数字段逐位比较：a > b 返回正数，a < b 返回负数，相等返回 0。
     * 非数字字符（如预发布后缀）被忽略，缺失段按 0 处理。
     */
    private fun compareVersions(a: String, b: String): Int {
        val partsA = a.split('.')
        val partsB = b.split('.')
        val size = maxOf(partsA.size, partsB.size)
        for (i in 0 until size) {
            val numA = partsA.getOrNull(i)?.takeWhile { it.isDigit() }?.toIntOrNull() ?: 0
            val numB = partsB.getOrNull(i)?.takeWhile { it.isDigit() }?.toIntOrNull() ?: 0
            if (numA != numB) return numA - numB
        }
        return 0
    }
}
