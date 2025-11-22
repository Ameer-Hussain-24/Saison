package takagi.ru.saison.ui.screens.task

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import takagi.ru.saison.R
import takagi.ru.saison.ui.components.*

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
                        description = currentTask.description!!,
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
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.error_task_not_found),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
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
