# 设计文档 - 事件页面

## 概述

本设计文档描述了事件页面功能的技术实现方案。该功能将为 Saison 应用添加一个专门的事件管理页面，采用 Material 3 设计规范，支持创建、查看和管理不同类型的事件（生日、纪念日、倒数日），并提供智能的时间计算功能。

## 架构

### 整体架构

事件页面将遵循应用现有的 MVVM 架构模式：

```
UI Layer (Compose)
    ├── EventScreen (主页面)
    ├── EventCard (事件卡片组件)
    └── CreateEventSheet (创建事件表单)
    
ViewModel Layer
    └── EventViewModel (状态管理和业务逻辑)
    
Domain Layer
    ├── Event (事件数据模型)
    ├── EventCategory (事件类别枚举)
    └── EventRepository (数据访问接口)
    
Data Layer
    ├── EventDao (数据库访问)
    └── EventRepositoryImpl (仓库实现)
```

### 数据流

1. **读取流程**: EventScreen → EventViewModel → EventRepository → EventDao → Database
2. **写入流程**: CreateEventSheet → EventViewModel → EventRepository → EventDao → Database
3. **状态更新**: Database → Flow → EventViewModel → StateFlow → EventScreen

## 组件和接口

### 1. 数据模型

#### Event 数据类

```kotlin
data class Event(
    val id: Long = 0,
    val title: String,
    val description: String? = null,
    val eventDate: LocalDateTime,
    val category: EventCategory,
    val isCompleted: Boolean = false,
    val reminderEnabled: Boolean = false,
    val reminderTime: LocalDateTime? = null,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)
```

#### EventCategory 枚举

```kotlin
enum class EventCategory(val value: Int) {
    BIRTHDAY(0),      // 生日
    ANNIVERSARY(1),   // 纪念日
    COUNTDOWN(2);     // 倒数日
    
    companion object {
        fun fromValue(value: Int): EventCategory {
            return entries.find { it.value == value } ?: COUNTDOWN
        }
    }
    
    @StringRes
    fun getDisplayNameResId(): Int {
        return when (this) {
            BIRTHDAY -> R.string.event_category_birthday
            ANNIVERSARY -> R.string.event_category_anniversary
            COUNTDOWN -> R.string.event_category_countdown
        }
    }
    
    fun getIcon(): ImageVector {
        return when (this) {
            BIRTHDAY -> Icons.Default.Cake
            ANNIVERSARY -> Icons.Default.Favorite
            COUNTDOWN -> Icons.Default.Event
        }
    }
}
```

#### EventEntity (数据库实体)

```kotlin
@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String?,
    val eventDate: Long, // 存储为时间戳
    val category: Int,
    val isCompleted: Boolean,
    val reminderEnabled: Boolean,
    val reminderTime: Long?,
    val createdAt: Long,
    val updatedAt: Long
)
```

### 2. UI 组件

#### EventScreen (主页面)

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventScreen(
    viewModel: EventViewModel = hiltViewModel(),
    onEventClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val events by viewModel.events.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    
    var showCreateSheet by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            EventTopBar(
                selectedCategory = selectedCategory,
                onCategoryChange = { viewModel.setCategory(it) }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateSheet = true },
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.action_create_event)
                )
            }
        }
    ) { paddingValues ->
        when (uiState) {
            is EventUiState.Loading -> LoadingIndicator()
            is EventUiState.Empty -> EmptyEventState()
            is EventUiState.Success -> {
                EventList(
                    events = events,
                    onEventClick = onEventClick,
                    modifier = Modifier.padding(paddingValues)
                )
            }
            is EventUiState.Error -> ErrorState()
        }
    }
    
    if (showCreateSheet) {
        CreateEventSheet(
            onDismiss = { showCreateSheet = false },
            onEventCreate = { event ->
                viewModel.createEvent(event)
                showCreateSheet = false
            }
        )
    }
}
```

#### EventCard (事件卡片)

```kotlin
@Composable
fun EventCard(
    event: Event,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val daysUntil = calculateDaysUntil(event.eventDate)
    val isPast = daysUntil < 0
    val isToday = daysUntil == 0
    
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPast) {
                MaterialTheme.colorScheme.surfaceVariant
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左侧：类别图标和信息
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = event.category.getIcon(),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
                
                Column {
                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = formatEventDate(event.eventDate),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // 右侧：天数显示
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = when {
                        isToday -> stringResource(R.string.event_today)
                        isPast -> stringResource(
                            R.string.event_days_passed,
                            Math.abs(daysUntil)
                        )
                        else -> stringResource(
                            R.string.event_days_remaining,
                            daysUntil
                        )
                    },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        isToday -> MaterialTheme.colorScheme.primary
                        isPast -> MaterialTheme.colorScheme.onSurfaceVariant
                        else -> MaterialTheme.colorScheme.tertiary
                    }
                )
                
                Text(
                    text = stringResource(event.category.getDisplayNameResId()),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
```

#### CreateEventSheet (创建事件表单)

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventSheet(
    onDismiss: () -> Unit,
    onEventCreate: (Event) -> Unit,
    modifier: Modifier = Modifier
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var selectedTime by remember { mutableStateOf<LocalTime?>(null) }
    var selectedCategory by remember { mutableStateOf(EventCategory.COUNTDOWN) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 标题
            Text(
                text = stringResource(R.string.event_create_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            // 事件标题输入
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(stringResource(R.string.event_title_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            // 事件描述输入
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text(stringResource(R.string.event_description_label)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )
            
            // 事件类别选择
            Text(
                text = stringResource(R.string.event_category_label),
                style = MaterialTheme.typography.titleSmall
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                EventCategory.entries.forEach { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        label = { Text(stringResource(category.getDisplayNameResId())) },
                        leadingIcon = {
                            Icon(
                                imageVector = category.getIcon(),
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                }
            }
            
            // 日期选择
            OutlinedCard(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null
                        )
                        Text(
                            text = selectedDate?.let { formatDate(it) }
                                ?: stringResource(R.string.event_select_date)
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null
                    )
                }
            }
            
            // 时间选择
            OutlinedCard(
                onClick = { showTimePicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null
                        )
                        Text(
                            text = selectedTime?.let { formatTime(it) }
                                ?: stringResource(R.string.event_select_time)
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null
                    )
                }
            }
            
            // 操作按钮
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.action_cancel))
                }
                
                Button(
                    onClick = {
                        if (title.isNotBlank() && selectedDate != null) {
                            val eventDateTime = LocalDateTime.of(
                                selectedDate,
                                selectedTime ?: LocalTime.of(0, 0)
                            )
                            onEventCreate(
                                Event(
                                    title = title,
                                    description = description.takeIf { it.isNotBlank() },
                                    eventDate = eventDateTime,
                                    category = selectedCategory,
                                    createdAt = LocalDateTime.now(),
                                    updatedAt = LocalDateTime.now()
                                )
                            )
                        }
                    },
                    enabled = title.isNotBlank() && selectedDate != null,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.action_save))
                }
            }
        }
    }
    
    // 日期选择器对话框
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            onDateSelected = { date ->
                selectedDate = date
                showDatePicker = false
            }
        )
    }
    
    // 时间选择器对话框
    if (showTimePicker) {
        TimePickerDialog(
            onDismissRequest = { showTimePicker = false },
            onTimeSelected = { time ->
                selectedTime = time
                showTimePicker = false
            }
        )
    }
}
```

### 3. ViewModel

#### EventViewModel

```kotlin
@HiltViewModel
class EventViewModel @Inject constructor(
    private val eventRepository: EventRepository
) : ViewModel() {
    
    private val _selectedCategory = MutableStateFlow<EventCategory?>(null)
    val selectedCategory: StateFlow<EventCategory?> = _selectedCategory.asStateFlow()
    
    private val _uiState = MutableStateFlow<EventUiState>(EventUiState.Loading)
    val uiState: StateFlow<EventUiState> = _uiState.asStateFlow()
    
    val events: StateFlow<List<Event>> = combine(
        eventRepository.getAllEvents(),
        _selectedCategory
    ) { allEvents, category ->
        if (category == null) {
            allEvents
        } else {
            allEvents.filter { it.category == category }
        }
    }.map { events ->
        // 按日期排序：未来事件在前，过去事件在后
        events.sortedWith(
            compareBy(
                { it.eventDate < LocalDateTime.now() }, // 过去的事件排后面
                { it.eventDate } // 按日期升序
            )
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    init {
        loadEvents()
    }
    
    private fun loadEvents() {
        viewModelScope.launch {
            _uiState.value = EventUiState.Loading
            try {
                events.collect { eventList ->
                    _uiState.value = if (eventList.isEmpty()) {
                        EventUiState.Empty
                    } else {
                        EventUiState.Success
                    }
                }
            } catch (e: Exception) {
                _uiState.value = EventUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
    
    fun setCategory(category: EventCategory?) {
        _selectedCategory.value = category
    }
    
    fun createEvent(event: Event) {
        viewModelScope.launch {
            eventRepository.insertEvent(event)
        }
    }
    
    fun updateEvent(event: Event) {
        viewModelScope.launch {
            eventRepository.updateEvent(event)
        }
    }
    
    fun deleteEvent(eventId: Long) {
        viewModelScope.launch {
            eventRepository.deleteEvent(eventId)
        }
    }
}

sealed class EventUiState {
    object Loading : EventUiState()
    object Empty : EventUiState()
    object Success : EventUiState()
    data class Error(val message: String) : EventUiState()
}
```

### 4. Repository 和 DAO

#### EventRepository 接口

```kotlin
interface EventRepository {
    fun getAllEvents(): Flow<List<Event>>
    fun getEventById(id: Long): Flow<Event?>
    fun getEventsByCategory(category: EventCategory): Flow<List<Event>>
    suspend fun insertEvent(event: Event): Long
    suspend fun updateEvent(event: Event)
    suspend fun deleteEvent(eventId: Long)
}
```

#### EventDao

```kotlin
@Dao
interface EventDao {
    @Query("SELECT * FROM events ORDER BY eventDate ASC")
    fun getAllEvents(): Flow<List<EventEntity>>
    
    @Query("SELECT * FROM events WHERE id = :id")
    fun getEventById(id: Long): Flow<EventEntity?>
    
    @Query("SELECT * FROM events WHERE category = :category ORDER BY eventDate ASC")
    fun getEventsByCategory(category: Int): Flow<List<EventEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: EventEntity): Long
    
    @Update
    suspend fun updateEvent(event: EventEntity)
    
    @Delete
    suspend fun deleteEvent(event: EventEntity)
    
    @Query("DELETE FROM events WHERE id = :id")
    suspend fun deleteEventById(id: Long)
}
```

## 工具函数

### 天数计算

```kotlin
object EventDateCalculator {
    /**
     * 计算事件距离今天的天数
     * 返回正数表示未来，负数表示过去，0 表示今天
     */
    fun calculateDaysUntil(eventDate: LocalDateTime): Int {
        val today = LocalDate.now()
        val eventDay = eventDate.toLocalDate()
        return ChronoUnit.DAYS.between(today, eventDay).toInt()
    }
    
    /**
     * 格式化天数显示文本
     */
    fun formatDaysText(
        daysUntil: Int,
        context: Context
    ): String {
        return when {
            daysUntil == 0 -> context.getString(R.string.event_today)
            daysUntil > 0 -> context.getString(
                R.string.event_days_remaining,
                daysUntil
            )
            else -> context.getString(
                R.string.event_days_passed,
                Math.abs(daysUntil)
            )
        }
    }
}
```

### 日期格式化

```kotlin
object EventDateFormatter {
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日")
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm")
    
    fun formatDate(date: LocalDate): String {
        return date.format(dateFormatter)
    }
    
    fun formatTime(time: LocalTime): String {
        return time.format(timeFormatter)
    }
    
    fun formatDateTime(dateTime: LocalDateTime): String {
        return dateTime.format(dateTimeFormatter)
    }
}
```

## 错误处理

### 异常类型

1. **EventNotFoundException**: 事件不存在
2. **InvalidEventDataException**: 事件数据无效
3. **DatabaseException**: 数据库操作失败

### 错误处理策略

```kotlin
sealed class EventError {
    object NetworkError : EventError()
    object DatabaseError : EventError()
    data class ValidationError(val field: String, val message: String) : EventError()
    data class UnknownError(val message: String) : EventError()
}

fun handleEventError(error: Throwable): EventError {
    return when (error) {
        is IOException -> EventError.NetworkError
        is SQLException -> EventError.DatabaseError
        is IllegalArgumentException -> EventError.ValidationError(
            field = "unknown",
            message = error.message ?: "Invalid data"
        )
        else -> EventError.UnknownError(error.message ?: "Unknown error occurred")
    }
}
```

## 测试策略

### 单元测试

1. **EventViewModel 测试**
   - 测试事件加载
   - 测试类别筛选
   - 测试事件创建、更新、删除

2. **EventDateCalculator 测试**
   - 测试未来日期计算
   - 测试过去日期计算
   - 测试今天日期计算
   - 测试边界情况

3. **EventRepository 测试**
   - 测试数据库操作
   - 测试数据映射

### UI 测试

1. **EventScreen 测试**
   - 测试事件列表显示
   - 测试空状态显示
   - 测试 FAB 点击

2. **CreateEventSheet 测试**
   - 测试表单输入
   - 测试日期选择
   - 测试保存功能

## 性能优化

### 数据库优化

1. 为 `eventDate` 字段添加索引
2. 为 `category` 字段添加索引
3. 使用分页加载大量事件

### UI 优化

1. 使用 `LazyColumn` 实现列表虚拟化
2. 使用 `remember` 缓存计算结果
3. 使用 `derivedStateOf` 优化状态派生

## 国际化

### 字符串资源

需要添加以下字符串资源（中文、英文、日文、越南文）：

```xml
<!-- 事件相关 -->
<string name="event_screen_title">事件</string>
<string name="event_create_title">创建事件</string>
<string name="event_title_label">事件标题</string>
<string name="event_description_label">事件描述</string>
<string name="event_category_label">事件类别</string>
<string name="event_select_date">选择日期</string>
<string name="event_select_time">选择时间</string>

<!-- 事件类别 -->
<string name="event_category_birthday">生日</string>
<string name="event_category_anniversary">纪念日</string>
<string name="event_category_countdown">倒数日</string>

<!-- 天数显示 -->
<string name="event_today">今天</string>
<string name="event_days_remaining">还有 %d 天</string>
<string name="event_days_passed">已过去 %d 天</string>

<!-- 空状态 -->
<string name="event_empty_title">暂无事件</string>
<string name="event_empty_message">点击右下角按钮创建第一个事件</string>
```

## 依赖项

无需添加新的依赖项，使用现有的：
- Jetpack Compose
- Material 3
- Hilt
- Room
- Kotlin Coroutines
- Flow
