package com.shuaiqiu.fuckets100.feature.activation

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

import com.shuaiqiu.fuckets100.ActivationMode
import com.shuaiqiu.fuckets100.core.common.maskPhoneNumber
/**
 * 当前授权状态卡片 - 仅云端模式
 * 本地三种读取模式（Shizuku / Root / 漏洞直读）已移除。
 */
@Composable
internal fun FeModeStatusCard(
    currentMode: ActivationMode,
    cloudLoggedIn: Boolean,
    cloudPhone: String?,
    context: android.content.Context,
) {
    val statusColor = if (cloudLoggedIn) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.error
    }

    val isActive = cloudLoggedIn
    val statusContainerColor = if (isActive) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.errorContainer
    }

    val statusTitle = if (cloudLoggedIn) "云端模式已激活" else "云端模式未激活"
    val statusDesc = if (cloudLoggedIn) {
        "（${maskPhoneNumber(cloudPhone)}）手机号已登录"
    } else {
        "需要登录 ETS100 账号"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = statusContainerColor,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = if (isActive) Icons.Default.CheckCircle else Icons.Default.Warning,
                contentDescription = null,
                tint = statusColor,
                modifier = Modifier.size(28.dp),
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        ActivationMode.CLOUD.icon,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(15.dp),
                    )
                    Spacer(Modifier.width(5.dp))
                    Text(statusTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.height(3.dp))
                Text(statusDesc, style = MaterialTheme.typography.bodySmall, color = statusColor)
            }
        }
    }
}
