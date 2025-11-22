# 课程表UI优化设计文档

## 概述

本设计文档描述了课程表UI优化的技术实现方案。目标是将当前的网格课程表界面改造为更清晰、更易用的布局，参考目标样式实现节次时间清晰显示、课程卡片美观布局和整体视觉优化。

## 架构

### 组件层次结构

```
CourseScreen (主屏幕)
├── CourseTopBar (顶部栏)
│   ├── 周次信息显示
│   ├── 快速返回当前周按钮
│   └── 设置/导入/导出按钮
└── GridTimetableView (网格课程表)
    ├── PeriodTimeColumn (节次时间列 - 新增)
    │   ├── 节次编号
    │   └── 时间范围
    ├── DayHeaderRow (星期标题行 - 优化)
    │   ├── 星期名称
    │   └── 日期显示
    └── CourseGrid (课程网格)
        ├── GridDayColumn (日期列)
        │   ├── GridCell (空白单元格)
        │   └── CourseGridCard (课程卡片 - 优化)
        └── EmptyStateView (空状态视图 - 新增)
```

### 数据流

```
CourseViewModel
    ↓ (StateFlow)
CourseScreen
    ↓ (Props)
GridTimetableView
    ↓ (Props)
GridDayColumn → CourseGridCard
```

## 组件和接口

### 1. PeriodTimeColumn (新增组件)

**职责**: 显示节次编号和时间范围的左侧固定列

**接口**:
```kotlin
@Composable
fun PeriodTimeColumn(
    periods: List<CoursePeriod>,
    cellHeight: Dp,
    currentPeriod: Int? = null,
    modifier: Modifier = Modifier
)
```

**设计要点**:
- 每个节次显示两行：第一行显示"第X节"，第二行显示时间范围
- 当前节次使用主题色背景高亮
- 使用简洁的文字显示，无需Card组件
- **固定宽度：60dp**（紧凑设计）
- 文字大小：节次编号12sp，时间10sp
- 内边距：8dp（减少内边距）

**视觉规范**:
```kotlin
// 普通状态
backgroundColor = MaterialTheme.colorScheme.surface
borderColor = MaterialTheme.colorScheme.outlineVariant
textColor = MaterialTheme.colorScheme.onSurface

// 当前节次状态
backgroundColor = MaterialTheme.colorScheme.primaryContainer
borderColor = MaterialTheme.colorScheme.primary
textColor = MaterialTheme.colorScheme.onPrimaryContainer
```

### 2. DayHeaderRow (优化现有组件)

**职责**: 显示星期名称和对应日期

**接口**:
```kotlin
@Composable
fun DayHeaderRow(
    weekDays: List<DayOfWeek>,
    currentWeek: Int,
    semesterStartDate: LocalDate,
    currentDay: DayOfWeek? = null,
    modifier: Modifier = Modifier
)
```

**设计要点**:
- 显示格式：星期名称（如"周一"）+ 日期（如"11/3"）
- 当前日期使用主题色背景
- **高度：56dp**（紧凑设计）
- 圆角：6dp
- 文字居中对齐
- **自动平分宽度**：使用weight(1f)确保所有星期列等宽

**日期计算逻辑**:
```kotlin
fun calculateDateForDay(
    semesterStartDate: LocalDate,
    currentWeek: Int,
    dayOfWeek: DayOfWeek
): LocalDate {
    val weekOffset = currentWeek - 1
    val daysFromStart = weekOffset * 7 + (dayOfWeek.value - 1)
    return semesterStartDate.plusDays(daysFromStart.toLong())
}
```

### 3. CourseGridCard (优化现有组件)

**职责**: 显示单个课程的卡片

**接口**:
```kotlin
@Composable
fun CourseGridCard(
    course: Course,
    position: CourseGridPosition,
    isCurrentlyActive: Boolean = false,
    hasConflict: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
)
```

**设计要点**:
- **圆角：10dp**（紧凑设计）
- **内边距：8dp**（紧凑设计）
- 阴影：1dp（普通）/ 3dp（当前课程）
- 文字层次：
  - **课程名称：13sp，Bold**（紧凑字体）
  - **地点：11sp，Medium**（紧凑字体）
  - **时间：10sp，Regular**（紧凑字体）
- 颜色透明度：0.85f

**布局结构**:
```
Card
└── Column (padding: 8dp)
    ├── Text (课程名称, maxLines: 2)
    ├── Spacer (4dp)
    ├── Row (地点信息)
    │   ├── Icon (📍, size: 12dp)
    │   └── Text (地点)
    ├── Spacer (2dp)
    └── Text (时间范围)
```

### 4. GridCell (优化现有组件)

**职责**: 显示空白单元格

**设计要点**:
- 背景色：`surfaceVariant.copy(alpha = 0.15f)`（降低透明度）
- 边框：0.5dp，`outlineVariant.copy(alpha = 0.25f)`（更细的边框）
- 圆角：4dp（紧凑设计）
- 点击反馈：涟漪效果（ripple）

### 5. EmptyStateView (新增组件)

**职责**: 显示空状态提示

**接口**:
```kotlin
@Composable
fun EmptyStateView(
    message: String,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
)
```

**设计要点**:
- 居中显示
- 图标：Icons.Outlined.EventNote，大小48dp
- 主文字：16sp，Medium
- 副文字：14sp，Regular
- 操作按钮：FilledTonalButton

### 6. GridTimetableView (重构现有组件)

**职责**: 整合所有子组件，管理布局和滚动

**主要变更**:
1. 将左侧列从两列（PeriodHeaderColumn + TimeHeaderColumn）合并为一列（PeriodTimeColumn）
2. 增加DayHeaderRow的高度以显示日期
3. 优化间距系统：统一使用2dp、4dp、8dp（紧凑间距）
4. **添加HorizontalPager支持周切换手势**
5. **移除课程网格的水平滚动，确保全周可见**

**布局结构**:
```
HorizontalPager (周切换)
└── Surface (background)
    └── Row
        ├── PeriodTimeColumn (固定, width: 60dp)
        └── Column
            ├── DayHeaderRow (height: 56dp, 固定)
            └── LazyColumn (垂直滚动)
                └── Row (课程网格, fillMaxWidth)
                    └── GridDayColumn × 7 (使用weight平分宽度)
```

**滚动行为**:
- **水平滑动**: 切换周次（HorizontalPager）
  - 向左滑动 → 下一周
  - 向右滑动 → 上一周
- **垂直滚动**: 查看不同节次（LazyColumn）
- **无课程网格水平滚动**: 所有星期列使用weight(1f)平分剩余宽度

## 数据模型

### CoursePeriod (现有模型)

```kotlin
data class CoursePeriod(
    val periodNumber: Int,
    val startTime: LocalTime,
    val endTime: LocalTime
)
```

### GridLayoutConfig (扩展现有模型)

```kotlin
data class GridLayoutConfig(
    val cellHeight: Dp = 70.dp,         // 紧凑：从80dp减少
    val periodColumnWidth: Dp = 60.dp,  // 紧凑：节次列宽度
    val headerHeight: Dp = 56.dp,       // 紧凑：星期标题高度
    val cardCornerRadius: Dp = 10.dp,   // 紧凑：卡片圆角
    val cellCornerRadius: Dp = 4.dp,    // 紧凑：单元格圆角
    val cardElevation: Dp = 1.dp,       // 紧凑：卡片阴影
    val spacing: Dp = 2.dp              // 紧凑：统一间距
)
```

### CourseUiState (扩展现有模型)

```kotlin
sealed class CourseUiState {
    object Loading : CourseUiState()
    object Success : CourseUiState()
    data class Error(val message: String) : CourseUiState()
    object Empty : CourseUiState()  // 新增：空状态
}
```

## 错误处理

### 1. 日期计算错误

**场景**: 学期开始日期未设置或无效

**处理**:
```kotlin
fun calculateDateForDay(...): LocalDate? {
    return try {
        // 计算逻辑
    } catch (e: DateTimeException) {
        Log.e("DayHeaderRow", "Failed to calculate date", e)
        null  // 返回null，UI显示星期名称但不显示日期
    }
}
```

### 2. 课程位置计算错误

**场景**: 课程节次超出范围

**处理**:
```kotlin
fun calculateCoursePosition(...): CourseGridPosition? {
    if (course.periodStart !in 1..periods.size) {
        Log.w("CoursePositionCalculator", "Invalid period: ${course.periodStart}")
        return null  // 不显示该课程
    }
    // 正常计算
}
```

### 3. 空数据处理

**场景**: 没有课程数据

**处理**:
- 显示EmptyStateView
- 提供"添加课程"快捷操作
- 保持网格结构可见（显示空单元格）

## 测试策略

### 单元测试

1. **日期计算测试**
```kotlin
@Test
fun `calculateDateForDay returns correct date for week 1 monday`() {
    val startDate = LocalDate.of(2024, 9, 2)
    val result = calculateDateForDay(startDate, 1, DayOfWeek.MONDAY)
    assertEquals(LocalDate.of(2024, 9, 2), result)
}
```

2. **课程位置计算测试**
```kotlin
@Test
fun `calculateCoursePosition returns correct height for multi-period course`() {
    val course = Course(periodStart = 1, periodEnd = 3, ...)
    val position = CoursePositionCalculator.calculateCoursePosition(course, periods, 80.dp)
    assertEquals(248.dp, position.height)  // 80*3 + 4*2 (spacing)
}
```

3. **颜色对比度测试**
```kotlin
@Test
fun `course card text has sufficient contrast`() {
    val backgroundColor = Color(0xFFE3F2FD)
    val textColor = Color.White
    val contrast = ColorContrastChecker.calculateContrast(backgroundColor, textColor)
    assertTrue(contrast >= 4.5f)  // WCAG AA标准
}
```

### UI测试

1. **组件渲染测试**
```kotlin
@Test
fun `PeriodTimeColumn displays all periods`() {
    composeTestRule.setContent {
        PeriodTimeColumn(periods = testPeriods, cellHeight = 80.dp)
    }
    testPeriods.forEach { period ->
        composeTestRule.onNodeWithText("第${period.periodNumber}节").assertExists()
    }
}
```

2. **交互测试**
```kotlin
@Test
fun `clicking empty cell triggers callback`() {
    var clickedPeriod: Int? = null
    composeTestRule.setContent {
        GridCell(
            period = testPeriod,
            isEmpty = true,
            onClick = { clickedPeriod = testPeriod.periodNumber }
        )
    }
    composeTestRule.onNode(hasClickAction()).performClick()
    assertEquals(1, clickedPeriod)
}
```

3. **滚动测试**
```kotlin
@Test
fun `auto scroll to current period works`() {
    composeTestRule.setContent {
        GridTimetableView(
            currentPeriod = 5,
            autoScrollToCurrentTime = true,
            ...
        )
    }
    // 验证第5节在可见区域内
    composeTestRule.onNodeWithText("第5节").assertIsDisplayed()
}
```

### 集成测试

1. **周切换测试**
```kotlin
@Test
fun `swipe left changes to next week`() {
    // 启动CourseScreen
    // 执行左滑手势
    // 验证周次+1
    // 验证课程数据更新
}
```

2. **主题切换测试**
```kotlin
@Test
fun `course colors adapt to theme change`() {
    // 设置浅色主题
    // 验证课程卡片颜色
    // 切换到深色主题
    // 验证课程卡片颜色变化
}
```

## 性能优化

### 1. 记忆化计算

```kotlin
// 缓存课程位置计算结果
val coursePositions = remember(courses, periods, cellHeight) {
    courses.associateWith { course ->
        CoursePositionCalculator.calculateCoursePosition(course, periods, cellHeight)
    }
}
```

### 2. 懒加载

```kotlin
// 使用LazyColumn只渲染可见区域
LazyColumn(state = listState) {
    items(periods) { period ->
        // 渲染单个节次
    }
}
```

### 3. 避免重组

```kotlin
// 使用derivedStateOf避免不必要的重组
val filteredCourses by remember {
    derivedStateOf {
        courses.filter { it.weekPattern.isActiveInWeek(currentWeek) }
    }
}
```

## 可访问性

### 1. 语义标签

```kotlin
// 为课程卡片添加语义描述
Modifier.semantics {
    contentDescription = "${course.name}, ${course.location}, " +
        "${course.startTime}到${course.endTime}"
}
```

### 2. 最小触摸目标

- 所有可点击元素最小尺寸：48dp × 48dp
- 课程卡片最小高度：80dp
- 空白单元格最小高度：80dp

### 3. 颜色对比度

- 文字与背景对比度 ≥ 4.5:1 (WCAG AA)
- 重要信息（当前节次）对比度 ≥ 7:1 (WCAG AAA)

## 国际化

### 新增字符串资源

```xml
<!-- strings.xml -->
<string name="period_number_format">第%d节</string>
<string name="time_range_format">%s-%s</string>
<string name="week_number_format">第 %d / %d 周</string>
<string name="empty_timetable_message">本周无课程</string>
<string name="empty_timetable_action">添加课程</string>
<string name="back_to_current_week">回到当前周</string>
```

## 动画和过渡

### 1. 周切换动画（HorizontalPager）

```kotlin
HorizontalPager(
    state = pagerState,
    modifier = Modifier.fillMaxSize()
) { page ->
    // 根据page计算周次
    val weekNumber = calculateWeekFromPage(page)
    
    // 渲染该周的课程表
    GridTimetableView(
        currentWeek = weekNumber,
        ...
    )
}

// 监听滑动事件更新ViewModel
LaunchedEffect(pagerState.currentPage) {
    viewModel.setWeekOffset(pagerState.currentPage - initialPage)
}
```

### 2. 当前节次高亮动画

```kotlin
val backgroundColor by animateColorAsState(
    targetValue = if (isCurrentPeriod) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    },
    animationSpec = tween(durationMillis = 300)
)
```

### 3. 课程卡片点击动画

```kotlin
val scale by animateFloatAsState(
    targetValue = if (isPressed) 0.95f else 1f,
    animationSpec = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium
    )
)
```

## 实现优先级

### Phase 1: 核心视觉优化（高优先级）
1. 创建PeriodTimeColumn组件
2. 优化DayHeaderRow显示日期
3. 优化CourseGridCard视觉样式
4. 优化GridCell样式

### Phase 2: 交互增强（中优先级）
5. 添加EmptyStateView组件
6. 增强点击反馈效果
7. 实现周切换手势

### Phase 3: 细节完善（低优先级）
8. 添加动画过渡
9. 优化性能
10. 完善可访问性

## 技术依赖

- Jetpack Compose 1.5+
- Material 3 (Material Design 3)
- Kotlin Coroutines
- Hilt (依赖注入)
- JUnit 4 (单元测试)
- Compose UI Test (UI测试)

## 设计决策记录

### 决策1: 合并节次和时间列

**背景**: 原设计有两列（PeriodHeaderColumn和TimeHeaderColumn）

**决策**: 合并为一列（PeriodTimeColumn）

**理由**:
- 减少水平空间占用
- 信息更集中，易于阅读
- 参考目标样式的设计

### 决策2: 增加星期标题高度

**背景**: 原标题只显示星期名称

**决策**: 增加高度以显示日期

**理由**:
- 用户需要知道具体日期
- 更容易规划未来课程
- 符合用户期望

### 决策3: 使用LazyColumn而非Column

**背景**: 课程表可能有很多节次

**决策**: 使用LazyColumn实现虚拟滚动

**理由**:
- 提升性能，只渲染可见区域
- 支持大量节次（如12节课）
- 更流畅的滚动体验

### 决策4: 使用HorizontalPager实现周切换

**背景**: 需要支持左右滑动切换周次

**决策**: 使用Compose的HorizontalPager组件

**理由**:
- 原生支持左右滑动手势
- 自动处理滑动动画和过渡
- 支持无限滚动（通过大的page count）
- 性能优化（只渲染当前页和相邻页）

### 决策5: 移除课程网格水平滚动

**背景**: 原设计支持水平滚动查看更多星期

**决策**: 移除水平滚动，使用weight平分宽度

**理由**:
- 用户期望一页显示全周课程
- 避免滚动冲突（水平滑动用于切换周次）
- 更紧凑的布局可以容纳7天
- 提升可用性，减少操作步骤

### 决策6: 紧凑布局设计

**背景**: 需要在一屏显示全周课程

**决策**: 采用紧凑的尺寸和间距

**理由**:
- 节次列宽度从80dp减少到60dp
- 单元格高度从80dp减少到70dp
- 间距从4dp减少到2dp
- 字体大小适当缩小
- 确保在标准手机屏幕上显示完整
