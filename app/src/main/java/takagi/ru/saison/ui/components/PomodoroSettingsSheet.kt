package takagi.ru.saison.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import takagi.ru.saison.R
import takagi.ru.saison.ui.screens.pomodoro.PomodoroSettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PomodoroSettingsSheet(
    settings: PomodoroSettings,
    onSettingsChange: (PomodoroSettings) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var tempSettings by remember { mutableStateOf(settings) }
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 标题
            Text(
                text = stringResource(R.string.pomodoro_settings_dialog_title),
                style = MaterialTheme.typography.titleLarge
            )
            
            // 工作时长
            SettingSlider(
                label = stringResource(R.string.pomodoro_work_duration_label),
                value = tempSettings.workDuration,
                valueRange = 15f..60f,
                steps = 8,
                onValueChange = { tempSettings = tempSettings.copy(workDuration = it.toInt()) }
            )
            
            // 短休息时长
            SettingSlider(
                label = stringResource(R.string.pomodoro_short_break_label),
                value = tempSettings.shortBreakDuration,
                valueRange = 3f..15f,
                steps = 11,
                onValueChange = { tempSettings = tempSettings.copy(shortBreakDuration = it.toInt()) }
            )
            
            // 长休息时长
            SettingSlider(
                label = stringResource(R.string.pomodoro_long_break_label),
                value = tempSettings.longBreakDuration,
                valueRange = 10f..30f,
                steps = 19,
                onValueChange = { tempSettings = tempSettings.copy(longBreakDuration = it.toInt()) }
            )
            
            Divider()
            
            // 声音提醒
            SettingSwitch(
                label = stringResource(R.string.pomodoro_settings_sound),
                checked = tempSettings.soundEnabled,
                onCheckedChange = { tempSettings = tempSettings.copy(soundEnabled = it) }
            )
            
            // 震动提醒
            SettingSwitch(
                label = stringResource(R.string.pomodoro_settings_vibration),
                checked = tempSettings.vibrationEnabled,
                onCheckedChange = { tempSettings = tempSettings.copy(vibrationEnabled = it) }
            )
            
            // 保存按钮
            Button(
                onClick = {
                    onSettingsChange(tempSettings)
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.common_action_confirm))
            }
        }
    }
}

@Composable
private fun SettingSlider(
    label: String,
    value: Int,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    onValueChange: (Float) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "$value 分钟",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        Slider(
            value = value.toFloat(),
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps
        )
    }
}

@Composable
private fun SettingSwitch(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge
        )
        
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
