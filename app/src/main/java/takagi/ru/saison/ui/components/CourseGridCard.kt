package takagi.ru.saison.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import takagi.ru.saison.domain.model.Course
import takagi.ru.saison.domain.model.CourseGridPosition
import takagi.ru.saison.ui.theme.rememberThemeAwareCourseColor
import java.time.format.DateTimeFormatter

/**
 * 网格课程卡片组件
 * 在网格布局中显示课程信息,支持动态高度
 * 
 * 设计规范:
 * - 圆角: 10dp
 * - 内边距: 8dp
 * - 课程名: 13sp
 * - 地点: 11sp
 * - 时间: 10sp
 * - 阴影: 1dp (普通) / 3dp (当前课程)
 * - 布局间距: 4dp 和 2dp
 * 
 * @param course 课程信息
 * @param position 网格位置(偏移和高度)
 * @param isCurrentlyActive 是否为当前正在进行的课程
 * @param hasConflict 是否存在时间冲突
 * @param onClick 点击回调
 * @param modifier 修饰符
 */
@Composable
fun CourseGridCard(
    course: Course,
    position: CourseGridPosition,
    isCurrentlyActive: Boolean = false,
    hasConflict: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 使用主题感知的颜色
    val courseColor = rememberThemeAwareCourseColor(course.color)
    
    // 当前课程的脉冲动画
    val scale by animateFloatAsState(
        targetValue = if (isCurrentlyActive) 1.02f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "course_scale"
    )
    
    // 时间格式化
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    
    // 根据高度决定显示内容
    val spanCount = (course.periodEnd ?: 1) - (course.periodStart ?: 1) + 1
    val showLocation = spanCount >= 2
    val showTime = spanCount >= 3
    
    // 确保背景色足够深，以保证白色文字的对比度
    // 使用较高的 alpha 值 (0.95) 来增强颜色饱和度
    val cardBackgroundColor = courseColor.copy(alpha = 0.95f)
    
    // 点击时的阴影动画
    val elevation by animateDpAsState(
        targetValue = if (isCurrentlyActive) 3.dp else 1.dp,
        animationSpec = tween(
            durationMillis = 150,
            easing = FastOutSlowInEasing
        ),
        label = "card_elevation"
    )
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(position.height)
            .scale(scale),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = cardBackgroundColor
        ),
        shape = RoundedCornerShape(10.dp),
        border = if (isCurrentlyActive) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else if (hasConflict) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.error)
        } else null,
        elevation = CardDefaults.cardElevation(
            defaultElevation = elevation,
            pressedElevation = elevation + 2.dp,
            focusedElevation = elevation + 1.dp,
            hoveredElevation = elevation + 1.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // 课程名称(始终显示) - 13sp
                // 使用纯白色确保最佳对比度 (WCAG AA 标准)
                Text(
                    text = course.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = if (showLocation) 2 else 3,
                    overflow = TextOverflow.Ellipsis
                )
                
                // 地点(两节课及以上显示) - 11sp
                // 使用高透明度白色保持良好可读性
                if (showLocation && !course.location.isNullOrBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "📍",
                            fontSize = 11.sp
                        )
                        Text(
                            text = course.location,
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.95f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                
                // 时间(三节课及以上显示) - 10sp
                // 使用稍低透明度区分层次，但仍保持可读性
                if (showTime) {
                    Text(
                        text = "${course.startTime.format(timeFormatter)}-${course.endTime.format(timeFormatter)}",
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.90f)
                    )
                }
            }
            
            // 冲突警告图标
            if (hasConflict) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "时间冲突",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(16.dp)
                )
            }
        }
    }
}
