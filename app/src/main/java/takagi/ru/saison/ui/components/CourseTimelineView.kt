package takagi.ru.saison.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import takagi.ru.saison.domain.model.Course
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * 课程时间轴视图组件
 * 显示 24 小时时间轴，用于展示课程安排
 * 
 * @param courses 课程列表
 * @param onCourseClick 点击课程的回调
 * @param onCourseLongClick 长按课程的回调
 * @param modifier Modifier
 * @param startHour 开始小时（默认 0）
 * @param endHour 结束小时（默认 23）
 * @param currentTime 当前时间，用于显示当前时间指示器
 * @param autoScrollToCurrent 是否自动滚动到当前时间
 */
@Composable
fun CourseTimelineView(
    courses: List<Course>,
    onCourseClick: (Course) -> Unit,
    onCourseLongClick: (Course) -> Unit = {},
    modifier: Modifier = Modifier,
    startHour: Int = 0,
    endHour: Int = 23,
    currentTime: LocalTime = LocalTime.now(),
    autoScrollToCurrent: Boolean = true
) {
    val hours = (startHour..endHour).toList()
    val listState = rememberLazyListState()
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    
    // 自动滚动到当前时间
    LaunchedEffect(autoScrollToCurrent) {
        if (autoScrollToCurrent && currentTime.hour in startHour..endHour) {
            val targetIndex = currentTime.hour - startHour
            if (targetIndex >= 0 && targetIndex < hours.size) {
                listState.animateScrollToItem(targetIndex)
            }
        }
    }
    
    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 16.dp,
                bottom = 16.dp
            )
        ) {
            items(hours) { hour ->
                // 找到该小时内的课程
                val hourCourses = courses.filter { course ->
                    course.startTime.hour == hour || 
                    (course.startTime.hour < hour && course.endTime.hour >= hour)
                }
                
                TimelineHourRow(
                    hour = hour,
                    courses = hourCourses,
                    currentTime = currentTime,
                    isCurrentHour = hour == currentTime.hour,
                    onCourseClick = onCourseClick,
                    onCourseLongClick = onCourseLongClick
                )
            }
        }
    }
}

/**
 * 时间轴小时行组件
 * 显示一个小时的时间刻度和时间线
 * 
 * @param hour 小时数（0-23）
 * @param courses 该小时内的课程列表
 * @param currentTime 当前时间
 * @param isCurrentHour 是否为当前小时
 * @param onCourseClick 点击课程的回调
 * @param onCourseLongClick 长按课程的回调
 */
@Composable
private fun TimelineHourRow(
    hour: Int,
    courses: List<Course>,
    currentTime: LocalTime,
    isCurrentHour: Boolean,
    onCourseClick: (Course) -> Unit,
    onCourseLongClick: (Course) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        verticalAlignment = Alignment.Top
    ) {
        // 时间标签
        TimeLabel(
            hour = hour,
            isCurrentHour = isCurrentHour,
            modifier = Modifier.width(60.dp)
        )
        
        // 时间线和内容区域
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            // 绘制时间线
            TimelineLine(
                isCurrentHour = isCurrentHour,
                modifier = Modifier.fillMaxSize()
            )
            
            // 如果是当前小时，绘制当前时间指示器
            if (isCurrentHour) {
                CurrentTimeIndicator(
                    currentTime = currentTime,
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            // 课程内容区域
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 12.dp)
            ) {
                // 处理课程重叠：按开始时间分组
                val groupedCourses = groupOverlappingCourses(courses, hour)
                
                groupedCourses.forEachIndexed { columnIndex, columnCourses ->
                    columnCourses.forEach { course ->
                        CourseBlock(
                            course = course,
                            hour = hour,
                            columnIndex = columnIndex,
                            totalColumns = groupedCourses.size,
                            onClick = { onCourseClick(course) },
                            onLongClick = { onCourseLongClick(course) },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}

/**
 * 时间标签组件
 * 显示小时数
 * 
 * @param hour 小时数
 * @param isCurrentHour 是否为当前小时
 * @param modifier Modifier
 */
@Composable
private fun TimeLabel(
    hour: Int,
    isCurrentHour: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .padding(end = 8.dp, top = 4.dp),
        contentAlignment = Alignment.TopEnd
    ) {
        Text(
            text = String.format("%02d:00", hour),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isCurrentHour) FontWeight.Bold else FontWeight.Normal,
            color = if (isCurrentHour) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}

/**
 * 时间线组件
 * 绘制垂直时间线
 * 
 * @param isCurrentHour 是否为当前小时
 * @param modifier Modifier
 */
@Composable
private fun TimelineLine(
    isCurrentHour: Boolean,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val lineColor = if (isCurrentHour) {
            Color(0xFFE91E63).copy(alpha = 0.3f)
        } else {
            Color(0xFFE0E0E0)
        }
        
        // 绘制垂直线
        drawLine(
            color = lineColor,
            start = Offset(0f, 0f),
            end = Offset(0f, size.height),
            strokeWidth = 1.dp.toPx()
        )
        
        // 绘制小时刻度线
        drawLine(
            color = lineColor,
            start = Offset(0f, 0f),
            end = Offset(8.dp.toPx(), 0f),
            strokeWidth = 1.dp.toPx()
        )
    }
}

/**
 * 当前时间指示器组件
 * 在当前时间位置绘制红色指示线
 * 
 * @param currentTime 当前时间
 * @param modifier Modifier
 */
@Composable
private fun CurrentTimeIndicator(
    currentTime: LocalTime,
    modifier: Modifier = Modifier
) {
    // 计算当前时间在小时内的位置（0.0 - 1.0）
    val minuteProgress = currentTime.minute / 60f
    
    Canvas(modifier = modifier) {
        val yPosition = size.height * minuteProgress
        val currentTimeColor = Color(0xFFE91E63)
        
        // 绘制圆点
        drawCircle(
            color = currentTimeColor,
            radius = 4.dp.toPx(),
            center = Offset(0f, yPosition)
        )
        
        // 绘制横线
        drawLine(
            color = currentTimeColor,
            start = Offset(0f, yPosition),
            end = Offset(size.width, yPosition),
            strokeWidth = 2.dp.toPx()
        )
    }
}

/**
 * 课程色块组件
 * 显示单个课程的色块
 * 
 * @param course 课程信息
 * @param hour 当前小时
 * @param columnIndex 列索引（用于处理重叠）
 * @param totalColumns 总列数
 * @param onClick 点击回调
 * @param onLongClick 长按回调
 * @param modifier Modifier
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CourseBlock(
    course: Course,
    hour: Int,
    columnIndex: Int,
    totalColumns: Int,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 计算课程在当前小时内的位置和高度
    val (topOffset, heightFraction) = calculateCoursePosition(course, hour)
    
    // 计算宽度和偏移（处理重叠）
    val widthFraction = 1f / totalColumns
    val leftOffset = widthFraction * columnIndex
    
    BoxWithConstraints(modifier = modifier) {
        val blockHeight = maxHeight * heightFraction
        val blockWidth = maxWidth * widthFraction
        val topPosition = maxHeight * topOffset
        val leftPosition = maxWidth * leftOffset
        
        Card(
            modifier = Modifier
                .offset(x = leftPosition, y = topPosition)
                .width(blockWidth - 4.dp)
                .height(blockHeight)
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick
                ),
            colors = CardDefaults.cardColors(
                containerColor = Color(course.color).copy(alpha = 0.9f)
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // 课程名称
                Text(
                    text = course.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                // 时间信息
                Text(
                    text = "${course.startTime.format(DateTimeFormatter.ofPattern("HH:mm"))} - ${course.endTime.format(DateTimeFormatter.ofPattern("HH:mm"))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.9f)
                )
                
                // 地点信息
                course.location?.let { location ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = Color.White.copy(alpha = 0.9f)
                        )
                        Text(
                            text = location,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.9f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

/**
 * 计算课程在当前小时内的位置和高度
 * 
 * @param course 课程信息
 * @param hour 当前小时
 * @return Pair<顶部偏移比例, 高度比例>
 */
private fun calculateCoursePosition(course: Course, hour: Int): Pair<Float, Float> {
    val hourStart = LocalTime.of(hour, 0)
    val hourEnd = LocalTime.of(hour, 59, 59)
    
    // 课程在当前小时内的实际开始和结束时间
    val effectiveStart = if (course.startTime.hour < hour) hourStart else course.startTime
    val effectiveEnd = if (course.endTime.hour > hour) hourEnd else course.endTime
    
    // 计算相对于小时开始的分钟数
    val startMinutes = ChronoUnit.MINUTES.between(hourStart, effectiveStart).toFloat()
    val endMinutes = ChronoUnit.MINUTES.between(hourStart, effectiveEnd).toFloat()
    
    // 转换为比例（0.0 - 1.0）
    val topOffset = startMinutes / 60f
    val heightFraction = (endMinutes - startMinutes) / 60f
    
    return Pair(topOffset, heightFraction.coerceAtLeast(0.1f)) // 最小高度 10%
}

/**
 * 将重叠的课程分组到不同的列
 * 
 * @param courses 课程列表
 * @param hour 当前小时
 * @return 分组后的课程列表（每个列表代表一列）
 */
private fun groupOverlappingCourses(courses: List<Course>, hour: Int): List<List<Course>> {
    if (courses.isEmpty()) return emptyList()
    
    // 按开始时间排序
    val sortedCourses = courses.sortedBy { it.startTime }
    
    val columns = mutableListOf<MutableList<Course>>()
    
    sortedCourses.forEach { course ->
        // 查找可以放置该课程的列
        var placed = false
        for (column in columns) {
            // 检查该列的最后一个课程是否与当前课程重叠
            val lastCourse = column.lastOrNull()
            if (lastCourse == null || !isOverlapping(lastCourse, course)) {
                column.add(course)
                placed = true
                break
            }
        }
        
        // 如果没有合适的列，创建新列
        if (!placed) {
            columns.add(mutableListOf(course))
        }
    }
    
    return columns
}

/**
 * 检查两个课程是否在时间上重叠
 * 
 * @param course1 课程1
 * @param course2 课程2
 * @return 是否重叠
 */
private fun isOverlapping(course1: Course, course2: Course): Boolean {
    return course1.startTime < course2.endTime && course2.startTime < course1.endTime
}
