package com.shuaiqiu.fuckets100.feature.activation

import androidx.compose.animation.*
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.shuaiqiu.fuckets100.core.designsystem.ExpressiveScaffold
import com.shuaiqiu.fuckets100.core.designsystem.ExpressiveSegmentedListItem
import com.shuaiqiu.fuckets100.core.designsystem.SegmentedColumn
import com.shuaiqiu.fuckets100.core.designsystem.TopBarBackButton
import com.shuaiqiu.fuckets100.core.designsystem.expressiveTopAppBarColors

import com.shuaiqiu.fuckets100.ActivationMode
import com.shuaiqiu.fuckets100.core.designsystem.FeThinDivider
/**
 * 运行授权与权限设置页 - 仅云端模式
 * 本地三种读取模式（Shizuku / Root / 漏洞直读）与 FE 账户登录已移除。
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ActivationSettingsScreen(
    currentMode: ActivationMode,
    cloudAuthRevision: Int = 0,
    onModeSelected: (ActivationMode) -> Unit,
    onBack: () -> Unit,
    onNavigateToCloudActivation: () -> Unit,
    onNavigateToRead: () -> Unit,
    viewModel: ActivationViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsState()
    val pageSnapshot = uiState.snapshot
    val cloudLoggedIn = pageSnapshot?.cloudLoggedIn == true
    val cloudPhone = pageSnapshot?.cloudPhone

    // 首次打开异步读取；登录成功后 cloudAuthRevision 变化会强制刷新，避免命中缓存。
    LaunchedEffect(cloudAuthRevision) {
        viewModel.refresh(force = true)
    }

    var hasPaused by remember { mutableStateOf(false) }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                hasPaused = true
            } else if (event == Lifecycle.Event.ON_RESUME && hasPaused) {
                viewModel.refresh(force = true)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    ExpressiveScaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        contentWindowInsets = WindowInsets.safeDrawing.only(
            WindowInsetsSides.Top + WindowInsetsSides.Horizontal,
        ),
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text("运行授权与权限") },
                subtitle = { Text("配置云端读取模式") },
                navigationIcon = { TopBarBackButton(onClick = onBack) },
                windowInsets = WindowInsets.safeDrawing.only(
                    WindowInsetsSides.Top + WindowInsetsSides.Horizontal,
                ),
                colors = expressiveTopAppBarColors(),
                scrollBehavior = scrollBehavior,
            )
        }
    ) { p ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(p)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // 当前模式状态卡片：云端登录状态。
            item {
                ActivationSectionLabel(text = "当前授权状态")
                FeModeStatusCard(
                    currentMode = currentMode,
                    cloudLoggedIn = cloudLoggedIn,
                    cloudPhone = cloudPhone,
                    context = context,
                )
            }

            // 云端模式卡片
            item {
                SegmentedColumn(
                    title = "读取模式",
                    items = listOf(
                        {
                            val isSelected = currentMode == ActivationMode.CLOUD
                            val transitionState = remember { MutableTransitionState(isSelected) }.apply {
                                targetState = isSelected
                            }
                            ExpressiveSegmentedListItem(
                                index = 0,
                                count = 1,
                                onClick = { onModeSelected(ActivationMode.CLOUD) },
                                headlineContent = {
                                    Text(
                                        ActivationMode.CLOUD.title,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                    )
                                },
                                leadingContent = {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = null,
                                        colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary),
                                    )
                                },
                                supportingContent = {
                                    Column {
                                        Text(ActivationMode.CLOUD.desc, style = MaterialTheme.typography.bodySmall)
                                        AnimatedVisibility(
                                            visibleState = transitionState,
                                            enter = expandVertically() + fadeIn(),
                                            exit = shrinkVertically() + fadeOut(),
                                        ) {
                                            Column {
                                                FeThinDivider(modifier = Modifier.padding(vertical = 12.dp))
                                                FeCloudActivationPanel(
                                                    context = context,
                                                    isLoggedIn = cloudLoggedIn,
                                                    phone = cloudPhone,
                                                    onCloudAuthChanged = { viewModel.refresh(force = true) },
                                                    onNavigateToCloudActivation = onNavigateToCloudActivation,
                                                    onNavigateToRead = onNavigateToRead,
                                                )
                                            }
                                        }
                                    }
                                },
                            )
                        },
                    ),
                )
            }
            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
internal fun ActivationSectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, bottom = 8.dp),
    )
}
