package com.shuaiqiu.fuckets100.core.common

/**
 * 认证缓存失效器：当登录状态/授权状态变化时，通知相关缓存失效。
 */
interface AuthCacheInvalidator {
    fun invalidateAuthCache()
}
