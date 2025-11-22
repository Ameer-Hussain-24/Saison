package takagi.ru.saison.ui.screens.semester

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import takagi.ru.saison.domain.model.Semester
import takagi.ru.saison.ui.components.SemesterCard
import takagi.ru.saison.ui.components.SemesterEditDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SemesterListScreen(
    viewModel: SemesterViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val semesters by viewModel.allSemesters.collectAsState()
    val stats by viewModel.semesterStats.collectAsState()
    val currentSemester by viewModel.currentSemester.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    
    var showCreateDialog by remember { mutableStateOf(false) }
    var semesterToEdit by remember { mutableStateOf<Semester?>(null) }
    var semesterToDelete by remember { mutableStateOf<Semester?>(null) }
    var semesterToCopy by remember { mutableStateOf<Semester?>(null) }
    
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
        when (uiState) {
            is SemesterUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .fillMaxSize()
                            .wrapContentSize()
                    )
                }
            }
            is SemesterUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    Text(
                        text = (uiState as SemesterUiState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .fillMaxSize()
                            .wrapContentSize()
                            .padding(16.dp)
                    )
                }
            }
            is SemesterUiState.Success -> {
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
                    
                    val activeSemesters = semesters.filter { !it.isArchived }
                    if (activeSemesters.isEmpty()) {
                        item {
                            Text(
                                text = "暂无活跃学期",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    } else {
                        items(activeSemesters) { semester ->
                            SemesterCard(
                                semester = semester,
                                stats = stats[semester.id],
                                isActive = semester.id == currentSemester?.id,
                                onSelect = { viewModel.switchSemester(semester.id) },
                                onEdit = { semesterToEdit = semester },
                                onDelete = { semesterToDelete = semester },
                                onArchive = { viewModel.toggleArchive(semester.id, true) },
                                onCopy = { semesterToCopy = semester }
                            )
                        }
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
                                onArchive = { viewModel.toggleArchive(semester.id, false) },
                                onCopy = { semesterToCopy = semester }
                            )
                        }
                    }
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
    
    // 复制学期对话框
    semesterToCopy?.let { semester ->
        CopySemesterDialog(
            sourceSemester = semester,
            onDismiss = { semesterToCopy = null },
            onConfirm = { newName ->
                viewModel.copySemester(semester.id, newName)
                semesterToCopy = null
            }
        )
    }
}

@Composable
private fun DeleteSemesterDialog(
    semester: Semester,
    courseCount: Int,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = { Text("删除学期") },
        text = {
            Column(modifier = Modifier.wrapContentHeight()) {
                Text("确定要删除学期 \"${semester.name}\" 吗？")
                Spacer(modifier = Modifier.height(8.dp))
                if (courseCount > 0) {
                    Text(
                        text = "警告：该学期包含 $courseCount 门课程，删除学期将同时删除所有课程！",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("删除")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
private fun CopySemesterDialog(
    sourceSemester: Semester,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var newName by remember { mutableStateOf("${sourceSemester.name} - 副本") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("复制学期") },
        text = {
            Column(modifier = Modifier.wrapContentHeight()) {
                Text("将复制学期 \"${sourceSemester.name}\" 的所有课程到新学期。")
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("新学期名称") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(newName) },
                enabled = newName.isNotBlank()
            ) {
                Text("复制")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
