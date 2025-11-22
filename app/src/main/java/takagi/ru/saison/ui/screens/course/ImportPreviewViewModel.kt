package takagi.ru.saison.ui.screens.course

import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import takagi.ru.saison.data.ics.IcsException
import takagi.ru.saison.domain.model.Course
import takagi.ru.saison.domain.model.CoursePeriod
import takagi.ru.saison.domain.model.TimeMatchingStrategy
import takagi.ru.saison.domain.usecase.IcsImportUseCase
import takagi.ru.saison.data.repository.CourseRepository
import javax.inject.Inject

/**
 * 导入预览状态
 */
sealed class ImportPreviewState {
    object Idle : ImportPreviewState()
    object Loading : ImportPreviewState()
    data class Preview(
        val courses: List<Course>,
        val selectedIndices: Set<Int>,
        val duplicateWarnings: List<String>,
        val generatedPeriods: List<CoursePeriod>? = null,
        val matchingWarnings: List<String> = emptyList(),
        val strategy: TimeMatchingStrategy = TimeMatchingStrategy.UseExistingPeriods,
        val suggestedSemesterStartDate: java.time.LocalDate? = null
    ) : ImportPreviewState()
    data class Success(val importedCount: Int) : ImportPreviewState()
    data class Error(val message: String) : ImportPreviewState()
}

/**
 * 导入预览ViewModel
 */
@HiltViewModel
class ImportPreviewViewModel @Inject constructor(
    private val icsImportUseCase: IcsImportUseCase,
    private val courseRepository: CourseRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<ImportPreviewState>(ImportPreviewState.Idle)
    val uiState: StateFlow<ImportPreviewState> = _uiState.asStateFlow()
    
    private var currentCourses: List<Course> = emptyList()
    private var currentSemesterId: Long? = null
    
    /**
     * 加载预览数据（增强版，支持时间匹配策略）
     */
    fun loadPreview(
        uri: Uri,
        semesterId: Long,
        primaryColor: Color,
        strategy: TimeMatchingStrategy = TimeMatchingStrategy.UseExistingPeriods,
        existingPeriods: List<CoursePeriod> = emptyList()
    ) {
        currentSemesterId = semesterId
        viewModelScope.launch {
            _uiState.value = ImportPreviewState.Loading
            
            try {
                val result = icsImportUseCase.previewImport(
                    uri = uri,
                    targetSemesterId = semesterId,
                    primaryColor = primaryColor,
                    strategy = strategy,
                    existingPeriods = existingPeriods
                )
                
                result.onSuccess { previewResult ->
                    currentCourses = previewResult.courses
                    
                    // 检测重复课程
                    val existingCourses = courseRepository.getCoursesBySemester(semesterId).first()
                    val duplicateWarnings = icsImportUseCase.detectDuplicates(previewResult.courses, existingCourses)
                    
                    // 默认全选
                    val selectedIndices = previewResult.courses.indices.toSet()
                    
                    _uiState.value = ImportPreviewState.Preview(
                        courses = previewResult.courses,
                        selectedIndices = selectedIndices,
                        duplicateWarnings = duplicateWarnings,
                        generatedPeriods = previewResult.generatedPeriods,
                        matchingWarnings = previewResult.matchingWarnings,
                        strategy = strategy,
                        suggestedSemesterStartDate = previewResult.suggestedSemesterStartDate
                    )
                }.onFailure { error ->
                    val message = when (error) {
                        is IcsException.EmptyFile -> "文件不包含课程数据"
                        is IcsException.InvalidFormat -> "ICS文件格式无效"
                        is IcsException.IoError -> error.message ?: "读取文件失败"
                        is IcsException.ParseError -> "解析失败: ${error.message}"
                        else -> error.message ?: "未知错误"
                    }
                    _uiState.value = ImportPreviewState.Error(message)
                }
            } catch (e: Exception) {
                _uiState.value = ImportPreviewState.Error(e.message ?: "加载失败")
            }
        }
    }
    
    /**
     * 切换课程选择状态
     */
    fun toggleCourseSelection(courseIndex: Int) {
        val currentState = _uiState.value
        if (currentState is ImportPreviewState.Preview) {
            val newSelectedIndices = if (courseIndex in currentState.selectedIndices) {
                currentState.selectedIndices - courseIndex
            } else {
                currentState.selectedIndices + courseIndex
            }
            
            _uiState.value = currentState.copy(selectedIndices = newSelectedIndices)
        }
    }
    
    /**
     * 全选
     */
    fun selectAll() {
        val currentState = _uiState.value
        if (currentState is ImportPreviewState.Preview) {
            val allIndices = currentState.courses.indices.toSet()
            _uiState.value = currentState.copy(selectedIndices = allIndices)
        }
    }
    
    /**
     * 取消全选
     */
    fun deselectAll() {
        val currentState = _uiState.value
        if (currentState is ImportPreviewState.Preview) {
            _uiState.value = currentState.copy(selectedIndices = emptySet())
        }
    }
    
    /**
     * 确认导入
     */
    fun confirmImport() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is ImportPreviewState.Preview) {
                _uiState.value = ImportPreviewState.Loading
                
                // 获取选中的课程
                val selectedCourses = currentState.selectedIndices.map { index ->
                    currentCourses[index]
                }
                
                if (selectedCourses.isEmpty()) {
                    _uiState.value = ImportPreviewState.Error("请至少选择一门课程")
                    return@launch
                }
                
                val result = icsImportUseCase.confirmImport(
                    courses = selectedCourses,
                    generatedPeriods = currentState.generatedPeriods,
                    suggestedSemesterStartDate = currentState.suggestedSemesterStartDate,
                    targetSemesterId = currentSemesterId
                )
                
                result.onSuccess { count ->
                    _uiState.value = ImportPreviewState.Success(count)
                }.onFailure { error ->
                    _uiState.value = ImportPreviewState.Error(error.message ?: "导入失败")
                }
            }
        }
    }
    
    /**
     * 重置状态
     */
    fun resetState() {
        _uiState.value = ImportPreviewState.Idle
        currentCourses = emptyList()
    }
}
