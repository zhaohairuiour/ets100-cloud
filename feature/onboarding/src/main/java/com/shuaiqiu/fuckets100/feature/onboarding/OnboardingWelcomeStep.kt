package com.shuaiqiu.fuckets100.feature.onboarding

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.shuaiqiu.fuckets100.core.designsystem.RighteousFont
import kotlinx.coroutines.launch

/**
 * 欢迎页：云端模式介绍。入场动画使用 Animatable + graphicsLayer 直读，
 * 在 draw phase 更新，避免每帧重组整棵树导致掉帧。
 */
@Composable
internal fun WelcomeStep(onNext: () -> Unit) {
    // 全部用 Animatable，graphicsLayer lambda 里读 .value，零重组
    val logoScale = remember { Animatable(0.5f) }
    val logoAlpha = remember { Animatable(0f) }
    val titleAlpha = remember { Animatable(0f) }
    val featuresAlpha = remember { Animatable(0f) }
    val featuresOffsetY = remember { Animatable(40f) }
    val ctaAlpha = remember { Animatable(0f) }
    val ctaOffsetY = remember { Animatable(24f) }

    LaunchedEffect(Unit) {
        launch {
            logoAlpha.animateTo(1f, tween(280))
        }
        launch {
            logoScale.animateTo(
                1f,
                spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
            )
        }
        launch {
            titleAlpha.animateTo(1f, tween(420, delayMillis = 160))
        }
        launch {
            featuresAlpha.animateTo(1f, tween(420, delayMillis = 340))
        }
        launch {
            featuresOffsetY.animateTo(0f, tween(480, delayMillis = 340))
        }
        launch {
            ctaAlpha.animateTo(1f, tween(360, delayMillis = 560))
        }
        launch {
            ctaOffsetY.animateTo(0f, tween(420, delayMillis = 560))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(36.dp))

        Box(
            modifier = Modifier
                .graphicsLayer {
                    scaleX = logoScale.value
                    scaleY = logoScale.value
                    alpha = logoAlpha.value
                }
                .size(108.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Default.Cloud,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(56.dp),
            )
        }

        Spacer(Modifier.height(28.dp))

        Column(
            modifier = Modifier.graphicsLayer { alpha = titleAlpha.value },
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Fe",
                style = MaterialTheme.typography.displaySmall,
                fontFamily = RighteousFont,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "云端作业读取工具",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = "登录 ETS100 账号后，通过官方云端接口在线获取作业并解析答案。",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(Modifier.height(32.dp))

        Column(
            modifier = Modifier.graphicsLayer {
                alpha = featuresAlpha.value
                translationY = featuresOffsetY.value
            },
        ) {
            FeatureRow(
                icon = Icons.Default.Cloud,
                title = "云端读取",
                desc = "通过 ETS100 官方云端 API 在线获取作业与答案",
            )
            FeatureRow(
                icon = Icons.Default.Security,
                title = "账号登录",
                desc = "使用 E听说账号密码登录，无需 Shizuku / Root 等本地权限",
            )
            FeatureRow(
                icon = Icons.Default.Bolt,
                title = "安全存储",
                desc = "登录凭证仅保存在本机，不经过第三方账户系统",
            )
        }

        Spacer(Modifier.height(36.dp))

        Button(
            onClick = onNext,
            modifier = Modifier
                .graphicsLayer {
                    alpha = ctaAlpha.value
                    translationY = ctaOffsetY.value
                }
                .fillMaxWidth()
                .height(54.dp),
            shape = MaterialTheme.shapes.large,
        ) {
            Text("开始使用", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.width(8.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
        }

        Spacer(Modifier.height(14.dp))
        Text(
            text = "继续即表示你已阅读并同意随应用内置的使用说明：仅限个人学习交流使用，请勿传播。",
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun FeatureRow(icon: ImageVector, title: String, desc: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(enabled = false) {}
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
