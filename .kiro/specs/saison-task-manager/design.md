# Saison 任务管理应用 - 设计文档

## 概述

Saison 是一款生产级 Android 任务管理应用，采用 Jetpack Compose + Material 3 Extended (M3E) 构建，支持日历视图、课程表、待办事项、番茄钟、节拍器和 WebDAV 同步。应用遵循 MVVM 架构模式，使用 Room 数据库、Kotlin Flow、Hilt 依赖注入，目标 Android 10-15 (API 29-35)。

### 核心设计原则

1. **M3E 优先**：所有 UI 组件使用 Material 3 Extended
2. **响应式设计**：适配手机、平板、折叠屏、桌面模式
3. **多语言支持**：英语、简体中文、日语、越南语
4. **无障碍优先**：完整 TalkBack 支持和语义标签
5. **安全第一**：AES-256-GCM 加密 + Android Keystore
6. **离线优先**：本地数据库 + 可选 WebDAV 同步

## 架构设计

### 整体架构

```
┌─────────────────────────────────────────────────────────┐
│                    UI Layer (Compose)                    │
│  ┌──────────┬──────────┬──────────┬──────────┬────────┐ │
│  │ Calendar │  Course  │   Todo   │ Pomodoro │Settings│ │
│  │  Screen  │  Screen  │  Screen  │  Screen  │ Screen │ │
│  └──────────┴──────────┴──────────┴──────────┴────────┘ │
└─────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────┐
│                   ViewModel Layer                        │
│  ┌──────────┬──────────┬──────────┬──────────┬────────┐ │
│  │ Calendar │  Course  │   Task   │ Pomodoro │ Theme  │ │
│  │ViewModel │ViewModel │ViewModel │ViewModel │Manager │ │
│  └──────────┴──────────┴──────────┴──────────┴────────┘ │
└─────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────┐
│                  Repository Layer                        │
│  ┌──────────┬──────────┬──────────┬──────────┬────────┐ │
│  │   Task   │  Course  │   Tag    │ Pomodoro │WebDAV  │ │
│  │   Repo   │   Repo   │   Repo   │   Repo   │  Repo  │ │
│  └──────────┴──────────┴──────────┴──────────┴────────┘ │
└─────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────┐
│                     Data Layer                           │
│  ┌──────────┬──────────┬──────────┬──────────┬────────┐ │
│  │  Room DB │DataStore │Encrypted │SoundPool │Network │ │
│  │          │          │   File   │          │        │ │
│  └──────────┴──────────┴──────────┴──────────┴────────┘ │
└─────────────────────────────────────────────────────────┘
```

### 模块结构


```
takagi.ru.saison/
├── app/
│   ├── src/main/
│   │   ├── java/takagi/ru/saison/
│   │   │   ├── SaisonApplication.kt
│   │   │   ├── MainActivity.kt
│   │   │   │
│   │   │   ├── ui/
│   │   │   │   ├── theme/
│   │   │   │   │   ├── Theme.kt
│   │   │   │   │   ├── Color.kt
│   │   │   │   │   ├── Type.kt
│   │   │   │   │   └── SeasonalThemes.kt
│   │   │   │   │
│   │   │   │   ├── navigation/
│   │   │   │   │   ├── SaisonNavHost.kt
│   │   │   │   │   └── NavigationDestinations.kt
│   │   │   │   │
│   │   │   │   ├── components/
│   │   │   │   │   ├── SaisonTopAppBar.kt
│   │   │   │   │   ├── SaisonBottomBar.kt
│   │   │   │   │   ├── SaisonSearchBar.kt
│   │   │   │   │   ├── TaskCard.kt
│   │   │   │   │   ├── CourseCard.kt
│   │   │   │   │   └── PrioritySegmentedButton.kt
│   │   │   │   │
│   │   │   │   ├── screens/
│   │   │   │   │   ├── calendar/
│   │   │   │   │   │   ├── CalendarScreen.kt
│   │   │   │   │   │   ├── CalendarViewModel.kt
│   │   │   │   │   │   ├── MonthView.kt
│   │   │   │   │   │   ├── WeekView.kt
│   │   │   │   │   │   ├── DayView.kt
│   │   │   │   │   │   └── AgendaView.kt
│   │   │   │   │   │
│   │   │   │   │   ├── course/
│   │   │   │   │   │   ├── CourseScreen.kt
│   │   │   │   │   │   ├── CourseViewModel.kt
│   │   │   │   │   │   ├── CourseScheduleView.kt
│   │   │   │   │   │   └── CourseOcrImport.kt
│   │   │   │   │   │
│   │   │   │   │   ├── task/
│   │   │   │   │   │   ├── TaskListScreen.kt
│   │   │   │   │   │   ├── TaskDetailScreen.kt
│   │   │   │   │   │   ├── TaskViewModel.kt
│   │   │   │   │   │   ├── SubtaskList.kt
│   │   │   │   │   │   └── NaturalLanguageInput.kt
│   │   │   │   │   │
│   │   │   │   │   ├── pomodoro/
│   │   │   │   │   │   ├── PomodoroScreen.kt
│   │   │   │   │   │   ├── PomodoroViewModel.kt
│   │   │   │   │   │   ├── CircularTimer.kt
│   │   │   │   │   │   ├── FocusMode.kt
│   │   │   │   │   │   └── PomodoroStats.kt
│   │   │   │   │   │
│   │   │   │   │   ├── metronome/
│   │   │   │   │   │   ├── MetronomeScreen.kt
│   │   │   │   │   │   ├── MetronomeViewModel.kt
│   │   │   │   │   │   └── BeatVisualizer.kt
│   │   │   │   │   │
│   │   │   │   │   └── settings/
│   │   │   │   │       ├── SettingsScreen.kt
│   │   │   │   │       ├── SettingsViewModel.kt
│   │   │   │   │       ├── ThemeSelector.kt
│   │   │   │   │       ├── LanguageSelector.kt
│   │   │   │   │       └── WebDavSettings.kt
│   │   │   │   │
│   │   │   │   └── widgets/
│   │   │   │       ├── TodayTasksWidget.kt
│   │   │   │       ├── CourseScheduleWidget.kt
│   │   │   │       └── PomodoroWidget.kt
│   │   │   │
│   │   │   ├── data/
│   │   │   │   ├── local/
│   │   │   │   │   ├── database/
│   │   │   │   │   │   ├── SaisonDatabase.kt
│   │   │   │   │   │   ├── dao/
│   │   │   │   │   │   │   ├── TaskDao.kt
│   │   │   │   │   │   │   ├── CourseDao.kt
│   │   │   │   │   │   │   ├── TagDao.kt
│   │   │   │   │   │   │   ├── PomodoroDao.kt
│   │   │   │   │   │   │   └── AttachmentDao.kt
│   │   │   │   │   │   │
│   │   │   │   │   │   └── entities/
│   │   │   │   │   │       ├── TaskEntity.kt
│   │   │   │   │   │       ├── CourseEntity.kt
│   │   │   │   │   │       ├── TagEntity.kt
│   │   │   │   │   │       ├── PomodoroSessionEntity.kt
│   │   │   │   │   │       ├── AttachmentEntity.kt
│   │   │   │   │   │       └── relations/
│   │   │   │   │   │
│   │   │   │   │   ├── datastore/
│   │   │   │   │   │   ├── PreferencesManager.kt
│   │   │   │   │   │   └── ThemePreferences.kt
│   │   │   │   │   │
│   │   │   │   │   └── encryption/
│   │   │   │   │       ├── EncryptionManager.kt
│   │   │   │   │       └── KeystoreHelper.kt
│   │   │   │   │
│   │   │   │   ├── remote/
│   │   │   │   │   ├── webdav/
│   │   │   │   │   │   ├── WebDavClient.kt
│   │   │   │   │   │   ├── WebDavSyncService.kt
│   │   │   │   │   │   └── ConflictResolver.kt
│   │   │   │   │   │
│   │   │   │   │   └── calendar/
│   │   │   │   │       ├── LunarCalendarProvider.kt
│   │   │   │   │       └── HolidayProvider.kt
│   │   │   │   │
│   │   │   │   └── repository/
│   │   │   │       ├── TaskRepository.kt
│   │   │   │       ├── CourseRepository.kt
│   │   │   │       ├── TagRepository.kt
│   │   │   │       ├── PomodoroRepository.kt
│   │   │   │       ├── AttachmentRepository.kt
│   │   │   │       └── SyncRepository.kt
│   │   │   │
│   │   │   ├── domain/
│   │   │   │   ├── model/
│   │   │   │   │   ├── Task.kt
│   │   │   │   │   ├── Course.kt
│   │   │   │   │   ├── Tag.kt
│   │   │   │   │   ├── PomodoroSession.kt
│   │   │   │   │   ├── Attachment.kt
│   │   │   │   │   └── CalendarEvent.kt
│   │   │   │   │
│   │   │   │   └── usecase/
│   │   │   │       ├── task/
│   │   │   │       │   ├── CreateTaskUseCase.kt
│   │   │   │       │   ├── ParseNaturalLanguageUseCase.kt
│   │   │   │       │   └── RecurringTaskUseCase.kt
│   │   │   │       │
│   │   │   │       ├── pomodoro/
│   │   │   │       │   ├── StartPomodoroUseCase.kt
│   │   │   │       │   └── CalculateStatsUseCase.kt
│   │   │   │       │
│   │   │   │       └── sync/
│   │   │   │           ├── SyncTasksUseCase.kt
│   │   │   │           └── ResolveConflictUseCase.kt
│   │   │   │
│   │   │   ├── service/
│   │   │   │   ├── MetronomeService.kt
│   │   │   │   ├── PomodoroTimerService.kt
│   │   │   │   └── NotificationService.kt
│   │   │   │
│   │   │   ├── worker/
│   │   │   │   ├── SyncWorker.kt
│   │   │   │   └── BackupWorker.kt
│   │   │   │
│   │   │   └── util/
│   │   │       ├── LocaleHelper.kt
│   │   │       ├── DateTimeUtil.kt
│   │   │       ├── NaturalLanguageParser.kt
│   │   │       ├── RRuleParser.kt
│   │   │       └── HapticFeedbackHelper.kt
│   │   │
│   │   └── res/
│   │       ├── values/
│   │       │   ├── strings.xml
│   │       │   ├── colors.xml
│   │       │   └── themes.xml
│   │       ├── values-zh-rCN/
│   │       │   └── strings.xml
│   │       ├── values-ja/
│   │       │   └── strings.xml
│   │       ├── values-vi/
│   │       │   └── strings.xml
│   │       └── raw/
│   │           ├── woodblock.wav
│   │           ├── click.wav
│   │           └── beep.wav
│   │
│   └── build.gradle.kts
│
└── build.gradle.kts
```

## 组件和接口设计

### 1. UI 层组件

#### 主界面框架


**MainActivity.kt**
- 使用 `enableEdgeToEdge()` 实现 Android 15 边到边显示
- 设置 `WindowCompat.setDecorFitsSystemWindows(window, false)`
- 集成 `PredictiveBackHandler` 支持预测性返回动画

**SaisonNavHost.kt**
- 使用 `NavHost` + `rememberNavController()`
- 定义路由：`calendar`, `course`, `tasks`, `pomodoro`, `metronome`, `settings`
- 支持深度链接和参数传递

**主屏幕布局**
```kotlin
Scaffold(
    topBar = { SaisonTopAppBar() },
    bottomBar = { 
        NavigationBar { /* Calendar, Course, Tasks, More */ }
    },
    floatingActionButton = {
        ExtendedFloatingActionButton(
            onClick = { /* Quick add task */ },
            icon = { Icon(Icons.Default.Add) },
            text = { Text(stringResource(R.string.quick_add)) }
        )
    }
)
```

#### M3E 组件使用

**SearchBar**
- 全局搜索任务、课程、标签
- 支持搜索历史和建议
- 使用 `SearchBar` + `SearchBarDefaults`

**SegmentedButton**
- 日历视图切换（月/周/日/议程）
- 任务优先级选择（紧急/重要矩阵）
- 课程周期选择（A/B周）

**DatePicker**
- 任务截止日期选择
- 支持农历显示
- 使用 `DatePickerDialog` + `rememberDatePickerState()`

**ModalBottomSheet**
- 任务详情编辑
- 快速操作菜单
- 标签选择器

### 2. ViewModel 层

#### TaskViewModel
```kotlin
@HiltViewModel
class TaskViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val parseNaturalLanguageUseCase: ParseNaturalLanguageUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()
    
    private val _uiState = MutableStateFlow<TaskUiState>(TaskUiState.Loading)
    val uiState: StateFlow<TaskUiState> = _uiState.asStateFlow()
    
    fun loadTasks(filter: TaskFilter) {
        viewModelScope.launch {
            taskRepository.getTasks(filter)
                .catch { e -> _uiState.value = TaskUiState.Error(e.message) }
                .collect { _tasks.value = it }
        }
    }
    
    fun createTaskFromNaturalLanguage(input: String) {
        viewModelScope.launch {
            val parsedTask = parseNaturalLanguageUseCase(input)
            taskRepository.insertTask(parsedTask)
        }
    }
}
```

#### PomodoroViewModel
```kotlin
@HiltViewModel
class PomodoroViewModel @Inject constructor(
    private val pomodoroRepository: PomodoroRepository,
    private val startPomodoroUseCase: StartPomodoroUseCase
) : ViewModel() {
    
    private val _timerState = MutableStateFlow<PomodoroState>(PomodoroState.Idle)
    val timerState: StateFlow<PomodoroState> = _timerState.asStateFlow()
    
    private val _remainingTime = MutableStateFlow(25 * 60)
    val remainingTime: StateFlow<Int> = _remainingTime.asStateFlow()
    
    fun startPomodoro(taskId: Long?, duration: Int = 25) {
        viewModelScope.launch {
            startPomodoroUseCase(taskId, duration)
                .collect { state ->
                    _timerState.value = state
                    _remainingTime.value = state.remainingSeconds
                }
        }
    }
}
```

#### CalendarViewModel
```kotlin
@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val courseRepository: CourseRepository,
    private val lunarCalendarProvider: LunarCalendarProvider,
    private val holidayProvider: HolidayProvider
) : ViewModel() {
    
    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()
    
    private val _viewMode = MutableStateFlow(CalendarViewMode.MONTH)
    val viewMode: StateFlow<CalendarViewMode> = _viewMode.asStateFlow()
    
    val events: StateFlow<List<CalendarEvent>> = combine(
        selectedDate,
        taskRepository.getAllTasks(),
        courseRepository.getAllCourses()
    ) { date, tasks, courses ->
        mergeEventsForDate(date, tasks, courses)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    fun getLunarDate(date: LocalDate): LunarDate {
        return lunarCalendarProvider.toLunar(date)
    }
}
```

### 3. Repository 层

#### TaskRepository
```kotlin
class TaskRepository @Inject constructor(
    private val taskDao: TaskDao,
    private val syncRepository: SyncRepository,
    private val encryptionManager: EncryptionManager
) {
    fun getTasks(filter: TaskFilter): Flow<List<Task>> {
        return taskDao.getTasksFlow(filter.toQuery())
            .map { entities -> entities.map { it.toDomain() } }
    }
    
    suspend fun insertTask(task: Task): Long {
        val entity = task.toEntity()
        val id = taskDao.insert(entity)
        syncRepository.markForSync(SyncEntity.TASK, id)
        return id
    }
    
    suspend fun updateTask(task: Task) {
        val entity = task.toEntity()
        taskDao.update(entity)
        syncRepository.markForSync(SyncEntity.TASK, task.id)
    }
    
    suspend fun deleteTask(taskId: Long) {
        taskDao.delete(taskId)
        syncRepository.markForDeletion(SyncEntity.TASK, taskId)
    }
    
    fun getTasksWithSubtasks(taskId: Long): Flow<TaskWithSubtasks> {
        return taskDao.getTaskWithSubtasks(taskId)
    }
}
```

#### WebDavSyncRepository
```kotlin
class WebDavSyncRepository @Inject constructor(
    private val webDavClient: WebDavClient,
    private val taskDao: TaskDao,
    private val preferencesManager: PreferencesManager
) {
    suspend fun syncTasks(): SyncResult {
        val config = preferencesManager.getWebDavConfig() ?: return SyncResult.NotConfigured
        
        return try {
            // 1. 获取远程 ETag
            val remoteETag = webDavClient.getETag(config.tasksPath)
            val localETag = preferencesManager.getLastSyncETag()
            
            if (remoteETag == localETag) {
                return SyncResult.NoChanges
            }
            
            // 2. 下载远程数据
            val remoteTasks = webDavClient.downloadTasks(config.tasksPath)
            val localTasks = taskDao.getAllTasksList()
            
            // 3. 冲突检测和解决
            val conflicts = detectConflicts(localTasks, remoteTasks)
            val resolved = resolveConflicts(conflicts)
            
            // 4. 合并数据
            val merged = mergeTasks(localTasks, remoteTasks, resolved)
            taskDao.insertAll(merged.map { it.toEntity() })
            
            // 5. 上传本地更改
            val localChanges = taskDao.getModifiedSince(localETag)
            webDavClient.uploadTasks(config.tasksPath, localChanges)
            
            // 6. 更新 ETag
            preferencesManager.setLastSyncETag(remoteETag)
            
            SyncResult.Success(merged.size)
        } catch (e: Exception) {
            SyncResult.Error(e.message ?: "Unknown error")
        }
    }
}
```

## 数据模型设计

### Room 数据库实体

#### TaskEntity
```kotlin
@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = TagEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("categoryId"), Index("dueDate"), Index("isCompleted")]
)
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val title: String,
    val description: String?,
    val dueDate: Long?,
    val reminderTime: Long?,
    val location: String?,
    
    @ColumnInfo(name = "priority")
    val priority: Int, // 0=Low, 1=Medium, 2=High, 3=Urgent
    
    val isCompleted: Boolean = false,
    val completedAt: Long?,
    
    val categoryId: Long,
    val parentTaskId: Long?, // For subtasks
    
    val repeatRule: String?, // RRULE format
    val repeatEndDate: Long?,
    
    val pomodoroCount: Int = 0,
    val estimatedPomodoros: Int?,
    
    val metronomeBpm: Int?,
    
    val isEncrypted: Boolean = false,
    val encryptedData: ByteArray?,
    
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val syncStatus: Int = SyncStatus.PENDING
)
```

#### CourseEntity
```kotlin
@Entity(
    tableName = "courses",
    indices = [Index("dayOfWeek"), Index("startTime")]
)
data class CourseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val name: String,
    val instructor: String?,
    val location: String?,
    val color: Int,
    
    val dayOfWeek: Int, // 1=Monday, 7=Sunday
    val startTime: String, // HH:mm format
    val endTime: String,
    
    val weekPattern: String, // "ALL", "A", "B", "ODD", "EVEN"
    val startDate: Long,
    val endDate: Long,
    
    val notificationMinutes: Int = 10,
    val autoSilent: Boolean = true,
    
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
```

#### TagEntity
```kotlin
@Entity(
    tableName = "tags",
    indices = [Index("parentId"), Index("path", unique = true)]
)
data class TagEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val name: String,
    val path: String, // e.g., "study/university/math"
    val parentId: Long?,
    
    val icon: String?, // MaterialIcons name
    val color: Int,
    
    val createdAt: Long = System.currentTimeMillis()
)
```

#### PomodoroSessionEntity
```kotlin
@Entity(
    tableName = "pomodoro_sessions",
    foreignKeys = [
        ForeignKey(
            entity = TaskEntity::class,
            parentColumns = ["id"],
            childColumns = ["taskId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("taskId"), Index("startTime")]
)
data class PomodoroSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val taskId: Long?,
    val startTime: Long,
    val endTime: Long?,
    val duration: Int, // minutes
    
    val isCompleted: Boolean,
    val isBreak: Boolean = false,
    val isLongBreak: Boolean = false,
    
    val interruptions: Int = 0,
    val notes: String?
)
```

#### AttachmentEntity
```kotlin
@Entity(
    tableName = "attachments",
    foreignKeys = [
        ForeignKey(
            entity = TaskEntity::class,
            parentColumns = ["id"],
            childColumns = ["taskId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("taskId")]
)
data class AttachmentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val taskId: Long,
    val fileName: String,
    val mimeType: String,
    val fileSize: Long,
    
    val filePath: String, // Encrypted file path
    val thumbnailPath: String?,
    
    val isEncrypted: Boolean = true,
    val uploadedToWebDav: Boolean = false,
    
    val createdAt: Long = System.currentTimeMillis()
)
```

### Domain 模型

#### Task
```kotlin
data class Task(
    val id: Long = 0,
    val title: String,
    val description: String? = null,
    val dueDate: LocalDateTime? = null,
    val reminderTime: LocalDateTime? = null,
    val location: String? = null,
    val priority: Priority,
    val isCompleted: Boolean = false,
    val completedAt: LocalDateTime? = null,
    val category: Tag,
    val subtasks: List<Task> = emptyList(),
    val repeatRule: RecurrenceRule? = null,
    val pomodoroCount: Int = 0,
    val estimatedPomodoros: Int? = null,
    val metronomeBpm: Int? = null,
    val attachments: List<Attachment> = emptyList(),
    val tags: List<Tag> = emptyList(),
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

enum class Priority(val value: Int) {
    LOW(0), MEDIUM(1), HIGH(2), URGENT(3)
}
```

#### RecurrenceRule
```kotlin
data class RecurrenceRule(
    val frequency: Frequency,
    val interval: Int = 1,
    val count: Int? = null,
    val until: LocalDate? = null,
    val byDay: List<DayOfWeek>? = null,
    val byMonthDay: List<Int>? = null
) {
    fun toRRule(): String {
        // Convert to RRULE format
    }
    
    companion object {
        fun fromRRule(rrule: String): RecurrenceRule {
            // Parse RRULE string
        }
    }
}

enum class Frequency {
    DAILY, WEEKLY, MONTHLY, YEARLY
}
```

## 核心功能实现

### 1. 自然语言解析

#### NaturalLanguageParser
```kotlin
class NaturalLanguageParser @Inject constructor(
    private val context: Context
) {
    private val datePatterns = listOf(
        "tomorrow" to { LocalDate.now().plusDays(1) },
        "today" to { LocalDate.now() },
        "next (\\w+)" to { match -> parseNextDay(match.groupValues[1]) },
        "in (\\d+) days?" to { match -> LocalDate.now().plusDays(match.groupValues[1].toLong()) }
    )
    
    private val timePatterns = listOf(
        "(\\d{1,2}):(\\d{2})" to { match -> 
            LocalTime.of(match.groupValues[1].toInt(), match.groupValues[2].toInt())
        },
        "(\\d{1,2})(am|pm)" to { match -> parseAmPm(match) }
    )
    
    private val priorityKeywords = mapOf(
        "urgent" to Priority.URGENT,
        "important" to Priority.HIGH,
        "asap" to Priority.URGENT
    )
    
    fun parse(input: String): ParsedTask {
        var title = input
        var dueDate: LocalDate? = null
        var time: LocalTime? = null
        var priority = Priority.MEDIUM
        val tags = mutableListOf<String>()
        
        // Extract date
        for ((pattern, parser) in datePatterns) {
            val regex = pattern.toRegex(RegexOption.IGNORE_CASE)
            val match = regex.find(input)
            if (match != null) {
                dueDate = parser(match)
                title = title.replace(match.value, "").trim()
                break
            }
        }
        
        // Extract time
        for ((pattern, parser) in timePatterns) {
            val regex = pattern.toRegex()
            val match = regex.find(input)
            if (match != null) {
                time = parser(match)
                title = title.replace(match.value, "").trim()
                break
            }
        }
        
        // Extract priority
        for ((keyword, pri) in priorityKeywords) {
            if (input.contains(keyword, ignoreCase = true)) {
                priority = pri
                title = title.replace(keyword, "", ignoreCase = true).trim()
            }
        }
        
        // Extract tags
        val tagRegex = "#(\\w+(?:/\\w+)*)".toRegex()
        tagRegex.findAll(input).forEach { match ->
            tags.add(match.groupValues[1])
            title = title.replace(match.value, "").trim()
        }
        
        return ParsedTask(
            title = title,
            dueDate = dueDate,
            time = time,
            priority = priority,
            tags = tags
        )
    }
}
```

### 2. 番茄钟计时器

#### PomodoroTimerService
```kotlin
@AndroidEntryPoint
class PomodoroTimerService : Service() {
    
    @Inject lateinit var pomodoroRepository: PomodoroRepository
    @Inject lateinit var notificationService: NotificationService
    
    private var timerJob: Job? = null
    private val binder = PomodoroTimerBinder()
    
    private val _state = MutableStateFlow<PomodoroState>(PomodoroState.Idle)
    val state: StateFlow<PomodoroState> = _state.asStateFlow()
    
    override fun onBind(intent: Intent): IBinder = binder
    
    fun startPomodoro(taskId: Long?, durationMinutes: Int) {
        timerJob?.cancel()
        
        val session = PomodoroSession(
            taskId = taskId,
            startTime = System.currentTimeMillis(),
            duration = durationMinutes,
            isCompleted = false
        )
        
        _state.value = PomodoroState.Running(
            sessionId = session.id,
            remainingSeconds = durationMinutes * 60,
            totalSeconds = durationMinutes * 60
        )
        
        timerJob = CoroutineScope(Dispatchers.Default).launch {
            var remaining = durationMinutes * 60
            
            while (remaining > 0 && isActive) {
                delay(1000)
                remaining--
                
                _state.value = PomodoroState.Running(
                    sessionId = session.id,
                    remainingSeconds = remaining,
                    totalSeconds = durationMinutes * 60
                )
                
                updateNotification(remaining)
            }
            
            if (remaining == 0) {
                onPomodoroComplete(session)
            }
        }
        
        startForeground(NOTIFICATION_ID, createNotification())
    }
    
    private suspend fun onPomodoroComplete(session: PomodoroSession) {
        pomodoroRepository.completeSession(session.id)
        notificationService.showPomodoroComplete()
        _state.value = PomodoroState.Completed(session.id)
        
        // Trigger haptic feedback
        val vibrator = getSystemService(Vibrator::class.java)
        vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
    }
    
    inner class PomodoroTimerBinder : Binder() {
        fun getService(): PomodoroTimerService = this@PomodoroTimerService
    }
}
```

### 3. 节拍器服务

#### MetronomeService


```kotlin
@AndroidEntryPoint
class MetronomeService : Service() {
    
    @Inject lateinit var soundPool: SoundPool
    
    private var metronomeJob: Job? = null
    private val binder = MetronomeBinder()
    
    private var soundIds = mutableMapOf<MetronomeSound, Int>()
    private var currentBpm = 120
    private var currentSound = MetronomeSound.WOODBLOCK
    private var subdivision = Subdivision.QUARTER
    
    private lateinit var mediaSession: MediaSessionCompat
    
    override fun onCreate() {
        super.onCreate()
        loadSounds()
        setupMediaSession()
    }
    
    private fun loadSounds() {
        soundIds[MetronomeSound.WOODBLOCK] = soundPool.load(this, R.raw.woodblock, 1)
        soundIds[MetronomeSound.CLICK] = soundPool.load(this, R.raw.click, 1)
        soundIds[MetronomeSound.BEEP] = soundPool.load(this, R.raw.beep, 1)
    }
    
    private fun setupMediaSession() {
        mediaSession = MediaSessionCompat(this, "MetronomeService").apply {
            setCallback(object : MediaSessionCompat.Callback() {
                override fun onPlay() {
                    startMetronome()
                }
                
                override fun onPause() {
                    stopMetronome()
                }
            })
            
            setPlaybackState(
                PlaybackStateCompat.Builder()
                    .setActions(PlaybackStateCompat.ACTION_PLAY or PlaybackStateCompat.ACTION_PAUSE)
                    .setState(PlaybackStateCompat.STATE_STOPPED, 0, 1.0f)
                    .build()
            )
            
            isActive = true
        }
    }
    
    fun startMetronome(bpm: Int = currentBpm, sound: MetronomeSound = currentSound) {
        currentBpm = bpm
        currentSound = sound
        
        metronomeJob?.cancel()
        
        val intervalMs = (60_000.0 / bpm).toLong()
        
        metronomeJob = CoroutineScope(Dispatchers.Default).launch {
            var beatCount = 0
            
            while (isActive) {
                val isAccent = beatCount % 4 == 0
                playBeat(isAccent)
                
                // Send broadcast for UI update
                sendBroadcast(Intent(ACTION_BEAT).apply {
                    putExtra(EXTRA_BEAT_COUNT, beatCount)
                    putExtra(EXTRA_IS_ACCENT, isAccent)
                })
                
                beatCount++
                delay(intervalMs)
            }
        }
        
        updateMediaSession(PlaybackStateCompat.STATE_PLAYING)
        startForeground(NOTIFICATION_ID, createNotification())
    }
    
    fun stopMetronome() {
        metronomeJob?.cancel()
        updateMediaSession(PlaybackStateCompat.STATE_STOPPED)
        stopForeground(STOP_FOREGROUND_REMOVE)
    }
    
    private fun playBeat(isAccent: Boolean) {
        val soundId = soundIds[currentSound] ?: return
        val volume = if (isAccent) 1.0f else 0.7f
        soundPool.play(soundId, volume, volume, 1, 0, 1.0f)
        
        // Trigger vibration
        val vibrator = getSystemService(Vibrator::class.java)
        vibrator.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE))
    }
    
    override fun onBind(intent: Intent): IBinder = binder
    
    inner class MetronomeBinder : Binder() {
        fun getService(): MetronomeService = this@MetronomeService
    }
    
    companion object {
        const val ACTION_BEAT = "takagi.ru.saison.METRONOME_BEAT"
        const val EXTRA_BEAT_COUNT = "beat_count"
        const val EXTRA_IS_ACCENT = "is_accent"
    }
}

enum class MetronomeSound {
    WOODBLOCK, CLICK, BEEP
}

enum class Subdivision {
    QUARTER, EIGHTH, TRIPLET
}
```

### 4. WebDAV 同步客户端

#### WebDavClient
```kotlin
class WebDavClient @Inject constructor(
    private val okHttpClient: OkHttpClient
) {
    suspend fun getETag(url: String, credentials: WebDavCredentials): String? {
        return withContext(Dispatchers.IO) {
            val request = Request.Builder()
                .url(url)
                .head()
                .addHeader("Authorization", credentials.toAuthHeader())
                .build()
            
            val response = okHttpClient.newCall(request).execute()
            response.header("ETag")
        }
    }
    
    suspend fun downloadTasks(url: String, credentials: WebDavCredentials): List<Task> {
        return withContext(Dispatchers.IO) {
            val request = Request.Builder()
                .url(url)
                .get()
                .addHeader("Authorization", credentials.toAuthHeader())
                .build()
            
            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                throw WebDavException("Download failed: ${response.code}")
            }
            
            val json = response.body?.string() ?: throw WebDavException("Empty response")
            Json.decodeFromString<List<Task>>(json)
        }
    }
    
    suspend fun uploadTasks(url: String, credentials: WebDavCredentials, tasks: List<Task>) {
        withContext(Dispatchers.IO) {
            val json = Json.encodeToString(tasks)
            
            val request = Request.Builder()
                .url(url)
                .put(json.toRequestBody("application/json".toMediaType()))
                .addHeader("Authorization", credentials.toAuthHeader())
                .addHeader("If-Match", "*") // Prevent overwrite conflicts
                .build()
            
            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                throw WebDavException("Upload failed: ${response.code}")
            }
        }
    }
    
    suspend fun checkConnection(url: String, credentials: WebDavCredentials): Boolean {
        return try {
            withContext(Dispatchers.IO) {
                val request = Request.Builder()
                    .url(url)
                    .method("PROPFIND", "".toRequestBody())
                    .addHeader("Authorization", credentials.toAuthHeader())
                    .addHeader("Depth", "0")
                    .build()
                
                val response = okHttpClient.newCall(request).execute()
                response.isSuccessful
            }
        } catch (e: Exception) {
            false
        }
    }
}

data class WebDavCredentials(
    val username: String,
    val password: String
) {
    fun toAuthHeader(): String {
        val credentials = "$username:$password"
        val encoded = Base64.encodeToString(credentials.toByteArray(), Base64.NO_WRAP)
        return "Basic $encoded"
    }
}
```

### 5. 加密管理

#### EncryptionManager
```kotlin
class EncryptionManager @Inject constructor(
    private val context: Context
) {
    private val keyAlias = "saison_master_key"
    
    private val masterKey: SecretKey by lazy {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        
        if (!keyStore.containsAlias(keyAlias)) {
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                "AndroidKeyStore"
            )
            
            val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                keyAlias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
            
            keyGenerator.init(keyGenParameterSpec)
            keyGenerator.generateKey()
        }
        
        keyStore.getKey(keyAlias, null) as SecretKey
    }
    
    fun encrypt(data: String): EncryptedData {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, masterKey)
        
        val iv = cipher.iv
        val encrypted = cipher.doFinal(data.toByteArray(Charsets.UTF_8))
        
        return EncryptedData(
            ciphertext = Base64.encodeToString(encrypted, Base64.NO_WRAP),
            iv = Base64.encodeToString(iv, Base64.NO_WRAP)
        )
    }
    
    fun decrypt(encryptedData: EncryptedData): String {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val iv = Base64.decode(encryptedData.iv, Base64.NO_WRAP)
        val spec = GCMParameterSpec(128, iv)
        
        cipher.init(Cipher.DECRYPT_MODE, masterKey, spec)
        
        val ciphertext = Base64.decode(encryptedData.ciphertext, Base64.NO_WRAP)
        val decrypted = cipher.doFinal(ciphertext)
        
        return String(decrypted, Charsets.UTF_8)
    }
    
    fun encryptFile(inputFile: File, outputFile: File) {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, masterKey)
        
        // Write IV to file header
        outputFile.outputStream().use { output ->
            output.write(cipher.iv)
            
            inputFile.inputStream().use { input ->
                val buffer = ByteArray(8192)
                var bytesRead: Int
                
                while (input.read(buffer).also { bytesRead = it } != -1) {
                    val encrypted = cipher.update(buffer, 0, bytesRead)
                    if (encrypted != null) {
                        output.write(encrypted)
                    }
                }
                
                val final = cipher.doFinal()
                if (final != null) {
                    output.write(final)
                }
            }
        }
    }
}

data class EncryptedData(
    val ciphertext: String,
    val iv: String
)
```

### 6. 主题管理

#### ThemeManager
```kotlin
@Singleton
class ThemeManager @Inject constructor(
    private val preferencesManager: PreferencesManager
) {
    private val _currentTheme = MutableStateFlow(SeasonalTheme.DYNAMIC)
    val currentTheme: StateFlow<SeasonalTheme> = _currentTheme.asStateFlow()
    
    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()
    
    init {
        // Load saved theme
        CoroutineScope(Dispatchers.IO).launch {
            preferencesManager.themePreferences.collect { prefs ->
                _currentTheme.value = prefs.theme
                _isDarkMode.value = prefs.isDarkMode
            }
        }
    }
    
    suspend fun setTheme(theme: SeasonalTheme) {
        _currentTheme.value = theme
        preferencesManager.setTheme(theme)
    }
    
    suspend fun setDarkMode(enabled: Boolean) {
        _isDarkMode.value = enabled
        preferencesManager.setDarkMode(enabled)
    }
    
    fun getColorScheme(context: Context, darkTheme: Boolean): ColorScheme {
        return when (currentTheme.value) {
            SeasonalTheme.DYNAMIC -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (darkTheme) dynamicDarkColorScheme(context)
                    else dynamicLightColorScheme(context)
                } else {
                    if (darkTheme) darkColorScheme() else lightColorScheme()
                }
            }
            SeasonalTheme.SAKURA -> if (darkTheme) SakuraDarkScheme else SakuraLightScheme
            SeasonalTheme.MINT -> if (darkTheme) MintDarkScheme else MintLightScheme
            SeasonalTheme.AMBER -> if (darkTheme) AmberDarkScheme else AmberLightScheme
            SeasonalTheme.SNOW -> if (darkTheme) SnowDarkScheme else SnowLightScheme
            SeasonalTheme.RAIN -> if (darkTheme) RainDarkScheme else RainLightScheme
            SeasonalTheme.MAPLE -> if (darkTheme) MapleDarkScheme else MapleLightScheme
            // ... 其他 6 个季节主题
        }
    }
}

enum class SeasonalTheme {
    DYNAMIC, SAKURA, MINT, AMBER, SNOW, RAIN, MAPLE,
    OCEAN, SUNSET, FOREST, LAVENDER, DESERT, AURORA
}

// 示例：樱花主题
private val SakuraLightScheme = lightColorScheme(
    primary = Color(0xFFE91E63),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFCE4EC),
    onPrimaryContainer = Color(0xFF880E4F),
    secondary = Color(0xFFFF4081),
    onSecondary = Color.White,
    tertiary = Color(0xFFF8BBD0),
    background = Color(0xFFFFF0F5),
    surface = Color.White,
    surfaceVariant = Color(0xFFFCE4EC)
)

private val SakuraDarkScheme = darkColorScheme(
    primary = Color(0xFFF48FB1),
    onPrimary = Color(0xFF880E4F),
    primaryContainer = Color(0xFFC2185B),
    onPrimaryContainer = Color(0xFFFCE4EC),
    secondary = Color(0xFFFF80AB),
    onSecondary = Color(0xFF880E4F),
    tertiary = Color(0xFFC2185B),
    background = Color(0xFF1A0A0F),
    surface = Color(0xFF2D1B20),
    surfaceVariant = Color(0xFF4A2C35)
)
```

## 错误处理

### 统一错误处理策略

#### Result 封装
```kotlin
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Exception, val message: String? = null) : Result<Nothing>()
    object Loading : Result<Nothing>()
}

inline fun <T> Result<T>.onSuccess(action: (T) -> Unit): Result<T> {
    if (this is Result.Success) action(data)
    return this
}

inline fun <T> Result<T>.onError(action: (Exception) -> Unit): Result<T> {
    if (this is Result.Error) action(exception)
    return this
}
```

#### Repository 错误处理
```kotlin
suspend fun <T> safeApiCall(apiCall: suspend () -> T): Result<T> {
    return try {
        Result.Success(apiCall())
    } catch (e: IOException) {
        Result.Error(e, "Network error. Please check your connection.")
    } catch (e: HttpException) {
        Result.Error(e, "Server error: ${e.code()}")
    } catch (e: Exception) {
        Result.Error(e, "An unexpected error occurred")
    }
}
```

#### UI 错误显示
```kotlin
@Composable
fun ErrorSnackbar(
    error: String?,
    onDismiss: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    
    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Short
            )
            onDismiss()
        }
    }
    
    SnackbarHost(hostState = snackbarHostState)
}
```

## 测试策略

### 单元测试

#### Repository 测试
```kotlin
@Test
fun `insertTask should save task and mark for sync`() = runTest {
    // Given
    val task = Task(title = "Test Task", priority = Priority.HIGH)
    
    // When
    val id = taskRepository.insertTask(task)
    
    // Then
    verify(taskDao).insert(any())
    verify(syncRepository).markForSync(SyncEntity.TASK, id)
}
```

#### ViewModel 测试
```kotlin
@Test
fun `loadTasks should update tasks state`() = runTest {
    // Given
    val expectedTasks = listOf(
        Task(id = 1, title = "Task 1"),
        Task(id = 2, title = "Task 2")
    )
    whenever(taskRepository.getTasks(any())).thenReturn(flowOf(expectedTasks))
    
    // When
    viewModel.loadTasks(TaskFilter.All)
    
    // Then
    assertEquals(expectedTasks, viewModel.tasks.value)
}
```

### UI 测试

#### Compose 测试
```kotlin
@Test
fun `clicking task should navigate to detail screen`() {
    composeTestRule.setContent {
        TaskListScreen(
            tasks = listOf(Task(id = 1, title = "Test Task")),
            onTaskClick = { taskId -> 
                assertEquals(1L, taskId)
            }
        )
    }
    
    composeTestRule.onNodeWithText("Test Task").performClick()
}
```

### 集成测试

#### WebDAV 同步测试
```kotlin
@Test
fun `sync should merge local and remote tasks`() = runTest {
    // Given
    val localTasks = listOf(Task(id = 1, title = "Local"))
    val remoteTasks = listOf(Task(id = 2, title = "Remote"))
    
    // When
    val result = syncRepository.syncTasks()
    
    // Then
    assertTrue(result is SyncResult.Success)
    val allTasks = taskDao.getAllTasksList()
    assertEquals(2, allTasks.size)
}
```

## 性能优化

### 数据库优化

1. **索引策略**
   - 为常用查询字段添加索引（dueDate, isCompleted, categoryId）
   - 使用复合索引优化多条件查询

2. **分页加载**
   ```kotlin
   @Query("SELECT * FROM tasks ORDER BY dueDate ASC LIMIT :limit OFFSET :offset")
   suspend fun getTasksPaged(limit: Int, offset: Int): List<TaskEntity>
   ```

3. **懒加载关联数据**
   ```kotlin
   @Transaction
   @Query("SELECT * FROM tasks WHERE id = :taskId")
   fun getTaskWithSubtasks(taskId: Long): Flow<TaskWithSubtasks>
   ```

### UI 优化

1. **LazyColumn 优化**
   ```kotlin
   LazyColumn(
       contentPadding = PaddingValues(16.dp),
       verticalArrangement = Arrangement.spacedBy(8.dp)
   ) {
       items(
           items = tasks,
           key = { it.id }
       ) { task ->
           TaskCard(task = task)
       }
   }
   ```

2. **记忆化计算**
   ```kotlin
   val filteredTasks = remember(tasks, filter) {
       tasks.filter { it.matchesFilter(filter) }
   }
   ```

3. **避免重组**
   ```kotlin
   @Composable
   fun TaskCard(
       task: Task,
       modifier: Modifier = Modifier
   ) {
       // Use derivedStateOf for computed values
       val isOverdue by remember {
           derivedStateOf {
               task.dueDate?.isBefore(LocalDateTime.now()) == true
           }
       }
   }
   ```

### 内存优化

1. **图片加载**
   - 使用 Coil 库进行图片加载和缓存
   - 限制缩略图尺寸

2. **数据缓存**
   - 使用 LruCache 缓存频繁访问的数据
   - 及时释放不再使用的资源

## 安全考虑

### 数据保护

1. **敏感数据加密**
   - 任务内容使用 AES-256-GCM 加密
   - 密钥存储在 Android Keystore

2. **网络安全**
   - WebDAV 连接使用 HTTPS
   - 证书固定防止中间人攻击

3. **认证保护**
   - 生物识别认证
   - 6 位 PIN 码备用方案
   - 3 分钟自动锁定

### 权限管理

```kotlin
// AndroidManifest.xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.VIBRATE" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.USE_BIOMETRIC" />
<uses-permission android:name="android.permission.CAMERA" /> <!-- For OCR -->
```

## 部署和构建

### Gradle 配置

```kotlin
// build.gradle.kts (app)
android {
    namespace = "takagi.ru.saison"
    compileSdk = 35
    
    defaultConfig {
        applicationId = "takagi.ru.saison"
        minSdk = 29
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
    }
    
    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    
    buildFeatures {
        compose = true
    }
    
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3"
    }
}
```

### 混淆规则

```proguard
# Keep Room entities
-keep class takagi.ru.saison.data.local.database.entities.** { *; }

# Keep Hilt generated classes
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# Keep Compose
-keep class androidx.compose.** { *; }
```

## 国际化和本地化

### 多语言资源

```xml
<!-- values/strings.xml -->
<resources>
    <string name="app_name">Saison</string>
    <string name="calendar">Calendar</string>
    <string name="tasks">Tasks</string>
    <string name="pomodoro">Pomodoro</string>
    <string name="metronome">Metronome</string>
    <string name="settings">Settings</string>
</resources>

<!-- values-zh-rCN/strings.xml -->
<resources>
    <string name="app_name">Saison</string>
    <string name="calendar">日历</string>
    <string name="tasks">任务</string>
    <string name="pomodoro">番茄钟</string>
    <string name="metronome">节拍器</string>
    <string name="settings">设置</string>
</resources>
```

### 动态语言切换

```kotlin
class LocaleHelper {
    companion object {
        fun applyLocale(context: Context, locale: Locale) {
            Locale.setDefault(locale)
            
            val config = context.resources.configuration
            config.setLocale(locale)
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.createConfigurationContext(config)
            } else {
                @Suppress("DEPRECATION")
                context.resources.updateConfiguration(config, context.resources.displayMetrics)
            }
        }
    }
}
```

## 总结

Saison 的设计遵循现代 Android 开发最佳实践，采用 MVVM 架构、Jetpack Compose UI、Material 3 Extended 设计语言，提供完整的任务管理功能，包括日历、课程表、番茄钟、节拍器和 WebDAV 同步。应用注重性能、安全性、无障碍和多语言支持，为用户提供"用完不想换"的极致体验。
