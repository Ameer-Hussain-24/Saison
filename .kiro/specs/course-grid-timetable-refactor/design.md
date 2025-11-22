# 课程表网格化重构设计文档

## 概述

本设计文档描述了如何将课程表从当前的连续时间轴布局重构为基于节次的网格布局。新的设计将提供更直观的课程时间展示,用户可以清楚地看到每节课的时间段,课程卡片会根据其跨越的节次自动调整大小和位置。

## 架构

### 组件层次结构

```
CourseScreen
├── CourseTopBar
├── GridTimetableView (新组件)
│   ├── PeriodHeaderColumn (节次列)
│   ├── TimeHeaderColumn (时间列)
│   ├── DayHeaderRow (星期标题行)
│   └── GridDayColumn (每天的网格列)
│       ├── GridCell (网格单元)
│       └── CourseGridCard (课程卡片)
├── AddCourseSheet (增强)
│   └── PeriodSelector (新组件)
└── EditCourseSheet (增强)
    └── PeriodSelector (新组件)
```

### 数据流

```
CourseViewModel
    ↓ (coursesByDay, periods, courseSettings)
GridTimetableView
    ↓ (计算网格布局)
GridDayColumn
    ↓ (渲染课程卡片)
CourseGridCard
```

## 核心组件设计

### 1. GridTimetableView

**职责**: 主网格布局容器,管理整体布局结构

**接口**:
```kotlin
@Composable
fun GridTimetableView(
    coursesByDay: Map<DayOfWeek, List<Course>>,
    periods: List<CoursePeriod>,
    onCourseClick: (Long) -> Unit,
    onEmptyCellClick: (DayOfWeek, Int) -> Unit, // 点击空白单元格
    currentPeriod: Int? = null, // 当前正在进行的节次
    modifier: Modifier = Modifier
)
```

**布局结构**:
- 使用 `LazyColumn` 实现垂直滚动
- 使用 `LazyRow` 或 `Row` + `HorizontalScrollState` 实现水平滚动
- 固定左侧的节次列和时间列(使用 `Box` 叠加层实现)

**关键特性**:
- 自动滚动到当前时间对应的节次
- 高亮显示当前正在进行的课程
- 支持点击空白单元格快速添加课程

### 2. PeriodHeaderColumn

**职责**: 显示节次编号列

**接口**:
```kotlin
@Composable
fun PeriodHeaderColumn(
    periods: List<CoursePeriod>,
    cellHeight: Dp,
    currentPeriod: Int? = null,
    modifier: Modifier = Modifier
)
```

**样式**:
- 每个单元格显示"第X节"
- 当前节次使用主题色高亮
- 固定宽度: 60.dp

### 3. TimeHeaderColumn

**职责**: 显示时间范围列

**接口**:
```kotlin
@Composable
fun TimeHeaderColumn(
    periods: List<CoursePeriod>,
    cellHeight: Dp,
    modifier: Modifier = Modifier
)
```

**样式**:
- 每个单元格显示"HH:mm-HH:mm"格式的时间
- 使用较小的字体和次要颜色
- 固定宽度: 80.dp

### 4. DayHeaderRow

**职责**: 显示星期标题行

**接口**:
```kotlin
@Composable
fun DayHeaderRow(
    weekDays: List<DayOfWeek>,
    currentDay: DayOfWeek? = null,
    modifier: Modifier = Modifier
)
```

**样式**:
- 显示"周一"到"周日"
- 当前日期使用主题色高亮
- 固定高度: 48.dp

### 5. GridDayColumn

**职责**: 单个工作日的网格列,包含所有节次的单元格和课程卡片

**接口**:
```kotlin
@Composable
fun GridDayColumn(
    day: DayOfWeek,
    courses: List<Course>,
    periods: List<CoursePeriod>,
    cellHeight: Dp,
    onCourseClick: (Long) -> Unit,
    onEmptyCellClick: (Int) -> Unit,
    modifier: Modifier = Modifier
)
```

**布局策略**:
- 使用 `Box` 作为容器
- 背景层: 绘制所有网格单元格
- 前景层: 使用 `offset` 和 `height` 定位课程卡片

**课程定位算法**:
```kotlin
fun calculateCoursePosition(
    course: Course,
    periods: List<CoursePeriod>,
    cellHeight: Dp
): CourseGridPosition {
    val startPeriod = course.periodStart ?: 1
    val endPeriod = course.periodEnd ?: 1
    
    // 计算偏移量(从第几节开始)
    val offsetY = cellHeight * (startPeriod - 1)
    
    // 计算高度(跨越几节课)
    val spanCount = endPeriod - startPeriod + 1
    val height = cellHeight * spanCount
    
    return CourseGridPosition(offsetY, height)
}
```

### 6. GridCell

**职责**: 单个网格单元格,表示一个时间段

**接口**:
```kotlin
@Composable
fun GridCell(
    period: CoursePeriod,
    isEmpty: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
)
```

**样式**:
- 空白单元格: 浅灰色背景,虚线边框
- 有课程单元格: 透明背景(课程卡片会覆盖)
- 点击效果: 涟漪动画

### 7. CourseGridCard

**职责**: 网格中的课程卡片,根据节次范围自动调整大小

**接口**:
```kotlin
@Composable
fun CourseGridCard(
    course: Course,
    position: CourseGridPosition,
    isCurrentlyActive: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
)
```

**样式**:
- 使用课程主题色作为背景
- 显示课程名称、地点、时间
- 当前正在进行的课程: 添加脉冲动画边框
- 最小高度: 确保至少能显示课程名称

**内容布局**:
- 单节课: 只显示课程名称
- 两节课: 显示课程名称 + 地点
- 三节课及以上: 显示课程名称 + 地点 + 时间

### 8. PeriodSelector

**职责**: 节次选择器,用于添加/编辑课程时选择节次范围

**接口**:
```kotlin
@Composable
fun PeriodSelector(
    periods: List<CoursePeriod>,
    selectedPeriodStart: Int?,
    selectedPeriodEnd: Int?,
    occupiedPeriods: Set<Int> = emptySet(),
    onPeriodRangeSelected: (start: Int, end: Int) -> Unit,
    modifier: Modifier = Modifier
)
```

**交互逻辑**:
1. 用户点击开始节次
2. 用户点击结束节次(必须 >= 开始节次)
3. 自动高亮选中的范围
4. 已被占用的节次显示为禁用状态

**样式**:
- 使用 `FlowRow` 布局
- 每个节次显示为 `FilterChip`
- 选中范围: 主题色背景
- 已占用: 灰色背景,禁用状态

## 数据模型

### CourseGridPosition

```kotlin
data class CourseGridPosition(
    val offsetY: Dp,      // 距离顶部的偏移
    val height: Dp        // 卡片高度
)
```

### GridLayoutConfig

```kotlin
data class GridLayoutConfig(
    val cellHeight: Dp = 80.dp,           // 每个单元格的高度
    val periodColumnWidth: Dp = 60.dp,    // 节次列宽度
    val timeColumnWidth: Dp = 80.dp,      // 时间列宽度
    val dayColumnMinWidth: Dp = 100.dp,   // 每天列的最小宽度
    val headerHeight: Dp = 48.dp,         // 标题行高度
    val horizontalPadding: Dp = 4.dp,     // 水平内边距
    val verticalPadding: Dp = 2.dp        // 垂直内边距
)
```

## 状态管理

### CourseViewModel 增强

添加以下状态和方法:

```kotlin
class CourseViewModel @Inject constructor(...) : ViewModel() {
    
    // 当前节次(根据当前时间计算)
    private val _currentPeriod = MutableStateFlow<Int?>(null)
    val currentPeriod: StateFlow<Int?> = _currentPeriod.asStateFlow()
    
    // 当前星期
    private val _currentDay = MutableStateFlow<DayOfWeek>(LocalDate.now().dayOfWeek)
    val currentDay: StateFlow<DayOfWeek> = _currentDay.asStateFlow()
    
    // 计算当前节次
    fun updateCurrentPeriod() {
        val now = LocalTime.now()
        val period = periods.value.find { 
            now >= it.startTime && now < it.endTime 
        }
        _currentPeriod.value = period?.periodNumber
    }
    
    // 获取指定星期和节次的已有课程
    fun getCoursesAt(day: DayOfWeek, periodNumber: Int): List<Course> {
        return coursesByDay.value[day]?.filter { course ->
            val start = course.periodStart ?: return@filter false
            val end = course.periodEnd ?: return@filter false
            periodNumber in start..end
        } ?: emptyList()
    }
    
    // 检测课程冲突
    fun detectConflict(
        day: DayOfWeek,
        periodStart: Int,
        periodEnd: Int,
        excludeCourseId: Long? = null
    ): List<Course> {
        return coursesByDay.value[day]?.filter { course ->
            if (course.id == excludeCourseId) return@filter false
            
            val start = course.periodStart ?: return@filter false
            val end = course.periodEnd ?: return@filter false
            
            // 检测时间段是否重叠
            !(periodEnd < start || periodStart > end)
        } ?: emptyList()
    }
}
```

## 响应式布局策略

### 屏幕宽度适配

```kotlin
@Composable
fun calculateDayColumnWidth(
    screenWidth: Dp,
    config: GridLayoutConfig
): Dp {
    val availableWidth = screenWidth - 
                        config.periodColumnWidth - 
                        config.timeColumnWidth - 
                        (config.horizontalPadding * 2)
    
    val dayColumnWidth = availableWidth / 7
    
    return dayColumnWidth.coerceAtLeast(config.dayColumnMinWidth)
}
```

### 横屏/竖屏适配

- **竖屏**: 显示周一到周五,周末需要横向滚动
- **横屏**: 尝试显示全部7天,如果空间不足则启用横向滚动

```kotlin
@Composable
fun getVisibleDays(
    screenWidth: Dp,
    isLandscape: Boolean
): List<DayOfWeek> {
    return if (isLandscape) {
        DayOfWeek.values().toList()
    } else {
        listOf(
            DayOfWeek.MONDAY,
            DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY
        )
    }
}
```

## 滚动和导航

### 自动滚动到当前时间

```kotlin
@Composable
fun GridTimetableView(...) {
    val listState = rememberLazyListState()
    val currentPeriod by viewModel.currentPeriod.collectAsState()
    
    LaunchedEffect(currentPeriod) {
        currentPeriod?.let { period ->
            // 滚动到当前节次,留出一些上下文
            val targetIndex = (period - 1).coerceAtLeast(0)
            listState.animateScrollToItem(targetIndex)
        }
    }
    
    // ... 布局代码
}
```

### 快速导航按钮

```kotlin
@Composable
fun QuickNavigationFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Icon(Icons.Default.Today, contentDescription = "回到当前时间")
    }
}
```

## 冲突检测和显示

### 冲突检测逻辑

在 `AddCourseSheet` 和 `EditCourseSheet` 中:

```kotlin
val conflictingCourses = remember(dayOfWeek, periodStart, periodEnd) {
    if (periodStart != null && periodEnd != null) {
        viewModel.detectConflict(
            day = dayOfWeek,
            periodStart = periodStart,
            periodEnd = periodEnd,
            excludeCourseId = course?.id
        )
    } else {
        emptyList()
    }
}

if (conflictingCourses.isNotEmpty()) {
    ConflictWarningCard(
        conflicts = conflictingCourses,
        modifier = Modifier.fillMaxWidth()
    )
}
```

### 冲突显示策略

当多个课程在同一时间段时:

1. **并排显示**: 如果列宽足够,将课程卡片并排显示
2. **重叠显示**: 如果空间不足,使用轻微偏移的重叠显示
3. **冲突标识**: 在卡片右上角显示警告图标

```kotlin
@Composable
fun GridDayColumn(...) {
    // 检测每个节次的课程数量
    val coursesPerPeriod = periods.associate { period ->
        period.periodNumber to courses.filter { course ->
            val start = course.periodStart ?: 0
            val end = course.periodEnd ?: 0
            period.periodNumber in start..end
        }
    }
    
    Box(modifier = modifier) {
        // 绘制网格背景
        GridBackground(periods, cellHeight)
        
        // 绘制课程卡片
        courses.forEachIndexed { index, course ->
            val position = calculateCoursePosition(course, periods, cellHeight)
            
            // 检测冲突
            val hasConflict = coursesPerPeriod.values.any { it.size > 1 && course in it }
            
            // 如果有冲突,调整宽度和偏移
            val width = if (hasConflict) 0.9f else 1f
            val offsetX = if (hasConflict) (index % 2) * 8.dp else 0.dp
            
            CourseGridCard(
                course = course,
                position = position,
                hasConflict = hasConflict,
                modifier = Modifier
                    .fillMaxWidth(width)
                    .offset(x = offsetX)
            )
        }
    }
}
```

## 性能优化

### 1. 懒加载

- 使用 `LazyColumn` 只渲染可见的节次
- 使用 `LazyRow` 只渲染可见的工作日

### 2. 记忆化计算

```kotlin
@Composable
fun GridTimetableView(...) {
    // 缓存课程位置计算结果
    val coursePositions = remember(courses, periods) {
        courses.associateWith { course ->
            calculateCoursePosition(course, periods, cellHeight)
        }
    }
    
    // 缓存冲突检测结果
    val conflicts = remember(courses) {
        detectAllConflicts(courses)
    }
}
```

### 3. 避免重组

- 使用 `derivedStateOf` 处理派生状态
- 使用 `key` 参数优化列表项重组

## 动画和过渡

### 1. 课程卡片动画

```kotlin
@Composable
fun CourseGridCard(...) {
    val scale by animateFloatAsState(
        targetValue = if (isCurrentlyActive) 1.05f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )
    
    Card(
        modifier = modifier.scale(scale),
        // ...
    )
}
```

### 2. 当前节次高亮动画

```kotlin
@Composable
fun PeriodHeaderColumn(...) {
    val infiniteTransition = rememberInfiniteTransition()
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    // 应用到当前节次的背景
}
```

## 可访问性

### 1. 语义标签

```kotlin
CourseGridCard(
    modifier = Modifier.semantics {
        contentDescription = "${course.name}, " +
                           "${getDayName(course.dayOfWeek)}, " +
                           "第${course.periodStart}节到第${course.periodEnd}节, " +
                           "${course.startTime}到${course.endTime}"
    }
)
```

### 2. 最小触摸目标

- 确保所有可点击元素至少 48.dp × 48.dp
- 课程卡片最小高度: 56.dp

### 3. 颜色对比度

- 确保课程卡片文字与背景的对比度 >= 4.5:1
- 使用现有的 `CourseColorUtils` 进行颜色调整

## 测试策略

### 单元测试

```kotlin
class CoursePositionCalculatorTest {
    @Test
    fun `calculate position for single period course`() {
        val course = createTestCourse(periodStart = 1, periodEnd = 1)
        val periods = createTestPeriods()
        val cellHeight = 80.dp
        
        val position = calculateCoursePosition(course, periods, cellHeight)
        
        assertEquals(0.dp, position.offsetY)
        assertEquals(80.dp, position.height)
    }
    
    @Test
    fun `calculate position for multi period course`() {
        val course = createTestCourse(periodStart = 2, periodEnd = 4)
        val periods = createTestPeriods()
        val cellHeight = 80.dp
        
        val position = calculateCoursePosition(course, periods, cellHeight)
        
        assertEquals(80.dp, position.offsetY)
        assertEquals(240.dp, position.height)
    }
}
```

### UI测试

```kotlin
@Test
fun testGridTimetableDisplay() {
    composeTestRule.setContent {
        GridTimetableView(
            coursesByDay = testCoursesByDay,
            periods = testPeriods,
            onCourseClick = {},
            onEmptyCellClick = { _, _ -> }
        )
    }
    
    // 验证星期标题显示
    composeTestRule.onNodeWithText("周一").assertIsDisplayed()
    
    // 验证节次显示
    composeTestRule.onNodeWithText("第1节").assertIsDisplayed()
    
    // 验证课程卡片显示
    composeTestRule.onNodeWithText("高等数学").assertIsDisplayed()
}
```

## 迁移策略

### 阶段1: 创建新组件(不影响现有功能)

1. 创建 `GridTimetableView` 及其子组件
2. 创建 `PeriodSelector` 组件
3. 添加单元测试

### 阶段2: 集成到CourseScreen

1. 在 `CourseScreen` 中添加视图切换开关
2. 用户可以在网格视图和时间轴视图之间切换
3. 收集用户反馈

### 阶段3: 完全替换

1. 移除旧的 `ContinuousTimelineView`
2. 将网格视图设为默认视图
3. 清理未使用的代码

## 配置选项

在 `CourseSettings` 中添加网格视图相关配置:

```kotlin
data class CourseSettings(
    // ... 现有字段
    
    // 网格视图配置
    val gridCellHeight: Int = 80,              // 网格单元格高度(dp)
    val showWeekends: Boolean = true,          // 是否显示周末
    val autoScrollToCurrentTime: Boolean = true, // 是否自动滚动到当前时间
    val highlightCurrentPeriod: Boolean = true   // 是否高亮当前节次
)
```

## 总结

这个设计提供了一个完整的网格化课程表解决方案,主要特点:

1. **直观的网格布局**: 清晰展示每节课的时间和位置
2. **灵活的节次选择**: 简化课程添加流程
3. **智能冲突检测**: 避免时间冲突
4. **响应式设计**: 适配不同屏幕尺寸
5. **流畅的动画**: 提升用户体验
6. **良好的可访问性**: 支持辅助功能

通过分阶段迁移策略,可以平滑地从现有时间轴视图过渡到新的网格视图,降低风险并及时收集用户反馈。
