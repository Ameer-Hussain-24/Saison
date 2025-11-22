# 课程周数选择器增强设计文档

## 概述

本设计文档描述了课程周数选择器的增强方案，提供可视化的周数选择界面，支持精确选择课程在哪些周上课。该功能将提供周数模式和日期模式两种选择方式，并集成学期设置功能。

## 架构

### 组件层次结构

```
CourseScreen
├── CourseViewModel
│   ├── CourseRepository
│   ├── SemesterSettingsRepository (新增)
│   └── WeekCalculator (新增)
└── UI组件
    ├── WeekSelectorDialog (新增)
    ├── WeekNumberGrid (新增)
    ├── QuickSelectionBar (新增)
    ├── DateRangeSelector (新增)
    ├── SemesterSettingsSheet (新增)
    └── CurrentWeekIndicator (新增)
```

### 数据流

```
用户选择周数 → WeekSelectorDialog → ViewModel → Repository → 数据库
                                        ↓
学期设置 → WeekCalculator → 计算当前周数 → UI更新
```

## 数据模型

### 1. SemesterSettings

**职责：** 存储学期配置信息

```kotlin
data class SemesterSettings(
    val semesterStartDate: LocalDate = LocalDate.now(),  // 学期开始日期
    val totalWeeks: Int = 18,                            // 学期总周数
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
```

### 2. Course 扩展

**新增字段：**

```kotlin
data class Course(
    // 现有字段...
    val customWeeks: List<Int>? = null,  // 自定义周数列表（如 [1,3,5,7]）
)
```


### 3. WeekPattern 扩展

**新增枚举值：**

```kotlin
enum class WeekPattern {
    ALL,      // 全周
    A,        // A周
    B,        // B周
    ODD,      // 单周
    EVEN,     // 双周
    CUSTOM;   // 自定义周数（新增）
    
    companion object {
        fun fromString(value: String): WeekPattern {
            return entries.find { it.name == value } ?: ALL
        }
    }
}
```

### 4. WeekSelectionState

**职责：** 管理周数选择状态

```kotlin
data class WeekSelectionState(
    val selectedWeeks: Set<Int> = emptySet(),           // 选中的周数
    val quickMode: QuickSelectionMode? = null,          // 快捷模式
    val totalWeeks: Int = 18,                           // 总周数
    val selectionMode: SelectionMode = SelectionMode.WEEK_NUMBER  // 选择模式
)

enum class QuickSelectionMode {
    ALL,      // 全周
    ODD,      // 单周
    EVEN      // 双周
}

enum class SelectionMode {
    WEEK_NUMBER,  // 周数模式
    DATE_RANGE    // 日期模式
}
```

## UI 组件设计

### 1. WeekSelectorDialog

**职责：** 周数选择对话框主容器

**布局结构：**
```
┌─────────────────────────────────┐
│ [请选择周数] [日期模式]          │  ← TabRow
├─────────────────────────────────┤
│ [全周] [单周] [双周]             │  ← QuickSelectionBar
├─────────────────────────────────┤
│ ┌───┬───┬───┬───┬───┬───┐       │
│ │ 1 │ 2 │ 3 │ 4 │ 5 │ 6 │       │  ← WeekNumberGrid
│ ├───┼───┼───┼───┼───┼───┤       │
│ │ 7 │ 8 │ 9 │10 │11 │12 │       │
│ ├───┼───┼───┼───┼───┼───┤       │
│ │13 │14 │15 │16 │17 │18 │       │
│ └───┴───┴───┴───┴───┴───┘       │
├─────────────────────────────────┤
│ 已选择 8 周                      │  ← 统计信息
├─────────────────────────────────┤
│ [取消]              [确定]       │  ← 操作按钮
└─────────────────────────────────┘
```

**实现：**

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeekSelectorDialog(
    initialWeekPattern: WeekPattern,
    initialCustomWeeks: List<Int>?,
    totalWeeks: Int,
    onDismiss: () -> Unit,
    onConfirm: (WeekPattern, List<Int>?) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectionMode by remember { mutableStateOf(SelectionMode.WEEK_NUMBER) }
    var selectedWeeks by remember { 
        mutableStateOf(
            when (initialWeekPattern) {
                WeekPattern.ALL -> (1..totalWeeks).toSet()
                WeekPattern.ODD -> (1..totalWeeks).filter { it % 2 == 1 }.toSet()
                WeekPattern.EVEN -> (1..totalWeeks).filter { it % 2 == 0 }.toSet()
                WeekPattern.CUSTOM -> initialCustomWeeks?.toSet() ?: emptySet()
                else -> emptySet()
            }
        )
    }
    var quickMode by remember { 
        mutableStateOf(
            when (initialWeekPattern) {
                WeekPattern.ALL -> QuickSelectionMode.ALL
                WeekPattern.ODD -> QuickSelectionMode.ODD
                WeekPattern.EVEN -> QuickSelectionMode.EVEN
                else -> null
            }
        )
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // 标题
                Text(
                    text = "选择上课周数",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // 模式切换 TabRow
                TabRow(
                    selectedTabIndex = if (selectionMode == SelectionMode.WEEK_NUMBER) 0 else 1
                ) {
                    Tab(
                        selected = selectionMode == SelectionMode.WEEK_NUMBER,
                        onClick = { selectionMode = SelectionMode.WEEK_NUMBER },
                        text = { Text("请选择周数") }
                    )
                    Tab(
                        selected = selectionMode == SelectionMode.DATE_RANGE,
                        onClick = { selectionMode = SelectionMode.DATE_RANGE },
                        text = { Text("日期模式") }
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 内容区域
                when (selectionMode) {
                    SelectionMode.WEEK_NUMBER -> {
                        WeekNumberSelectionContent(
                            selectedWeeks = selectedWeeks,
                            quickMode = quickMode,
                            totalWeeks = totalWeeks,
                            onWeekToggle = { week ->
                                selectedWeeks = if (week in selectedWeeks) {
                                    selectedWeeks - week
                                } else {
                                    selectedWeeks + week
                                }
                                quickMode = null  // 手动选择后取消快捷模式
                            },
                            onQuickModeSelect = { mode ->
                                quickMode = mode
                                selectedWeeks = when (mode) {
                                    QuickSelectionMode.ALL -> (1..totalWeeks).toSet()
                                    QuickSelectionMode.ODD -> (1..totalWeeks).filter { it % 2 == 1 }.toSet()
                                    QuickSelectionMode.EVEN -> (1..totalWeeks).filter { it % 2 == 0 }.toSet()
                                }
                            }
                        )
                    }
                    SelectionMode.DATE_RANGE -> {
                        DateRangeSelectionContent(
                            onDateRangeSelected = { startDate, endDate ->
                                // TODO: 根据日期范围计算周数
                            }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 统计信息
                Text(
                    text = "已选择 ${selectedWeeks.size} 周",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 操作按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("取消")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val weekPattern = when {
                                quickMode == QuickSelectionMode.ALL -> WeekPattern.ALL
                                quickMode == QuickSelectionMode.ODD -> WeekPattern.ODD
                                quickMode == QuickSelectionMode.EVEN -> WeekPattern.EVEN
                                else -> WeekPattern.CUSTOM
                            }
                            val customWeeks = if (weekPattern == WeekPattern.CUSTOM) {
                                selectedWeeks.sorted()
                            } else null
                            onConfirm(weekPattern, customWeeks)
                        },
                        enabled = selectedWeeks.isNotEmpty()
                    ) {
                        Text("确定")
                    }
                }
            }
        }
    }
}
```


### 2. WeekNumberSelectionContent

**职责：** 周数选择内容区域

**实现：**

```kotlin
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WeekNumberSelectionContent(
    selectedWeeks: Set<Int>,
    quickMode: QuickSelectionMode?,
    totalWeeks: Int,
    onWeekToggle: (Int) -> Unit,
    onQuickModeSelect: (QuickSelectionMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // 快捷选择按钮
        QuickSelectionBar(
            selectedMode = quickMode,
            onModeSelect = onQuickModeSelect
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 周数网格
        WeekNumberGrid(
            selectedWeeks = selectedWeeks,
            totalWeeks = totalWeeks,
            onWeekToggle = onWeekToggle
        )
    }
}
```

### 3. QuickSelectionBar

**职责：** 快捷选择按钮栏

**实现：**

```kotlin
@Composable
private fun QuickSelectionBar(
    selectedMode: QuickSelectionMode?,
    onModeSelect: (QuickSelectionMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selectedMode == QuickSelectionMode.ALL,
            onClick = { onModeSelect(QuickSelectionMode.ALL) },
            label = { Text("全周") },
            modifier = Modifier.weight(1f)
        )
        FilterChip(
            selected = selectedMode == QuickSelectionMode.ODD,
            onClick = { onModeSelect(QuickSelectionMode.ODD) },
            label = { Text("单周") },
            modifier = Modifier.weight(1f)
        )
        FilterChip(
            selected = selectedMode == QuickSelectionMode.EVEN,
            onClick = { onModeSelect(QuickSelectionMode.EVEN) },
            label = { Text("双周") },
            modifier = Modifier.weight(1f)
        )
    }
}
```

### 4. WeekNumberGrid

**职责：** 周数网格选择器

**实现：**

```kotlin
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WeekNumberGrid(
    selectedWeeks: Set<Int>,
    totalWeeks: Int,
    onWeekToggle: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        maxItemsInEachRow = 6
    ) {
        for (week in 1..totalWeeks) {
            WeekNumberButton(
                weekNumber = week,
                isSelected = week in selectedWeeks,
                onClick = { onWeekToggle(week) }
            )
        }
    }
}
```

### 5. WeekNumberButton

**职责：** 单个周数按钮

**实现：**

```kotlin
@Composable
private fun WeekNumberButton(
    weekNumber: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    
    val contentColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    Surface(
        onClick = onClick,
        modifier = modifier.size(48.dp),
        shape = CircleShape,
        color = containerColor,
        contentColor = contentColor
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = weekNumber.toString(),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}
```

### 6. DateRangeSelectionContent

**职责：** 日期范围选择内容区域

**实现：**

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateRangeSelectionContent(
    onDateRangeSelected: (LocalDate, LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    var startDate by remember { mutableStateOf<LocalDate?>(null) }
    var endDate by remember { mutableStateOf<LocalDate?>(null) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 开始日期
        OutlinedCard(
            onClick = { showStartDatePicker = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "开始日期",
                        style = MaterialTheme.typography.labelMedium
                    )
                    Text(
                        text = startDate?.toString() ?: "请选择",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null
                )
            }
        }
        
        // 结束日期
        OutlinedCard(
            onClick = { showEndDatePicker = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "结束日期",
                        style = MaterialTheme.typography.labelMedium
                    )
                    Text(
                        text = endDate?.toString() ?: "请选择",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null
                )
            }
        }
        
        // 重复模式
        Text("重复模式", style = MaterialTheme.typography.titleSmall)
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier.fillMaxWidth()
        ) {
            SegmentedButton(
                selected = true,
                onClick = { },
                shape = SegmentedButtonDefaults.itemShape(0, 3)
            ) {
                Text("每周")
            }
            SegmentedButton(
                selected = false,
                onClick = { },
                shape = SegmentedButtonDefaults.itemShape(1, 3)
            ) {
                Text("单周")
            }
            SegmentedButton(
                selected = false,
                onClick = { },
                shape = SegmentedButtonDefaults.itemShape(2, 3)
            ) {
                Text("双周")
            }
        }
    }
    
    // DatePicker 对话框
    if (showStartDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = { showStartDatePicker = false }) {
                    Text("确定")
                }
            }
        ) {
            DatePicker(state = rememberDatePickerState())
        }
    }
    
    if (showEndDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = { showEndDatePicker = false }) {
                    Text("确定")
                }
            }
        ) {
            DatePicker(state = rememberDatePickerState())
        }
    }
}
```


### 7. CurrentWeekIndicator

**职责：** 显示当前周数

**实现：**

```kotlin
@Composable
fun CurrentWeekIndicator(
    currentWeek: Int,
    totalWeeks: Int,
    showAllCourses: Boolean,
    onToggleShowAll: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "第 $currentWeek 周",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "共 $totalWeeks 周",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            FilterChip(
                selected = !showAllCourses,
                onClick = { onToggleShowAll(!showAllCourses) },
                label = { Text(if (showAllCourses) "显示所有" else "仅本周") }
            )
        }
    }
}
```

## ViewModel 扩展

### CourseViewModel 新增方法

```kotlin
class CourseViewModel @Inject constructor(
    private val courseRepository: CourseRepository,
    private val semesterSettingsRepository: SemesterSettingsRepository,
    private val weekCalculator: WeekCalculator
) : ViewModel() {
    
    // 学期设置
    val semesterSettings: StateFlow<SemesterSettings> = 
        semesterSettingsRepository.getSettings()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = SemesterSettings()
            )
    
    // 当前周数
    val currentWeek: StateFlow<Int> = combine(
        semesterSettings,
        flow { emit(LocalDate.now()) }
    ) { settings, today ->
        weekCalculator.calculateCurrentWeek(settings.semesterStartDate, today)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 1
    )
    
    // 是否显示所有课程
    private val _showAllCourses = MutableStateFlow(false)
    val showAllCourses: StateFlow<Boolean> = _showAllCourses.asStateFlow()
    
    // 过滤后的课程列表
    val filteredCourses: StateFlow<List<Course>> = combine(
        courses,
        currentWeek,
        showAllCourses
    ) { courseList, week, showAll ->
        if (showAll) {
            courseList
        } else {
            courseList.filter { course ->
                isCoursActiveInWeek(course, week)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    // 判断课程在某周是否上课
    private fun isCourseActiveInWeek(course: Course, week: Int): Boolean {
        return when (course.weekPattern) {
            WeekPattern.ALL -> true
            WeekPattern.ODD -> week % 2 == 1
            WeekPattern.EVEN -> week % 2 == 0
            WeekPattern.CUSTOM -> course.customWeeks?.contains(week) ?: false
            WeekPattern.A, WeekPattern.B -> true  // 简化处理
        }
    }
    
    // 更新学期设置
    fun updateSemesterSettings(settings: SemesterSettings) {
        viewModelScope.launch {
            semesterSettingsRepository.updateSettings(settings)
        }
    }
    
    // 切换显示模式
    fun toggleShowAllCourses() {
        _showAllCourses.value = !_showAllCourses.value
    }
    
    // 添加课程（支持自定义周数）
    fun addCourseWithCustomWeeks(
        name: String,
        dayOfWeek: DayOfWeek,
        startTime: LocalTime,
        endTime: LocalTime,
        weekPattern: WeekPattern,
        customWeeks: List<Int>?,
        // 其他参数...
    ) {
        viewModelScope.launch {
            val course = Course(
                name = name,
                dayOfWeek = dayOfWeek,
                startTime = startTime,
                endTime = endTime,
                weekPattern = weekPattern,
                customWeeks = customWeeks,
                // 其他字段...
            )
            courseRepository.insertCourse(course)
        }
    }
}
```

## 工具类

### WeekCalculator

**职责：** 周数计算工具

```kotlin
class WeekCalculator {
    /**
     * 计算当前是第几周
     */
    fun calculateCurrentWeek(semesterStartDate: LocalDate, currentDate: LocalDate): Int {
        val daysBetween = ChronoUnit.DAYS.between(semesterStartDate, currentDate)
        return (daysBetween / 7).toInt() + 1
    }
    
    /**
     * 根据日期范围计算周数列表
     */
    fun calculateWeeksFromDateRange(
        semesterStartDate: LocalDate,
        rangeStartDate: LocalDate,
        rangeEndDate: LocalDate
    ): List<Int> {
        val startWeek = calculateCurrentWeek(semesterStartDate, rangeStartDate)
        val endWeek = calculateCurrentWeek(semesterStartDate, rangeEndDate)
        return (startWeek..endWeek).toList()
    }
    
    /**
     * 获取某周的日期范围
     */
    fun getWeekDateRange(semesterStartDate: LocalDate, weekNumber: Int): Pair<LocalDate, LocalDate> {
        val weekStartDate = semesterStartDate.plusWeeks((weekNumber - 1).toLong())
        val weekEndDate = weekStartDate.plusDays(6)
        return weekStartDate to weekEndDate
    }
}
```

## 数据层

### 1. SemesterSettingsRepository

**职责：** 管理学期设置

```kotlin
interface SemesterSettingsRepository {
    fun getSettings(): Flow<SemesterSettings>
    suspend fun updateSettings(settings: SemesterSettings)
}

class SemesterSettingsRepositoryImpl @Inject constructor(
    private val preferencesManager: PreferencesManager
) : SemesterSettingsRepository {
    
    override fun getSettings(): Flow<SemesterSettings> {
        return preferencesManager.semesterSettings
    }
    
    override suspend fun updateSettings(settings: SemesterSettings) {
        preferencesManager.setSemesterSettings(settings)
    }
}
```

### 2. PreferencesManager 扩展

**新增设置键：**

```kotlin
private object PreferencesKeys {
    // 现有键...
    val SEMESTER_START_DATE = stringPreferencesKey("semester_start_date")
    val SEMESTER_TOTAL_WEEKS = intPreferencesKey("semester_total_weeks")
}

val semesterSettings: Flow<SemesterSettings>
    get() = dataStore.data.map { preferences ->
        SemesterSettings(
            semesterStartDate = preferences[SEMESTER_START_DATE]
                ?.let { LocalDate.parse(it) } ?: LocalDate.now(),
            totalWeeks = preferences[SEMESTER_TOTAL_WEEKS] ?: 18
        )
    }

suspend fun setSemesterSettings(settings: SemesterSettings) {
    dataStore.edit { preferences ->
        preferences[SEMESTER_START_DATE] = settings.semesterStartDate.toString()
        preferences[SEMESTER_TOTAL_WEEKS] = settings.totalWeeks
    }
}
```

### 3. 数据库迁移

**Course 表更新：**

```kotlin
val MIGRATION_X_Y = object : Migration(X, Y) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // 添加 customWeeks 字段
        database.execSQL(
            "ALTER TABLE courses ADD COLUMN custom_weeks TEXT"
        )
    }
}
```

### 4. Room Converter

**自定义周数列表转换器：**

```kotlin
class Converters {
    private val gson = Gson()
    
    @TypeConverter
    fun fromWeekList(weeks: List<Int>?): String? {
        return weeks?.let { gson.toJson(it) }
    }
    
    @TypeConverter
    fun toWeekList(weeksJson: String?): List<Int>? {
        return weeksJson?.let {
            val type = object : TypeToken<List<Int>>() {}.type
            gson.fromJson(it, type)
        }
    }
}
```


## 集成到现有组件

### 1. AddCourseSheet 集成

**修改 WeekDetailDialog 调用：**

```kotlin
// 在 AddCourseSheet 中
if (showWeekDetailDialog) {
    WeekSelectorDialog(
        initialWeekPattern = weekPattern,
        initialCustomWeeks = customWeeks,
        totalWeeks = semesterSettings.totalWeeks,
        onDismiss = { showWeekDetailDialog = false },
        onConfirm = { pattern, weeks ->
            weekPattern = pattern
            customWeeks = weeks
            showWeekDetailDialog = false
        }
    )
}
```

### 2. CourseCard 显示周数信息

**扩展 CourseCard 显示：**

```kotlin
@Composable
fun CourseCard(
    course: Course,
    currentWeek: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 颜色条
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(48.dp)
                    .background(
                        color = Color(course.color),
                        shape = RoundedCornerShape(2.dp)
                    )
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = course.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    // 周数标签
                    WeekPatternChip(
                        weekPattern = course.weekPattern,
                        customWeeks = course.customWeeks,
                        currentWeek = currentWeek
                    )
                }
                
                // 时间、教师、地点等信息
                // ...
            }
        }
    }
}

@Composable
private fun WeekPatternChip(
    weekPattern: WeekPattern,
    customWeeks: List<Int>?,
    currentWeek: Int,
    modifier: Modifier = Modifier
) {
    val text = when (weekPattern) {
        WeekPattern.ALL -> "全周"
        WeekPattern.ODD -> "单周"
        WeekPattern.EVEN -> "双周"
        WeekPattern.CUSTOM -> {
            customWeeks?.let { weeks ->
                if (weeks.size <= 3) {
                    "第${weeks.joinToString(",")}周"
                } else {
                    "自定义"
                }
            } ?: "自定义"
        }
        else -> weekPattern.name
    }
    
    // 判断本周是否有课
    val isActiveThisWeek = when (weekPattern) {
        WeekPattern.ALL -> true
        WeekPattern.ODD -> currentWeek % 2 == 1
        WeekPattern.EVEN -> currentWeek % 2 == 0
        WeekPattern.CUSTOM -> customWeeks?.contains(currentWeek) ?: false
        else -> true
    }
    
    AssistChip(
        onClick = { },
        label = { Text(text) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = if (isActiveThisWeek) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        modifier = modifier
    )
}
```

### 3. CourseScreen 添加当前周指示器

```kotlin
@Composable
fun CourseScreen(
    viewModel: CourseViewModel = hiltViewModel()
) {
    val courses by viewModel.filteredCourses.collectAsState()
    val currentWeek by viewModel.currentWeek.collectAsState()
    val semesterSettings by viewModel.semesterSettings.collectAsState()
    val showAllCourses by viewModel.showAllCourses.collectAsState()
    
    Column(modifier = Modifier.fillMaxSize()) {
        // 当前周指示器
        CurrentWeekIndicator(
            currentWeek = currentWeek,
            totalWeeks = semesterSettings.totalWeeks,
            showAllCourses = showAllCourses,
            onToggleShowAll = { viewModel.toggleShowAllCourses() },
            modifier = Modifier.padding(16.dp)
        )
        
        // 课程列表
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(courses) { course ->
                CourseCard(
                    course = course,
                    currentWeek = currentWeek,
                    onClick = { /* 导航到详情 */ }
                )
            }
        }
    }
}
```

## 错误处理

### 错误类型

1. **周数超出范围**
   - 验证选择的周数不超过学期总周数
   - 显示错误提示

2. **日期范围无效**
   - 验证结束日期晚于开始日期
   - 验证日期在学期范围内

3. **数据加载失败**
   - 使用默认学期设置
   - 显示错误提示

### 验证逻辑

```kotlin
object WeekSelectionValidator {
    fun validateWeekNumbers(weeks: List<Int>, totalWeeks: Int): ValidationResult {
        val invalidWeeks = weeks.filter { it < 1 || it > totalWeeks }
        return if (invalidWeeks.isEmpty()) {
            ValidationResult.Success
        } else {
            ValidationResult.Error("周数 ${invalidWeeks.joinToString(",")} 超出范围")
        }
    }
    
    fun validateDateRange(startDate: LocalDate, endDate: LocalDate): ValidationResult {
        return if (endDate.isAfter(startDate)) {
            ValidationResult.Success
        } else {
            ValidationResult.Error("结束日期必须晚于开始日期")
        }
    }
}

sealed class ValidationResult {
    object Success : ValidationResult()
    data class Error(val message: String) : ValidationResult()
}
```

## 测试策略

### 单元测试

1. **WeekCalculator 测试**
   - 当前周数计算
   - 日期范围转周数
   - 边界情况

2. **周数过滤测试**
   - 全周模式过滤
   - 单周模式过滤
   - 双周模式过滤
   - 自定义周数过滤

3. **数据转换测试**
   - List<Int> 与 JSON 互转
   - WeekPattern 映射

### UI 测试

1. **周数选择器测试**
   - 点击选择周数
   - 快捷模式切换
   - 统计信息更新

2. **过滤功能测试**
   - 切换显示模式
   - 课程列表更新

## 性能优化

### 优化策略

1. **周数计算缓存**
   - 使用 `remember` 缓存计算结果
   - 只在学期设置变化时重新计算

2. **课程过滤优化**
   - 使用 `derivedStateOf` 避免不必要的重组
   - 在后台线程进行过滤

3. **UI 渲染优化**
   - 使用 `key` 参数优化列表渲染
   - 延迟加载非关键数据

## 国际化

### 新增字符串资源

```xml
<!-- 周数选择器 -->
<string name="week_selector_title">选择上课周数</string>
<string name="week_selector_tab_week_number">请选择周数</string>
<string name="week_selector_tab_date_range">日期模式</string>
<string name="week_selector_quick_all">全周</string>
<string name="week_selector_quick_odd">单周</string>
<string name="week_selector_quick_even">双周</string>
<string name="week_selector_selected_count">已选择 %d 周</string>
<string name="week_selector_confirm">确定</string>
<string name="week_selector_cancel">取消</string>

<!-- 日期模式 -->
<string name="date_range_start_date">开始日期</string>
<string name="date_range_end_date">结束日期</string>
<string name="date_range_repeat_mode">重复模式</string>
<string name="date_range_repeat_weekly">每周</string>
<string name="date_range_repeat_odd">单周</string>
<string name="date_range_repeat_even">双周</string>

<!-- 当前周指示器 -->
<string name="current_week_indicator">第 %d 周</string>
<string name="total_weeks_indicator">共 %d 周</string>
<string name="show_all_courses">显示所有</string>
<string name="show_current_week_only">仅本周</string>

<!-- 周数标签 -->
<string name="week_pattern_all">全周</string>
<string name="week_pattern_odd">单周</string>
<string name="week_pattern_even">双周</string>
<string name="week_pattern_custom">自定义</string>
<string name="week_pattern_custom_detail">第%s周</string>

<!-- 学期设置 -->
<string name="semester_settings">学期设置</string>
<string name="semester_start_date">学期开始日期</string>
<string name="semester_total_weeks">学期总周数</string>

<!-- 错误提示 -->
<string name="error_no_weeks_selected">请至少选择一周</string>
<string name="error_week_out_of_range">周数超出范围</string>
<string name="error_invalid_date_range">日期范围无效</string>
```

## Material 3 设计细节

### 颜色系统

```kotlin
// 周数按钮（选中）
containerColor = MaterialTheme.colorScheme.primary
contentColor = MaterialTheme.colorScheme.onPrimary

// 周数按钮（未选中）
containerColor = MaterialTheme.colorScheme.surfaceVariant
contentColor = MaterialTheme.colorScheme.onSurfaceVariant

// 快捷选择按钮（选中）
containerColor = MaterialTheme.colorScheme.secondaryContainer
contentColor = MaterialTheme.colorScheme.onSecondaryContainer

// 当前周指示器
containerColor = MaterialTheme.colorScheme.primaryContainer
contentColor = MaterialTheme.colorScheme.onPrimaryContainer
```

### 尺寸规范

```kotlin
// 周数按钮
size = 48.dp
shape = CircleShape

// 按钮间距
horizontalSpacing = 8.dp
verticalSpacing = 8.dp

// 对话框
minWidth = 320.dp
maxWidth = 560.dp
padding = 24.dp
```

### 动画

```kotlin
// 选中状态动画
animateColorAsState(
    targetValue = if (isSelected) primary else surfaceVariant,
    animationSpec = tween(durationMillis = 200)
)

// 统计信息更新动画
AnimatedContent(
    targetState = selectedCount,
    transitionSpec = {
        slideInVertically { it } + fadeIn() with
        slideOutVertically { -it } + fadeOut()
    }
)
```

## 实现阶段

### 阶段1：数据模型和基础设施
- 扩展 Course 数据模型
- 创建 SemesterSettings 模型
- 实现数据库迁移
- 创建 WeekCalculator 工具类

### 阶段2：周数选择器UI
- 实现 WeekSelectorDialog
- 实现 WeekNumberGrid
- 实现 QuickSelectionBar
- 实现选择逻辑

### 阶段3：日期模式
- 实现 DateRangeSelectionContent
- 集成 DatePicker
- 实现日期转周数逻辑

### 阶段4：ViewModel 集成
- 扩展 CourseViewModel
- 实现过滤逻辑
- 实现学期设置管理

### 阶段5：UI 集成
- 集成到 AddCourseSheet
- 更新 CourseCard 显示
- 添加 CurrentWeekIndicator
- 实现过滤切换

### 阶段6：测试和优化
- 编写单元测试
- 进行 UI 测试
- 性能优化
- 国际化

## 设计决策

### 为什么使用圆形按钮显示周数？
- 符合 Material Design 3 规范
- 视觉上更紧凑
- 易于点击
- 选中状态清晰

### 为什么提供快捷选择模式？
- 满足常见使用场景
- 减少用户操作
- 提高配置效率

### 为什么支持日期模式？
- 提供更直观的选择方式
- 适合不熟悉周数概念的用户
- 增加灵活性

### 为什么需要当前周指示器？
- 帮助用户了解当前进度
- 提供快速过滤功能
- 增强时间感知

### 为什么使用 CUSTOM 枚举值而不是新的数据结构？
- 保持与现有系统的兼容性
- 简化数据模型
- 易于扩展

