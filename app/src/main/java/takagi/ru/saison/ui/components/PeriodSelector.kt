package takagi.ru.saison.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import takagi.ru.saison.domain.model.CoursePeriod

/**
 * 节次选择器组件
 * 用于选择课程的开始和结束节次
 * 
 * @param periods 节次列表
 * @param selectedPeriodStart 选中的开始节次
 * @param selectedPeriodEnd 选中的结束节次
 * @param occupiedPeriods 已被占用的节次集合
 * @param onPeriodRangeSelected 节次范围选择回调
 * @param modifier 修饰符
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PeriodSelector(
    periods: List<CoursePeriod>,
    selectedPeriodStart: Int?,
    selectedPeriodEnd: Int?,
    occupiedPeriods: Set<Int> = emptySet(),
    onPeriodRangeSelected: (start: Int, end: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var tempStart by remember(selectedPeriodStart) { mutableStateOf(selectedPeriodStart) }
    
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "选择节次",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        if (selectedPeriodStart != null && selectedPeriodEnd != null) {
            Text(
                text = "已选择: 第${selectedPeriodStart}节 - 第${selectedPeriodEnd}节",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        } else if (tempStart != null) {
            Text(
                text = "开始节次: 第${tempStart}节 (请选择结束节次)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
            )
        } else {
            Text(
                text = "请先选择开始节次",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            periods.forEach { period ->
                val periodNumber = period.periodNumber
                val isOccupied = periodNumber in occupiedPeriods
                val isInSelectedRange = selectedPeriodStart != null && 
                                       selectedPeriodEnd != null &&
                                       periodNumber in selectedPeriodStart..selectedPeriodEnd
                val isStart = periodNumber == selectedPeriodStart
                val isEnd = periodNumber == selectedPeriodEnd
                val isTempStart = periodNumber == tempStart
                
                // 如果已选择开始节次但未选择结束节次,禁用小于开始节次的选项
                val tempStartValue = tempStart
                val isDisabled = isOccupied || (tempStartValue != null && periodNumber < tempStartValue)
                
                FilterChip(
                    selected = isInSelectedRange || isTempStart,
                    onClick = {
                        if (!isDisabled) {
                            when {
                                // 如果还没有选择开始节次,设置为开始节次
                                tempStart == null -> {
                                    tempStart = periodNumber
                                }
                                // 如果已有开始节次,设置为结束节次并触发回调
                                else -> {
                                    val start = tempStart
                                    if (start != null) {
                                        onPeriodRangeSelected(start, periodNumber)
                                        tempStart = null
                                    }
                                }
                            }
                        }
                    },
                    label = {
                        Text(
                            text = "第${periodNumber}节",
                            style = MaterialTheme.typography.bodySmall
                        )
                    },
                    enabled = !isDisabled,
                    leadingIcon = if (isStart || isTempStart) {
                        {
                            Text(
                                text = "始",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    } else if (isEnd) {
                        {
                            Text(
                                text = "终",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    } else null,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = if (isOccupied) {
                            MaterialTheme.colorScheme.errorContainer
                        } else {
                            MaterialTheme.colorScheme.primaryContainer
                        },
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                )
            }
        }
        
        // 重置按钮
        if (selectedPeriodStart != null || tempStart != null) {
            TextButton(
                onClick = {
                    tempStart = null
                    if (selectedPeriodStart != null) {
                        onPeriodRangeSelected(0, 0) // 触发清除
                    }
                }
            ) {
                Text("重新选择")
            }
        }
    }
}
