package takagi.ru.saison.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun EnhancedBeatVisualizer(
    isPlaying: Boolean,
    bpm: Int,
    currentBeat: Int,
    timeSignature: Int,
    modifier: Modifier = Modifier
) {
    val beatInterval = 60000 / bpm // milliseconds per beat
    
    // 节拍进度动画
    val infiniteTransition = rememberInfiniteTransition(label = "beat")
    val beatProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (isPlaying) beatInterval else 1000,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "beat_progress"
    )
    
    // 脉冲动画
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isPlaying) 1.15f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = beatInterval / 2,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val surfaceColor = MaterialTheme.colorScheme.surfaceVariant
    
    Canvas(
        modifier = modifier.size(240.dp)
    ) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.minDimension / 3
        
        if (isPlaying) {
            // 外圈脉冲环
            drawCircle(
                color = primaryColor.copy(alpha = 0.1f * (1f - beatProgress)),
                radius = radius * (1f + 0.5f * beatProgress) * pulseScale,
                center = center
            )
            
            // 中圈
            drawCircle(
                color = primaryColor.copy(alpha = 0.2f),
                radius = radius * 1.2f,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )
            
            // 内圈 - 节拍进度
            drawArc(
                color = primaryColor,
                startAngle = -90f,
                sweepAngle = 360f * beatProgress,
                useCenter = false,
                topLeft = Offset(
                    center.x - radius,
                    center.y - radius
                ),
                size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                style = Stroke(
                    width = 8.dp.toPx(),
                    cap = StrokeCap.Round
                )
            )
            
            // 中心圆
            drawCircle(
                color = primaryColor,
                radius = radius * 0.6f * pulseScale,
                center = center
            )
            
            // 节拍点指示器
            drawBeatDots(
                center = center,
                radius = radius * 1.5f,
                currentBeat = currentBeat,
                timeSignature = timeSignature,
                primaryColor = primaryColor,
                secondaryColor = secondaryColor
            )
        } else {
            // 静态显示
            drawCircle(
                color = surfaceColor,
                radius = radius * 1.2f,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )
            
            drawCircle(
                color = primaryColor.copy(alpha = 0.3f),
                radius = radius * 0.6f,
                center = center
            )
            
            // 静态节拍点
            drawBeatDots(
                center = center,
                radius = radius * 1.5f,
                currentBeat = -1,
                timeSignature = timeSignature,
                primaryColor = primaryColor.copy(alpha = 0.3f),
                secondaryColor = secondaryColor.copy(alpha = 0.3f)
            )
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawBeatDots(
    center: Offset,
    radius: Float,
    currentBeat: Int,
    timeSignature: Int,
    primaryColor: Color,
    secondaryColor: Color
) {
    val angleStep = 360f / timeSignature
    
    for (i in 0 until timeSignature) {
        val angle = Math.toRadians((i * angleStep - 90).toDouble())
        val x = center.x + radius * cos(angle).toFloat()
        val y = center.y + radius * sin(angle).toFloat()
        
        val isCurrentBeat = i == currentBeat
        val isFirstBeat = i == 0
        
        val dotColor = when {
            isCurrentBeat -> primaryColor
            isFirstBeat -> secondaryColor
            else -> primaryColor.copy(alpha = 0.3f)
        }
        
        val dotRadius = when {
            isCurrentBeat -> 10.dp.toPx()
            isFirstBeat -> 8.dp.toPx()
            else -> 6.dp.toPx()
        }
        
        drawCircle(
            color = dotColor,
            radius = dotRadius,
            center = Offset(x, y)
        )
    }
}
