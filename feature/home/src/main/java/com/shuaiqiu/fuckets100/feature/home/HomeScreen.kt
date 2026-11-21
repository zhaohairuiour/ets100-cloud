package com.shuaiqiu.fuckets100.feature.home

import android.content.ClipData
import android.content.ClipboardManager
import android.os.Build
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shuaiqiu.fuckets100.ActivationMode
import com.shuaiqiu.fuckets100.core.designsystem.RighteousFont
import com.shuaiqiu.fuckets100.UpdateStatus
import com.shuaiqiu.fuckets100.core.designsystem.ExpressiveScaffold
import com.shuaiqiu.fuckets100.core.designsystem.expressiveTopAppBarColors
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

/**
 * 首页主屏幕
 * 仅云端模式：显示云端登录状态、设备信息和版本信息
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    mode: ActivationMode,
    refreshRevision: Int = 0,
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToActivation: () -> Unit,
    onNavigateToRemoteContent: (UpdateStatus?) -> Unit,
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val runtimeStatus = uiState.runtimeStatus
    val remoteStatus = uiState.remoteStatus
    val isRefreshing = uiState.isRefreshing
    val pullRefreshState = rememberPullToRefreshState()

    val onRefresh: () -> Unit = viewModel::refresh

    // 返回首页时后台刷新真实状态，但保留旧状态直到检测完成，避免先闪成未激活。
    val lifecycleOwner = LocalLifecycleOwner.current
    var hasPaused by remember { mutableStateOf(false) }
    DisposableEffect(lifecycleOwner, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> hasPaused = true
                Lifecycle.Event.ON_RESUME -> if (hasPaused) viewModel.onResumed()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(mode, refreshRevision, viewModel) {
        viewModel.onRuntimeInputsChanged(mode, refreshRevision)
    }

    // 云端模式激活条件：ETS100 账号已登录
    val isTrulyActivated = runtimeStatus.cloudLoggedIn

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    ExpressiveScaffold(
        topBar = {
            LargeFlexibleTopAppBar(
                title = {
                    Text(
                        text = "Fe",
                        fontFamily = RighteousFont,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.primary,
                    )
                },
                colors = expressiveTopAppBarColors(),
                windowInsets = WindowInsets.safeDrawing.only(
                    WindowInsetsSides.Top + WindowInsetsSides.Horizontal,
                ),
                scrollBehavior = scrollBehavior,
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        contentWindowInsets = WindowInsets.safeDrawing.only(
            WindowInsetsSides.Top + WindowInsetsSides.Horizontal,
        ),
    ) { paddingValues ->
        PullToRefreshBox(
            state = pullRefreshState,
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier.fillMaxSize(),
            indicator = {
                PullToRefreshDefaults.LoadingIndicator(
                    modifier = Modifier
                        .padding(top = paddingValues.calculateTopPadding())
                        .align(Alignment.TopCenter),
                    state = pullRefreshState,
                    isRefreshing = isRefreshing,
                )
            },
        ) {
            Column(
                Modifier
                    .fillMaxSize()
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
                    .verticalScroll(rememberScrollState())
                    .padding(
                        top = paddingValues.calculateTopPadding() + 4.dp,
                        start = 16.dp,
                        end = 16.dp,
                        bottom = paddingValues.calculateBottomPadding() + 24.dp
                    ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatusCard(
                    mode = mode,
                    isTrulyActivated = isTrulyActivated,
                    cloudLoggedIn = runtimeStatus.cloudLoggedIn,
                    etsAppVersion = runtimeStatus.etsAppInfo?.second,
                    onNavigateToActivation = onNavigateToActivation
                )

                // 版本信息（设备、Android、管理器版本、e听说状态）
                HomeVersionInfoSection(
                    context = context,
                    appVersion = uiState.appVersion,
                    etsAppInfo = runtimeStatus.etsAppInfo,
                )

                // 公告
                HomeAnnouncementSection(
                    status = remoteStatus,
                    onNavigateToRemoteContent = onNavigateToRemoteContent,
                )

            }
        }
    }
}

/**
 * 状态卡片组件 - 仅云端模式
 * 本地三种读取模式（Shizuku / Root / 漏洞直读）与 FE 账户已移除。
 */
@Composable
fun StatusCard(
    mode: ActivationMode,
    isTrulyActivated: Boolean,
    cloudLoggedIn: Boolean,
    etsAppVersion: String?,
    onNavigateToActivation: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val statusContainerColor = if (isTrulyActivated) {
        MaterialTheme.colorScheme.secondaryContainer
    } else {
        MaterialTheme.colorScheme.errorContainer
    }
    val animatedContainerColor by animateColorAsState(
        targetValue = statusContainerColor,
        animationSpec = tween(durationMillis = 280),
        label = "homeStatusContainerColor",
    )
    val animatedContentColor by animateColorAsState(
        targetValue = contentColorFor(statusContainerColor),
        animationSpec = tween(durationMillis = 220),
        label = "homeStatusContentColor",
    )
    val statusContentColor = animatedContentColor

    val displayTitle = when {
        cloudLoggedIn -> "云端模式已激活"
        else -> "云端模式未激活"
    }
    val displayIcon = if (isTrulyActivated) Icons.Outlined.CheckCircle else Icons.Outlined.Warning
    val summary = when {
        cloudLoggedIn -> "云端作业与试卷解析服务已连接"
        else -> "需要登录 ETS100 账号，点击前往激活"
    }

    Card(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
            onNavigateToActivation()
        },
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = tween(280)),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = animatedContainerColor,
            contentColor = statusContentColor,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AnimatedContent(
                targetState = displayIcon,
                transitionSpec = {
                    fadeIn(tween(180)) togetherWith fadeOut(tween(120))
                },
                label = "homeStatusIcon",
            ) { icon ->
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = statusContentColor,
                    modifier = Modifier.size(32.dp),
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center,
            ) {
                AnimatedContent(
                    targetState = displayTitle,
                    transitionSpec = {
                        fadeIn(tween(180)) togetherWith fadeOut(tween(120))
                    },
                    label = "homeStatusTitle",
                ) { title ->
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMediumEmphasized,
                        color = statusContentColor,
                        maxLines = 1,
                        minLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(Modifier.height(2.dp))
                AnimatedContent(
                    targetState = summary,
                    transitionSpec = {
                        fadeIn(tween(180)) togetherWith fadeOut(tween(120))
                    },
                    label = "homeStatusSummary",
                ) { text ->
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = statusContentColor.copy(alpha = 0.82f),
                        maxLines = 1,
                        minLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            StatusTag(
                label = if (isTrulyActivated) mode.shortLabel else "未就绪",
                containerColor = if (isTrulyActivated) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                contentColor = if (isTrulyActivated) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onError,
            )
        }
    }
}

internal object DeviceInfoHelper {
    fun getDeviceModel(): String {
        return runCatching {
            val systemProperties = Class.forName("android.os.SystemProperties")
            val getMethod = systemProperties.getMethod("get", String::class.java, String::class.java)
            val marketNameKeys = listOf(
                "ro.product.marketname",
                "ro.vendor.oplus.market.name",
                "ro.vivo.market.name",
                "ro.config.marketing_name"
            )
            for (key in marketNameKeys) {
                val marketName = runCatching { getMethod.invoke(null, key, "") as String }.getOrDefault("")
                if (marketName.isNotBlank()) {
                    return@runCatching marketName
                }
            }
            getFallbackDeviceInfo()
        }.getOrElse { getFallbackDeviceInfo() }
    }

    private fun getFallbackDeviceInfo(): String {
        val manufacturer = Build.MANUFACTURER?.replaceFirstChar { it.uppercase() } ?: "未知"
        val model = Build.MODEL ?: ""
        return if (model.startsWith(manufacturer, ignoreCase = true)) {
            model
        } else {
            "$manufacturer $model".trim()
        }
    }

    fun getAndroidVersion(): String {
        return "${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"
    }

    fun getAppVersion(buildVersion: String): String = buildVersion
}

internal fun copyToClipboard(context: android.content.Context, label: String, text: String) {
    runCatching {
        val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "$label 已复制到剪贴板", Toast.LENGTH_SHORT).show()
    }
}
