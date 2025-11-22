package takagi.ru.saison.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.ripple
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import takagi.ru.saison.domain.model.CoursePeriod

/**
 * 网格单元格组件
 * 表示课程表中的一个时间段单元格
 * 
 * 设计规范:
 * - 边框: 0.5dp
 * - 圆角: 4dp
 * - 背景色透明度优化
 * - 涟漪效果正常工作
 * - 长按高亮效果
 * - 流畅的交互动画 (<200ms)
 * 
 * @param period 课程节次信息
 * @param isEmpty 是否为空白单元格
 * @param onClick 点击回调
 * @param onLongClick 长按回调
 * @param modifier 修饰符
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GridCell(
    period: CoursePeriod,
    isEmpty: Boolean,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    // 长按高亮状态
    var isLongPressed by remember { mutableStateOf(false) }
    
    // 长按高亮动画
    val highlightAlpha by animateFloatAsState(
        targetValue = if (isLongPressed) 0.5f else 0.2f,
        animationSpec = tween(
            durationMillis = 150,
            easing = FastOutSlowInEasing
        ),
        label = "cell_highlight"
    )
    
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                enabled = isEmpty,
                onClick = onClick,
                onLongClick = {
                    isLongPressed = true
                    onLongClick?.invoke()
                }
            ),
        color = if (isEmpty) {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = highlightAlpha)
        } else {
            Color.Transparent
        },
        shape = RoundedCornerShape(4.dp),
        border = if (isEmpty) {
            BorderStroke(
                width = 0.5.dp,
                color = if (isLongPressed) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                } else {
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                }
            )
        } else null
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        )
    }
}
