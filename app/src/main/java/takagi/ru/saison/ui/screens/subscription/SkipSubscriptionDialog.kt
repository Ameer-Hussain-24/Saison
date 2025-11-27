package takagi.ru.saison.ui.screens.subscription

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import takagi.ru.saison.R
import takagi.ru.saison.data.local.database.entities.SubscriptionEntity
import takagi.ru.saison.domain.model.SkipDuration
import takagi.ru.saison.domain.model.SkipUnit
import takagi.ru.saison.util.SubscriptionDateCalculator
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkipSubscriptionDialog(
    subscription: SubscriptionEntity?,
    onDismiss: () -> Unit,
    onConfirm: (SkipDuration) -> Unit
) {
    if (subscription == null) {
        onDismiss()
        return
    }
    
    var selectedPreset by remember { mutableStateOf<SkipPreset?>(null) }
    var customAmount by remember { mutableStateOf("1") }
    var customUnit by remember { mutableStateOf(SkipUnit.MONTHS) }
    var showUnitDropdown by remember { mutableStateOf(false) }
    
    val currentRenewalDate = remember(subscription) {
        Instant.ofEpochMilli(subscription.nextRenewalDate)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
    }
    
    val previewDate = remember(selectedPreset, customAmount, customUnit) {
        val duration = when {
            selectedPreset != null -> selectedPreset!!.duration
            customAmount.toIntOrNull() != null -> SkipDuration(customAmount.toInt(), customUnit)
            else -> null
        }
        
        duration?.let {
            SubscriptionDateCalculator.calculateSkippedRenewalDate(currentRenewalDate, it)
        }
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.subscription_action_skip)) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.subscription_skip_description),
                    style = MaterialTheme.typography.bodyMedium
                )
                
                // 提示：跳过期间不计费
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = stringResource(R.string.subscription_skip_no_charge_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                
                // 预设选项
                Text(
                    text = stringResource(R.string.subscription_skip_presets),
                    style = MaterialTheme.typography.titleSmall
                )
                
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SkipPreset.values().forEach { preset ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = selectedPreset == preset,
                                    onClick = { selectedPreset = preset }
                                )
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedPreset == preset,
                                onClick = { selectedPreset = preset }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(preset.label)
                        }
                    }
                }
                
                Divider()
                
                // 自定义选项
                Text(
                    text = stringResource(R.string.subscription_skip_custom),
                    style = MaterialTheme.typography.titleSmall
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedPreset == null,
                        onClick = { selectedPreset = null }
                    )
                    
                    OutlinedTextField(
                        value = customAmount,
                        onValueChange = { 
                            if (it.all { char -> char.isDigit() }) {
                                customAmount = it
                                selectedPreset = null
                            }
                        },
                        label = { Text(stringResource(R.string.subscription_skip_amount)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = getUnitText(customUnit),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(stringResource(R.string.subscription_skip_unit)) },
                            trailingIcon = {
                                IconButton(onClick = { 
                                    showUnitDropdown = true
                                    selectedPreset = null
                                }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        DropdownMenu(
                            expanded = showUnitDropdown,
                            onDismissRequest = { showUnitDropdown = false }
                        ) {
                            SkipUnit.values().forEach { unit ->
                                DropdownMenuItem(
                                    text = { Text(getUnitText(unit)) },
                                    onClick = {
                                        customUnit = unit
                                        showUnitDropdown = false
                                        selectedPreset = null
                                    }
                                )
                            }
                        }
                    }
                }
                
                // 预览
                if (previewDate != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.subscription_skip_preview_title),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = previewDate.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日")),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val duration = when {
                        selectedPreset != null -> selectedPreset!!.duration
                        customAmount.toIntOrNull() != null -> SkipDuration(customAmount.toInt(), customUnit)
                        else -> null
                    }
                    
                    duration?.let {
                        onConfirm(it)
                        onDismiss()
                    }
                },
                enabled = selectedPreset != null || customAmount.toIntOrNull() != null
            ) {
                Text(stringResource(R.string.subscription_confirm_button))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.subscription_cancel_button))
            }
        }
    )
}

private enum class SkipPreset(val label: String, val duration: SkipDuration) {
    ONE_DAY("跳过1天", SkipDuration(1, SkipUnit.DAYS)),
    ONE_MONTH("跳过1个月", SkipDuration(1, SkipUnit.MONTHS)),
    ONE_YEAR("跳过1年", SkipDuration(1, SkipUnit.YEARS))
}

@Composable
private fun getUnitText(unit: SkipUnit): String {
    return when (unit) {
        SkipUnit.DAYS -> stringResource(R.string.subscription_skip_days)
        SkipUnit.MONTHS -> stringResource(R.string.subscription_skip_months)
        SkipUnit.YEARS -> stringResource(R.string.subscription_skip_years)
    }
}
