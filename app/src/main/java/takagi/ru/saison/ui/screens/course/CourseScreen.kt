package takagi.ru.saison.ui.screens.course

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import takagi.ru.saison.domain.model.Course
import takagi.ru.saison.domain.model.WeekPattern
import takagi.ru.saison.ui.components.CourseCard
import takagi.ru.saison.ui.components.EditCourseSheet
import takagi.ru.saison.ui.components.ExportCoursesDialog
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseScreen(
    viewModel: CourseViewModel = hiltViewModel(),
    onCourseClick: (Long) -> Unit = {},
    onNavigateToImportPreview: (Uri, Long) -> Unit = { _, _ -> },
    onNavigateToAllCourses: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val coursesByDay by viewModel.coursesByDay.collectAsState()
    val currentWeek by viewModel.currentWeek.collectAsState()
    val weekOffset by viewModel.weekOffset.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val courseSettings by viewModel.courseSettings.collectAsState()
    val periods by viewModel.periods.collectAsState()
    val currentSemesterId by viewModel.currentSemesterIdState.collectAsState()
    val currentPeriod by viewModel.currentPeriod.collectAsState()
    val currentDay by viewModel.currentDay.collectAsState()
    val showWeekSelectorSheet by viewModel.showWeekSelectorSheet.collectAsState()
    
    // 更新当前节次
    LaunchedEffect(Unit) {
        viewModel.updateCurrentPeriod()
    }
    
    var showAddSheet by remember { mutableStateOf(false) }
    var showSettingsSheet by remember { mutableStateOf(false) }
    var showSemesterSettingsSheet by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var courseToEdit by remember { mutableStateOf<Course?>(null) }
    
    // 文件选择器 - 用于导入ICS文件
    val context = androidx.compose.ui.platform.LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            // 立即读取文件内容（在权限有效期内）
            coroutineScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                try {
                    val content = context.contentResolver.openInputStream(it)?.use { stream ->
                        stream.bufferedReader().use { reader ->
                            reader.readText()
                        }
                    }
                    
                    if (content != null) {
                        // 将内容存储到临时缓存
                        takagi.ru.saison.util.TempFileCache.store(content)
                        
                        // 切换回主线程进行导航
                        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                            val semesterId = currentSemesterId ?: 1L
                            onNavigateToImportPreview(it, semesterId)
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("CourseScreen", "Failed to read file", e)
                }
            }
        }
    }
    
    // 学期相关状态
    val semesterViewModel: takagi.ru.saison.ui.screens.semester.SemesterViewModel = hiltViewModel()
    val currentSemester by semesterViewModel.currentSemester.collectAsState()
    val allSemesters by semesterViewModel.allSemesters.collectAsState()
    var showSemesterList by remember { mutableStateOf(false) }
    
    // HorizontalPager 状态
    // 使用totalWeeks作为页面数量，每页对应一周（1到totalWeeks）
    val totalWeeks = courseSettings.totalWeeks
    val baseWeek: Int = remember(courseSettings.semesterStartDate) {
        getCurrentWeekNumber(courseSettings.semesterStartDate)
    }
    
    // 初始页面对应当前周（baseWeek）
    // 页面索引从0开始，对应第1周；页面索引totalWeeks-1对应第totalWeeks周
    val initialPage: Int = remember(baseWeek, totalWeeks) { 
        (baseWeek - 1).coerceIn(0, totalWeeks - 1)
    }
    
    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { totalWeeks }
    )
    
    // 监听 pager 页面变化，更新 weekOffset
    LaunchedEffect(pagerState.currentPage, initialPage) {
        val offset = pagerState.currentPage - initialPage
        viewModel.setWeekOffset(offset)
        
        android.util.Log.d("CourseScreen", "Pager page changed: page=${pagerState.currentPage}, initialPage=$initialPage, offset=$offset")
    }
    
    // 当 weekOffset 从外部改变时（如点击"回到当前周"按钮），同步更新 pager
    LaunchedEffect(weekOffset) {
        val targetPage = initialPage + weekOffset
        // 确保目标页面在有效范围内（0到totalWeeks-1）
        if (targetPage != pagerState.currentPage && targetPage in 0 until totalWeeks) {
            pagerState.animateScrollToPage(targetPage)
            android.util.Log.d("CourseScreen", "Animating to page: $targetPage (week ${targetPage + 1})")
        }
    }
    
    Scaffold(
        topBar = {
            CourseTopBar(
                displayWeek = pagerState.currentPage + 1,
                currentWeek = currentWeek,
                onSettingsClick = { showSettingsSheet = true },
                onSemesterSettingsClick = { showSemesterSettingsSheet = true },
                onImportClick = {
                    // 启动文件选择器，选择ICS文件
                    // OpenDocument需要传递MIME类型数组
                    importLauncher.launch(arrayOf("text/calendar", "text/x-vcalendar", "*/*"))
                },
                onExportClick = {
                    showExportDialog = true
                },
                onViewAllClick = onNavigateToAllCourses,
                totalWeeks = courseSettings.totalWeeks,
                weekOffset = weekOffset,
                onBackToCurrentWeek = { viewModel.goToCurrentWeek() }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddSheet = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("添加课程") }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // HorizontalPager 包装的网格课程表视图
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                // 计算当前页面对应的周数（页面0对应第1周，页面1对应第2周，以此类推）
                val pageWeek = page + 1
                
                android.util.Log.d("CourseScreen", "Rendering page $page, week $pageWeek")
                
                // 根据当前页面的周数过滤课程
                val filteredCoursesByDay = remember(coursesByDay, pageWeek) {
                    coursesByDay.mapValues { (_, courses) ->
                        courses.filter { course ->
                            viewModel.isCourseActiveInWeek(course, pageWeek)
                        }
                    }
                }
                
                // 检查是否有课程
                val hasAnyCourses = filteredCoursesByDay.values.any { it.isNotEmpty() }
                
                if (!hasAnyCourses) {
                    EmptyCourseList()
                } else {
                    // 网格课程表
                    takagi.ru.saison.ui.components.GridTimetableView(
                            coursesByDay = filteredCoursesByDay,
                            periods = periods,
                            breakPeriods = courseSettings.breakPeriods,
                            semesterStartDate = courseSettings.semesterStartDate ?: LocalDate.now(),
                            currentWeek = pageWeek,
                            onCourseClick = { courseId ->
                                // 找到被点击的课程并打开编辑面板
                                val course = filteredCoursesByDay.values.flatten().find { it.id == courseId }
                                courseToEdit = course
                            },
                            onEmptyCellClick = { day, periodNumber ->
                                // 点击空白单元格,打开添加课程面板并预填充信息
                                // TODO: 实现预填充逻辑
                                showAddSheet = true
                            },
                            currentPeriod = currentPeriod,
                            currentDay = currentDay,
                            config = takagi.ru.saison.domain.model.GridLayoutConfig(
                                cellHeight = courseSettings.gridCellHeight.dp,
                                showBreakSeparators = false
                            ),
                            weekDays = if (courseSettings.showWeekends) {
                                DayOfWeek.values().toList()
                            } else {
                                listOf(
                                    DayOfWeek.MONDAY,
                                    DayOfWeek.TUESDAY,
                                    DayOfWeek.WEDNESDAY,
                                    DayOfWeek.THURSDAY,
                                    DayOfWeek.FRIDAY
                                )
                            },
                            autoScrollToCurrentTime = courseSettings.autoScrollToCurrentTime,
                            onWeekLabelClick = { viewModel.showWeekSelectorSheet() }
                        )
                }
            }
        }
        
        // 错误提示
        when (val state = uiState) {
            is CourseUiState.Error -> {
                Snackbar(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(state.message)
                }
            }
            else -> {}
        }
    }
    
    // 课程设置面板
    if (showSettingsSheet) {
        takagi.ru.saison.ui.components.CourseSettingsSheet(
            currentSettings = courseSettings,
            periods = periods,
            onDismiss = { showSettingsSheet = false },
            onSave = { newSettings ->
                viewModel.updateSettings(newSettings)
                showSettingsSheet = false
            },
            onNavigateToSemesterManagement = {
                showSemesterList = true
            }
        )
    }
    
    // 学期设置面板
    if (showSemesterSettingsSheet) {
        takagi.ru.saison.ui.components.SemesterSettingsSheet(
            currentSettings = courseSettings,
            onDismiss = { showSemesterSettingsSheet = false },
            onSave = { newSettings ->
                viewModel.updateSettings(newSettings)
                showSemesterSettingsSheet = false
            }
        )
    }
    
    // 学期列表界面
    if (showSemesterList) {
        takagi.ru.saison.ui.screens.semester.SemesterListScreen(
            viewModel = semesterViewModel,
            onNavigateBack = { showSemesterList = false }
        )
    }
    
    // 添加课程面板
    if (showAddSheet) {
        // 获取所有现有课程用于颜色分配
        val allCourses = coursesByDay.values.flatten()
        
        takagi.ru.saison.ui.components.AddCourseSheet(
            periods = periods,
            occupiedPeriods = emptySet(), // TODO: 根据选中的星期获取已占用节次
            existingCourses = allCourses,
            onDismiss = { showAddSheet = false },
            onSave = { course ->
                viewModel.addCourse(course)
                showAddSheet = false
            }
        )
    }
    
    // 编辑课程面板
    courseToEdit?.let { course ->
        // 获取所有现有课程用于颜色分配(排除当前编辑的课程)
        val allCourses = coursesByDay.values.flatten().filter { it.id != course.id }
        
        EditCourseSheet(
            course = course,
            periods = periods,
            occupiedPeriods = emptySet(), // TODO: 根据选中的星期获取已占用节次
            existingCourses = allCourses,
            onDismiss = { courseToEdit = null },
            onSave = { updatedCourse ->
                viewModel.updateCourse(updatedCourse)
                courseToEdit = null
            },
            onDelete = {
                viewModel.deleteCourse(course.id)
                courseToEdit = null
            }
        )
    }
    
    // 导出对话框
    if (showExportDialog) {
        ExportCoursesDialog(
            onDismiss = { showExportDialog = false },
            onExport = { fileName ->
                // TODO: 调用viewModel的导出方法
                // viewModel.exportCurrentSemester(fileName)
                showExportDialog = false
            },
            isLoading = false
        )
    }
    
    // 周次选择器底部抽屉
    if (showWeekSelectorSheet) {
        takagi.ru.saison.ui.components.WeekSelectorBottomSheet(
            currentWeek = currentWeek,
            totalWeeks = courseSettings.totalWeeks,
            onWeekSelected = { week ->
                viewModel.selectWeek(week)
            },
            onDismiss = { viewModel.hideWeekSelectorSheet() }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CourseTopBar(
    displayWeek: Int,
    currentWeek: Int,
    onSettingsClick: () -> Unit,
    onSemesterSettingsClick: () -> Unit,
    onImportClick: () -> Unit,
    onExportClick: () -> Unit,
    onViewAllClick: () -> Unit,
    totalWeeks: Int = 18,
    weekOffset: Int = 0,
    onBackToCurrentWeek: () -> Unit = {}
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = "课程表",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "第 $displayWeek / $totalWeeks 周",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        actions = {
            // 如果不在当前周，显示"回到当前周"按钮
            if (weekOffset != 0) {
                IconButton(onClick = onBackToCurrentWeek) {
                    Icon(Icons.Default.Today, contentDescription = "回到当前周")
                }
            }
            
            // 导入按钮
            IconButton(onClick = onImportClick) {
                Icon(Icons.Default.FileUpload, contentDescription = "导入课程表")
            }
            
            // 导出按钮
            IconButton(onClick = onExportClick) {
                Icon(Icons.Default.FileDownload, contentDescription = "导出课程表")
            }
            
            // 查看所有课程按钮
            IconButton(onClick = onViewAllClick) {
                Icon(Icons.Outlined.MenuBook, contentDescription = "所有课程")
            }
            
            IconButton(onClick = onSemesterSettingsClick) {
                Icon(Icons.Default.DateRange, contentDescription = "学期设置")
            }
            IconButton(onClick = onSettingsClick) {
                Icon(Icons.Default.Settings, contentDescription = "课程设置")
            }
        }
    )
}

@Composable
private fun DaySection(
    day: DayOfWeek,
    courses: List<Course>,
    onCourseClick: (Long) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 星期标题
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = MaterialTheme.shapes.small
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = getDayName(day),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "${courses.size} 节课",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        
        // 课程列表
        courses.sortedBy { it.startTime }.forEach { course ->
            CourseCard(
                course = course,
                onClick = { onCourseClick(course.id) }
            )
        }
    }
}

@Composable
private fun EmptyCourseList() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Material 3 icon for books/courses, replacing emoji for consistency
            Icon(
                imageVector = Icons.Outlined.MenuBook,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
            )
            Text(
                text = "还没有课程",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "点击右下角按钮添加课程",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun getDayName(day: DayOfWeek): String {
    return when (day) {
        DayOfWeek.MONDAY -> "周一"
        DayOfWeek.TUESDAY -> "周二"
        DayOfWeek.WEDNESDAY -> "周三"
        DayOfWeek.THURSDAY -> "周四"
        DayOfWeek.FRIDAY -> "周五"
        DayOfWeek.SATURDAY -> "周六"
        DayOfWeek.SUNDAY -> "周日"
    }
}

/**
 * 计算当前周数
 * @param semesterStartDate 学期开始日期，如果为null则返回1
 */
private fun getCurrentWeekNumber(semesterStartDate: LocalDate?): Int {
    if (semesterStartDate == null) {
        return 1
    }
    
    val today = LocalDate.now()
    val daysBetween = java.time.temporal.ChronoUnit.DAYS.between(semesterStartDate, today)
    val weekNumber = (daysBetween / 7).toInt() + 1
    
    return weekNumber.coerceAtLeast(1)
}
