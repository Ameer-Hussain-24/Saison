# 任务预览详情页面设计文档

## 概述

本设计文档详细说明如何为Saison应用创建全新的任务预览详情页面。该页面采用只读视图设计，用于直观地查看任务的所有信息，与现有的任务编辑页面（TaskDetailScreen）分离。用户通过点击任务卡片进入预览页面，通过侧滑编辑按钮进入编辑页面。

## 架构

### 组件层次结构

```
TaskPreviewScreen (任务预览主屏幕 - 新建)
├── TopAppBar (顶部栏)
│   ├── BackButton (返回按钮)
│   ├── Title (标题: "任务详情")
│   ├── EditButton (编辑按钮)
│   └── MoreActionsMenu (更多操作菜单)
├── ScrollableContent (可滚动内容)
│   ├── TaskHeader (任务头部)
│   │   ├── CompletionIcon (完成状态图标)
│   │   └── TaskTitle (任务标题)
│   ├── DescriptionCard (描述卡片)
│   ├── InfoSection (信息区域)
│   │   ├── PriorityBadge (优先级标签)
│   │   ├── DueDateInfo (截止日期信息)
│   │   ├── RecurrenceInfo (重复规则信息)
│   │   └── LocationInfo (位置信息)
│   ├── TagsSection (标签区域)
│   ├── SubtasksCard (子任务卡片)
│   ├── AttachmentsGrid (附件网格)
│   └── MetadataFooter (元数据页脚)
└── FloatingActionButton (浮动操作按钮)
    └── ToggleCompletionButton (切换完成状态按钮)

TaskEditScreen (任务编辑屏幕 - 重命名)
├── 保持现有TaskDetailScreen的所有功能
└── 顶部栏标题改为"任务编辑"
```

### 导航流程

```
TaskListScreen
    ├── 点击任务卡片 → TaskPreviewScreen (新路由: taskPreview/{taskId})
    └── 侧滑编辑按钮 → TaskEditScreen (现有路由: taskDetail/{taskId})

TaskPreviewScreen
    ├── 点击编辑按钮 → TaskEditScreen
    ├── 点击FAB切换完成状态 → 更新任务状态
    └── 点击返回按钮 → 返回TaskListScreen

TaskEditScreen
    ├── 保存后 → 返回上一页面
    └── 取消 → 返回上一页面
```

### 数据流

```
TaskPreviewViewModel (新建)
    ↓
StateFlow<Task?>
StateFlow<TaskPreviewUiState>
    ↓
TaskPreviewScreen
    ↓
User Actions (toggleCompletion, navigateToEdit, delete)
    ↓
Repository Layer
```

## 组件设计

### 1. TaskPreviewScreen 整体布局

#### 视觉设计

**布局结构：**
```
┌─────────────────────────────────────────────┐
│ ← 任务详情                    ✏️ ⋮          │ TopAppBar
├─────────────────────────────────────────────┤
│                                             │
│ ✓ 完成任务标题                              │ Task Header
│   (大号字体，已完成显示删除线)               │
│                                             │
│ ┌─────────────────────────────────────────┐ │
│ │ 📝 描述                                 │ │
│ │ 这是任务的详细描述内容...               │ │ Description Card
│ └─────────────────────────────────────────┘ │
│                                             │
│ [高优先级] 📅 2024年10月31日 下午3:00      │ Info Section
│ 还有3天                                     │
│                                             │
│ 🔁 每周一重复                               │ Recurrence
│ 下次: 11月4日                               │
│                                             │
│ 📍 办公室                                   │ Location
│                                             │
│ 🏷️ [工作] [重要]                            │ Tags
│                                             │
│ ┌─────────────────────────────────────────┐ │
│ │ ☑️ 子任务 (2/5) ━━━━━━━━━━━━━━━━━━━━━━ │ │
│ │ ✓ 子任务1                               │ │
│ │ ☐ 子任务2                               │ │ Subtasks Card
│ │ ☐ 子任务3                               │ │
│ └─────────────────────────────────────────┘ │
│                                             │
│ 📎 附件 (3)                                 │
│ [图片1] [文档1] [图片2]                     │ Attachments
│                                             │
│ 创建于 3天前 · 最后修改 1小时前             │ Metadata
│                                             │
└─────────────────────────────────────────────┘
                    [✓ 标记完成]                FAB
```


**尺寸规范：**
- 内容区域内边距：16dp
- 组件间距：12dp
- 卡片圆角：12dp
- 卡片内边距：16dp

**颜色方案：**
- 背景：`surface`
- 卡片背景：`surfaceVariant`
- 已完成任务标题：`onSurfaceVariant` + 删除线
- 未完成任务标题：`onSurface`
- 逾期日期：`error`
- 正常日期：`primary`

### 2. TaskPreviewScreen 实现

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskPreviewScreen(
    taskId: Long,
    viewModel: TaskPreviewViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val task by viewModel.task.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }
    
    LaunchedEffect(taskId) {
        viewModel.loadTask(taskId)
    }
    
    task?.let { currentTask ->
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.task_preview_title)) },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = stringResource(R.string.cd_navigate_back)
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { onNavigateToEdit(taskId) }) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = stringResource(R.string.cd_edit_task)
                            )
                        }
                        IconButton(onClick = { showMoreMenu = true }) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = stringResource(R.string.cd_more_actions)
                            )
                        }
                        DropdownMenu(
                            expanded = showMoreMenu,
                            onDismissRequest = { showMoreMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.action_share)) },
                                onClick = { /* TODO: 分享任务 */ },
                                leadingIcon = {
                                    Icon(Icons.Default.Share, contentDescription = null)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.action_delete)) },
                                onClick = {
                                    showMoreMenu = false
                                    showDeleteDialog = true
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            )
                        }
                    }
                )
            },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = { viewModel.toggleCompletion() },
                    icon = {
                        Icon(
                            if (currentTask.isCompleted) Icons.Default.CheckCircle
                            else Icons.Default.RadioButtonUnchecked,
                            contentDescription = null
                        )
                    },
                    text = {
                        Text(
                            if (currentTask.isCompleted)
                                stringResource(R.string.action_mark_incomplete)
                            else
                                stringResource(R.string.action_mark_complete)
                        )
                    }
                )
            },
            modifier = modifier
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 任务头部
                TaskHeader(
                    task = currentTask,
                    modifier = Modifier.fillMaxWidth()
                )
                
                // 描述卡片
                if (!currentTask.description.isNullOrBlank()) {
                    DescriptionCard(
                        description = currentTask.description,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                // 信息区域
                InfoSection(
                    task = currentTask,
                    modifier = Modifier.fillMaxWidth()
                )
                
                // 标签
                if (currentTask.tags.isNotEmpty()) {
                    TagsSection(
                        tags = currentTask.tags,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                // 子任务
                if (currentTask.subtasks.isNotEmpty()) {
                    SubtasksCard(
                        subtasks = currentTask.subtasks,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                // 附件
                if (currentTask.attachments.isNotEmpty()) {
                    AttachmentsGrid(
                        attachments = currentTask.attachments,
                        onAttachmentClick = { /* TODO: 预览附件 */ },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                // 元数据页脚
                MetadataFooter(
                    createdAt = currentTask.createdAt,
                    updatedAt = currentTask.updatedAt,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        
        // 删除确认对话框
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text(stringResource(R.string.dialog_delete_task_title)) },
                text = { Text(stringResource(R.string.dialog_delete_task_message)) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteTask()
                            showDeleteDialog = false
                            onNavigateBack()
                        }
                    ) {
                        Text(
                            stringResource(R.string.action_delete),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text(stringResource(R.string.action_cancel))
                    }
                }
            )
        }
    } ?: run {
        // 加载状态或任务不存在
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            when (uiState) {
                is TaskPreviewUiState.Loading -> CircularProgressIndicator()
                is TaskPreviewUiState.Error -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.error_task_not_found),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Button(onClick = onNavigateBack) {
                            Text(stringResource(R.string.action_go_back))
                        }
                    }
                }
                else -> {}
            }
        }
    }
}
```

### 3. TaskHeader 组件

```kotlin
@Composable
fun TaskHeader(
    task: Task,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (task.isCompleted) Icons.Default.CheckCircle
            else Icons.Default.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (task.isCompleted)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(32.dp)
        )
        
        Text(
            text = task.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = if (task.isCompleted)
                MaterialTheme.colorScheme.onSurfaceVariant
            else
                MaterialTheme.colorScheme.onSurface,
            textDecoration = if (task.isCompleted)
                TextDecoration.LineThrough
            else
                null,
            modifier = Modifier.weight(1f)
        )
    }
}
```

### 4. DescriptionCard 组件

```kotlin
@Composable
fun DescriptionCard(
    description: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Description,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(R.string.task_description),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Text(
                text = description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
```

### 5. InfoSection 组件

```kotlin
@Composable
fun InfoSection(
    task: Task,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 优先级和日期
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 优先级标签
            PriorityBadge(priority = task.priority)
            
            // 截止日期
            task.dueDate?.let { dueDate ->
                DueDateInfo(
                    dueDate = dueDate,
                    isCompleted = task.isCompleted
                )
            }
        }
        
        // 重复规则
        task.recurrenceRule?.let { rule ->
            RecurrenceInfo(
                rule = rule,
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        // 位置
        task.location?.let { location ->
            LocationInfo(
                location = location,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun PriorityBadge(
    priority: Priority,
    modifier: Modifier = Modifier
) {
    val (label, color) = when (priority) {
        Priority.URGENT -> stringResource(R.string.priority_urgent) to MaterialTheme.colorScheme.error
        Priority.HIGH -> stringResource(R.string.priority_high) to MaterialTheme.colorScheme.tertiary
        Priority.MEDIUM -> stringResource(R.string.priority_medium) to MaterialTheme.colorScheme.primary
        Priority.LOW -> stringResource(R.string.priority_low) to MaterialTheme.colorScheme.outline
    }
    
    AssistChip(
        onClick = {},
        label = { Text(label) },
        leadingIcon = {
            Icon(
                Icons.Default.Flag,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = color.copy(alpha = 0.15f),
            labelColor = color,
            leadingIconContentColor = color
        ),
        modifier = modifier
    )
}

@Composable
fun DueDateInfo(
    dueDate: LocalDateTime,
    isCompleted: Boolean,
    modifier: Modifier = Modifier
) {
    val now = LocalDateTime.now()
    val isOverdue = !isCompleted && dueDate.isBefore(now)
    val relativeTime = getRelativeTimeString(dueDate)
    
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.Schedule,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = if (isOverdue) MaterialTheme.colorScheme.error
            else MaterialTheme.colorScheme.primary
        )
        
        Column {
            Text(
                text = dueDate.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm")),
                style = MaterialTheme.typography.bodyMedium,
                color = if (isOverdue) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = relativeTime,
                style = MaterialTheme.typography.bodySmall,
                color = if (isOverdue) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.primary
            )
        }
    }
}
```


### 6. RecurrenceInfo 和 LocationInfo 组件

```kotlin
@Composable
fun RecurrenceInfo(
    rule: RecurrenceRule,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Repeat,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            
            Column {
                Text(
                    text = formatRecurrenceRule(rule),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                
                Text(
                    text = stringResource(
                        R.string.recurrence_next_occurrence,
                        formatDate(rule.getNextOccurrence())
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun LocationInfo(
    location: String,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.LocationOn,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            
            Text(
                text = location,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
```

### 7. TagsSection 组件

```kotlin
@Composable
fun TagsSection(
    tags: List<Tag>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Label,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(R.string.task_tags),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tags.forEach { tag ->
                AssistChip(
                    onClick = {},
                    label = { Text(tag.name) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = tag.color.copy(alpha = 0.3f),
                        labelColor = tag.color
                    )
                )
            }
        }
    }
}
```

### 8. SubtasksCard 组件

```kotlin
@Composable
fun SubtasksCard(
    subtasks: List<Subtask>,
    modifier: Modifier = Modifier
) {
    val completedCount = subtasks.count { it.isCompleted }
    val totalCount = subtasks.size
    val progress = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f
    
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 标题和进度
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = stringResource(R.string.task_subtasks),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Text(
                    text = "$completedCount / $totalCount",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // 进度条
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            
            // 子任务列表
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                subtasks.forEach { subtask ->
                    SubtaskPreviewItem(subtask = subtask)
                }
            }
        }
    }
}

@Composable
fun SubtaskPreviewItem(
    subtask: Subtask,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (subtask.isCompleted) Icons.Default.CheckBox
            else Icons.Default.CheckBoxOutlineBlank,
            contentDescription = null,
            tint = if (subtask.isCompleted)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        
        Text(
            text = subtask.title,
            style = MaterialTheme.typography.bodyMedium,
            textDecoration = if (subtask.isCompleted)
                TextDecoration.LineThrough
            else
                null,
            color = if (subtask.isCompleted)
                MaterialTheme.colorScheme.onSurfaceVariant
            else
                MaterialTheme.colorScheme.onSurface
        )
    }
}
```

### 9. AttachmentsGrid 组件

```kotlin
@Composable
fun AttachmentsGrid(
    attachments: List<Attachment>,
    onAttachmentClick: (Attachment) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.AttachFile,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(R.string.task_attachments),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "(${attachments.size})",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.heightIn(max = 400.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(attachments) { attachment ->
                AttachmentPreviewCard(
                    attachment = attachment,
                    onClick = { onAttachmentClick(attachment) }
                )
            }
        }
    }
}

@Composable
fun AttachmentPreviewCard(
    attachment: Attachment,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.aspectRatio(1f)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            when (attachment.type) {
                AttachmentType.IMAGE -> {
                    AsyncImage(
                        model = attachment.uri,
                        contentDescription = attachment.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                else -> {
                    // 文档类型显示图标
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.InsertDriveFile,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = attachment.name,
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
```

### 10. MetadataFooter 组件

```kotlin
@Composable
fun MetadataFooter(
    createdAt: LocalDateTime,
    updatedAt: LocalDateTime,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = stringResource(
                R.string.task_created_at,
                getRelativeTimeString(createdAt)
            ),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        if (updatedAt != createdAt) {
            Text(
                text = stringResource(
                    R.string.task_updated_at,
                    getRelativeTimeString(updatedAt)
                ),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
```

### 11. TaskPreviewViewModel

```kotlin
@HiltViewModel
class TaskPreviewViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    private val taskId: Long = savedStateHandle.get<Long>("taskId") ?: 0L
    
    private val _task = MutableStateFlow<Task?>(null)
    val task: StateFlow<Task?> = _task.asStateFlow()
    
    private val _uiState = MutableStateFlow<TaskPreviewUiState>(TaskPreviewUiState.Loading)
    val uiState: StateFlow<TaskPreviewUiState> = _uiState.asStateFlow()
    
    fun loadTask(taskId: Long) {
        viewModelScope.launch {
            try {
                _uiState.value = TaskPreviewUiState.Loading
                taskRepository.getTaskById(taskId).collect { task ->
                    if (task != null) {
                        _task.value = task
                        _uiState.value = TaskPreviewUiState.Success
                    } else {
                        _uiState.value = TaskPreviewUiState.Error("Task not found")
                    }
                }
            } catch (e: Exception) {
                _uiState.value = TaskPreviewUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
    
    fun toggleCompletion() {
        viewModelScope.launch {
            _task.value?.let { currentTask ->
                val updatedTask = currentTask.copy(
                    isCompleted = !currentTask.isCompleted,
                    updatedAt = LocalDateTime.now()
                )
                taskRepository.updateTask(updatedTask)
            }
        }
    }
    
    fun deleteTask() {
        viewModelScope.launch {
            _task.value?.let { currentTask ->
                taskRepository.deleteTask(currentTask)
            }
        }
    }
}

sealed class TaskPreviewUiState {
    object Loading : TaskPreviewUiState()
    object Success : TaskPreviewUiState()
    data class Error(val message: String) : TaskPreviewUiState()
}
```

### 12. 导航配置更新

在 `SaisonNavHost.kt` 中添加新路由：

```kotlin
// 添加新的 Screen 定义
sealed class Screen(val route: String) {
    // ... 现有路由 ...
    
    object TaskPreview : Screen("taskPreview/{taskId}") {
        fun createRoute(taskId: Long) = "taskPreview/$taskId"
    }
    
    object TaskEdit : Screen("taskEdit/{taskId}") {  // 重命名原 TaskDetail
        fun createRoute(taskId: Long) = "taskEdit/$taskId"
    }
}

// 在 NavHost 中添加路由
composable(
    route = Screen.TaskPreview.route,
    arguments = listOf(
        navArgument("taskId") { type = NavType.LongType }
    )
) { backStackEntry ->
    val taskId = backStackEntry.arguments?.getLong("taskId") ?: 0L
    TaskPreviewScreen(
        taskId = taskId,
        onNavigateBack = {
            if (navController.currentBackStackEntry != null) {
                navController.popBackStack()
            }
        },
        onNavigateToEdit = { editTaskId ->
            navController.navigate(Screen.TaskEdit.createRoute(editTaskId))
        }
    )
}

// 更新原有的 TaskDetail 路由为 TaskEdit
composable(
    route = Screen.TaskEdit.route,
    arguments = listOf(
        navArgument("taskId") { type = NavType.LongType }
    )
) { backStackEntry ->
    val taskId = backStackEntry.arguments?.getLong("taskId") ?: 0L
    TaskDetailScreen(  // 保持原组件名，只改标题
        taskId = taskId,
        onNavigateBack = {
            if (navController.currentBackStackEntry != null) {
                navController.popBackStack()
            }
        }
    )
}
```

### 13. TaskListScreen 更新

更新任务卡片的点击和侧滑行为：

```kotlin
// 在 TaskListScreen 中
SwipeableTaskCard(
    task = task,
    onClick = {
        // 点击卡片 → 预览页面
        onTaskClick(task.id)  // 这将导航到 TaskPreview
    },
    onEdit = {
        // 侧滑编辑 → 编辑页面
        navController.navigate(Screen.TaskEdit.createRoute(task.id))
    },
    // ... 其他参数
)
```


### 14. 工具函数

```kotlin
// 相对时间格式化
fun getRelativeTimeString(dateTime: LocalDateTime): String {
    val now = LocalDateTime.now()
    val duration = Duration.between(now, dateTime)
    
    return when {
        duration.isNegative -> {
            val absDuration = duration.abs()
            when {
                absDuration.toDays() > 0 -> "已逾期${absDuration.toDays()}天"
                absDuration.toHours() > 0 -> "已逾期${absDuration.toHours()}小时"
                else -> "已逾期"
            }
        }
        duration.toDays() == 0L -> "今天"
        duration.toDays() == 1L -> "明天"
        duration.toDays() < 7 -> "还有${duration.toDays()}天"
        duration.toDays() < 30 -> "还有${duration.toDays() / 7}周"
        else -> "还有${duration.toDays() / 30}个月"
    }
}

// 重复规则格式化
fun formatRecurrenceRule(rule: RecurrenceRule): String {
    return when (rule.pattern) {
        RecurrencePattern.DAILY -> if (rule.interval == 1) "每天" else "每${rule.interval}天"
        RecurrencePattern.WEEKLY -> if (rule.interval == 1) "每周" else "每${rule.interval}周"
        RecurrencePattern.MONTHLY -> if (rule.interval == 1) "每月" else "每${rule.interval}月"
        RecurrencePattern.YEARLY -> if (rule.interval == 1) "每年" else "每${rule.interval}年"
    }
}

// 日期格式化
fun formatDate(date: LocalDateTime): String {
    return date.format(DateTimeFormatter.ofPattern("MM月dd日"))
}
```

## 动画和过渡

### 页面进入动画

```kotlin
// 在 NavHost 中配置页面过渡动画
composable(
    route = Screen.TaskPreview.route,
    arguments = listOf(
        navArgument("taskId") { type = NavType.LongType }
    ),
    enterTransition = {
        fadeIn(animationSpec = tween(300)) +
        slideInVertically(
            initialOffsetY = { it / 10 },
            animationSpec = tween(300)
        )
    },
    exitTransition = {
        fadeOut(animationSpec = tween(300))
    }
) { /* ... */ }
```

### 完成状态切换动画

```kotlin
// 在 FAB 点击时使用动画
AnimatedContent(
    targetState = task.isCompleted,
    transitionSpec = {
        scaleIn(initialScale = 0.8f) + fadeIn() with
        scaleOut(targetScale = 0.8f) + fadeOut()
    }
) { isCompleted ->
    Icon(
        if (isCompleted) Icons.Default.CheckCircle
        else Icons.Default.RadioButtonUnchecked,
        contentDescription = null
    )
}
```

## 响应式布局

### 窗口尺寸检测

```kotlin
@Composable
fun rememberWindowSize(): WindowSize {
    val configuration = LocalConfiguration.current
    return when {
        configuration.screenWidthDp < 600 -> WindowSize.Compact
        configuration.screenWidthDp < 840 -> WindowSize.Medium
        else -> WindowSize.Expanded
    }
}

enum class WindowSize {
    Compact,  // 手机
    Medium,   // 平板竖屏
    Expanded  // 平板横屏
}
```

### 响应式 TaskPreviewScreen

```kotlin
@Composable
fun TaskPreviewScreen(
    taskId: Long,
    viewModel: TaskPreviewViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val windowSize = rememberWindowSize()
    
    when (windowSize) {
        WindowSize.Compact -> {
            // 手机：全屏显示
            TaskPreviewScreenContent(
                taskId = taskId,
                viewModel = viewModel,
                onNavigateBack = onNavigateBack,
                onNavigateToEdit = onNavigateToEdit,
                modifier = modifier
            )
        }
        WindowSize.Medium, WindowSize.Expanded -> {
            // 平板：对话框模式
            Dialog(
                onDismissRequest = onNavigateBack,
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .fillMaxHeight(0.9f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    TaskPreviewScreenContent(
                        taskId = taskId,
                        viewModel = viewModel,
                        onNavigateBack = onNavigateBack,
                        onNavigateToEdit = onNavigateToEdit
                    )
                }
            }
        }
    }
}
```

## 无障碍支持

### 语义标签

```kotlin
// 为所有交互元素添加语义标签
Icon(
    Icons.Default.Edit,
    contentDescription = stringResource(R.string.cd_edit_task),
    modifier = Modifier.semantics {
        role = Role.Button
        contentDescription = "编辑任务"
    }
)

// 为状态信息添加语义描述
Text(
    text = task.title,
    modifier = Modifier.semantics {
        stateDescription = if (task.isCompleted) "已完成" else "未完成"
    }
)
```

### 触摸目标尺寸

```kotlin
// 确保所有按钮至少 48dp
IconButton(
    onClick = { /* ... */ },
    modifier = Modifier.size(48.dp)
) {
    Icon(/* ... */)
}
```

## 性能优化

### 状态管理优化

```kotlin
// 使用 derivedStateOf 避免不必要的重组
val completionRate by remember {
    derivedStateOf {
        val completed = subtasks.count { it.isCompleted }
        val total = subtasks.size
        if (total > 0) completed.toFloat() / total else 0f
    }
}
```

### 图片加载优化

```kotlin
// 配置 Coil 缓存策略
AsyncImage(
    model = ImageRequest.Builder(LocalContext.current)
        .data(attachment.uri)
        .crossfade(true)
        .size(200) // 限制缩略图尺寸
        .build(),
    contentDescription = attachment.name,
    modifier = Modifier.fillMaxSize(),
    contentScale = ContentScale.Crop,
    placeholder = painterResource(R.drawable.ic_image_placeholder),
    error = painterResource(R.drawable.ic_image_error)
)
```

## 国际化支持

### 字符串资源

在 `strings.xml` 中添加：

```xml
<!-- 任务预览页面 -->
<string name="task_preview_title">任务详情</string>
<string name="task_edit_title">任务编辑</string>
<string name="cd_edit_task">编辑任务</string>
<string name="cd_navigate_back">返回</string>
<string name="cd_more_actions">更多操作</string>
<string name="action_mark_complete">标记完成</string>
<string name="action_mark_incomplete">标记未完成</string>
<string name="action_share">分享</string>
<string name="action_go_back">返回</string>
<string name="error_task_not_found">任务不存在</string>
<string name="dialog_delete_task_title">删除任务</string>
<string name="dialog_delete_task_message">确定要删除这个任务吗？此操作无法撤销。</string>
<string name="task_description">描述</string>
<string name="task_tags">标签</string>
<string name="task_subtasks">子任务</string>
<string name="task_attachments">附件</string>
<string name="task_created_at">创建于 %s</string>
<string name="task_updated_at">最后修改 %s</string>
<string name="recurrence_next_occurrence">下次: %s</string>
<string name="priority_urgent">紧急</string>
<string name="priority_high">高</string>
<string name="priority_medium">中</string>
<string name="priority_low">低</string>
```

## 测试策略

### UI 测试

```kotlin
@Test
fun taskPreviewScreen_displaysTaskInformation() {
    // 准备测试数据
    val testTask = Task(
        id = 1L,
        title = "测试任务",
        description = "这是测试描述",
        priority = Priority.HIGH,
        dueDate = LocalDateTime.now().plusDays(3),
        isCompleted = false
    )
    
    // 启动屏幕
    composeTestRule.setContent {
        TaskPreviewScreen(
            taskId = 1L,
            onNavigateBack = {},
            onNavigateToEdit = {}
        )
    }
    
    // 验证任务信息显示
    composeTestRule.onNodeWithText("测试任务").assertIsDisplayed()
    composeTestRule.onNodeWithText("这是测试描述").assertIsDisplayed()
    composeTestRule.onNodeWithText("高").assertIsDisplayed()
}

@Test
fun taskPreviewScreen_toggleCompletion() {
    composeTestRule.setContent {
        TaskPreviewScreen(
            taskId = 1L,
            onNavigateBack = {},
            onNavigateToEdit = {}
        )
    }
    
    // 点击完成按钮
    composeTestRule.onNodeWithText("标记完成").performClick()
    
    // 验证状态变化
    composeTestRule.onNodeWithText("标记未完成").assertIsDisplayed()
}

@Test
fun taskPreviewScreen_navigateToEdit() {
    var navigatedToEdit = false
    
    composeTestRule.setContent {
        TaskPreviewScreen(
            taskId = 1L,
            onNavigateBack = {},
            onNavigateToEdit = { navigatedToEdit = true }
        )
    }
    
    // 点击编辑按钮
    composeTestRule.onNodeWithContentDescription("编辑任务").performClick()
    
    // 验证导航
    assert(navigatedToEdit)
}
```

### 单元测试

```kotlin
@Test
fun taskPreviewViewModel_loadTask_success() = runTest {
    // 准备测试数据
    val testTask = Task(id = 1L, title = "测试任务")
    val taskRepository = FakeTaskRepository().apply {
        addTask(testTask)
    }
    
    val viewModel = TaskPreviewViewModel(taskRepository, SavedStateHandle())
    
    // 加载任务
    viewModel.loadTask(1L)
    
    // 验证状态
    assertEquals(testTask, viewModel.task.value)
    assertEquals(TaskPreviewUiState.Success, viewModel.uiState.value)
}

@Test
fun taskPreviewViewModel_toggleCompletion() = runTest {
    val testTask = Task(id = 1L, title = "测试任务", isCompleted = false)
    val taskRepository = FakeTaskRepository().apply {
        addTask(testTask)
    }
    
    val viewModel = TaskPreviewViewModel(taskRepository, SavedStateHandle())
    viewModel.loadTask(1L)
    
    // 切换完成状态
    viewModel.toggleCompletion()
    
    // 验证状态变化
    assertTrue(viewModel.task.value?.isCompleted == true)
}
```

## 错误处理

### 加载错误

```kotlin
when (uiState) {
    is TaskPreviewUiState.Loading -> {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
    is TaskPreviewUiState.Error -> {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.Error,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = (uiState as TaskPreviewUiState.Error).message,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { viewModel.loadTask(taskId) }) {
                Text(stringResource(R.string.action_retry))
            }
        }
    }
    is TaskPreviewUiState.Success -> {
        // 显示任务内容
    }
}
```

## 总结

本设计文档详细说明了任务预览详情页面的实现方案，包括：

1. **清晰的架构**：分离预览和编辑功能，使用独立的 ViewModel 和 Screen
2. **优雅的 UI**：采用 Material Design 3 设计规范，只读视图展示所有任务信息
3. **流畅的导航**：点击卡片进入预览，侧滑编辑进入编辑页面
4. **完整的功能**：支持查看所有任务信息、快速切换完成状态、导航到编辑页面
5. **响应式设计**：支持不同屏幕尺寸，平板使用对话框模式
6. **无障碍支持**：完整的语义标签和触摸目标尺寸
7. **性能优化**：使用 derivedStateOf、图片缓存等优化技术
8. **国际化支持**：完整的字符串资源和相对时间格式化
9. **测试覆盖**：UI 测试和单元测试确保功能正确性

这个设计将为用户提供一个清晰、直观、高效的任务预览体验，同时保持与现有编辑功能的良好分离。
