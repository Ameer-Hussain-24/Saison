# 课程单双周显示修复 - 设计文档

## 概述

本设计文档描述如何诊断和修复课程表中单双周课程显示的问题。核心策略是：
1. 添加详细的日志输出来诊断问题
2. 验证 `isCourseActiveInWeek` 方法的逻辑
3. 确保 CourseScreen 中的过滤逻辑正确执行
4. 验证周模式标识的显示

## 架构

### 当前实现分析

根据代码审查，当前的实现结构如下：

```
CourseViewModel
  ├─ allCourses: StateFlow<List<Course>>
  ├─ coursesByDay: StateFlow<Map<DayOfWeek, List<Course>>>
  ├─ currentWeek: StateFlow<Int>
  └─ isCourseActiveInWeek(course: Course, week: Int): Boolean

CourseScreen
  └─ HorizontalPager
      └─ 每一页
          ├─ 计算 pageWeek = (page - centerPage) + currentWeek
          ├─ 过滤课程: filteredCoursesByDay
          └─ ContinuousTimelineView(filteredCoursesByDay)
```

### 可能的问题点

1. **周数计算错误**: `pageWeek` 的计算可能不正确
2. **过滤逻辑错误**: `isCourseActiveInWeek` 方法可能有bug
3. **数据传递错误**: 过滤后的数据可能没有正确传递给 ContinuousTimelineView
4. **周模式数据错误**: 课程的 weekPattern 字段可能没有正确设置

## 诊断策略

### 1. 添加日志输出

在关键位置添加日志输出，帮助诊断问题：

```kotlin
// CourseScreen.kt - HorizontalPager 内部
HorizontalPager(state = pagerState) { page ->
    val pageWeek = (page - centerPage) + currentWeek
    
    // 添加日志
    Log.d("CourseScreen", "=== Page $page ===")
    Log.d("CourseScreen", "centerPage: $centerPage, currentWeek: $currentWeek")
    Log.d("CourseScreen", "Calculated pageWeek: $pageWeek")
    
    // 过滤前的课程数量
    val totalCoursesBefore = coursesByDay.values.flatten().size
    Log.d("CourseScreen", "Total courses before filter: $totalCoursesBefore")
    
    // 过滤课程
    val filteredCoursesByDay = coursesByDay.mapValues { (day, courses) ->
        courses.filter { course ->
            val isActive = viewModel.isCourseActiveInWeek(course, pageWeek)
            Log.d("CourseScreen", "Course: ${course.name}, Day: $day, Pattern: ${course.weekPattern}, Week: $pageWeek, Active: $isActive")
            isActive
        }
    }.filterValues { it.isNotEmpty() }
    
    // 过滤后的课程数量
    val totalCoursesAfter = filteredCoursesByDay.values.flatten().size
    Log.d("CourseScreen", "Total courses after filter: $totalCoursesAfter")
    
    // ... 渲染UI
}
```

```kotlin
// CourseViewModel.kt - isCourseActiveInWeek 方法
fun isCourseActiveInWeek(course: Course, week: Int): Boolean {
    val result = when (course.weekPattern) {
        WeekPattern.ALL -> true
        WeekPattern.ODD -> week % 2 == 1
        WeekPattern.EVEN -> week % 2 == 0
        WeekPattern.CUSTOM -> course.customWeeks?.contains(week) ?: false
        WeekPattern.A, WeekPattern.B -> true // 简化处理
    }
    
    Log.d("CourseViewModel", "isCourseActiveInWeek: ${course.name}, pattern=${course.weekPattern}, week=$week, result=$result")
    
    return result
}
```

### 2. 验证周数计算

确保当前周数的计算是正确的：

```kotlin
// CourseViewModel.kt - currentWeek 计算
val currentWeek: StateFlow<Int> = combine(
    courseSettings,
    _weekOffset
) { settings, offset ->
    val baseWeek = getCurrentWeekNumber(settings.semesterStartDate)
    val calculatedWeek = (baseWeek + offset).coerceIn(1, settings.totalWeeks)
    
    Log.d("CourseViewModel", "Week calculation: baseWeek=$baseWeek, offset=$offset, result=$calculatedWeek")
    
    calculatedWeek
}.stateIn(...)
```

### 3. 创建测试课程

为了验证过滤逻辑，建议创建以下测试课程：

- 课程A: 周一 8:00-10:00, WeekPattern.ALL
- 课程B: 周一 10:00-12:00, WeekPattern.ODD
- 课程C: 周一 14:00-16:00, WeekPattern.EVEN
- 课程D: 周二 8:00-10:00, WeekPattern.CUSTOM, customWeeks=[1,3,5,7]

预期结果：
- 第1周（单周）：应显示 A, B, D
- 第2周（双周）：应显示 A, C
- 第3周（单周）：应显示 A, B, D
- 第4周（双周）：应显示 A, C

## 修复方案

### 方案1: 修复 isCourseActiveInWeek 方法

如果发现 `isCourseActiveInWeek` 方法有问题，修复逻辑：

```kotlin
fun isCourseActiveInWeek(course: Course, week: Int): Boolean {
    return when (course.weekPattern) {
        WeekPattern.ALL -> {
            // 全周课程，总是显示
            true
        }
        WeekPattern.ODD -> {
            // 单周课程，只在奇数周显示
            week % 2 == 1
        }
        WeekPattern.EVEN -> {
            // 双周课程，只在偶数周显示
            week % 2 == 0
        }
        WeekPattern.CUSTOM -> {
            // 自定义周课程，检查是否在指定周数列表中
            course.customWeeks?.contains(week) ?: false
        }
        WeekPattern.A -> {
            // A周模式 - 需要根据实际需求实现
            // 暂时简化处理，总是显示
            true
        }
        WeekPattern.B -> {
            // B周模式 - 需要根据实际需求实现
            // 暂时简化处理，总是显示
            true
        }
    }
}
```

### 方案2: 修复周数计算

如果发现周数计算有问题，修复计算逻辑：

```kotlin
private fun getCurrentWeekNumber(semesterStartDate: LocalDate?): Int {
    val today = LocalDate.now()
    val startDate = semesterStartDate ?: LocalDate.of(today.year, 1, 1)
    
    // 使用 WeekCalculator 计算周数
    val week = weekCalculator.calculateCurrentWeek(startDate, today)
    
    Log.d("CourseViewModel", "getCurrentWeekNumber: startDate=$startDate, today=$today, week=$week")
    
    return week
}
```

### 方案3: 修复过滤逻辑

如果发现过滤逻辑有问题，确保正确过滤：

```kotlin
// 在 HorizontalPager 中
val filteredCoursesByDay = coursesByDay.mapValues { (_, courses) ->
    courses.filter { course ->
        viewModel.isCourseActiveInWeek(course, pageWeek)
    }
}.filterValues { it.isNotEmpty() }

// 确保传递过滤后的数据
ContinuousTimelineView(
    coursesByDay = filteredCoursesByDay,  // 使用过滤后的数据
    onCourseClick = { courseId -> ... },
    timelineCompactness = courseSettings.timelineCompactness,
    firstPeriodStartTime = courseSettings.firstPeriodStartTime
)
```

### 方案4: 增强周模式标识显示

确保周模式标识正确显示：

```kotlin
// ContinuousTimelineView.kt - CourseBlock 组件
@Composable
private fun CourseBlock(...) {
    // ... 其他代码
    
    // 周模式标识
    if (course.weekPattern != WeekPattern.ALL) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = course.name, ...)
            
            // 周模式标识
            Surface(
                color = Color.White.copy(alpha = 0.3f),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = when (course.weekPattern) {
                        WeekPattern.ODD -> "单周"
                        WeekPattern.EVEN -> "双周"
                        WeekPattern.A -> "A周"
                        WeekPattern.B -> "B周"
                        WeekPattern.CUSTOM -> "自定义"
                        else -> ""
                    },
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 8.sp,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
        }
    }
}
```

## 测试策略

### 1. 手动测试步骤

1. 打开应用，查看 Logcat 输出
2. 记录当前周数
3. 检查显示的课程是否符合预期
4. 左右滑动切换周数，观察课程变化
5. 验证单周课程只在单周显示
6. 验证双周课程只在双周显示
7. 验证周模式标识是否正确显示

### 2. 日志分析

查看 Logcat 输出，确认：
- 当前周数计算正确
- pageWeek 计算正确
- isCourseActiveInWeek 返回值正确
- 过滤前后的课程数量变化合理

### 3. 单元测试

创建单元测试验证 `isCourseActiveInWeek` 方法：

```kotlin
@Test
fun testOddWeekPattern() {
    val course = Course(
        name = "Test Course",
        weekPattern = WeekPattern.ODD,
        // ... 其他字段
    )
    
    // 单周应该显示
    assertTrue(viewModel.isCourseActiveInWeek(course, 1))
    assertTrue(viewModel.isCourseActiveInWeek(course, 3))
    assertTrue(viewModel.isCourseActiveInWeek(course, 5))
    
    // 双周不应该显示
    assertFalse(viewModel.isCourseActiveInWeek(course, 2))
    assertFalse(viewModel.isCourseActiveInWeek(course, 4))
    assertFalse(viewModel.isCourseActiveInWeek(course, 6))
}

@Test
fun testEvenWeekPattern() {
    val course = Course(
        name = "Test Course",
        weekPattern = WeekPattern.EVEN,
        // ... 其他字段
    )
    
    // 双周应该显示
    assertTrue(viewModel.isCourseActiveInWeek(course, 2))
    assertTrue(viewModel.isCourseActiveInWeek(course, 4))
    assertTrue(viewModel.isCourseActiveInWeek(course, 6))
    
    // 单周不应该显示
    assertFalse(viewModel.isCourseActiveInWeek(course, 1))
    assertFalse(viewModel.isCourseActiveInWeek(course, 3))
    assertFalse(viewModel.isCourseActiveInWeek(course, 5))
}
```

## 实现顺序

1. 添加日志输出到 CourseScreen 和 CourseViewModel
2. 运行应用，查看日志，诊断问题
3. 根据日志分析结果，确定问题所在
4. 实施相应的修复方案
5. 验证修复效果
6. 移除或注释掉调试日志
7. 编写单元测试确保问题不再出现

## 错误处理

### 1. 周数为0或负数

```kotlin
fun isCourseActiveInWeek(course: Course, week: Int): Boolean {
    // 确保周数有效
    if (week < 1) {
        Log.w("CourseViewModel", "Invalid week number: $week")
        return false
    }
    
    // ... 正常逻辑
}
```

### 2. customWeeks 为 null

```kotlin
WeekPattern.CUSTOM -> {
    val customWeeks = course.customWeeks
    if (customWeeks == null || customWeeks.isEmpty()) {
        Log.w("CourseViewModel", "Course ${course.name} has CUSTOM pattern but no customWeeks")
        return false
    }
    customWeeks.contains(week)
}
```

### 3. 学期开始日期未设置

```kotlin
private fun getCurrentWeekNumber(semesterStartDate: LocalDate?): Int {
    if (semesterStartDate == null) {
        Log.w("CourseViewModel", "Semester start date not set, using year start")
    }
    
    val today = LocalDate.now()
    val startDate = semesterStartDate ?: LocalDate.of(today.year, 1, 1)
    
    return weekCalculator.calculateCurrentWeek(startDate, today)
}
```
