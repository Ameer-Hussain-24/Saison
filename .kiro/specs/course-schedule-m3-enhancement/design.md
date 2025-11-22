# 课程表 M3 增强设计文档

## 概述

本设计文档描述了课程表功能的 Material Design 3 风格重设计方案，增加灵活的课程配置功能，允许用户自定义每天的课程节数、课程时长、课间休息等参数，并提供预设模板快速配置。该功能将提供更符合不同学校和个人需求的课程管理体验。

## 架构

### 组件层次结构

```
CourseScreen (UI层)
├── CourseViewModel (业务逻辑层)
│   ├── CourseRepository (数据层)
│   ├── CourseSettingsRepository (设置数据层)
│   └── PreferencesManager (设置管理)
└── UI组件
    ├── CourseScheduleView (课程表视图)
    ├── CourseCard (课程卡片)
    ├── CourseSettingsSheet (设置面板)
    ├── AddCourseSheet (添加课程面板)
    ├── PeriodSelector (节次选择器)
    └── TemplateSelector (模板选择器)
```

### 数据流

```
用户操作 → ViewModel → Repository → 数据库/DataStore
                ↓
            UI更新 ← StateFlow
```

## 组件和接口

### 1. 数据模型

#### 1.1 CourseSettings

**职责：** 存储课程表配置信息

```kotlin
data class CourseSettings(
    val periodsPerDay: Int = 8,              // 每天课程节数
    val periodDuration: Int = 45,            // 课程时长（分钟）
    val breakDuration: Int = 10,             // 课间休息（分钟）
    val firstPeriodStartTime: LocalTime = LocalTime.of(8, 0),  // 第一节课开始时间
    val lunchBreakAfterPeriod: Int? = 4,     // 午休在第几节课后
    val lunchBreakDuration: Int = 90,        // 午休时长（分钟）
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
```

#### 1.2 CoursePeriod

**职责：** 表示课程节次信息

```kotlin
data class CoursePeriod(
    val periodNumber: Int,                   // 节次编号（1-based）
    val startTime: LocalTime,                // 开始时间
    val endTime: LocalTime,                  // 结束时间
    val isAfterLunchBreak: Boolean = false   // 是否在午休后
)
```


#### 1.3 ScheduleTemplate

**职责：** 预设的课程表模板

```kotlin
data class ScheduleTemplate(
    val id: String,
    val name: String,
    val description: String,
    val periodsPerDay: Int,
    val periodDuration: Int,
    val breakDuration: Int,
    val firstPeriodStartTime: LocalTime,
    val lunchBreakAfterPeriod: Int?,
    val lunchBreakDuration: Int
)

object ScheduleTemplates {
    val PRIMARY_SCHOOL = ScheduleTemplate(
        id = "primary",
        name = "小学模板",
        description = "6节课/天，40分钟/节",
        periodsPerDay = 6,
        periodDuration = 40,
        breakDuration = 10,
        firstPeriodStartTime = LocalTime.of(8, 0),
        lunchBreakAfterPeriod = 4,
        lunchBreakDuration = 120
    )
    
    val MIDDLE_SCHOOL = ScheduleTemplate(
        id = "middle",
        name = "中学模板",
        description = "8节课/天，45分钟/节",
        periodsPerDay = 8,
        periodDuration = 45,
        breakDuration = 10,
        firstPeriodStartTime = LocalTime.of(8, 0),
        lunchBreakAfterPeriod = 4,
        lunchBreakDuration = 90
    )
    
    val UNIVERSITY = ScheduleTemplate(
        id = "university",
        name = "大学模板",
        description = "5节课/天，50分钟/节",
        periodsPerDay = 5,
        periodDuration = 50,
        breakDuration = 10,
        firstPeriodStartTime = LocalTime.of(8, 30),
        lunchBreakAfterPeriod = 2,
        lunchBreakDuration = 120
    )
    
    val all = listOf(PRIMARY_SCHOOL, MIDDLE_SCHOOL, UNIVERSITY)
}
```

#### 1.4 Course 扩展

**新增字段：**

```kotlin
data class Course(
    // 现有字段...
    val periodStart: Int? = null,            // 开始节次
    val periodEnd: Int? = null,              // 结束节次
    val isCustomTime: Boolean = false        // 是否使用自定义时间
)
```

### 2. UI 组件

#### 2.1 CourseSettingsSheet

**职责：** 课程设置面板，使用 ModalBottomSheet

**主要元素：**
- 标题："课程设置"
- 模板选择区域
- 自定义配置区域
  - 课程节数 Slider（1-12节）
  - 课程时长 Slider（30-120分钟，步进5）
  - 课间休息 Slider（5-30分钟，步进5）
  - 第一节课开始时间 TimePicker
  - 午休设置（可选）
- 时间预览列表
- 保存按钮

**Material 3 设计规范：**
- 使用 `ModalBottomSheet` 组件
- Slider 使用 `MaterialTheme.colorScheme.primary`
- 预览列表使用 `LazyColumn` + `Card`
- 按钮使用 `FilledButton`


#### 2.2 TemplateSelector

**职责：** 模板选择器

**实现方式：** 水平滚动的 Card 列表

**每个模板卡片显示：**
- 模板名称（标题）
- 模板描述（副标题）
- 关键参数（节数、时长）
- 选中状态指示

**样式：**
- 未选中：`Card` with `outlined` style
- 选中：`Card` with `containerColor = primaryContainer`
- 卡片尺寸：宽度 160dp，高度 120dp
- 使用 `HorizontalPager` 或 `LazyRow`

#### 2.3 PeriodSelector

**职责：** 节次选择器，用于添加课程时选择节次

**实现方式：** 网格布局的可选择按钮

**显示内容：**
- 节次编号（如"第1节"）
- 时间范围（如"08:00-08:45"）
- 选中状态
- 已有课程的节次显示为禁用状态

**交互：**
- 单击选择单个节次
- 支持选择连续的多个节次（如第3-4节）
- 使用 `FlowRow` 布局

**样式：**
- 使用 `FilterChip` 组件
- 选中：`selected = true`
- 禁用：`enabled = false`

#### 2.4 AddCourseSheet 增强

**职责：** 添加/编辑课程面板

**新增元素：**
- 时间输入模式切换
  - SegmentedButton：["按节次" | "自定义时间"]
- 按节次模式：
  - PeriodSelector 组件
  - 自动填充开始/结束时间
- 自定义时间模式：
  - 现有的 TimePicker

**布局：**
```
[课程名称输入框]
[时间模式切换]
[节次选择器 / 时间选择器]
[星期选择]
[教师、地点等其他字段]
[保存按钮]
```

#### 2.5 CourseCard 增强

**职责：** 显示课程信息卡片

**新增显示内容：**
- 节次信息（如"第1节" 或 "第3-4节"）
- 节次标签使用 Badge 或 AssistChip

**布局调整：**
```
[颜色条] [课程名称]              [节次标签]
        [时间 | 教师 | 地点]
        [周模式标签]
```


#### 2.6 CourseScheduleView 增强

**职责：** 课程表主视图

**显示优化：**
- 按节次分组显示课程
- 显示节次时间轴
- 空节次显示占位符

**布局方式：**
```
周一
  第1节 (08:00-08:45)
    [课程卡片]
  第2节 (08:55-09:40)
    [空]
  第3-4节 (09:50-11:20)
    [课程卡片]
  ...
```

### 3. ViewModel

#### 3.1 CourseViewModel 扩展

**新增状态：**

```kotlin
data class CourseUiState(
    // 现有字段...
    val settings: CourseSettings = CourseSettings(),
    val periods: List<CoursePeriod> = emptyList(),
    val showSettingsSheet: Boolean = false,
    val showTemplateWarning: Boolean = false
)
```

**新增方法：**

```kotlin
// 设置管理
fun loadSettings()
fun updateSettings(settings: CourseSettings)
fun applyTemplate(template: ScheduleTemplate)

// 节次计算
fun calculatePeriods(settings: CourseSettings): List<CoursePeriod>
fun getPeriodByNumber(periodNumber: Int): CoursePeriod?
fun getAvailablePeriods(dayOfWeek: DayOfWeek): List<CoursePeriod>

// 课程管理增强
fun addCourseByPeriod(
    name: String,
    dayOfWeek: DayOfWeek,
    periodStart: Int,
    periodEnd: Int,
    // 其他参数...
)

// 验证
fun validatePeriodRange(periodStart: Int, periodEnd: Int): Boolean
fun checkPeriodConflict(
    dayOfWeek: DayOfWeek,
    periodStart: Int,
    periodEnd: Int,
    excludeCourseId: Long? = null
): Boolean
```

**节次计算逻辑：**

```kotlin
private fun calculatePeriods(settings: CourseSettings): List<CoursePeriod> {
    val periods = mutableListOf<CoursePeriod>()
    var currentTime = settings.firstPeriodStartTime
    
    for (i in 1..settings.periodsPerDay) {
        val startTime = currentTime
        val endTime = currentTime.plusMinutes(settings.periodDuration.toLong())
        
        periods.add(
            CoursePeriod(
                periodNumber = i,
                startTime = startTime,
                endTime = endTime,
                isAfterLunchBreak = settings.lunchBreakAfterPeriod?.let { i > it } ?: false
            )
        )
        
        // 计算下一节课开始时间
        currentTime = endTime.plusMinutes(settings.breakDuration.toLong())
        
        // 如果是午休前的最后一节课，加上午休时间
        if (settings.lunchBreakAfterPeriod == i) {
            currentTime = currentTime.plusMinutes(settings.lunchBreakDuration.toLong())
        }
    }
    
    return periods
}
```


### 4. 数据层

#### 4.1 CourseSettingsRepository

**职责：** 管理课程设置的持久化

**实现：**

```kotlin
interface CourseSettingsRepository {
    fun getSettings(): Flow<CourseSettings>
    suspend fun updateSettings(settings: CourseSettings)
    suspend fun resetToDefault()
}

class CourseSettingsRepositoryImpl @Inject constructor(
    private val preferencesManager: PreferencesManager
) : CourseSettingsRepository {
    
    override fun getSettings(): Flow<CourseSettings> {
        return preferencesManager.courseSettings
    }
    
    override suspend fun updateSettings(settings: CourseSettings) {
        preferencesManager.setCourseSettings(settings)
    }
    
    override suspend fun resetToDefault() {
        preferencesManager.setCourseSettings(CourseSettings())
    }
}
```

#### 4.2 PreferencesManager 扩展

**新增设置键：**

```kotlin
private object PreferencesKeys {
    // 现有键...
    val COURSE_PERIODS_PER_DAY = intPreferencesKey("course_periods_per_day")
    val COURSE_PERIOD_DURATION = intPreferencesKey("course_period_duration")
    val COURSE_BREAK_DURATION = intPreferencesKey("course_break_duration")
    val COURSE_FIRST_PERIOD_START = stringPreferencesKey("course_first_period_start")
    val COURSE_LUNCH_BREAK_AFTER = intPreferencesKey("course_lunch_break_after")
    val COURSE_LUNCH_BREAK_DURATION = intPreferencesKey("course_lunch_break_duration")
}
```

**新增方法：**

```kotlin
val courseSettings: Flow<CourseSettings>
    get() = dataStore.data.map { preferences ->
        CourseSettings(
            periodsPerDay = preferences[COURSE_PERIODS_PER_DAY] ?: 8,
            periodDuration = preferences[COURSE_PERIOD_DURATION] ?: 45,
            breakDuration = preferences[COURSE_BREAK_DURATION] ?: 10,
            firstPeriodStartTime = preferences[COURSE_FIRST_PERIOD_START]
                ?.let { LocalTime.parse(it) } ?: LocalTime.of(8, 0),
            lunchBreakAfterPeriod = preferences[COURSE_LUNCH_BREAK_AFTER],
            lunchBreakDuration = preferences[COURSE_LUNCH_BREAK_DURATION] ?: 90
        )
    }

suspend fun setCourseSettings(settings: CourseSettings) {
    dataStore.edit { preferences ->
        preferences[COURSE_PERIODS_PER_DAY] = settings.periodsPerDay
        preferences[COURSE_PERIOD_DURATION] = settings.periodDuration
        preferences[COURSE_BREAK_DURATION] = settings.breakDuration
        preferences[COURSE_FIRST_PERIOD_START] = settings.firstPeriodStartTime.toString()
        settings.lunchBreakAfterPeriod?.let {
            preferences[COURSE_LUNCH_BREAK_AFTER] = it
        }
        preferences[COURSE_LUNCH_BREAK_DURATION] = settings.lunchBreakDuration
    }
}
```

#### 4.3 数据库迁移

**Course 表更新：**

```kotlin
val MIGRATION_X_Y = object : Migration(X, Y) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "ALTER TABLE courses ADD COLUMN period_start INTEGER"
        )
        database.execSQL(
            "ALTER TABLE courses ADD COLUMN period_end INTEGER"
        )
        database.execSQL(
            "ALTER TABLE courses ADD COLUMN is_custom_time INTEGER NOT NULL DEFAULT 0"
        )
    }
}
```


## 错误处理

### 错误类型

1. **设置加载失败**
   - 使用默认设置
   - 显示错误提示
   - 记录日志

2. **节次冲突**
   - 检测时间重叠
   - 显示冲突提示对话框
   - 高亮冲突的课程

3. **模板应用警告**
   - 检查是否有课程超出新的节次范围
   - 显示警告对话框列出受影响的课程
   - 提供"继续"和"取消"选项

4. **数据验证失败**
   - 课程节数范围验证（1-12）
   - 时长范围验证（30-120分钟）
   - 节次范围验证（periodStart <= periodEnd）

### 错误恢复策略

```kotlin
sealed class CourseSettingsError {
    object LoadFailed : CourseSettingsError()
    data class ValidationFailed(val message: String) : CourseSettingsError()
    data class PeriodConflict(val conflictingCourses: List<Course>) : CourseSettingsError()
    data class TemplateWarning(val affectedCourses: List<Course>) : CourseSettingsError()
}
```

## 测试策略

### 单元测试

1. **节次计算测试**
   - 基本节次计算
   - 包含午休的节次计算
   - 边界情况（1节课、12节课）
   - 不同时长组合

2. **冲突检测测试**
   - 完全重叠
   - 部分重叠
   - 相邻不冲突
   - 跨节次课程

3. **设置持久化测试**
   - 保存和加载
   - 默认值处理
   - 数据迁移

### UI 测试

1. **设置界面测试**
   - Slider 交互
   - TimePicker 交互
   - 模板选择
   - 预览更新

2. **课程添加测试**
   - 节次模式添加
   - 自定义时间模式添加
   - 模式切换
   - 冲突检测

### 集成测试

1. **端到端流程**
   - 应用模板 → 添加课程 → 显示验证
   - 修改设置 → 课程时间更新
   - 节次冲突 → 错误提示

## 性能考虑

### 优化策略

1. **节次计算缓存**
   - 使用 `remember` 缓存计算结果
   - 只在设置变化时重新计算
   - 使用 `derivedStateOf` 计算派生状态

2. **课程列表渲染**
   - 使用 `LazyColumn` 虚拟滚动
   - 使用 `key` 参数优化重组
   - 最小化状态更新范围

3. **设置加载**
   - 使用 `Flow` 实现响应式更新
   - 延迟加载非关键数据
   - 使用 `StateFlow` 避免重复计算


## 国际化

### 新增字符串资源

```xml
<!-- 课程设置 -->
<string name="course_settings">课程设置</string>
<string name="course_settings_template">选择模板</string>
<string name="course_settings_custom">自定义配置</string>
<string name="course_settings_periods_per_day">每天课程节数</string>
<string name="course_settings_period_duration">课程时长（分钟）</string>
<string name="course_settings_break_duration">课间休息（分钟）</string>
<string name="course_settings_first_period_start">第一节课开始时间</string>
<string name="course_settings_lunch_break">午休设置</string>
<string name="course_settings_lunch_break_after">午休在第几节课后</string>
<string name="course_settings_lunch_break_duration">午休时长（分钟）</string>
<string name="course_settings_preview">时间预览</string>
<string name="course_settings_save">保存设置</string>

<!-- 模板 -->
<string name="template_primary_school">小学模板</string>
<string name="template_primary_school_desc">6节课/天，40分钟/节</string>
<string name="template_middle_school">中学模板</string>
<string name="template_middle_school_desc">8节课/天，45分钟/节</string>
<string name="template_university">大学模板</string>
<string name="template_university_desc">5节课/天，50分钟/节</string>

<!-- 节次 -->
<string name="period_number">第%d节</string>
<string name="period_range">第%d-%d节</string>
<string name="period_time_format">%s - %s</string>

<!-- 添加课程 -->
<string name="add_course_time_mode">时间输入模式</string>
<string name="add_course_by_period">按节次</string>
<string name="add_course_custom_time">自定义时间</string>
<string name="add_course_select_period">选择节次</string>

<!-- 错误和警告 -->
<string name="error_period_conflict">该时间段已有课程</string>
<string name="error_invalid_period_range">节次范围无效</string>
<string name="warning_template_apply_title">应用模板</string>
<string name="warning_template_apply_message">应用此模板将影响 %d 门课程的时间，是否继续？</string>
<string name="warning_affected_courses">受影响的课程：</string>
<string name="action_continue">继续</string>
<string name="action_cancel">取消</string>

<!-- 空状态 -->
<string name="empty_period">空闲</string>
</xml>
```

## 可访问性

### 无障碍支持

1. **内容描述**
   - Slider 添加值描述
   - 节次按钮添加时间描述
   - 模板卡片添加详细描述

2. **语义化标签**
   - 使用 `semantics` 标记重要信息
   - 节次冲突时提供语音提示

3. **触摸目标**
   - 所有可点击元素最小 48dp
   - 节次选择按钮间距至少 8dp

4. **颜色对比**
   - 文本与背景对比度 ≥ 4.5:1
   - 选中状态不仅依赖颜色

## 安全性

### 数据验证

```kotlin
object CourseSettingsValidator {
    fun validatePeriodsPerDay(value: Int): Boolean {
        return value in 1..12
    }
    
    fun validatePeriodDuration(value: Int): Boolean {
        return value in 30..120 && value % 5 == 0
    }
    
    fun validateBreakDuration(value: Int): Boolean {
        return value in 5..30 && value % 5 == 0
    }
    
    fun validatePeriodRange(start: Int, end: Int, maxPeriods: Int): Boolean {
        return start in 1..maxPeriods && 
               end in 1..maxPeriods && 
               start <= end
    }
}
```

### 数据保护

- 设置数据使用 DataStore 安全存储
- 支持现有的加密和同步机制
- 课程数据与设置数据分离存储


## 依赖关系

### 现有依赖

- Hilt（依赖注入）
- Room（数据库）
- DataStore（设置存储）
- Compose（UI）
- Coroutines（异步）

### 新增依赖

无需新增外部依赖，使用现有技术栈。

## 迁移策略

### 数据库迁移

**Course 表更新：**

```kotlin
val MIGRATION_X_Y = object : Migration(X, Y) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // 添加新字段
        database.execSQL(
            "ALTER TABLE courses ADD COLUMN period_start INTEGER"
        )
        database.execSQL(
            "ALTER TABLE courses ADD COLUMN period_end INTEGER"
        )
        database.execSQL(
            "ALTER TABLE courses ADD COLUMN is_custom_time INTEGER NOT NULL DEFAULT 1"
        )
        
        // 现有课程默认使用自定义时间模式
        database.execSQL(
            "UPDATE courses SET is_custom_time = 1"
        )
    }
}
```

### 设置迁移

- 首次启动使用默认设置（中学模板）
- 现有课程保持不变，标记为自定义时间
- 新添加的课程可选择节次模式

### 向后兼容

- 保持现有 Course 模型的所有字段
- 新字段为可选（nullable）
- 支持两种时间输入模式共存

## 实现阶段

### 阶段1：数据模型和设置存储
- 创建 CourseSettings 数据模型
- 实现 CourseSettingsRepository
- 扩展 PreferencesManager
- 数据库迁移

### 阶段2：节次计算逻辑
- 实现节次计算算法
- 实现冲突检测逻辑
- 添加数据验证
- 单元测试

### 阶段3：设置界面
- 实现 CourseSettingsSheet
- 实现 TemplateSelector
- 实现 Slider 和 TimePicker
- 实现时间预览列表

### 阶段4：课程添加增强
- 实现 PeriodSelector 组件
- 扩展 AddCourseSheet
- 实现模式切换
- 集成节次选择逻辑

### 阶段5：课程表视图优化
- 更新 CourseCard 显示节次
- 优化 CourseScheduleView 布局
- 实现按节次分组显示
- 应用 M3 设计规范

### 阶段6：测试和优化
- 编写单元测试
- 进行 UI 测试
- 性能优化
- 无障碍测试

## 设计决策

### 为什么使用 ModalBottomSheet 作为设置界面？
- 符合 Material 3 设计规范
- 提供更好的移动端体验
- 可以容纳较多的设置项
- 不打断用户当前流程

### 为什么提供预设模板？
- 快速配置常见场景
- 降低用户学习成本
- 提供最佳实践参考
- 减少配置错误

### 为什么支持两种时间输入模式？
- 满足不同用户需求
- 节次模式适合规律课程
- 自定义模式适合特殊安排
- 提供更大的灵活性

### 为什么节次范围是 1-12？
- 覆盖绝大多数学校场景
- 避免过于复杂的配置
- 保持界面简洁
- 可以通过自定义时间模式处理特殊情况

### 为什么要显示时间预览？
- 帮助用户理解配置效果
- 及时发现配置错误
- 提供即时反馈
- 增强用户信心

### 为什么午休设置是可选的？
- 不是所有学校都有午休
- 大学课程通常没有固定午休
- 提供更大的灵活性
- 简化配置流程

## Material 3 设计细节

### 颜色系统

```kotlin
// 课程卡片
containerColor = MaterialTheme.colorScheme.surfaceVariant
contentColor = MaterialTheme.colorScheme.onSurfaceVariant

// 节次选择器（选中）
containerColor = MaterialTheme.colorScheme.primaryContainer
contentColor = MaterialTheme.colorScheme.onPrimaryContainer

// 节次选择器（未选中）
containerColor = MaterialTheme.colorScheme.surface
contentColor = MaterialTheme.colorScheme.onSurface

// 模板卡片（选中）
containerColor = MaterialTheme.colorScheme.primaryContainer
borderColor = MaterialTheme.colorScheme.primary

// 模板卡片（未选中）
containerColor = MaterialTheme.colorScheme.surface
borderColor = MaterialTheme.colorScheme.outline
```

### 排版系统

```kotlin
// 标题
titleLarge: 22sp, Medium weight
titleMedium: 16sp, Medium weight

// 正文
bodyLarge: 16sp, Regular weight
bodyMedium: 14sp, Regular weight

// 标签
labelLarge: 14sp, Medium weight
labelMedium: 12sp, Medium weight
```

### 圆角和间距

```kotlin
// 圆角
Card: RoundedCornerShape(16.dp)
Button: RoundedCornerShape(20.dp)
Chip: RoundedCornerShape(8.dp)

// 间距
extraSmall: 4.dp
small: 8.dp
medium: 16.dp
large: 24.dp
extraLarge: 32.dp
```

### 动画

```kotlin
// 展开/收起动画
AnimatedVisibility(
    enter = fadeIn() + expandVertically(),
    exit = fadeOut() + shrinkVertically()
)

// 值变化动画
animateFloatAsState(
    targetValue = value,
    animationSpec = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )
)
```
