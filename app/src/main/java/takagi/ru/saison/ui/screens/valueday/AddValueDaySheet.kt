package takagi.ru.saison.ui.screens.valueday

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import takagi.ru.saison.data.local.database.entities.ValueDayEntity
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddValueDaySheet(
    isEdit: Boolean = false,
    initialValueDay: ValueDayEntity? = null,
    categories: List<String>,
    onDismiss: () -> Unit,
    onAdd: (itemName: String, price: Double, date: LocalDate, category: String, warrantyEndDate: LocalDate?) -> Unit,
    onAddCategory: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var itemName by remember { mutableStateOf(initialValueDay?.itemName ?: "") }
    var priceText by remember { mutableStateOf(initialValueDay?.purchasePrice?.toString() ?: "") }
    var selectedDate by remember { mutableStateOf(initialValueDay?.getPurchaseDateAsLocalDate() ?: LocalDate.now()) }
    var category by remember { mutableStateOf(initialValueDay?.category ?: "未分类") }
    var showCategoryDialog by remember { mutableStateOf(false) }
    var customCategory by remember { mutableStateOf("") }
    
    // 保修相关状态
    var hasWarranty by remember { mutableStateOf(initialValueDay?.warrantyEndDate != null) }
    var warrantyYears by remember { mutableStateOf("") }
    var warrantyEndDate by remember { mutableStateOf<LocalDate?>(initialValueDay?.getWarrantyEndDateAsLocalDate()) }
    var warrantyInputMode by remember { mutableStateOf<WarrantyInputMode>(WarrantyInputMode.YEARS) }
    
    var showPurchaseDatePicker by remember { mutableStateOf(false) }
    var showWarrantyDatePicker by remember { mutableStateOf(false) }
    
    val dateFormatter = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd") }
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = if (isEdit) "编辑买断记录" else "添加买断记录",
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
            
            // 分类选择
            OutlinedTextField(
                value = category,
                onValueChange = {},
                label = { Text("分类") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { showCategoryDialog = true }) {
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "选择分类")
                    }
                },
                leadingIcon = {
                    Icon(Icons.Default.Category, contentDescription = null)
                }
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 购买价格
            OutlinedTextField(
                value = priceText,
                onValueChange = { 
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
                    IconButton(onClick = { showPurchaseDatePicker = true }) {
                        Icon(Icons.Default.CalendarToday, contentDescription = "选择日期")
                    }
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 保修信息（可选）
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "保修信息",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        Switch(
                            checked = hasWarranty,
                            onCheckedChange = { 
                                hasWarranty = it
                                if (!it) {
                                    warrantyEndDate = null
                                    warrantyYears = ""
                                }
                            }
                        )
                    }
                    
                    if (hasWarranty) {
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // 保修输入方式选择
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = warrantyInputMode == WarrantyInputMode.YEARS,
                                onClick = { warrantyInputMode = WarrantyInputMode.YEARS },
                                label = { Text("输入年限") },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = warrantyInputMode == WarrantyInputMode.DATE,
                                onClick = { warrantyInputMode = WarrantyInputMode.DATE },
                                label = { Text("选择到期日") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        when (warrantyInputMode) {
                            WarrantyInputMode.YEARS -> {
                                OutlinedTextField(
                                    value = warrantyYears,
                                    onValueChange = { 
                                        if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) {
                                            warrantyYears = it
                                            // 根据年限自动计算到期日
                                            it.toDoubleOrNull()?.let { years ->
                                                val days = (years * 365).toLong()
                                                warrantyEndDate = selectedDate.plusDays(days)
                                            }
                                        }
                                    },
                                    label = { Text("保修年限") },
                                    modifier = Modifier.fillMaxWidth(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    suffix = { Text("年") },
                                    singleLine = true,
                                    supportingText = {
                                        warrantyEndDate?.let {
                                            Text(
                                                text = "到期日：${it.format(dateFormatter)}",
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        }
                                    }
                                )
                            }
                            WarrantyInputMode.DATE -> {
                                OutlinedTextField(
                                    value = warrantyEndDate?.format(dateFormatter) ?: "",
                                    onValueChange = {},
                                    label = { Text("保修到期日") },
                                    modifier = Modifier.fillMaxWidth(),
                                    readOnly = true,
                                    trailingIcon = {
                                        IconButton(onClick = { showWarrantyDatePicker = true }) {
                                            Icon(Icons.Default.CalendarToday, contentDescription = "选择日期")
                                        }
                                    },
                                    placeholder = { Text("点击选择日期") }
                                )
                            }
                        }
                    }
                }
            }
            
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
                            onAdd(
                                itemName, 
                                price, 
                                selectedDate, 
                                category,
                                if (hasWarranty) warrantyEndDate else null
                            )
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = itemName.isNotBlank() && 
                              priceText.toDoubleOrNull()?.let { it > 0 } == true &&
                              (!hasWarranty || warrantyEndDate != null)
                ) {
                    Text(if (isEdit) "保存" else "添加")
                }
            }
        }
    }
    
    // 分类选择对话框
    if (showCategoryDialog) {
        AlertDialog(
            onDismissRequest = { showCategoryDialog = false },
            title = { Text("选择分类") },
            text = {
                Column {
                    // 现有分类列表
                    categories.forEach { cat ->
                        TextButton(
                            onClick = {
                                category = cat
                                showCategoryDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = cat,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    // 自定义分类
                    OutlinedTextField(
                        value = customCategory,
                        onValueChange = { customCategory = it },
                        label = { Text("新建分类") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Button(
                        onClick = {
                            if (customCategory.isNotBlank()) {
                                onAddCategory(customCategory)
                                category = customCategory
                                customCategory = ""
                                showCategoryDialog = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = customCategory.isNotBlank()
                    ) {
                        Text("创建并使用")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showCategoryDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
    
    // 购买日期选择器
    if (showPurchaseDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.toEpochDay() * 24 * 60 * 60 * 1000
        )
        
        DatePickerDialog(
            onDismissRequest = { showPurchaseDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            selectedDate = LocalDate.ofEpochDay(millis / (24 * 60 * 60 * 1000))
                            // 如果是按年限输入保修，需要重新计算到期日
                            if (hasWarranty && warrantyInputMode == WarrantyInputMode.YEARS) {
                                warrantyYears.toDoubleOrNull()?.let { years ->
                                    val days = (years * 365).toLong()
                                    warrantyEndDate = selectedDate.plusDays(days)
                                }
                            }
                        }
                        showPurchaseDatePicker = false
                    }
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPurchaseDatePicker = false }) {
                    Text("取消")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
    
    // 保修到期日期选择器
    if (showWarrantyDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = warrantyEndDate?.toEpochDay()?.times(24 * 60 * 60 * 1000)
                ?: selectedDate.plusYears(1).toEpochDay() * 24 * 60 * 60 * 1000
        )
        
        DatePickerDialog(
            onDismissRequest = { showWarrantyDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            warrantyEndDate = LocalDate.ofEpochDay(millis / (24 * 60 * 60 * 1000))
                        }
                        showWarrantyDatePicker = false
                    }
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showWarrantyDatePicker = false }) {
                    Text("取消")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

private enum class WarrantyInputMode {
    YEARS,  // 输入保修年限
    DATE    // 直接选择到期日期
}
