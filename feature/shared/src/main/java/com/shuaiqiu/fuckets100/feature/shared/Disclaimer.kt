package com.shuaiqiu.fuckets100.feature.shared

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shuaiqiu.fuckets100.core.designsystem.TonalCard

/**
 * 本地免责声明（不依赖云端协议）。
 * 用于关于页、协议页与登录页，确保任何情况下都对本应用的责任边界作出说明。
 */
object AppDisclaimer {
    const val TITLE = "免责声明"

    const val SHORT =
        "本应用为非官方个人改版，与 ETS100（e听说）官方、LastStudio 及任何第三方平台无隶属、授权或背书关系。使用本应用产生的一切后果由你自行承担。"

    val PARAGRAPHS = listOf(
        "1. 本应用是第三方个人开发的非官方改版，与 ETS100（e听说）及其运营方、学校、畅言、LastStudio 或任何第三方平台均无隶属、代理、授权或背书关系。",
        "2. 本应用按现状提供，不保证可用性、准确性、完整性或持续可用；账号状态、接口与数据均来自第三方，可能随时变更或失效。",
        "3. 你因下载、安装、登录或使用本应用产生的一切后果与责任（包括但不限于账号受限或封禁、数据丢失、成绩与学术处分、考试违规、法律纠纷、设备或网络损失），均由你本人自行承担；本应用作者、贡献者及上述第三方不承担任何责任。",
        "4. 请仅用于个人学习与技术交流，遵守所在学校、平台规则与当地法律；严禁用于考试作弊、代答、传播答案或其他违规用途。",
        "5. 继续使用即表示你已阅读、理解并同意本声明；如不同意，请立即停止使用并卸载本应用。",
    )
}

@Composable
fun AppDisclaimerCard(
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    TonalCard(
        modifier = modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = AppDisclaimer.TITLE,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            if (compact) {
                Text(
                    text = AppDisclaimer.SHORT,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp,
                )
            } else {
                AppDisclaimer.PARAGRAPHS.forEach { paragraph ->
                    Text(
                        text = paragraph,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp,
                    )
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "一切责任由使用者自行承担，与本应用作者及上述第三方无关。",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}
