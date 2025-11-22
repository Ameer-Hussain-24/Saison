package takagi.ru.saison.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import takagi.ru.saison.domain.model.Course
import takagi.ru.saison.domain.model.CoursePeriod
import java.time.DayOfWeek
import java.time.format.DateTimeFormatter

/**
 * 周课程表时间轴视图
 * 类似日历的时间轴布局，纵向显示时间，横向显示每天的课程
 */
@Composable
fun WeeklyTimelineView(
    periods: List<CoursePeriod>,
    coursesByDay: Map<DayOfWeek, List<Course>>,
    onCourseClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    val weekDays = listOf(
        DayOfWeek.MONDAY,
        DayOfWeek.TUESDAY,
        DayOfWeek.WEDNESDAY,
        DayOfWeek.THURSDAY,
        DayOfWeek.FRIDAY,
        DayOfWeek.SATURDAY,
        DayOfWeek.SUNDAY
    )
    
    Column(modifier = modifier.fillMaxSize()) {
        // 星期标题行
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(vertical = 8.dp)
        ) {
            // 时间列占位
            Box(
                modifier = Modifier.width(60.dp),
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
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        
        Divider()
        
        // 时间轴和课程
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(periods) { period ->
                TimelineRow(
                    period = period,
                    weekDays = weekDays,
                    coursesByDay = coursesByDay,
                    onCourseClick = onCourseClick,
                    timeFormatter = timeFormatter
                )
            }
        }
    }
}

@Composable
private fun TimelineRow(
    period: CoursePeriod,
    weekDays: List<DayOfWeek>,
    coursesByDay: Map<DayOfWeek, List<Course>>,
    onCourseClick: (Long) -> Unit,
    timeFormatter: DateTimeFormatter
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .padding(vertical = 4.dp)
    ) {
        // 时间列
        Column(
            modifier = Modifier
                .width(60.dp)
                .fillMaxHeight()
                .padding(horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "${period.periodNumber}",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = period.startTime.format(timeFormatter),
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = period.endTime.format(timeFormatter),
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        // 每天的课程
        weekDays.forEach { day ->
            val coursesForDay = coursesByDay[day] ?: emptyList()
            val courseForPeriod = coursesForDay.find { course ->
                if (course.isCustomTime) {
                    // 自定义时间模式：检查时间是否在节次范围内
                    course.startTime <= period.startTime && course.endTime >= period.endTime
                } else {
                    // 节次模式：检查节次是否在范围内
                    val start = course.periodStart ?: return@find false
                    val end = course.periodEnd ?: return@find false
                    period.periodNumber in start..end
                }
            }
            
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 2.dp)
            ) {
                if (courseForPeriod != null) {
                    CourseCell(
                        course = courseForPeriod,
                        period = period,
                        onClick = { onCourseClick(courseForPeriod.id) }
                    )
                } else {
                    // 空单元格
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CourseCell(
    course: Course,
    period: CoursePeriod,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(course.color).copy(alpha = 0.8f),
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = course.name,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                fontSize = 11.sp
            )
            
            Column {
                course.location?.let { location ->
                    Text(
                        text = "@$location",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                course.instructor?.let { instructor ->
                    Text(
                        text = instructor,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                // 显示周模式
                if (course.weekPattern.name != "ALL") {
                    Text(
                        text = getWeekPatternText(course.weekPattern.name),
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 8.sp
                    )
                }
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
