package takagi.ru.saison.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun BeatVisualizer(
    isPlaying: Boolean,
    beatCount: Int,
    beatsPerMeasure: Int = 4,
    modifier: Modifier = Modifier
) {
    val currentBeat = beatCount % beatsPerMeasure
    
    // 节拍动画
    val scale by animateFloatAsState(
        targetValue = if (isPlaying) 1.2f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "beat"
    )
    
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        repeat(beatsPerMeasure) { index ->
            BeatIndicator(
                isActive = index == currentBeat && isPlaying,
                isAccent = index == 0,
                scale = if (index == currentBeat) scale else 1f
            )
        }
    }
}

@Composable
private fun BeatIndicator(
    isActive: Boolean,
    isAccent: Boolean,
    scale: Float
) {
    val color = when {
        isActive && isAccent -> MaterialTheme.colorScheme.error
        isActive -> MaterialTheme.colorScheme.primary
        isAccent -> MaterialTheme.colorScheme.onSurfaceVariant
        else -> MaterialTheme.colorScheme.outlineVariant
    }
    
    Canvas(
        modifier = Modifier
            .size(48.dp)
            .padding(4.dp)
    ) {
        val radius = size.minDimension / 2 * scale
        drawCircle(
            color = color,
            radius = radius,
            center = Offset(size.width / 2, size.height / 2)
        )
    }
}
