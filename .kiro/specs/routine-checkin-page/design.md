# 设计文档 - 日程打卡页面

## 概述

日程打卡页面是一个专注于周期性任务管理的功能模块，采用 Material 3 设计语言。该页面的核心设计理念是通过视觉层次和打卡次数的突出显示，帮助用户快速识别当前需要完成的任务。页面将活跃任务（处于周期内）和非活跃任务（不在周期内）进行明确区分，通过颜色、透明度和位置来传达任务状态。

## 架构

### 整体架构

```
┌─────────────────────────────────────┐
│         Presentation Layer          │
│  ┌──────────────────────────────┐  │
│  │   RoutineScreen (Composable) │  │
│  │   RoutineCard (Composable)   │  │
│  │   CheckInButton (Composable) │  │
│  └──────────────────────────────┘  │
└─────────────────────────────────────┘
                 ↓
┌─────────────────────────────────────┐
│          ViewModel Layer            │
│  ┌──────────────────────────────┐  │
│  │     RoutineViewModel         │  │
│  │  - UI State Management       │  │
│  │  - Business Logic            │  │
│  └──────────────────────────────┘  │
└─────────────────────────────────────┘
                 ↓
┌─────────────────────────────────────┐
│          Domain Layer               │
│  ┌──────────────────────────────┐  │
│  │  RoutineTask (Model)         │  │
│  │  CheckInRecord (Model)       │  │
│  │  CycleCalculator (Utility)   │  │
│  └──────────────────────────────┘  │
└─────────────────────────────────────┘
                 ↓
┌─────────────────────────────────────┐
│           Data Layer                │
│  ┌──────────────────────────────┐  │
│  │   RoutineRepository          │  │
│  │   RoutineDao                 │  │
│  │   CheckInDao                 │  │
│  └──────────────────────────────┘  │
└─────────────────────────────────────┘
```

### 数据流

1. 用户交互 → RoutineScreen
2. RoutineScreen → RoutineViewModel (事件)
3. RoutineViewModel → Repository (数据请求)
4. Repository → DAO → Database
5. Database → DAO → Repository → ViewModel
6. ViewModel → UI State → RoutineScreen (更新显示)


## 组件和接口

### 1. 数据模型

#### RoutineTask
```kotlin
data class RoutineTask(
    val id: Long = 0,
    val title: String,
    val description: String? = null,
    val icon: String? = null,  // Material Icon name
    val cycleType: CycleType,
    val cycleConfig: CycleConfig,
    val isActive: Boolean = true,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

enum class CycleType {
    DAILY,      // 每日
    WEEKLY,     // 每周
    MONTHLY,    // 每月
    CUSTOM      // 自定义
}

sealed class CycleConfig {
    data class Daily(val time: LocalTime? = null) : CycleConfig()
    data class Weekly(val daysOfWeek: List<DayOfWeek>) : CycleConfig()
    data class Monthly(val daysOfMonth: List<Int>) : CycleConfig()
    data class Custom(val rrule: String) : CycleConfig()
}
```

#### CheckInRecord
```kotlin
data class CheckInRecord(
    val id: Long = 0,
    val routineTaskId: Long,
    val checkInTime: LocalDateTime,
    val note: String? = null,
    val cycleStartDate: LocalDate,  // 所属周期的开始日期
    val cycleEndDate: LocalDate     // 所属周期的结束日期
)
```

#### RoutineTaskWithStats
```kotlin
data class RoutineTaskWithStats(
    val task: RoutineTask,
    val checkInCount: Int,              // 当前周期打卡次数
    val isInActiveCycle: Boolean,       // 是否在活跃周期内
    val currentCycleStart: LocalDate?,  // 当前周期开始日期
    val currentCycleEnd: LocalDate?,    // 当前周期结束日期
    val nextActiveDate: LocalDate?,     // 下次活跃日期（非活跃任务）
    val lastCheckInTime: LocalDateTime? // 最后打卡时间
)
```

### 2. Repository 接口

```kotlin
interface RoutineRepository {
    // 任务管理
    suspend fun createRoutineTask(task: RoutineTask): Long
    suspend fun updateRoutineTask(task: RoutineTask)
    suspend fun deleteRoutineTask(taskId: Long)
    suspend fun getRoutineTask(taskId: Long): RoutineTask?
    fun getAllRoutineTasks(): Flow<List<RoutineTask>>
    
    // 打卡管理
    suspend fun checkIn(taskId: Long, note: String? = null): CheckInRecord
    suspend fun deleteCheckIn(checkInId: Long)
    fun getCheckInRecords(taskId: Long): Flow<List<CheckInRecord>>
    fun getCheckInRecordsInCycle(
        taskId: Long, 
        cycleStart: LocalDate, 
        cycleEnd: LocalDate
    ): Flow<List<CheckInRecord>>
    
    // 统计查询
    fun getRoutineTasksWithStats(): Flow<List<RoutineTaskWithStats>>
    suspend fun getCheckInCountInCycle(
        taskId: Long, 
        cycleStart: LocalDate, 
        cycleEnd: LocalDate
    ): Int
}
```

### 3. ViewModel

```kotlin
class RoutineViewModel(
    private val repository: RoutineRepository,
    private val cycleCalculator: CycleCalculator
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(RoutineUiState())
    val uiState: StateFlow<RoutineUiState> = _uiState.asStateFlow()
    
    fun loadRoutineTasks()
    fun checkInTask(taskId: Long, note: String? = null)
    fun createRoutineTask(task: RoutineTask)
    fun updateRoutineTask(task: RoutineTask)
    fun deleteRoutineTask(taskId: Long)
    fun navigateToDetail(taskId: Long)
}

data class RoutineUiState(
    val activeTasks: List<RoutineTaskWithStats> = emptyList(),
    val inactiveTasks: List<RoutineTaskWithStats> = emptyList(),
    val currentDate: LocalDate = LocalDate.now(),
    val isLoading: Boolean = false,
    val error: String? = null
)
```

### 4. UI 组件

#### RoutineScreen
主屏幕组件，显示所有周期性任务列表

```kotlin
@Composable
fun RoutineScreen(
    viewModel: RoutineViewModel,
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToCreate: () -> Unit
)
```

#### RoutineCard
任务卡片组件，显示单个周期性任务

```kotlin
@Composable
fun RoutineCard(
    taskWithStats: RoutineTaskWithStats,
    onCheckIn: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
)
```

#### CheckInButton
打卡按钮组件

```kotlin
@Composable
fun CheckInButton(
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
)
```


## UI 设计规范

### Material 3 设计元素

#### 1. 卡片设计

**活跃任务卡片：**
- 使用 `Card` 组件，elevation = 2.dp
- 圆角：16.dp
- 背景色：`MaterialTheme.colorScheme.surface`
- 边框：1.dp，`MaterialTheme.colorScheme.outline`
- 内边距：16.dp

**非活跃任务卡片：**
- 使用 `Card` 组件，elevation = 0.dp
- 圆角：16.dp
- 背景色：`MaterialTheme.colorScheme.surfaceVariant`
- 不透明度：0.6f
- 内边距：16.dp

#### 2. 打卡次数显示

- 字体：`MaterialTheme.typography.displayMedium`
- 颜色（活跃）：`MaterialTheme.colorScheme.primary`
- 颜色（非活跃）：`MaterialTheme.colorScheme.onSurfaceVariant`
- 位置：卡片右上角
- 大小：48sp

#### 3. 任务信息布局

```
┌─────────────────────────────────────┐
│  [Icon]  任务标题            [48]  │
│          周期信息            次    │
│          ─────────────────────────  │
│          [打卡按钮]                │
└─────────────────────────────────────┘
```

- 图标：24.dp，使用 Material Icons
- 标题：`MaterialTheme.typography.titleLarge`
- 周期信息：`MaterialTheme.typography.bodyMedium`
- 打卡按钮：FilledTonalButton，高度 40.dp

#### 4. 颜色使用

**活跃任务：**
- 主色：`primary`
- 表面色：`surface`
- 文字色：`onSurface`
- 次要文字：`onSurfaceVariant`

**非活跃任务：**
- 主色：`onSurfaceVariant`（灰色）
- 表面色：`surfaceVariant`
- 文字色：`onSurfaceVariant`
- 整体不透明度：60%

#### 5. 动画效果

**打卡成功动画：**
- 次数增加：数字放大动画（scale 1.0 → 1.3 → 1.0）
- 持续时间：300ms
- 缓动函数：FastOutSlowInEasing

**卡片点击反馈：**
- 涟漪效果：使用 Material Ripple
- 按压状态：轻微缩放（0.98f）

### 页面布局

#### 顶部栏
```
┌─────────────────────────────────────┐
│  [返回]  日程打卡        [添加]    │
│                                     │
│  2024年11月1日 星期五               │
└─────────────────────────────────────┘
```

- 高度：64.dp + 48.dp（日期显示）
- 背景：`MaterialTheme.colorScheme.surface`
- 标题：`MaterialTheme.typography.headlineSmall`
- 日期：`MaterialTheme.typography.titleMedium`

#### 任务列表
```
┌─────────────────────────────────────┐
│  活跃任务                           │
│  ┌───────────────────────────────┐ │
│  │  [Icon] 任务1         [5次]  │ │
│  │         每日任务              │ │
│  │         [打卡]                │ │
│  └───────────────────────────────┘ │
│                                     │
│  ┌───────────────────────────────┐ │
│  │  [Icon] 任务2         [2次]  │ │
│  │         每周任务              │ │
│  │         [打卡]                │ │
│  └───────────────────────────────┘ │
│                                     │
│  非活跃任务                         │
│  ┌───────────────────────────────┐ │
│  │  [Icon] 任务3         [0次]  │ │ (灰色)
│  │         每周任务              │ │
│  │         下次：11月3日         │ │
│  └───────────────────────────────┘ │
└─────────────────────────────────────┘
```

- 列表间距：12.dp
- 分组标题：`MaterialTheme.typography.labelLarge`
- 分组标题颜色：`onSurfaceVariant`
- 分组标题内边距：horizontal = 16.dp, vertical = 8.dp


## 周期计算逻辑

### CycleCalculator 工具类

```kotlin
class CycleCalculator {
    
    /**
     * 判断任务在指定日期是否处于活跃周期
     */
    fun isInActiveCycle(
        task: RoutineTask, 
        date: LocalDate = LocalDate.now()
    ): Boolean
    
    /**
     * 获取任务在指定日期的当前周期范围
     */
    fun getCurrentCycle(
        task: RoutineTask, 
        date: LocalDate = LocalDate.now()
    ): Pair<LocalDate, LocalDate>?
    
    /**
     * 获取任务的下一个活跃日期
     */
    fun getNextActiveDate(
        task: RoutineTask, 
        fromDate: LocalDate = LocalDate.now()
    ): LocalDate?
    
    /**
     * 获取周期描述文本
     */
    fun getCycleDescription(task: RoutineTask): String
}
```

### 周期计算规则

#### 每日任务（DAILY）
- 活跃条件：每天都是活跃的
- 周期范围：当天 00:00 到 23:59
- 次数重置：每天 00:00

#### 每周任务（WEEKLY）
- 活跃条件：当前星期几在配置的 `daysOfWeek` 列表中
- 周期范围：当周的周一 00:00 到周日 23:59
- 次数重置：每周一 00:00
- 示例：配置为 [MONDAY, WEDNESDAY, FRIDAY]，则只在这三天显示为活跃

#### 每月任务（MONTHLY）
- 活跃条件：当前日期在配置的 `daysOfMonth` 列表中
- 周期范围：当月 1 日 00:00 到月末 23:59
- 次数重置：每月 1 日 00:00
- 示例：配置为 [1, 15]，则只在每月 1 号和 15 号显示为活跃

#### 自定义任务（CUSTOM）
- 活跃条件：根据 RRULE 规则计算
- 周期范围：根据 RRULE 的 FREQ 参数确定
- 次数重置：根据周期范围确定

### 排序逻辑

任务列表排序规则：

1. 首先按活跃状态分组（活跃在前，非活跃在后）
2. 活跃任务内部排序：
   - 按打卡次数升序（次数少的在前，鼓励完成）
   - 次数相同时，按最后打卡时间升序（久未打卡的在前）
   - 都未打卡时，按创建时间降序
3. 非活跃任务内部排序：
   - 按下次活跃日期升序（即将活跃的在前）
   - 日期相同时，按创建时间降序

## 数据持久化

### Database Schema

#### routine_tasks 表
```sql
CREATE TABLE routine_tasks (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT NOT NULL,
    description TEXT,
    icon TEXT,
    cycle_type TEXT NOT NULL,
    cycle_config TEXT NOT NULL,  -- JSON 格式存储
    is_active INTEGER NOT NULL DEFAULT 1,
    created_at INTEGER NOT NULL,
    updated_at INTEGER NOT NULL
)
```

#### check_in_records 表
```sql
CREATE TABLE check_in_records (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    routine_task_id INTEGER NOT NULL,
    check_in_time INTEGER NOT NULL,
    note TEXT,
    cycle_start_date INTEGER NOT NULL,
    cycle_end_date INTEGER NOT NULL,
    FOREIGN KEY (routine_task_id) REFERENCES routine_tasks(id) ON DELETE CASCADE
)

CREATE INDEX idx_check_in_task_id ON check_in_records(routine_task_id);
CREATE INDEX idx_check_in_time ON check_in_records(check_in_time);
CREATE INDEX idx_check_in_cycle ON check_in_records(cycle_start_date, cycle_end_date);
```

### DAO 接口

```kotlin
@Dao
interface RoutineTaskDao {
    @Insert
    suspend fun insert(task: RoutineTaskEntity): Long
    
    @Update
    suspend fun update(task: RoutineTaskEntity)
    
    @Delete
    suspend fun delete(task: RoutineTaskEntity)
    
    @Query("SELECT * FROM routine_tasks WHERE id = :id")
    suspend fun getById(id: Long): RoutineTaskEntity?
    
    @Query("SELECT * FROM routine_tasks WHERE is_active = 1 ORDER BY created_at DESC")
    fun getAllActive(): Flow<List<RoutineTaskEntity>>
}

@Dao
interface CheckInRecordDao {
    @Insert
    suspend fun insert(record: CheckInRecordEntity): Long
    
    @Delete
    suspend fun delete(record: CheckInRecordEntity)
    
    @Query("SELECT * FROM check_in_records WHERE routine_task_id = :taskId ORDER BY check_in_time DESC")
    fun getByTaskId(taskId: Long): Flow<List<CheckInRecordEntity>>
    
    @Query("""
        SELECT * FROM check_in_records 
        WHERE routine_task_id = :taskId 
        AND cycle_start_date = :cycleStart 
        AND cycle_end_date = :cycleEnd
        ORDER BY check_in_time DESC
    """)
    fun getByCycle(
        taskId: Long, 
        cycleStart: Long, 
        cycleEnd: Long
    ): Flow<List<CheckInRecordEntity>>
    
    @Query("""
        SELECT COUNT(*) FROM check_in_records 
        WHERE routine_task_id = :taskId 
        AND cycle_start_date = :cycleStart 
        AND cycle_end_date = :cycleEnd
    """)
    suspend fun getCountInCycle(
        taskId: Long, 
        cycleStart: Long, 
        cycleEnd: Long
    ): Int
}
```


## 错误处理

### 错误类型

1. **数据库错误**
   - 场景：数据库操作失败
   - 处理：显示 Snackbar 提示用户，记录日志
   - 用户提示："保存失败，请重试"

2. **周期计算错误**
   - 场景：RRULE 解析失败或配置无效
   - 处理：使用默认周期（每日），记录警告日志
   - 用户提示："周期配置异常，已使用默认设置"

3. **打卡失败**
   - 场景：网络问题或数据库锁定
   - 处理：重试机制（最多 3 次），失败后提示用户
   - 用户提示："打卡失败，请稍后重试"

4. **非活跃任务打卡**
   - 场景：用户尝试对非活跃任务打卡
   - 处理：禁用打卡按钮，显示下次活跃时间
   - 用户提示："此任务当前不在活跃周期"

### 异常处理策略

```kotlin
sealed class RoutineError {
    data class DatabaseError(val message: String) : RoutineError()
    data class CycleCalculationError(val message: String) : RoutineError()
    data class CheckInError(val message: String) : RoutineError()
    data class ValidationError(val message: String) : RoutineError()
}

// ViewModel 中的错误处理
private fun handleError(error: RoutineError) {
    _uiState.update { 
        it.copy(
            error = when (error) {
                is RoutineError.DatabaseError -> "数据保存失败"
                is RoutineError.CycleCalculationError -> "周期计算错误"
                is RoutineError.CheckInError -> "打卡失败"
                is RoutineError.ValidationError -> error.message
            }
        )
    }
}
```

## 测试策略

### 单元测试

#### 1. CycleCalculator 测试
```kotlin
class CycleCalculatorTest {
    @Test
    fun `daily task is always active`()
    
    @Test
    fun `weekly task is active on configured days`()
    
    @Test
    fun `monthly task is active on configured dates`()
    
    @Test
    fun `get current cycle for daily task`()
    
    @Test
    fun `get next active date for weekly task`()
    
    @Test
    fun `cycle description formatting`()
}
```

#### 2. Repository 测试
```kotlin
class RoutineRepositoryTest {
    @Test
    fun `create routine task successfully`()
    
    @Test
    fun `check in creates record with correct cycle`()
    
    @Test
    fun `get check in count in current cycle`()
    
    @Test
    fun `delete task cascades to check in records`()
}
```

#### 3. ViewModel 测试
```kotlin
class RoutineViewModelTest {
    @Test
    fun `load tasks separates active and inactive`()
    
    @Test
    fun `check in updates task stats`()
    
    @Test
    fun `tasks are sorted correctly`()
    
    @Test
    fun `error handling displays correct message`()
}
```

### UI 测试

```kotlin
class RoutineScreenTest {
    @Test
    fun `displays active tasks at top`()
    
    @Test
    fun `displays inactive tasks with reduced opacity`()
    
    @Test
    fun `check in button disabled for inactive tasks`()
    
    @Test
    fun `check in count updates after check in`()
    
    @Test
    fun `card click navigates to detail`()
}
```

### 集成测试

```kotlin
class RoutineIntegrationTest {
    @Test
    fun `complete check in flow updates UI`()
    
    @Test
    fun `cycle transition updates task status`()
    
    @Test
    fun `multiple check ins accumulate count`()
}
```

## 性能优化

### 1. 数据库优化
- 为 `routine_task_id`、`check_in_time`、`cycle_start_date` 创建索引
- 使用批量查询减少数据库访问次数
- 实现分页加载（如果任务数量很大）

### 2. 计算缓存
- 缓存周期计算结果（当天的活跃状态）
- 使用 `remember` 缓存 Composable 中的计算结果
- 避免在 UI 层进行复杂计算

### 3. UI 优化
- 使用 `LazyColumn` 实现列表虚拟化
- 使用 `key` 参数优化列表项重组
- 动画使用 `animateContentSize` 和 `animateFloatAsState`
- 避免过度重组，使用 `derivedStateOf`

### 4. 内存优化
- 限制历史记录查询范围（如最近 3 个月）
- 及时释放不需要的资源
- 使用 Flow 进行响应式数据流

## 国际化支持

### 字符串资源

```xml
<!-- strings.xml -->
<string name="routine_screen_title">日程打卡</string>
<string name="routine_active_tasks">活跃任务</string>
<string name="routine_inactive_tasks">非活跃任务</string>
<string name="routine_check_in">打卡</string>
<string name="routine_check_in_count">%d 次</string>
<string name="routine_next_active">下次：%s</string>
<string name="routine_cycle_daily">每日</string>
<string name="routine_cycle_weekly">每周</string>
<string name="routine_cycle_monthly">每月</string>
<string name="routine_cycle_custom">自定义</string>
<string name="routine_error_check_in_failed">打卡失败，请重试</string>
<string name="routine_error_load_failed">加载失败</string>
```

### 日期格式化
- 使用 `DateTimeFormatter` 根据系统语言格式化日期
- 支持中文、英文、日文等多语言日期显示
- 星期几的本地化显示

## 可访问性

### 1. 内容描述
- 为所有图标添加 `contentDescription`
- 为打卡按钮添加状态描述（"可打卡" / "不可打卡"）
- 为次数显示添加语义描述（"已打卡 5 次"）

### 2. 触摸目标
- 所有可点击元素最小尺寸 48.dp × 48.dp
- 打卡按钮高度 40.dp，宽度至少 88.dp
- 卡片整体可点击，最小高度 72.dp

### 3. 颜色对比度
- 确保文字与背景对比度至少 4.5:1
- 非活跃任务的灰色文字对比度至少 3:1
- 使用 Material 3 的颜色系统确保对比度

### 4. 屏幕阅读器支持
- 使用 `semantics` 修饰符提供语义信息
- 为复杂组件提供合并语义
- 确保导航顺序符合逻辑

