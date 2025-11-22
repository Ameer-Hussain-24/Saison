package takagi.ru.saison.ui.screens.subscription

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSubscriptionSheet(
    onDismiss: () -> Unit,
    onSave: (String, String, Double, String, Int, LocalDate, String?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("数字产品") }
    var price by remember { mutableStateOf("") }
    var cycleType by remember { mutableStateOf("MONTHLY") }
    var cycleDuration by remember { mutableStateOf("1") }
    var startDate by remember { mutableStateOf(LocalDate.now()) }
    var note by remember { mutableStateOf("") }
    
    var showDatePicker by remember { mutableStateOf(false) }
    var showCycleTypeDropdown by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .padding(bottom = 32.dp)
                .navigationBarsPadding()
                .imePadding(), // Avoid keyboard
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "添加订阅",
                style = MaterialTheme.typography.headlineSmall
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("名称") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                label = { Text("类别") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = price,
                    onValueChange = { if (it.all { char -> char.isDigit() || char == '.' }) price = it },
                    label = { Text("价格") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                
                // Cycle Type Dropdown
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = when(cycleType) {
                            "MONTHLY" -> "按月"
                            "QUARTERLY" -> "按季"
                            "YEARLY" -> "按年"
                            else -> cycleType
                        },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("周期类型") },
                        trailingIcon = {
                            IconButton(onClick = { showCycleTypeDropdown = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    DropdownMenu(
                        expanded = showCycleTypeDropdown,
                        onDismissRequest = { showCycleTypeDropdown = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("按月") },
                            onClick = { cycleType = "MONTHLY"; showCycleTypeDropdown = false }
                        )
                        DropdownMenuItem(
                            text = { Text("按季") },
                            onClick = { cycleType = "QUARTERLY"; showCycleTypeDropdown = false }
                        )
                        DropdownMenuItem(
                            text = { Text("按年") },
                            onClick = { cycleType = "YEARLY"; showCycleTypeDropdown = false }
                        )
                    }
                }
            }
            
            // Date Picker Trigger
            OutlinedTextField(
                value = startDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                onValueChange = {},
                readOnly = true,
                label = { Text("开始日期") },
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                },
                modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true },
                enabled = false, // Disable text input, handle click on container or icon
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            Button(
                onClick = {
                    val priceVal = price.toDoubleOrNull() ?: 0.0
                    val durationVal = cycleDuration.toIntOrNull() ?: 1
                    onSave(name, category, priceVal, cycleType, durationVal, startDate, note)
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank() && price.isNotBlank()
            ) {
                Text("保存")
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = startDate.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        startDate = java.time.Instant.ofEpochMilli(millis).atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                    }
                    showDatePicker = false
                }) {
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
