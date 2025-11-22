# 设计文档

## 概述

本设计文档描述了番茄钟页面的Material 3风格重设计和与日程打卡功能的集成方案。该功能将提供现代化的UI体验，增加闹钟提醒功能，并允许用户将番茄钟与日程打卡任务关联，实现自动打卡和灵活的任务完成标记。

## 架构

### 组件层次结构

```
PomodoroScreen (UI层)
├── PomodoroViewModel (业务逻辑层)
│   ├── PomodoroRepository (数据层)
│   ├── RoutineRepository (数据层)
│   ├── PreferencesManager (设置管理)
│   ├── PomodoroNotificationManager (通知管理)
│   └── VibrationManager (震动管理)
└── UI组件
    ├── TimerDisplay (计时器显示)
    ├── RoutineTaskSelector (任务选择器)
    ├── TimerControls (控制按钮)
    ├── PomodoroSettingsSheet (设置面板)
    └── EarlyFinishDialog (提前结束对话框)
```

### 数据流

```
用户操作 → ViewModel → Repository → 数据库/DataStore
                ↓
            UI更新 ← StateFlow
```

## 组件和接口

### 1. UI组件

#### 1.1 PomodoroScreen

**职责：** 番茄钟主屏幕，采用Material 3设计规范

**主要元素：**
- TopAppBar：标题和设置按钮
- TimerDisplay：圆形进度指示器和剩余时间
- RoutineTaskCard：显示选中的日程任务（如果有）
- TimerControls：开始/暂停/停止/提前结束按钮
- StatisticsCard：今日统计信息

**Material 3设计规范：**
- 使用 `MaterialTheme.colorScheme` 的颜色系统
- 圆角：Card使用 `RoundedCornerShape(16.dp)`，Button使用默认圆角
- 间距：使用4dp基准（8dp, 12dp, 16dp, 24dp）
- 高度：Card elevation 2dp，按钮使用默认高度
- 动画：使用 `AnimatedVisibility` 和 `animateFloatAsState`

#### 1.2 TimerDisplay

**职责：** 显示倒计时进度和剩余时间

**组件：**
- `CircularProgressIndicator`：环形进度条
- 中心文本：显示剩余时间（MM:SS格式）
- 状态指示：不同颜色表示运行/暂停/完成状态

**颜色方案：**
- 运行中：`primary`
- 暂停：`tertiary`
- 完成：`secondary`
- 背景：`surfaceVariant`

#### 1.3 RoutineTaskSelector

**职责：** 选择要关联的日程打卡任务

**实现方式：** ModalBottomSheet

**内容：**
- 搜索框（可选）
- 任务列表（LazyColumn）
  - 每项显示：图标、标题、活动时长
  - 过滤条件：`durationMinutes != null && durationMinutes > 0`
- 空状态提示

#### 1.4 RoutineTaskCard

**职责：** 显示当前选中的日程任务信息

**显示内容：**
- 任务图标
- 任务标题
- 活动时长
- 更换任务按钮

**样式：**
- `Card` with `containerColor = primaryContainer`
- 图标大小：32dp
- 标题：`titleMedium`

#### 1.5 TimerControls

**职责：** 提供计时器控制按钮

**按钮状态：**

**空闲状态：**
- 选择任务按钮（如果未选择）
- 开始按钮（FilledButton）

**运行状态：**
- 暂停按钮（OutlinedButton）
- 提前结束按钮（TextButton）
- 停止按钮（IconButton，error色）

**暂停状态：**
- 继续按钮（FilledButton）
- 停止按钮（OutlinedButton，error色）

#### 1.6 EarlyFinishDialog

**职责：** 提前结束确认对话框

**内容：**
- 标题："提前结束"
- 说明文本：显示已用时间
- 选项：
  - "标记完成"（主要操作）
  - "仅停止"（次要操作）
  - "取消"（返回）

#### 1.7 PomodoroSettingsSheet

**职责：** 番茄钟设置面板

**设置项：**
- 工作时长（Slider：15-60分钟）
- 短休息时长（Slider：3-15分钟）
- 长休息时长（Slider：10-30分钟）
- 声音提醒开关（Switch）
- 震动提醒开关（Switch）

### 2. ViewModel

#### 2.1 PomodoroViewModel

**状态管理：**

```kotlin
data class PomodoroUiState(
    val timerState: TimerState = TimerState.Idle,
    val remainingSeconds: Int = 0,
    val totalSeconds: Int = 0,
    val selectedRoutineTask: RoutineTask? = null,
    val completedPomodoros: Int = 0,
    val todayStats: PomodoroStats = PomodoroStats(),
    val settings: PomodoroSettings = PomodoroSettings(),
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class TimerState {
    object Idle : TimerState()
    data class Running(val startTime: Long) : TimerState()
    data class Paused(val pausedAt: Long) : TimerState()
    data class Completed(val isEarlyFinish: Boolean = false) : TimerState()
}

data class PomodoroSettings(
    val workDuration: Int = 25,
    val shortBreakDuration: Int = 5,
    val longBreakDuration: Int = 15,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true
)
```

**主要方法：**

```kotlin
// 任务选择
fun selectRoutineTask(task: RoutineTask?)
fun loadAvailableRoutineTasks(): Flow<List<RoutineTask>>

// 计时器控制
fun startTimer()
fun pauseTimer()
fun resumeTimer()
fun stopTimer()
fun earlyFinish(markComplete: Boolean)

// 设置管理
fun updateSettings(settings: PomodoroSettings)

// 私有方法
private fun onTimerComplete()
private fun createCheckInRecord(isEarlyFinish: Boolean, actualDuration: Int)
private fun playNotification()
private fun triggerVibration()
```

### 3. 数据层

#### 3.1 PomodoroNotificationManager

**职责：** 管理番茄钟完成时的声音通知

**实现：**

```kotlin
@Singleton
class PomodoroNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var soundPool: SoundPool? = null
    private var completionSoundId: Int = -1
    
    fun initialize()
    fun playCompletionSound(volume: Float = 1.0f)
    fun release()
}
```

**音频资源：**
- 使用系统ToneGenerator作为后备方案
- 可选：添加自定义音频文件到 `res/raw/pomodoro_complete.mp3`

#### 3.2 VibrationManager

**职责：** 管理设备震动

**实现：**

```kotlin
@Singleton
class VibrationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val vibrator: Vibrator?
    
    fun vibrateCompletion()
    fun vibratePattern(pattern: LongArray)
}
```

**震动模式：**
- 完成震动：`longArrayOf(0, 500, 200, 500)` （震动-停止-震动）

#### 3.3 PreferencesManager扩展

**新增设置键：**

```kotlin
private object PreferencesKeys {
    // 现有键...
    val POMODORO_SOUND_ENABLED = booleanPreferencesKey("pomodoro_sound_enabled")
    val POMODORO_VIBRATION_ENABLED = booleanPreferencesKey("pomodoro_vibration_enabled")
}
```

**新增方法：**

```kotlin
val pomodoroSoundEnabled: Flow<Boolean>
suspend fun setPomodoroSoundEnabled(enabled: Boolean)

val pomodoroVibrationEnabled: Flow<Boolean>
suspend fun setPomodoroVibrationEnabled(enabled: Boolean)
```

#### 3.4 RoutineRepository接口扩展

**新增方法：**

```kotlin
// 获取设置了活动时长的任务
fun getRoutineTasksWithDuration(): Flow<List<RoutineTask>>

// 为任务创建打卡记录（带备注）
suspend fun checkInWithNote(taskId: Long, note: String): CheckInRecord
```

### 4. 数据模型

#### 4.1 PomodoroSession扩展

```kotlin
data class PomodoroSession(
    val id: Long = 0,
    val taskId: Long? = null,
    val routineTaskId: Long? = null,  // 新增：关联的日程任务ID
    val startTime: Long,
    val endTime: Long? = null,
    val duration: Int,  // 计划时长（分钟）
    val actualDuration: Int? = null,  // 实际时长（分钟）
    val isBreak: Boolean = false,
    val isLongBreak: Boolean = false,
    val isCompleted: Boolean = false,
    val isEarlyFinish: Boolean = false,  // 新增：是否提前结束
    val interruptions: Int = 0
)
```

## 错误处理

### 错误类型

1. **任务加载失败**
   - 显示错误提示
   - 提供重试按钮
   - 允许继续使用番茄钟（不关联任务）

2. **打卡失败**
   - 显示错误Snackbar
   - 提供手动重试选项
   - 记录失败的打卡尝试

3. **通知播放失败**
   - 静默失败
   - 记录日志
   - 尝试备用通知方式

4. **权限问题**
   - 震动权限：检查并请求VIBRATE权限
   - 通知权限：检查POST_NOTIFICATIONS权限（Android 13+）

### 错误恢复策略

- 使用 `try-catch` 包裹所有Repository调用
- 使用 `Flow.catch` 处理Flow错误
- 提供用户友好的错误消息
- 关键操作失败时保留用户数据

## 测试策略

### 单元测试

1. **PomodoroViewModel测试**
   - 计时器状态转换
   - 任务选择逻辑
   - 自动打卡逻辑
   - 提前结束逻辑
   - 设置更新

2. **Repository测试**
   - 任务过滤（只返回有时长的任务）
   - 打卡记录创建
   - 设置持久化

### UI测试

1. **交互测试**
   - 选择任务流程
   - 开始/暂停/停止计时器
   - 提前结束流程
   - 设置更改

2. **状态测试**
   - 不同计时器状态的UI显示
   - 任务选择前后的UI变化
   - 完成动画

### 集成测试

1. **端到端流程**
   - 选择任务 → 开始计时 → 完成 → 自动打卡
   - 选择任务 → 开始计时 → 提前结束 → 标记完成
   - 不选择任务 → 开始计时 → 完成（仅记录番茄钟）

## 性能考虑

### 优化策略

1. **计时器性能**
   - 使用 `delay(1000)` 而非更频繁的更新
   - 计时器在后台时暂停UI更新
   - 使用 `remember` 缓存计算结果

2. **任务列表加载**
   - 使用 `Flow` 实现响应式更新
   - 过滤在数据库层完成
   - 使用 `LazyColumn` 实现虚拟滚动

3. **通知和震动**
   - 延迟初始化SoundPool
   - 使用单例模式避免重复创建
   - 及时释放资源

4. **状态管理**
   - 使用 `StateFlow` 避免不必要的重组
   - 使用 `derivedStateOf` 计算派生状态
   - 最小化状态更新范围

## 国际化

### 新增字符串资源

```xml
<!-- 任务选择 -->
<string name="pomodoro_select_task">选择任务</string>
<string name="pomodoro_no_task_selected">未选择任务</string>
<string name="pomodoro_task_duration_format">%d 分钟</string>
<string name="pomodoro_no_tasks_with_duration">没有设置活动时长的任务</string>

<!-- 提前结束 -->
<string name="pomodoro_early_finish">提前结束</string>
<string name="pomodoro_early_finish_message">已用时 %d 分钟，是否标记任务完成？</string>
<string name="pomodoro_mark_complete">标记完成</string>
<string name="pomodoro_just_stop">仅停止</string>

<!-- 设置 -->
<string name="pomodoro_settings_sound">声音提醒</string>
<string name="pomodoro_settings_vibration">震动提醒</string>

<!-- 通知 -->
<string name="pomodoro_complete_title">番茄钟完成</string>
<string name="pomodoro_complete_message">专注时间结束，休息一下吧！</string>
<string name="pomodoro_checkin_success">已自动打卡</string>
<string name="pomodoro_checkin_failed">自动打卡失败</string>

<!-- 打卡备注 -->
<string name="pomodoro_checkin_note_complete">通过番茄钟完成</string>
<string name="pomodoro_checkin_note_early_finish">提前结束（实际用时 %d 分钟）</string>
```

## 可访问性

### 无障碍支持

1. **内容描述**
   - 所有图标按钮添加 `contentDescription`
   - 计时器状态使用语义化描述

2. **触摸目标**
   - 所有可点击元素最小48dp
   - 按钮间距至少8dp

3. **颜色对比**
   - 文本与背景对比度 ≥ 4.5:1
   - 重要信息不仅依赖颜色

4. **屏幕阅读器**
   - 使用 `semantics` 提供状态信息
   - 计时器更新时提供语音反馈

## 安全性

### 权限管理

```xml
<!-- AndroidManifest.xml -->
<uses-permission android:name="android.permission.VIBRATE" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
```

### 数据保护

- 打卡记录与番茄钟会话关联
- 支持现有的加密和同步机制
- 设置数据使用DataStore安全存储

## 依赖关系

### 现有依赖

- Hilt（依赖注入）
- Room（数据库）
- DataStore（设置存储）
- Compose（UI）
- Coroutines（异步）

### 新增依赖

无需新增外部依赖，使用Android SDK内置功能：
- `android.media.SoundPool`（音频）
- `android.os.Vibrator`（震动）
- `android.media.ToneGenerator`（备用音效）

## 迁移策略

### 数据库迁移

**PomodoroSession表更新：**

```kotlin
// Migration
val MIGRATION_X_Y = object : Migration(X, Y) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "ALTER TABLE pomodoro_sessions ADD COLUMN routine_task_id INTEGER"
        )
        database.execSQL(
            "ALTER TABLE pomodoro_sessions ADD COLUMN actual_duration INTEGER"
        )
        database.execSQL(
            "ALTER TABLE pomodoro_sessions ADD COLUMN is_early_finish INTEGER NOT NULL DEFAULT 0"
        )
    }
}
```

### 设置迁移

- 新设置项使用默认值（声音和震动默认启用）
- 现有番茄钟设置保持不变
- 向后兼容旧版本数据

## 实现阶段

### 阶段1：UI重设计（Material 3）
- 更新PomodoroScreen布局
- 应用Material 3颜色和样式
- 添加动画效果

### 阶段2：闹钟提醒功能
- 实现PomodoroNotificationManager
- 实现VibrationManager
- 添加设置开关
- 集成到计时器完成流程

### 阶段3：任务选择功能
- 实现RoutineTaskSelector组件
- 添加任务过滤逻辑
- 实现任务显示卡片
- 集成到ViewModel

### 阶段4：自动打卡功能
- 扩展PomodoroSession模型
- 实现自动打卡逻辑
- 添加错误处理
- 显示打卡结果

### 阶段5：提前结束功能
- 实现EarlyFinishDialog
- 添加提前结束逻辑
- 计算实际用时
- 创建带备注的打卡记录

### 阶段6：测试和优化
- 编写单元测试
- 进行UI测试
- 性能优化
- 无障碍测试

## 设计决策

### 为什么使用ModalBottomSheet选择任务？
- 符合Material 3设计规范
- 提供更好的移动端体验
- 不打断用户当前流程
- 易于实现搜索和过滤

### 为什么提供"仅停止"选项？
- 用户可能因为其他原因需要停止计时器
- 避免误操作导致错误的打卡记录
- 提供更灵活的使用方式

### 为什么自动打卡使用备注标识？
- 区分手动打卡和自动打卡
- 记录实际用时信息
- 便于后续数据分析

### 为什么声音和震动可以独立控制？
- 不同场景有不同需求（如会议中只需震动）
- 提供更精细的控制
- 符合用户期望
