package com.shuaiqiu.fuckets100.feature.home

data class HomeRuntimeStatus(
    val cloudLoggedIn: Boolean,
    val etsAppInfo: Pair<Boolean, String>? = null,
)
