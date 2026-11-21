package com.shuaiqiu.fuckets100

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamiccolor.ColorSpec
import com.shuaiqiu.fuckets100.core.designsystem.rememberFeMainPagerState
import com.shuaiqiu.fuckets100.feature.onboarding.OnboardingScreen
import com.shuaiqiu.fuckets100.feature.read.ReadScreen
import com.shuaiqiu.fuckets100.feature.settings.SettingsScreen
import com.shuaiqiu.fuckets100.navigation.FeNavContainer
import com.shuaiqiu.fuckets100.navigation.FeRoute
import com.shuaiqiu.fuckets100.navigation.rememberFeNavigator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 应用主界面：只保留"读取"与"设置"两个入口。
 * 仅支持云端读取：登录 ETS100 账号，在线获取作业与答案。
 * 本地三种读取模式（Shizuku / Root / 漏洞直读）与 FE 账户已移除。
 */
@Composable
fun FeAppMain() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val navigator = rememberFeNavigator(FeRoute.Main)
    LaunchedEffect(navigator) {
        navigator.onRouteRemoved = { route ->
            if (route is FeRoute.Answer) PaperStore.remove(route.paperKey)
        }
    }

    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 2 })
    val mainPagerState = rememberFeMainPagerState(pagerState)
    var predictiveBackAnimation by remember {
        mutableStateOf(SettingsManager.getPredictiveBackAnimation())
    }
    var predictiveBackExitDirection by remember {
        mutableStateOf(SettingsManager.getPredictiveBackExitDirection())
    }
    var compactAnswerDisplay by remember { mutableStateOf(SettingsManager.getCompactAnswerDisplay()) }

    var currentTheme by remember { mutableStateOf(ThemeManager.getSavedTheme()) }
    var isDarkMode by remember { mutableStateOf(ThemeManager.getSavedDarkMode()) }
    var isAutoDarkMode by remember { mutableStateOf(ThemeManager.getSavedAutoDarkMode()) }
    var useDynamicColor by remember { mutableStateOf(ThemeManager.getSavedDynamicColor()) }
    var keyColor by remember { mutableStateOf(ThemeManager.getSavedKeyColor()) }
    var paletteStyle by remember { mutableStateOf(ThemeManager.getSavedPaletteStyle()) }
    var colorSpec by remember { mutableStateOf(ThemeManager.getSavedColorSpec()) }
    var isAmoled by remember { mutableStateOf(ThemeManager.getSavedAmoled()) }
    val systemDarkMode = isSystemInDarkTheme()
    val effectiveDarkMode = if (isAutoDarkMode) systemDarkMode else isDarkMode

    var showOnboarding by remember { mutableStateOf(!SettingsManager.hasCompletedOnboarding()) }

    // 主界面淡入：onboarding 结束后首帧从 0 淡入到 1，避免硬切；draw phase 直读不触发重组。
    val mainContentAlpha = remember { Animatable(if (showOnboarding) 0f else 1f) }
    LaunchedEffect(showOnboarding) {
        if (!showOnboarding) {
            mainContentAlpha.snapTo(0f)
            mainContentAlpha.animateTo(1f, tween(320))
        }
    }

    // 仅云端模式：固定使用 CLOUD（兼容旧存档中已删除的本地模式值）。
    var currentMode by remember {
        mutableStateOf(SettingsManager.getSavedActivationMode() ?: ActivationMode.CLOUD)
    }

    var readAccessChecking by remember { mutableStateOf(true) }
    var readAccessIssue by remember { mutableStateOf<String?>(null) }
    var readAccessRefreshRevision by remember { mutableIntStateOf(0) }
    var computedReadAccessRevision by remember { mutableIntStateOf(0) }

    suspend fun computeReadAccessIssue(): String? = withContext(Dispatchers.IO) {
        when {
            currentMode != ActivationMode.CLOUD -> "仅支持云端读取模式，请前往激活设置"
            !ETS100AuthManager.isLoggedIn(context) ->
                "云端模式尚未登录，请先在运行授权页登录 ETS100 账号"
            else -> null
        }
    }

    LaunchedEffect(
        currentMode,
        readAccessRefreshRevision,
    ) {
        val resumedOnlyRefresh = readAccessRefreshRevision != computedReadAccessRevision
        computedReadAccessRevision = readAccessRefreshRevision
        if (!resumedOnlyRefresh) {
            readAccessChecking = true
        }
        readAccessIssue = computeReadAccessIssue()
        readAccessChecking = false
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                readAccessRefreshRevision++
                currentMode = SettingsManager.getSavedActivationMode() ?: ActivationMode.CLOUD
                currentTheme = ThemeManager.getSavedTheme()
                isDarkMode = ThemeManager.getSavedDarkMode()
                isAutoDarkMode = ThemeManager.getSavedAutoDarkMode()
                useDynamicColor = ThemeManager.getSavedDynamicColor()
                keyColor = ThemeManager.getSavedKeyColor()
                paletteStyle = ThemeManager.getSavedPaletteStyle()
                colorSpec = ThemeManager.getSavedColorSpec()
                isAmoled = ThemeManager.getSavedAmoled()
                predictiveBackAnimation = SettingsManager.getPredictiveBackAnimation()
                predictiveBackExitDirection = SettingsManager.getPredictiveBackExitDirection()
                compactAnswerDisplay = SettingsManager.getCompactAnswerDisplay()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(pagerState.currentPage) {
        mainPagerState.syncPage()
    }

    FeThemeWrapper(
        theme = currentTheme,
        isDarkMode = effectiveDarkMode,
        useDynamicColor = useDynamicColor,
        keyColor = keyColor,
        paletteStyle = paletteStyle,
        colorSpec = colorSpec,
        isAmoled = isAmoled,
    ) {
        if (showOnboarding) {
            OnboardingScreen(
                onFinished = {
                    showOnboarding = false
                    currentMode = SettingsManager.getSavedActivationMode()
                        ?: ActivationMode.CLOUD
                },
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = mainContentAlpha.value },
            ) {
            FeNavContainer(
                navigator = navigator,
                predictiveBackAnimation = predictiveBackAnimation,
                predictiveBackExitDirection = predictiveBackExitDirection,
                currentMode = currentMode,
                readAccessChecking = readAccessChecking,
                readAccessIssue = readAccessIssue,
                onHomeRefreshRequested = {},
                onActivationModeChanged = { currentMode = it },
                onSelectRootPage = mainPagerState::handlePageChange,
                onDarkModeChanged = { isDarkMode = it },
                onAutoDarkModeChanged = { isAutoDarkMode = it },
                onDynamicColorChanged = { useDynamicColor = it },
                onKeyColorChanged = { keyColor = it },
                onPaletteStyleChanged = { paletteStyle = it },
                onColorSpecChanged = { colorSpec = it },
                onAmoledChanged = { isAmoled = it },
                onPredictiveBackChanged = { predictiveBackAnimation = it },
                onPredictiveBackExitDirectionChanged = { predictiveBackExitDirection = it },
                selectedRootPage = mainPagerState.selectedPage,
                mainContent = {
                    BackHandler(pagerState.currentPage != 0) {
                        mainPagerState.handlePageChange(0)
                    }
                    HorizontalPager(
                        state = pagerState,
                        userScrollEnabled = mainPagerState.userScrollEnabled,
                        beyondViewportPageCount = 1,
                        modifier = Modifier.fillMaxSize(),
                    ) { page ->
                        when (page) {
                            0 -> ReadScreen(
                                currentMode = currentMode,
                                accountOnline = true,
                                readAccessChecking = readAccessChecking,
                                readAccessIssue = readAccessIssue,
                                onNavigateToActivation = {
                                    navigator.push(FeRoute.Activation)
                                },
                                onOpenPaperDetail = { paper ->
                                    navigator.push(FeRoute.Answer(PaperStore.put(paper)))
                                },
                                onOpenChangyanLogin = {
                                    context.startActivity(ChangyanWebLoginActivity.createIntent(context))
                                },
                                compactAnswerDisplay = compactAnswerDisplay,
                            )

                            else -> SettingsScreen(
                                onNavigateActivation = { navigator.push(FeRoute.Activation) },
                                onNavigateGeneralSettings = { navigator.push(FeRoute.GeneralSettings) },
                                onNavigateThemeSettings = { navigator.push(FeRoute.ThemeSettings) },
                            )
                        }
                    }
                },
            )
            }
        }
    }
}
