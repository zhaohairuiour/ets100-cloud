package com.shuaiqiu.fuckets100.feature.settings

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.shuaiqiu.fuckets100.core.designsystem.ExpressiveScaffold
import com.shuaiqiu.fuckets100.core.designsystem.ExpressiveSegmentedListItem
import com.shuaiqiu.fuckets100.core.designsystem.SegmentedColumn
import com.shuaiqiu.fuckets100.core.designsystem.expressiveTopAppBarColors
import kotlinx.coroutines.launch

/**
 * 设置页：只保留云端读取相关配置（运行授权 / 通用 / 主题）。
 * 账号、本地读取模式、官网、关于、捐赠、公告等入口已全部移除。
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsScreen(
    onNavigateActivation: () -> Unit,
    onNavigateGeneralSettings: () -> Unit,
    onNavigateThemeSettings: () -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    // 内容入场：整块淡入 + 上滑，draw-phase 直读，避免逐帧重组。
    val contentAlpha = remember { Animatable(0f) }
    val contentOffsetY = remember { Animatable(28f) }
    LaunchedEffect(Unit) {
        launch { contentAlpha.animateTo(1f, tween(360, delayMillis = 80)) }
        launch { contentOffsetY.animateTo(0f, tween(440, delayMillis = 80)) }
    }

    ExpressiveScaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text("设置") },
                windowInsets = WindowInsets.safeDrawing.only(
                    WindowInsetsSides.Top + WindowInsetsSides.Horizontal,
                ),
                colors = expressiveTopAppBarColors(),
                scrollBehavior = scrollBehavior,
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        contentWindowInsets = WindowInsets.safeDrawing.only(
            WindowInsetsSides.Top + WindowInsetsSides.Horizontal,
        ),
    ) { p ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(p)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(16.dp))

            Column(
                modifier = Modifier.graphicsLayer {
                    alpha = contentAlpha.value
                    translationY = contentOffsetY.value
                },
            ) {
                SegmentedColumn(
                    title = "读取模式",
                    items = listOf(
                        {
                            SettingsSegmentedItem(
                                index = 0,
                                count = 3,
                                icon = Icons.Default.Cloud,
                                title = "运行授权",
                                supportingText = "云端模式登录与授权",
                                onClick = onNavigateActivation,
                            )
                        },
                        {
                            SettingsSegmentedItem(
                                index = 1,
                                count = 3,
                                icon = Icons.Default.Tune,
                                title = "通用设置",
                                supportingText = "配置答案显示方式",
                                onClick = onNavigateGeneralSettings,
                            )
                        },
                        {
                            SettingsSegmentedItem(
                                index = 2,
                                count = 3,
                                icon = Icons.Default.Palette,
                                title = "主题",
                                supportingText = "彩色主题与明暗模式",
                                onClick = onNavigateThemeSettings,
                            )
                        },
                    ),
                )
            }

            Spacer(Modifier.height(88.dp))
        }
    }
}

@Composable
private fun SettingsSegmentedItem(
    index: Int,
    count: Int,
    icon: ImageVector,
    title: String,
    supportingText: String,
    onClick: () -> Unit,
) {
    ExpressiveSegmentedListItem(
        index = index,
        count = count,
        onClick = onClick,
        headlineContent = { Text(title) },
        supportingContent = { Text(supportingText) },
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        },
        trailingContent = {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
    )
}
