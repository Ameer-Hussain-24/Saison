package takagi.ru.saison.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import takagi.ru.saison.data.local.datastore.PreferencesManager
import takagi.ru.saison.data.repository.SyncRepository
import takagi.ru.saison.ui.theme.ThemeManager
import javax.inject.Inject

// UI State
sealed class SettingsUiState {
    object Idle : SettingsUiState()
    object Loading : SettingsUiState()
    data class Success(val message: String) : SettingsUiState()
    data class Error(val message: String) : SettingsUiState()
}

// UI Events
sealed class SettingsUiEvent {
    data class ShowSnackbar(val message: String, val isError: Boolean = false) : SettingsUiEvent()
    data class ShowError(val message: String) : SettingsUiEvent()
    object NavigateToSystemSettings : SettingsUiEvent()
    object RestartRequired : SettingsUiEvent()
}

// Sync Status
data class SyncStatus(
    val isSyncing: Boolean = false,
    val lastSyncTime: Long? = null,
    val lastSyncSuccess: Boolean = true,
    val errorMessage: String? = null
)

// Notification Settings
data class NotificationSettings(
    val notificationsEnabled: Boolean = true,
    val taskRemindersEnabled: Boolean = true,
    val courseRemindersEnabled: Boolean = true,
    val pomodoroRemindersEnabled: Boolean = true
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val themeManager: ThemeManager,
    private val syncRepository: SyncRepository
) : ViewModel() {
    
    // UI State
    private val _uiState = MutableStateFlow<SettingsUiState>(SettingsUiState.Idle)
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
    
    // UI Events
    private val _uiEvent = MutableSharedFlow<SettingsUiEvent>()
    val uiEvent: SharedFlow<SettingsUiEvent> = _uiEvent.asSharedFlow()
    
    // Theme Settings
    val currentTheme = themeManager.currentTheme
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), takagi.ru.saison.data.local.datastore.SeasonalTheme.DYNAMIC)
    
    val themeMode = themeManager.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), takagi.ru.saison.data.local.datastore.ThemeMode.FOLLOW_SYSTEM)
    
    val useDynamicColor = preferencesManager.themePreferences
        .map { it.useDynamicColor }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    
    // Language Settings
    val currentLanguage = preferencesManager.language
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "zh")
    
    // Notification Settings
    val notificationsEnabled = preferencesManager.notificationsEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    
    val taskRemindersEnabled = preferencesManager.taskRemindersEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    
    val courseRemindersEnabled = preferencesManager.courseRemindersEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    
    val pomodoroRemindersEnabled = preferencesManager.pomodoroRemindersEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    
    val notificationSettings = combine(
        notificationsEnabled,
        taskRemindersEnabled,
        courseRemindersEnabled,
        pomodoroRemindersEnabled
    ) { notifications, task, course, pomodoro ->
        NotificationSettings(notifications, task, course, pomodoro)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        NotificationSettings()
    )
    
    // Sync Settings
    val autoSyncEnabled = preferencesManager.autoSyncEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    
    val syncOnWifiOnly = preferencesManager.syncOnWifiOnly
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    
    private val _syncStatus = MutableStateFlow(SyncStatus())
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()
    
    // Pomodoro Settings
    val pomodoroWorkDuration = preferencesManager.pomodoroWorkDuration
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 25)
    
    val pomodoroBreakDuration = preferencesManager.pomodoroBreakDuration
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 5)
    
    val pomodoroLongBreakDuration = preferencesManager.pomodoroLongBreakDuration
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 15)
    
    // Metronome Settings
    val metronomeDefaultBpm = preferencesManager.metronomeDefaultBpm
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 120)
    
    val metronomeSound = preferencesManager.metronomeSound
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "WOODBLOCK")
    
    // Theme Settings Functions
    fun setTheme(theme: takagi.ru.saison.data.local.datastore.SeasonalTheme) {
        viewModelScope.launch {
            try {
                _uiState.value = SettingsUiState.Loading
                themeManager.setTheme(theme)
                _uiState.value = SettingsUiState.Idle
                // 移除提示消息，主题切换已经有视觉反馈
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }
    
    fun setThemeMode(mode: takagi.ru.saison.data.local.datastore.ThemeMode) {
        viewModelScope.launch {
            try {
                themeManager.setThemeMode(mode)
                // 移除提示消息，主题模式切换已经有视觉反馈
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }
    
    fun setUseDynamicColor(enabled: Boolean) {
        viewModelScope.launch {
            try {
                preferencesManager.setUseDynamicColor(enabled)
                // 移除提示消息，动态颜色切换已经有视觉反馈
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }
    
    // Language Settings Functions
    fun setLanguage(language: String) {
        viewModelScope.launch {
            try {
                preferencesManager.setLanguage(language)
                // 发送重新创建 Activity 的事件
                _uiEvent.emit(SettingsUiEvent.RestartRequired)
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }
    
    // Notification Settings Functions
    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            try {
                preferencesManager.setNotificationsEnabled(enabled)
                _uiEvent.emit(SettingsUiEvent.ShowSnackbar(if (enabled) "已启用通知" else "已禁用通知"))
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }
    
    fun setTaskRemindersEnabled(enabled: Boolean) {
        viewModelScope.launch {
            try {
                preferencesManager.setTaskRemindersEnabled(enabled)
                _uiEvent.emit(SettingsUiEvent.ShowSnackbar(if (enabled) "已启用任务提醒" else "已禁用任务提醒"))
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }
    
    fun setCourseRemindersEnabled(enabled: Boolean) {
        viewModelScope.launch {
            try {
                preferencesManager.setCourseRemindersEnabled(enabled)
                _uiEvent.emit(SettingsUiEvent.ShowSnackbar(if (enabled) "已启用课程提醒" else "已禁用课程提醒"))
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }
    
    fun setPomodoroRemindersEnabled(enabled: Boolean) {
        viewModelScope.launch {
            try {
                preferencesManager.setPomodoroRemindersEnabled(enabled)
                _uiEvent.emit(SettingsUiEvent.ShowSnackbar(if (enabled) "已启用番茄钟提醒" else "已禁用番茄钟提醒"))
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }
    
    // Sync Settings Functions
    fun setAutoSyncEnabled(enabled: Boolean) {
        viewModelScope.launch {
            try {
                preferencesManager.setAutoSyncEnabled(enabled)
                _uiEvent.emit(SettingsUiEvent.ShowSnackbar(if (enabled) "已启用自动同步" else "已禁用自动同步"))
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }
    
    fun setSyncOnWifiOnly(enabled: Boolean) {
        viewModelScope.launch {
            try {
                preferencesManager.setSyncOnWifiOnly(enabled)
                _uiEvent.emit(SettingsUiEvent.ShowSnackbar(if (enabled) "仅在 WiFi 下同步" else "允许移动网络同步"))
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }
    
    // WebDAV Configuration Functions
    fun setWebDavConfig(url: String, username: String, password: String) {
        viewModelScope.launch {
            try {
                _uiState.value = SettingsUiState.Loading
                preferencesManager.setWebDavConfig(url, username, password)
                _uiState.value = SettingsUiState.Success("WebDAV 配置已保存")
                _uiEvent.emit(SettingsUiEvent.ShowSnackbar("WebDAV 配置已保存"))
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }
    
    fun clearWebDavConfig() {
        viewModelScope.launch {
            try {
                preferencesManager.clearWebDavConfig()
                _uiEvent.emit(SettingsUiEvent.ShowSnackbar("WebDAV 配置已清除"))
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }
    
    // Pomodoro Settings Functions
    fun setPomodoroWorkDuration(minutes: Int) {
        viewModelScope.launch {
            try {
                preferencesManager.setPomodoroWorkDuration(minutes)
                _uiEvent.emit(SettingsUiEvent.ShowSnackbar("工作时长已更新为 $minutes 分钟"))
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }
    
    fun setPomodoroBreakDuration(minutes: Int) {
        viewModelScope.launch {
            try {
                preferencesManager.setPomodoroBreakDuration(minutes)
                _uiEvent.emit(SettingsUiEvent.ShowSnackbar("短休息时长已更新为 $minutes 分钟"))
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }
    
    fun setPomodoroLongBreakDuration(minutes: Int) {
        viewModelScope.launch {
            try {
                preferencesManager.setPomodoroLongBreakDuration(minutes)
                _uiEvent.emit(SettingsUiEvent.ShowSnackbar("长休息时长已更新为 $minutes 分钟"))
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }
    
    // Metronome Settings Functions
    fun setMetronomeDefaultBpm(bpm: Int) {
        viewModelScope.launch {
            try {
                preferencesManager.setMetronomeDefaultBpm(bpm)
                _uiEvent.emit(SettingsUiEvent.ShowSnackbar("默认 BPM 已更新为 $bpm"))
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }
    
    fun setMetronomeSound(sound: String) {
        viewModelScope.launch {
            try {
                preferencesManager.setMetronomeSound(sound)
                _uiEvent.emit(SettingsUiEvent.ShowSnackbar("节拍器声音已更新"))
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }
    
    // WebDAV Connection Test
    fun testWebDavConnection(url: String, username: String, password: String) {
        viewModelScope.launch {
            try {
                _uiState.value = SettingsUiState.Loading
                val isConnected = syncRepository.testConnection(url, username, password)
                if (isConnected) {
                    _uiState.value = SettingsUiState.Success("连接成功")
                    _uiEvent.emit(SettingsUiEvent.ShowSnackbar("WebDAV 连接测试成功"))
                } else {
                    _uiState.value = SettingsUiState.Error("连接失败")
                    _uiEvent.emit(SettingsUiEvent.ShowError("无法连接到 WebDAV 服务器"))
                }
            } catch (e: Exception) {
                _uiState.value = SettingsUiState.Error("连接失败: ${e.message}")
                _uiEvent.emit(SettingsUiEvent.ShowError("连接测试失败: ${e.message}"))
            }
        }
    }
    
    // Manual Sync
    fun triggerManualSync() {
        viewModelScope.launch {
            try {
                _syncStatus.value = _syncStatus.value.copy(isSyncing = true, errorMessage = null)
                _uiEvent.emit(SettingsUiEvent.ShowSnackbar("开始同步..."))
                
                val result = syncRepository.syncTasks()
                
                when (result) {
                    is takagi.ru.saison.data.repository.SyncResult.Success -> {
                        _syncStatus.value = SyncStatus(
                            isSyncing = false,
                            lastSyncTime = System.currentTimeMillis(),
                            lastSyncSuccess = true,
                            errorMessage = null
                        )
                        _uiEvent.emit(
                            SettingsUiEvent.ShowSnackbar(
                                "同步成功：已同步 ${result.syncedCount} 个任务" +
                                        if (result.conflictsResolved > 0) "，解决 ${result.conflictsResolved} 个冲突" else ""
                            )
                        )
                    }
                    is takagi.ru.saison.data.repository.SyncResult.NoChanges -> {
                        _syncStatus.value = SyncStatus(
                            isSyncing = false,
                            lastSyncTime = System.currentTimeMillis(),
                            lastSyncSuccess = true,
                            errorMessage = null
                        )
                        _uiEvent.emit(SettingsUiEvent.ShowSnackbar("同步完成：无需更新"))
                    }
                    is takagi.ru.saison.data.repository.SyncResult.NotConfigured -> {
                        _syncStatus.value = _syncStatus.value.copy(
                            isSyncing = false,
                            errorMessage = "未配置 WebDAV"
                        )
                        _uiEvent.emit(SettingsUiEvent.ShowError("请先配置 WebDAV 服务器"))
                    }
                    is takagi.ru.saison.data.repository.SyncResult.Error -> {
                        _syncStatus.value = SyncStatus(
                            isSyncing = false,
                            lastSyncTime = System.currentTimeMillis(),
                            lastSyncSuccess = false,
                            errorMessage = result.message
                        )
                        _uiEvent.emit(SettingsUiEvent.ShowError("同步失败: ${result.message}"))
                    }
                }
            } catch (e: Exception) {
                _syncStatus.value = SyncStatus(
                    isSyncing = false,
                    lastSyncTime = System.currentTimeMillis(),
                    lastSyncSuccess = false,
                    errorMessage = e.message
                )
                _uiEvent.emit(SettingsUiEvent.ShowError("同步失败: ${e.message}"))
            }
        }
    }
    
    // Error Handling
    private suspend fun handleError(exception: Exception) {
        _uiState.value = SettingsUiState.Error(exception.message ?: "未知错误")
        _uiEvent.emit(SettingsUiEvent.ShowError(exception.message ?: "操作失败"))
    }
    
    // Bottom Navigation Settings
    val bottomNavVisibility = preferencesManager.bottomNavVisibility
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            takagi.ru.saison.data.local.datastore.BottomNavVisibility()
        )
    
    val bottomNavOrder = preferencesManager.bottomNavOrder
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            takagi.ru.saison.data.local.datastore.BottomNavTab.DEFAULT_ORDER
        )
    
    fun updateBottomNavVisibility(tab: takagi.ru.saison.data.local.datastore.BottomNavTab, visible: Boolean) {
        viewModelScope.launch {
            try {
                preferencesManager.updateBottomNavVisibility(tab, visible)
                _uiEvent.emit(SettingsUiEvent.ShowSnackbar("已更新导航栏设置"))
            } catch (e: Exception) {
                _uiEvent.emit(SettingsUiEvent.ShowError("更新失败: ${e.message}"))
            }
        }
    }
    
    fun updateBottomNavOrder(order: List<takagi.ru.saison.data.local.datastore.BottomNavTab>) {
        viewModelScope.launch {
            try {
                preferencesManager.updateBottomNavOrder(order)
                _uiEvent.emit(SettingsUiEvent.ShowSnackbar("已更新导航栏顺序"))
            } catch (e: Exception) {
                _uiEvent.emit(SettingsUiEvent.ShowError("更新失败: ${e.message}"))
            }
        }
    }
}
