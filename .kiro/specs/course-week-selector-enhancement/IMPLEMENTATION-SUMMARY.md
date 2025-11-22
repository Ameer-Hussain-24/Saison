# 课程周数选择器增强 - 实现总结

## 项目概述

本项目为课程表系统添加了高级周数选择功能，允许用户精确控制课程在哪些周上课。主要功能包括：

1. **可视化周数选择器** - 网格布局的圆形按钮，支持点击选择
2. **快捷选择模式** - 全周、单周、双周快速配置
3. **自定义周数** - 支持任意周数组合
4. **学期设置** - 配置学期开始日期和总周数
5. **当前周显示** - 自动计算并显示当前周数
6. **课程过滤** - 根据当前周数过滤课程

## 已完成的核心功能

### 1. 数据层实现 ✅

#### 数据模型扩展
- **Course 模型**：添加 `customWeeks: List<Int>?` 字段
- **WeekPattern 枚举**：添加 `CUSTOM` 选项
- **CourseEntity**：添加 `customWeeks: String?` 字段（JSON格式）

#### 数据库迁移
```kotlin
// 版本 7 → 8
ALTER TABLE courses ADD COLUMN customWeeks TEXT DEFAULT NULL
```

#### 数据转换
- **WeekListConverter**：List<Int> ↔ JSON 字符串转换
- **CourseMapper**：Domain ↔ Entity 转换支持 customWeeks

### 2. 学期设置 ✅

CourseSettings 模型已包含：
- `semesterStartDate: LocalDate?` - 学期开始日期
- `totalWeeks: Int` - 学期总周数（默认18周）

PreferencesManager 已支持学期设置的持久化存储。

### 3. 周数计算工具 ✅

**WeekCalculator 类**提供以下功能：

```kotlin
// 计算当前周数
fun calculateCurrentWeek(semesterStartDate: LocalDate, currentDate: LocalDate): Int

// 日期范围转周数列表
fun calculateWeeksFromDateRange(
    semesterStartDate: LocalDate,
    rangeStartDate: LocalDate,
    rangeEndDate: LocalDate
): List<Int>

// 获取某周的日期范围
fun getWeekDateRange(semesterStartDate: LocalDate, weekNumber: Int): Pair<LocalDate, LocalDate>

// 判断日期是否在指定周数中
fun isDateInWeeks(date: LocalDate, semesterStartDate: LocalDate, weeks: List<Int>): Boolean
```

**单元测试**：18个测试用例，覆盖各种边界情况。

### 4. UI组件修复 ✅

修复了 WeekPattern.CUSTOM 导致的 when 表达式穷尽性问题：
- AddCourseSheet.kt
- CourseCard.kt

## 待实现功能

### UI组件（高优先级）

#### 1. WeekSelectorDialog
主对话框组件，包含：
- TabRow（周数模式/日期模式切换）
- WeekNumberSelectionContent
- DateRangeSelectionContent
- 统计信息显示
- 确定/取消按钮

#### 2. WeekNumberButton
圆形周数按钮：
- 尺寸：48dp
- 形状：CircleShape
- 选中/未选中状态
- 点击动画

#### 3. WeekNumberGrid
网格布局：
- FlowRow 布局
- 每行6个按钮
- 8dp 间距

#### 4. QuickSelectionBar
快捷选择按钮栏：
- FilterChip 组件
- 全周/单周/双周
- 选中状态指示

#### 5. DateRangeSelectionContent
日期模式选择：
- 开始/结束日期选择
- DatePicker 集成
- 重复模式选择

#### 6. CurrentWeekIndicator
当前周指示器：
- 显示当前周数
- 显示模式切换按钮
- primaryContainer 颜色

### ViewModel扩展（高优先级）

#### CourseViewModel 新增功能

```kotlin
// 当前周数
val currentWeek: StateFlow<Int>

// 显示模式
val showAllCourses: StateFlow<Boolean>

// 过滤后的课程
val filteredCourses: StateFlow<List<Course>>

// 判断课程是否在某周上课
fun isCourseActiveInWeek(course: Course, week: Int): Boolean

// 切换显示模式
fun toggleShowAllCourses()

// 添加自定义周数课程
fun addCourseWithCustomWeeks(...)
```

### 集成工作（中优先级）

#### 1. 更新 AddCourseSheet
- 集成 WeekSelectorDialog
- 添加 customWeeks 状态管理
- 保存自定义周数

#### 2. 更新 CourseCard
- 创建 WeekPatternChip 组件
- 显示自定义周数信息
- 本周是否有课的视觉指示

#### 3. 更新 CourseScreen
- 添加 CurrentWeekIndicator
- 使用 filteredCourses
- 显示模式切换

### 国际化（中优先级）

需要添加的字符串资源：
- 周数选择器相关（约15个）
- 日期模式相关（约8个）
- 当前周指示器相关（约4个）
- 错误提示相关（约5个）

支持语言：中文、英文、日文、越南文

### 测试（低优先级）

#### 单元测试
- isCourseActiveInWeek 方法测试
- 课程过滤逻辑测试
- WeekSelectionValidator 测试
- WeekListConverter 测试

#### UI测试
- 周数选择器交互测试
- 快捷模式切换测试
- 过滤功能测试

## 实现建议

### 推荐实现顺序

1. **WeekNumberButton** → **WeekNumberGrid** → **QuickSelectionBar**
   - 从小到大构建UI组件
   - 每个组件独立测试

2. **WeekNumberSelectionContent**
   - 组合上述组件
   - 实现选择逻辑

3. **WeekSelectorDialog**
   - 集成所有内容
   - 添加验证逻辑

4. **CourseViewModel 扩展**
   - 添加当前周计算
   - 实现过滤逻辑

5. **集成到现有组件**
   - AddCourseSheet
   - CourseCard
   - CourseScreen

6. **国际化和测试**
   - 添加字符串资源
   - 编写测试用例

### 关键技术点

#### Material 3 设计规范
```kotlin
// 颜色
containerColor = MaterialTheme.colorScheme.primary  // 选中
containerColor = MaterialTheme.colorScheme.surfaceVariant  // 未选中

// 尺寸
size = 48.dp
shape = CircleShape
spacing = 8.dp

// 动画
animateColorAsState(...)
AnimatedContent(...)
```

#### 状态管理
```kotlin
// 选择状态
var selectedWeeks by remember { mutableStateOf(setOf<Int>()) }
var quickMode by remember { mutableStateOf<QuickSelectionMode?>(null) }

// 联动逻辑
// 手动选择 → 取消快捷模式
// 快捷模式 → 更新选择状态
```

#### 数据验证
```kotlin
object WeekSelectionValidator {
    fun validateWeekNumbers(weeks: List<Int>, totalWeeks: Int): ValidationResult
    fun validateDateRange(startDate: LocalDate, endDate: LocalDate): ValidationResult
}
```

## 使用示例

### 添加自定义周数课程

```kotlin
// 用户选择第 1, 3, 5, 7 周
val customWeeks = listOf(1, 3, 5, 7)

val course = Course(
    name = "高等数学",
    weekPattern = WeekPattern.CUSTOM,
    customWeeks = customWeeks,
    // ... 其他字段
)

courseRepository.insertCourse(course)
```

### 判断课程是否在本周上课

```kotlin
val currentWeek = weekCalculator.calculateCurrentWeek(
    semesterStartDate = settings.semesterStartDate,
    currentDate = LocalDate.now()
)

val isActiveThisWeek = when (course.weekPattern) {
    WeekPattern.ALL -> true
    WeekPattern.ODD -> currentWeek % 2 == 1
    WeekPattern.EVEN -> currentWeek % 2 == 0
    WeekPattern.CUSTOM -> course.customWeeks?.contains(currentWeek) ?: false
    else -> true
}
```

### 过滤本周课程

```kotlin
val thisWeekCourses = allCourses.filter { course ->
    isCourseActiveInWeek(course, currentWeek)
}
```

## 性能优化建议

1. **周数计算缓存**
   ```kotlin
   val periods by remember(settings) {
       derivedStateOf { calculatePeriods(settings) }
   }
   ```

2. **课程过滤优化**
   ```kotlin
   val filteredCourses = combine(courses, currentWeek, showAll) { ... }
       .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
   ```

3. **UI渲染优化**
   ```kotlin
   LazyColumn {
       items(courses, key = { it.id }) { course ->
           CourseCard(course)
       }
   }
   ```

## 已知问题和限制

1. **测试环境问题**
   - 其他测试文件有 Mockito 依赖问题
   - WeekCalculatorTest 已编写但未运行

2. **A/B周模式**
   - 当前简化处理，显示所有周
   - 需要额外的学期配置来定义A/B周

3. **跨学期课程**
   - 当前假设课程在单个学期内
   - 跨学期课程需要额外处理

## 总结

核心数据层和工具类已完成，为UI实现打下了坚实的基础。剩余工作主要集中在UI组件的实现和集成上。建议按照推荐的实现顺序逐步完成，每个组件完成后进行测试，确保质量。

整个功能完成后，用户将能够：
- 精确控制课程在哪些周上课
- 快速配置常见的周数模式
- 查看当前周数和本周课程
- 通过直观的UI进行周数选择

这将大大提升课程表系统的灵活性和用户体验。
