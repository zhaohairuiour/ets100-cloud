package com.shuaiqiu.fuckets100.feature.onboarding

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shuaiqiu.fuckets100.core.designsystem.TonalCard

import com.shuaiqiu.fuckets100.ActivationMode
// ============================================================================
// 步骤 2: 运行模式选择（仅云端模式）
// 本地三种读取模式（Shizuku / Root / 漏洞直读）已移除。
// ============================================================================

@Composable
internal fun ModeAndPermissionStep(
    initialMode: ActivationMode,
    onNext: (ActivationMode) -> Unit,
) {
    val titleAlpha = remember { Animatable(0f) }
    val titleOffsetY = remember { Animatable(24f) }
    val cardAlpha = remember { Animatable(0f) }
    val cardOffsetY = remember { Animatable(40f) }
    val ctaAlpha = remember { Animatable(0f) }
    val ctaOffsetY = remember { Animatable(24f) }

    LaunchedEffect(Unit) {
        titleAlpha.animateTo(1f, tween(360, delayMillis = 80))
        titleOffsetY.animateTo(0f, tween(400, delayMillis = 80))
        cardAlpha.animateTo(1f, tween(420, delayMillis = 220))
        cardOffsetY.animateTo(0f, tween(480, delayMillis = 220))
        ctaAlpha.animateTo(1f, tween(360, delayMillis = 420))
        ctaOffsetY.animateTo(0f, tween(420, delayMillis = 420))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 12.dp),
    ) {
        Column(
            modifier = Modifier.graphicsLayer {
                alpha = titleAlpha.value
                translationY = titleOffsetY.value
            },
        ) {
            Text(
                text = "选择运行模式",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "仅支持云端读取，无需额外系统权限",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(Modifier.height(14.dp))

        Column(
            modifier = Modifier.graphicsLayer {
                alpha = cardAlpha.value
                translationY = cardOffsetY.value
            },
        ) {
            TonalCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "云端模式通过 ETS100 官方云端 API 在线获取作业与答案，只需登录 ETS100 账号，登录信息仅保存在本机。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // 云端模式（唯一可选）
            ModeSelectionCard(
                title = "云端模式",
                subtitle = "登录 ETS100 账号，在线获取作业与答案",
                badge = "需联网",
                icon = Icons.Default.Cloud,
                selected = true,
                onClick = {},
            )

            Spacer(Modifier.height(14.dp))

            // 就绪状态：无需额外敏感权限
            Text(
                text = "所需权限与就绪状态",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(8.dp))

            TonalCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp),
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "无需额外敏感权限",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "云端模式直接通过安全通信接口工作，不读取本地文件与应用列表，纯净轻量。",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(28.dp))

        Button(
            onClick = { onNext(ActivationMode.CLOUD) },
            modifier = Modifier
                .graphicsLayer {
                    alpha = ctaAlpha.value
                    translationY = ctaOffsetY.value
                }
                .fillMaxWidth()
                .height(52.dp),
            shape = MaterialTheme.shapes.large,
        ) {
            Text("完成配置", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.width(8.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
        }

        Spacer(Modifier.height(16.dp))
    }
}
