package takagi.ru.saison.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import takagi.ru.saison.domain.model.Semester
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SemesterEditDialog(
    semester: Semester?,
    onDismiss: () -> Unit,
    onSave: (name: String, startDate: LocalDate, totalWeeks: Int) -> Unit
) {
    var name by remember { mutableStateOf(semester?.name ?: "") }
    var startDate by remember { mutableStateOf(semester?.startDate ?: LocalDate.now()) }
    var totalWeeks by remember { mutableStateOf(semester?.totalWeeks?.toFloat() ?: 18f) }
    var showDatePicker by remember { mutableStateOf(false) }
    
    val isEdit = semester != null
    val dateFormatter = remember { DateTimeFormatter.ofPattern("yyyy年MM月dd日") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEdit) "编辑学期" else "创建学期") },
        text = {
            Column(
                modifier = Modifier.wrapContentHeight(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 学期名称
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("学期名称") },
                    placeholder = { Text("如：2025春季学期") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                // 开始日期
                OutlinedCard(
                    onClick = { showDatePicker = true },
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
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = startDate.format(dateFormatter),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                // 总周数
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "学期总周数",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "${totalWeeks.toInt()} 周",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Slider(
                        value = totalWeeks,
                        onValueChange = { totalWeeks = it },
                        valueRange = 8f..30f,
                        steps = 21,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "8周",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "30周",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // 结束日期预览
                val endDate = startDate.plusWeeks(totalWeeks.toLong())
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "结束日期",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            text = endDate.format(dateFormatter),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(name, startDate, totalWeeks.toInt())
                },
                enabled = name.isNotBlank()
            ) {
                Text(if (isEdit) "保存" else "创建")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
    
    // 日期选择器
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = startDate.toEpochDay() * 86400000L
        )
        
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            startDate = LocalDate.ofEpochDay(millis / 86400000L)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("取消")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
