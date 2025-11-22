# 多学期课表管理设计文档

## 概述

本文档描述多学期课表管理功能的技术设计，包括数据模型、架构设计、UI组件和实现细节。

## 架构设计

### 数据层

#### 1. Semester 数据模型

```kotlin
data class Semester(
    val id: Long = 0,
    val name: String,                    // 学期名称，如"2024秋季学期"
    val startDate: LocalDate,            // 学期开始日期
    val endDate: LocalDate,              // 学期结束日期
    val totalWeeks: Int = 18,            // 学期总周数
    val isArchived: Boolean = false,     // 是否归档
    val isDefault: Boolean = false,      // 是否为默认学期
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
```

#### 2. Course 数据模型扩展

```kotlin
data class Course(
    // ... 现有字段 ...
    val semesterId: Long,                // 所属学期ID（新增）
    // ... 其他字段 ...
)
```

#### 3. 数据库表设计

**semester 表：**
```sql
CREATE TABLE semester (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    name TEXT NOT NULL,
    start_date INTEGER NOT NULL,
    end_date INTEGER NOT NULL,
    total_weeks INTEGER NOT NULL DEFAULT 18,
    is_archived INTEGER NOT NULL DEFAULT 0,
    is_default INTEGER NOT NULL DEFAULT 0,
    created_at INTEGER NOT NULL,
    updated_at INTEGER NOT NULL
)
```

**course 表修改：**
```sql
ALTER TABLE course ADD COLUMN semester_id INTEGER NOT NULL DEFAULT 1 
    REFERENCES semester(id) ON DELETE CASCADE
```

### Repository 层

#### SemesterRepository

```kotlin
interface SemesterRepository {
    fun getAllSemesters(): Flow<List<Semester>>
    fun getActiveSemesters(): Flow<List<Semester>>
    fun getArchivedSemesters(): Flow<List<Semester>>
    fun getSemesterById(id: Long): Flow<Semester?>
    suspend fun insertSemester(semester: Semester): Long
    suspend fun updateSemester(semester: Semester)
    suspend fun deleteSemester(id: Long)
    suspend fun archiveSemester(id: Long)
    suspend fun unarchiveSemester(id: Long)
    suspend fun copySemester(semesterId: Long, newName: String): Long
}
```

#### CourseRepository 扩展

```kotlin
interface CourseRepository {
    // ... 现有方法 ...
    fun getCoursesBySemester(semesterId: Long): Flow<List<Course>>
    suspend fun getCourseCountBySemester(semesterId: Long): Int
    suspend fun moveCourseToSemester(courseId: Long, semesterId: Long)
    // ... 其他方法 ...
}
```

### ViewModel 层

#### SemesterViewModel

```kotlin
@HiltViewModel
class SemesterViewModel @Inject constructor(
    private val semesterRepository: SemesterRepository,
    private val courseRepository: CourseRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {
    
    // 所有学期
    val allSemesters: StateFlow<List<Semester>>
    
    // 活跃学期
    val activeSemesters: StateFlow<List<Semester>>
    
    // 当前选中的学期
    val currentSemester: StateFlow<Semester?>
    
    // 学期统计信息
    val semesterStats: StateFlow<Map<Long, SemesterStats>>
    
    // UI状态
    val uiState: StateFlow<SemesterUiState>
    
    // 创建学期
    fun createSemester(name: String, startDate: LocalDate, totalWeeks: Int)
    
    // 切换学期
    fun switchSemester(semesterId: Long)
    
    // 编辑学期
    fun updateSemester(semester: Semester)
    
    // 删除学期
    fun deleteSemester(semesterId: Long)
    
    // 归档/取消归档
    fun toggleArchive(semesterId: Long)
    
    // 复制学期
    fun copySemester(semesterId: Long, newName: String)
}

data class SemesterStats(
    val courseCount: Int,
    val totalHours: Int,
    val weekRange: String
)

sealed class SemesterUiState {
    object Loading : SemesterUiState()
    object Success : SemesterUiState()
    data class Error(val message: String) : SemesterUiState()
}
```

#### CourseViewModel 扩展

```kotlin
@HiltViewModel
class CourseViewModel @Inject constructor(
    // ... 现有依赖 ...
    private val semesterViewModel: SemesterViewModel
) : ViewModel() {
    
    // 当前学期的课程
    val currentSemesterCourses: StateFlow<List<Course>>
    
    // ... 其他现有方法 ...
}
```

## UI 组件设计

### 1. SemesterSelectorDropdown

课程表顶部的学期快速切换下拉菜单

```kotlin
@Composable
fun SemesterSelectorDropdown(
    currentSemester: Semester?,
    recentSemesters: List<Semester>,
    onSemesterSelected: (Long) -> Unit,
    onViewAllClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedCard(
            modifier = modifier.menuAnchor()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = currentSemester?.name ?: "未选择学期",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = currentSemester?.let { 
                            "${it.startDate.format(dateFormatter)} - ${it.endDate.format(dateFormatter)}"
                        } ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
            }
        }
        
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            recentSemesters.forEach { semester ->
                DropdownMenuItem(
                    text = { Text(semester.name) },
                    onClick = {
                        onSemesterSelected(semester.id)
                        expanded = false
                    },
                    leadingIcon = {
                        if (semester.id == currentSemester?.id) {
                            Icon(Icons.Default.Check, contentDescription = null)
                        }
                    }
                )
            }
            
            Divider()
            
            DropdownMenuItem(
                text = { Text("查看所有学期") },
                onClick = {
                    onViewAllClick()
                    expanded = false
                },
                leadingIcon = {
                    Icon(Icons.Default.List, contentDescription = null)
                }
            )
        }
    }
}
```

### 2. SemesterListScreen

学期列表管理界面

```kotlin
@Composable
fun SemesterListScreen(
    viewModel: SemesterViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val semesters by viewModel.allSemesters.collectAsState()
    val stats by viewModel.semesterStats.collectAsState()
    val currentSemester by viewModel.currentSemester.collectAsState()
    
    var showCreateDialog by remember { mutableStateOf(false) }
    var semesterToEdit by remember { mutableStateOf<Semester?>(null) }
    var semesterToDelete by remember { mutableStateOf<Semester?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("学期管理") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showCreateDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("创建学期") }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 活跃学期
            item {
                Text(
                    text = "活跃学期",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            items(semesters.filter { !it.isArchived }) { semester ->
                SemesterCard(
                    semester = semester,
                    stats = stats[semester.id],
                    isActive = semester.id == currentSemester?.id,
                    onSelect = { viewModel.switchSemester(semester.id) },
                    onEdit = { semesterToEdit = semester },
                    onDelete = { semesterToDelete = semester },
                    onArchive = { viewModel.toggleArchive(semester.id) },
                    onCopy = { /* 显示复制对话框 */ }
                )
            }
            
            // 归档学期
            val archivedSemesters = semesters.filter { it.isArchived }
            if (archivedSemesters.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "归档学期",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                items(archivedSemesters) { semester ->
                    SemesterCard(
                        semester = semester,
                        stats = stats[semester.id],
                        isActive = false,
                        isArchived = true,
                        onSelect = { viewModel.switchSemester(semester.id) },
                        onEdit = { semesterToEdit = semester },
                        onDelete = { semesterToDelete = semester },
                        onArchive = { viewModel.toggleArchive(semester.id) },
                        onCopy = { /* 显示复制对话框 */ }
                    )
                }
            }
        }
    }
    
    // 创建/编辑对话框
    if (showCreateDialog || semesterToEdit != null) {
        SemesterEditDialog(
            semester = semesterToEdit,
            onDismiss = {
                showCreateDialog = false
                semesterToEdit = null
            },
            onSave = { name, startDate, totalWeeks ->
                if (semesterToEdit != null) {
                    viewModel.updateSemester(
                        semesterToEdit!!.copy(
                            name = name,
                            startDate = startDate,
                            totalWeeks = totalWeeks
                        )
                    )
                } else {
                    viewModel.createSemester(name, startDate, totalWeeks)
                }
                showCreateDialog = false
                semesterToEdit = null
            }
        )
    }
    
    // 删除确认对话框
    semesterToDelete?.let { semester ->
        DeleteSemesterDialog(
            semester = semester,
            courseCount = stats[semester.id]?.courseCount ?: 0,
            onDismiss = { semesterToDelete = null },
            onConfirm = {
                viewModel.deleteSemester(semester.id)
                semesterToDelete = null
            }
        )
    }
}
```

### 3. SemesterCard

学期信息卡片

```kotlin
@Composable
fun SemesterCard(
    semester: Semester,
    stats: SemesterStats?,
    isActive: Boolean,
    isArchived: Boolean = false,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onArchive: () -> Unit,
    onCopy: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) {
                MaterialTheme.colorScheme.primaryContainer
            } else if (isArchived) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isActive) 4.dp else 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onSelect)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = semester.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        if (isActive) {
                            AssistChip(
                                onClick = {},
                                label = { Text("当前") },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    labelColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                        if (isArchived) {
                            AssistChip(
                                onClick = {},
                                label = { Text("已归档") }
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "${semester.startDate.format(dateFormatter)} - ${semester.endDate.format(dateFormatter)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // 更多操作菜单
                var showMenu by remember { mutableStateOf(false) }
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "更多")
                    }
                    
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("编辑") },
                            onClick = {
                                onEdit()
                                showMenu = false
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Edit, contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("复制") },
                            onClick = {
                                onCopy()
                                showMenu = false
                            },
                            leadingIcon = {
                                Icon(Icons.Default.ContentCopy, contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(if (isArchived) "取消归档" else "归档") },
                            onClick = {
                                onArchive()
                                showMenu = false
                            },
                            leadingIcon = {
                                Icon(
                                    if (isArchived) Icons.Default.Unarchive else Icons.Default.Archive,
                                    contentDescription = null
                                )
                            }
                        )
                        Divider()
                        DropdownMenuItem(
                            text = { Text("删除") },
                            onClick = {
                                onDelete()
                                showMenu = false
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            },
                            colors = MenuDefaults.itemColors(
                                textColor = MaterialTheme.colorScheme.error
                            )
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 统计信息
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatItem(
                    icon = Icons.Default.Class,
                    label = "课程",
                    value = "${stats?.courseCount ?: 0} 门"
                )
                StatItem(
                    icon = Icons.Default.CalendarToday,
                    label = "周数",
                    value = "${semester.totalWeeks} 周"
                )
                stats?.totalHours?.let { hours ->
                    StatItem(
                        icon = Icons.Default.Schedule,
                        label = "总学时",
                        value = "$hours 小时"
                    )
                }
            }
        }
    }
}

@Composable
private fun StatItem(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
```

### 4. SemesterEditDialog

学期创建/编辑对话框

```kotlin
@Composable
fun SemesterEditDialog(
    semester: Semester?,
    onDismiss: () -> Unit,
    onSave: (name: String, startDate: LocalDate, totalWeeks: Int) -> Unit
) {
    var name by remember { mutableStateOf(semester?.name ?: "") }
    var startDate by remember { mutableStateOf(semester?.startDate ?: LocalDate.now()) }
    var totalWeeks by remember { mutableStateOf(semester?.totalWeeks?.toFloat() ?: 18f) }
    var showDatePicker by remember { mutableStateOf(false) }
    
    val isEdit = semester != null
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEdit) "编辑学期" else "创建学期") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 学期名称
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("学期名称") },
                    placeholder = { Text("如：2025春季学期") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                // 开始日期
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
                                text = "开始日期",
                                style = MaterialTheme.typography.labelMedium
                            )
                            Text(
                                text = startDate.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日")),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        Icon(Icons.Default.CalendarToday, contentDescription = null)
                    }
                }
                
                // 总周数
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("学期总周数")
                        Text(
                            text = "${totalWeeks.toInt()} 周",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Slider(
                        value = totalWeeks,
                        onValueChange = { totalWeeks = it },
                        valueRange = 16f..24f,
                        steps = 7
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(name, startDate, totalWeeks.toInt())
                },
                enabled = name.isNotBlank()
            ) {
                Text(if (isEdit) "保存" else "创建")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
    
    // 日期选择器
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = startDate.toEpochDay() * 86400000L
        )
        
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            startDate = LocalDate.ofEpochDay(millis / 86400000L)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("取消")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
```

## 数据迁移策略

### 迁移步骤

1. **创建 semester 表**
2. **创建默认学期**
3. **为 course 表添加 semester_id 列**
4. **将所有现有课程关联到默认学期**
5. **添加外键约束**

### 迁移脚本

```kotlin
val MIGRATION_X_Y = object : Migration(X, Y) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // 1. 创建 semester 表
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS semester (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL,
                start_date INTEGER NOT NULL,
                end_date INTEGER NOT NULL,
                total_weeks INTEGER NOT NULL DEFAULT 18,
                is_archived INTEGER NOT NULL DEFAULT 0,
                is_default INTEGER NOT NULL DEFAULT 0,
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL
            )
        """)
        
        // 2. 创建默认学期
        val now = System.currentTimeMillis()
        val startDate = LocalDate.now().with(DayOfWeek.MONDAY).toEpochDay()
        val endDate = LocalDate.now().plusWeeks(18).toEpochDay()
        
        database.execSQL("""
            INSERT INTO semester (name, start_date, end_date, total_weeks, is_archived, is_default, created_at, updated_at)
            VALUES ('当前学期', $startDate, $endDate, 18, 0, 1, $now, $now)
        """)
        
        // 3. 为 course 表添加 semester_id 列
        database.execSQL("""
            ALTER TABLE course ADD COLUMN semester_id INTEGER NOT NULL DEFAULT 1
        """)
        
        // 4. 创建索引
        database.execSQL("""
            CREATE INDEX IF NOT EXISTS index_course_semester_id ON course(semester_id)
        """)
    }
}
```

## 性能优化

1. **懒加载学期数据** - 只在需要时加载学期列表
2. **缓存当前学期** - 使用 DataStore 缓存当前选中的学期ID
3. **索引优化** - 为 semester_id 创建索引加速查询
4. **批量操作** - 复制学期时使用事务批量插入课程
5. **分页加载** - 如果学期数量很多，考虑分页加载

## 错误处理

1. **学期名称重复** - 提示用户修改名称
2. **删除当前学期** - 禁止删除，提示先切换到其他学期
3. **删除最后一个学期** - 禁止删除，至少保留一个学期
4. **数据库错误** - 显示友好的错误提示并记录日志
5. **网络同步冲突** - 如果启用了 WebDAV 同步，需要处理学期数据的冲突

## 测试策略

1. **单元测试** - 测试 Repository 和 ViewModel 的业务逻辑
2. **UI测试** - 测试学期切换、创建、编辑、删除等操作
3. **数据迁移测试** - 测试从旧版本迁移到新版本的数据完整性
4. **性能测试** - 测试大量学期和课程数据的性能表现
