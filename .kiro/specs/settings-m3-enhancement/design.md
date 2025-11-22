# 设计文档

## 概述

本文档描述了 Saison 任务管理应用设置页面的 Material 3 (M3) 设计增强方案。设计目标是创建一个功能完整、视觉现代化、用户体验优秀的设置界面，完全符合 Material 3 设计规范。

设计将基于 MVVM 架构模式，使用 Jetpack Compose 构建 UI，通过 Hilt 进行依赖注入，使用 DataStore 进行数据持久化。

## 架构

### 整体架构

```
┌─────────────────────────────────────────────────────────────┐
│                      SettingsScreen                          │
│                    (Composable UI)                           │
└──────────────────────┬──────────────────────────────────────┘
                       │ observes StateFlow
                       │ calls functions
┌──────────────────────▼──────────────────────────────────────┐
│                   SettingsViewModel                          │
│              (State Management & Logic)                      │
│  - Theme settings                                            │
│  - Language settings                                         │
│  - Notification settings                                     │
│  - Sync settings                                             │
│  - WebDAV configuration                                      │
│  - UI events                                                 │
└──────────────────────┬──────────────────────────────────────┘
                       │ uses
┌──────────────────────▼──────────────────────────────────────┐
│                  PreferencesManager                          │
│              (Data Persistence Layer)                        │
│  - DataStore operations                                      │
│  - Settings CRUD                                             │
└──────────────────────┬──────────────────────────────────────┘
                       │ persists to
┌──────────────────────▼──────────────────────────────────────┐
│                    DataStore                                 │
│              (Persistent Storage)                            │
└─────────────────────────────────────────────────────────────┘
```

### 层次结构

1. **UI Layer (SettingsScreen.kt)**
   - 负责渲染 UI 组件
   - 观察 ViewModel 的状态
   - 响应用户交互
   - 显示对话框和反馈

2. **ViewModel Layer (SettingsViewModel.kt)**
   - 管理 UI 状态
   - 处理业务逻辑
   - 协调数据层操作
   - 发出 UI 事件

3. **Data Layer (PreferencesManager.kt)**
   - 已存在，需要扩展
   - 提供设置的 CRUD 操作
   - 使用 DataStore 进行持久化

## 组件和接口

### 1. SettingsViewModel

#### 状态定义

```kotlin
// UI 状态
sealed class SettingsUiState {
    object Idle : SettingsUiState()
    object Loading : SettingsUiState()
    data class Success(val message: String) : SettingsUiState()
    data class Error(val message: String) : SettingsUiState()
}

// UI 事件
sealed class SettingsUiEvent {
    data class ShowSnackbar(val message: String) : SettingsUiEvent()
    data class ShowError(val message: String) : SettingsUiEvent()
    object NavigateToSystemSettings : SettingsUiEvent()
    object RestartRequired : SettingsUiEvent()
}

// 同步状态
data class SyncStatus(
    val lastSyncTime: Long? = null,
    val isSyncing: Boolean = false,
    val syncError: String? = null
)

// 通知设置
data class NotificationSettings(
    val enabled: Boolean = true,
    val taskReminders: Boolean = true,
    val courseReminders: Boolean = true,
    val pomodoroReminders: Boolean = true
)
```

#### ViewModel 接口

```kotlin
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val themeManager: ThemeManager,
    private val syncRepository: SyncRepository
) : ViewModel() {
    
    // 状态流
    val uiState: StateFlow<SettingsUiState>
    val uiEvent: SharedFlow<SettingsUiEvent>
    
    // 主题设置
    val currentTheme: StateFlow<SeasonalTheme>
    val isDarkMode: StateFlow<Boolean>
    val useDynamicColor: StateFlow<Boolean>
    
    // 语言设置
    val currentLanguage: StateFlow<String>
    
    // 通知设置
    val notificationSettings: StateFlow<NotificationSettings>
    
    // 同步设置
    val autoSyncEnabled: StateFlow<Boolean>
    val syncOnWifiOnly: StateFlow<Boolean>
    val syncStatus: StateFlow<SyncStatus>
    
    // WebDAV 配置
    val webDavConfigured: StateFlow<Boolean>
    
    // 番茄钟设置
    val pomodoroWorkDuration: StateFlow<Int>
    val pomodoroBreakDuration: StateFlow<Int>
    val pomodoroLongBreakDuration: StateFlow<Int>
    
    // 节拍器设置
    val metronomeDefaultBpm: StateFlow<Int>
    val metronomeSound: StateFlow<String>
    
    // 函数
    fun setTheme(theme: SeasonalTheme)
    fun setDarkMode(enabled: Boolean)
    fun setUseDynamicColor(enabled: Boolean)
    fun setLanguage(languageCode: String)
    fun setNotificationsEnabled(enabled: Boolean)
    fun setTaskRemindersEnabled(enabled: Boolean)
    fun setCourseRemindersEnabled(enabled: Boolean)
    fun setPomodoroRemindersEnabled(enabled: Boolean)
    fun setAutoSyncEnabled(enabled: Boolean)
    fun setSyncOnWifiOnly(enabled: Boolean)
    fun setWebDavConfig(url: String, username: String, password: String)
    fun testWebDavConnection(url: String, username: String, password: String)
    fun clearWebDavConfig()
    fun triggerManualSync()
    fun setPomodoroWorkDuration(minutes: Int)
    fun setPomodoroBreakDuration(minutes: Int)
    fun setPomodoroLongBreakDuration(minutes: Int)
    fun setMetronomeDefaultBpm(bpm: Int)
    fun setMetronomeSound(sound: String)
}
```

### 2. SettingsScreen

#### 主屏幕组件

```kotlin
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
)
```

**功能：**
- 使用 Scaffold 作为容器
- TopAppBar 显示标题和返回按钮
- 垂直滚动的设置列表
- 卡片式布局显示设置项
- 分组显示设置项，每组使用独立的卡片
- 响应用户交互
- 显示 Snackbar 反馈
- 页面使用 16dp 水平边距
- 卡片间使用 8dp 垂直间距
- 分组间使用 16dp 垂直间距

#### 设置分组

1. **外观设置 (Appearance)**
   - 主题选择
   - 深色模式开关
   - 动态颜色开关（Android 12+）

2. **语言设置 (Language)**
   - 应用语言选择

3. **通知设置 (Notifications)**
   - 总通知开关
   - 任务提醒开关
   - 课程提醒开关
   - 番茄钟提醒开关

4. **同步设置 (Sync)**
   - 自动同步开关
   - 仅 WiFi 同步开关
   - WebDAV 配置
   - 上次同步时间
   - 立即同步按钮

5. **番茄钟设置 (Pomodoro)**
   - 工作时长
   - 短休息时长
   - 长休息时长

6. **节拍器设置 (Metronome)**
   - 默认 BPM
   - 声音选择

7. **关于 (About)**
   - 应用信息
   - 版本号
   - 开源许可

### 3. 可复用组件

#### SettingsSection

```kotlin
@Composable
fun SettingsSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
)
```

**设计：**
- 使用 M3 Typography.labelLarge 显示标题
- 使用 onSurfaceVariant 颜色
- 标题全部大写或使用小型大写字母
- 水平内边距 16dp
- 底部边距 8dp（到卡片的距离）
- 顶部边距 16dp（分组之间的距离）

#### SettingsItem

```kotlin
@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
)
```

**设计：**
- 使用 M3 Card 组件包裹 ListItem
- 卡片使用 surfaceVariant 颜色
- 圆角 12dp
- 左侧图标
- 标题和副标题
- 右侧箭头图标
- 点击涟漪效果
- 卡片间距 8dp

#### SettingsSwitchItem

```kotlin
@Composable
fun SettingsSwitchItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
)
```

**设计：**
- 使用 M3 Card 组件包裹 ListItem
- 卡片使用 surfaceVariant 颜色
- 圆角 12dp
- 左侧图标
- 标题和副标题
- 右侧 Switch 组件
- 支持禁用状态
- 卡片间距 8dp

#### SettingsSliderItem

```kotlin
@Composable
fun SettingsSliderItem(
    icon: ImageVector,
    title: String,
    value: Int,
    valueRange: IntRange,
    onValueChange: (Int) -> Unit,
    valueLabel: (Int) -> String,
    modifier: Modifier = Modifier
)
```

**设计：**
- 使用 M3 Slider 组件
- 显示当前值
- 实时更新

### 4. 对话框组件

#### ThemeSelectionDialog

```kotlin
@Composable
fun ThemeSelectionDialog(
    currentTheme: SeasonalTheme,
    onThemeSelected: (SeasonalTheme) -> Unit,
    onDismiss: () -> Unit
)
```

**设计：**
- 使用 M3 AlertDialog
- 显示所有主题选项
- 每个主题显示颜色预览卡片
- 使用 RadioButton 标识选中项
- 支持动态颜色（Android 12+）

**主题预览卡片：**
```kotlin
@Composable
fun ThemePreviewCard(
    theme: SeasonalTheme,
    isSelected: Boolean,
    onClick: () -> Unit
)
```
- 显示主题名称
- 显示主题颜色条
- 选中时显示边框

#### LanguageSelectionDialog

```kotlin
@Composable
fun LanguageSelectionDialog(
    currentLanguage: String,
    onLanguageSelected: (String) -> Unit,
    onDismiss: () -> Unit
)
```

**设计：**
- 使用 M3 AlertDialog
- 列表显示语言选项
- 使用 RadioButton 标识选中项
- 显示语言的本地化名称

#### WebDavConfigDialog

```kotlin
@Composable
fun WebDavConfigDialog(
    initialUrl: String = "",
    initialUsername: String = "",
    onSave: (url: String, username: String, password: String) -> Unit,
    onTest: (url: String, username: String, password: String) -> Unit,
    onDismiss: () -> Unit,
    testResult: WebDavTestResult? = null
)
```

**设计：**
- 使用 M3 AlertDialog
- 三个 OutlinedTextField：服务器地址、用户名、密码
- 密码字段支持显示/隐藏
- 测试连接按钮
- 显示测试结果（成功/失败）
- 输入验证

#### PomodoroSettingsDialog

```kotlin
@Composable
fun PomodoroSettingsDialog(
    workDuration: Int,
    breakDuration: Int,
    longBreakDuration: Int,
    onSave: (work: Int, break: Int, longBreak: Int) -> Unit,
    onDismiss: () -> Unit
)
```

**设计：**
- 使用 M3 AlertDialog
- 三个数字输入字段
- 使用 Slider 或 NumberPicker
- 显示时间预览

#### MetronomeSettingsDialog

```kotlin
@Composable
fun MetronomeSettingsDialog(
    defaultBpm: Int,
    sound: String,
    onSave: (bpm: Int, sound: String) -> Unit,
    onDismiss: () -> Unit
)
```

**设计：**
- 使用 M3 AlertDialog
- BPM Slider (40-240)
- 声音选择下拉菜单
- 预览按钮

#### AboutDialog

```kotlin
@Composable
fun AboutDialog(
    onDismiss: () -> Unit,
    onOpenLicenses: () -> Unit
)
```

**设计：**
- 使用 M3 AlertDialog
- 应用图标
- 应用名称和版本
- 简短描述
- 版权信息
- 开源许可链接
- 反馈链接

## 数据模型

### PreferencesManager 扩展

需要添加以下新的偏好设置：

```kotlin
// 新增的 Keys
private object PreferencesKeys {
    // 通知设置
    val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
    val TASK_REMINDERS_ENABLED = booleanPreferencesKey("task_reminders_enabled")
    val COURSE_REMINDERS_ENABLED = booleanPreferencesKey("course_reminders_enabled")
    val POMODORO_REMINDERS_ENABLED = booleanPreferencesKey("pomodoro_reminders_enabled")
    
    // 同步设置
    val SYNC_ON_WIFI_ONLY = booleanPreferencesKey("sync_on_wifi_only")
}

// 新增的函数
suspend fun setNotificationsEnabled(enabled: Boolean)
suspend fun setTaskRemindersEnabled(enabled: Boolean)
suspend fun setCourseRemindersEnabled(enabled: Boolean)
suspend fun setPomodoroRemindersEnabled(enabled: Boolean)
suspend fun setSyncOnWifiOnly(enabled: Boolean)

val notificationsEnabled: Flow<Boolean>
val taskRemindersEnabled: Flow<Boolean>
val courseRemindersEnabled: Flow<Boolean>
val pomodoroRemindersEnabled: Flow<Boolean>
val syncOnWifiOnly: Flow<Boolean>
```

### SeasonalTheme 枚举

已存在，包含以下主题：
- DYNAMIC（动态颜色）
- SAKURA（樱花）
- MINT（薄荷）
- AMBER（琥珀）
- SNOW（雪）
- RAIN（雨）
- MAPLE（枫叶）
- OCEAN（海洋）
- SUNSET（日落）
- FOREST（森林）
- LAVENDER（薰衣草）
- DESERT（沙漠）
- AURORA（极光）

## 错误处理

### 错误类型

1. **数据加载错误**
   - 场景：从 DataStore 读取失败
   - 处理：使用默认值，显示错误 Snackbar

2. **数据保存错误**
   - 场景：写入 DataStore 失败
   - 处理：回滚状态，显示错误 Snackbar，提供重试选项

3. **WebDAV 连接错误**
   - 场景：测试连接失败
   - 处理：显示具体错误信息，允许修改配置

4. **权限错误**
   - 场景：通知权限被拒绝
   - 处理：显示引导对话框，提供跳转到系统设置的选项

### 错误处理策略

```kotlin
// ViewModel 中的错误处理
private fun handleError(error: Throwable, operation: String) {
    viewModelScope.launch {
        val message = when (error) {
            is IOException -> "网络错误，请检查连接"
            is SecurityException -> "权限不足"
            else -> "操作失败：${error.message}"
        }
        _uiEvent.emit(SettingsUiEvent.ShowError(message))
        _uiState.value = SettingsUiState.Error(message)
    }
}
```

## 测试策略

### 单元测试

1. **SettingsViewModel 测试**
   - 测试状态初始化
   - 测试设置更新
   - 测试错误处理
   - 测试 WebDAV 连接测试

2. **PreferencesManager 测试**
   - 测试数据读写
   - 测试默认值
   - 测试错误恢复

### UI 测试

1. **SettingsScreen 测试**
   - 测试设置项显示
   - 测试用户交互
   - 测试对话框显示
   - 测试状态更新

2. **对话框测试**
   - 测试输入验证
   - 测试保存操作
   - 测试取消操作

### 集成测试

1. **端到端设置流程**
   - 测试主题切换
   - 测试语言切换
   - 测试 WebDAV 配置
   - 测试同步触发

## Material 3 设计规范

### 颜色系统

使用 M3 的语义化颜色：
- `primary`: 主要操作和强调
- `secondary`: 次要操作
- `tertiary`: 第三级操作
- `surface`: 表面背景
- `surfaceVariant`: 变体表面
- `onSurface`: 表面上的内容
- `onSurfaceVariant`: 变体表面上的内容
- `outline`: 边框和分隔线

### 排版系统

使用 M3 的排版规范：
- `displayLarge/Medium/Small`: 大标题
- `headlineLarge/Medium/Small`: 标题
- `titleLarge/Medium/Small`: 小标题
- `bodyLarge/Medium/Small`: 正文
- `labelLarge/Medium/Small`: 标签

**设置页面使用：**
- 分组标题：`titleSmall`
- 设置项标题：`bodyLarge`
- 设置项副标题：`bodyMedium`
- 对话框标题：`headlineSmall`

### 间距系统

使用 M3 的间距规范：
- 4dp: 最小间距
- 8dp: 小间距
- 12dp: 中小间距
- 16dp: 标准间距
- 24dp: 大间距
- 32dp: 超大间距

**设置页面使用：**
- 设置项内边距：16dp
- 卡片间距：8dp
- 卡片圆角：12dp
- 分组间距：16dp
- 分组标题到卡片间距：8dp
- 对话框内边距：24dp
- 页面水平边距：16dp

### 组件规范

1. **ListItem**
   - 最小高度：56dp
   - 图标大小：24dp
   - 图标与文本间距：16dp

2. **Switch**
   - 触摸目标：48dp
   - 动画时长：150ms

3. **AlertDialog**
   - 最大宽度：560dp
   - 圆角：28dp
   - 内边距：24dp

4. **OutlinedTextField**
   - 最小高度：56dp
   - 圆角：4dp
   - 边框宽度：1dp

### 动画和过渡

1. **状态变化动画**
   - Switch 切换：150ms
   - 对话框淡入淡出：200ms
   - Snackbar 滑入滑出：250ms

2. **涟漪效果**
   - 使用 M3 的默认涟漪效果
   - 颜色：primary.copy(alpha = 0.12f)

3. **滚动行为**
   - 使用标准的滚动物理效果
   - 支持过度滚动

## 无障碍设计

### 内容描述

所有交互元素必须提供 `contentDescription`：

```kotlin
Icon(
    imageVector = Icons.Default.Palette,
    contentDescription = "主题设置"
)
```

### 语义化标签

使用 `semantics` 修饰符提供额外信息：

```kotlin
Switch(
    checked = enabled,
    onCheckedChange = onCheckedChange,
    modifier = Modifier.semantics {
        stateDescription = if (enabled) "已启用" else "已禁用"
    }
)
```

### 触摸目标

确保所有可点击元素的最小触摸目标为 48dp：

```kotlin
modifier = Modifier
    .size(48.dp)
    .clickable { onClick() }
```

### 键盘导航

支持 Tab 键导航和 Enter 键确认。

### 屏幕阅读器

- 使用语义化的标题层级
- 提供状态变化的宣告
- 支持 TalkBack

## 性能优化

### 状态管理优化

1. **使用 StateFlow 而非 LiveData**
   - 更好的 Compose 集成
   - 更少的重组

2. **使用 derivedStateOf**
   - 避免不必要的重组
   - 优化计算密集型状态

3. **使用 remember 和 rememberSaveable**
   - 缓存计算结果
   - 保存配置更改时的状态

### UI 优化

1. **延迟加载**
   - 对话框内容按需加载
   - 使用 LazyColumn（如果列表很长）

2. **避免过度重组**
   - 使用 `key` 参数
   - 拆分可组合函数

3. **使用 Modifier 缓存**
   - 提取常用的 Modifier 到变量

### 数据持久化优化

1. **批量更新**
   - 合并多个设置更新
   - 减少 DataStore 写入次数

2. **异步操作**
   - 所有 I/O 操作在协程中执行
   - 使用 Dispatchers.IO

## 国际化支持

### 字符串资源

所有文本必须使用字符串资源：

```xml
<!-- strings.xml -->
<string name="settings_title">设置</string>
<string name="settings_appearance">外观</string>
<string name="settings_theme">主题</string>
<string name="settings_dark_mode">深色模式</string>
<!-- ... -->
```

### 支持的语言

- 简体中文 (zh)
- 英语 (en)
- 日语 (ja)
- 越南语 (vi)

### RTL 支持

虽然当前支持的语言都是 LTR，但设计应考虑 RTL 支持：
- 使用 `start/end` 而非 `left/right`
- 图标方向自动镜像

## 安全考虑

### WebDAV 密码存储

1. **加密存储**
   - 使用 EncryptedSharedPreferences 或 EncryptionManager
   - 不在日志中输出密码

2. **密码输入**
   - 使用密码输入类型
   - 提供显示/隐藏切换

3. **连接测试**
   - 在后台线程执行
   - 超时处理
   - 错误信息不泄露敏感信息

### 权限管理

1. **通知权限**
   - Android 13+ 需要运行时权限
   - 提供权限说明
   - 处理权限拒绝

2. **网络权限**
   - 在 Manifest 中声明
   - 检查网络状态

## 实现优先级

### 高优先级（MVP）

1. ViewModel 完整实现
2. 基础 UI 组件（SettingsSection, SettingsItem, SettingsSwitchItem）
3. 主题选择功能
4. 语言选择功能
5. 基础通知设置
6. WebDAV 配置界面

### 中优先级

1. 番茄钟设置
2. 节拍器设置
3. 同步状态显示
4. 手动同步功能
5. 详细通知设置
6. 主题预览卡片

### 低优先级

1. WebDAV 连接测试
2. 高级动画效果
3. 设置搜索功能
4. 设置导入/导出
5. 开发者选项

## 技术栈

- **UI**: Jetpack Compose
- **架构**: MVVM
- **依赖注入**: Hilt
- **数据持久化**: DataStore
- **异步**: Kotlin Coroutines + Flow
- **主题**: Material 3
- **导航**: Compose Navigation
- **测试**: JUnit, Mockito, Compose Testing

## 设计决策

### 为什么使用 StateFlow 而非 LiveData？

- 更好的 Compose 集成
- 更简洁的 API
- 更好的性能
- 支持操作符组合

### 为什么使用 DataStore 而非 SharedPreferences？

- 类型安全
- 异步 API
- 更好的错误处理
- 支持 Flow

### 为什么分离 ThemeManager？

- 主题管理是全局状态
- 需要在多个地方访问
- 便于测试和维护

### 为什么使用对话框而非独立页面？

- 设置项相对简单
- 减少导航层级
- 更好的用户体验
- 符合 M3 设计规范

## 未来扩展

1. **设置同步**
   - 通过 WebDAV 同步设置
   - 多设备设置一致性

2. **设置备份**
   - 导出设置到文件
   - 从文件恢复设置

3. **高级主题定制**
   - 自定义颜色
   - 自定义字体

4. **实验性功能**
   - 开发者选项
   - Beta 功能开关

5. **设置搜索**
   - 快速查找设置项
   - 搜索历史

6. **设置推荐**
   - 基于使用习惯推荐设置
   - 新功能提示
