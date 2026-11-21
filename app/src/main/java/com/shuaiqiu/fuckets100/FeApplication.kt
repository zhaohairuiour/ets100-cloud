package com.shuaiqiu.fuckets100

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Fe 应用 Application：只初始化设置与离线解析授权。
 * 本地三种读取模式（Shizuku / Root / 漏洞直读）与 FE 账户已移除，
 * 不再初始化 Shizuku / Sui 运行时，也不再做远程配置、公告、更新检查或法务协议云端同步。
 */
@HiltAndroidApp
class FeApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    companion object {
        private const val TAG = "FeApplication"
    }

    override fun onCreate() {
        super.onCreate()

        cleanupShareCache()

        SettingsManager.init(this)

        // 解析器本地授权初始化：离线启用解析能力（不发网络请求）
        runCatching { ResolverAuthorizer.ensureLocalNativeConfigured(this) }

        Log.d(TAG, "FeApplication onCreate")
    }

    private fun cleanupShareCache() {
        try {
            val cacheDir = cacheDir
            val files = cacheDir.listFiles() ?: return
            var count = 0
            for (file in files) {
                if (file.name.startsWith("Fe_Answer_") && file.name.endsWith(".png")) {
                    if (file.delete()) count++
                }
            }
            if (count > 0) {
                Log.d(TAG, "Cleaned up " + count + " share cache files")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to cleanup share cache", e)
        }
    }
}
