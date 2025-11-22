# 底部导航栏自定义功能实现

## 概述

参考Monica for Android项目，为Saison添加底部导航栏自定义功能，允许用户：
1. 显示/隐藏不需要的导航项
2. 调整导航项的顺序
3. 至少保留一个可见项

## 实现步骤

### 1. 数据模型

创建底部导航相关的数据类：

```kotlin
// 底部导航标签枚举
enum class BottomNavTab {
    CALENDAR,
    COURSE,
    TASKS,
    POMODORO,
    METRONOME,
    SETTINGS;
    
    companion object {
        val DEFAULT_ORDER = listOf(
            CALENDAR,
            COURSE,
            TASKS,
            POMODORO,
            METRONOME,
            SETTINGS
        )
    }
}

// 底部导航可见性设置
data class BottomNavVisibility(
    val calendar: Boolean = true,
    val course: Boolean = true,
    val tasks: Boolean = true,
    val pomodoro: Boolean = true,
    val metronome: Boolean = true,
    val settings: Boolean = true  // 设置项始终可见
) {
    fun isVisible(tab: BottomNavTab): Boolean = when (tab) {
        BottomNavTab.CALENDAR -> calendar
        BottomNavTab.COURSE -> course
        BottomNavTab.TASKS -> tasks
        BottomNavTab.POMODORO -> pomodoro
        BottomNavTab.METRONOME -> metronome
        BottomNavTab.SETTINGS -> settings
    }
    
    fun visibleCount(): Int = listOf(
        calendar, course, tasks, pomodoro, metronome, settings
    ).count { it }
}
```

### 2. PreferencesManager 扩展

添加底栏设置的存储和读取：

```kotlin
// 在 PreferencesKeys 中添加
val BOTTOM_NAV_CALENDAR = booleanPreferencesKey("bottom_nav_calendar")
val BOTTOM_NAV_COURSE = booleanPreferencesKey("bottom_nav_course")
val BOTTOM_NAV_TASKS = booleanPreferencesKey("bottom_nav_tasks")
val BOTTOM_NAV_POMODORO = booleanPreferencesKey("bottom_nav_pomodoro")
val BOTTOM_NAV_METRONOME = booleanPreferencesKey("bottom_nav_metronome")
val BOTTOM_NAV_SETTINGS = booleanPreferencesKey("bottom_nav_settings")
val BOTTOM_NAV_ORDER = stringPreferencesKey("bottom_nav_order")

// 添加 Flow
val bottomNavVisibility: Flow<BottomNavVisibility>
val bottomNavOrder: Flow<List<BottomNavTab>>

// 添加更新函数
suspend fun updateBottomNavVisibility(tab: BottomNavTab, visible: Boolean)
suspend fun updateBottomNavOrder(order: List<BottomNavTab>)
```

### 3. SettingsViewModel 扩展

添加底栏设置的状态和函数：

```kotlin
val bottomNavVisibility: StateFlow<BottomNavVisibility>
val bottomNavOrder: StateFlow<List<BottomNavTab>>

fun updateBottomNavVisibility(tab: BottomNavTab, visible: Boolean)
fun updateBottomNavOrder(order: List<BottomNavTab>)
```

### 4. BottomNavSettingsScreen

创建底栏设置界面：

```kotlin
@Composable
fun BottomNavSettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit
) {
    // 显示所有导航项
    // 每项包含：图标、标题、开关、上移/下移按钮
    // 至少保留一个可见项
}

@Composable
private fun BottomNavConfigRow(
    icon: ImageVector,
    title: String,
    checked: Boolean,
    switchEnabled: Boolean,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
)
```

### 5. MainActivity 更新

根据设置动态显示导航项：

```kotlin
@Composable
fun SaisonApp() {
    val navController = rememberNavController()
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val bottomNavVisibility by settingsViewModel.bottomNavVisibility.collectAsState()
    val bottomNavOrder by settingsViewModel.bottomNavOrder.collectAsState()
    
    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomNavOrder
                    .filter { bottomNavVisibility.isVisible(it) }
                    .forEach { tab ->
                        NavigationBarItem(...)
                    }
            }
        }
    ) { ... }
}
```

### 6. SettingsScreen 添加入口

在设置页面添加"底部导航栏设置"选项：

```kotlin
SettingsSection(title = "界面") {
    SettingsItem(
        icon = Icons.Default.ViewWeek,
        title = "底部导航栏",
        subtitle = "自定义显示的导航项",
        onClick = { /* 导航到 BottomNavSettingsScreen */ }
    )
}
```

### 7. 字符串资源

添加相关字符串：

```xml
<!-- values/strings.xml -->
<string name="bottom_nav_settings">底部导航栏</string>
<string name="bottom_nav_settings_subtitle">自定义显示的导航项</string>
<string name="bottom_nav_hint">至少保留一个可见项</string>
<string name="bottom_nav_move_up">上移</string>
<string name="bottom_nav_move_down">下移</string>
```

## 功能特点

1. **灵活性**: 用户可以隐藏不需要的功能
2. **可排序**: 用户可以调整导航项顺序
3. **安全性**: 至少保留一个可见项，防止用户隐藏所有项
4. **持久化**: 设置保存在 DataStore 中
5. **实时更新**: 修改后立即生效

## 用户体验

- 简洁的开关控制显示/隐藏
- 直观的上移/下移按钮调整顺序
- 当只剩一个可见项时，自动禁用其开关
- 提示文字说明功能

## 技术细节

- 使用 DataStore 存储设置
- 使用 StateFlow 响应式更新 UI
- 使用 Hilt 依赖注入
- 遵循 Material Design 3 规范
