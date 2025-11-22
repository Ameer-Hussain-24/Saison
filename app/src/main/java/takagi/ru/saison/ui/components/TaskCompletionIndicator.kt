package takagi.ru.saison.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp

/**
 * 任务完成进度指示器
 * 显示任务完成百分比，根据完成度应用不同颜色
 * 
 * @param completionRate 完成率 (0.0 - 1.0)
 * @param modifier Modifier
 */
@Composable
fun TaskCompletionIndicator(
    completionRate: Float,
    modifier: Modifier = Modifier
) {
    // 动画过渡效果
    val animatedProgress by animateFloatAsState(
        targetValue = completionRate.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 300),
        label = "progress_animation"
    )
    
    // 根据完成度获取颜色
    val indicatorColor = when {
        completionRate >= 0.8f -> Color(0xFF4CAF50) // 绿色
        completionRate >= 0.5f -> Color(0xFFFFC107) // 黄色
        else -> Color(0xFFFF5722) // 红色
    }
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(4.dp)
            .clip(RoundedCornerShape(2.dp))
    ) {
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier.fillMaxWidth(),
            color = indicatorColor,
            trackColor = Color.Transparent,
            strokeCap = StrokeCap.Round
        )
    }
}

/**
 * 任务完成进度指示器（带背景轨道）
 * 
 * @param completionRate 完成率 (0.0 - 1.0)
 * @param modifier Modifier
 * @param trackColor 轨道颜色
 */
@Composable
fun TaskCompletionIndicatorWithTrack(
    completionRate: Float,
    modifier: Modifier = Modifier,
    trackColor: Color = Color(0xFFE0E0E0)
) {
    // 动画过渡效果
    val animatedProgress by animateFloatAsState(
        targetValue = completionRate.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 300),
        label = "progress_animation"
    )
    
    // 根据完成度获取颜色
    val indicatorColor = when {
        completionRate >= 0.8f -> Color(0xFF4CAF50) // 绿色
        completionRate >= 0.5f -> Color(0xFFFFC107) // 黄色
        else -> Color(0xFFFF5722) // 红色
    }
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(4.dp)
            .clip(RoundedCornerShape(2.dp))
    ) {
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier.fillMaxWidth(),
            color = indicatorColor,
            trackColor = trackColor,
            strokeCap = StrokeCap.Round
        )
    }
}
