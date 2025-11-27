package takagi.ru.saison.ui.screens.plus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import takagi.ru.saison.data.local.datastore.PreferencesManager
import takagi.ru.saison.domain.model.plus.PlusFeature
import takagi.ru.saison.domain.model.plus.PlusFeatures
import javax.inject.Inject

/**
 * Saison Plus功能的ViewModel
 * 
 * 负责管理Plus激活状态、功能列表和相关业务逻辑
 */
@HiltViewModel
class SaisonPlusViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager
) : ViewModel() {
    
    // UI状态
    private val _uiState = MutableStateFlow(SaisonPlusUiState())
    val uiState: StateFlow<SaisonPlusUiState> = _uiState.asStateFlow()
    
    // Plus激活状态
    val isPlusActivated: StateFlow<Boolean> = preferencesManager.isPlusActivated
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )
    
    init {
        loadPlusStatus()
        loadPlusFeatures()
    }
    
    /**
     * 加载Plus激活状态
     */
    private fun loadPlusStatus() {
        viewModelScope.launch {
            try {
                preferencesManager.isPlusActivated.collect { activated ->
                    _uiState.update { it.copy(isPlusActivated = activated) }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = "加载Plus状态失败: ${e.message}")
                }
            }
        }
    }
    
    /**
     * 加载Plus功能列表
     */
    private fun loadPlusFeatures() {
        viewModelScope.launch {
            try {
                val features = PlusFeatures.getPlaceholderFeatures()
                _uiState.update { it.copy(features = features) }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = "加载功能列表失败: ${e.message}")
                }
            }
        }
    }
    
    /**
     * 激活Saison Plus
     */
    fun activatePlus() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }
                preferencesManager.setPlusActivated(true)
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        isPlusActivated = true
                    )
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "激活失败: ${e.message}"
                    )
                }
            }
        }
    }
    
    /**
     * 停用Saison Plus（用于测试）
     */
    fun deactivatePlus() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }
                preferencesManager.setPlusActivated(false)
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        isPlusActivated = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "停用失败: ${e.message}"
                    )
                }
            }
        }
    }
    
    /**
     * 获取显示用的功能列表（根据Plus状态标记解锁状态）
     */
    fun getDisplayFeatures(features: List<PlusFeature>): List<DisplayPlusFeature> {
        val isActivated = _uiState.value.isPlusActivated
        return features.map { feature ->
            DisplayPlusFeature(
                feature = feature,
                isUnlocked = isActivated
            )
        }
    }
    
    /**
     * 清除错误消息
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

/**
 * Saison Plus UI状态
 */
data class SaisonPlusUiState(
    val isPlusActivated: Boolean = false,
    val features: List<PlusFeature> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * 用于显示的Plus功能（包含解锁状态）
 */
data class DisplayPlusFeature(
    val feature: PlusFeature,
    val isUnlocked: Boolean
)
