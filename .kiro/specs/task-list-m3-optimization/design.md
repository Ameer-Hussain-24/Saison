# 任务列表 Material 3 优化设计文档

## 概述

本设计文档详细说明如何将Saison应用的任务列表页面优化为符合Material 3设计规范的优雅、简洁、大方的Todo列表界面。设计重点关注视觉层次、交互流畅性、信息密度和用户体验。

## 架构

### 组件层次结构

```
TaskListScreen (主屏幕)
├── TopAppBar (顶部栏)
│   ├── SearchBar (搜索栏)
│   └── ActionButtons (操作按钮)
├── TaskStatsCard (统计卡片 - 优化版)
│   ├── CircularProgressIndicator (环形进度)
│   └── AnimatedCounter (动画数字)
├── FilterChipRow (过滤器芯片行)
├── GroupHeader (分组标题 - 粘性)
└── TaskList (任务列表)
    ├── TaskCard (任务卡片 - 优化版)
    │   ├── SwipeableCard (可滑动容器)
    │   ├── CheckboxButton (完成复选框)
    │   ├── TaskContent (任务内容)
    │   ├── MetaInfo (元信息)
    │   ├── PriorityIndicator (优先级指示器)
    │   └── FavoriteButton (收藏按钮)
    └── CompletedTasksSection (已完成任务区域)
        ├── SectionDivider (分隔线)
        └── ExpandCollapseButton (展开/折叠按钮)
```

### 数据流

```
ViewModel (TaskViewModel)
    ↓
StateFlow<List<Task>>
    ↓
TaskListScreen
    ↓
LazyColumn with AnimatedVisibility
    ↓
TaskCard with Gesture Detection
```

## 组件设计

### 1. TaskCard 优化设计

#### 视觉设计

**卡片结构：**
```
┌─────────────────────────────────────────────┐
│ [✓] 任务标题 (最多1行)              [★] [•] │
│     描述文本 (最多2行，灰色)                │
│     📅 今天 14:00  📍 办公室  ☑ 2/5        │
└─────────────────────────────────────────────┘
```

**尺寸规范：**
- 卡片圆角：12dp (Material 3 标准)
- 内边距：16dp
- 卡片间距：8dp
- 最小高度：72dp
- 阴影：elevation = 1dp (未完成), 0dp (已完成)

**颜色方案：**
- 未完成任务：`surfaceVariant` 背景
- 已完成任务：`surfaceVariant` + 50% alpha
- 逾期任务：`errorContainer` + 30% alpha
- 收藏任务：`primaryContainer` 背景

**优先级指示器：**
- 位置：卡片右侧，垂直居中
- 形状：圆形，直径 10dp
- 颜色映射：
  - LOW: `Color(0xFF4CAF50)` - 绿色
  - MEDIUM: `Color(0xFF2196F3)` - 蓝色
  - HIGH: `Color(0xFFFF9800)` - 橙色
  - URGENT: `Color(0xFFF44336)` - 红色

#### 交互设计

**手势操作：**

1. **向右滑动 (完成任务)**
   ```kotlin
   SwipeableState(
       threshold = 0.3f,  // 30% 滑动距离触发
       direction = SwipeDirection.EndToStart,
       backgroundContent = {
           // 绿色背景 + 勾选图标
           Box(
               modifier = Modifier
                   .fillMaxSize()
                   .background(Color(0xFF4CAF50)),
               contentAlignment = Alignment.CenterEnd
           ) {
               Icon(Icons.Default.Check, tint = Color.White)
           }
       }
   )
   ```

2. **向左滑动 (快捷操作)**
   ```kotlin
   SwipeableState(
       threshold = 0.3f,
       direction = SwipeDirection.StartToEnd,
       backgroundContent = {
           Row {
               // 编辑按钮 (蓝色背景)
               ActionButton(
                   icon = Icons.Default.Edit,
                   color = Color(0xFF2196F3),
                   onClick = { /* 编辑 */ }
               )
               // 删除按钮 (红色背景)
               ActionButton(
                   icon = Icons.Default.Delete,
                   color = Color(0xFFF44336),
                   onClick = { /* 删除 */ }
               )
           }
       }
   )
   ```

3. **长按 (多选模式)**
   - 触发震动反馈
   - 卡片左侧显示复选框
   - 顶部栏切换为批量操作工具栏

4. **点击 (查看详情)**
   - 涟漪效果动画
   - 导航到任务详情页

**动画规范：**

```kotlin
// 完成状态切换动画
val scale by animateFloatAsState(
    targetValue = if (isCompleted) 0.95f else 1f,
    animationSpec = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )
)

// 添加任务动画
AnimatedVisibility(
    visible = visible,
    enter = fadeIn(animationSpec = tween(300)) + 
            slideInVertically(initialOffsetY = { -it }),
    exit = fadeOut(animationSpec = tween(200)) + 
           slideOutVertically(targetOffsetY = { -it })
)

// 删除任务动画
AnimatedVisibility(
    visible = !isDeleted,
    exit = shrinkVertically(animationSpec = tween(300)) + 
           fadeOut(animationSpec = tween(200))
)
```

### 2. TaskStatsCard 优化设计

#### 视觉设计

```
┌─────────────────────────────────────────────┐
│  ⭕ 75%        📋 12 个        ⚠️ 3 个     │
│  今日完成      未完成任务      逾期任务      │
└─────────────────────────────────────────────┘
```

**布局规范：**
- 背景：`primaryContainer`
- 圆角：16dp
- 内边距：16dp
- 高度：自适应，最小 80dp

**环形进度指示器：**
```kotlin
CircularProgressIndicator(
    progress = completionRate,
    modifier = Modifier.size(48.dp),
    strokeWidth = 4.dp,
    color = MaterialTheme.colorScheme.primary,
    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
)
```

**数字滚动动画：**
```kotlin
AnimatedContent(
    targetState = count,
    transitionSpec = {
        if (targetState > initialState) {
            slideInVertically { -it } + fadeIn() togetherWith
            slideOutVertically { it } + fadeOut()
        } else {
            slideInVertically { it } + fadeIn() togetherWith
            slideOutVertically { -it } + fadeOut()
        }
    }
) { count ->
    Text(
        text = count.toString(),
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
    )
}
```

### 3. 智能分组与排序

#### 分组策略

**按日期分组：**
```kotlin
sealed class DateGroup {
    object Overdue : DateGroup()      // 逾期
    object Today : DateGroup()        // 今天
    object Tomorrow : DateGroup()     // 明天
    object ThisWeek : DateGroup()     // 本周
    object NextWeek : DateGroup()     // 下周
    object Later : DateGroup()        // 以后
    object NoDate : DateGroup()       // 无日期
}
```

**按优先级分组：**
```kotlin
enum class PriorityGroup {
    URGENT,    // 紧急
    HIGH,      // 高
    MEDIUM,    // 中
    LOW        // 低
}
```

**按项目/标签分组：**
```kotlin
data class TagGroup(
    val tag: Tag,
    val tasks: List<Task>
)
```

#### 排序逻辑

**默认排序规则：**
1. 收藏任务置顶
2. 未完成任务在前
3. 按优先级排序（URGENT > HIGH > MEDIUM > LOW）
4. 按截止日期排序（近期在前）
5. 按创建时间排序（新的在前）
6. 已完成任务置底

**实现代码：**
```kotlin
fun List<Task>.smartSort(): List<Task> {
    val (completed, incomplete) = partition { it.isCompleted }
    
    val sortedIncomplete = incomplete.sortedWith(
        compareByDescending<Task> { it.isFavorite }
            .thenByDescending { it.priority.ordinal }
            .thenBy { it.dueDate }
            .thenByDescending { it.createdAt }
    )
    
    return sortedIncomplete + completed
}
```

#### 粘性分组标题

```kotlin
@Composable
fun StickyGroupHeader(
    group: DateGroup,
    taskCount: Int
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = group.displayName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    text = taskCount.toString(),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}
```

### 4. 已完成任务区域

#### 设计规范

```
┌─────────────────────────────────────────────┐
│ ─────────── 已完成 (5) ▼ ──────────────    │
│                                             │
│ [✓] 已完成的任务 1 (50% 透明度)            │
│ [✓] 已完成的任务 2                          │
│ ...                                         │
└─────────────────────────────────────────────┘
```

**分隔线设计：**
```kotlin
Row(
    modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 16.dp),
    verticalAlignment = Alignment.CenterVertically
) {
    HorizontalDivider(
        modifier = Modifier.weight(1f),
        color = MaterialTheme.colorScheme.outlineVariant
    )
    
    TextButton(
        onClick = { isExpanded = !isExpanded }
    ) {
        Text("已完成 ($completedCount)")
        Icon(
            imageVector = if (isExpanded) 
                Icons.Default.ExpandLess 
            else 
                Icons.Default.ExpandMore,
            contentDescription = null
        )
    }
    
    HorizontalDivider(
        modifier = Modifier.weight(1f),
        color = MaterialTheme.colorScheme.outlineVariant
    )
}
```

**折叠/展开动画：**
```kotlin
AnimatedVisibility(
    visible = isExpanded,
    enter = expandVertically(animationSpec = tween(300)) + fadeIn(),
    exit = shrinkVertically(animationSpec = tween(300)) + fadeOut()
) {
    LazyColumn {
        items(completedTasks) { task ->
            TaskCard(
                task = task,
                modifier = Modifier.alpha(0.5f)
            )
        }
    }
}
```

### 5. 快速添加任务

#### 底部表单设计

替代对话框，使用 ModalBottomSheet：

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickAddTaskSheet(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    var taskInput by remember { mutableStateOf("") }
    var isVoiceInput by remember { mutableStateOf(false) }
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "快速添加任务",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = taskInput,
                onValueChange = { taskInput = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("例如：明天下午3点开会") },
                trailingIcon = {
                    IconButton(onClick = { isVoiceInput = true }) {
                        Icon(Icons.Default.Mic, contentDescription = "语音输入")
                    }
                },
                minLines = 2,
                maxLines = 4
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text("取消")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { onConfirm(taskInput) },
                    enabled = taskInput.isNotBlank()
                ) {
                    Text("添加")
                }
            }
        }
    }
}
```

### 6. 空状态设计

#### 视觉设计

```kotlin
@Composable
fun EmptyTaskState(
    filterMode: TaskFilterMode
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 插图
        Icon(
            imageVector = Icons.Outlined.TaskAlt,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 主标题
        Text(
            text = when (filterMode) {
                TaskFilterMode.ALL -> "还没有任务"
                TaskFilterMode.ACTIVE -> "没有待办任务"
                TaskFilterMode.COMPLETED -> "还没有完成任务"
            },
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // 副标题
        Text(
            text = "点击下方按钮创建第一个任务",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // 操作按钮
        FilledTonalButton(
            onClick = { /* 创建任务 */ }
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("创建任务")
        }
    }
}
```

### 7. 响应式布局

#### 断点定义

```kotlin
enum class WindowSize {
    COMPACT,    // < 600dp (手机竖屏)
    MEDIUM,     // 600-840dp (手机横屏/小平板)
    EXPANDED    // > 840dp (平板/折叠屏)
}
```

#### 布局适配

**COMPACT (< 600dp):**
- 单列列表
- FAB 在右下角
- 全屏底部表单

**MEDIUM (600-840dp):**
- 双列网格布局
- FAB 居中底部
- 半屏底部表单

**EXPANDED (> 840dp):**
- 三列网格布局 或 列表+侧边栏
- 侧边栏显示任务详情
- 模态对话框替代底部表单

```kotlin
@Composable
fun ResponsiveTaskList(
    windowSize: WindowSize,
    tasks: List<Task>
) {
    when (windowSize) {
        WindowSize.COMPACT -> {
            LazyColumn {
                items(tasks) { task ->
                    TaskCard(task)
                }
            }
        }
        WindowSize.MEDIUM -> {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(tasks) { task ->
                    TaskCard(task)
                }
            }
        }
        WindowSize.EXPANDED -> {
            Row {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.weight(2f)
                ) {
                    items(tasks) { task ->
                        TaskCard(task)
                    }
                }
                TaskDetailSidebar(
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
```

## 数据模型扩展

### Task 模型添加字段

```kotlin
data class Task(
    // ... 现有字段 ...
    val isFavorite: Boolean = false,  // 收藏标记
    val sortOrder: Int = 0,           // 自定义排序
    val tags: List<Tag> = emptyList() // 标签列表
)
```

### ViewModel 状态管理

```kotlin
data class TaskListUiState(
    val tasks: List<Task> = emptyList(),
    val groupMode: GroupMode = GroupMode.DATE,
    val sortMode: SortMode = SortMode.SMART,
    val filterMode: TaskFilterMode = TaskFilterMode.ALL,
    val searchQuery: String = "",
    val isMultiSelectMode: Boolean = false,
    val selectedTasks: Set<Long> = emptySet(),
    val isCompletedExpanded: Boolean = false,
    val completionRate: Float = 0f,
    val incompleteCount: Int = 0,
    val overdueCount: Int = 0,
    val todayCompletedCount: Int = 0
)

enum class GroupMode {
    NONE,       // 不分组
    DATE,       // 按日期
    PRIORITY,   // 按优先级
    TAG         // 按标签
}

enum class SortMode {
    SMART,      // 智能排序
    DATE_ASC,   // 日期升序
    DATE_DESC,  // 日期降序
    PRIORITY,   // 优先级
    TITLE       // 标题
}
```

## 错误处理

### 网络错误

```kotlin
@Composable
fun NetworkErrorSnackbar(
    message: String,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    Snackbar(
        action = {
            TextButton(onClick = onRetry) {
                Text("重试")
            }
        },
        dismissAction = {
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "关闭")
            }
        }
    ) {
        Text(message)
    }
}
```

### 操作失败

- 使用 SnackBar 显示错误信息
- 提供"撤销"操作（删除任务后）
- 自动重试机制（网络请求）

## 测试策略

### UI 测试

1. **快照测试**
   - TaskCard 各种状态
   - 空状态界面
   - 统计卡片

2. **交互测试**
   - 滑动手势
   - 长按多选
   - 完成/取消完成

3. **动画测试**
   - 验证动画完成
   - 检查性能

### 单元测试

1. **排序逻辑测试**
2. **分组逻辑测试**
3. **过滤逻辑测试**

## 性能优化

### LazyColumn 优化

```kotlin
LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp)
) {
    items(
        items = tasks,
        key = { it.id },  // 稳定的 key
        contentType = { "task" }  // 内容类型
    ) { task ->
        TaskCard(
            task = task,
            modifier = Modifier.animateItemPlacement()  // 位置动画
        )
    }
}
```

### 图片加载优化

- 使用 Coil 异步加载附件缩略图
- 实现图片缓存策略
- 懒加载非可见区域

### 状态管理优化

- 使用 `derivedStateOf` 避免不必要的重组
- 合理使用 `remember` 和 `rememberSaveable`
- 避免在 Composable 中进行复杂计算

## 可访问性

### 语义标签

```kotlin
TaskCard(
    modifier = Modifier.semantics {
        contentDescription = "任务：${task.title}，" +
            "优先级：${task.priority}，" +
            if (task.isCompleted) "已完成" else "未完成"
        role = Role.Checkbox
    }
)
```

### 触摸目标

- 最小触摸目标：48dp × 48dp
- 复选框和按钮符合无障碍标准

### 颜色对比度

- 确保文字与背景对比度 ≥ 4.5:1
- 使用 Material 3 语义颜色系统

## 国际化

### 支持语言

- 中文（简体/繁体）
- 英文
- 日文
- 越南文

### 日期格式

```kotlin
fun formatDueDate(
    dueDate: LocalDateTime,
    locale: Locale
): String {
    val now = LocalDateTime.now()
    val today = now.toLocalDate()
    val dueDay = dueDate.toLocalDate()
    
    return when {
        dueDay == today -> stringResource(R.string.date_today)
        dueDay == today.plusDays(1) -> stringResource(R.string.date_tomorrow)
        dueDay == today.minusDays(1) -> stringResource(R.string.date_yesterday)
        else -> DateTimeFormatter
            .ofLocalizedDate(FormatStyle.SHORT)
            .withLocale(locale)
            .format(dueDate)
    }
}
```

## 总结

本设计文档提供了一个全面的Material 3风格任务列表优化方案，重点关注：

1. **视觉优雅**：清晰的层次、适度的留白、柔和的色彩
2. **交互流畅**：手势操作、平滑动画、即时反馈
3. **信息密度**：恰当的信息展示、智能分组、可折叠区域
4. **用户体验**：快速操作、智能排序、响应式布局

所有设计决策都基于Material 3设计规范，确保与应用整体风格保持一致。
