# Design Document

## Overview

设计一个智能的ICS课程导入时间匹配系统，解决当前所有课程都被错误分配到第1节的问题。系统将提供灵活的时间匹配选项，支持使用现有节次配置或根据导入文件自动创建节次。

## Architecture

### 组件结构

```
UI Layer:
├── TimeMatchingDialog (新增)
│   └── 时间匹配选项对话框
├── ImportPreviewScreen (修改)
│   └── 显示匹配结果和预览
└── CourseScreen (修改)
    └── 触发导入流程

Domain Layer:
├── TimeMatchingStrategy (新增)
│   ├── UseExistingPeriods
│   └── AutoCreatePeriods
├── PeriodMatcher (新增)
│   └── 时间到节次的匹配逻辑
└── PeriodGenerator (新增)
    └── 从ICS生成节次配置

Data Layer:
├── CourseConverter (修改)
│   └── 增强时间匹配逻辑
└── IcsImportUseCase (修改)
    └── 集成时间匹配策略
```

## Components and Interfaces

### 1. TimeMatchingDialog

时间匹配选项对话框组件

```kotlin
@Composable
fun TimeMatchingDialog(
    onDismiss: () -> Unit,
    onUseExistingPeriods: () -> Unit,
    onAutoCreatePeriods: () -> Unit
)
```

**功能:**
- 显示两个选项：使用当前课表时间 / 根据导入文件自动匹配
- 提供每个选项的说明文字
- 处理用户选择并回调

### 2. TimeMatchingStrategy

时间匹配策略接口

```kotlin
sealed class TimeMatchingStrategy {
    /**
     * 使用现有节次配置
     */
    object UseExistingPeriods : TimeMatchingStrategy()
    
    /**
     * 根据导入文件自动创建节次
     */
    object AutoCreatePeriods : TimeMatchingStrategy()
}
```

### 3. PeriodMatcher

节次匹配器，负责将时间映射到节次

```kotlin
object PeriodMatcher {
    /**
     * 根据时间和描述匹配节次
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param description 课程描述（可能包含节次信息）
     * @param existingPeriods 现有节次列表
     * @return 匹配的节次范围 (startPeriod, endPeriod)
     */
    fun matchPeriod(
        startTime: LocalTime,
        endTime: LocalTime,
        description: String?,
        existingPeriods: List<CoursePeriod>
    ): Pair<Int?, Int?>
    
    /**
     * 从描述中提取节次信息
     * 支持格式: "第7-8节", "第7 - 8节", "第7节"
     */
    fun extractPeriodFromDescription(description: String?): Pair<Int?, Int?>?
    
    /**
     * 根据时间查找最匹配的节次
     * 使用时间重叠度算法
     */
    fun findBestMatchingPeriod(
        startTime: LocalTime,
        endTime: LocalTime,
        periods: List<CoursePeriod>
    ): Pair<Int?, Int?>
}
```

### 4. PeriodGenerator

节次生成器，从ICS文件生成节次配置

```kotlin
object PeriodGenerator {
    /**
     * 从解析的课程列表生成节次配置
     * @param parsedCourses 解析后的课程列表
     * @return 生成的节次列表
     */
    fun generatePeriodsFromCourses(
        parsedCourses: List<ParsedCourse>
    ): List<CoursePeriod>
    
    /**
     * 提取所有唯一的时间段
     */
    private fun extractUniqueTimeSlots(
        parsedCourses: List<ParsedCourse>
    ): List<Pair<LocalTime, LocalTime>>
    
    /**
     * 将时间段转换为节次配置
     */
    private fun timeSlotsToPerio ds(
        timeSlots: List<Pair<LocalTime, LocalTime>>
    ): List<CoursePeriod>
}
```

### 5. Enhanced CourseConverter

增强的课程转换器

```kotlin
object CourseConverter {
    /**
     * 将ParsedCourse转换为Course（增强版）
     * @param strategy 时间匹配策略
     * @param existingPeriods 现有节次（UseExistingPeriods时使用）
     * @param generatedPeriods 生成的节次（AutoCreatePeriods时使用）
     */
    fun toCourse(
        parsed: ParsedCourse,
        semesterId: Long,
        semesterStartDate: LocalDate,
        primaryColor: Color,
        existingCourses: List<Course>,
        strategy: TimeMatchingStrategy,
        existingPeriods: List<CoursePeriod> = emptyList(),
        generatedPeriods: List<CoursePeriod> = emptyList()
    ): Course
}
```

### 6. Enhanced IcsImportUseCase

增强的导入用例

```kotlin
class IcsImportUseCase {
    /**
     * 预览导入（增强版）
     * @param strategy 时间匹配策略
     */
    suspend fun previewImport(
        uri: Uri,
        semesterId: Long,
        primaryColor: Color,
        strategy: TimeMatchingStrategy,
        existingPeriods: List<CoursePeriod>
    ): Result<ImportPreviewResult>
    
    /**
     * 应用自动生成的节次配置
     */
    suspend fun applyGeneratedPeriods(
        periods: List<CoursePeriod>
    ): Result<Unit>
}

data class ImportPreviewResult(
    val courses: List<Course>,
    val generatedPeriods: List<CoursePeriod>? = null,
    val matchingWarnings: List<String> = emptyList()
)
```

## Data Models

### TimeSlot

时间段数据模型

```kotlin
data class TimeSlot(
    val startTime: LocalTime,
    val endTime: LocalTime,
    val occurrences: Int = 1  // 出现次数，用于排序
)
```

### PeriodMatchResult

节次匹配结果

```kotlin
data class PeriodMatchResult(
    val periodStart: Int?,
    val periodEnd: Int?,
    val matchType: MatchType,
    val confidence: Float  // 匹配置信度 0.0-1.0
)

enum class MatchType {
    EXACT_DESCRIPTION,  // 从描述中精确提取
    TIME_OVERLAP,       // 时间重叠匹配
    CUSTOM_TIME,        // 自定义时间（无匹配）
    GENERATED_PERIOD    // 使用生成的节次
}
```

## Error Handling

### 错误类型

```kotlin
sealed class TimeMatchingException : Exception() {
    data class InvalidTimeFormat(override val message: String) : TimeMatchingException()
    data class PeriodConflict(override val message: String) : TimeMatchingException()
    data class NoMatchingPeriod(override val message: String) : TimeMatchingException()
}
```

### 错误处理策略

1. **时间格式错误**: 跳过该课程，在预览界面显示警告
2. **节次冲突**: 提示用户选择保留哪个配置
3. **无匹配节次**: 
   - UseExistingPeriods模式：创建自定义时间课程
   - AutoCreatePeriods模式：使用生成的节次

## Testing Strategy

### 单元测试

1. **PeriodMatcher测试**
   - 测试从描述中提取节次信息
   - 测试时间重叠匹配算法
   - 测试边界情况（午夜跨越、时间倒序等）

2. **PeriodGenerator测试**
   - 测试从多个课程生成节次
   - 测试时间段去重
   - 测试节次编号分配

3. **CourseConverter测试**
   - 测试两种策略下的转换逻辑
   - 测试节次信息的正确分配

### 集成测试

1. 测试完整的导入流程
2. 测试时间匹配对话框的交互
3. 测试节次配置的保存和应用

### UI测试

1. 测试TimeMatchingDialog的显示和交互
2. 测试导入预览界面的匹配结果显示
3. 测试错误提示的显示

## Implementation Notes

### 时间匹配算法

使用时间重叠度计算最佳匹配：

```kotlin
fun calculateOverlap(
    courseStart: LocalTime,
    courseEnd: LocalTime,
    periodStart: LocalTime,
    periodEnd: LocalTime
): Float {
    val overlapStart = maxOf(courseStart, periodStart)
    val overlapEnd = minOf(courseEnd, periodEnd)
    
    if (overlapStart >= overlapEnd) return 0f
    
    val overlapMinutes = ChronoUnit.MINUTES.between(overlapStart, overlapEnd)
    val courseMinutes = ChronoUnit.MINUTES.between(courseStart, courseEnd)
    
    return overlapMinutes.toFloat() / courseMinutes.toFloat()
}
```

### 节次生成策略

1. 收集所有唯一的时间段
2. 按开始时间排序
3. 分配连续的节次编号
4. 识别午休时间（12:00-14:00之间的间隔）

### UI流程

```
用户点击导入
    ↓
显示TimeMatchingDialog
    ↓
用户选择策略
    ↓
解析ICS文件
    ↓
应用时间匹配
    ↓
显示ImportPreviewScreen
    ↓
用户确认导入
    ↓
保存课程和节次配置
```

## Performance Considerations

1. **缓存节次匹配结果**: 避免重复计算
2. **批量保存节次**: 一次性保存所有生成的节次
3. **异步处理**: 在后台线程进行时间匹配计算

## Accessibility

1. 对话框选项使用清晰的标签
2. 提供每个选项的详细说明
3. 支持键盘导航
4. 匹配结果使用不同颜色和图标区分
