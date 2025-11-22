package takagi.ru.saison.ui.screens.semester

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import takagi.ru.saison.data.local.datastore.PreferencesManager
import takagi.ru.saison.data.repository.CourseRepository
import takagi.ru.saison.data.repository.SemesterRepository
import takagi.ru.saison.domain.model.Semester
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.first

@HiltViewModel
class SemesterViewModel @Inject constructor(
    private val semesterRepository: SemesterRepository,
    private val courseRepository: CourseRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {
    
    // 所有学期
    val allSemesters: StateFlow<List<Semester>> = semesterRepository.getAllSemesters()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    // 活跃学期
    val activeSemesters: StateFlow<List<Semester>> = semesterRepository.getActiveSemesters()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    // 归档学期
    val archivedSemesters: StateFlow<List<Semester>> = semesterRepository.getArchivedSemesters()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    // 当前选中的学期
    private val _currentSemesterId = MutableStateFlow<Long?>(null)
    val currentSemester: StateFlow<Semester?> = _currentSemesterId
        .flatMapLatest { id ->
            if (id != null) {
                semesterRepository.getSemesterById(id)
            } else {
                flowOf(null)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
    
    // 学期统计信息
    private val _semesterStats = MutableStateFlow<Map<Long, SemesterStats>>(emptyMap())
    val semesterStats: StateFlow<Map<Long, SemesterStats>> = _semesterStats.asStateFlow()
    
    // UI状态
    private val _uiState = MutableStateFlow<SemesterUiState>(SemesterUiState.Loading)
    val uiState: StateFlow<SemesterUiState> = _uiState.asStateFlow()
    
    init {
        loadCurrentSemester()
        observeSemestersForStats()
    }
    
    private fun loadCurrentSemester() {
        viewModelScope.launch {
            try {
                // 从 PreferencesManager 加载当前学期ID
                val savedSemesterId = preferencesManager.getCurrentSemesterId()
                
                if (savedSemesterId != null) {
                    _currentSemesterId.value = savedSemesterId
                } else {
                    // 如果没有保存的学期ID，尝试获取默认学期或最新学期
                    val defaultSemester = semesterRepository.getDefaultSemester()
                        ?: semesterRepository.getLatestSemester()
                    
                    if (defaultSemester != null) {
                        _currentSemesterId.value = defaultSemester.id
                        preferencesManager.setCurrentSemesterId(defaultSemester.id)
                    }
                }
                
                _uiState.value = SemesterUiState.Success
            } catch (e: Exception) {
                _uiState.value = SemesterUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
    
    private fun observeSemestersForStats() {
        viewModelScope.launch {
            allSemesters.collect { semesters ->
                val stats = mutableMapOf<Long, SemesterStats>()
                semesters.forEach { semester ->
                    val courseCount = courseRepository.getCourseCountBySemester(semester.id)
                    // 计算总学时（假设每门课程平均2小时/周）
                    val totalHours = courseCount * 2 * semester.totalWeeks
                    val weekRange = "${semester.startDate} - ${semester.endDate}"
                    
                    stats[semester.id] = SemesterStats(
                        courseCount = courseCount,
                        totalHours = totalHours,
                        weekRange = weekRange
                    )
                }
                _semesterStats.value = stats
            }
        }
    }
    
    // 创建学期
    fun createSemester(name: String, startDate: LocalDate, totalWeeks: Int) {
        viewModelScope.launch {
            try {
                _uiState.value = SemesterUiState.Loading
                
                val endDate = startDate.plusWeeks(totalWeeks.toLong())
                val semester = Semester(
                    name = name,
                    startDate = startDate,
                    endDate = endDate,
                    totalWeeks = totalWeeks,
                    isArchived = false,
                    isDefault = false
                )
                
                val newId = semesterRepository.insertSemester(semester)
                
                // 自动切换到新创建的学期
                switchSemester(newId)
                
                _uiState.value = SemesterUiState.Success
            } catch (e: Exception) {
                _uiState.value = SemesterUiState.Error(e.message ?: "Failed to create semester")
            }
        }
    }
    
    // 切换学期
    fun switchSemester(semesterId: Long) {
        viewModelScope.launch {
            try {
                _currentSemesterId.value = semesterId
                preferencesManager.setCurrentSemesterId(semesterId)
                
                // 记录到访问历史
                preferencesManager.addToSemesterHistory(semesterId)
                
                // 同步更新CourseSettings的totalWeeks
                val semester = semesterRepository.getSemesterByIdSync(semesterId)
                if (semester != null) {
                    val currentSettings = preferencesManager.courseSettings.first()
                    preferencesManager.setCourseSettings(
                        currentSettings.copy(
                            totalWeeks = semester.totalWeeks,
                            semesterStartDate = semester.startDate
                        )
                    )
                }
            } catch (e: Exception) {
                _uiState.value = SemesterUiState.Error(e.message ?: "Failed to switch semester")
            }
        }
    }
    
    // 编辑学期
    fun updateSemester(semester: Semester) {
        viewModelScope.launch {
            try {
                _uiState.value = SemesterUiState.Loading
                
                val endDate = semester.startDate.plusWeeks(semester.totalWeeks.toLong())
                val updatedSemester = semester.copy(endDate = endDate)
                semesterRepository.updateSemester(updatedSemester)
                
                // 如果更新的是当前学期，同步更新CourseSettings
                if (semester.id == _currentSemesterId.value) {
                    val currentSettings = preferencesManager.courseSettings.first()
                    preferencesManager.setCourseSettings(
                        currentSettings.copy(
                            totalWeeks = semester.totalWeeks,
                            semesterStartDate = semester.startDate
                        )
                    )
                }
                
                _uiState.value = SemesterUiState.Success
            } catch (e: Exception) {
                _uiState.value = SemesterUiState.Error(e.message ?: "Failed to update semester")
            }
        }
    }
    
    // 删除学期
    fun deleteSemester(semesterId: Long) {
        viewModelScope.launch {
            try {
                _uiState.value = SemesterUiState.Loading
                
                // 检查是否是最后一个学期
                val semesterCount = semesterRepository.getSemesterCount()
                if (semesterCount <= 1) {
                    _uiState.value = SemesterUiState.Error("Cannot delete the last semester.")
                    return@launch
                }

                // 如果是当前学期，需要先切换到其他学期
                if (_currentSemesterId.value == semesterId) {
                    // 获取所有学期
                    val allSemesters = semesterRepository.getAllSemesters().first()
                    // 找到一个不是当前要删除的学期
                    val nextSemester = allSemesters.find { it.id != semesterId }
                    
                    if (nextSemester != null) {
                        // 切换到新学期
                        preferencesManager.setCurrentSemesterId(nextSemester.id)
                        _currentSemesterId.value = nextSemester.id
                        
                        // 更新CourseSettings中的学期开始日期
                        val currentSettings = preferencesManager.courseSettings.first()
                        preferencesManager.setCourseSettings(
                            currentSettings.copy(
                                totalWeeks = nextSemester.totalWeeks,
                                semesterStartDate = nextSemester.startDate
                            )
                        )
                    } else {
                        // 理论上不会发生，因为前面检查了count <= 1
                        _uiState.value = SemesterUiState.Error("Cannot delete the last semester.")
                        return@launch
                    }
                }
                
                // 删除学期及其所有课程
                courseRepository.deleteCoursesBySemester(semesterId)
                semesterRepository.deleteSemester(semesterId)
                
                _uiState.value = SemesterUiState.Success
            } catch (e: Exception) {
                _uiState.value = SemesterUiState.Error(e.message ?: "Failed to delete semester")
            }
        }
    }
    
    // 归档/取消归档
    fun toggleArchive(semesterId: Long, isArchived: Boolean) {
        viewModelScope.launch {
            try {
                semesterRepository.updateArchiveStatus(semesterId, isArchived)
            } catch (e: Exception) {
                _uiState.value = SemesterUiState.Error(e.message ?: "Failed to toggle archive status")
            }
        }
    }
    
    // 复制学期
    fun copySemester(semesterId: Long, newName: String) {
        viewModelScope.launch {
            try {
                _uiState.value = SemesterUiState.Loading
                
                val newSemesterId = semesterRepository.copySemester(semesterId, newName)
                
                // 自动切换到新学期
                switchSemester(newSemesterId)
                
                _uiState.value = SemesterUiState.Success
            } catch (e: Exception) {
                _uiState.value = SemesterUiState.Error(e.message ?: "Failed to copy semester")
            }
        }
    }
    
    // 获取最近使用的学期
    fun getRecentSemesters(): Flow<List<Semester>> {
        return preferencesManager.getSemesterHistory()
            .map { historyIds ->
                historyIds.take(3).mapNotNull { id ->
                    semesterRepository.getSemesterByIdSync(id)
                }
            }
    }
}

data class SemesterStats(
    val courseCount: Int,
    val totalHours: Int,
    val weekRange: String
)

sealed class SemesterUiState {
    object Loading : SemesterUiState()
    object Success : SemesterUiState()
    data class Error(val message: String) : SemesterUiState()
}
