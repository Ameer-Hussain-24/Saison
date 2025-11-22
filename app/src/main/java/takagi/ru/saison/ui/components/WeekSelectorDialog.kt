package takagi.ru.saison.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import takagi.ru.saison.domain.model.WeekPattern
import java.time.LocalDate

/**
 * 周数选择模式
 */
enum class SelectionMode {
    WEEK_NUMBER,  // 周数模式
    DATE_RANGE    // 日期模式
}

/**
 * 快捷选择模式
 */
enum class QuickSelectionMode {
    ALL,   // 全周
    ODD,   // 单周
    EVEN   // 双周
}

/**
 * 周数选择器对话框
 * 支持周数模式和日期模式两种选择方式
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeekSelectorDialog(
    initialWeekPattern: WeekPattern,
    initialCustomWeeks: List<Int>?,
    totalWeeks: Int,
    onDismiss: () -> Unit,
    onConfirm: (WeekPattern, List<Int>?) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectionMode by remember { mutableStateOf(SelectionMode.WEEK_NUMBER) }
    var selectedWeeks by remember {
        mutableStateOf(
            when (initialWeekPattern) {
                WeekPattern.ALL -> (1..totalWeeks).toSet()
                WeekPattern.ODD -> (1..totalWeeks).filter { it % 2 == 1 }.toSet()
                WeekPattern.EVEN -> (1..totalWeeks).filter { it % 2 == 0 }.toSet()
                WeekPattern.CUSTOM -> initialCustomWeeks?.toSet() ?: emptySet()
                else -> emptySet()
            }
        )
    }
    var quickMode by remember {
        mutableStateOf(
            when (initialWeekPattern) {
                WeekPattern.ALL -> QuickSelectionMode.ALL
                WeekPattern.ODD -> QuickSelectionMode.ODD
                WeekPattern.EVEN -> QuickSelectionMode.EVEN
                else -> null
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // 标题
                Text(
                    text = "选择上课周数",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // 模式切换 TabRow
                TabRow(
                    selectedTabIndex = if (selectionMode == SelectionMode.WEEK_NUMBER) 0 else 1
                ) {
                    Tab(
                        selected = selectionMode == SelectionMode.WEEK_NUMBER,
                        onClick = { selectionMode = SelectionMode.WEEK_NUMBER },
                        text = { Text("请选择周数") }
                    )
                    Tab(
                        selected = selectionMode == SelectionMode.DATE_RANGE,
                        onClick = { selectionMode = SelectionMode.DATE_RANGE },
                        text = { Text("日期模式") }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 内容区域
                when (selectionMode) {
                    SelectionMode.WEEK_NUMBER -> {
                        WeekNumberSelectionContent(
                            selectedWeeks = selectedWeeks,
                            quickMode = quickMode,
                            totalWeeks = totalWeeks,
                            onWeekToggle = { week ->
                                selectedWeeks = if (week in selectedWeeks) {
                                    selectedWeeks - week
                                } else {
                                    selectedWeeks + week
                                }
                                quickMode = null  // 手动选择后取消快捷模式
                            },
                            onQuickModeSelect = { mode ->
                                quickMode = mode
                                selectedWeeks = when (mode) {
                                    QuickSelectionMode.ALL -> (1..totalWeeks).toSet()
                                    QuickSelectionMode.ODD -> (1..totalWeeks).filter { it % 2 == 1 }.toSet()
                                    QuickSelectionMode.EVEN -> (1..totalWeeks).filter { it % 2 == 0 }.toSet()
                                }
                            }
                        )
                    }
                    SelectionMode.DATE_RANGE -> {
                        DateRangeSelectionContent(
                            onDateRangeSelected = { startDate, endDate ->
                                // TODO: 根据日期范围计算周数
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 统计信息
                AnimatedContent(
                    targetState = selectedWeeks.size,
                    label = "week count"
                ) { count ->
                    Text(
                        text = "已选择 $count 周",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 操作按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("取消")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val weekPattern = when {
                                quickMode == QuickSelectionMode.ALL -> WeekPattern.ALL
                                quickMode == QuickSelectionMode.ODD -> WeekPattern.ODD
                                quickMode == QuickSelectionMode.EVEN -> WeekPattern.EVEN
                                else -> WeekPattern.CUSTOM
                            }
                            val customWeeks = if (weekPattern == WeekPattern.CUSTOM) {
                                selectedWeeks.sorted()
                            } else null
                            onConfirm(weekPattern, customWeeks)
                        },
                        enabled = selectedWeeks.isNotEmpty()
                    ) {
                        Text("确定")
                    }
                }
            }
        }
    }
}

/**
 * 周数选择内容区域
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WeekNumberSelectionContent(
    selectedWeeks: Set<Int>,
    quickMode: QuickSelectionMode?,
    totalWeeks: Int,
    onWeekToggle: (Int) -> Unit,
    onQuickModeSelect: (QuickSelectionMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // 快捷选择按钮
        QuickSelectionBar(
            selectedMode = quickMode,
            onModeSelect = onQuickModeSelect
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 周数网格
        WeekNumberGrid(
            selectedWeeks = selectedWeeks,
            totalWeeks = totalWeeks,
            onWeekToggle = onWeekToggle
        )
    }
}

/**
 * 快捷选择按钮栏
 */
@Composable
private fun QuickSelectionBar(
    selectedMode: QuickSelectionMode?,
    onModeSelect: (QuickSelectionMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selectedMode == QuickSelectionMode.ALL,
            onClick = { onModeSelect(QuickSelectionMode.ALL) },
            label = { Text("全周") },
            modifier = Modifier.weight(1f)
        )
        FilterChip(
            selected = selectedMode == QuickSelectionMode.ODD,
            onClick = { onModeSelect(QuickSelectionMode.ODD) },
            label = { Text("单周") },
            modifier = Modifier.weight(1f)
        )
        FilterChip(
            selected = selectedMode == QuickSelectionMode.EVEN,
            onClick = { onModeSelect(QuickSelectionMode.EVEN) },
            label = { Text("双周") },
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * 周数网格
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WeekNumberGrid(
    selectedWeeks: Set<Int>,
    totalWeeks: Int,
    onWeekToggle: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        maxItemsInEachRow = 6
    ) {
        for (week in 1..totalWeeks) {
            WeekNumberButton(
                weekNumber = week,
                isSelected = week in selectedWeeks,
                onClick = { onWeekToggle(week) }
            )
        }
    }
}

/**
 * 单个周数按钮
 */
@Composable
private fun WeekNumberButton(
    weekNumber: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        label = "container color"
    )

    val contentColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.onPrimary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        label = "content color"
    )

    Surface(
        onClick = onClick,
        modifier = modifier.size(48.dp),
        shape = CircleShape,
        color = containerColor,
        contentColor = contentColor
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = weekNumber.toString(),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

/**
 * 日期范围选择内容区域
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateRangeSelectionContent(
    onDateRangeSelected: (LocalDate, LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    var startDate by remember { mutableStateOf<LocalDate?>(null) }
    var endDate by remember { mutableStateOf<LocalDate?>(null) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 开始日期
        OutlinedCard(
            onClick = { showStartDatePicker = true },
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
                        text = startDate?.toString() ?: "请选择",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null
                )
            }
        }

        // 结束日期
        OutlinedCard(
            onClick = { showEndDatePicker = true },
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
                        text = "结束日期",
                        style = MaterialTheme.typography.labelMedium
                    )
                    Text(
                        text = endDate?.toString() ?: "请选择",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null
                )
            }
        }

        // 重复模式
        Text("重复模式", style = MaterialTheme.typography.titleSmall)
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier.fillMaxWidth()
        ) {
            SegmentedButton(
                selected = true,
                onClick = { },
                shape = SegmentedButtonDefaults.itemShape(0, 3)
            ) {
                Text("每周")
            }
            SegmentedButton(
                selected = false,
                onClick = { },
                shape = SegmentedButtonDefaults.itemShape(1, 3)
            ) {
                Text("单周")
            }
            SegmentedButton(
                selected = false,
                onClick = { },
                shape = SegmentedButtonDefaults.itemShape(2, 3)
            ) {
                Text("双周")
            }
        }
    }

    // DatePicker 对话框
    if (showStartDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = { showStartDatePicker = false }) {
                    Text("确定")
                }
            }
        ) {
            DatePicker(state = rememberDatePickerState())
        }
    }

    if (showEndDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = { showEndDatePicker = false }) {
                    Text("确定")
                }
            }
        ) {
            DatePicker(state = rememberDatePickerState())
        }
    }
}
