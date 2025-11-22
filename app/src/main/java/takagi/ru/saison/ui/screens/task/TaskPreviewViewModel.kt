package takagi.ru.saison.ui.screens.task

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import takagi.ru.saison.data.repository.TaskRepository
import takagi.ru.saison.domain.model.Task
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class TaskPreviewViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    private val taskId: Long = savedStateHandle.get<Long>("taskId") ?: 0L
    
    private val _task = MutableStateFlow<Task?>(null)
    val task: StateFlow<Task?> = _task.asStateFlow()
    
    private val _uiState = MutableStateFlow<TaskPreviewUiState>(TaskPreviewUiState.Loading)
    val uiState: StateFlow<TaskPreviewUiState> = _uiState.asStateFlow()
    
    fun loadTask(taskId: Long) {
        viewModelScope.launch {
            try {
                _uiState.value = TaskPreviewUiState.Loading
                val task = taskRepository.getTaskById(taskId)
                if (task != null) {
                    _task.value = task
                    _uiState.value = TaskPreviewUiState.Success
                } else {
                    _uiState.value = TaskPreviewUiState.Error("Task not found")
                }
            } catch (e: Exception) {
                _uiState.value = TaskPreviewUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
    
    fun toggleCompletion() {
        viewModelScope.launch {
            _task.value?.let { currentTask ->
                val updatedTask = currentTask.copy(
                    isCompleted = !currentTask.isCompleted,
                    updatedAt = LocalDateTime.now()
                )
                taskRepository.updateTask(updatedTask)
            }
        }
    }
    
    fun deleteTask() {
        viewModelScope.launch {
            _task.value?.let { currentTask ->
                taskRepository.deleteTask(currentTask.id)
            }
        }
    }
}

sealed class TaskPreviewUiState {
    object Loading : TaskPreviewUiState()
    object Success : TaskPreviewUiState()
    data class Error(val message: String) : TaskPreviewUiState()
}
