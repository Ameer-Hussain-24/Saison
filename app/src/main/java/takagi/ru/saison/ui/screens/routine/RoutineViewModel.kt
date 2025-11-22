package takagi.ru.saison.ui.screens.routine

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import takagi.ru.saison.data.repository.RoutineRepository
import takagi.ru.saison.domain.model.routine.RoutineTask
import takagi.ru.saison.domain.model.routine.RoutineTaskWithStats
import java.time.LocalDate
import javax.inject.Inject

/**
 * 日程打卡页面 ViewModel
 */
@HiltViewModel
class RoutineViewModel @Inject constructor(
    private val repository: RoutineRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(RoutineUiState())
    val uiState: StateFlow<RoutineUiState> = _uiState.asStateFlow()
    
    init {
        loadRoutineTasks()
    }
    
    /**
     * 加载所有周期性任务
     */
    fun loadRoutineTasks() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            repository.getRoutineTasksWithStats()
                .catch { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "加载失败：${error.message}"
                        )
                    }
                }
                .collect { tasksWithStats ->
                    // 分离活跃和非活跃任务
                    val (active, inactive) = tasksWithStats.partition { it.isInActiveCycle }
                    
                    // 排序活跃任务：按打卡次数升序，次数相同按最后打卡时间升序
                    val sortedActive = active.sortedWith(
                        compareBy<RoutineTaskWithStats> { it.checkInCount }
                            .thenBy { it.lastCheckInTime ?: LocalDate.MIN.atStartOfDay() }
                            .thenByDescending { it.task.createdAt }
                    )
                    
                    // 排序非活跃任务：按下次活跃日期升序
                    val sortedInactive = inactive.sortedWith(
                        compareBy<RoutineTaskWithStats> { it.nextActiveDate ?: LocalDate.MAX }
                            .thenByDescending { it.task.createdAt }
                    )
                    
                    _uiState.update {
                        it.copy(
                            activeTasks = sortedActive,
                            inactiveTasks = sortedInactive,
                            isLoading = false,
                            error = null
                        )
                    }
                }
        }
    }
    
    /**
     * 打卡
     */
    fun checkInTask(taskId: Long, note: String? = null) {
        viewModelScope.launch {
            try {
                // 乐观更新：立即更新 UI 中的打卡次数
                val currentState = _uiState.value
                val updatedActiveTasks = currentState.activeTasks.map { taskWithStats ->
                    if (taskWithStats.task.id == taskId) {
                        taskWithStats.copy(
                            checkInCount = taskWithStats.checkInCount + 1,
                            lastCheckInTime = java.time.LocalDateTime.now()
                        )
                    } else {
                        taskWithStats
                    }
                }
                
                // 重新排序：按打卡次数升序
                val sortedActiveTasks = updatedActiveTasks.sortedWith(
                    compareBy<RoutineTaskWithStats> { it.checkInCount }
                        .thenBy { it.lastCheckInTime ?: LocalDate.MIN.atStartOfDay() }
                        .thenByDescending { it.task.createdAt }
                )
                
                _uiState.update {
                    it.copy(activeTasks = sortedActiveTasks)
                }
                
                // 后台执行实际的打卡操作
                repository.checkIn(taskId, note)
                
                // 静默刷新以确保数据一致性（不触发 loading 状态）
                refreshTasksQuietly()
            } catch (e: Exception) {
                // 如果失败，恢复之前的状态
                loadRoutineTasks()
                _uiState.update {
                    it.copy(error = "打卡失败：${e.message}")
                }
            }
        }
    }
    
    /**
     * 静默刷新任务列表（不显示 loading 状态）
     */
    private suspend fun refreshTasksQuietly() {
        try {
            repository.getRoutineTasksWithStats()
                .catch { /* 忽略错误，保持当前状态 */ }
                .collect { tasksWithStats ->
                    val (active, inactive) = tasksWithStats.partition { it.isInActiveCycle }
                    
                    val sortedActive = active.sortedWith(
                        compareBy<RoutineTaskWithStats> { it.checkInCount }
                            .thenBy { it.lastCheckInTime ?: LocalDate.MIN.atStartOfDay() }
                            .thenByDescending { it.task.createdAt }
                    )
                    
                    val sortedInactive = inactive.sortedWith(
                        compareBy<RoutineTaskWithStats> { it.nextActiveDate ?: LocalDate.MAX }
                            .thenByDescending { it.task.createdAt }
                    )
                    
                    _uiState.update {
                        it.copy(
                            activeTasks = sortedActive,
                            inactiveTasks = sortedInactive
                        )
                    }
                }
        } catch (e: Exception) {
            // 静默失败，保持当前状态
        }
    }
    
    /**
     * 创建周期性任务
     */
    fun createRoutineTask(task: RoutineTask) {
        viewModelScope.launch {
            try {
                repository.createRoutineTask(task)
                loadRoutineTasks()
                _uiState.update {
                    it.copy(successMessage = "任务创建成功")
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "创建失败：${e.message}")
                }
            }
        }
    }
    
    /**
     * 更新周期性任务
     */
    fun updateRoutineTask(task: RoutineTask) {
        viewModelScope.launch {
            try {
                repository.updateRoutineTask(task)
                loadRoutineTasks()
                _uiState.update {
                    it.copy(successMessage = "任务更新成功")
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "更新失败：${e.message}")
                }
            }
        }
    }
    
    /**
     * 删除周期性任务
     */
    fun deleteRoutineTask(taskId: Long) {
        viewModelScope.launch {
            try {
                repository.deleteRoutineTask(taskId)
                loadRoutineTasks()
                _uiState.update {
                    it.copy(successMessage = "任务删除成功")
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "删除失败：${e.message}")
                }
            }
        }
    }
    
    /**
     * 清除错误消息
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    /**
     * 清除成功消息
     */
    fun clearSuccessMessage() {
        _uiState.update { it.copy(successMessage = null) }
    }
}

/**
 * 日程打卡页面 UI 状态
 */
data class RoutineUiState(
    val activeTasks: List<RoutineTaskWithStats> = emptyList(),
    val inactiveTasks: List<RoutineTaskWithStats> = emptyList(),
    val currentDate: LocalDate = LocalDate.now(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)
