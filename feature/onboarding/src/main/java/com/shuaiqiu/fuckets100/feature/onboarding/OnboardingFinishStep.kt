package com.shuaiqiu.fuckets100.feature.onboarding

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

import com.shuaiqiu.fuckets100.ActivationMode
import kotlinx.coroutines.launch

/**
 * 步骤 3：完成页，仅云端模式。
 * 入场动画使用 Animatable + graphicsLayer 直读，避免逐帧重组整棵树。
 */
@Composable
internal fun FinishStep(
    selectedMode: ActivationMode,
    onFinish: () -> Unit,
) {
    val iconScale = remember { Animatable(0.4f) }
    val iconAlpha = remember { Animatable(0f) }
    val titleAlpha = remember { Animatable(0f) }
    val titleOffsetY = remember { Animatable(24f) }
    val cardAlpha = remember { Animatable(0f) }
    val cardOffsetY = remember { Animatable(40f) }
    val ctaAlpha = remember { Animatable(0f) }
    val ctaOffsetY = remember { Animatable(24f) }

    LaunchedEffect(Unit) {
        launch { iconAlpha.animateTo(1f, tween(260)) }
        launch {
            iconScale.animateTo(
                1f,
                spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
            )
        }
        launch { titleAlpha.animateTo(1f, tween(380, delayMillis = 180)) }
        launch { titleOffsetY.animateTo(0f, tween(420, delayMillis = 180)) }
        launch { cardAlpha.animateTo(1f, tween(420, delayMillis = 340)) }
        launch { cardOffsetY.animateTo(0f, tween(480, delayMillis = 340)) }
        launch { ctaAlpha.animateTo(1f, tween(360, delayMillis = 540)) }
        launch { ctaOffsetY.animateTo(0f, tween(420, delayMillis = 540)) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Spacer(Modifier.weight(0.7f))

        Box(
            modifier = Modifier
                .graphicsLayer {
                    scaleX = iconScale.value
                    scaleY = iconScale.value
                    alpha = iconAlpha.value
                }
                .size(96.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Default.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(54.dp),
            )
        }

        Spacer(Modifier.height(24.dp))

        Column(
            modifier = Modifier.graphicsLayer {
                alpha = titleAlpha.value
                translationY = titleOffsetY.value
            },
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "准备就绪",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "登录 ETS100 账号即可在线获取作业与答案",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }

        Spacer(Modifier.height(28.dp))

        Card(
            modifier = Modifier
                .graphicsLayer {
                    alpha = cardAlpha.value
                    translationY = cardOffsetY.value
                }
                .fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
            ),
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                SummaryItem(text = "仅云端读取，无需 Shizuku / Root 等本地权限")
                SummaryItem(text = "运行模式：" + selectedMode.title)
                SummaryItem(text = "登录信息仅保存在本机")
            }
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = "提示：进入后请先登录 ETS100 账号，再刷新作业列表。",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp),
        )

        Spacer(Modifier.weight(1f))

        Button(
            onClick = onFinish,
            modifier = Modifier
                .graphicsLayer {
                    alpha = ctaAlpha.value
                    translationY = ctaOffsetY.value
                }
                .fillMaxWidth()
                .height(52.dp),
            shape = MaterialTheme.shapes.large,
        ) {
            Text("进入应用", style = MaterialTheme.typography.titleMedium)
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
internal fun SummaryItem(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            Icons.Default.Check,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}
