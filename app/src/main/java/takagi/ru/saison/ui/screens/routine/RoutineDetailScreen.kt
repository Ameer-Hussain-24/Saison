package takagi.ru.saison.ui.screens.routine

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import takagi.ru.saison.domain.model.routine.CheckInRecord
import takagi.ru.saison.domain.model.routine.CycleType
import takagi.ru.saison.ui.components.CheckInButton
import takagi.ru.saison.util.CycleCalculator
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/**
 * 日程任务详情页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineDetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Long) -> Unit,
    viewModel: RoutineDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showEditSheet by remember { mutableStateOf(false) }
    
    // 显示错误消息
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }
    
    // 显示成功消息
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearSuccessMessage()
        }
    }
    
    // 任务删除后返回
    LaunchedEffect(uiState.taskDeleted) {
        if (uiState.taskDeleted) {
            onNavigateBack()
        }
    }
    
    var showDeleteDialog by remember { mutableStateOf(false) }
    var deleteCheckInId by remember { mutableStateOf<Long?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("任务详情") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    // 编辑按钮
                    IconButton(
                        onClick = { showEditSheet = true }
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "编辑")
                    }
                    // 删除按钮
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "删除")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            uiState.task?.let { task ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 任务信息卡片
                    item {
                        TaskInfoCard(
                            task = task,
                            currentCycleCount = uiState.currentCycleCount,
                            isInActiveCycle = uiState.isInActiveCycle,
                            currentCycle = uiState.currentCycle,
                            onCheckIn = { viewModel.checkIn() }
                        )
                    }
                    
                    // 打卡历史标题
                    item {
                        Text(
                            text = "打卡历史",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    // 按周期分组显示打卡记录
                    if (uiState.groupedRecords.isEmpty()) {
                        item {
                            Text(
                                text = "暂无打卡记录",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 32.dp)
                            )
                        }
                    } else {
                        items(uiState.groupedRecords) { group ->
                            CycleGroupCard(
                                group = group,
                                onDeleteCheckIn = { checkInId ->
                                    deleteCheckInId = checkInId
                                }
                            )
                        }
                    }
                }
            }
        }
    }
    
    // 删除任务确认对话框
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("删除任务") },
            text = { Text("确定要删除这个任务吗？所有打卡记录也将被删除。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteTask()
                        showDeleteDialog = false
                    }
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
    
    // 删除打卡记录确认对话框
    deleteCheckInId?.let { checkInId ->
        AlertDialog(
            onDismissRequest = { deleteCheckInId = null },
            title = { Text("删除打卡记录") },
            text = { Text("确定要删除这条打卡记录吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteCheckIn(checkInId)
                        deleteCheckInId = null
                    }
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteCheckInId = null }) {
                    Text("取消")
                }
            }
        )
    }
    
    // 编辑任务底部表单
    if (showEditSheet && uiState.task != null) {
        takagi.ru.saison.ui.components.CreateRoutineSheet(
            task = uiState.task,
            onDismiss = { showEditSheet = false },
            onSave = { task ->
                viewModel.updateTask(task)
                showEditSheet = false
            }
        )
    }
}


/**
 * 任务信息卡片
 */
@Composable
private fun TaskInfoCard(
    task: takagi.ru.saison.domain.model.routine.RoutineTask,
    currentCycleCount: Int,
    isInActiveCycle: Boolean,
    currentCycle: Pair<java.time.LocalDate, java.time.LocalDate>?,
    onCheckIn: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isInActiveCycle) {
                MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isInActiveCycle) 2.dp else 0.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 标题和图标
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // 图标
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = if (isInActiveCycle) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                    
                    // 标题
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.headlineSmall,
                        color = if (isInActiveCycle) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
                
                // 当前周期打卡次数
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "$currentCycleCount",
                        style = MaterialTheme.typography.displayMedium,
                        color = if (isInActiveCycle && currentCycleCount > 0) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                    Text(
                        text = "次",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // 描述
            task.description?.let { description ->
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Divider()
            
            // 周期信息
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "周期类型",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = when (task.cycleType) {
                            CycleType.DAILY -> "每日"
                            CycleType.WEEKLY -> "每周"
                            CycleType.MONTHLY -> "每月"
                            CycleType.CUSTOM -> "自定义"
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                currentCycle?.let { (start, end) ->
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "当前周期",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${start.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT))} - ${end.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT))}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
            
            // 时长信息
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "活动时长",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = task.durationMinutes?.let { duration ->
                            if (duration > 0) {
                                takagi.ru.saison.util.DurationFormatter.formatDuration(duration)
                            } else {
                                "未设置"
                            }
                        } ?: "未设置",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            
            // 打卡按钮
            CheckInButton(
                enabled = isInActiveCycle,
                onClick = onCheckIn,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * 周期分组卡片
 */
@Composable
private fun CycleGroupCard(
    group: CycleGroup,
    onDeleteCheckIn: (Long) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
            // 周期标题
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${group.cycleStart.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))} - ${group.cycleEnd.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))}",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = "${group.count} 次",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Divider()
            
            // 打卡记录列表
            group.records.forEach { record ->
                CheckInRecordItem(
                    record = record,
                    onDelete = { onDeleteCheckIn(record.id) }
                )
            }
        }
    }
}

/**
 * 打卡记录项
 */
@Composable
private fun CheckInRecordItem(
    record: CheckInRecord,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = record.checkInTime.format(
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            record.note?.let { note ->
                Text(
                    text = note,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        
        IconButton(onClick = onDelete) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "删除",
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}
