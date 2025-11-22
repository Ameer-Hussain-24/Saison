# 课程表网格布局重构 - 设计文档

## 概述

本设计文档描述了课程表界面的完全重构方案，实现与参考UI一致的网格布局。新设计将采用固定头部的网格视图，包含周次选择器、日期头部、时间轴、休息时段分隔以及可编辑的课程卡片。

## 架构

### 组件层次结构

```
CourseScreen (主屏幕)
├── WeekSelectorButton (周次选择按钮)
├── WeekSelectorBottomSheet (周次选择抽屉)
├── GridTimetableView (重构后的网格课程表)
│   ├── WeekDateHeader (周一到周日日期头部)
│   ├── PeriodTimeColumn (节次时间列)
│   ├── BreakSeparatorRow (休息时段分隔行)
│   └── CourseGridCell (课程网格单元格)
│       ├── CourseCardCompact (紧凑课程卡片)
│       └── EmptyCell (空白可点击单元格)
└── EditCourseSheet (课程编辑面板)
```

### 数据流

```
CourseViewModel
├── currentWeek: StateFlow<Int>
├── selectedWeek: StateFlow<Int>
├── coursesByDay: StateFlow<Map<DayOfWeek, List<Course>>>
├── periods: StateFlow<List<CoursePeriod>>
├── breakPeriods: StateFlow<List<BreakPeriod>>
└── courseSettings: StateFlow<CourseSettings>
```

## 组件和接口

### 1. WeekSelectorButton

**职责**: 显示当前周次并触发周次选择抽屉

**接口**:
```kotlin
@Composable
fun WeekSelectorButton(
    currentWeek: Int,
    totalWeeks: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
)
```

**UI规格**:
- 位置: 左上角固定
- 显示格式: "X周" (如 "11周")
- 样式: Material 3 FilledTonalButton
- 尺寸: 紧凑，适合单行显示
- 交互: 点击打开底部抽屉

### 2. WeekSelectorBottomSheet

**职责**: 提供周次选择界面

**接口**:
```kotlin
@Composable
fun WeekSelectorBottomSheet(
    currentWeek: Int,
    totalWeeks: Int,
    onWeekSelected: (Int) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
)
```

**UI规格**:
- 类型: ModalBottomSheet
- 内容: 垂直滚动的周次列表
- 列表项: 阿拉伯数字 (1, 2, 3, ...)
- 当前周: 高亮显示
- 交互: 点击选择，自动关闭

**实现细节**:
```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeekSelectorBottomSheet(
    currentWeek: Int,
    totalWeeks: Int,
    onWeekSelected: (Int) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            items(totalWeeks) { index ->
                val week = index + 1
                ListItem(
                    headlineContent = {
                        Text(
                            text = "第 $week 周",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (week == currentWeek) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onWeekSelected(week)
                            onDismiss()
                        },
                    colors = ListItemDefaults.colors(
                        containerColor = if (week == currentWeek) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            Color.Transparent
                        }
                    )
                )
            }
        }
    }
}
```

### 3. WeekDateHeader (重构)

**职责**: 显示周一到周日的日期头部

**接口**:
```kotlin
@Composable
fun WeekDateHeader(
    semesterStartDate: LocalDate,
    currentWeek: Int,
    weekDays: List<DayOfWeek>,
    currentDay: DayOfWeek?,
    modifier: Modifier = Modifier
)
```

**UI规格**:
- 布局: 水平排列，均分宽度
- 每列显示:
  - 第一行: 星期几 (一、二、三...)
  - 第二行: 日期 (11/3, 11/4...)
- 当天高亮: 使用主题色背景
- 固定位置: 滚动时保持在顶部

**实现细节**:
```kotlin
@Composable
fun WeekDateHeader(
    semesterStartDate: LocalDate,
    currentWeek: Int,
    weekDays: List<DayOfWeek>,
    currentDay: DayOfWeek?,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        weekDays.forEach { day ->
            val date = getDateForDayInWeek(semesterStartDate, currentWeek, day)
            val isToday = date == LocalDate.now()
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .background(
                        if (isToday) MaterialTheme.colorScheme.primaryContainer
                        else Color.Transparent,
                        shape = MaterialTheme.shapes.small
                    )
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = getDayShortName(day), // "一", "二", "三"...
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isToday) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${date.monthValue}/${date.dayOfMonth}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isToday) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun getDayShortName(day: DayOfWeek): String {
    return when (day) {
        DayOfWeek.MONDAY -> "一"
        DayOfWeek.TUESDAY -> "二"
        DayOfWeek.WEDNESDAY -> "三"
        DayOfWeek.THURSDAY -> "四"
        DayOfWeek.FRIDAY -> "五"
        DayOfWeek.SATURDAY -> "六"
        DayOfWeek.SUNDAY -> "日"
    }
}

private fun getDateForDayInWeek(
    semesterStartDate: LocalDate,
    week: Int,
    day: DayOfWeek
): LocalDate {
    val weekStartDate = semesterStartDate.plusWeeks((week - 1).toLong())
    val mondayOfWeek = weekStartDate.with(java.time.temporal.TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    return mondayOfWeek.plusDays((day.value - 1).toLong())
}
```

### 4. PeriodTimeColumn (重构)

**职责**: 显示节次和时间信息

**接口**:
```kotlin
@Composable
fun PeriodTimeColumn(
    periods: List<CoursePeriod>,
    breakPeriods: List<BreakPeriod>,
    currentPeriod: Int?,
    cellHeight: Dp,
    modifier: Modifier = Modifier
)
```

**UI规格**:
- 布局: 垂直排列
- 每个单元格显示:
  - 第一行: 节次号 (1, 2, 3...)
  - 第二行: 开始时间 (08:30)
  - 第三行: 结束时间 (09:15)
- 固定宽度: 60dp
- 不可编辑
- 当前节次: 高亮显示
- 固定位置: 滚动时保持在左侧

**实现细节**:
```kotlin
@Composable
fun PeriodTimeColumn(
    periods: List<CoursePeriod>,
    breakPeriods: List<BreakPeriod>,
    currentPeriod: Int?,
    cellHeight: Dp,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.width(60.dp)) {
        periods.forEach { period ->
            // 检查是否需要在此节次前插入休息分隔
            val breakBefore = breakPeriods.find { it.afterPeriod == period.periodNumber - 1 }
            
            if (breakBefore != null) {
                BreakSeparatorCell(
                    breakName = breakBefore.name,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            PeriodTimeCell(
                period = period,
                cellHeight = cellHeight,
                isCurrentPeriod = currentPeriod == period.periodNumber,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp)
            )
        }
    }
}

@Composable
private fun PeriodTimeCell(
    period: CoursePeriod,
    cellHeight: Dp,
    isCurrentPeriod: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.height(cellHeight),
        color = if (isCurrentPeriod) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.extraSmall
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = period.periodNumber.toString(),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = if (isCurrentPeriod) MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = period.startTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                style = MaterialTheme.typography.labelSmall,
                color = if (isCurrentPeriod) MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = period.endTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                style = MaterialTheme.typography.labelSmall,
                color = if (isCurrentPeriod) MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
```

### 5. BreakSeparatorRow

**职责**: 显示休息时段分隔（午休、晚修）

**接口**:
```kotlin
@Composable
fun BreakSeparatorRow(
    breakName: String,
    weekDays: List<DayOfWeek>,
    modifier: Modifier = Modifier
)
```

**UI规格**:
- 布局: 横跨所有日期列
- 显示: 居中文本 ("午休" 或 "晚修")
- 高度: 32dp
- 背景: surfaceContainer
- 文字: onSurfaceVariant

**实现细节**:
```kotlin
@Composable
fun BreakSeparatorRow(
    breakName: String,
    weekDays: List<DayOfWeek>,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(32.dp),
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = breakName,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun BreakSeparatorCell(
    breakName: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.height(32.dp),
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = breakName,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
```

### 6. CourseGridCell

**职责**: 显示单个网格单元格（课程或空白）

**接口**:
```kotlin
@Composable
fun CourseGridCell(
    course: Course?,
    period: CoursePeriod,
    day: DayOfWeek,
    cellHeight: Dp,
    onCourseClick: (Long) -> Unit,
    onEmptyCellClick: (DayOfWeek, Int) -> Unit,
    modifier: Modifier = Modifier
)
```

**UI规格**:
- 有课程时: 显示紧凑课程卡片
- 无课程时: 显示空白可点击区域
- 边框: 细线分隔
- 点击: 触发相应回调

### 7. CourseCardCompact

**职责**: 在网格中显示紧凑的课程信息

**接口**:
```kotlin
@Composable
fun CourseCardCompact(
    course: Course,
    cellHeight: Dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
)
```

**UI规格**:
- 显示内容:
  - 课程名称（粗体）
  - 地点（小字）
- 背景: 课程主题色
- 圆角: small
- 内边距: 4dp
- 文字: 自动截断

**实现细节**:
```kotlin
@Composable
fun CourseCardCompact(
    course: Course,
    cellHeight: Dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(cellHeight)
            .clickable(onClick = onClick),
        color = Color(course.color).copy(alpha = 0.9f),
        shape = MaterialTheme.shapes.small
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = course.name,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (!course.location.isNullOrBlank()) {
                Text(
                    text = course.location,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.9f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
```

### 8. GridTimetableView (完全重构)

**职责**: 整合所有组件，实现完整的网格课程表

**接口**:
```kotlin
@Composable
fun GridTimetableView(
    coursesByDay: Map<DayOfWeek, List<Course>>,
    periods: List<CoursePeriod>,
    breakPeriods: List<BreakPeriod>,
    semesterStartDate: LocalDate,
    currentWeek: Int,
    onCourseClick: (Long) -> Unit,
    onEmptyCellClick: (DayOfWeek, Int) -> Unit,
    currentPeriod: Int?,
    currentDay: DayOfWeek?,
    config: GridLayoutConfig,
    weekDays: List<DayOfWeek>,
    modifier: Modifier = Modifier
)
```

**布局结构**:
```
┌─────────────────────────────────────────┐
│ [周次] │  一  │  二  │  三  │  四  │  五  │ ← 固定头部
│        │ 11/3 │ 11/4 │ 11/5 │ 11/6 │ 11/7 │
├─────────────────────────────────────────┤
│   1    │      │      │      │      │      │
│ 08:30  │      │      │      │      │      │ ← 可滚动内容
│ 09:15  │      │      │      │      │      │
├─────────────────────────────────────────┤
│   2    │      │      │      │      │      │
│ 09:25  │      │      │      │      │      │
│ 10:10  │      │      │      │      │      │
├─────────────────────────────────────────┤
│              午休                        │ ← 分隔行
├─────────────────────────────────────────┤
│   5    │      │      │      │      │      │
│ 14:00  │      │      │      │      │      │
│ 14:45  │      │      │      │      │      │
├─────────────────────────────────────────┤
│              晚修                        │ ← 分隔行
├─────────────────────────────────────────┤
│   8    │      │      │      │      │      │
│ 16:45  │      │      │      │      │      │
│ 17:30  │      │      │      │      │      │
└─────────────────────────────────────────┘
```

**实现细节**:
```kotlin
@Composable
fun GridTimetableView(
    coursesByDay: Map<DayOfWeek, List<Course>>,
    periods: List<CoursePeriod>,
    breakPeriods: List<BreakPeriod>,
    semesterStartDate: LocalDate,
    currentWeek: Int,
    onCourseClick: (Long) -> Unit,
    onEmptyCellClick: (DayOfWeek, Int) -> Unit,
    currentPeriod: Int?,
    currentDay: DayOfWeek?,
    config: GridLayoutConfig,
    weekDays: List<DayOfWeek>,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    
    Column(modifier = modifier.fillMaxSize()) {
        // 固定头部
        Row(modifier = Modifier.fillMaxWidth()) {
            // 左上角占位符（与时间列对齐）
            Spacer(modifier = Modifier.width(60.dp).height(config.headerHeight))
            
            // 日期头部
            WeekDateHeader(
                semesterStartDate = semesterStartDate,
                currentWeek = currentWeek,
                weekDays = weekDays,
                currentDay = currentDay,
                modifier = Modifier.weight(1f)
            )
        }
        
        // 可滚动内容
        Row(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // 左侧时间列（固定）
            PeriodTimeColumn(
                periods = periods,
                breakPeriods = breakPeriods,
                currentPeriod = currentPeriod,
                cellHeight = config.cellHeight,
                modifier = Modifier.width(60.dp)
            )
            
            // 右侧课程网格
            Column(modifier = Modifier.weight(1f)) {
                periods.forEach { period ->
                    // 检查是否需要在此节次前插入休息分隔
                    val breakBefore = breakPeriods.find { it.afterPeriod == period.periodNumber - 1 }
                    
                    if (breakBefore != null) {
                        BreakSeparatorRow(
                            breakName = breakBefore.name,
                            weekDays = weekDays,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    
                    // 课程行
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        weekDays.forEach { day ->
                            val coursesForDay = coursesByDay[day] ?: emptyList()
                            val courseForPeriod = coursesForDay.find { course ->
                                course.periodStart == period.periodNumber
                            }
                            
                            CourseGridCell(
                                course = courseForPeriod,
                                period = period,
                                day = day,
                                cellHeight = config.cellHeight,
                                onCourseClick = onCourseClick,
                                onEmptyCellClick = onEmptyCellClick,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}
```

## 数据模型

### BreakPeriod

```kotlin
data class BreakPeriod(
    val name: String,           // "午休" 或 "晚修"
    val afterPeriod: Int        // 在第几节课后显示
)
```

### GridLayoutConfig (扩展)

```kotlin
data class GridLayoutConfig(
    val cellHeight: Dp = 80.dp,
    val headerHeight: Dp = 56.dp,
    val showBreakSeparators: Boolean = true
)
```

### CourseSettings (扩展)

```kotlin
data class CourseSettings(
    // ... 现有字段 ...
    val breakPeriods: List<BreakPeriod> = listOf(
        BreakPeriod("午休", afterPeriod = 4),
        BreakPeriod("晚修", afterPeriod = 7)
    )
)
```

## 错误处理

1. **无学期数据**: 显示提示创建学期
2. **无节次数据**: 显示提示配置节次
3. **日期计算错误**: 使用默认当前周
4. **课程冲突**: 在同一单元格显示多个课程时，堆叠显示

## 测试策略

### 单元测试

1. 日期计算逻辑测试
   - 测试 `getDateForDayInWeek` 函数
   - 验证不同周次的日期计算

2. 休息时段插入逻辑测试
   - 验证分隔行在正确位置显示

3. 课程过滤逻辑测试
   - 验证周次过滤正确性

### UI测试

1. 周次选择器测试
   - 验证点击打开抽屉
   - 验证选择周次后更新

2. 网格布局测试
   - 验证固定头部在滚动时保持位置
   - 验证课程卡片正确显示

3. 交互测试
   - 验证点击课程卡片打开编辑
   - 验证点击空白单元格打开添加

## 性能优化

1. **懒加载**: 使用 `LazyColumn` 优化长列表
2. **记忆化**: 使用 `remember` 缓存计算结果
3. **状态提升**: 避免不必要的重组
4. **滚动优化**: 使用共享 `ScrollState` 实现同步滚动

## 可访问性

1. **语义标签**: 为所有可点击元素添加 `contentDescription`
2. **对比度**: 确保文字和背景对比度符合 WCAG 标准
3. **触摸目标**: 确保所有可点击区域至少 48dp
4. **屏幕阅读器**: 支持 TalkBack 导航

## 国际化

1. 星期名称: 使用 `strings.xml` 资源
2. 日期格式: 根据系统语言自动调整
3. 休息时段名称: 支持多语言配置

## 迁移策略

1. **保留现有数据**: 不修改数据库结构
2. **渐进式重构**: 先实现新组件，再替换旧组件
3. **功能对等**: 确保新UI支持所有现有功能
4. **用户测试**: 在正式发布前进行用户测试
