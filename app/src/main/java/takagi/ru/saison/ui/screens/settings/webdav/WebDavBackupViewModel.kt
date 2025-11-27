package takagi.ru.saison.ui.screens.settings.webdav

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import takagi.ru.saison.data.repository.backup.WebDavBackupRepository
import takagi.ru.saison.domain.model.backup.BackupFile
import takagi.ru.saison.domain.model.backup.BackupPreferences
import takagi.ru.saison.domain.model.backup.RestoreSummary
import takagi.ru.saison.domain.model.backup.WebDavConfig
import takagi.ru.saison.domain.usecase.backup.CreateBackupUseCase
import takagi.ru.saison.domain.usecase.backup.DeleteBackupUseCase
import takagi.ru.saison.domain.usecase.backup.ListBackupsUseCase
import takagi.ru.saison.domain.usecase.backup.RestoreBackupUseCase
import takagi.ru.saison.worker.AutoBackupWorker
import javax.inject.Inject

data class WebDavBackupUiState(
    val isConfigured: Boolean = false,
    val config: WebDavConfig? = null,
    val isEditingConfig: Boolean = false,
    val isTestingConnection: Boolean = false,
    val connectionTestResult: ConnectionTestResult? = null,
    val backupList: List<BackupFile> = emptyList(),
    val isLoadingBackups: Boolean = false,
    val isCreatingBackup: Boolean = false,
    val isRestoringBackup: Boolean = false,
    val isDeletingBackup: Boolean = false,
    val backupPreferences: BackupPreferences = BackupPreferences(),
    val isAutoBackupEnabled: Boolean = false,
    val lastBackupTime: Long = 0,
    val error: String? = null,
    val successMessage: String? = null,
    val restoreSummary: RestoreSummary? = null
)

sealed class ConnectionTestResult {
    object Success : ConnectionTestResult()
    data class Failure(val message: String) : ConnectionTestResult()
}

@HiltViewModel
class WebDavBackupViewModel @Inject constructor(
    private val backupRepository: WebDavBackupRepository,
    private val createBackupUseCase: CreateBackupUseCase,
    private val restoreBackupUseCase: RestoreBackupUseCase,
    private val listBackupsUseCase: ListBackupsUseCase,
    private val deleteBackupUseCase: DeleteBackupUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(WebDavBackupUiState())
    val uiState: StateFlow<WebDavBackupUiState> = _uiState.asStateFlow()
    
    init {
        loadConfiguration()
    }
    
    private fun loadConfiguration() {
        viewModelScope.launch {
            val isConfigured = backupRepository.isConfigured()
            val config = backupRepository.getCurrentConfig()
            val preferences = backupRepository.getBackupPreferences()
            val isAutoBackupEnabled = backupRepository.isAutoBackupEnabled()
            val lastBackupTime = backupRepository.getLastBackupTime()
            
            _uiState.update {
                it.copy(
                    isConfigured = isConfigured,
                    config = config,
                    backupPreferences = preferences,
                    isAutoBackupEnabled = isAutoBackupEnabled,
                    lastBackupTime = lastBackupTime
                )
            }
            
            if (isConfigured) {
                loadBackupList()
            }
        }
    }
    
    fun configure(url: String, username: String, password: String) {
        viewModelScope.launch {
            try {
                backupRepository.configure(url, username, password)
                _uiState.update {
                    it.copy(
                        isConfigured = true,
                        isEditingConfig = false,
                        config = WebDavConfig(url, username),
                        successMessage = "配置已保存"
                    )
                }
                loadBackupList()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "配置失败: ${e.message}")
                }
            }
        }
    }
    
    fun testConnection() {
        viewModelScope.launch {
            _uiState.update { it.copy(isTestingConnection = true, connectionTestResult = null) }
            
            val result = backupRepository.testConnection()
            
            _uiState.update {
                it.copy(
                    isTestingConnection = false,
                    connectionTestResult = if (result.isSuccess) {
                        ConnectionTestResult.Success
                    } else {
                        ConnectionTestResult.Failure(result.exceptionOrNull()?.message ?: "连接失败")
                    }
                )
            }
        }
    }
    
    fun createBackup() {
        viewModelScope.launch {
            try {
                android.util.Log.d("WebDavBackup", "开始创建备份")
                _uiState.update { it.copy(isCreatingBackup = true, error = null) }
                
                val preferences = _uiState.value.backupPreferences
                android.util.Log.d("WebDavBackup", "备份偏好: $preferences")
                
                val result = createBackupUseCase(preferences)
                android.util.Log.d("WebDavBackup", "备份结果: ${result.isSuccess}, ${result.getOrNull()}, ${result.exceptionOrNull()}")
                
                if (result.isSuccess) {
                    _uiState.update {
                        it.copy(
                            isCreatingBackup = false,
                            successMessage = "备份创建成功: ${result.getOrNull()}",
                            lastBackupTime = System.currentTimeMillis()
                        )
                    }
                    loadBackupList()
                } else {
                    val errorMsg = result.exceptionOrNull()?.message ?: "未知错误"
                    android.util.Log.e("WebDavBackup", "备份失败: $errorMsg", result.exceptionOrNull())
                    _uiState.update {
                        it.copy(
                            isCreatingBackup = false,
                            error = "备份失败: $errorMsg"
                        )
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("WebDavBackup", "创建备份异常", e)
                _uiState.update {
                    it.copy(
                        isCreatingBackup = false,
                        error = "备份异常: ${e.message}"
                    )
                }
            }
        }
    }
    
    fun loadBackupList() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingBackups = true, error = null) }
            
            val result = listBackupsUseCase()
            
            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        isLoadingBackups = false,
                        backupList = result.getOrNull() ?: emptyList()
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoadingBackups = false,
                        error = "加载备份列表失败: ${result.exceptionOrNull()?.message}"
                    )
                }
            }
        }
    }
    
    fun restoreBackup(backupFile: BackupFile) {
        viewModelScope.launch {
            _uiState.update { it.copy(isRestoringBackup = true, error = null, restoreSummary = null) }
            
            val result = restoreBackupUseCase(backupFile)
            
            if (result.isSuccess) {
                val summary = result.getOrNull()
                _uiState.update {
                    it.copy(
                        isRestoringBackup = false,
                        restoreSummary = summary,
                        successMessage = "恢复成功，共导入 ${summary?.totalImported ?: 0} 项"
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isRestoringBackup = false,
                        error = "恢复失败: ${result.exceptionOrNull()?.message}"
                    )
                }
            }
        }
    }
    
    fun deleteBackup(backupFile: BackupFile) {
        viewModelScope.launch {
            _uiState.update { it.copy(isDeletingBackup = true, error = null) }
            
            val result = deleteBackupUseCase(backupFile)
            
            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        isDeletingBackup = false,
                        successMessage = "备份已删除"
                    )
                }
                loadBackupList()
            } else {
                _uiState.update {
                    it.copy(
                        isDeletingBackup = false,
                        error = "删除失败: ${result.exceptionOrNull()?.message}"
                    )
                }
            }
        }
    }
    
    fun toggleAutoBackup(enabled: Boolean, context: android.content.Context) {
        viewModelScope.launch {
            backupRepository.configureAutoBackup(enabled)
            _uiState.update { it.copy(isAutoBackupEnabled = enabled) }
            
            if (enabled) {
                AutoBackupWorker.schedule(context)
            } else {
                AutoBackupWorker.cancel(context)
            }
        }
    }
    
    fun updateBackupPreferences(preferences: BackupPreferences) {
        viewModelScope.launch {
            backupRepository.saveBackupPreferences(preferences)
            _uiState.update { it.copy(backupPreferences = preferences) }
        }
    }
    
    fun editConfig() {
        _uiState.update { it.copy(isEditingConfig = true) }
    }
    
    fun cancelEditConfig() {
        _uiState.update { it.copy(isEditingConfig = false) }
    }
    
    fun clearConfig() {
        viewModelScope.launch {
            backupRepository.clearConfig()
            _uiState.update {
                WebDavBackupUiState()
            }
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    fun clearSuccessMessage() {
        _uiState.update { it.copy(successMessage = null) }
    }
    
    fun clearRestoreSummary() {
        _uiState.update { it.copy(restoreSummary = null) }
    }
}
