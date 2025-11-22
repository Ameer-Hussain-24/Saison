# 课程节次设置重构设计文档

## Overview

本设计文档描述如何重构课程设置系统，从单一的"每天节次数"配置改为按时段（上午/下午/晚上）分别配置，并移除不直观的"时间轴紧凑度"设置。

## Architecture

### 数据流
```
User Input (UI) 
  → CourseSettingsSheet 
  → CourseViewModel 
  → PreferencesManager 
  → DataStore
  → PeriodGenerator (生成节次列表)
```

## Components and Interfaces

### 1. 数据模型更新

#### CourseSettings.kt
```kotlin
data class CourseSettings(
    // 移除: val periodsPerDay: Int
    // 移除: val timelineCompactness: Float
    
    // 新增: 分时段节次配置
    val morningPeriods: Int = 4,        // 上午节次数
    val afternoonPeriods: Int = 4,      // 下午节次数  
    val eveningPeriods: Int = 0,        // 晚上节次数
    
    val periodDuration: Int = 45,
    val breakDuration: Int = 10,
    val firstPeriodStartTime: LocalTime = LocalTime.of(8, 0),
    val lunchBreakDuration: Int = 90,
    val dinnerBreakDuration: Int = 60,  // 新增: 晚休时长
    val semesterStartDate: LocalDate? = null,
    val totalWeeks: Int = 18,
    val gridCellHeight: Int = 80,
    val showWeekends: Boolean = true,
    val autoScrollToCurrentTime: Boolean = true,
    val highlightCurrentPeriod: Boolean = true,
    val breakPeriods: List<BreakPeriod> = emptyList(),  // 将由生成器自动填充
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    // 计算总节次数
    val totalPeriods: Int
        get() = morningPeriods + afternoonPeriods + eveningPeriods
}
```

### 2. UI组件更新

#### CourseSettingsSheet.kt
移除:
- 时间轴紧凑度滑块

添加:
- 上午节次数选择器 (1-6节)
- 下午节次数选择器 (1-6节)
- 晚上节次数选择器 (0-3节)

布局结构:
```
节次配置
├── 上午节次: [1] [2] [3] [4] [5] [6]
├── 下午节次: [1] [2] [3] [4] [5] [6]
└── 晚上节次: [0] [1] [2] [3]

其他设置
├── 课程时长
├── 课间休息
└── ...
```

### 3. 节次生成器

#### PeriodGenerator.kt
新增方法:
```kotlin
fun generatePeriodsFromSegments(
    morningPeriods: Int,
    afternoonPeriods: Int,
    eveningPeriods: Int,
    firstPeriodStartTime: LocalTime,
    periodDuration: Int,
    breakDuration: Int,
    lunchBreakDuration: Int,
    dinnerBreakDuration: Int
): Pair<List<CoursePeriod>, List<BreakPeriod>>
```

生成逻辑:
1. 上午节次: 从firstPeriodStartTime开始
2. 午休: 在上午最后一节后
3. 下午节次: 午休后开始
4. 晚休: 在下午最后一节后 (如果有晚上节次)
5. 晚上节次: 晚休后开始

### 4. 模板更新

#### TemplateSelector.kt
更新大学模板:
```kotlin
CourseTemplate.UNIVERSITY -> CourseSettings(
    morningPeriods = 4,
    afternoonPeriods = 4,
    eveningPeriods = 1,
    periodDuration = 45,
    breakDuration = 10,
    firstPeriodStartTime = LocalTime.of(8, 0),
    lunchBreakDuration = 120,  // 2小时午休
    dinnerBreakDuration = 60    // 1小时晚休
)
```

其他模板保持兼容:
- 高中模板: morningPeriods=4, afternoonPeriods=4, eveningPeriods=2
- 初中模板: morningPeriods=4, afternoonPeriods=4, eveningPeriods=0

## Data Models

### CoursePeriod (保持不变)
```kotlin
data class CoursePeriod(
    val periodNumber: Int,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val timeOfDay: TimeOfDay  // MORNING, AFTERNOON, EVENING
)
```

### BreakPeriod (保持不变)
```kotlin
data class BreakPeriod(
    val name: String,
    val afterPeriod: Int
)
```

### TimeOfDay (新增枚举)
```kotlin
enum class TimeOfDay {
    MORNING,    // 上午
    AFTERNOON,  // 下午
    EVENING     // 晚上
}
```

## Error Handling

### 验证规则
1. 上午节次: 1-6节
2. 下午节次: 1-6节
3. 晚上节次: 0-3节
4. 总节次数: 不超过15节

### 错误提示
- "上午节次数必须在1-6之间"
- "下午节次数必须在1-6之间"
- "晚上节次数必须在0-3之间"
- "总节次数不能超过15节"

## Testing Strategy

### 单元测试
1. CourseSettings.totalPeriods 计算测试
2. PeriodGenerator 生成逻辑测试
   - 测试不同时段组合
   - 测试休息时段插入
   - 测试时间计算准确性

### 集成测试
1. UI更新后数据正确保存
2. 模板选择后配置正确应用
3. 节次列表正确生成和显示

## Migration Strategy

### 数据迁移
对于现有用户的 `periodsPerDay` 数据:
```kotlin
fun migrateOldSettings(oldSettings: CourseSettings): CourseSettings {
    val totalPeriods = oldSettings.periodsPerDay
    return when {
        totalPeriods <= 8 -> oldSettings.copy(
            morningPeriods = 4,
            afternoonPeriods = totalPeriods - 4,
            eveningPeriods = 0
        )
        totalPeriods <= 10 -> oldSettings.copy(
            morningPeriods = 4,
            afternoonPeriods = 4,
            eveningPeriods = totalPeriods - 8
        )
        else -> oldSettings.copy(
            morningPeriods = 4,
            afternoonPeriods = 4,
            eveningPeriods = min(totalPeriods - 8, 3)
        )
    }
}
```

### 向后兼容
- 保留 `periodsPerDay` 字段作为计算属性
- 在读取旧数据时自动迁移
- 新数据只使用新字段

## Implementation Notes

1. **UI实现**: 使用 SegmentedButton 或 Slider 来选择节次数
2. **实时预览**: 在设置页面显示生成的节次时间预览
3. **性能**: 节次生成应该是轻量级操作，可以实时计算
4. **国际化**: 添加相关字符串资源
