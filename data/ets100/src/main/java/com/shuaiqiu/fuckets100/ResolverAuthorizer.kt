package com.shuaiqiu.fuckets100

import android.content.Context
import android.util.Log
import com.shuaiqiu.fuckets100.core.common.AuthCacheInvalidator

/**
 * 解析器本地授权初始化（本项目已移除作者后端与 FE 账户）。
 *
 * 只做本地配置：写入 base_url / software_key / feature_key / 公钥 / 版本号 +
 * 本地占位令牌，然后 setAuthEnabled(true)。不会发起任何网络请求；
 * libresolver.so 已打补丁跳过服务端 grant 校验，因此本地读取与云端解析都可直接使用。
 *
 * 幂等、线程安全；重复调用无副作用。
 */
object ResolverAuthorizer : AuthCacheInvalidator {

    const val BASE_URL = "https://api.lastudio.cc"
    const val SOFTWARE_KEY = "fe"
    const val FEATURE_KEY = "answer_parser"

    // 生产后端 Ed25519 公钥 (urlsafe base64 no-pad)。换钥时需同步更新。
    const val PUBLIC_KEY_B64 = "8i06qnGdq6lm6wj8NDm22Nh0_nP6_k38Kg_Lz0xHIiY"

    private const val TAG = "ResolverAuthorizer"

    @Volatile private var localNativeConfigured = false

    /**
     * 解析器离线本地授权初始化（本项目已移除作者后端）。
     *
     * 只做本地配置：写入 base_url / software_key / feature_key / 公钥 / 版本号 +
     * 本地占位令牌，然后 setAuthEnabled(true)。不会发起任何网络请求；
     * libresolver.so 已打补丁跳过服务端 grant 校验，因此本地读取与云端解析都可直接使用。
     *
     * 幂等、线程安全；重复调用无副作用。
     */
    fun ensureLocalNativeConfigured(context: Context) {
        if (localNativeConfigured) return
        synchronized(this) {
            if (localNativeConfigured) return
            val versionCode = runCatching {
                val info = context.packageManager.getPackageInfo(context.packageName, 0)
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                    info.longVersionCode
                } else {
                    @Suppress("DEPRECATION")
                    info.versionCode.toLong()
                }
            }.getOrDefault(0L)
            runCatching {
                ResolverNative.setIntegrityCheckEnabled(false)
                val rc = ResolverNative.setAuthConfig(
                    baseUrl = BASE_URL,
                    softwareKey = SOFTWARE_KEY,
                    featureKey = FEATURE_KEY,
                    publicKeyB64 = PUBLIC_KEY_B64,
                    appVersionCode = versionCode,
                )
                if (rc != 0) {
                    Log.w(TAG, "setAuthConfig 失败: code=" + rc + "（解析器可能无法工作）")
                    return@synchronized
                }
                // 本地占位令牌：只满足解析器的非空校验，不会上传到任何服务器。
                ResolverNative.setAccessToken("offline-local-read-only")
                ResolverNative.setAuthEnabled(true)
                localNativeConfigured = true
                Log.i(TAG, "解析器本地授权已初始化（离线模式，versionCode=" + versionCode + "）")
            }.onFailure { Log.e(TAG, "解析器本地授权初始化异常", it) }
        }
    }

    /** 诊断信息（用于错误提示/日志，不包含隐私数据）。 */
    fun localAuthStatus(): String = runCatching {
        "authEnabled=" + ResolverNative.isAuthEnabled() + ", configured=" + localNativeConfigured
    }.getOrDefault("authEnabled=unknown")

    override fun invalidateAuthCache() = Unit

    /**
     * 内部授权门控（离线改版：仅确保解析器已在本地完成配置）。
     * @throws ResolverAuthorizationDeniedException 未登录/功能关闭/网络失败等
     */
    suspend fun requireAuthorized(context: Context) {
        ensureLocalNativeConfigured(context)
    }

    /** 配置 SO 共用的后端、签名公钥和 access token，不提前指定实际功能权限。 */
    suspend fun requireConfigured(context: Context) {
        ensureLocalNativeConfigured(context)
    }

    suspend fun requireCloudAuthorized(context: Context) {
        ensureLocalNativeConfigured(context)
    }
}

/** 答案解析功能被后端阻断时抛出。 */
class ResolverAuthorizationDeniedException(message: String) : Exception(message)
