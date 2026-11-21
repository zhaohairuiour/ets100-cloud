package com.shuaiqiu.fuckets100.feature.read

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.delay

/**
 * 列表项首屏入场：按下标做 stagger 淡入 + 上滑。
 *
 * 采用 Animatable + graphicsLayer draw-phase 直读，动画期间不触发重组；
 * LazyColumn 只组合可见项，因此天然只对首屏生效。
 * 下标超过 [maxStaggerIndex] 后统一延迟，避免深列表累积过长等待。
 */
@Composable
internal fun StaggerEntrance(
    index: Int,
    modifier: Modifier = Modifier,
    maxStaggerIndex: Int = 8,
    stepMillis: Long = 45L,
    durationMillis: Int = 360,
    offsetPx: Float = 28f,
    content: @Composable () -> Unit,
) {
    val alpha = remember { Animatable(0f) }
    val translationY = remember { Animatable(offsetPx) }

    LaunchedEffect(Unit) {
        delay(index.coerceIn(0, maxStaggerIndex) * stepMillis)
        alpha.animateTo(1f, tween(durationMillis, easing = FastOutSlowInEasing))
        translationY.animateTo(0f, tween(durationMillis, easing = FastOutSlowInEasing))
    }

    Box(
        modifier = modifier.graphicsLayer {
            this.alpha = alpha.value
            this.translationY = translationY.value
        },
    ) {
        content()
    }
}
