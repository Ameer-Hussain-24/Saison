package takagi.ru.saison.ui.screens.metronome

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import takagi.ru.saison.data.local.datastore.MetronomePreferencesManager
import takagi.ru.saison.domain.model.MetronomePreset
import takagi.ru.saison.util.HapticFeedbackManager
import takagi.ru.saison.util.MetronomeSoundManager
import javax.inject.Inject

@HiltViewModel
class MetronomeViewModel @Inject constructor(
    private val soundManager: MetronomeSoundManager,
    private val hapticManager: HapticFeedbackManager,
    private val preferencesManager: MetronomePreferencesManager
) : ViewModel() {
    
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()
    
    private val _bpm = MutableStateFlow(120)
    val bpm: StateFlow<Int> = _bpm.asStateFlow()
    
    private val _beatCount = MutableStateFlow(0)
    val beatCount: StateFlow<Int> = _beatCount.asStateFlow()
    
    private val _timeSignature = MutableStateFlow(4) // 4/4 time
    val timeSignature: StateFlow<Int> = _timeSignature.asStateFlow()
    
    private val _currentBeat = MutableStateFlow(0)
    val currentBeat: StateFlow<Int> = _currentBeat.asStateFlow()
    
    private val _volume = MutableStateFlow(0.8f)
    val volume: StateFlow<Float> = _volume.asStateFlow()
    
    private val _accentFirstBeat = MutableStateFlow(true)
    val accentFirstBeat: StateFlow<Boolean> = _accentFirstBeat.asStateFlow()
    
    private val _enableVibration = MutableStateFlow(true)
    val enableVibration: StateFlow<Boolean> = _enableVibration.asStateFlow()
    
    private val _beatsPerMeasure = MutableStateFlow(4)
    val beatsPerMeasure: StateFlow<Int> = _beatsPerMeasure.asStateFlow()
    
    private val _soundType = MutableStateFlow(MetronomeSound.CLICK)
    val soundType: StateFlow<MetronomeSound> = _soundType.asStateFlow()
    
    // Tap Tempo 相关
    private val tapTimes = mutableListOf<Long>()
    private val _tapTempoCount = MutableStateFlow(0)
    val tapTempoCount: StateFlow<Int> = _tapTempoCount.asStateFlow()
    
    // 保存的预设
    val savedPresets: StateFlow<List<MetronomePreset>> = 
        preferencesManager.savedPresets.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    private var metronomeJob: Job? = null
    
    init {
        // 加载最后使用的设置
        loadLastSettings()
    }
    
    private fun loadLastSettings() {
        viewModelScope.launch {
            preferencesManager.lastBpm.collect { _bpm.value = it }
        }
        viewModelScope.launch {
            preferencesManager.lastTimeSignature.collect { _timeSignature.value = it }
        }
        viewModelScope.launch {
            preferencesManager.lastVolume.collect { _volume.value = it }
        }
        viewModelScope.launch {
            preferencesManager.lastAccentFirstBeat.collect { _accentFirstBeat.value = it }
        }
        viewModelScope.launch {
            preferencesManager.lastEnableVibration.collect { _enableVibration.value = it }
        }
    }
    
    // 预设 BPM
    val bpmPresets = listOf(
        "Largo" to 60,
        "Adagio" to 72,
        "Andante" to 92,
        "Moderato" to 108,
        "Allegro" to 132,
        "Presto" to 168
    )
    
    fun setBpm(newBpm: Int) {
        _bpm.value = newBpm.coerceIn(30, 240)
    }
    
    fun setTimeSignature(signature: Int) {
        _timeSignature.value = signature
        _currentBeat.value = 0
    }
    
    fun setVolume(newVolume: Float) {
        _volume.value = newVolume.coerceIn(0f, 1f)
    }
    
    fun setAccentFirstBeat(accent: Boolean) {
        _accentFirstBeat.value = accent
    }
    
    fun setEnableVibration(enable: Boolean) {
        _enableVibration.value = enable
    }
    
    fun setBeatsPerMeasure(beats: Int) {
        _beatsPerMeasure.value = beats.coerceIn(2, 8)
    }
    
    fun setSoundType(sound: MetronomeSound) {
        _soundType.value = sound
    }
    
    fun togglePlayPause() {
        if (_isPlaying.value) {
            stop()
        } else {
            start()
        }
    }
    
    private fun start() {
        _isPlaying.value = true
        _beatCount.value = 0
        _currentBeat.value = 0
        
        metronomeJob = viewModelScope.launch {
            while (_isPlaying.value) {
                val intervalMs = (60000 / _bpm.value).toLong()
                
                // 播放节拍音效
                playBeatSound()
                
                // 更新计数
                _beatCount.value += 1
                _currentBeat.value = (_currentBeat.value + 1) % _timeSignature.value
                
                delay(intervalMs)
            }
        }
    }
    
    fun stop() {
        _isPlaying.value = false
        _beatCount.value = 0
        _currentBeat.value = 0
        metronomeJob?.cancel()
    }
    
    fun tapTempo() {
        val currentTime = System.currentTimeMillis()
        tapTimes.add(currentTime)
        _tapTempoCount.value = tapTimes.size
        
        // 触觉反馈
        if (_enableVibration.value) {
            hapticManager.performLightTap()
        }
        
        // 只保留最近 4 次点击
        if (tapTimes.size > 4) {
            tapTimes.removeAt(0)
        }
        
        // 清除超过 2 秒的旧点击
        tapTimes.removeAll { currentTime - it > 2000 }
        
        // 至少需要 2 次点击才能计算 BPM
        if (tapTimes.size >= 2) {
            val intervals = mutableListOf<Long>()
            for (i in 1 until tapTimes.size) {
                intervals.add(tapTimes[i] - tapTimes[i - 1])
            }
            
            // 计算平均间隔
            val avgInterval = intervals.average()
            
            // 转换为 BPM
            val calculatedBpm = (60000 / avgInterval).toInt()
            
            // 设置新的 BPM
            setBpm(calculatedBpm)
        }
        
        // 3 秒后重置计数
        viewModelScope.launch {
            delay(3000)
            if (System.currentTimeMillis() - currentTime >= 3000) {
                tapTimes.clear()
                _tapTempoCount.value = 0
            }
        }
    }
    
    private fun playBeatSound() {
        val isAccent = _accentFirstBeat.value && _currentBeat.value == 0
        
        // 播放音效
        if (isAccent) {
            soundManager.playAccentBeat(_volume.value)
        } else {
            soundManager.playNormalBeat(_volume.value)
        }
        
        // 触觉反馈
        if (_enableVibration.value) {
            if (isAccent) {
                hapticManager.performMediumTap()
            } else {
                hapticManager.performLightTap()
            }
        }
    }
    
    // 保存当前设置为预设
    fun saveCurrentAsPreset(name: String) {
        viewModelScope.launch {
            val preset = MetronomePreset(
                name = name,
                bpm = _bpm.value,
                timeSignature = _timeSignature.value,
                volume = _volume.value,
                accentFirstBeat = _accentFirstBeat.value,
                enableVibration = _enableVibration.value
            )
            preferencesManager.savePreset(preset)
        }
    }
    
    // 加载预设
    fun loadPreset(preset: MetronomePreset) {
        _bpm.value = preset.bpm
        _timeSignature.value = preset.timeSignature
        _volume.value = preset.volume
        _accentFirstBeat.value = preset.accentFirstBeat
        _enableVibration.value = preset.enableVibration
        _currentBeat.value = 0
    }
    
    // 删除预设
    fun deletePreset(presetId: String) {
        viewModelScope.launch {
            preferencesManager.deletePreset(presetId)
        }
    }
    
    // 保存当前设置
    private fun saveCurrentSettings() {
        viewModelScope.launch {
            preferencesManager.saveLastSettings(
                bpm = _bpm.value,
                timeSignature = _timeSignature.value,
                volume = _volume.value,
                accentFirstBeat = _accentFirstBeat.value,
                enableVibration = _enableVibration.value
            )
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        metronomeJob?.cancel()
        soundManager.release()
        saveCurrentSettings()
    }
}

enum class MetronomeSound {
    WOODBLOCK,
    CLICK,
    BEEP
}
