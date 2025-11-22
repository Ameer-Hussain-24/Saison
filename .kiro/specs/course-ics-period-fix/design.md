# Design Document

## Overview

本设计文档描述如何修复ICS导入后课程全部显示在第1节的问题。通过分析ICS文件和代码，我们发现问题的根本原因是：

1. **节次提取问题**：`PeriodMatcher.extractPeriodFromDescription()` 能够正确提取节次信息，但可能无法匹配某些格式（如"第7 - 8节"中间有空格）
2. **学期开始日期未设置**：导入时没有根据ICS文件中的课程开始日期自动设置学期开始日期，导致周次计算错误
3. **显示逻辑问题**：在显示时，Course对象的periodStart/periodEnd字段可能没有被正确使用

**ICS文件分析**：
- 最早的课程开始日期：2025年8月27日
- 课程描述包含节次信息：如"第7 - 8节"
- 课程是周期性的：使用RRULE定义重复规则

## Architecture

### 数据流

```
ICS文件 
  → IcsParser.parse() 
  → ParsedCourse对象（包含description字段）
  → CourseConverter.toCourse()
    → PeriodMatcher.matchPeriod()
      → extractPeriodFromDescription() // 从描述中提取节次
      → findBestMatchingPeriod() // 时间匹配算法
    → 返回periodStart和periodEnd
  → Course对象（包含periodStart和periodEnd字段）
  → 数据库
  → UI显示
```

### 问题诊断

通过代码分析，发现以下潜在问题：

1. **节次提取正则表达式**：`PeriodMatcher.extractPeriodFromDescription()` 使用的正则表达式可能无法匹配所有格式
2. **节次验证逻辑**：提取的节次可能被验证逻辑拒绝
3. **显示逻辑**：UI可能没有正确使用periodStart/periodEnd字段

## Components and Interfaces

### 1. PeriodMatcher增强

**当前实现问题**：
- 正则表达式 `第?\s*(\d+)\s*[-~]\s*(\d+)\s*节?` 可能无法匹配 "第7 - 8节" 格式（中间有空格）
- 验证逻辑要求节次在existingPeriods范围内，但ICS文件的节次可能超出范围

**改进方案**：
```kotlin
fun extractPeriodFromDescription(description: String?): Pair<Int?, Int?>? {
    if (description.isNullOrBlank()) return null
    
    // 增强的正则表达式，支持更多格式
    val patterns = listOf(
        Regex("""第?\s*(\d+)\s*[-~－—]\s*(\d+)\s*节?"""),  // 支持多种连字符
        Regex("""(\d+)\s*[-~－—]\s*(\d+)\s*节"""),        // 不带"第"
        Regex("""第?\s*(\d+)\s*节""")                     // 单节次
    )
    
    for (pattern in patterns) {
        val match = pattern.find(description)
        if (match != null) {
            // 提取并返回
        }
    }
    
    return null
}
```

### 2. 自动学期开始日期检测

**问题**：
导入ICS文件时，如果学期开始日期没有正确设置，系统无法计算当前周次，导致课程显示错误。

**解决方案**：
在 `IcsImportUseCase.previewImport()` 中，自动检测学期开始日期：

```kotlin
suspend fun previewImport(...): Result<ImportPreviewResult> {
    // 1. 解析ICS文件
    val parsedCourses = icsParser.parseFromUri(context, uri)
    
    // 2. 找到最早的课程开始日期
    val earliestDate = parsedCourses
        .mapNotNull { it.dtStart.toLocalDate() }
        .minOrNull() ?: LocalDate.now()
    
    // 3. 计算学期开始日期（该周的周一）
    val semesterStartDate = earliestDate.with(DayOfWeek.MONDAY)
    
    // 4. 获取或创建学期
    var semester = semesterRepository.getSemesterByIdSync(targetSemesterId)
    if (semester == null) {
        // 创建新学期，使用检测到的开始日期
        semester = Semester(
            name = "导入学期 ${earliestDate.year}",
            startDate = semesterStartDate,
            endDate = semesterStartDate.plusMonths(4),
            totalWeeks = 18,
            isDefault = true
        )
        val semesterId = semesterRepository.insertSemester(semester)
        semester = semester.copy(id = semesterId)
    } else if (semester.startDate != semesterStartDate) {
        // 更新现有学期的开始日期
        semester = semester.copy(startDate = semesterStartDate)
        semesterRepository.updateSemester(semester)
    }
    
    // 5. 记录日志
    android.util.Log.d("IcsImportUseCase", 
        "Detected semester start date: $semesterStartDate, earliest course: $earliestDate")
    
    // 6. 转换课程
    val courses = CourseConverter.groupAndConvert(...)
    
    return Result.success(ImportPreviewResult(courses, ...))
}
```

### 3. CourseConverter修复

**当前实现问题**：
- 在 `toCourse()` 方法中，即使 `extractPeriodFromDescription()` 返回了有效的节次，也可能被后续的验证逻辑拒绝

**改进方案**：
```kotlin
fun toCourse(...): Course {
    // 1. 优先从描述中提取节次
    val extractedPeriod = PeriodMatcher.extractPeriodFromDescription(parsed.description)
    
    val (periodStart, periodEnd) = if (extractedPeriod != null) {
        val (start, end) = extractedPeriod
        // 直接使用提取的节次，不进行范围验证
        // 因为ICS文件的节次配置可能与当前设置不同
        if (start != null && end != null && start > 0 && end > 0 && start <= end) {
            Pair(start, end)
        } else {
            // 提取失败，使用时间匹配
            PeriodMatcher.findBestMatchingPeriod(startTime, endTime, existingPeriods)
        }
    } else {
        // 没有描述信息，使用时间匹配
        PeriodMatcher.findBestMatchingPeriod(startTime, endTime, existingPeriods)
    }
    
    return Course(
        ...
        periodStart = periodStart,
        periodEnd = periodEnd,
        isCustomTime = periodStart == null && periodEnd == null
    )
}
```

### 4. 显示逻辑修复

**问题诊断**：
需要检查 `GridDayColumn` 和相关组件如何使用 `periodStart` 和 `periodEnd` 字段。

**可能的问题**：
- `CoursePositionCalculator` 可能不存在或实现有误
- 课程位置计算可能没有正确使用 `periodStart` 和 `periodEnd`

**改进方案**：
```kotlin
// 在GridDayColumn中
fun calculateCoursePosition(course: Course, periods: List<CoursePeriod>, cellHeight: Dp): CoursePosition {
    // 如果有节次信息，使用节次计算位置
    if (course.periodStart != null && course.periodEnd != null) {
        val startPeriod = periods.find { it.periodNumber == course.periodStart }
        val endPeriod = periods.find { it.periodNumber == course.periodEnd }
        
        if (startPeriod != null && endPeriod != null) {
            val offsetY = (course.periodStart - 1) * (cellHeight + 2.dp)
            val height = (course.periodEnd - course.periodStart + 1) * cellHeight + 
                        (course.periodEnd - course.periodStart) * 2.dp
            return CoursePosition(offsetY, height)
        }
    }
    
    // 如果没有节次信息，使用时间计算位置
    // ...
}
```

## Data Models

### ParsedCourse
```kotlin
data class ParsedCourse(
    val summary: String,
    val location: String?,
    val description: String?,  // 包含节次信息，如"第7 - 8节\n行m404"
    val dtStart: LocalDateTime,
    val dtEnd: LocalDateTime,
    val rrule: RecurrenceInfo?,
    val alarmMinutes: Int?
)
```

### Course
```kotlin
data class Course(
    ...
    val periodStart: Int?,      // 开始节次，如7
    val periodEnd: Int?,        // 结束节次，如8
    val isCustomTime: Boolean   // 是否使用自定义时间
)
```

## Error Handling

### 节次提取失败
- 记录警告日志
- 回退到时间匹配算法
- 如果时间匹配也失败，标记为自定义时间课程

### 节次超出范围
- 不拒绝导入
- 允许节次号超出当前配置的范围
- 在UI中显示警告（可选）

### 显示异常
- 如果无法计算课程位置，显示在默认位置
- 添加视觉标记表示异常状态

## Testing Strategy

### 单元测试
1. 测试 `extractPeriodFromDescription()` 对各种格式的支持
   - "第7-8节"
   - "第7 - 8节"（有空格）
   - "第7节"
   - "7-8节"（不带"第"）
   - "第7~8节"（波浪号）

2. 测试 `toCourse()` 的节次分配逻辑
   - 有描述信息的情况
   - 无描述信息的情况
   - 节次超出范围的情况

### 集成测试
1. 导入真实的ICS文件
2. 验证导入后的课程数据
3. 验证UI显示是否正确

### 调试日志
在关键位置添加日志：
- ICS解析时记录description字段
- 节次提取时记录输入和输出
- 课程转换时记录periodStart和periodEnd
- UI渲染时记录课程位置计算结果

## Implementation Notes

### 优先级
1. **最高优先级**：实现自动学期开始日期检测
2. **高优先级**：修复 `PeriodMatcher.extractPeriodFromDescription()` 的正则表达式
3. **高优先级**：移除节次范围验证逻辑
4. **中优先级**：添加详细的调试日志
5. **低优先级**：优化UI显示逻辑

### 向后兼容性
- 确保修改不影响现有的手动添加课程功能
- 确保修改不影响现有的课程显示逻辑

### 性能考虑
- 正则表达式匹配应该很快，不会影响性能
- 课程位置计算应该缓存结果，避免重复计算
