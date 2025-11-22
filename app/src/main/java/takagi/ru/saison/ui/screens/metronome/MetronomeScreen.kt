package takagi.ru.saison.ui.screens.metronome

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import takagi.ru.saison.R
import takagi.ru.saison.ui.components.EnhancedBeatVisualizer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MetronomeScreen(
    viewModel: MetronomeViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val isPlaying by viewModel.isPlaying.collectAsState()
    val bpm by viewModel.bpm.collectAsState()
    val beatCount by viewModel.beatCount.collectAsState()
    val timeSignature by viewModel.timeSignature.collectAsState()
    val currentBeat by viewModel.currentBeat.collectAsState()
    val volume by viewModel.volume.collectAsState()
    val accentFirstBeat by viewModel.accentFirstBeat.collectAsState()
    val enableVibration by viewModel.enableVibration.collectAsState()
    val tapTempoCount by viewModel.tapTempoCount.collectAsState()
    val savedPresets by viewModel.savedPresets.collectAsState()
    
    var showSettings by remember { mutableStateOf(false) }
    var showPresets by remember { mutableStateOf(false) }
    var showSavePresetDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.metronome_title)) },
                actions = {
                    IconButton(onClick = { showPresets = true }) {
                        Icon(Icons.Default.Bookmark, contentDescription = stringResource(R.string.metronome_presets))
                    }
                    IconButton(onClick = { showSavePresetDialog = true }) {
                        Icon(Icons.Default.Save, contentDescription = stringResource(R.string.metronome_save_preset))
                    }
                    IconButton(onClick = { showSettings = true }) {
                        Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.cd_settings))
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            // 节拍可视化指示器
            EnhancedBeatVisualizer(
                isPlaying = isPlaying,
                bpm = bpm,
                currentBeat = currentBeat,
                timeSignature = timeSignature
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Tack 风格的 BPM 显示和调整按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 左侧调整按钮
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = { viewModel.setBpm(bpm - 1) },
                        enabled = bpm > 30
                    ) {
                        Icon(Icons.Default.ChevronLeft, stringResource(R.string.metronome_decrease_1), modifier = Modifier.size(32.dp))
                    }
                    IconButton(
                        onClick = { viewModel.setBpm(bpm - 5) },
                        enabled = bpm > 35
                    ) {
                        Icon(Icons.Default.KeyboardDoubleArrowLeft, stringResource(R.string.metronome_decrease_5), modifier = Modifier.size(28.dp))
                    }
                    IconButton(
                        onClick = { viewModel.setBpm(bpm - 10) },
                        enabled = bpm > 40
                    ) {
                        Icon(Icons.Default.FastRewind, "减少 10", modifier = Modifier.size(24.dp))
                    }
                }
                
                Spacer(modifier = Modifier.width(24.dp))
                
                // 中央圆形徽章
                Card(
                    modifier = Modifier.size(200.dp),
                    shape = androidx.compose.foundation.shape.CircleShape,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // 速度标签
                            Text(
                                text = getTempoLabel(bpm),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            // BPM 数字
                            Text(
                                text = "$bpm",
                                style = MaterialTheme.typography.displayLarge.copy(
                                    fontSize = 64.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            // BPM 标签
                            Text(
                                text = "bpm",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.width(24.dp))
                
                // 右侧调整按钮
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = { viewModel.setBpm(bpm + 1) },
                        enabled = bpm < 240
                    ) {
                        Icon(Icons.Default.ChevronRight, "增加 1", modifier = Modifier.size(32.dp))
                    }
                    IconButton(
                        onClick = { viewModel.setBpm(bpm + 5) },
                        enabled = bpm < 235
                    ) {
                        Icon(Icons.Default.KeyboardDoubleArrowRight, "增加 5", modifier = Modifier.size(28.dp))
                    }
                    IconButton(
                        onClick = { viewModel.setBpm(bpm + 10) },
                        enabled = bpm < 230
                    ) {
                        Icon(Icons.Default.FastForward, "增加 10", modifier = Modifier.size(24.dp))
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 精确调节滑块（可选，保持简洁）
            if (!isPlaying) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("30", style = MaterialTheme.typography.labelSmall)
                        Text("240", style = MaterialTheme.typography.labelSmall)
                    }
                    
                    Slider(
                        value = bpm.toFloat(),
                        onValueChange = { viewModel.setBpm(it.toInt()) },
                        valueRange = 30f..240f,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Tack 风格的底部控制栏
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 设置按钮
                IconButton(
                    onClick = { showSettings = true },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = "设置",
                        modifier = Modifier.size(28.dp)
                    )
                }
                
                // Tap Tempo 按钮
                IconButton(
                    onClick = { viewModel.tapTempo() },
                    modifier = Modifier.size(48.dp),
                    enabled = !isPlaying
                ) {
                    Icon(
                        Icons.Default.TouchApp,
                        contentDescription = "Tap Tempo",
                        modifier = Modifier.size(28.dp)
                    )
                }
                
                // 播放/暂停按钮（大圆形按钮）
                FilledIconButton(
                    onClick = { viewModel.togglePlayPause() },
                    modifier = Modifier.size(80.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "暂停" else "播放",
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
                
                // 预设按钮
                IconButton(
                    onClick = { showPresets = true },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        Icons.Default.Bookmark,
                        contentDescription = "预设",
                        modifier = Modifier.size(28.dp)
                    )
                }
                
                // 音量按钮
                IconButton(
                    onClick = { /* 显示音量调节 */ },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        Icons.Default.VolumeUp,
                        contentDescription = "音量",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            
            // 节拍计数和当前拍显示
            if (isPlaying) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "拍号",
                                style = MaterialTheme.typography.labelMedium
                            )
                            Text(
                                text = "$timeSignature/4",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "当前拍",
                                style = MaterialTheme.typography.labelMedium
                            )
                            Text(
                                text = "${currentBeat + 1}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "总节拍",
                                style = MaterialTheme.typography.labelMedium
                            )
                            Text(
                                text = "$beatCount",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
    
    // 设置对话框
    if (showSettings) {
        MetronomeSettingsDialog(
            timeSignature = timeSignature,
            volume = volume,
            accentFirstBeat = accentFirstBeat,
            enableVibration = enableVibration,
            onTimeSignatureChange = { viewModel.setTimeSignature(it) },
            onVolumeChange = { viewModel.setVolume(it) },
            onAccentFirstBeatChange = { viewModel.setAccentFirstBeat(it) },
            onEnableVibrationChange = { viewModel.setEnableVibration(it) },
            onDismiss = { showSettings = false }
        )
    }
    
    // 预设列表对话框
    if (showPresets) {
        PresetsDialog(
            presets = savedPresets,
            onPresetSelected = { preset ->
                viewModel.loadPreset(preset)
                showPresets = false
            },
            onPresetDeleted = { presetId ->
                viewModel.deletePreset(presetId)
            },
            onDismiss = { showPresets = false }
        )
    }
    
    // 保存预设对话框
    if (showSavePresetDialog) {
        SavePresetDialog(
            onSave = { name ->
                viewModel.saveCurrentAsPreset(name)
                showSavePresetDialog = false
            },
            onDismiss = { showSavePresetDialog = false }
        )
    }
}

@Composable
private fun MetronomeSettingsDialog(
    timeSignature: Int,
    volume: Float,
    accentFirstBeat: Boolean,
    enableVibration: Boolean,
    onTimeSignatureChange: (Int) -> Unit,
    onVolumeChange: (Float) -> Unit,
    onAccentFirstBeatChange: (Boolean) -> Unit,
    onEnableVibrationChange: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("节拍器设置") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 拍号设置
                Text("拍号", style = MaterialTheme.typography.titleSmall)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(2, 3, 4, 6).forEach { signature ->
                        FilterChip(
                            selected = timeSignature == signature,
                            onClick = { onTimeSignatureChange(signature) },
                            label = { Text("$signature/4") }
                        )
                    }
                }
                
                // 音量设置
                Text("音量", style = MaterialTheme.typography.titleSmall)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.VolumeDown,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Slider(
                        value = volume,
                        onValueChange = onVolumeChange,
                        valueRange = 0f..1f,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        Icons.Default.VolumeUp,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = "${(volume * 100).toInt()}%",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.align(Alignment.End)
                )
                
                // 重音设置
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("第一拍重音", style = MaterialTheme.typography.titleSmall)
                        Text(
                            "强调每小节的第一拍",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = accentFirstBeat,
                        onCheckedChange = onAccentFirstBeatChange
                    )
                }
                
                // 振动反馈设置
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("振动反馈", style = MaterialTheme.typography.titleSmall)
                        Text(
                            "每次节拍时提供触觉反馈",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = enableVibration,
                        onCheckedChange = onEnableVibrationChange
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


@Composable
private fun PresetsDialog(
    presets: List<takagi.ru.saison.domain.model.MetronomePreset>,
    onPresetSelected: (takagi.ru.saison.domain.model.MetronomePreset) -> Unit,
    onPresetDeleted: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("我的预设") },
        text = {
            if (presets.isEmpty()) {
                Text(
                    text = "还没有保存的预设\n点击顶部保存按钮创建预设",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presets.forEach { preset ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { onPresetSelected(preset) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = preset.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${preset.bpm} BPM • ${preset.timeSignature}/4",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(onClick = { onPresetDeleted(preset.id) }) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "删除",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        }
    )
}

@Composable
private fun SavePresetDialog(
    onSave: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var presetName by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("保存预设") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "为当前设置命名",
                    style = MaterialTheme.typography.bodyMedium
                )
                OutlinedTextField(
                    value = presetName,
                    onValueChange = { presetName = it },
                    label = { Text("预设名称") },
                    placeholder = { Text("例如：练习曲 120") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { 
                    if (presetName.isNotBlank()) {
                        onSave(presetName.trim())
                    }
                },
                enabled = presetName.isNotBlank()
            ) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}


// 根据 BPM 获取速度标签
private fun getTempoLabel(bpm: Int): String {
    return when {
        bpm < 40 -> "极慢板"
        bpm < 60 -> "广板"
        bpm < 66 -> "慢板"
        bpm < 76 -> "柔板"
        bpm < 108 -> "行板"
        bpm < 120 -> "小行板"
        bpm < 168 -> "中板"
        bpm < 176 -> "小快板"
        bpm < 200 -> "快板"
        else -> "急板"
    }
}
