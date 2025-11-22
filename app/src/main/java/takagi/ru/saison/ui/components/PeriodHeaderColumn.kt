package takagi.ru.saison.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import takagi.ru.saison.domain.model.CoursePeriod

/**
 * 节次标题列组件
 * 显示课程节次编号("第1节", "第2节"等)
 * 
 * @param periods 节次列表
 * @param cellHeight 单元格高度
 * @param currentPeriod 当前节次(用于高亮)
 * @param modifier 修饰符
 */
@Composable
fun PeriodHeaderColumn(
    periods: List<CoursePeriod>,
    cellHeight: Dp,
    currentPeriod: Int? = null,
    modifier: Modifier = Modifier
) {
    // 当前节次的呼吸动画
    val infiniteTransition = rememberInfiniteTransition(label = "period_highlight")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "period_alpha"
    )
    
    Column(
        modifier = modifier.width(60.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        periods.forEach { period ->
            val isCurrentPeriod = currentPeriod == period.periodNumber
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(cellHeight)
                    .background(
                        color = if (isCurrentPeriod) {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = alpha)
                        } else {
                            MaterialTheme.colorScheme.surface
                        },
                        shape = RoundedCornerShape(4.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "第${period.periodNumber}节",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = if (isCurrentPeriod) FontWeight.Bold else FontWeight.Normal,
                    color = if (isCurrentPeriod) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
