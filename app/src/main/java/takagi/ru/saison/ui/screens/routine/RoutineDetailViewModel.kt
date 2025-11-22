package takagi.ru.saison.ui.screens.routine

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import takagi.ru.saison.data.repository.RoutineRepository
import takagi.ru.saison.domain.model.routine.CheckInRecord
import takagi.ru.saison.domain.model.routine.RoutineTask
import takagi.ru.saison.util.CycleCalculator
import java.time.LocalDate
import javax.inject.Inject

/**
 * 日程任务详情页面 ViewModel
 */
@HiltViewModel
class RoutineDetailViewModel @Inject constructor(
    private val repository: RoutineRepository,
    private val cycleCalculator: CycleCalculator,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    private val taskId: Long = checkNotNull(savedStateHandle["taskId"])
    
    private val _uiState = MutableStateFlow(RoutineDetailUiState())
    val uiState: StateFlow<RoutineDetailUiState> = _uiState.asStateFlow()
    
    init {
        loadTaskDetail()
    }
    
    /**
     * 加载任务详情和打卡历史
     */
    private fun loadTaskDetail() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                // 获取任务信息
                val task = repository.getRoutineTask(taskId)
                if (task == null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "任务不存在"
                        )
                    }
                    return@launch
                }
                
                // 获取打卡记录并按周期分组
                repository.getCheckInRecords(taskId)
                    .catch { error ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = "加载失败：${error.message}"
                            )
                        }
                    }
                    .collect { records ->
                        val groupedRecords = groupRecordsByCycle(records, task)
                        val currentCycle = cycleCalculator.getCurrentCycle(task)
                        val isInActiveCycle = cycleCalculator.isInActiveCycle(task)
                        val currentCycleCount = if (currentCycle != null) {
                            records.count { record ->
                                !record.checkInTime.toLocalDate().isBefore(currentCycle.first) &&
                                !record.checkInTime.toLocalDate().isAfter(currentCycle.second)
                            }
                        } else {
                            0
                        }
                        
                        _uiState.update {
                            it.copy(
                                task = task,
                                checkInRecords = records,
                                groupedRecords = groupedRecords,
                                currentCycleCount = currentCycleCount,
                                isInActiveCycle = isInActiveCycle,
                                currentCycle = currentCycle,
                                isLoading = false,
                                error = null
                            )
                        }
                    }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "加载失败：${e.message}"
                    )
                }
            }
        }
    }
    
    /**
     * 按周期分组打卡记录
     */
    private fun groupRecordsByCycle(
        records: List<CheckInRecord>,
        task: RoutineTask
    ): List<CycleGroup> {
        // 按周期开始日期分组
        val grouped = records.groupBy { it.cycleStartDate }
        
        return grouped.map { (cycleStart, cycleRecords) ->
            val cycleEnd = cycleRecords.firstOrNull()?.cycleEndDate ?: cycleStart
            CycleGroup(
                cycleStart = cycleStart,
                cycleEnd = cycleEnd,
                records = cycleRecords.sortedByDescending { it.checkInTime },
                count = cycleRecords.size
            )
        }.sortedByDescending { it.cycleStart }
    }
    
    /**
     * 打卡
     */
    fun checkIn(note: String? = null) {
        viewModelScope.launch {
            try {
                val task = _uiState.value.task ?: return@launch
                val currentCycle = _uiState.value.currentCycle ?: return@launch
                
                // 乐观更新：立即更新 UI 状态
                val newCheckInRecord = CheckInRecord(
                    id = System.currentTimeMillis(), // 临时 ID
                    routineTaskId = taskId,
                    checkInTime = java.time.LocalDateTime.now(),
                    cycleStartDate = currentCycle.first,
                    cycleEndDate = currentCycle.second,
                    note = note
                )
                
                val updatedRecords = listOf(newCheckInRecord) + _uiState.value.checkInRecords
                val updatedGroupedRecords = groupRecordsByCycle(updatedRecords, task)
                val updatedCycleCount = _uiState.value.currentCycleCount + 1
                
                _uiState.update {
                    it.copy(
                        checkInRecords = updatedRecords,
                        groupedRecords = updatedGroupedRecords,
                        currentCycleCount = updatedCycleCount
                    )
                }
                
                // 后台执行实际的打卡操作
                repository.checkIn(taskId, note)
                
                // 静默刷新以获取正确的 ID（不触发 UI 重新加载）
                val actualRecords = repository.getCheckInRecordsOnce(taskId)
                val actualGroupedRecords = groupRecordsByCycle(actualRecords, task)
                val actualCycleCount = actualRecords.count { record ->
                    !record.checkInTime.toLocalDate().isBefore(currentCycle.first) &&
                    !record.checkInTime.toLocalDate().isAfter(currentCycle.second)
                }
                
                _uiState.update {
                    it.copy(
                        checkInRecords = actualRecords,
                        groupedRecords = actualGroupedRecords,
                        currentCycleCount = actualCycleCount
                    )
                }
            } catch (e: Exception) {
                // 如果失败，恢复之前的状态
                loadTaskDetail()
                _uiState.update {
                    it.copy(error = "打卡失败：${e.message}")
                }
            }
        }
    }
    
    /**
     * 删除打卡记录
     */
    fun deleteCheckIn(checkInId: Long) {
        viewModelScope.launch {
            try {
                repository.deleteCheckIn(checkInId)
                _uiState.update {
                    it.copy(successMessage = "删除成功")
                }
                refreshTaskDetail()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "删除失败：${e.message}")
                }
            }
        }
    }
    
    /**
     * 刷新任务详情（不显示加载状态，避免闪烁）
     */
    private fun refreshTaskDetail() {
        viewModelScope.launch {
            try {
                // 获取任务信息
                val task = repository.getRoutineTask(taskId) ?: return@launch
                
                // 获取打卡记录（只取一次，不使用 Flow）
                repository.getCheckInRecords(taskId)
                    .catch { /* 忽略错误，保持当前状态 */ }
                    .collect { records ->
                        val groupedRecords = groupRecordsByCycle(records, task)
                        val currentCycle = cycleCalculator.getCurrentCycle(task)
                        val isInActiveCycle = cycleCalculator.isInActiveCycle(task)
                        val currentCycleCount = if (currentCycle != null) {
                            records.count { record ->
                                !record.checkInTime.toLocalDate().isBefore(currentCycle.first) &&
                                !record.checkInTime.toLocalDate().isAfter(currentCycle.second)
                            }
                        } else {
                            0
                        }
                        
                        _uiState.update {
                            it.copy(
                                task = task,
                                checkInRecords = records,
                                groupedRecords = groupedRecords,
                                currentCycleCount = currentCycleCount,
                                isInActiveCycle = isInActiveCycle,
                                currentCycle = currentCycle,
                                error = null
                            )
                        }
                    }
            } catch (e: Exception) {
                // 刷新失败时不更新错误状态，保持当前界面
            }
        }
    }
    
    /**
     * 更新任务
     */
    fun updateTask(task: RoutineTask) {
        viewModelScope.launch {
            try {
                repository.updateRoutineTask(task)
                _uiState.update {
                    it.copy(successMessage = "任务更新成功")
                }
                loadTaskDetail()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "更新失败：${e.message}")
                }
            }
        }
    }
    
    /**
     * 删除任务
     */
    fun deleteTask() {
        viewModelScope.launch {
            try {
                repository.deleteRoutineTask(taskId)
                _uiState.update {
                    it.copy(
                        successMessage = "任务删除成功",
                        taskDeleted = true
                    )
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
 * 日程任务详情页面 UI 状态
 */
data class RoutineDetailUiState(
    val task: RoutineTask? = null,
    val checkInRecords: List<CheckInRecord> = emptyList(),
    val groupedRecords: List<CycleGroup> = emptyList(),
    val currentCycleCount: Int = 0,
    val isInActiveCycle: Boolean = false,
    val currentCycle: Pair<LocalDate, LocalDate>? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val taskDeleted: Boolean = false
)

/**
 * 周期分组
 */
data class CycleGroup(
    val cycleStart: LocalDate,
    val cycleEnd: LocalDate,
    val records: List<CheckInRecord>,
    val count: Int
)
