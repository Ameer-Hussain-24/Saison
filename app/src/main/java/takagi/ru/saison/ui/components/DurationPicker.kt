package takagi.ru.saison.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import takagi.ru.saison.R
import takagi.ru.saison.util.DurationFormatter

/**
 * 时长选择器对话框
 * 
 * @param initialMinutes 初始时长（分钟）
 * @param onDismiss 关闭对话框回调
 * @param onConfirm 确认选择回调，返回选择的总分钟数
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DurationPickerDialog(
    initialMinutes: Int?,
    onDismiss: () -> Unit,
    onConfirm: (Int?) -> Unit
) {
    val (initialHours, initialMins) = if (initialMinutes != null && initialMinutes > 0) {
        DurationFormatter.fromMinutes(initialMinutes)
    } else {
        Pair(0, 0)
    }
    
    var selectedHours by remember { mutableIntStateOf(initialHours) }
    var selectedMinutes by remember { mutableIntStateOf(initialMins) }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.98f)
                .padding(horizontal = 4.dp),
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // 标题
                Text(
                    text = "设置时长",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // 时长选择器
                DurationPicker(
                    hours = selectedHours,
                    minutes = selectedMinutes,
                    onHoursChange = { selectedHours = it },
                    onMinutesChange = { selectedMinutes = it }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 常用时长快捷按钮
                Text(
                    text = "常用时长",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                QuickDurationButtons(
                    onDurationSelected = { minutes ->
                        val (h, m) = DurationFormatter.fromMinutes(minutes)
                        selectedHours = h
                        selectedMinutes = m
                    }
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // 按钮行
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 清除按钮 - 移除图标避免文字挤压
                    OutlinedButton(
                        onClick = {
                            onConfirm(null)
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 12.dp)
                    ) {
                        Text("清除")
                    }
                    
                    // 取消按钮
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 12.dp)
                    ) {
                        Text("取消")
                    }
                    
                    // 确认按钮
                    Button(
                        onClick = {
                            val totalMinutes = DurationFormatter.toMinutes(selectedHours, selectedMinutes)
                            onConfirm(if (totalMinutes > 0) totalMinutes else null)
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 12.dp)
                    ) {
                        Text("确定")
                    }
                }
            }
        }
    }
}

/**
 * 时长选择器组件 - 使用滑动条
 */
@Composable
private fun DurationPicker(
    hours: Int,
    minutes: Int,
    onHoursChange: (Int) -> Unit,
    onMinutesChange: (Int) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // 小时选择器
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "小时",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = hours.toString().padStart(2, '0'),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Slider(
                value = hours.toFloat(),
                onValueChange = { onHoursChange(it.toInt()) },
                valueRange = 0f..23f,
                steps = 22,
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        // 分钟选择器
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "分钟",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = minutes.toString().padStart(2, '0'),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Slider(
                value = minutes.toFloat(),
                onValueChange = { onMinutesChange(it.toInt()) },
                valueRange = 0f..45f,
                steps = 2, // 0, 15, 30, 45
                modifier = Modifier.fillMaxWidth()
            )
            // 显示刻度标签
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("0", "15", "30", "45").forEach { label ->
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * 常用时长快捷按钮 - 每行2个，避免文字挤压
 */
@Composable
private fun QuickDurationButtons(
    onDurationSelected: (Int) -> Unit
) {
    val quickDurations = listOf(
        15 to "15分钟",
        30 to "30分钟",
        45 to "45分钟",
        60 to "1小时",
        90 to "1.5小时",
        120 to "2小时"
    )
    
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        quickDurations.chunked(2).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { (minutes, label) ->
                    OutlinedButton(
                        onClick = { onDurationSelected(minutes) },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
                // 填充空白以保持对齐
                repeat(2 - row.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
