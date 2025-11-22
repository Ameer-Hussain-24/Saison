# 课程表单双周显示修复 - 设计文档

## 概述

本设计文档描述了如何重构课程表显示区域，实现准确的单双周过滤和智能紧凑显示。核心思路是：
1. 在 CourseScreen 层面统一进行周数过滤
2. 在 ContinuousTimelineView 中实现智能紧凑布局算法
3. 优化时间轴计算和视觉呈现

## 架构

### 组件层次结构

```
CourseScreen (过滤层)
  └─ HorizontalPager
      └─ ContinuousTimelineView (显示层)
          ├─ TimelineHeader (星期标题)
          ├─ CompactTimelineContent (紧凑时间轴内容)
          │   ├─ TimeColumn (时间列)
          │   └─ DayColumn × 7 (每天的课程列)
          │       └─ CourseBlock × N (课程卡片)
```

### 数据流

```
CourseViewModel.allCourses
  ↓
CourseViewModel.isCourseActiveInWeek(course, week)
  ↓
filteredCoursesByDay (按周过滤)
  ↓
ContinuousTimelineView (接收已过滤数据)
  ↓
calculateCompactTimeSlots() (计算紧凑时间段)
  ↓
渲染课程表
```

## 核心算法

### 1. 周数过滤算法

在 `CourseScreen` 的 `HorizontalPager` 中实现：

```kotlin
// 计算当前页对应的周数
val pageWeek = (page - centerPage) + currentWeek

// 过滤课程
val filteredCoursesByDay = coursesByDay.mapValues { (_, courses) ->
    courses.filter { course ->
        viewModel.isCourseActiveInWeek(course, pageWeek)
    }
}.filterValues { it.isNotEmpty() }
```

`isCourseActiveInWeek` 方法逻辑：
- `WeekPattern.ALL`: 总是返回 true
- `WeekPattern.ODD`: 返回 `week % 2 == 1`
- `WeekPattern.EVEN`: 返回 `week % 2 == 0`
- `WeekPattern.CUSTOM`: 返回 `course.customWeeks?.contains(week) ?: false`
- `WeekPattern.A/B`: 根据具体实现返回

### 2. 智能紧凑显示算法

#### 2.1 时间段分析

```kotlin
data class TimeSlot(
    val hour: Int,                    // 小时数
    val hasAnyCourse: Boolean,        // 是否有任何课程
    val courseCount: Int,             // 课程数量
    val displayHeight: Dp             // 显示高度
)

fun analyzeTimeSlots(
    coursesByDay: Map<DayOfWeek, List<Course>>,
    startHour: Int,
    endHour: Int
): List<TimeSlot> {
    val slots = mutableListOf<TimeSlot>()
    
    for (hour in startHour..endHour) {
        // 检查这个小时在一周内是否有课程
        val hasAnyCourse = coursesByDay.values.any { courses ->
            courses.any { course ->
                course.startTime.hour <= hour && course.endTime.hour >= hour
            }
        }
        
        // 计算课程数量
        val courseCount = coursesByDay.values.sumOf { courses ->
            courses.count { course ->
                course.startTime.hour <= hour && course.endTime.hour >= hour
            }
        }
        
        // 根据是否有课程决定显示高度
        val displayHeight = if (hasAnyCourse) {
            60.dp * timelineCompactness  // 正常高度
        } else {
            20.dp  // 压缩高度（仅显示时间标签）
        }
        
        slots.add(TimeSlot(hour, hasAnyCourse, courseCount, displayHeight))
    }
    
    return slots
}
```

#### 2.2 课程位置计算

```kotlin
data class CoursePosition(
    val offsetFromTop: Dp,      // 距离顶部的偏移
    val height: Dp              // 课程高度
)

fun calculateCoursePosition(
    course: Course,
    timeSlots: List<TimeSlot>,
    startHour: Int,
    pixelsPerMinute: Dp
): CoursePosition {
    // 计算课程开始时间之前的累积高度
    var offsetFromTop = 0.dp
    
    for (slot in timeSlots) {
        if (slot.hour < course.startTime.hour) {
            // 完全在课程开始之前的时间段
            offsetFromTop += slot.displayHeight
        } else if (slot.hour == course.startTime.hour) {
            // 课程开始的时间段，需要加上分钟偏移
            val minuteOffset = course.startTime.minute
            if (slot.hasAnyCourse) {
                offsetFromTop += pixelsPerMinute * minuteOffset
            } else {
                // 如果这个时间段被压缩，按比例计算偏移
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
        
        val minutesInThisSlot = if (currentHour == course.startTime.hour) {
            60 - course.startTime.minute
        } else {
            60.coerceAtMost(remainingMinutes)
        }
        
        if (slot.hasAnyCourse) {
            totalHeight += pixelsPerMinute * minutesInThisSlot
        } else {
            totalHeight += (slot.displayHeight * minutesInThisSlot) / 60
        }
        
        remainingMinutes -= minutesInThisSlot
        currentHour++
    }
    
    return CoursePosition(offsetFromTop, totalHeight)
}
```

### 3. 时间轴范围计算

```kotlin
data class TimeRange(
    val startHour: Int,
    val endHour: Int
)

fun calculateOptimalTimeRange(
    coursesByDay: Map<DayOfWeek, List<Course>>,
    firstPeriodStartTime: LocalTime
): TimeRange {
    val allCourses = coursesByDay.values.flatten()
    
    if (allCourses.isEmpty()) {
        // 没有课程时使用默认范围
        return TimeRange(8, 18)
    }
    
    // 找出最早和最晚的课程时间
    val earliestCourseTime = allCourses.minOf { it.startTime }
    val latestCourseTime = allCourses.maxOf { it.endTime }
    
    // 使用第一节课开始时间或最早课程时间（取较早的）
    val earliestTime = if (earliestCourseTime < firstPeriodStartTime) {
        earliestCourseTime
    } else {
        firstPeriodStartTime
    }
    
    // 调整到整点
    val startHour = earliestTime.hour
    val endHour = if (latestCourseTime.minute > 0) {
        latestCourseTime.hour + 1
    } else {
        latestCourseTime.hour
    }
    
    return TimeRange(startHour, endHour)
}
```

## 组件设计

### 1. CourseScreen 修改

**职责：**
- 管理 HorizontalPager 状态
- 为每一页过滤对应周数的课程
- 传递已过滤的课程数据给 ContinuousTimelineView

**关键修改：**
```kotlin
HorizontalPager(state = pagerState) { page ->
    val pageWeek = (page - centerPage) + currentWeek
    
    // 在这里进行周数过滤
    val filteredCoursesByDay = coursesByDay.mapValues { (_, courses) ->
        courses.filter { course ->
            viewModel.isCourseActiveInWeek(course, pageWeek)
        }
    }.filterValues { it.isNotEmpty() }
    
    if (filteredCoursesByDay.isEmpty()) {
        EmptyCourseList()
    } else {
        ContinuousTimelineView(
            coursesByDay = filteredCoursesByDay,  // 传递已过滤的数据
            onCourseClick = { courseId -> ... },
            timelineCompactness = courseSettings.timelineCompactness,
            firstPeriodStartTime = courseSettings.firstPeriodStartTime
        )
    }
}
```

### 2. ContinuousTimelineView 重构

**职责：**
- 接收已过滤的课程数据
- 计算智能紧凑布局
- 渲染课程表

**新增组件：**

#### CompactTimelineContent
```kotlin
@Composable
private fun CompactTimelineContent(
    timeSlots: List<TimeSlot>,
    weekDays: List<DayOfWeek>,
    coursesByDay: Map<DayOfWeek, List<Course>>,
    onCourseClick: (Long) -> Unit,
    pixelsPerMinute: Dp
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // 时间列
            CompactTimeColumn(
                timeSlots = timeSlots,
                modifier = Modifier.width(50.dp)
            )
            
            // 每天的课程列
            weekDays.forEach { day ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 2.dp)
                ) {
                    CompactDayColumn(
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
        CompactTimeGrid(
            timeSlots = timeSlots,
            modifier = Modifier.fillMaxWidth().padding(start = 50.dp)
        )
    }
}
```

#### CompactTimeColumn
```kotlin
@Composable
private fun CompactTimeColumn(
    timeSlots: List<TimeSlot>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
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
}
```

#### CompactDayColumn
```kotlin
@Composable
private fun CompactDayColumn(
    day: DayOfWeek,
    courses: List<Course>,
    timeSlots: List<TimeSlot>,
    pixelsPerMinute: Dp,
    onCourseClick: (Long) -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        courses.forEach { course ->
            val position = calculateCoursePosition(
                course = course,
                timeSlots = timeSlots,
                startHour = timeSlots.first().hour,
                pixelsPerMinute = pixelsPerMinute
            )
            
            CompactCourseBlock(
                course = course,
                position = position,
                onClick = { onCourseClick(course.id) }
            )
        }
    }
}
```

### 3. CourseBlock 视觉优化

**改进点：**
- 更清晰的信息层次
- 优化字体大小和间距
- 添加周模式标识
- 改进颜色对比度

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CompactCourseBlock(
    course: Course,
    position: CoursePosition,
    onClick: () -> Unit
) {
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    val courseColor = Color(course.color)
    val backgroundColor = courseColor.copy(alpha = 0.9f)
    
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .offset(y = position.offsetFromTop)
            .height(position.height)
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
            // 顶部：课程名称和周模式标识
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = course.name,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 12.sp,
                    modifier = Modifier.weight(1f)
                )
                
                // 周模式标识
                if (course.weekPattern != WeekPattern.ALL) {
                    Surface(
                        color = Color.White.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.padding(start = 4.dp)
                    ) {
                        Text(
                            text = getWeekPatternText(course.weekPattern.name),
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 8.sp,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            
            // 底部：详细信息
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                // 时间
                Text(
                    text = "${course.startTime.format(timeFormatter)}-${course.endTime.format(timeFormatter)}",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp
                )
                
                // 地点
                course.location?.let { location ->
                    Text(
                        text = "@$location",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                // 教师
                course.instructor?.let { instructor ->
                    Text(
                        text = instructor,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
```

## 数据模型

### TimeSlot
```kotlin
data class TimeSlot(
    val hour: Int,                    // 小时数 (0-23)
    val hasAnyCourse: Boolean,        // 是否有任何课程
    val courseCount: Int,             // 课程数量
    val displayHeight: Dp             // 显示高度
)
```

### CoursePosition
```kotlin
data class CoursePosition(
    val offsetFromTop: Dp,      // 距离顶部的偏移
    val height: Dp              // 课程高度
)
```

### TimeRange
```kotlin
data class TimeRange(
    val startHour: Int,         // 起始小时
    val endHour: Int            // 结束小时
)
```

## 错误处理

### 1. 空课程列表
- 显示 EmptyCourseList 组件
- 提示用户添加课程

### 2. 无效周数
- 确保周数在 1 到 totalWeeks 范围内
- 使用 coerceIn 限制周数范围

### 3. 时间计算异常
- 确保 startTime < endTime
- 处理跨天课程（如果存在）
- 提供默认时间范围

## 性能优化

### 1. 计算缓存
- 缓存 timeSlots 计算结果
- 使用 remember 缓存课程位置计算

### 2. 重组优化
- 使用 key 参数优化列表渲染
- 避免不必要的重组

### 3. 布局优化
- 使用 BoxWithConstraints 进行精确布局
- 避免嵌套过深的布局结构

## 测试策略

### 单元测试
1. 测试 `isCourseActiveInWeek` 方法
   - 测试所有 WeekPattern 类型
   - 测试边界情况

2. 测试 `analyzeTimeSlots` 方法
   - 测试空课程列表
   - 测试有课程的情况
   - 测试全天无课的情况

3. 测试 `calculateCoursePosition` 方法
   - 测试正常课程
   - 测试跨多个时间段的课程
   - 测试压缩时间段中的课程

### UI 测试
1. 测试周数切换
   - 验证课程过滤正确性
   - 验证单双周显示

2. 测试紧凑显示
   - 验证空白时间段被压缩
   - 验证课程位置正确

3. 测试视觉效果
   - 验证课程卡片显示
   - 验证周模式标识显示

## 实现顺序

1. 修改 CourseScreen 的过滤逻辑
2. 实现 TimeSlot 分析算法
3. 实现 CoursePosition 计算算法
4. 重构 ContinuousTimelineView
5. 优化 CourseBlock 视觉效果
6. 添加单元测试
7. 进行 UI 测试和调优
