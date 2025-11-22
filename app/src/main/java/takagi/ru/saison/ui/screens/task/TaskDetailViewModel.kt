package takagi.ru.saison.ui.screens.task

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import takagi.ru.saison.data.repository.TaskRepository
import takagi.ru.saison.domain.model.Priority
import takagi.ru.saison.domain.model.Tag
import takagi.ru.saison.domain.model.Task
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@OptIn(FlowPreview::class)
@HiltViewModel
class TaskDetailViewModel @Inject constructor(
    private val taskRepository: TaskRepository
) : ViewModel() {
    
    private val _task = MutableStateFlow<Task?>(null)
    val task: StateFlow<Task?> = _task.asStateFlow()
    
    private val _uiState = MutableStateFlow<TaskDetailUiState>(TaskDetailUiState.Loading)
    val uiState: StateFlow<TaskDetailUiState> = _uiState.asStateFlow()
    
    private val _pendingTask = MutableStateFlow<Task?>(null)
    
    init {
        // 自动保存逻辑：延迟1秒后保存
        viewModelScope.launch {
            _pendingTask
                .debounce(1.seconds)
                .filterNotNull()
                .collect { task ->
                    try {
                        taskRepository.updateTask(task)
                        setHasUnsavedChanges(false)
                    } catch (e: Exception) {
                        _uiState.value = TaskDetailUiState.Error(e.message ?: "自动保存失败")
                    }
                }
        }
    }
    
    fun loadTask(taskId: Long) {
        viewModelScope.launch {
            try {
                _uiState.value = TaskDetailUiState.Loading
                taskRepository.getTaskByIdFlow(taskId).collect { loadedTask ->
                    _task.value = loadedTask
                    _uiState.value = if (loadedTask != null) {
                        TaskDetailUiState.Success()
                    } else {
                        TaskDetailUiState.Error("任务不存在")
                    }
                }
            } catch (e: Exception) {
                _uiState.value = TaskDetailUiState.Error(e.message ?: "加载失败")
            }
        }
    }
    
    fun updateUiState(update: (TaskDetailUiState.Success) -> TaskDetailUiState.Success) {
        val currentState = _uiState.value
        if (currentState is TaskDetailUiState.Success) {
            _uiState.value = update(currentState)
        }
    }
    
    fun setHasUnsavedChanges(hasChanges: Boolean) {
        updateUiState { it.copy(hasUnsavedChanges = hasChanges) }
    }
    
    fun setShowDatePicker(show: Boolean) {
        updateUiState { it.copy(showDatePicker = show) }
    }
    
    fun setShowTimePicker(show: Boolean) {
        updateUiState { it.copy(showTimePicker = show) }
    }
    
    fun setShowRecurrenceDialog(show: Boolean) {
        updateUiState { it.copy(showRecurrenceDialog = show) }
    }
    
    fun loadAvailableTags(tags: List<Tag>) {
        updateUiState { it.copy(availableTags = tags) }
    }
    
    fun loadTemplates(templates: List<TaskTemplate>) {
        updateUiState { it.copy(templates = templates) }
    }
    
    fun updateTask(task: Task) {
        viewModelScope.launch {
            try {
                taskRepository.updateTask(task)
                setHasUnsavedChanges(false)
            } catch (e: Exception) {
                _uiState.value = TaskDetailUiState.Error(e.message ?: "更新失败")
            }
        }
    }
    
    fun updateTaskWithAutoSave(task: Task) {
        setHasUnsavedChanges(true)
        _pendingTask.value = task
    }
    
    fun deleteTask() {
        viewModelScope.launch {
            try {
                _task.value?.let { task ->
                    taskRepository.deleteTask(task.id)
                }
            } catch (e: Exception) {
                _uiState.value = TaskDetailUiState.Error(e.message ?: "删除失败")
            }
        }
    }
}

sealed class TaskDetailUiState {
    object Loading : TaskDetailUiState()
    data class Success(
        val hasUnsavedChanges: Boolean = false,
        val showDatePicker: Boolean = false,
        val showTimePicker: Boolean = false,
        val showRecurrenceDialog: Boolean = false,
        val availableTags: List<Tag> = emptyList(),
        val templates: List<TaskTemplate> = emptyList()
    ) : TaskDetailUiState()
    data class Error(val message: String) : TaskDetailUiState()
}

/**
 * Represents a task template for quick task creation
 */
data class TaskTemplate(
    val id: Long = 0,
    val name: String,
    val title: String,
    val description: String? = null,
    val priority: Priority = Priority.MEDIUM,
    val tags: List<Tag> = emptyList(),
    val estimatedPomodoros: Int? = null
)
