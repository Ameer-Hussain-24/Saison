package takagi.ru.saison.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import takagi.ru.saison.domain.model.BreakPeriod
import takagi.ru.saison.domain.model.Course
import takagi.ru.saison.domain.model.CoursePeriod
import takagi.ru.saison.domain.model.GridLayoutConfig
import takagi.ru.saison.ui.theme.rememberThemeAwareCourseColor
import takagi.ru.saison.util.buildGridRows
import java.time.DayOfWeek
import java.time.LocalDate

/**
 * 网格课程表视图（重构版 - 基于时间轴）
 * 以网格形式显示课程表,支持垂直滚动,全周可见,课程卡片根据实际时间长度显示
 * 
 * @param coursesByDay 按星期分组的课程列表
 * @param periods 节次列表
 * @param breakPeriods 休息时段列表
 * @param semesterStartDate 学期开始日期
 * @param currentWeek 当前周次（1-based）
 * @param onCourseClick 课程点击回调
 * @param onEmptyCellClick 空白单元格点击回调(星期, 节次)
 * @param currentPeriod 当前节次
 * @param currentDay 当前星期
 * @param config 网格布局配置
 * @param weekDays 要显示的星期列表
 * @param autoScrollToCurrentTime 是否自动滚动到当前时间
 * @param onWeekLabelClick 周次标签点击回调
 * @param modifier 修饰符
 */
@Composable
fun GridTimetableView(
    coursesByDay: Map<DayOfWeek, List<Course>>,
    periods: List<CoursePeriod>,
    breakPeriods: List<BreakPeriod> = emptyList(),
    semesterStartDate: LocalDate,
    currentWeek: Int,
    onCourseClick: (Long) -> Unit,
    onEmptyCellClick: (DayOfWeek, Int) -> Unit,
    currentPeriod: Int? = null,
    currentDay: DayOfWeek? = null,
    config: GridLayoutConfig = GridLayoutConfig(),
    weekDays: List<DayOfWeek> = DayOfWeek.values().toList(),
    autoScrollToCurrentTime: Boolean = true,
    onWeekLabelClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current
    
    // 计算时间轴范围：从最早的课程到最晚的课程
    val (startTime, endTime) = remember(periods) {
        if (periods.isEmpty()) {
            java.time.LocalTime.of(8, 0) to java.time.LocalTime.of(18, 0)
        } else {
            val earliest = periods.minByOrNull { it.startTime }?.startTime ?: java.time.LocalTime.of(8, 0)
            val latest = periods.maxByOrNull { it.endTime }?.endTime ?: java.time.LocalTime.of(18, 0)
            earliest to latest
        }
    }
    
    // 每分钟对应的像素高度（增加到3dp以获得更好的视觉效果）
    val pixelsPerMinute = 3.dp
    
    // 自动滚动到当前时间
    LaunchedEffect(currentPeriod, autoScrollToCurrentTime) {
        if (autoScrollToCurrentTime && currentPeriod != null && currentPeriod > 0) {
            val currentPeriodInfo = periods.find { it.periodNumber == currentPeriod }
            if (currentPeriodInfo != null) {
                val minutesFromStart = java.time.temporal.ChronoUnit.MINUTES.between(startTime, currentPeriodInfo.startTime)
                val targetOffset = with(density) { (minutesFromStart * pixelsPerMinute.toPx()).toInt() }
                coroutineScope.launch {
                    scrollState.animateScrollTo(targetOffset)
                }
            }
        }
    }
    
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 固定头部: 左上角周次标签 + 日期头部
            Row(modifier = Modifier.fillMaxWidth()) {
                // 左上角显示周次（可点击）
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .height(config.headerHeight)
                        .clickable { onWeekLabelClick() },
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.material3.Text(
                        text = "${currentWeek}周",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 4.dp)
                ) {
                    WeekDateHeader(
                        semesterStartDate = semesterStartDate,
                        currentWeek = currentWeek,
                        weekDays = weekDays,
                        currentDay = currentDay,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            
            // 可滚动内容: 时间列 + 课程网格(使用Box+绝对定位)
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {
                // 左侧: 时间轴列（固定）
                TimeAxisColumn(
                    startTime = startTime,
                    endTime = endTime,
                    pixelsPerMinute = pixelsPerMinute,
                    modifier = Modifier.width(60.dp)
                )
                
                // 右侧: 使用Box实现时间轴背景+绝对定位课程卡片
                // 先计算总高度
                val totalMinutes = java.time.temporal.ChronoUnit.MINUTES.between(startTime, endTime)
                val totalHeight = pixelsPerMinute * totalMinutes.toInt()
                
                BoxWithConstraints(
                    modifier = Modifier
                        .weight(1f)
                        .height(totalHeight)  // 明确设置高度
                        .padding(start = 4.dp)
                ) {
                    val gridLineColor = MaterialTheme.colorScheme.outlineVariant
                    val cellSpacingPx = with(density) { 4.dp.toPx() }
                    val dayColumnWidth = (maxWidth - 4.dp * (weekDays.size - 1)) / weekDays.size
                    
                    // 绘制时间轴背景网格
                    val cellBackgroundColor = MaterialTheme.colorScheme.surfaceContainerLow
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(totalHeight)
                    ) {
                        val dayColumnWidthPx = (size.width - (weekDays.size - 1) * cellSpacingPx) / weekDays.size
                        val hourHeightPx = with(density) { (pixelsPerMinute * 60).toPx() }
                        
                        // 绘制每个小时的网格
                        var currentTime = startTime
                        var currentY = 0f
                        
                        while (currentTime.isBefore(endTime)) {
                            // 绘制每一列（每天）
                            weekDays.forEachIndexed { index, _ ->
                                val x = index * (dayColumnWidthPx + cellSpacingPx)
                                
                                // 绘制单元格背景
                                drawRect(
                                    color = cellBackgroundColor,
                                    topLeft = Offset(x, currentY),
                                    size = androidx.compose.ui.geometry.Size(dayColumnWidthPx, hourHeightPx)
                                )
                                
                                // 绘制单元格边框（淡色）
                                drawRect(
                                    color = gridLineColor.copy(alpha = 0.3f),
                                    topLeft = Offset(x, currentY),
                                    size = androidx.compose.ui.geometry.Size(dayColumnWidthPx, hourHeightPx),
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1f)
                                )
                            }
                            
                            currentY += hourHeightPx
                            currentTime = currentTime.plusHours(1)
                        }
                    }
                    
                    // 绘制课程卡片(基于实际时间定位)
                    weekDays.forEachIndexed { dayIndex, day ->
                        val coursesForDay = coursesByDay[day] ?: emptyList()
                        
                        coursesForDay.forEach { course ->
                            // 计算课程卡片的Y位置：从startTime到课程开始时间的分钟数
                            val minutesFromStart = java.time.temporal.ChronoUnit.MINUTES.between(startTime, course.startTime)
                            val cardOffsetY = pixelsPerMinute * minutesFromStart.toInt()
                            
                            // 计算课程卡片的高度：课程持续时间
                            val courseDurationMinutes = java.time.temporal.ChronoUnit.MINUTES.between(course.startTime, course.endTime)
                            val cardHeight = (pixelsPerMinute * courseDurationMinutes.toInt()).coerceAtLeast(40.dp) // 最小高度40dp
                            
                            // 调试日志
                            android.util.Log.d("GridTimetable", "Course: ${course.name}, Start: ${course.startTime}, End: ${course.endTime}, Duration: ${courseDurationMinutes}min, Height: $cardHeight")
                            
                            // 计算X位置
                            val cardOffsetX = (dayColumnWidth + 4.dp) * dayIndex
                            
                            // 使用Surface直接绘制课程卡片，避免高度计算问题
                            Surface(
                                modifier = Modifier
                                    .offset(x = cardOffsetX, y = cardOffsetY)
                                    .width(dayColumnWidth)
                                    .height(cardHeight)
                                    .clickable { onCourseClick(course.id) },
                                color = rememberThemeAwareCourseColor(course.color).copy(alpha = 0.9f),
                                shape = MaterialTheme.shapes.small
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(6.dp),
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = course.name,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                        color = Color.White,
                                        maxLines = 3,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                    if (!course.location.isNullOrBlank()) {
                                        Text(
                                            text = course.location,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White.copy(alpha = 0.9f),
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


/**
 * 时间轴列组件
 * 显示时间刻度（使用Box+绝对定位，不限制高度）
 */
@Composable
private fun TimeAxisColumn(
    startTime: java.time.LocalTime,
    endTime: java.time.LocalTime,
    pixelsPerMinute: Dp,
    modifier: Modifier = Modifier
) {
    val timeFormatter = java.time.format.DateTimeFormatter.ofPattern("HH:mm")
    val hourHeight = pixelsPerMinute * 60
    
    // 计算总高度
    val totalMinutes = java.time.temporal.ChronoUnit.MINUTES.between(startTime, endTime)
    val totalHeight = pixelsPerMinute * totalMinutes.toInt()
    
    Box(modifier = modifier.height(totalHeight)) {
        var currentTime = startTime
        var offsetY = 0.dp
        
        while (currentTime.isBefore(endTime) || currentTime == endTime) {
            androidx.compose.material3.Text(
                text = currentTime.format(timeFormatter),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .offset(y = offsetY)
                    .fillMaxWidth()
                    .wrapContentHeight(Alignment.Top)
            )
            
            offsetY += hourHeight
            currentTime = currentTime.plusHours(1)
        }
    }
}
