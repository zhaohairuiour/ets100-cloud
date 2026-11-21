package com.shuaiqiu.fuckets100.feature.home

import android.content.Context
import android.os.SystemClock
import com.shuaiqiu.fuckets100.ETS100AuthManager
import com.shuaiqiu.fuckets100.core.common.EtsAppVersion
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class HomeRuntimeStatusRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private var cached: HomeRuntimeStatus? = null
    private var lastRefreshTime = 0L

    fun cachedOrNull(force: Boolean): HomeRuntimeStatus? {
        val value = cached ?: return null
        val fresh = SystemClock.elapsedRealtime() - lastRefreshTime < CACHE_TTL_MS
        return value.takeIf { !force && fresh }
    }

    suspend fun load(force: Boolean): HomeRuntimeStatus {
        cachedOrNull(force)?.let { return it }
        return withContext(Dispatchers.IO) {
            HomeRuntimeStatus(
                cloudLoggedIn = ETS100AuthManager.isLoggedIn(context),
                etsAppInfo = loadEtsAppInfo(context),
            ).also(::update)
        }
    }

    fun update(status: HomeRuntimeStatus) {
        cached = status
        lastRefreshTime = SystemClock.elapsedRealtime()
    }

    private fun loadEtsAppInfo(context: Context): Pair<Boolean, String>? = try {
        val packageInfo = context.packageManager.getPackageInfo(EtsAppVersion.PACKAGE_NAME, 0)
        true to (packageInfo.versionName?.trim()?.takeIf { it.isNotEmpty() } ?: "未知")
    } catch (_: android.content.pm.PackageManager.NameNotFoundException) {
        false to ""
    } catch (_: Exception) {
        null
    }

    private companion object {
        const val CACHE_TTL_MS = 30_000L
    }
}
