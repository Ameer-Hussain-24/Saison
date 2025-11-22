# 设计文档

## 概述

本文档描述了 Saison 任务管理应用主题模式选择功能的设计方案。设计目标是将当前的简单深色模式开关升级为更灵活的主题模式选择器，支持"跟随系统"、"白天模式"和"夜间模式"三个选项，提供更好的用户体验和更符合现代应用的设计模式。

设计将基于现有的 MVVM 架构，最小化代码改动，确保向后兼容，并提供完整的多语言支持。

## 架构

### 整体架构

```
┌─────────────────────────────────────────────────────────────┐
│                      MainActivity                            │
│                  (Compose Entry Point)                       │
└──────────────────────┬──────────────────────────────────────┘
                       │ observes
┌──────────────────────▼──────────────────────────────────────┐
│                   ThemeViewModel                             │
│              (Theme State Provider)                          │
│  - themeMode: StateFlow<ThemeMode>                          │
│  - currentTheme: StateFlow<SeasonalTheme>                   │
└──────────────────────┬──────────────────────────────────────┘
                       │ uses
┌──────────────────────▼──────────────────────────────────────┐
│                   ThemeManager                               │
│              (Theme State Management)                        │
│  - themeMode: StateFlow<ThemeMode>                          │
│  - setThemeMode(mode: ThemeMode)                            │
└──────────────────────┬──────────────────────────────────────┘
                       │ uses
┌──────────────────────▼──────────────────────────────────────┐
│                  PreferencesManager                          │
│              (Data Persistence Layer)                        │
│  - themeMode: Flow<ThemeMode>                               │
│  - setThemeMode(mode: ThemeMode)                            │
└──────────────────────┬──────────────────────────────────────┘
                       │ persists to
┌──────────────────────▼──────────────────────────────────────┐
│                    DataStore                                 │
│              (Persistent Storage)                            │
└─────────────────────────────────────────────────────────────┘
```

### 数据流

1. **用户选择主题模式**
   - SettingsScreen → SettingsViewModel.setThemeMode()
   - SettingsViewModel → ThemeManager.setThemeMode()
   - ThemeManager → PreferencesManager.setThemeMode()
   - PreferencesManager → DataStore

2. **主题模式应用**
   - DataStore → PreferencesManager.themeMode Flow
   - PreferencesManager → ThemeManager.themeMode StateFlow
   - ThemeManager → ThemeViewModel.themeMode StateFlow
   - ThemeViewModel → MainActivity → SaisonTheme

## 组件和接口

### 1. ThemeMode 枚举

```kotlin
enum class ThemeMode(
    val displayNameRes: Int,
    val descriptionRes: Int
) {
    FOLLOW_SYSTEM(
        displayNameRes = R.string.theme_mode_follow_system,
        descriptionRes = R.string.theme_mode_follow_system_desc
    ),
    LIGHT(
        displayNameRes = R.string.theme_mode_light,
        descriptionRes = R.string.theme_mode_light_desc
    ),
    DARK(
        displayNameRes = R.string.theme_mode_dark,
        descriptionRes = R.string.theme_mode_dark_desc
    );
    
    companion object {
        fun fromString(value: String): ThemeMode {
            return values().find { it.name == value } ?: FOLLOW_SYSTEM
        }
    }
}
```

**位置:** `app/src/main/java/takagi/ru/saison/data/local/datastore/ThemeMode.kt`

**设计决策:**
- 使用枚举而非密封类，因为选项固定且简单
- 包含字符串资源 ID 以支持多语言
- 提供 fromString 方法用于从 DataStore 反序列化


### 2. ThemePreferences 数据类更新

```kotlin
data class ThemePreferences(
    val theme: SeasonalTheme = SeasonalTheme.DYNAMIC,
    val themeMode: ThemeMode = ThemeMode.FOLLOW_SYSTEM,  // 替代 isDarkMode
    val useDynamicColor: Boolean = true
)
```

**位置:** `app/src/main/java/takagi/ru/saison/data/local/datastore/ThemePreferences.kt`

**变更:**
- 移除 `isDarkMode: Boolean`
- 添加 `themeMode: ThemeMode`
- 默认值为 `FOLLOW_SYSTEM`

### 3. PreferencesManager 更新

```kotlin
@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore
    
    private object PreferencesKeys {
        val THEME = stringPreferencesKey("theme")
        val THEME_MODE = stringPreferencesKey("theme_mode")  // 新增
        // 移除 IS_DARK_MODE
        val USE_DYNAMIC_COLOR = booleanPreferencesKey("use_dynamic_color")
        // ... 其他 keys
    }
    
    // Theme Preferences
    val themePreferences: Flow<ThemePreferences> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            // 数据迁移逻辑
            val themeModeString = preferences[PreferencesKeys.THEME_MODE]
            val themeMode = if (themeModeString != null) {
                ThemeMode.fromString(themeModeString)
            } else {
                // 迁移旧数据
                val oldIsDarkMode = preferences[booleanPreferencesKey("is_dark_mode")]
                when (oldIsDarkMode) {
                    true -> ThemeMode.DARK
                    false -> ThemeMode.LIGHT
                    null -> ThemeMode.FOLLOW_SYSTEM
                }
            }
            
            ThemePreferences(
                theme = SeasonalTheme.valueOf(
                    preferences[PreferencesKeys.THEME] ?: SeasonalTheme.DYNAMIC.name
                ),
                themeMode = themeMode,
                useDynamicColor = preferences[PreferencesKeys.USE_DYNAMIC_COLOR] ?: true
            )
        }
    
    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = mode.name
            // 清理旧数据
            preferences.remove(booleanPreferencesKey("is_dark_mode"))
        }
    }
    
    // 移除 setDarkMode 函数
}
```

**设计决策:**
- 在读取时自动迁移旧的 `IS_DARK_MODE` 数据
- 保存新数据时清理旧键
- 使用字符串存储枚举值，便于序列化

### 4. ThemeManager 更新

```kotlin
@Singleton
class ThemeManager @Inject constructor(
    private val preferencesManager: PreferencesManager
) {
    private val _currentTheme = MutableStateFlow(SeasonalTheme.DYNAMIC)
    val currentTheme: StateFlow<SeasonalTheme> = _currentTheme.asStateFlow()
    
    private val _themeMode = MutableStateFlow(ThemeMode.FOLLOW_SYSTEM)  // 新增
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()
    
    // 移除 _isDarkMode 和 isDarkMode
    
    private val _useDynamicColor = MutableStateFlow(true)
    val useDynamicColor: StateFlow<Boolean> = _useDynamicColor.asStateFlow()
    
    init {
        CoroutineScope(Dispatchers.IO).launch {
            preferencesManager.themePreferences.collect { prefs ->
                _currentTheme.value = prefs.theme
                _themeMode.value = prefs.themeMode  // 更新
                _useDynamicColor.value = prefs.useDynamicColor
            }
        }
    }
    
    suspend fun setTheme(theme: SeasonalTheme) {
        _currentTheme.value = theme
        preferencesManager.setTheme(theme)
    }
    
    suspend fun setThemeMode(mode: ThemeMode) {  // 新增
        _themeMode.value = mode
        preferencesManager.setThemeMode(mode)
    }
    
    // 移除 setDarkMode 函数
    
    suspend fun setUseDynamicColor(enabled: Boolean) {
        _useDynamicColor.value = enabled
        preferencesManager.setUseDynamicColor(enabled)
    }
}
```

**位置:** `app/src/main/java/takagi/ru/saison/ui/theme/ThemeManager.kt`

### 5. ThemeViewModel 更新

```kotlin
@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val themeManager: ThemeManager
) : ViewModel() {
    
    val currentTheme: StateFlow<SeasonalTheme> = themeManager.currentTheme
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            SeasonalTheme.DYNAMIC
        )
    
    val themeMode: StateFlow<ThemeMode> = themeManager.themeMode  // 新增
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            ThemeMode.FOLLOW_SYSTEM
        )
    
    // 移除 isDarkMode
    
    val useDynamicColor: StateFlow<Boolean> = themeManager.useDynamicColor
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            true
        )
}
```

**位置:** `app/src/main/java/takagi/ru/saison/ui/theme/ThemeViewModel.kt`

### 6. MainActivity 更新

```kotlin
@Composable
fun SaisonAppWithTheme() {
    val themeViewModel = hiltViewModel<ThemeViewModel>()
    val currentTheme by themeViewModel.currentTheme.collectAsState()
    val themeMode by themeViewModel.themeMode.collectAsState()  // 新增
    val useDynamicColor by themeViewModel.useDynamicColor.collectAsState()
    
    // 根据 themeMode 计算实际的 darkTheme 值
    val systemInDarkTheme = isSystemInDarkTheme()
    val darkTheme = when (themeMode) {
        ThemeMode.FOLLOW_SYSTEM -> systemInDarkTheme
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    
    SaisonTheme(
        seasonalTheme = currentTheme,
        darkTheme = darkTheme,  // 使用计算后的值
        dynamicColor = useDynamicColor
    ) {
        SaisonApp()
    }
}
```

**位置:** `app/src/main/java/takagi/ru/saison/MainActivity.kt`

**设计决策:**
- 在 Composable 层面计算 darkTheme 值
- 使用 `isSystemInDarkTheme()` 检测系统主题
- 保持 SaisonTheme 接口不变

### 7. SettingsViewModel 更新

```kotlin
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val themeManager: ThemeManager,
    private val syncRepository: SyncRepository
) : ViewModel() {
    
    val currentTheme = themeManager.currentTheme
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SeasonalTheme.DYNAMIC)
    
    val themeMode = themeManager.themeMode  // 新增
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ThemeMode.FOLLOW_SYSTEM)
    
    // 移除 isDarkMode
    
    fun setThemeMode(mode: ThemeMode) {  // 新增
        viewModelScope.launch {
            try {
                themeManager.setThemeMode(mode)
                val message = when (mode) {
                    ThemeMode.FOLLOW_SYSTEM -> "已设置为跟随系统"
                    ThemeMode.LIGHT -> "已切换到白天模式"
                    ThemeMode.DARK -> "已切换到夜间模式"
                }
                _uiEvent.emit(SettingsUiEvent.ShowSnackbar(message))
            } catch (e: Exception) {
                handleError(e, "设置主题模式")
            }
        }
    }
    
    // 移除 setDarkMode 函数
}
```

**位置:** `app/src/main/java/takagi/ru/saison/ui/screens/settings/SettingsViewModel.kt`

## UI 组件设计

### 1. ThemeModeSelectionDialog

```kotlin
@Composable
fun ThemeModeSelectionDialog(
    currentMode: ThemeMode,
    onModeSelected: (ThemeMode) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.settings_theme_mode_title),
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ThemeMode.values().forEach { mode ->
                    ThemeModeOptionCard(
                        mode = mode,
                        isSelected = mode == currentMode,
                        onClick = {
                            onModeSelected(mode)
                            onDismiss()
                        }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_action_close))
            }
        },
        modifier = modifier
    )
}
```

**设计特点:**
- 使用 Material 3 AlertDialog
- 垂直排列三个选项
- 点击选项立即应用并关闭
- 提供关闭按钮

### 2. ThemeModeOptionCard

```kotlin
@Composable
private fun ThemeModeOptionCard(
    mode: ThemeMode,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected) 
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary) 
        else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onClick
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(mode.displayNameRes),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
                Text(
                    text = stringResource(mode.descriptionRes),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
```

**设计特点:**
- 卡片式布局
- 左侧 RadioButton
- 显示名称和描述
- 选中时高亮边框和背景色

### 3. SettingsScreen 更新

```kotlin
// 在外观设置分组中
SettingsSection(title = stringResource(R.string.settings_section_appearance)) {
    SettingsItem(
        icon = Icons.Default.Palette,
        title = stringResource(R.string.settings_theme_title),
        subtitle = getThemeName(currentTheme),
        onClick = { showThemeBottomSheet = true }
    )
    
    // 替换原来的深色模式开关
    SettingsItem(
        icon = Icons.Default.DarkMode,
        title = stringResource(R.string.settings_theme_mode_title),
        subtitle = stringResource(getThemeModeName(themeMode)),  // 新增
        onClick = { showThemeModeDialog = true }  // 新增
    )
    
    SettingsItem(
        icon = Icons.Default.ViewWeek,
        title = stringResource(R.string.settings_bottom_nav_title),
        subtitle = stringResource(R.string.settings_bottom_nav_subtitle),
        onClick = { showBottomNavBottomSheet = true }
    )
}

// 添加对话框状态
var showThemeModeDialog by remember { mutableStateOf(false) }

// 添加对话框
if (showThemeModeDialog) {
    ThemeModeSelectionDialog(
        currentMode = themeMode,
        onModeSelected = { mode ->
            viewModel.setThemeMode(mode)
        },
        onDismiss = { showThemeModeDialog = false }
    )
}
```

**变更:**
- 移除 `SettingsSwitchItem` (深色模式开关)
- 添加 `SettingsItem` (主题模式选择)
- 添加 `ThemeModeSelectionDialog`

### 4. 辅助函数

```kotlin
@Composable
fun getThemeModeName(mode: ThemeMode): Int {
    return mode.displayNameRes
}
```

**位置:** `app/src/main/java/takagi/ru/saison/ui/screens/settings/SettingsScreen.kt`

## 字符串资源

### 中文 (values-zh-rCN/strings.xml)

```xml
<!-- 主题模式 -->
<string name="settings_theme_mode_title">主题模式</string>
<string name="theme_mode_follow_system">跟随系统</string>
<string name="theme_mode_follow_system_desc">根据系统设置自动切换深色/浅色模式</string>
<string name="theme_mode_light">白天模式</string>
<string name="theme_mode_light_desc">始终使用浅色主题</string>
<string name="theme_mode_dark">夜间模式</string>
<string name="theme_mode_dark_desc">始终使用深色主题</string>
```

### 英文 (values/strings.xml)

```xml
<!-- Theme Mode -->
<string name="settings_theme_mode_title">Theme Mode</string>
<string name="theme_mode_follow_system">Follow System</string>
<string name="theme_mode_follow_system_desc">Automatically switch between light and dark themes based on system settings</string>
<string name="theme_mode_light">Light Mode</string>
<string name="theme_mode_light_desc">Always use light theme</string>
<string name="theme_mode_dark">Dark Mode</string>
<string name="theme_mode_dark_desc">Always use dark theme</string>
```

### 日文 (values-ja/strings.xml)

```xml
<!-- テーマモード -->
<string name="settings_theme_mode_title">テーマモード</string>
<string name="theme_mode_follow_system">システムに従う</string>
<string name="theme_mode_follow_system_desc">システム設定に基づいてライト/ダークテーマを自動切り替え</string>
<string name="theme_mode_light">ライトモード</string>
<string name="theme_mode_light_desc">常にライトテーマを使用</string>
<string name="theme_mode_dark">ダークモード</string>
<string name="theme_mode_dark_desc">常にダークテーマを使用</string>
```

### 越南文 (values-vi/strings.xml)

```xml
<!-- Chế độ giao diện -->
<string name="settings_theme_mode_title">Chế độ giao diện</string>
<string name="theme_mode_follow_system">Theo hệ thống</string>
<string name="theme_mode_follow_system_desc">Tự động chuyển đổi giữa giao diện sáng/tối dựa trên cài đặt hệ thống</string>
<string name="theme_mode_light">Chế độ sáng</string>
<string name="theme_mode_light_desc">Luôn sử dụng giao diện sáng</string>
<string name="theme_mode_dark">Chế độ tối</string>
<string name="theme_mode_dark_desc">Luôn sử dụng giao diện tối</string>
```

## 数据迁移策略

### 迁移流程

1. **首次读取时迁移**
   - 检查是否存在 `THEME_MODE` 键
   - 如果不存在，读取旧的 `IS_DARK_MODE` 键
   - 根据旧值映射到新的 ThemeMode
   - 返回迁移后的 ThemePreferences

2. **首次保存时清理**
   - 当用户首次修改主题模式时
   - 保存新的 `THEME_MODE` 值
   - 同时删除旧的 `IS_DARK_MODE` 键

3. **默认值处理**
   - 如果两个键都不存在（新用户）
   - 使用 `FOLLOW_SYSTEM` 作为默认值

### 迁移映射表

| 旧值 (IS_DARK_MODE) | 新值 (THEME_MODE) |
|---------------------|-------------------|
| true                | DARK              |
| false               | LIGHT             |
| null (不存在)        | FOLLOW_SYSTEM     |

## 无障碍设计

### ContentDescription

```kotlin
// 主题模式选择项
Icon(
    imageVector = Icons.Default.DarkMode,
    contentDescription = stringResource(R.string.settings_theme_mode_title)
)

// RadioButton
RadioButton(
    selected = isSelected,
    onClick = onClick,
    modifier = Modifier.semantics {
        contentDescription = stringResource(mode.displayNameRes)
        stateDescription = if (isSelected) "已选中" else "未选中"
    }
)
```

### 状态宣告

```kotlin
// 在 SettingsViewModel 中
fun setThemeMode(mode: ThemeMode) {
    viewModelScope.launch {
        try {
            themeManager.setThemeMode(mode)
            val message = when (mode) {
                ThemeMode.FOLLOW_SYSTEM -> context.getString(R.string.theme_mode_follow_system)
                ThemeMode.LIGHT -> context.getString(R.string.theme_mode_light)
                ThemeMode.DARK -> context.getString(R.string.theme_mode_dark)
            }
            // 通过 Snackbar 宣告，TalkBack 会自动读取
            _uiEvent.emit(SettingsUiEvent.ShowSnackbar("已切换到$message"))
        } catch (e: Exception) {
            handleError(e, "设置主题模式")
        }
    }
}
```

## 测试策略

### 单元测试

1. **ThemeMode 测试**
   - 测试 fromString 方法
   - 测试所有枚举值

2. **PreferencesManager 测试**
   - 测试数据迁移逻辑
   - 测试 setThemeMode 和 themeMode Flow
   - 测试旧数据清理

3. **ThemeManager 测试**
   - 测试 setThemeMode
   - 测试状态更新

4. **SettingsViewModel 测试**
   - 测试 setThemeMode
   - 测试错误处理

### UI 测试

1. **ThemeModeSelectionDialog 测试**
   - 测试选项显示
   - 测试选择交互
   - 测试对话框关闭

2. **SettingsScreen 测试**
   - 测试主题模式选择项显示
   - 测试点击打开对话框
   - 测试主题模式更新

### 集成测试

1. **端到端主题切换**
   - 测试选择不同主题模式
   - 验证主题实际应用
   - 测试数据持久化

2. **数据迁移测试**
   - 模拟旧版本数据
   - 验证迁移正确性
   - 验证旧数据清理

## 性能考虑

### 状态管理优化

1. **使用 StateFlow**
   - 避免不必要的重组
   - 更好的 Compose 集成

2. **使用 stateIn**
   - 控制订阅生命周期
   - 避免内存泄漏

3. **使用 derivedStateOf**
   - 在 MainActivity 中计算 darkTheme
   - 避免重复计算

### 数据持久化优化

1. **异步操作**
   - 所有 DataStore 操作在 IO 线程
   - 不阻塞 UI 线程

2. **批量更新**
   - 迁移时一次性完成
   - 减少写入次数

## 设计决策

### 为什么使用枚举而非密封类？

- 选项固定且简单（只有3个）
- 枚举更轻量级
- 更容易序列化和反序列化
- 提供 values() 方法方便遍历

### 为什么在 MainActivity 计算 darkTheme？

- 保持 SaisonTheme 接口不变
- 避免修改主题系统核心逻辑
- 更容易测试和维护
- 符合单一职责原则

### 为什么使用对话框而非 Bottom Sheet？

- 选项较少（只有3个）
- 对话框更简洁
- 符合 Material Design 指南
- 与其他设置项保持一致

### 为什么默认值是 FOLLOW_SYSTEM？

- 符合现代应用的最佳实践
- 尊重用户的系统级偏好
- 提供更好的用户体验
- Android 10+ 系统原生支持深色模式

## 实现优先级

### 高优先级（必须）

1. ThemeMode 枚举定义
2. PreferencesManager 更新（包括迁移逻辑）
3. ThemeManager 更新
4. ThemeViewModel 更新
5. MainActivity 更新
6. SettingsViewModel 更新
7. SettingsScreen UI 更新
8. ThemeModeSelectionDialog 实现
9. 字符串资源添加（所有语言）

### 中优先级（重要）

1. 单元测试
2. UI 测试
3. 无障碍支持完善
4. 错误处理优化

### 低优先级（可选）

1. 集成测试
2. 性能优化
3. 动画效果增强

## 风险和挑战

### 数据迁移风险

**风险:** 旧数据迁移可能失败或不完整

**缓解措施:**
- 提供默认值作为后备
- 充分测试迁移逻辑
- 保留旧数据直到确认迁移成功

### 向后兼容性

**风险:** 可能影响依赖旧 API 的代码

**缓解措施:**
- 全面搜索 `isDarkMode` 和 `setDarkMode` 的使用
- 逐步替换所有引用
- 进行回归测试

### 用户体验

**风险:** 用户可能不理解新的选项

**缓解措施:**
- 提供清晰的描述文本
- 使用直观的图标
- 保持 UI 简洁明了

## 未来扩展

1. **自定义时间段**
   - 允许用户设置自动切换时间
   - 例如：白天 6:00-18:00，夜间 18:00-6:00

2. **位置感知**
   - 根据日出日落时间自动切换
   - 使用地理位置 API

3. **主题预览**
   - 在对话框中实时预览主题效果
   - 提供更直观的选择体验

4. **快捷切换**
   - 添加快捷方式到通知栏
   - 支持桌面小部件
