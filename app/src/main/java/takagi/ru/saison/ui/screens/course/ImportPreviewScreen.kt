package takagi.ru.saison.ui.screens.course

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import takagi.ru.saison.R
import takagi.ru.saison.domain.model.Course
import java.time.format.DateTimeFormatter

/**
 * 导入预览界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportPreviewScreen(
    uri: Uri,
    semesterId: Long,
    primaryColor: Color,
    onNavigateBack: () -> Unit,
    onImportSuccess: () -> Unit,
    viewModel: ImportPreviewViewModel = hiltViewModel(),
    courseViewModel: CourseViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val periods by courseViewModel.periods.collectAsState()
    
    LaunchedEffect(uri, periods) {
        android.util.Log.d("ImportPreviewScreen", "LaunchedEffect triggered: periods.size=${periods.size}")
        // 等待periods加载完成后再调用loadPreview
        // 如果periods为空，也调用loadPreview（使用空列表）
        viewModel.loadPreview(
            uri = uri,
            semesterId = semesterId,
            primaryColor = primaryColor,
            strategy = takagi.ru.saison.domain.model.TimeMatchingStrategy.AutoCreatePeriods,
            existingPeriods = periods
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.import_preview_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    when (uiState) {
                        is ImportPreviewState.Preview -> {
                            val state = uiState as ImportPreviewState.Preview
                            IconButton(
                                onClick = { viewModel.selectAll() },
                                enabled = state.selectedIndices.size < state.courses.size
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = "全选")
                            }
                            IconButton(
                                onClick = { viewModel.deselectAll() },
                                enabled = state.selectedIndices.isNotEmpty()
                            ) {
                                Icon(Icons.Default.Cancel, contentDescription = "取消全选")
                            }
                        }
                        else -> {}
                    }
                }
            )
        },
        bottomBar = {
            when (uiState) {
                is ImportPreviewState.Preview -> {
                    val state = uiState as ImportPreviewState.Preview
                    Surface(
                        tonalElevation = 3.dp,
                        shadowElevation = 8.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(
                                    R.string.import_selected_count,
                                    state.selectedIndices.size,
                                    state.courses.size
                                ),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            
                            Button(
                                onClick = { viewModel.confirmImport() },
                                enabled = state.selectedIndices.isNotEmpty()
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(stringResource(R.string.confirm_import))
                            }
                        }
                    }
                }
                else -> {}
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is ImportPreviewState.Idle -> {
                    // 空状态
                }
                is ImportPreviewState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is ImportPreviewState.Preview -> {
                    CoursePreviewList(
                        courses = state.courses,
                        selectedIndices = state.selectedIndices,
                        duplicateWarnings = state.duplicateWarnings,
                        suggestedSemesterStartDate = state.suggestedSemesterStartDate,
                        onToggleSelection = { index ->
                            viewModel.toggleCourseSelection(index)
                        }
                    )
                }
                is ImportPreviewState.Success -> {
                    LaunchedEffect(Unit) {
                        onImportSuccess()
                    }
                }
                is ImportPreviewState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onNavigateBack) {
                            Text(stringResource(R.string.back))
                        }
                    }
                }
            }
        }
    }
}

/**
 * 课程预览列表
 */
@Composable
private fun CoursePreviewList(
    courses: List<Course>,
    selectedIndices: Set<Int>,
    duplicateWarnings: List<String>,
    suggestedSemesterStartDate: java.time.LocalDate? = null,
    onToggleSelection: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 建议的学期开始日期
        if (suggestedSemesterStartDate != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "检测到学期开始日期",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            Text(
                                text = "将自动设置为: ${suggestedSemesterStartDate.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日"))}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }
            }
        }

        // 重复警告
        if (duplicateWarnings.isNotEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.duplicate_warning),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        duplicateWarnings.forEach { warning ->
                            Text(
                                text = "• $warning",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }
        }
        
        // 课程列表
        itemsIndexed(courses) { index, course ->
            CoursePreviewItem(
                course = course,
                isSelected = index in selectedIndices,
                onToggle = { onToggleSelection(index) }
            )
        }
    }
}

/**
 * 课程预览项
 */
@Composable
private fun CoursePreviewItem(
    course: Course,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggle() }
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = course.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${course.dayOfWeek} ${course.startTime.format(timeFormatter)}-${course.endTime.format(timeFormatter)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                if (!course.location.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = course.location,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                if (course.periodStart != null && course.periodEnd != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (course.periodStart == course.periodEnd) {
                            "第${course.periodStart}节"
                        } else {
                            "第${course.periodStart}-${course.periodEnd}节"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
