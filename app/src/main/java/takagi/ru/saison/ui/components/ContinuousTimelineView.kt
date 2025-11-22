package takagi.ru.saison.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import takagi.ru.saison.domain.model.Course
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * 时间段数据模型
 * 用于智能紧凑显示算法
 */
data class TimeSlot(
    val hour: Int,                    // 小时数 (0-23)
    val hasAnyCourse: Boolean,        // 是否有任何课程
    val courseCount: Int,             // 课程数量
    val displayHeight: Dp             // 显示高度
)

/**
 * 课程位置数据模型
 * 用于计算课程在紧凑布局中的位置
 */
data class CoursePosition(
    val offsetFromTop: Dp,      // 距离顶部的偏移
    val height: Dp              // 课程高度
)

/**
 * 连续时间轴课程表视图
 * 根据实际时间长度显示课程，不按节次分格
 */
@Composable
fun ContinuousTimelineView(
    coursesByDay: Map<DayOfWeek, List<Course>>,
    onCourseClick: (Long) -> Unit,
    timelineCompactness: Float = 1.0f,
    firstPeriodStartTime: LocalTime = LocalTime.of(8, 0),
    modifier: Modifier = Modifier
) {
    val weekDays = listOf(
        DayOfWeek.MONDAY,
        DayOfWeek.TUESDAY,
        DayOfWeek.WEDNESDAY,
        DayOfWeek.THURSDAY,
        DayOfWeek.FRIDAY,
        DayOfWeek.SATURDAY,
        DayOfWeek.SUNDAY
    )
    
    // 找出最早和最晚的时间
    val allCourses = coursesByDay.values.flatten()
    val earliestCourseTime = allCourses.minOfOrNull { it.startTime }
    val latestTime = allCourses.maxOfOrNull { it.endTime } ?: LocalTime.of(18, 0)
    
    // 使用第一节课开始时间或最早课程时间（取较早的）
    val earliestTime = if (earliestCourseTime != null && earliestCourseTime < firstPeriodStartTime) {
        earliestCourseTime
    } else {
        firstPeriodStartTime
    }
    
    // 调整到整点
    val startHour = earliestTime.hour
    val endHour = if (latestTime.minute > 0) latestTime.hour + 1 else latestTime.hour
    
    val listState = rememberLazyListState()
    
    Column(modifier = modifier.fillMaxSize()) {
        // 星期标题行
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            // 时间列占位
            Box(
                modifier = Modifier.width(50.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "时间",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // 星期标题
            weekDays.forEach { day ->
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = getDayShortName(day),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
        
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        
        // 时间轴和课程
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    TimelineContent(
                        startHour = startHour,
                        endHour = endHour,
                        weekDays = weekDays,
                        coursesByDay = coursesByDay,
                        onCourseClick = onCourseClick,
                        timelineCompactness = timelineCompactness
                    )
                }
            }
        }
    }
}

@Composable
private fun TimelineContent(
    startHour: Int,
    endHour: Int,
    weekDays: List<DayOfWeek>,
    coursesByDay: Map<DayOfWeek, List<Course>>,
    onCourseClick: (Long) -> Unit,
    timelineCompactness: Float = 1.0f
) {
    val pixelsPerMinute = (2.dp * timelineCompactness) // 每分钟的高度，根据紧凑度调整
    
    // 分析时间段，实现智能紧凑布局
    val timeSlots = analyzeTimeSlots(coursesByDay, startHour, endHour, timelineCompactness)
    
    Box(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // 时间轴列
            Column(
                modifier = Modifier.width(50.dp)
            ) {
                timeSlots.forEach { slot ->
                    Box(
                        modifier = Modifier.height(slot.displayHeight),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Text(
                            text = String.format("%02d:00", slot.hour),
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // 每天的课程列
            weekDays.forEach { day ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 2.dp)
                ) {
                    DayColumn(
                        day = day,
                        courses = coursesByDay[day] ?: emptyList(),
                        timeSlots = timeSlots,
                        pixelsPerMinute = pixelsPerMinute,
                        onCourseClick = onCourseClick
                    )
                }
            }
        }
        
        // 绘制时间网格线
        Column(modifier = Modifier.fillMaxWidth().padding(start = 50.dp)) {
            var accumulatedHeight = 0.dp
            timeSlots.forEach { slot ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(slot.displayHeight)
                ) {
                    Divider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopStart),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )
                }
                accumulatedHeight += slot.displayHeight
            }
        }
    }
}

@Composable
private fun DayColumn(
    day: DayOfWeek,
    courses: List<Course>,
    timeSlots: List<TimeSlot>,
    pixelsPerMinute: Dp,
    onCourseClick: (Long) -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        courses.forEach { course ->
            // 使用智能紧凑布局计算课程位置
            val position = calculateCoursePosition(course, timeSlots, pixelsPerMinute)
            
            CourseBlockCompact(
                course = course,
                offset = position.offsetFromTop,
                height = position.height,
                onClick = { onCourseClick(course.id) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CourseBlockCompact(
    course: Course,
    offset: Dp,
    height: Dp,
    onClick: () -> Unit
) {
    val courseColor = Color(course.color)
    val backgroundColor = courseColor.copy(alpha = 0.9f)
    
    // 使用 ElevatedCard 替代 Card 以支持点击
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .offset(y = offset)
            .height(height)
            .padding(vertical = 1.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = backgroundColor,
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(6.dp),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 顶部：课程名称 - 完整显示，不截断
            Text(
                text = course.name,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                lineHeight = 16.sp
            )
            
            // 底部：地点
            course.location?.let { location ->
                Text(
                    text = "@$location",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

private fun getDayShortName(day: DayOfWeek): String {
    return when (day) {
        DayOfWeek.MONDAY -> "周一"
        DayOfWeek.TUESDAY -> "周二"
        DayOfWeek.WEDNESDAY -> "周三"
        DayOfWeek.THURSDAY -> "周四"
        DayOfWeek.FRIDAY -> "周五"
        DayOfWeek.SATURDAY -> "周六"
        DayOfWeek.SUNDAY -> "周日"
    }
}

private fun getWeekPatternText(pattern: String): String {
    return when (pattern) {
        "A" -> "A周"
        "B" -> "B周"
        "ODD" -> "单周"
        "EVEN" -> "双周"
        else -> ""
    }
}

/**
 * 分析时间段，计算每个小时在一周内的课程分布
 * 用于智能紧凑显示算法 - 只返回有课程的时间段
 *
 * @param coursesByDay 按星期分组的课程列表
 * @param startHour 起始小时
 * @param endHour 结束小时
 * @param timelineCompactness 时间轴紧凑度
 * @return 时间段列表（只包含有课程的时间段）
 */
private fun analyzeTimeSlots(
    coursesByDay: Map<DayOfWeek, List<Course>>,
    startHour: Int,
    endHour: Int,
    timelineCompactness: Float = 1.0f
): List<TimeSlot> {
    val slots = mutableListOf<TimeSlot>()
    
    for (hour in startHour..endHour) {
        // 检查这个小时在一周内是否有课程
        val hasAnyCourse = coursesByDay.values.any { courses ->
            courses.any { course ->
                val courseStartHour = course.startTime.hour
                val courseEndHour = course.endTime.hour
                val courseEndMinute = course.endTime.minute
                
                // 如果课程结束时间的分钟数为0，说明课程在整点结束，不包含这个小时
                val actualEndHour = if (courseEndMinute == 0) courseEndHour - 1 else courseEndHour
                
                courseStartHour <= hour && hour <= actualEndHour
            }
        }
        
        // 只添加有课程的时间段
        if (hasAnyCourse) {
            val courseCount = coursesByDay.values.sumOf { courses ->
                courses.count { course ->
                    val courseStartHour = course.startTime.hour
                    val courseEndHour = course.endTime.hour
                    val courseEndMinute = course.endTime.minute
                    val actualEndHour = if (courseEndMinute == 0) courseEndHour - 1 else courseEndHour
                    
                    courseStartHour <= hour && hour <= actualEndHour
                }
            }
            
            slots.add(TimeSlot(
                hour = hour,
                hasAnyCourse = true,
                courseCount = courseCount,
                displayHeight = 60.dp * timelineCompactness
            ))
        }
    }
    
    return slots
}

/**
 * 计算课程在紧凑布局中的位置
 * 考虑压缩时间段对课程位置的影响
 *
 * @param course 课程对象
 * @param timeSlots 时间段列表
 * @param pixelsPerMinute 每分钟的像素数（用于正常时间段）
 * @return 课程位置信息
 */
private fun calculateCoursePosition(
    course: Course,
    timeSlots: List<TimeSlot>,
    pixelsPerMinute: Dp
): CoursePosition {
    // 计算课程开始时间之前的累积高度
    var offsetFromTop = 0.dp
    
    for (slot in timeSlots) {
        if (slot.hour < course.startTime.hour) {
            // 完全在课程开始之前的时间段，累加整个时间段的高度
            offsetFromTop += slot.displayHeight
        } else if (slot.hour == course.startTime.hour) {
            // 课程开始的时间段，需要加上分钟偏移
            val minuteOffset = course.startTime.minute
            if (slot.hasAnyCourse) {
                // 有课程的时间段：使用pixelsPerMinute计算
                offsetFromTop += pixelsPerMinute * minuteOffset
            } else {
                // 没有课程的时间段：按比例压缩
                offsetFromTop += (slot.displayHeight * minuteOffset) / 60
            }
            break
        }
    }
    
    // 计算课程高度
    val durationMinutes = ChronoUnit.MINUTES.between(
        course.startTime,
        course.endTime
    )
    
    // 计算跨越的时间段的总高度
    var totalHeight = 0.dp
    var currentHour = course.startTime.hour
    var remainingMinutes = durationMinutes.toInt()
    
    while (remainingMinutes > 0 && currentHour <= timeSlots.last().hour) {
        val slot = timeSlots.find { it.hour == currentHour } ?: break
        
        // 计算在当前时间段内的分钟数
        val minutesInThisSlot = if (currentHour == course.startTime.hour) {
            // 课程开始的时间段，从开始分钟到60分钟
            (60 - course.startTime.minute).coerceAtMost(remainingMinutes)
        } else {
            // 其他时间段，最多60分钟或剩余分钟数
            60.coerceAtMost(remainingMinutes)
        }
        
        if (slot.hasAnyCourse) {
            // 有课程的时间段：使用pixelsPerMinute计算，保持正常比例
            totalHeight += pixelsPerMinute * minutesInThisSlot
        } else {
            // 没有课程的时间段：按比例压缩
            totalHeight += (slot.displayHeight * minutesInThisSlot) / 60
        }
        
        remainingMinutes -= minutesInThisSlot
        currentHour++
    }
    
    return CoursePosition(offsetFromTop, totalHeight)
}
