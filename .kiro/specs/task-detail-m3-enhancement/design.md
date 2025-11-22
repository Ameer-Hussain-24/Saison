# 任务详情与添加页面 Material 3 增强设计文档

## 概述

本设计文档详细说明如何将Saison应用的任务详情页面和快速添加任务功能优化为符合Material 3设计规范的优雅、直观、功能完善的界面。设计重点关注信息架构、交互流畅性、视觉美感和用户体验。

## 架构

### 组件层次结构

```
TaskDetailScreen (任务详情主屏幕)
├── TopAppBar (顶部栏)
│   ├── BackButton (返回按钮)
│   ├── SaveButton (保存按钮)
│   └── MoreActionsMenu (更多操作菜单)
├── ScrollableContent (可滚动内容)
│   ├── CompletionCheckbox (完成状态复选框)
│   ├── TitleSection (标题区域)
│   │   └── LargeTextField (大号文本框)
│   ├── DescriptionSection (描述区域)
│   │   └── OutlinedTextField (多行文本框)
│   ├── PrioritySection (优先级区域)
│   │   └── SegmentedButton (分段按钮)
│   ├── DateTimeSection (日期时间区域)
│   │   ├── DatePickerCard (日期选择卡片)
│   │   └── TimePickerCard (时间选择卡片)
│   ├── RecurrenceSection (重复规则区域)
│   │   └── RecurrenceSelector (重复选择器)
│   ├── LocationSection (位置区域)
│   │   └── LocationTextField (位置输入框)
│   ├── TagsSection (标签区域)
│   │   └── TagChipGroup (标签芯片组)
│   ├── SubtasksSection (子任务区域)
│   │   ├── SubtaskList (子任务列表)
│   │   └── AddSubtaskField (添加子任务输入框)
│   └── AttachmentsSection (附件区域)
│       ├── AttachmentGrid (附件网格)
│       └── AddAttachmentButton (添加附件按钮)

AddTaskSheet (快速添加任务底部表单)
├── DragHandle (拖动手柄)
├── ItemTypeSelector (项目类型选择器)
│   ├── TaskTypeButton (任务类型按钮)
│   ├── MilestoneTypeButton (里程碑类型按钮 - 预留)
│   └── CountdownTypeButton (倒计时类型按钮 - 预留)
├── SheetHeader (表单头部)
├── QuickInputField (快速输入框)
│   └── TemplateButton (模板按钮)
├── QuickActionsRow (快捷操作行)
│   ├── DateQuickButton (日期快捷按钮)
│   ├── PriorityQuickButton (优先级快捷按钮)
│   └── ExpandButton (展开按钮)
└── ActionButtons (操作按钮)
    ├── CancelButton (取消按钮)
    └── AddButton (添加按钮)
```


### 数据流

```
TaskDetailViewModel
    ↓
StateFlow<Task?>
StateFlow<TaskDetailUiState>
    ↓
TaskDetailScreen
    ↓
User Interactions
    ↓
ViewModel Actions (updateTask, deleteTask, saveTask)
    ↓
Repository Layer
    ↓
Local Database / Remote Sync
```

## 组件设计

### 1. TaskDetailScreen 整体布局

#### 视觉设计

**布局结构：**
```
┌─────────────────────────────────────────────┐
│ ← 任务详情                    💾 ⋮          │ TopAppBar
├─────────────────────────────────────────────┤
│                                             │
│ ☐ 任务标题 (大号字体)                       │ Title Section
│                                             │
│ ┌─────────────────────────────────────────┐ │
│ │ 描述文本区域                            │ │ Description
│ │ (多行输入)                              │ │
│ └─────────────────────────────────────────┘ │
│                                             │
│ 优先级                                      │
│ [低] [中] [高] [紧急]                       │ Priority
│                                             │
│ ┌─────────────────────────────────────────┐ │
│ │ 📅 截止日期                             │ │
│ │    2024年10月31日 下午3:00             │ │ Date/Time
│ └─────────────────────────────────────────┘ │
│                                             │
│ ┌─────────────────────────────────────────┐ │
│ │ 🔁 重复                                 │ │
│ │    每周一                               │ │ Recurrence
│ └─────────────────────────────────────────┘ │
│                                             │
│ 📍 位置                                     │ Location
│ ┌─────────────────────────────────────────┐ │
│ │ 办公室                                  │ │
│ └─────────────────────────────────────────┘ │
│                                             │
│ 🏷️ 标签                                     │
│ [工作] [重要] [+]                           │ Tags
│                                             │
│ ☑️ 子任务 (2/5)                             │
│ ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━  │ Progress
│ ☑ 子任务1                                   │
│ ☐ 子任务2                                   │ Subtasks
│ + 添加子任务                                │
│                                             │
│ 📎 附件 (3)                                 │
│ [图片1] [文档1] [图片2]                     │ Attachments
│                                             │
└─────────────────────────────────────────────┘
```

**尺寸规范：**
- 内容区域内边距：16dp
- 区域间距：24dp
- 卡片圆角：12dp
- 输入框圆角：8dp

**颜色方案：**
- 背景：`surface`
- 卡片背景：`surfaceVariant`
- 输入框边框：`outline`
- 强调色：`primary`


### 2. 标题和描述编辑区域

#### 标题输入框设计

```kotlin
@Composable
fun TitleSection(
    title: String,
    onTitleChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    TextField(
        value = title,
        onValueChange = onTitleChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { 
            Text(
                text = stringResource(R.string.task_title_placeholder),
                style = MaterialTheme.typography.headlineSmall
            ) 
        },
        textStyle = MaterialTheme.typography.headlineSmall.copy(
            fontWeight = FontWeight.Bold
        ),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        maxLines = 3
    )
}
```

**特点：**
- 无边框设计，融入背景
- 使用 headlineSmall 字体样式
- 支持最多3行显示
- 占位符提示用户输入

#### 描述输入框设计

```kotlin
@Composable
fun DescriptionSection(
    description: String,
    onDescriptionChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = description,
        onValueChange = onDescriptionChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(stringResource(R.string.task_description_label)) },
        placeholder = { Text(stringResource(R.string.task_description_placeholder)) },
        minLines = 3,
        maxLines = 8,
        supportingText = {
            Text(
                text = "${description.length} / 500",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End
            )
        }
    )
}
```

**特点：**
- 带边框的多行输入框
- 显示字符计数器
- 最少3行，最多8行
- 支持文本和占位符

### 3. 日期时间选择器

#### 日期选择卡片

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTimeSection(
    dueDate: LocalDateTime?,
    onDateTimeChange: (LocalDateTime?) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 快捷日期选项
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            QuickDateChip(
                label = stringResource(R.string.date_today),
                onClick = { onDateTimeChange(LocalDateTime.now()) }
            )
            QuickDateChip(
                label = stringResource(R.string.date_tomorrow),
                onClick = { onDateTimeChange(LocalDateTime.now().plusDays(1)) }
            )
            QuickDateChip(
                label = stringResource(R.string.date_next_week),
                onClick = { onDateTimeChange(LocalDateTime.now().plusWeeks(1)) }
            )
        }
        
        // 日期时间显示卡片
        OutlinedCard(
            onClick = { showDatePicker = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.task_due_date),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = dueDate?.let { formatDateTime(it) } 
                            ?: stringResource(R.string.date_not_set),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    if (dueDate != null) {
                        Text(
                            text = getRelativeTimeString(dueDate),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (dueDate != null) {
                        IconButton(onClick = { onDateTimeChange(null) }) {
                            Icon(
                                Icons.Default.Clear,
                                contentDescription = stringResource(R.string.cd_clear_date)
                            )
                        }
                    }
                    Icon(
                        Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
    
    // DatePicker Dialog
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = { 
                    showDatePicker = false
                    showTimePicker = true
                }) {
                    Text(stringResource(R.string.action_next))
                }
            }
        ) {
            DatePicker(state = rememberDatePickerState())
        }
    }
    
    // TimePicker Dialog
    if (showTimePicker) {
        TimePickerDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text(stringResource(R.string.action_confirm))
                }
            }
        ) {
            TimePicker(state = rememberTimePickerState())
        }
    }
}
```

**特点：**
- 快捷日期按钮（今天、明天、下周）
- 显示相对时间提示（"3天后"）
- 支持清除日期
- Material 3 DatePicker 和 TimePicker
- 两步选择流程（先日期后时间）


### 4. 优先级选择器

已有 `PrioritySegmentedButton` 组件，需要优化样式：

```kotlin
@Composable
fun PrioritySection(
    priority: Priority,
    onPriorityChange: (Priority) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.task_priority),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium
        )
        
        PrioritySegmentedButton(
            selectedPriority = priority,
            onPrioritySelected = onPriorityChange,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
```

**优先级颜色映射：**
- LOW: `Color(0xFF4CAF50)` - 绿色
- MEDIUM: `Color(0xFF2196F3)` - 蓝色
- HIGH: `Color(0xFFFF9800)` - 橙色
- URGENT: `Color(0xFFF44336)` - 红色

### 5. 子任务管理

#### 子任务列表设计

```kotlin
@Composable
fun SubtasksSection(
    subtasks: List<Subtask>,
    onSubtaskToggle: (Long, Boolean) -> Unit,
    onSubtaskAdd: (String) -> Unit,
    onSubtaskDelete: (Long) -> Unit,
    onSubtaskReorder: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var newSubtaskText by remember { mutableStateOf("") }
    val completedCount = subtasks.count { it.isCompleted }
    val totalCount = subtasks.size
    val progress = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f
    
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 标题和进度
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.task_subtasks),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
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
        LazyColumn(
            modifier = Modifier.heightIn(max = 300.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(
                items = subtasks,
                key = { it.id }
            ) { subtask ->
                SubtaskItem(
                    subtask = subtask,
                    onToggle = { onSubtaskToggle(subtask.id, !subtask.isCompleted) },
                    onDelete = { onSubtaskDelete(subtask.id) },
                    modifier = Modifier.animateItemPlacement()
                )
            }
        }
        
        // 添加子任务输入框
        OutlinedTextField(
            value = newSubtaskText,
            onValueChange = { newSubtaskText = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(stringResource(R.string.subtask_add_placeholder)) },
            leadingIcon = {
                Icon(Icons.Default.Add, contentDescription = null)
            },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    if (newSubtaskText.isNotBlank()) {
                        onSubtaskAdd(newSubtaskText)
                        newSubtaskText = ""
                    }
                }
            ),
            singleLine = true
        )
    }
}

@Composable
fun SubtaskItem(
    subtask: Subtask,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = subtask.isCompleted,
            onCheckedChange = { onToggle() }
        )
        
        Text(
            text = subtask.title,
            modifier = Modifier.weight(1f),
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
        
        IconButton(
            onClick = { showDeleteDialog = true },
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = stringResource(R.string.cd_delete_subtask),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
```

**特点：**
- 显示完成进度条
- 支持回车键快速添加
- 子任务项带复选框和删除按钮
- 已完成子任务显示删除线
- 列表项动画


### 6. 附件管理

```kotlin
@Composable
fun AttachmentsSection(
    attachments: List<Attachment>,
    onAttachmentAdd: (Uri) -> Unit,
    onAttachmentDelete: (Long) -> Unit,
    onAttachmentClick: (Attachment) -> Unit,
    modifier: Modifier = Modifier
) {
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { onAttachmentAdd(it) }
    }
    
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.task_attachments),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${attachments.size}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        // 附件网格
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.heightIn(max = 400.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(attachments) { attachment ->
                AttachmentCard(
                    attachment = attachment,
                    onClick = { onAttachmentClick(attachment) },
                    onDelete = { onAttachmentDelete(attachment.id) }
                )
            }
            
            // 添加附件按钮
            item {
                AddAttachmentCard(
                    onClick = { imagePickerLauncher.launch("image/*") }
                )
            }
        }
    }
}

@Composable
fun AttachmentCard(
    attachment: Attachment,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {
        // 缩略图
        AsyncImage(
            model = attachment.uri,
            contentDescription = attachment.name,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        
        // 删除按钮
        IconButton(
            onClick = onDelete,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
                .size(24.dp)
                .background(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                    shape = CircleShape
                )
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = stringResource(R.string.cd_delete_attachment),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun AddAttachmentCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        onClick = onClick,
        modifier = modifier.aspectRatio(1f),
        border = BorderStroke(
            width = 2.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = stringResource(R.string.cd_add_attachment),
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
```

**特点：**
- 3列网格布局
- 图片缩略图预览
- 删除按钮覆盖在右上角
- 添加按钮卡片
- 使用 Coil 异步加载图片

### 7. 标签管理

```kotlin
@Composable
fun TagsSection(
    selectedTags: List<Tag>,
    availableTags: List<Tag>,
    onTagAdd: (Tag) -> Unit,
    onTagRemove: (Tag) -> Unit,
    onCreateTag: (String, Color) -> Unit,
    modifier: Modifier = Modifier
) {
    var showTagSelector by remember { mutableStateOf(false) }
    
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(R.string.task_tags),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium
        )
        
        // 已选标签
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            selectedTags.forEach { tag ->
                FilterChip(
                    selected = true,
                    onClick = { onTagRemove(tag) },
                    label = { Text(tag.name) },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = tag.color.copy(alpha = 0.3f),
                        selectedLabelColor = tag.color
                    )
                )
            }
            
            // 添加标签按钮
            FilterChip(
                selected = false,
                onClick = { showTagSelector = true },
                label = { Text(stringResource(R.string.tag_add)) },
                leadingIcon = {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            )
        }
    }
    
    // 标签选择器对话框
    if (showTagSelector) {
        TagSelectorDialog(
            availableTags = availableTags,
            selectedTags = selectedTags,
            onTagSelect = onTagAdd,
            onCreateTag = onCreateTag,
            onDismiss = { showTagSelector = false }
        )
    }
}
```

**特点：**
- FlowRow 自动换行布局
- FilterChip 显示标签
- 标签带颜色标识
- 点击标签移除
- 标签选择器对话框


### 8. 重复任务设置

```kotlin
@Composable
fun RecurrenceSection(
    recurrenceRule: RecurrenceRule?,
    onRecurrenceChange: (RecurrenceRule?) -> Unit,
    modifier: Modifier = Modifier
) {
    var showRecurrenceDialog by remember { mutableStateOf(false) }
    
    OutlinedCard(
        onClick = { showRecurrenceDialog = true },
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = stringResource(R.string.task_recurrence),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = recurrenceRule?.let { formatRecurrenceRule(it) }
                        ?: stringResource(R.string.recurrence_none),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                if (recurrenceRule != null) {
                    Text(
                        text = stringResource(
                            R.string.recurrence_next_occurrence,
                            formatDate(recurrenceRule.getNextOccurrence())
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (recurrenceRule != null) {
                    IconButton(onClick = { onRecurrenceChange(null) }) {
                        Icon(
                            Icons.Default.Clear,
                            contentDescription = stringResource(R.string.cd_clear_recurrence)
                        )
                    }
                }
                Icon(
                    Icons.Default.Repeat,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
    
    if (showRecurrenceDialog) {
        RecurrenceDialog(
            currentRule = recurrenceRule,
            onConfirm = { rule ->
                onRecurrenceChange(rule)
                showRecurrenceDialog = false
            },
            onDismiss = { showRecurrenceDialog = false }
        )
    }
}

@Composable
fun RecurrenceDialog(
    currentRule: RecurrenceRule?,
    onConfirm: (RecurrenceRule) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedPattern by remember { 
        mutableStateOf(currentRule?.pattern ?: RecurrencePattern.DAILY) 
    }
    var interval by remember { mutableStateOf(currentRule?.interval ?: 1) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.recurrence_dialog_title)) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 重复模式选择
                Text(
                    text = stringResource(R.string.recurrence_pattern),
                    style = MaterialTheme.typography.labelMedium
                )
                
                RecurrencePattern.values().forEach { pattern ->
                    FilterChip(
                        selected = selectedPattern == pattern,
                        onClick = { selectedPattern = pattern },
                        label = { Text(pattern.displayName) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                // 间隔设置
                Text(
                    text = stringResource(R.string.recurrence_interval),
                    style = MaterialTheme.typography.labelMedium
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = { if (interval > 1) interval-- }
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = null)
                    }
                    
                    Text(
                        text = interval.toString(),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                    
                    IconButton(
                        onClick = { interval++ }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(RecurrenceRule(pattern = selectedPattern, interval = interval))
                }
            ) {
                Text(stringResource(R.string.action_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    )
}
```

**特点：**
- 显示重复规则摘要
- 显示下次重复时间
- 支持清除重复规则
- 对话框选择重复模式和间隔


### 9. 快速添加任务底部表单

```kotlin
enum class ItemType(val displayName: String) {
    TASK("任务"),
    MILESTONE("里程碑"),
    COUNTDOWN("倒计时")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskSheet(
    onDismiss: () -> Unit,
    onTaskAdd: (Task) -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false
    )
    var taskInput by remember { mutableStateOf("") }
    var isExpanded by remember { mutableStateOf(false) }
    var selectedPriority by remember { mutableStateOf(Priority.MEDIUM) }
    var selectedDate by remember { mutableStateOf<LocalDateTime?>(null) }
    var selectedItemType by remember { mutableStateOf(ItemType.TASK) }
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 项目类型选择器
            ItemTypeSelector(
                selectedType = selectedItemType,
                onTypeSelected = { selectedItemType = it },
                modifier = Modifier.fillMaxWidth()
            )
            
            // 标题
            Text(
                text = when (selectedItemType) {
                    ItemType.TASK -> stringResource(R.string.add_task_title)
                    ItemType.MILESTONE -> stringResource(R.string.add_milestone_title)
                    ItemType.COUNTDOWN -> stringResource(R.string.add_countdown_title)
                },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            // 输入框
            OutlinedTextField(
                value = taskInput,
                onValueChange = { 
                    taskInput = it
                    // 自动解析自然语言
                    parseNaturalLanguage(it)?.let { parsed ->
                        selectedDate = parsed.date
                        selectedPriority = parsed.priority
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { 
                    Text(stringResource(R.string.add_task_placeholder)) 
                },
                trailingIcon = {
                    // 模板按钮
                    IconButton(onClick = { /* 显示模板选择器 */ }) {
                        Icon(
                            Icons.Default.Description,
                            contentDescription = stringResource(R.string.cd_templates)
                        )
                    }
                },
                minLines = if (isExpanded) 3 else 1,
                maxLines = if (isExpanded) 5 else 3
            )
            
            // 快捷操作行
            AnimatedVisibility(visible = taskInput.isNotBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 日期快捷按钮
                    FilterChip(
                        selected = selectedDate != null,
                        onClick = { /* 显示日期选择器 */ },
                        label = { 
                            Text(
                                selectedDate?.let { formatShortDate(it) }
                                    ?: stringResource(R.string.date_add)
                            ) 
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.CalendarToday,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                    
                    // 优先级快捷按钮
                    FilterChip(
                        selected = selectedPriority != Priority.MEDIUM,
                        onClick = { /* 循环切换优先级 */ },
                        label = { Text(selectedPriority.displayName) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Flag,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = selectedPriority.color
                            )
                        }
                    )
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    // 展开按钮
                    IconButton(
                        onClick = { isExpanded = !isExpanded }
                    ) {
                        Icon(
                            if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = stringResource(R.string.cd_expand)
                        )
                    }
                }
            }
            
            // 展开的详细选项
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 描述输入
                    OutlinedTextField(
                        value = "",
                        onValueChange = { },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.task_description_label)) },
                        minLines = 2,
                        maxLines = 4
                    )
                    
                    // 位置输入
                    OutlinedTextField(
                        value = "",
                        onValueChange = { },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.task_location_label)) },
                        leadingIcon = {
                            Icon(Icons.Default.LocationOn, contentDescription = null)
                        },
                        singleLine = true
                    )
                }
            }
            
            // 操作按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.action_cancel))
                }
                
                Button(
                    onClick = {
                        if (taskInput.isNotBlank()) {
                            onTaskAdd(
                                Task(
                                    title = taskInput,
                                    priority = selectedPriority,
                                    dueDate = selectedDate
                                )
                            )
                            onDismiss()
                        }
                    },
                    enabled = taskInput.isNotBlank()
                ) {
                    Text(stringResource(R.string.action_add))
                }
            }
        }
    }
}
```

**特点：**
- ModalBottomSheet 容器
- 项目类型选择器（任务/里程碑/倒计时）
- 支持展开/收起
- 模板按钮
- 快捷日期和优先级选择
- 自然语言解析
- 展开后显示更多选项

### 10. 项目类型选择器

```kotlin
@Composable
fun ItemTypeSelector(
    selectedType: ItemType,
    onTypeSelected: (ItemType) -> Unit,
    modifier: Modifier = Modifier
) {
    SingleChoiceSegmentedButtonRow(
        modifier = modifier
    ) {
        ItemType.values().forEachIndexed { index, type ->
            SegmentedButton(
                selected = selectedType == type,
                onClick = { onTypeSelected(type) },
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = ItemType.values().size
                ),
                icon = {
                    SegmentedButtonDefaults.Icon(active = selectedType == type) {
                        Icon(
                            imageVector = when (type) {
                                ItemType.TASK -> Icons.Default.CheckCircle
                                ItemType.MILESTONE -> Icons.Default.Flag
                                ItemType.COUNTDOWN -> Icons.Default.Timer
                            },
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                },
                enabled = type == ItemType.TASK // 暂时只启用任务类型
            ) {
                Text(
                    text = type.displayName,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}
```

**特点：**
- 使用 SingleChoiceSegmentedButtonRow 实现单选
- 三个选项：任务、里程碑、倒计时
- 每个选项带图标
- 暂时只启用"任务"类型，其他两个禁用（为未来功能预留）
- 选中状态使用 filled 样式

### 11. 自然语言解析

```kotlin
object NaturalLanguageParser {
    fun parse(input: String): ParsedTask? {
        var title = input
        var date: LocalDateTime? = null
        var priority = Priority.MEDIUM
        
        // 日期关键词
        val datePatterns = mapOf(
            "今天|今日" to { LocalDateTime.now() },
            "明天|明日" to { LocalDateTime.now().plusDays(1) },
            "后天" to { LocalDateTime.now().plusDays(2) },
            "下周一" to { getNextWeekday(DayOfWeek.MONDAY) },
            "下周二" to { getNextWeekday(DayOfWeek.TUESDAY) },
            "下周三" to { getNextWeekday(DayOfWeek.WEDNESDAY) },
            "下周四" to { getNextWeekday(DayOfWeek.THURSDAY) },
            "下周五" to { getNextWeekday(DayOfWeek.FRIDAY) },
            "下周六" to { getNextWeekday(DayOfWeek.SATURDAY) },
            "下周日|下周天" to { getNextWeekday(DayOfWeek.SUNDAY) }
        )
        
        datePatterns.forEach { (pattern, dateProvider) ->
            if (input.contains(Regex(pattern))) {
                date = dateProvider()
                title = title.replace(Regex(pattern), "").trim()
            }
        }
        
        // 时间关键词
        val timePatterns = mapOf(
            "上午" to 9,
            "中午" to 12,
            "下午" to 15,
            "晚上" to 19
        )
        
        timePatterns.forEach { (pattern, hour) ->
            if (input.contains(pattern)) {
                date = (date ?: LocalDateTime.now()).withHour(hour).withMinute(0)
                title = title.replace(pattern, "").trim()
            }
        }
        
        // 优先级关键词
        val priorityPatterns = mapOf(
            "紧急|urgent" to Priority.URGENT,
            "重要|important" to Priority.HIGH,
            "普通|normal" to Priority.MEDIUM,
            "低优先级|low" to Priority.LOW
        )
        
        priorityPatterns.forEach { (pattern, p) ->
            if (input.contains(Regex(pattern, RegexOption.IGNORE_CASE))) {
                priority = p
                title = title.replace(Regex(pattern, RegexOption.IGNORE_CASE), "").trim()
            }
        }
        
        return if (title.isNotBlank()) {
            ParsedTask(title = title, date = date, priority = priority)
        } else {
            null
        }
    }
    
    private fun getNextWeekday(dayOfWeek: DayOfWeek): LocalDateTime {
        val now = LocalDateTime.now()
        val daysUntil = (dayOfWeek.value - now.dayOfWeek.value + 7) % 7
        return now.plusDays(daysUntil.toLong())
    }
}

data class ParsedTask(
    val title: String,
    val date: LocalDateTime?,
    val priority: Priority
)
```

**支持的自然语言模式：**
- 日期：今天、明天、后天、下周一至下周日
- 时间：上午、中午、下午、晚上
- 优先级：紧急、重要、普通、低优先级

**示例：**
- "明天下午开会" → 标题："开会"，日期：明天15:00，优先级：中
- "紧急 今天完成报告" → 标题："完成报告"，日期：今天，优先级：紧急


## 数据模型扩展

### Task 模型更新

```kotlin
data class Task(
    val id: Long = 0,
    val title: String,
    val description: String? = null,
    val priority: Priority = Priority.MEDIUM,
    val dueDate: LocalDateTime? = null,
    val isCompleted: Boolean = false,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    val location: String? = null,
    val tags: List<Tag> = emptyList(),
    val subtasks: List<Subtask> = emptyList(),
    val attachments: List<Attachment> = emptyList(),
    val recurrenceRule: RecurrenceRule? = null,
    val isFavorite: Boolean = false
)

data class Tag(
    val id: Long = 0,
    val name: String,
    val color: Color
)

data class Subtask(
    val id: Long = 0,
    val title: String,
    val isCompleted: Boolean = false,
    val order: Int = 0
)

data class Attachment(
    val id: Long = 0,
    val name: String,
    val uri: String,
    val type: AttachmentType,
    val size: Long,
    val createdAt: LocalDateTime = LocalDateTime.now()
)

enum class AttachmentType {
    IMAGE, DOCUMENT, AUDIO, VIDEO, OTHER
}

data class RecurrenceRule(
    val pattern: RecurrencePattern,
    val interval: Int = 1,
    val endDate: LocalDateTime? = null
) {
    fun getNextOccurrence(from: LocalDateTime = LocalDateTime.now()): LocalDateTime {
        return when (pattern) {
            RecurrencePattern.DAILY -> from.plusDays(interval.toLong())
            RecurrencePattern.WEEKLY -> from.plusWeeks(interval.toLong())
            RecurrencePattern.MONTHLY -> from.plusMonths(interval.toLong())
            RecurrencePattern.YEARLY -> from.plusYears(interval.toLong())
        }
    }
}

enum class RecurrencePattern(val displayName: String) {
    DAILY("每天"),
    WEEKLY("每周"),
    MONTHLY("每月"),
    YEARLY("每年")
}
```

### ViewModel 状态管理

```kotlin
data class TaskDetailUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val hasUnsavedChanges: Boolean = false,
    val error: String? = null,
    val showDatePicker: Boolean = false,
    val showTimePicker: Boolean = false,
    val showRecurrenceDialog: Boolean = false,
    val showTagSelector: Boolean = false,
    val availableTags: List<Tag> = emptyList(),
    val templates: List<TaskTemplate> = emptyList()
)

sealed class TaskDetailUiState {
    object Loading : TaskDetailUiState()
    data class Success(val task: Task) : TaskDetailUiState()
    data class Error(val message: String) : TaskDetailUiState()
}

class TaskDetailViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val tagRepository: TagRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    private val taskId: Long = savedStateHandle["taskId"] ?: 0L
    
    private val _task = MutableStateFlow<Task?>(null)
    val task: StateFlow<Task?> = _task.asStateFlow()
    
    private val _uiState = MutableStateFlow(TaskDetailUiState())
    val uiState: StateFlow<TaskDetailUiState> = _uiState.asStateFlow()
    
    private val _hasUnsavedChanges = MutableStateFlow(false)
    val hasUnsavedChanges: StateFlow<Boolean> = _hasUnsavedChanges.asStateFlow()
    
    init {
        loadTask(taskId)
        loadAvailableTags()
    }
    
    fun loadTask(id: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                taskRepository.getTaskById(id).collect { task ->
                    _task.value = task
                    _uiState.update { it.copy(isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message
                    ) 
                }
            }
        }
    }
    
    fun updateTask(updatedTask: Task) {
        viewModelScope.launch {
            _task.value = updatedTask
            _hasUnsavedChanges.value = true
            // 自动保存（防抖）
            delay(1000)
            saveTask()
        }
    }
    
    fun saveTask() {
        viewModelScope.launch {
            _task.value?.let { task ->
                _uiState.update { it.copy(isSaving = true) }
                try {
                    taskRepository.updateTask(task)
                    _hasUnsavedChanges.value = false
                    _uiState.update { it.copy(isSaving = false) }
                } catch (e: Exception) {
                    _uiState.update { 
                        it.copy(
                            isSaving = false,
                            error = e.message
                        ) 
                    }
                }
            }
        }
    }
    
    fun deleteTask() {
        viewModelScope.launch {
            _task.value?.let { task ->
                taskRepository.deleteTask(task)
            }
        }
    }
    
    fun addSubtask(title: String) {
        _task.value?.let { task ->
            val newSubtask = Subtask(
                id = System.currentTimeMillis(),
                title = title,
                order = task.subtasks.size
            )
            updateTask(task.copy(subtasks = task.subtasks + newSubtask))
        }
    }
    
    fun addAttachment(uri: Uri) {
        viewModelScope.launch {
            _task.value?.let { task ->
                val attachment = Attachment(
                    id = System.currentTimeMillis(),
                    name = getFileName(uri),
                    uri = uri.toString(),
                    type = getAttachmentType(uri),
                    size = getFileSize(uri)
                )
                updateTask(task.copy(attachments = task.attachments + attachment))
            }
        }
    }
    
    private fun loadAvailableTags() {
        viewModelScope.launch {
            tagRepository.getAllTags().collect { tags ->
                _uiState.update { it.copy(availableTags = tags) }
            }
        }
    }
}
```

## 动画和微交互

### 页面进入动画

```kotlin
@Composable
fun TaskDetailScreen(
    taskId: Long,
    onNavigateBack: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        visible = true
    }
    
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(300)) +
                slideInVertically(
                    initialOffsetY = { it / 4 },
                    animationSpec = tween(300)
                )
    ) {
        TaskDetailContent(...)
    }
}
```

### 字段焦点动画

```kotlin
@Composable
fun AnimatedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.02f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )
    
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .scale(scale)
            .onFocusChanged { isFocused = it.isFocused }
    )
}
```

### 保存成功动画

```kotlin
@Composable
fun SaveSuccessIndicator(
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = scaleIn(animationSpec = spring()) + fadeIn(),
        exit = scaleOut(animationSpec = spring()) + fadeOut()
    ) {
        Row(
            modifier = modifier
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Check,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = stringResource(R.string.save_success),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}
```


## 错误处理

### 验证错误

```kotlin
data class ValidationResult(
    val isValid: Boolean,
    val errorMessage: String? = null
)

object TaskValidator {
    fun validateTitle(title: String): ValidationResult {
        return when {
            title.isBlank() -> ValidationResult(
                isValid = false,
                errorMessage = "标题不能为空"
            )
            title.length > 200 -> ValidationResult(
                isValid = false,
                errorMessage = "标题不能超过200个字符"
            )
            else -> ValidationResult(isValid = true)
        }
    }
    
    fun validateDueDate(dueDate: LocalDateTime?): ValidationResult {
        return when {
            dueDate != null && dueDate.isBefore(LocalDateTime.now()) -> ValidationResult(
                isValid = false,
                errorMessage = "截止日期不能早于当前时间"
            )
            else -> ValidationResult(isValid = true)
        }
    }
    
    fun validateAttachmentSize(size: Long): ValidationResult {
        val maxSize = 10 * 1024 * 1024 // 10MB
        return when {
            size > maxSize -> ValidationResult(
                isValid = false,
                errorMessage = "附件大小不能超过10MB"
            )
            else -> ValidationResult(isValid = true)
        }
    }
}
```

### 错误提示 UI

```kotlin
@Composable
fun ErrorSnackbar(
    message: String,
    onRetry: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    Snackbar(
        modifier = Modifier.padding(16.dp),
        action = {
            if (onRetry != null) {
                TextButton(onClick = onRetry) {
                    Text(stringResource(R.string.action_retry))
                }
            }
        },
        dismissAction = {
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = null)
            }
        },
        containerColor = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer
    ) {
        Text(message)
    }
}
```

### 未保存更改提示

```kotlin
@Composable
fun UnsavedChangesDialog(
    onSave: () -> Unit,
    onDiscard: () -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        icon = {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = { Text(stringResource(R.string.unsaved_changes_title)) },
        text = { Text(stringResource(R.string.unsaved_changes_message)) },
        confirmButton = {
            Button(onClick = onSave) {
                Text(stringResource(R.string.action_save))
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onDiscard) {
                    Text(stringResource(R.string.action_discard))
                }
                TextButton(onClick = onCancel) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        }
    )
}
```

## 响应式布局

### 窗口尺寸检测

```kotlin
enum class WindowSize {
    COMPACT,    // < 600dp
    MEDIUM,     // 600-840dp
    EXPANDED    // > 840dp
}

@Composable
fun rememberWindowSize(): WindowSize {
    val configuration = LocalConfiguration.current
    return when {
        configuration.screenWidthDp < 600 -> WindowSize.COMPACT
        configuration.screenWidthDp < 840 -> WindowSize.MEDIUM
        else -> WindowSize.EXPANDED
    }
}
```

### 响应式任务详情布局

```kotlin
@Composable
fun ResponsiveTaskDetailScreen(
    taskId: Long,
    onNavigateBack: () -> Unit
) {
    val windowSize = rememberWindowSize()
    
    when (windowSize) {
        WindowSize.COMPACT -> {
            // 单列布局
            TaskDetailScreen(
                taskId = taskId,
                onNavigateBack = onNavigateBack
            )
        }
        WindowSize.MEDIUM -> {
            // 双列布局
            Row(modifier = Modifier.fillMaxSize()) {
                TaskDetailScreen(
                    taskId = taskId,
                    onNavigateBack = onNavigateBack,
                    modifier = Modifier.weight(1f)
                )
                TaskPreviewPanel(
                    taskId = taskId,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        WindowSize.EXPANDED -> {
            // 三列布局或对话框模式
            TaskDetailDialog(
                taskId = taskId,
                onDismiss = onNavigateBack
            )
        }
    }
}
```

### 平板对话框模式

```kotlin
@Composable
fun TaskDetailDialog(
    taskId: Long,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .fillMaxHeight(0.9f),
            shape = RoundedCornerShape(28.dp),
            tonalElevation = 6.dp
        ) {
            TaskDetailContent(
                taskId = taskId,
                onNavigateBack = onDismiss
            )
        }
    }
}
```

## 无障碍支持

### 语义标签

```kotlin
@Composable
fun AccessibleTaskDetailScreen(
    task: Task,
    onTaskUpdate: (Task) -> Unit
) {
    Column(
        modifier = Modifier.semantics {
            contentDescription = "任务详情页面"
            heading()
        }
    ) {
        // 标题输入
        TextField(
            value = task.title,
            onValueChange = { onTaskUpdate(task.copy(title = it)) },
            modifier = Modifier.semantics {
                contentDescription = "任务标题输入框"
                stateDescription = if (task.title.isBlank()) "空" else "已填写"
            }
        )
        
        // 完成复选框
        Checkbox(
            checked = task.isCompleted,
            onCheckedChange = { onTaskUpdate(task.copy(isCompleted = it)) },
            modifier = Modifier.semantics {
                contentDescription = if (task.isCompleted) 
                    "任务已完成，点击标记为未完成" 
                else 
                    "任务未完成，点击标记为已完成"
                role = Role.Checkbox
            }
        )
        
        // 优先级选择
        PrioritySegmentedButton(
            selectedPriority = task.priority,
            onPrioritySelected = { onTaskUpdate(task.copy(priority = it)) },
            modifier = Modifier.semantics {
                contentDescription = "优先级选择器，当前选择：${task.priority.displayName}"
                role = Role.RadioButton
            }
        )
    }
}
```

### 键盘导航

```kotlin
@Composable
fun KeyboardNavigableTaskDetail(
    task: Task,
    onTaskUpdate: (Task) -> Unit
) {
    val focusManager = LocalFocusManager.current
    
    Column {
        // 标题输入 - Tab 1
        TextField(
            value = task.title,
            onValueChange = { onTaskUpdate(task.copy(title = it)) },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            )
        )
        
        // 描述输入 - Tab 2
        TextField(
            value = task.description ?: "",
            onValueChange = { onTaskUpdate(task.copy(description = it)) },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            )
        )
        
        // 其他可聚焦元素...
    }
}
```

## 性能优化

### LazyColumn 优化

```kotlin
@Composable
fun OptimizedSubtaskList(
    subtasks: List<Subtask>,
    onSubtaskToggle: (Long, Boolean) -> Unit
) {
    LazyColumn(
        modifier = Modifier.heightIn(max = 300.dp)
    ) {
        items(
            items = subtasks,
            key = { it.id },  // 稳定的 key
            contentType = { "subtask" }  // 内容类型提示
        ) { subtask ->
            SubtaskItem(
                subtask = subtask,
                onToggle = { onSubtaskToggle(subtask.id, !subtask.isCompleted) },
                modifier = Modifier.animateItemPlacement()
            )
        }
    }
}
```

### 图片加载优化

```kotlin
@Composable
fun OptimizedAttachmentImage(
    attachment: Attachment,
    modifier: Modifier = Modifier
) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(attachment.uri)
            .crossfade(true)
            .memoryCacheKey(attachment.uri)
            .diskCacheKey(attachment.uri)
            .size(200, 200)  // 缩略图尺寸
            .build(),
        contentDescription = attachment.name,
        modifier = modifier,
        contentScale = ContentScale.Crop,
        placeholder = painterResource(R.drawable.ic_image_placeholder),
        error = painterResource(R.drawable.ic_image_error)
    )
}
```

### 状态管理优化

```kotlin
@Composable
fun OptimizedTaskDetailContent(
    task: Task,
    onTaskUpdate: (Task) -> Unit
) {
    // 使用 derivedStateOf 避免不必要的重组
    val completedSubtasksCount by remember {
        derivedStateOf {
            task.subtasks.count { it.isCompleted }
        }
    }
    
    val hasAttachments by remember {
        derivedStateOf {
            task.attachments.isNotEmpty()
        }
    }
    
    // 使用 remember 缓存计算结果
    val formattedDueDate = remember(task.dueDate) {
        task.dueDate?.let { formatDateTime(it) }
    }
    
    // UI 组件...
}
```

## 测试策略

### UI 测试

```kotlin
@Test
fun testTaskDetailScreen_displaysTaskInformation() {
    val testTask = Task(
        id = 1,
        title = "测试任务",
        description = "这是一个测试任务",
        priority = Priority.HIGH
    )
    
    composeTestRule.setContent {
        TaskDetailScreen(
            taskId = testTask.id,
            onNavigateBack = {}
        )
    }
    
    composeTestRule
        .onNodeWithText("测试任务")
        .assertIsDisplayed()
    
    composeTestRule
        .onNodeWithText("这是一个测试任务")
        .assertIsDisplayed()
}

@Test
fun testAddTaskSheet_createsTaskWithNaturalLanguage() {
    var createdTask: Task? = null
    
    composeTestRule.setContent {
        AddTaskSheet(
            onDismiss = {},
            onTaskAdd = { createdTask = it }
        )
    }
    
    composeTestRule
        .onNodeWithText("快速添加任务")
        .assertIsDisplayed()
    
    composeTestRule
        .onNode(hasSetTextAction())
        .performTextInput("明天下午开会")
    
    composeTestRule
        .onNodeWithText("添加")
        .performClick()
    
    assertNotNull(createdTask)
    assertEquals("开会", createdTask?.title)
    assertNotNull(createdTask?.dueDate)
}
```

### 单元测试

```kotlin
@Test
fun testNaturalLanguageParser_parsesDateCorrectly() {
    val result = NaturalLanguageParser.parse("明天下午开会")
    
    assertNotNull(result)
    assertEquals("开会", result?.title)
    assertNotNull(result?.date)
    assertEquals(15, result?.date?.hour)
}

@Test
fun testTaskValidator_validatesTitleCorrectly() {
    val emptyTitleResult = TaskValidator.validateTitle("")
    assertFalse(emptyTitleResult.isValid)
    
    val validTitleResult = TaskValidator.validateTitle("有效标题")
    assertTrue(validTitleResult.isValid)
    
    val longTitleResult = TaskValidator.validateTitle("a".repeat(201))
    assertFalse(longTitleResult.isValid)
}
```

## 国际化

### 字符串资源

```xml
<!-- strings.xml -->
<resources>
    <!-- Task Detail -->
    <string name="task_detail_title">任务详情</string>
    <string name="task_title_placeholder">输入任务标题...</string>
    <string name="task_description_label">描述</string>
    <string name="task_description_placeholder">添加任务描述...</string>
    <string name="task_priority">优先级</string>
    <string name="task_due_date">截止日期</string>
    <string name="task_recurrence">重复</string>
    <string name="task_location_label">位置</string>
    <string name="task_tags">标签</string>
    <string name="task_subtasks">子任务</string>
    <string name="task_attachments">附件</string>
    
    <!-- Add Task -->
    <string name="add_task_title">快速添加任务</string>
    <string name="add_task_placeholder">例如：明天下午3点开会</string>
    
    <!-- Actions -->
    <string name="action_save">保存</string>
    <string name="action_cancel">取消</string>
    <string name="action_add">添加</string>
    <string name="action_confirm">确认</string>
    <string name="action_discard">放弃</string>
    <string name="action_retry">重试</string>
    
    <!-- Dates -->
    <string name="date_today">今天</string>
    <string name="date_tomorrow">明天</string>
    <string name="date_next_week">下周</string>
    <string name="date_not_set">未设置</string>
    
    <!-- Messages -->
    <string name="save_success">已保存</string>
    <string name="unsaved_changes_title">未保存的更改</string>
    <string name="unsaved_changes_message">您有未保存的更改，是否保存？</string>
</resources>
```

## 总结

本设计文档提供了任务详情和快速添加任务功能的完整Material 3优化方案，重点包括：

1. **信息架构优化**：清晰的区域划分、合理的信息层次
2. **交互体验提升**：项目类型选择、自然语言解析、快捷操作
3. **视觉设计精致**：Material 3组件、流畅动画、优雅配色
4. **功能完善**：子任务、附件、标签、重复任务等全面支持
5. **扩展性设计**：为里程碑和倒计时功能预留接口
6. **响应式布局**：适配不同屏幕尺寸
7. **无障碍友好**：完善的语义标签和键盘导航
8. **性能优化**：高效的状态管理和图片加载

所有设计决策都基于Material 3设计规范，确保与应用整体风格保持一致。

