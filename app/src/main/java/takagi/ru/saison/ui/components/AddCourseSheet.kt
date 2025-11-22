package takagi.ru.saison.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import takagi.ru.saison.domain.model.Course
import takagi.ru.saison.domain.model.CoursePeriod
import takagi.ru.saison.domain.model.WeekPattern
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * 添加/编辑课程底部面板
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCourseSheet(
    course: Course? = null,
    periods: List<CoursePeriod>,
    occupiedPeriods: Set<Int>,
    existingCourses: List<Course> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (Course) -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf(course?.name ?: "") }
    var instructor by remember { mutableStateOf(course?.instructor ?: "") }
    var location by remember { mutableStateOf(course?.location ?: "") }
    var dayOfWeek by remember { mutableStateOf(course?.dayOfWeek ?: DayOfWeek.MONDAY) }
    var weekPattern by remember { mutableStateOf(course?.weekPattern ?: WeekPattern.ALL) }
    var customWeeks by remember { mutableStateOf(course?.customWeeks) }
    var showWeekDetailDialog by remember { mutableStateOf(false) }
    
    // 时间输入模式：true=按节次，false=自定义时间
    var usePeriodsMode by remember { mutableStateOf(course?.isCustomTime == false) }
    
    // 节次模式
    var periodStart by remember { mutableStateOf(course?.periodStart) }
    var periodEnd by remember { mutableStateOf(course?.periodEnd) }
    
    // 自定义时间模式
    var startTime by remember { mutableStateOf(course?.startTime ?: LocalTime.of(8, 0)) }
    var endTime by remember { mutableStateOf(course?.endTime ?: LocalTime.of(9, 0)) }
    
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            // 标题栏
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (course == null) "添加课程" else "编辑课程",
                    style = MaterialTheme.typography.titleLarge
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "关闭")
                }
            }
            
            Divider()
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                
                // 课程名称
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("课程名称") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                // 星期选择
                Text("星期", style = MaterialTheme.typography.titleSmall)
                DayOfWeekSelector(
                    selectedDay = dayOfWeek,
                    onDaySelected = { dayOfWeek = it }
                )
                
                // 时间输入模式切换
                Text("时间输入方式", style = MaterialTheme.typography.titleSmall)
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SegmentedButton(
                        selected = usePeriodsMode,
                        onClick = { usePeriodsMode = true },
                        shape = SegmentedButtonDefaults.itemShape(0, 2)
                    ) {
                        Text("按节次")
                    }
                    SegmentedButton(
                        selected = !usePeriodsMode,
                        onClick = { usePeriodsMode = false },
                        shape = SegmentedButtonDefaults.itemShape(1, 2)
                    ) {
                        Text("自定义时间")
                    }
                }
                
                // 时间选择区域
                if (usePeriodsMode) {
                    Text("选择节次", style = MaterialTheme.typography.titleSmall)
                    PeriodSelector(
                        periods = periods,
                        selectedPeriodStart = periodStart,
                        selectedPeriodEnd = periodEnd,
                        occupiedPeriods = occupiedPeriods,
                        onPeriodRangeSelected = { start, end ->
                            if (start > 0 && end > 0) {
                                periodStart = start
                                periodEnd = end
                                // 自动填充时间
                                periods.find { it.periodNumber == start }?.let { 
                                    startTime = it.startTime 
                                }
                                periods.find { it.periodNumber == end }?.let { 
                                    endTime = it.endTime 
                                }
                            }
                        }
                    )
                } else {
                    // 自定义时间选择
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedCard(
                            onClick = { showStartTimePicker = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("开始时间", style = MaterialTheme.typography.labelSmall)
                                Text(
                                    startTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                        OutlinedCard(
                            onClick = { showEndTimePicker = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("结束时间", style = MaterialTheme.typography.labelSmall)
                                Text(
                                    endTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                    }
                }
                
                // 教师
                OutlinedTextField(
                    value = instructor,
                    onValueChange = { instructor = it },
                    label = { Text("教师（可选）") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                // 地点
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("地点（可选）") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                // 周模式
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("周模式", style = MaterialTheme.typography.titleSmall)
                    TextButton(onClick = { showWeekDetailDialog = true }) {
                        Text("查看详情")
                    }
                }
                WeekPatternSelector(
                    selectedPattern = weekPattern,
                    onPatternSelected = { weekPattern = it }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // 获取主题色(在Composable上下文中)
            val primaryColor = MaterialTheme.colorScheme.primary
            
            // 周数详情对话框
            if (showWeekDetailDialog) {
                WeekSelectorDialog(
                    initialWeekPattern = weekPattern,
                    initialCustomWeeks = customWeeks,
                    totalWeeks = 18, // TODO: 从设置中获取
                    onDismiss = { showWeekDetailDialog = false },
                    onConfirm = { pattern, weeks ->
                        weekPattern = pattern
                        customWeeks = weeks
                        showWeekDetailDialog = false
                    }
                )
            }
            
            // 保存按钮
            Button(
                onClick = {
                    // 自动分配颜色(编辑时保留原颜色)
                    val assignedColor = course?.color ?: 
                        takagi.ru.saison.util.CourseColorAssigner.assignColor(
                            existingCourses = existingCourses,
                            dayOfWeek = dayOfWeek,
                            startTime = startTime,
                            endTime = endTime,
                            primaryColor = primaryColor
                        )
                    
                    val newCourse = Course(
                        id = course?.id ?: 0,
                        name = name,
                        instructor = instructor.ifBlank { null },
                        location = location.ifBlank { null },
                        color = assignedColor,
                        dayOfWeek = dayOfWeek,
                        startTime = startTime,
                        endTime = endTime,
                        weekPattern = weekPattern,
                        customWeeks = customWeeks,
                        startDate = course?.startDate ?: LocalDate.now(),
                        endDate = course?.endDate ?: LocalDate.now().plusMonths(4),
                        periodStart = if (usePeriodsMode) periodStart else null,
                        periodEnd = if (usePeriodsMode) periodEnd else null,
                        isCustomTime = !usePeriodsMode
                    )
                    onSave(newCourse)
                },
                enabled = name.isNotBlank() && 
                         ((!usePeriodsMode) || (periodStart != null && periodEnd != null)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(if (course == null) "添加课程" else "保存修改")
            }
        }
    }
    
    // 时间选择器
    if (showStartTimePicker) {
        TimePickerDialog(
            initialTime = startTime,
            onDismiss = { showStartTimePicker = false },
            onConfirm = { 
                startTime = it
                showStartTimePicker = false
            }
        )
    }
    
    if (showEndTimePicker) {
        TimePickerDialog(
            initialTime = endTime,
            onDismiss = { showEndTimePicker = false },
            onConfirm = { 
                endTime = it
                showEndTimePicker = false
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DayOfWeekSelector(
    selectedDay: DayOfWeek,
    onDaySelected: (DayOfWeek) -> Unit,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        DayOfWeek.values().forEach { day ->
            FilterChip(
                selected = selectedDay == day,
                onClick = { onDaySelected(day) },
                label = { Text(getDayName(day)) }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WeekPatternSelector(
    selectedPattern: WeekPattern,
    onPatternSelected: (WeekPattern) -> Unit,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val patterns = listOf(
            WeekPattern.ALL to "全部",
            WeekPattern.A to "A周",
            WeekPattern.B to "B周",
            WeekPattern.ODD to "单周",
            WeekPattern.EVEN to "双周"
        )
        
        patterns.forEach { (pattern, label) ->
            FilterChip(
                selected = selectedPattern == pattern,
                onClick = { onPatternSelected(pattern) },
                label = { Text(label) }
            )
        }
    }
}

/**
 * 周数详情对话框
 * 显示所有周数，并提供快捷选择按钮
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WeekDetailDialog(
    selectedPattern: WeekPattern,
    totalWeeks: Int = 18,
    onDismiss: () -> Unit,
    onPatternSelected: (WeekPattern) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("课程周数详情") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 快捷选择按钮
                Text("快捷选择", style = MaterialTheme.typography.titleSmall)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedPattern == WeekPattern.ALL,
                        onClick = { onPatternSelected(WeekPattern.ALL) },
                        label = { Text("全部") }
                    )
                    FilterChip(
                        selected = selectedPattern == WeekPattern.ODD,
                        onClick = { onPatternSelected(WeekPattern.ODD) },
                        label = { Text("单周") }
                    )
                    FilterChip(
                        selected = selectedPattern == WeekPattern.EVEN,
                        onClick = { onPatternSelected(WeekPattern.EVEN) },
                        label = { Text("双周") }
                    )
                }
                
                Divider()
                
                // 周数列表
                Text("周数列表", style = MaterialTheme.typography.titleSmall)
                Text(
                    "以下周数将上这门课：",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // 显示哪些周会上课
                val activeWeeks = (1..totalWeeks).filter { week ->
                    when (selectedPattern) {
                        WeekPattern.ALL -> true
                        WeekPattern.ODD -> week % 2 == 1
                        WeekPattern.EVEN -> week % 2 == 0
                        WeekPattern.A, WeekPattern.B -> true // 简化处理
                        WeekPattern.CUSTOM -> true // 自定义模式显示所有周
                    }
                }
                
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    activeWeeks.forEach { week ->
                        AssistChip(
                            onClick = { },
                            label = { Text("第${week}周") }
                        )
                    }
                }
                
                if (activeWeeks.size < totalWeeks) {
                    Text(
                        "共 ${activeWeeks.size} 周",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("确定")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    initialTime: LocalTime,
    onDismiss: () -> Unit,
    onConfirm: (LocalTime) -> Unit
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialTime.hour,
        initialMinute = initialTime.minute,
        is24Hour = true
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择时间") },
        text = { TimePicker(state = timePickerState) },
        confirmButton = {
            TextButton(onClick = {
                onConfirm(LocalTime.of(timePickerState.hour, timePickerState.minute))
            }) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CourseColorSelector(
    selectedColor: Int,
    onColorSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val courseColors = listOf(
        0xFFE57373.toInt() to "红色",
        0xFFF06292.toInt() to "粉色",
        0xFFBA68C8.toInt() to "紫色",
        0xFF9575CD.toInt() to "深紫色",
        0xFF7986CB.toInt() to "靛蓝",
        0xFF64B5F6.toInt() to "蓝色",
        0xFF4FC3F7.toInt() to "浅蓝",
        0xFF4DD0E1.toInt() to "青色",
        0xFF4DB6AC.toInt() to "蓝绿",
        0xFF81C784.toInt() to "绿色",
        0xFFAED581.toInt() to "浅绿",
        0xFFDCE775.toInt() to "黄绿",
        0xFFFFF176.toInt() to "黄色",
        0xFFFFD54F.toInt() to "琥珀",
        0xFFFFB74D.toInt() to "橙色",
        0xFFFF8A65.toInt() to "深橙"
    )
    
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        courseColors.forEach { (color, _) ->
            androidx.compose.foundation.Canvas(
                modifier = Modifier
                    .size(48.dp)
                    .clickable { onColorSelected(color) }
            ) {
                drawCircle(
                    color = androidx.compose.ui.graphics.Color(color),
                    radius = size.minDimension / 2
                )
                if (selectedColor == color) {
                    drawCircle(
                        color = androidx.compose.ui.graphics.Color.White,
                        radius = size.minDimension / 4
                    )
                }
            }
        }
    }
}

/**
 * 编辑课程底部面板(带删除按钮)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCourseSheet(
    course: Course,
    periods: List<CoursePeriod>,
    occupiedPeriods: Set<Int>,
    existingCourses: List<Course> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (Course) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf(course.name) }
    var instructor by remember { mutableStateOf(course.instructor ?: "") }
    var location by remember { mutableStateOf(course.location ?: "") }
    var dayOfWeek by remember { mutableStateOf(course.dayOfWeek) }
    var weekPattern by remember { mutableStateOf(course.weekPattern) }
    var customWeeks by remember { mutableStateOf(course.customWeeks) }
    var showWeekDetailDialog by remember { mutableStateOf(false) }
    
    // 时间输入模式：true=按节次，false=自定义时间
    var usePeriodsMode by remember { mutableStateOf(!course.isCustomTime) }
    
    // 节次模式
    var periodStart by remember { mutableStateOf(course.periodStart) }
    var periodEnd by remember { mutableStateOf(course.periodEnd) }
    
    // 自定义时间模式
    var startTime by remember { mutableStateOf(course.startTime) }
    var endTime by remember { mutableStateOf(course.endTime) }
    
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            // 标题栏
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "编辑课程",
                    style = MaterialTheme.typography.titleLarge
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "关闭")
                }
            }
            
            Divider()
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                
                // 课程名称
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("课程名称") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                // 星期选择
                Text("星期", style = MaterialTheme.typography.titleSmall)
                DayOfWeekSelector(
                    selectedDay = dayOfWeek,
                    onDaySelected = { dayOfWeek = it }
                )
                
                // 时间输入模式切换
                Text("时间输入方式", style = MaterialTheme.typography.titleSmall)
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SegmentedButton(
                        selected = usePeriodsMode,
                        onClick = { usePeriodsMode = true },
                        shape = SegmentedButtonDefaults.itemShape(0, 2)
                    ) {
                        Text("按节次")
                    }
                    SegmentedButton(
                        selected = !usePeriodsMode,
                        onClick = { usePeriodsMode = false },
                        shape = SegmentedButtonDefaults.itemShape(1, 2)
                    ) {
                        Text("自定义时间")
                    }
                }
                
                // 时间选择区域
                if (usePeriodsMode) {
                    Text("选择节次", style = MaterialTheme.typography.titleSmall)
                    PeriodSelector(
                        periods = periods,
                        selectedPeriodStart = periodStart,
                        selectedPeriodEnd = periodEnd,
                        occupiedPeriods = occupiedPeriods,
                        onPeriodRangeSelected = { start, end ->
                            if (start > 0 && end > 0) {
                                periodStart = start
                                periodEnd = end
                                // 自动填充时间
                                periods.find { it.periodNumber == start }?.let { 
                                    startTime = it.startTime 
                                }
                                periods.find { it.periodNumber == end }?.let { 
                                    endTime = it.endTime 
                                }
                            }
                        }
                    )
                } else {
                    // 自定义时间选择
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedCard(
                            onClick = { showStartTimePicker = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("开始时间", style = MaterialTheme.typography.labelSmall)
                                Text(
                                    startTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                        OutlinedCard(
                            onClick = { showEndTimePicker = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("结束时间", style = MaterialTheme.typography.labelSmall)
                                Text(
                                    endTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                    }
                }
                
                // 教师
                OutlinedTextField(
                    value = instructor,
                    onValueChange = { instructor = it },
                    label = { Text("教师（可选）") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                // 地点
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("地点（可选）") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                // 周模式
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("周模式", style = MaterialTheme.typography.titleSmall)
                    TextButton(onClick = { showWeekDetailDialog = true }) {
                        Text("查看详情")
                    }
                }
                WeekPatternSelector(
                    selectedPattern = weekPattern,
                    onPatternSelected = { weekPattern = it }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // 周数详情对话框
            if (showWeekDetailDialog) {
                WeekSelectorDialog(
                    initialWeekPattern = weekPattern,
                    initialCustomWeeks = customWeeks,
                    totalWeeks = 18, // TODO: 从设置中获取
                    onDismiss = { showWeekDetailDialog = false },
                    onConfirm = { pattern, weeks ->
                        weekPattern = pattern
                        customWeeks = weeks
                        showWeekDetailDialog = false
                    }
                )
            }
            
            // 按钮区域
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 保存按钮
                Button(
                    onClick = {
                        val updatedCourse = Course(
                            id = course.id,
                            name = name,
                            instructor = instructor.ifBlank { null },
                            location = location.ifBlank { null },
                            color = course.color, // 保留原颜色
                            dayOfWeek = dayOfWeek,
                            startTime = startTime,
                            endTime = endTime,
                            weekPattern = weekPattern,
                            customWeeks = customWeeks,
                            startDate = course.startDate,
                            endDate = course.endDate,
                            periodStart = if (usePeriodsMode) periodStart else null,
                            periodEnd = if (usePeriodsMode) periodEnd else null,
                            isCustomTime = !usePeriodsMode
                        )
                        onSave(updatedCourse)
                    },
                    enabled = name.isNotBlank() && 
                             ((!usePeriodsMode) || (periodStart != null && periodEnd != null)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("保存修改")
                }
                
                // 删除按钮
                OutlinedButton(
                    onClick = { showDeleteConfirmation = true },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("删除课程")
                }
            }
        }
    }
    
    // 时间选择器
    if (showStartTimePicker) {
        TimePickerDialog(
            initialTime = startTime,
            onDismiss = { showStartTimePicker = false },
            onConfirm = { 
                startTime = it
                showStartTimePicker = false
            }
        )
    }
    
    if (showEndTimePicker) {
        TimePickerDialog(
            initialTime = endTime,
            onDismiss = { showEndTimePicker = false },
            onConfirm = { 
                endTime = it
                showEndTimePicker = false
            }
        )
    }
    
    // 删除确认对话框
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("删除课程") },
            text = { Text("确定要删除课程「${course.name}」吗？此操作无法撤销。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmation = false
                        onDelete()
                    }
                ) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("取消")
                }
            }
        )
    }
}
