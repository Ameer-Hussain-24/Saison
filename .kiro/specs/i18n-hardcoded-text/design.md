# 设计文档

## 概述

本设计文档描述了如何系统化地检测、提取和替换 Saison 应用中的硬编码文本，以实现完整的多语言国际化支持。当前应用已建立基础的 i18n 框架，包括：

- 字符串资源文件（values/strings.xml 及各语言变体）
- LocaleHelper 用于语言切换
- 支持英语、中文、日语、越南语

然而，代码审查发现多个 UI 组件中仍存在硬编码的中文文本，这些文本需要被提取到字符串资源中。

## 架构

### 当前 i18n 架构

```
app/src/main/res/
├── values/
│   └── strings.xml (默认英语)
├── values-zh-rCN/
│   └── strings.xml (简体中文)
├── values-ja/
│   └── strings.xml (日语)
└── values-vi/
    └── strings.xml (越南语)

app/src/main/java/takagi/ru/saison/
└── util/
    └── LocaleHelper.kt (语言切换工具)
```

### 硬编码文本分类

根据代码审查，硬编码文本主要分布在以下模块：

1. **任务管理模块** (TaskListScreen.kt, TaskDetailScreen.kt)
   - UI 标签：快速添加、搜索任务、全部、进行中、已完成
   - 空状态消息：还没有任务、点击右下角按钮添加新任务
   - 统计标签：待完成、已逾期
   - 对话框：快速添加任务、任务描述、添加、取消

2. **设置模块** (SettingsScreen.kt)
   - 分组标题：外观、语言、通知、同步、番茄钟、节拍器、关于
   - 设置项标签：主题、深色模式、底部导航栏、应用语言等
   - 设置项描述：使用深色主题、自定义显示的导航项等
   - 同步状态：刚刚、X分钟前、X小时前、X天前、尚未同步、错误

3. **节拍器模块** (MetronomeScreen.kt)
   - UI 标签：节拍器、预设、保存预设、设置
   - 控制按钮：减少、增加

4. **番茄钟模块** (PomodoroScreen.kt)
   - 可能存在的硬编码文本（需进一步检查）

5. **日历模块** (CalendarScreen.kt)
   - 可能存在的硬编码文本（需进一步检查）

6. **课程模块** (CourseScreen.kt)
   - 可能存在的硬编码文本（需进一步检查）

## 组件和接口

### 1. 硬编码文本检测工具

虽然本项目主要通过手动代码审查来识别硬编码文本，但可以使用以下方法辅助检测：

**检测策略：**
- 搜索 `Text("中文字符")` 模式
- 搜索 `text = "中文字符"` 模式
- 搜索 `title = "中文字符"` 模式
- 搜索 `subtitle = "中文字符"` 模式
- 搜索 `label = "中文字符"` 模式

**排除项：**
- 日志标签（Log.d, Log.e 等）
- 数据库键名
- SharedPreferences 键名
- 测试数据
- 注释

### 2. 字符串资源命名规范

采用统一的命名规范以提高可维护性：

```
<功能模块>_<类型>_<具体描述>

示例：
- task_action_quick_add (任务_操作_快速添加)
- task_filter_all (任务_筛选_全部)
- task_filter_active (任务_筛选_进行中)
- task_filter_completed (任务_筛选_已完成)
- task_empty_no_tasks (任务_空状态_没有任务)
- task_empty_hint (任务_空状态_提示)
- task_stats_incomplete (任务_统计_待完成)
- task_stats_overdue (任务_统计_已逾期)
- task_search_placeholder (任务_搜索_占位符)
- task_search_no_results (任务_搜索_无结果)
- task_dialog_quick_add_title (任务_对话框_快速添加_标题)
- task_dialog_description_label (任务_对话框_描述_标签)

- settings_section_appearance (设置_分组_外观)
- settings_section_language (设置_分组_语言)
- settings_section_notifications (设置_分组_通知)
- settings_section_sync (设置_分组_同步)
- settings_theme_title (设置_主题_标题)
- settings_dark_mode_title (设置_深色模式_标题)
- settings_dark_mode_subtitle (设置_深色模式_副标题)
- settings_sync_now (设置_同步_立即同步)
- settings_sync_last_time (设置_同步_上次时间)
- settings_sync_not_yet (设置_同步_尚未同步)
- settings_sync_error (设置_同步_错误)

- metronome_title (节拍器_标题)
- metronome_presets (节拍器_预设)
- metronome_save_preset (节拍器_保存预设)
- metronome_decrease (节拍器_减少)
- metronome_increase (节拍器_增加)

- common_action_add (通用_操作_添加)
- common_action_cancel (通用_操作_取消)
- common_action_save (通用_操作_保存)
- common_action_delete (通用_操作_删除)
- common_action_edit (通用_操作_编辑)
- common_action_search (通用_操作_搜索)
- common_action_filter (通用_操作_筛选)
- common_action_sync (通用_操作_同步)

- time_just_now (时间_刚刚)
- time_minutes_ago (时间_分钟前)
- time_hours_ago (时间_小时前)
- time_days_ago (时间_天前)
```

### 3. 字符串资源文件结构

在 strings.xml 中按功能模块组织，使用 XML 注释分组：

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <!-- App Name -->
    <string name="app_name">Saison</string>
    
    <!-- Navigation -->
    <string name="nav_calendar">Calendar</string>
    ...
    
    <!-- Common Actions -->
    <string name="common_action_add">Add</string>
    ...
    
    <!-- Common Time -->
    <string name="time_just_now">Just now</string>
    <string name="time_minutes_ago">%d minutes ago</string>
    <string name="time_hours_ago">%d hours ago</string>
    <string name="time_days_ago">%d days ago</string>
    
    <!-- Task Module -->
    <string name="task_action_quick_add">Quick Add</string>
    <string name="task_filter_all">All</string>
    <string name="task_filter_active">Active</string>
    <string name="task_filter_completed">Completed</string>
    <string name="task_stats_incomplete">Incomplete</string>
    <string name="task_stats_overdue">Overdue</string>
    <string name="task_search_placeholder">Search tasks...</string>
    <string name="task_search_no_results">No tasks found</string>
    <string name="task_empty_no_tasks">No tasks yet</string>
    <string name="task_empty_no_active">No active tasks</string>
    <string name="task_empty_no_completed">No completed tasks yet</string>
    <string name="task_empty_hint">Tap the button below to add a new task</string>
    <string name="task_dialog_quick_add_title">Quick Add Task</string>
    <string name="task_dialog_description_label">Task description</string>
    <string name="task_dialog_example_hint">Use natural language, e.g.:</string>
    <string name="task_dialog_example_text">\"Meeting tomorrow at 3pm #work\"</string>
    
    <!-- Settings Module -->
    <string name="settings_title">Settings</string>
    <string name="settings_section_appearance">Appearance</string>
    <string name="settings_section_language">Language</string>
    <string name="settings_section_notifications">Notifications</string>
    <string name="settings_section_sync">Sync</string>
    <string name="settings_section_pomodoro">Pomodoro</string>
    <string name="settings_section_metronome">Metronome</string>
    <string name="settings_section_about">About</string>
    
    <string name="settings_theme_title">Theme</string>
    <string name="settings_dark_mode_title">Dark Mode</string>
    <string name="settings_dark_mode_subtitle">Use dark theme</string>
    <string name="settings_bottom_nav_title">Bottom Navigation</string>
    <string name="settings_bottom_nav_subtitle">Customize navigation items</string>
    <string name="settings_language_title">App Language</string>
    
    <string name="settings_notifications_enable_title">Enable Notifications</string>
    <string name="settings_notifications_enable_subtitle">Receive task and course reminders</string>
    <string name="settings_task_reminders_title">Task Reminders</string>
    <string name="settings_task_reminders_subtitle">Receive task due date reminders</string>
    <string name="settings_course_reminders_title">Course Reminders</string>
    <string name="settings_course_reminders_subtitle">Receive course start reminders</string>
    <string name="settings_pomodoro_reminders_title">Pomodoro Reminders</string>
    <string name="settings_pomodoro_reminders_subtitle">Receive pomodoro completion reminders</string>
    
    <string name="settings_auto_sync_title">Auto Sync</string>
    <string name="settings_auto_sync_subtitle">Automatically sync to WebDAV</string>
    <string name="settings_wifi_only_title">WiFi Only Sync</string>
    <string name="settings_wifi_only_subtitle">Sync only on WiFi network</string>
    <string name="settings_webdav_title">WebDAV Configuration</string>
    <string name="settings_webdav_subtitle">Configure WebDAV server</string>
    <string name="settings_sync_now_title">Sync Now</string>
    <string name="settings_sync_last_time">Last sync: %s</string>
    <string name="settings_sync_not_yet">Not synced yet</string>
    <string name="settings_sync_error">Error: %s</string>
    <string name="settings_sync_button">Sync</string>
    
    <string name="settings_pomodoro_duration_title">Pomodoro Duration</string>
    <string name="settings_pomodoro_duration_subtitle">Work: %1$dmin | Short break: %2$dmin | Long break: %3$dmin</string>
    
    <string name="settings_metronome_title">Metronome Settings</string>
    <string name="settings_metronome_subtitle">Default BPM: %d</string>
    
    <string name="settings_about_title">About Saison</string>
    <string name="settings_about_subtitle">Version 1.0.0</string>
    
    <!-- Metronome Module -->
    <string name="metronome_title">Metronome</string>
    <string name="metronome_presets">Presets</string>
    <string name="metronome_save_preset">Save Preset</string>
    <string name="metronome_settings">Settings</string>
    <string name="metronome_decrease_1">Decrease 1</string>
    <string name="metronome_decrease_5">Decrease 5</string>
    <string name="metronome_increase_1">Increase 1</string>
    <string name="metronome_increase_5">Increase 5</string>
    
    <!-- Content Descriptions -->
    <string name="cd_back">Back</string>
    <string name="cd_search">Search</string>
    <string name="cd_close_search">Close search</string>
    <string name="cd_filter">Filter</string>
    <string name="cd_calendar">Calendar</string>
    <string name="cd_sync_now">Sync now</string>
</resources>
```

### 4. 代码中使用字符串资源

#### 在 Composable 函数中

```kotlin
import androidx.compose.ui.res.stringResource
import takagi.ru.saison.R

@Composable
fun MyScreen() {
    Text(text = stringResource(R.string.task_action_quick_add))
    
    // 带参数的字符串
    Text(text = stringResource(R.string.time_minutes_ago, 5))
    
    // 在 Icon 的 contentDescription 中
    Icon(
        imageVector = Icons.Default.Search,
        contentDescription = stringResource(R.string.cd_search)
    )
}
```

#### 在 ViewModel 或其他非 Composable 类中

```kotlin
class MyViewModel @Inject constructor(
    private val context: Context
) : ViewModel() {
    
    fun showMessage() {
        val message = context.getString(R.string.task_action_quick_add)
        // 使用 message
    }
    
    // 带参数的字符串
    fun showTimeAgo(minutes: Int) {
        val message = context.getString(R.string.time_minutes_ago, minutes)
        // 使用 message
    }
}
```

### 5. 翻译策略

#### 翻译原则

1. **准确性**：确保翻译准确传达原文含义
2. **本地化**：使用目标语言的自然表达方式
3. **一致性**：相同概念在不同位置使用相同翻译
4. **简洁性**：UI 文本应简洁明了，避免冗长
5. **文化适应**：考虑文化差异，避免不当表达

#### 特殊处理

- **品牌名称**：Saison 在所有语言中保持不变
- **技术术语**：WebDAV、BPM 等保持英文
- **数字和单位**：使用目标语言的习惯表达
- **时间表达**：适应不同语言的时间表达习惯

#### 翻译示例

| 英语 | 中文 | 日语 | 越南语 |
|------|------|------|--------|
| Quick Add | 快速添加 | クイック追加 | Thêm nhanh |
| All | 全部 | すべて | Tất cả |
| Active | 进行中 | 進行中 | Đang thực hiện |
| Completed | 已完成 | 完了 | Đã hoàn thành |
| No tasks yet | 还没有任务 | まだタスクがありません | Chưa có việc nào |
| Just now | 刚刚 | たった今 | Vừa xong |
| %d minutes ago | %d 分钟前 | %d 分前 | %d phút trước |

## 数据模型

### 字符串资源条目

```kotlin
data class StringResourceEntry(
    val key: String,              // 资源 ID，如 "task_action_quick_add"
    val defaultValue: String,     // 默认值（英语）
    val translations: Map<String, String>, // 各语言翻译
    val category: String,         // 分类，如 "Task Module"
    val hasParameters: Boolean,   // 是否包含参数
    val parameterCount: Int = 0   // 参数数量
)
```

### 硬编码文本条目

```kotlin
data class HardcodedTextEntry(
    val filePath: String,         // 文件路径
    val lineNumber: Int,          // 行号
    val text: String,             // 硬编码文本
    val context: String,          // 上下文代码
    val suggestedKey: String,     // 建议的资源 ID
    val needsTranslation: Boolean // 是否需要翻译
)
```

## 错误处理

### 缺失翻译

**问题**：某个字符串资源在某些语言文件中缺失

**处理策略**：
1. 编译时：Android 会自动回退到默认语言（英语）
2. 运行时：应用正常运行，但显示英语文本
3. 开发时：使用 Lint 检查工具检测缺失的翻译

**预防措施**：
- 在添加新字符串时，同时在所有语言文件中添加
- 使用脚本验证所有语言文件的字符串资源键是否一致
- 在 CI/CD 流程中添加翻译完整性检查

### 参数不匹配

**问题**：字符串资源中的参数数量或类型不匹配

**处理策略**：
1. 编译时：如果参数类型不匹配，编译器会报错
2. 运行时：如果参数数量不匹配，可能导致 FormatException

**预防措施**：
- 在所有语言文件中使用相同的参数占位符
- 使用类型安全的参数（%1$s, %2$d 等）
- 添加单元测试验证参数化字符串

### 文本过长导致 UI 问题

**问题**：某些语言的翻译文本过长，导致 UI 布局问题

**处理策略**：
1. 使用 Compose 的自适应布局（如 `maxLines`, `overflow`）
2. 为长文本提供缩写版本
3. 调整 UI 设计以适应不同长度的文本

**预防措施**：
- 在设计 UI 时考虑文本长度变化
- 测试所有支持的语言
- 使用 `Text` 组件的 `softWrap` 和 `overflow` 属性

## 测试策略

### 1. 单元测试

**目标**：验证字符串资源的正确性

```kotlin
@Test
fun `verify all string resources exist in all languages`() {
    val languages = listOf("", "zh-rCN", "ja", "vi")
    val defaultStrings = getStringResourceKeys("values")
    
    languages.forEach { lang ->
        val langStrings = getStringResourceKeys("values-$lang")
        assertEquals(defaultStrings, langStrings, "Missing translations in $lang")
    }
}

@Test
fun `verify parameterized strings have correct format`() {
    val parameterizedStrings = listOf(
        R.string.time_minutes_ago,
        R.string.time_hours_ago,
        R.string.time_days_ago
    )
    
    parameterizedStrings.forEach { stringRes ->
        val string = context.getString(stringRes, 1)
        assertNotNull(string)
        assertFalse(string.contains("%"))
    }
}
```

### 2. UI 测试

**目标**：验证 UI 在不同语言下的显示效果

```kotlin
@Test
fun `test task list screen in all languages`() {
    val languages = listOf("en", "zh", "ja", "vi")
    
    languages.forEach { lang ->
        setAppLanguage(lang)
        composeTestRule.setContent {
            TaskListScreen(...)
        }
        
        // 验证关键文本显示
        composeTestRule.onNodeWithText(
            context.getString(R.string.task_action_quick_add)
        ).assertExists()
    }
}
```

### 3. 手动测试

**测试清单**：
- [ ] 切换到每种支持的语言
- [ ] 验证所有屏幕的文本显示正确
- [ ] 检查文本是否有截断或溢出
- [ ] 验证对话框和提示消息
- [ ] 测试动态文本（如时间、数量）
- [ ] 验证 contentDescription 的可访问性

### 4. 自动化检查

**Lint 规则**：
- 检测硬编码字符串
- 检测缺失的翻译
- 检测未使用的字符串资源

**CI/CD 集成**：
```bash
# 运行 Lint 检查
./gradlew lint

# 检查翻译完整性
./scripts/check_translations.sh

# 运行 i18n 相关测试
./gradlew testDebugUnitTest --tests "*I18n*"
```

## 实施计划概述

实施将分为以下阶段：

1. **准备阶段**：审查代码，识别所有硬编码文本
2. **资源创建阶段**：在 strings.xml 中创建新的字符串资源
3. **翻译阶段**：为所有新字符串资源提供翻译
4. **代码替换阶段**：将硬编码文本替换为字符串资源引用
5. **测试阶段**：验证所有语言的显示效果
6. **文档阶段**：更新开发指南和最佳实践

详细的实施步骤将在任务列表中定义。
