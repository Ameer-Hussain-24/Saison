package takagi.ru.saison.ui.screens.settings.webdav

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import takagi.ru.saison.domain.model.backup.BackupFile
import takagi.ru.saison.domain.model.backup.BackupPreferences
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebDavBackupSettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: WebDavBackupViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    
    // 显示错误或成功消息
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Long
            )
            viewModel.clearError()
        }
    }
    
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Short
            )
            viewModel.clearSuccessMessage()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("WebDAV 备份") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "返回")
                    }
                }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (!uiState.isConfigured || uiState.isEditingConfig) {
                WebDavConfigurationCard(
                    initialUrl = uiState.config?.serverUrl ?: "",
                    initialUsername = uiState.config?.username ?: "",
                    onConfigure = { url, username, password ->
                        viewModel.configure(url, username, password)
                    },
                    onTestConnection = { viewModel.testConnection() },
                    onCancel = if (uiState.isEditingConfig) {
                        { viewModel.cancelEditConfig() }
                    } else null,
                    isTestingConnection = uiState.isTestingConnection,
                    connectionTestResult = uiState.connectionTestResult
                )
            } else {
                WebDavConfigSummaryCard(
                    config = uiState.config,
                    onEdit = { viewModel.editConfig() },
                    onClear = { viewModel.clearConfig() }
                )
                
                AutoBackupCard(
                    isEnabled = uiState.isAutoBackupEnabled,
                    lastBackupTime = uiState.lastBackupTime,
                    onToggle = { enabled -> viewModel.toggleAutoBackup(enabled, context) },
                    onBackupNow = { viewModel.createBackup() },
                    isCreatingBackup = uiState.isCreatingBackup
                )
                
                SelectiveBackupCard(
                    preferences = uiState.backupPreferences,
                    onPreferencesChange = { viewModel.updateBackupPreferences(it) }
                )
                
                BackupListCard(
                    backupList = uiState.backupList,
                    isLoading = uiState.isLoadingBackups,
                    onRefresh = { viewModel.loadBackupList() },
                    onCreateBackup = { viewModel.createBackup() },
                    onRestoreBackup = { viewModel.restoreBackup(it) },
                    onDeleteBackup = { viewModel.deleteBackup(it) },
                    isCreatingBackup = uiState.isCreatingBackup,
                    isRestoringBackup = uiState.isRestoringBackup,
                    isDeletingBackup = uiState.isDeletingBackup
                )
            }
        }
    }
}

@Composable
fun WebDavConfigurationCard(
    initialUrl: String = "",
    initialUsername: String = "",
    onConfigure: (String, String, String) -> Unit,
    onTestConnection: () -> Unit,
    onCancel: (() -> Unit)? = null,
    isTestingConnection: Boolean,
    connectionTestResult: ConnectionTestResult?
) {
    var url by remember(initialUrl) { mutableStateOf(initialUrl) }
    var username by remember(initialUsername) { mutableStateOf(initialUsername) }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("配置 WebDAV", style = MaterialTheme.typography.titleMedium)
            
            OutlinedTextField(
                value = url,
                onValueChange = { url = it },
                label = { Text("服务器地址") },
                placeholder = { Text("https://example.com/webdav") },
                modifier = Modifier.fillMaxWidth()
            )
            
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("用户名") },
                modifier = Modifier.fillMaxWidth()
            )
            
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("密码") },
                placeholder = { Text(if (initialUrl.isNotEmpty()) "留空则保持原密码不变" else "") },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            "切换密码可见性"
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
            
            connectionTestResult?.let { result ->
                when (result) {
                    is ConnectionTestResult.Success -> {
                        Text("✓ 连接成功", color = MaterialTheme.colorScheme.primary)
                    }
                    is ConnectionTestResult.Failure -> {
                        Text("✗ ${result.message}", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (onCancel != null) {
                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("取消")
                    }
                }
                
                OutlinedButton(
                    onClick = onTestConnection,
                    enabled = !isTestingConnection && url.isNotBlank() && username.isNotBlank() && password.isNotBlank(),
                    modifier = Modifier.weight(1f)
                ) {
                    if (isTestingConnection) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp))
                    } else {
                        Text("测试连接")
                    }
                }
                
                Button(
                    onClick = { onConfigure(url, username, password) },
                    enabled = url.isNotBlank() && username.isNotBlank() && (password.isNotBlank() || initialUrl.isNotEmpty()),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("保存配置")
                }
            }
        }
    }
}

@Composable
fun WebDavConfigSummaryCard(
    config: takagi.ru.saison.domain.model.backup.WebDavConfig?,
    onEdit: () -> Unit,
    onClear: () -> Unit
) {
    var showClearDialog by remember { mutableStateOf(false) }
    
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("WebDAV 配置", style = MaterialTheme.typography.titleMedium)
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, "编辑")
                    }
                    IconButton(onClick = { showClearDialog = true }) {
                        Icon(Icons.Default.Delete, "清除")
                    }
                }
            }
            
            config?.let {
                Text("服务器: ${it.serverUrl}", style = MaterialTheme.typography.bodyMedium)
                Text("用户名: ${it.username}", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
    
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("清除配置") },
            text = { Text("确定要清除 WebDAV 配置吗？") },
            confirmButton = {
                TextButton(onClick = {
                    onClear()
                    showClearDialog = false
                }) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
fun AutoBackupCard(
    isEnabled: Boolean,
    lastBackupTime: Long,
    onToggle: (Boolean) -> Unit,
    onBackupNow: () -> Unit,
    isCreatingBackup: Boolean
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("自动备份", style = MaterialTheme.typography.titleMedium)
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("启用自动备份")
                Switch(checked = isEnabled, onCheckedChange = onToggle)
            }
            
            if (lastBackupTime > 0) {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                Text(
                    "上次备份: ${dateFormat.format(Date(lastBackupTime))}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            Button(
                onClick = onBackupNow,
                enabled = !isCreatingBackup,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isCreatingBackup) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                }
                Text("立即备份")
            }
        }
    }
}

@Composable
fun SelectiveBackupCard(
    preferences: BackupPreferences,
    onPreferencesChange: (BackupPreferences) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("备份内容", style = MaterialTheme.typography.titleMedium)
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "收起" else "展开"
                )
            }
            
            if (isExpanded) {
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                
                BackupToggleItem("任务", preferences.includeTasks) {
                    onPreferencesChange(preferences.copy(includeTasks = it))
                }
                BackupToggleItem("课程", preferences.includeCourses) {
                    onPreferencesChange(preferences.copy(includeCourses = it))
                }
                BackupToggleItem("事件", preferences.includeEvents) {
                    onPreferencesChange(preferences.copy(includeEvents = it))
                }
                BackupToggleItem("日程", preferences.includeRoutines) {
                    onPreferencesChange(preferences.copy(includeRoutines = it))
                }
                BackupToggleItem("订阅", preferences.includeSubscriptions) {
                    onPreferencesChange(preferences.copy(includeSubscriptions = it))
                }
                BackupToggleItem("买断", preferences.includeValueDays) {
                    onPreferencesChange(preferences.copy(includeValueDays = it))
                }
                BackupToggleItem("番茄钟", preferences.includePomodoroSessions) {
                    onPreferencesChange(preferences.copy(includePomodoroSessions = it))
                }
                BackupToggleItem("学期", preferences.includeSemesters) {
                    onPreferencesChange(preferences.copy(includeSemesters = it))
                }
                BackupToggleItem("偏好设置", preferences.includePreferences) {
                    onPreferencesChange(preferences.copy(includePreferences = it))
                }
            }
        }
    }
}

@Composable
fun BackupToggleItem(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun BackupListCard(
    backupList: List<BackupFile>,
    isLoading: Boolean,
    onRefresh: () -> Unit,
    onCreateBackup: () -> Unit,
    onRestoreBackup: (BackupFile) -> Unit,
    onDeleteBackup: (BackupFile) -> Unit,
    isCreatingBackup: Boolean,
    isRestoringBackup: Boolean,
    isDeletingBackup: Boolean
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("备份列表", style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = onRefresh, enabled = !isLoading) {
                    Icon(Icons.Default.Refresh, "刷新")
                }
            }
            
            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (backupList.isEmpty()) {
                Text("暂无备份", style = MaterialTheme.typography.bodyMedium)
            } else {
                backupList.forEach { backup ->
                    BackupItem(
                        backup = backup,
                        onRestore = { onRestoreBackup(backup) },
                        onDelete = { onDeleteBackup(backup) },
                        isRestoring = isRestoringBackup,
                        isDeleting = isDeletingBackup
                    )
                }
            }
        }
    }
}

@Composable
fun BackupItem(
    backup: BackupFile,
    onRestore: () -> Unit,
    onDelete: () -> Unit,
    isRestoring: Boolean,
    isDeleting: Boolean
) {
    var showRestoreDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(backup.name, style = MaterialTheme.typography.bodyMedium)
            
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            Text(
                "日期: ${dateFormat.format(backup.modified)}",
                style = MaterialTheme.typography.bodySmall
            )
            
            Text(
                "大小: ${formatFileSize(backup.size)}",
                style = MaterialTheme.typography.bodySmall
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { showRestoreDialog = true },
                    enabled = !isRestoring && !isDeleting,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("恢复")
                }
                
                OutlinedButton(
                    onClick = { showDeleteDialog = true },
                    enabled = !isRestoring && !isDeleting,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("删除")
                }
            }
        }
    }
    
    if (showRestoreDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreDialog = false },
            title = { Text("恢复备份") },
            text = { Text("确定要恢复此备份吗？重复的数据将被跳过。") },
            confirmButton = {
                TextButton(onClick = {
                    onRestore()
                    showRestoreDialog = false
                }) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
    
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("删除备份") },
            text = { Text("确定要删除此备份吗？此操作无法撤销。") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete()
                    showDeleteDialog = false
                }) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}

private fun formatFileSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        else -> "${bytes / (1024 * 1024)} MB"
    }
}
