package takagi.ru.saison.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import takagi.ru.saison.domain.model.Course
import takagi.ru.saison.domain.model.WeekPattern
import takagi.ru.saison.ui.theme.rememberThemeAwareCourseColor
import java.time.format.DateTimeFormatter

@Composable
fun CourseCard(
    course: Course,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    currentWeek: Int? = null
) {
    // 使用主题感知的颜色
    val courseColor = rememberThemeAwareCourseColor(course.color)
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = courseColor.copy(alpha = 0.15f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左侧颜色条
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(60.dp)
                    .background(
                        color = courseColor,
                        shape = RoundedCornerShape(2.dp)
                    )
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // 课程信息
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // 课程名称
                Text(
                    text = course.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // 时间
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${course.startTime.format(timeFormatter)} - ${course.endTime.format(timeFormatter)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // 教师和地点
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    course.instructor?.let { instructor ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = instructor,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    
                    course.location?.let { location ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = location,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
            
            // 周模式标签 - 始终显示以提供清晰的周数信息
            WeekPatternChip(
                course = course,
                currentWeek = currentWeek
            )
        }
    }
}

private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

/**
 * 周数模式标签组件
 * 显示课程的周数模式，并指示本周是否有课
 * 
 * @param course 课程信息
 * @param currentWeek 当前周数（可选）
 * @param modifier 修饰符
 * 
 * 功能：
 * - 显示不同周数模式的标签（全周、单周、双周、自定义等）
 * - 根据当前周数判断本周是否有课
 * - 使用不同的颜色和样式区分本周有课/无课状态
 * - 自定义周数显示优化：少于3周显示具体周数，否则显示总数
 */
@Composable
fun WeekPatternChip(
    course: Course,
    currentWeek: Int?,
    modifier: Modifier = Modifier
) {
    // 使用主题感知的颜色
    val courseColor = rememberThemeAwareCourseColor(course.color)
    
    // 判断本周是否有课
    val isActiveThisWeek = currentWeek?.let { week ->
        when (course.weekPattern) {
            WeekPattern.ALL -> true
            WeekPattern.ODD -> week % 2 == 1
            WeekPattern.EVEN -> week % 2 == 0
            WeekPattern.CUSTOM -> course.customWeeks?.contains(week) ?: false
            WeekPattern.A, WeekPattern.B -> true  // A/B周简化处理，始终显示为有课
        }
    } ?: true  // 如果没有提供当前周数，默认显示为有课状态
    
    // 获取显示文本
    val text = when (course.weekPattern) {
        WeekPattern.ALL -> "全周"
        WeekPattern.ODD -> "单周"
        WeekPattern.EVEN -> "双周"
        WeekPattern.A -> "A周"
        WeekPattern.B -> "B周"
        WeekPattern.CUSTOM -> {
            course.customWeeks?.let { weeks ->
                when {
                    weeks.isEmpty() -> "自定义"
                    weeks.size <= 3 -> "第${weeks.joinToString(",")}周"
                    else -> "自定义(${weeks.size}周)"
                }
            } ?: "自定义"
        }
    }
    
    // 根据是否本周有课选择颜色方案
    val containerColor = if (isActiveThisWeek) {
        // 本周有课：使用课程颜色的浅色背景
        courseColor.copy(alpha = 0.3f)
    } else {
        // 本周无课：使用中性的表面变体颜色
        MaterialTheme.colorScheme.surfaceVariant
    }
    
    val contentColor = if (isActiveThisWeek) {
        // 本周有课：使用课程颜色
        courseColor
    } else {
        // 本周无课：使用中性的表面变体文字颜色
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    // 使用 AssistChip 提供更好的 Material 3 体验
    AssistChip(
        onClick = { /* 点击事件可以在未来扩展，例如显示详细周数信息 */ },
        label = {
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isActiveThisWeek) FontWeight.Bold else FontWeight.Normal
            )
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = containerColor,
            labelColor = contentColor
        ),
        border = if (isActiveThisWeek) {
            // 本周有课：添加边框增强视觉效果
            BorderStroke(
                width = 1.dp,
                color = courseColor.copy(alpha = 0.5f)
            )
        } else {
            // 本周无课：使用默认边框
            BorderStroke(
                width = 0.5.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
        },
        modifier = modifier
    )
}
