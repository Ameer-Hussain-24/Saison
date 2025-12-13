package takagi.ru.saison.ui.screens.valueday

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddValueDaySheet(
    onDismiss: () -> Unit,
    onAdd: (itemName: String, price: Double, date: LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    var itemName by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var showDatePicker by remember { mutableStateOf(false) }
    
    val dateFormatter = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd") }
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "添加购买记录",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // 商品名称
            OutlinedTextField(
                value = itemName,
                onValueChange = { itemName = it },
                label = { Text("商品名称") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 购买价格
            OutlinedTextField(
                value = priceText,
                onValueChange = { 
                    // 只允许输入数字和小数点
                    if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) {
                        priceText = it
                    }
                },
                label = { Text("购买价格") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                prefix = { Text("¥") },
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 购买日期
            OutlinedTextField(
                value = selectedDate.format(dateFormatter),
                onValueChange = {},
                label = { Text("购买日期") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.CalendarToday, contentDescription = "选择日期")
                    }
                }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("取消")
                }
                
                Button(
                    onClick = {
                        val price = priceText.toDoubleOrNull()
                        if (itemName.isNotBlank() && price != null && price > 0) {
                            onAdd(itemName, price, selectedDate)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = itemName.isNotBlank() && priceText.toDoubleOrNull()?.let { it > 0 } == true
                ) {
                    Text("添加")
                }
            }
        }
    }
    
    // 日期选择器
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.toEpochDay() * 24 * 60 * 60 * 1000
        )
        
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            selectedDate = LocalDate.ofEpochDay(millis / (24 * 60 * 60 * 1000))
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
