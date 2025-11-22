# 设计文档

## 概述

本设计实现了一个 Material 3 风格的 Bottom Sheet 选择器，允许用户在任务列表页面的顶部应用栏点击标题后，从底部弹出的选择器中选择不同的项目类型（任务、日程、事件）。这为未来的多类型项目管理系统奠定了基础。

## 架构

### 组件层次结构

```
TaskListScreen
├── TopAppBar (修改)
│   └── ItemTypeSelectorButton (新增)
│       └── Text + DropdownIcon
└── ItemTypeSelectorBottomSheet (新增)
    └── ModalBottomSheet
        └── ItemTypeOptions
            ├── TaskOption
            ├── ScheduleOption
            └── EventOption
```

### 数据流

```
用户点击标题
    ↓
显示 Bottom Sheet
    ↓
用户选择项目类型
    ↓
更新 ViewModel 状态
    ↓
保存到 PreferencesManager
    ↓
更新 TopAppBar 标题
    ↓
关闭 Bottom Sheet
```

## 组件和接口

### 1. ItemType 枚举（扩展现有）

当前 `ItemType` 枚举定义了 `TASK`, `MILESTONE`, `COUNTDOWN`。我们需要将其更新为符合新需求的类型：

```kotlin
enum class ItemType(val value: Int) {
    TASK(0),        // 任务
    SCHEDULE(1),    // 日程
    EVENT(2);       // 事件
    
    companion object {
        fun fromValue(value: Int): ItemType {
            return entries.find { it.value == value } ?: TASK
        }
    }
}
```

### 2. ItemTypeSelectorBottomSheet 组件

新建 Composable 组件，用于显示项目类型选择器：

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemTypeSelectorBottomSheet(
    currentType: ItemType,
    onDismiss: () -> Unit,
    onTypeSelected: (ItemType) -> Unit
)
```

**功能：**
- 使用 `ModalBottomSheet` 实现 Material 3 风格
- 显示三个选项：任务、日程、事件
- 当前选中的类型使用不同的视觉样式（选中状态）
- 点击选项后触发回调并关闭 Bottom Sheet

**UI 元素：**
- 拖动手柄（Drag Handle）
- 标题："选择类型"
- 三个可选项，每个包含：
  - 图标（Material Icons）
  - 标签文本
  - 选中指示器（Radio Button 或 Checkmark）

### 3. TaskListTopBar 修改

修改现有的 `TaskListTopBar` 组件，使标题可点击：

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskListTopBar(
    currentItemType: ItemType,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onFilterClick: () -> Unit,
    onItemTypeSelectorClick: () -> Unit  // 新增
)
```

**修改内容：**
- 将标题文本包装在 `Surface` 或 `TextButton` 中使其可点击
- 添加下拉箭头图标（`Icons.Default.ArrowDropDown`）
- 根据 `currentItemType` 显示对应的标题文本
- 点击时触发 `onItemTypeSelectorClick` 回调

### 4. TaskViewModel 扩展

在 `TaskViewModel` 中添加项目类型状态管理：

```kotlin
class TaskViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
    // ... 其他依赖
) : ViewModel() {
    
    // 当前选中的项目类型
    private val _currentItemType = MutableStateFlow(ItemType.TASK)
    val currentItemType: StateFlow<ItemType> = _currentItemType.asStateFlow()
    
    init {
        // 从 PreferencesManager 加载保存的项目类型
        viewModelScope.launch {
            preferencesManager.selectedItemType.collect { type ->
                _currentItemType.value = type
            }
        }
    }
    
    // 更新项目类型
    fun setItemType(type: ItemType) {
        viewModelScope.launch {
            _currentItemType.value = type
            preferencesManager.setSelectedItemType(type)
        }
    }
}
```

### 5. PreferencesManager 扩展

在 `PreferencesManager` 中添加项目类型持久化：

```kotlin
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object PreferencesKeys {
        // ... 现有 keys
        val SELECTED_ITEM_TYPE = intPreferencesKey("selected_item_type")
    }
    
    val selectedItemType: Flow<ItemType> = dataStore.data
        .map { preferences ->
            val value = preferences[PreferencesKeys.SELECTED_ITEM_TYPE] ?: ItemType.TASK.value
            ItemType.fromValue(value)
        }
    
    suspend fun setSelectedItemType(type: ItemType) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SELECTED_ITEM_TYPE] = type.value
        }
    }
}
```

## 数据模型

### ItemType 扩展

更新 `ItemType` 枚举以支持新的类型定义，并添加辅助方法：

```kotlin
enum class ItemType(val value: Int) {
    TASK(0),
    SCHEDULE(1),
    EVENT(2);
    
    companion object {
        fun fromValue(value: Int): ItemType {
            return entries.find { it.value == value } ?: TASK
        }
    }
    
    // 获取显示名称的字符串资源 ID
    @StringRes
    fun getDisplayNameResId(): Int {
        return when (this) {
            TASK -> R.string.item_type_task
            SCHEDULE -> R.string.item_type_schedule
            EVENT -> R.string.item_type_event
        }
    }
    
    // 获取图标
    fun getIcon(): ImageVector {
        return when (this) {
            TASK -> Icons.Default.Task
            SCHEDULE -> Icons.Default.CalendarMonth
            EVENT -> Icons.Default.Event
        }
    }
}
```

## UI 设计规范

### Bottom Sheet 样式

- **容器颜色**：`MaterialTheme.colorScheme.surface`
- **圆角**：顶部 28.dp（Material 3 标准）
- **拖动手柄**：4.dp 高度，32.dp 宽度，居中显示
- **内边距**：水平 24.dp，垂直 16.dp
- **高度**：wrap_content，最大不超过屏幕高度的 50%

### 选项列表样式

每个选项使用 `ListItem` 或自定义 `Surface`：

- **高度**：64.dp
- **图标大小**：24.dp
- **图标颜色**：
  - 未选中：`MaterialTheme.colorScheme.onSurfaceVariant`
  - 选中：`MaterialTheme.colorScheme.primary`
- **文本样式**：`MaterialTheme.typography.bodyLarge`
- **文本颜色**：
  - 未选中：`MaterialTheme.colorScheme.onSurface`
  - 选中：`MaterialTheme.colorScheme.primary`
- **选中指示器**：`Icons.Default.Check`，显示在右侧
- **点击效果**：Ripple effect

### TopAppBar 标题按钮样式

- **文本样式**：`MaterialTheme.typography.titleLarge`
- **字体粗细**：`FontWeight.Bold`
- **下拉图标**：`Icons.Default.ArrowDropDown`，16.dp
- **图标颜色**：与文本颜色相同
- **点击效果**：Ripple effect
- **内边距**：水平 8.dp

## 错误处理

### 场景 1：PreferencesManager 读取失败

- **处理**：使用默认值 `ItemType.TASK`
- **日志**：记录错误信息到 Logcat

### 场景 2：PreferencesManager 写入失败

- **处理**：显示 Snackbar 提示用户保存失败
- **回退**：保持当前 UI 状态不变
- **日志**：记录错误信息到 Logcat

### 场景 3：Bottom Sheet 显示异常

- **处理**：捕获异常，自动关闭 Bottom Sheet
- **日志**：记录错误堆栈

## 测试策略

### 单元测试

1. **ItemType 枚举测试**
   - 测试 `fromValue()` 方法的正确性
   - 测试默认值处理
   - 测试 `getDisplayNameResId()` 和 `getIcon()` 方法

2. **PreferencesManager 测试**
   - 测试 `setSelectedItemType()` 保存功能
   - 测试 `selectedItemType` Flow 的数据流
   - 测试默认值处理

3. **TaskViewModel 测试**
   - 测试 `setItemType()` 方法
   - 测试初始化时从 PreferencesManager 加载数据
   - 测试状态更新的正确性

### UI 测试

1. **Bottom Sheet 显示测试**
   - 验证点击标题后 Bottom Sheet 正确显示
   - 验证 Bottom Sheet 包含三个选项
   - 验证当前选中的类型有正确的视觉指示

2. **选项选择测试**
   - 验证点击选项后 Bottom Sheet 关闭
   - 验证 TopAppBar 标题更新为选中的类型
   - 验证选择被正确保存

3. **持久化测试**
   - 验证应用重启后选中的类型被正确恢复
   - 验证首次启动时默认显示"任务"

4. **国际化测试**
   - 验证不同语言下标签显示正确
   - 验证中文、日语、越南语、英语的支持

### 集成测试

1. **完整流程测试**
   - 用户打开应用 → 点击标题 → 选择类型 → 验证 UI 更新 → 重启应用 → 验证持久化

2. **边界情况测试**
   - 快速连续点击标题
   - 在 Bottom Sheet 动画过程中点击外部
   - 旋转屏幕时的状态保持

## 国际化

### 新增字符串资源

需要在所有语言的 `strings.xml` 中添加以下资源：

```xml
<!-- Item Types -->
<string name="item_type_task">任务</string>
<string name="item_type_schedule">日程</string>
<string name="item_type_event">事件</string>
<string name="item_type_selector_title">选择类型</string>

<!-- Content Descriptions -->
<string name="cd_item_type_selector">项目类型选择器</string>
<string name="cd_dropdown_icon">下拉图标</string>
```

### 支持的语言

- 简体中文（zh-rCN）
- 日语（ja）
- 越南语（vi）
- 英语（默认）

## 性能考虑

1. **状态管理**：使用 `StateFlow` 确保状态更新的高效性
2. **重组优化**：使用 `remember` 和 `derivedStateOf` 减少不必要的重组
3. **动画性能**：Bottom Sheet 使用 Material 3 的标准动画，性能已优化
4. **持久化**：使用 DataStore 的异步 API，不阻塞主线程

## 可访问性

1. **内容描述**：为所有图标和可点击元素添加 `contentDescription`
2. **语义标签**：使用 `Modifier.semantics` 为 Bottom Sheet 添加语义信息
3. **焦点管理**：确保键盘导航时焦点顺序正确
4. **屏幕阅读器**：确保所有文本和操作都能被屏幕阅读器正确读取

## 未来扩展

1. **数据库分离**：为每种项目类型创建独立的数据库表和 DAO
2. **页面实现**：为"日程"和"事件"类型实现独立的 UI 页面
3. **数据迁移**：提供从旧的 `ItemType` 定义迁移到新定义的工具
4. **快捷切换**：考虑添加手势或快捷键快速切换项目类型
5. **自定义类型**：允许用户创建自定义的项目类型

## 技术债务

1. **ItemType 重命名**：当前 `ItemType` 枚举中的 `MILESTONE` 和 `COUNTDOWN` 需要重命名为 `SCHEDULE` 和 `EVENT`，这可能影响现有数据
2. **数据迁移策略**：需要制定清晰的数据迁移策略，确保现有用户的数据不丢失
3. **向后兼容性**：考虑保留旧的枚举值作为别名，以支持数据迁移期间的兼容性
