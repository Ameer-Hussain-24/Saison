package takagi.ru.saison.domain.usecase

import android.content.Context
import android.net.Uri
import androidx.compose.ui.graphics.Color
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import takagi.ru.saison.data.ics.IcsException
import takagi.ru.saison.data.ics.IcsParser
import takagi.ru.saison.data.repository.CourseRepository
import takagi.ru.saison.data.repository.SemesterRepository
import takagi.ru.saison.domain.repository.CourseSettingsRepository
import takagi.ru.saison.domain.model.Course
import takagi.ru.saison.domain.model.CoursePeriod
import takagi.ru.saison.domain.model.ImportPreviewResult
import takagi.ru.saison.domain.model.TimeMatchingStrategy
import takagi.ru.saison.util.CourseConverter
import takagi.ru.saison.util.PeriodGenerator
import javax.inject.Inject

/**
 * ICS导入用例
 */
class IcsImportUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val icsParser: IcsParser,
    private val courseRepository: CourseRepository,
    private val semesterRepository: SemesterRepository,
    private val courseSettingsRepository: CourseSettingsRepository
) {
    
    /**
     * 从Uri导入课程
     * @param uri ICS文件的Uri
     * @param targetSemesterId 目标学期ID
     * @param primaryColor 主题色
     * @return 导入的课程数量
     */
    suspend fun importFromUri(
        uri: Uri,
        targetSemesterId: Long,
        primaryColor: Color
    ): Result<Int> = withContext(Dispatchers.IO) {
        try {
            // 解析ICS文件
            val parsedCourses = icsParser.parseFromUri(context, uri)
            
            // 获取学期信息
            val semester = semesterRepository.getSemesterByIdSync(targetSemesterId)
                ?: return@withContext Result.failure(Exception("学期不存在"))
            
            // 获取已存在的课程（用于颜色分配）
            val existingCourses = mutableListOf<Course>()
            
            // 转换为Course对象
            val courses = CourseConverter.groupAndConvert(
                parsedList = parsedCourses,
                semesterId = targetSemesterId,
                semesterStartDate = semester.startDate,
                primaryColor = primaryColor
            )
            
            // 保存到数据库
            courseRepository.insertCourses(courses)
            
            Result.success(courses.size)
        } catch (e: IcsException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(Exception("导入失败: ${e.message}"))
        }
    }
    
    /**
     * 预览导入（增强版，支持时间匹配策略）
     * 注意：必须在 URI 权限有效期内调用（文件选择后立即调用）
     * @param strategy 时间匹配策略
     * @param existingPeriods 现有节次（UseExistingPeriods时使用）
     */
    suspend fun previewImport(
        uri: Uri,
        targetSemesterId: Long,
        primaryColor: Color,
        strategy: TimeMatchingStrategy = TimeMatchingStrategy.UseExistingPeriods,
        existingPeriods: List<CoursePeriod> = emptyList()
    ): Result<ImportPreviewResult> = withContext(Dispatchers.IO) {
        try {
            android.util.Log.d("IcsImportUseCase", "Starting preview import from URI: $uri")
            android.util.Log.d("IcsImportUseCase", "Target semester ID: $targetSemesterId")
            android.util.Log.d("IcsImportUseCase", "Thread: ${Thread.currentThread().name}")
            
            // 立即解析ICS文件（在权限有效期内）
            val parsedCourses = icsParser.parseFromUri(context, uri)
            android.util.Log.d("IcsImportUseCase", "Parsed ${parsedCourses.size} courses from ICS file")
            
            // 1.1 检测最早的课程日期
            val earliestDate = parsedCourses
                .mapNotNull { it.dtStart.toLocalDate() }
                .minOrNull()
            
            android.util.Log.d("IcsImportUseCase", "Earliest course date detected: $earliestDate")
            
            // 计算该日期所在周的周一作为学期开始日期
            val detectedSemesterStartDate = earliestDate?.with(java.time.DayOfWeek.MONDAY)
                ?: java.time.LocalDate.now().with(java.time.DayOfWeek.MONDAY)
            
            android.util.Log.d("IcsImportUseCase", "Detected semester start date (Monday of earliest week): $detectedSemesterStartDate")
            
            // 1.2 获取或创建学期，并自动设置/更新学期开始日期
            var semester = semesterRepository.getSemesterByIdSync(targetSemesterId)
            if (semester == null) {
                android.util.Log.w("IcsImportUseCase", "Semester not found: $targetSemesterId, creating default semester")
                
                // 从解析的课程中获取日期范围
                val dates = parsedCourses.mapNotNull { it.dtStart.toLocalDate() }
                val startDate = detectedSemesterStartDate
                val endDate = dates.maxOrNull()?.plusMonths(4) ?: startDate.plusMonths(4)
                
                // 创建默认学期，使用检测到的开始日期
                val newSemester = takagi.ru.saison.domain.model.Semester(
                    name = "导入学期 ${java.time.LocalDate.now().year}",
                    startDate = startDate,
                    endDate = endDate,
                    totalWeeks = 18,
                    isDefault = true
                )
                
                val semesterId = semesterRepository.insertSemester(newSemester)
                semester = newSemester.copy(id = semesterId)
                android.util.Log.d("IcsImportUseCase", "Created new semester: ${semester.name} (ID: $semesterId) with start date: $startDate")
            } else if (semester.startDate != detectedSemesterStartDate) {
                // 如果学期已存在但开始日期不同，我们暂时不更新数据库，而是使用检测到的日期进行预览
                // 并在结果中建议更新
                android.util.Log.d("IcsImportUseCase", "Detected different semester start date: $detectedSemesterStartDate (current: ${semester.startDate})")
                // 使用检测到的日期创建一个临时的学期对象用于转换
                semester = semester.copy(startDate = detectedSemesterStartDate)
            }
            
            // 记录学期开始日期和当前周次
            val currentDate = java.time.LocalDate.now()
            val daysBetween = java.time.temporal.ChronoUnit.DAYS.between(semester.startDate, currentDate)
            val currentWeek = (daysBetween / 7).toInt() + 1
            
            android.util.Log.d("IcsImportUseCase", "Using semester: ${semester.name}")
            android.util.Log.d("IcsImportUseCase", "Semester start date: ${semester.startDate}")
            android.util.Log.d("IcsImportUseCase", "Current date: $currentDate")
            android.util.Log.d("IcsImportUseCase", "Current week number: $currentWeek")
            
            // 根据策略生成或使用节次配置
            val generatedPeriods = when (strategy) {
                is TimeMatchingStrategy.AutoCreatePeriods -> {
                    android.util.Log.d("IcsImportUseCase", "Generating periods from parsed courses descriptions")
                    // 优先尝试从描述中提取节次信息
                    PeriodGenerator.generatePeriodsFromDescriptions(parsedCourses)
                }
                is TimeMatchingStrategy.UseExistingPeriods -> null
            }
            
            android.util.Log.d("IcsImportUseCase", "Generated periods: ${generatedPeriods?.size ?: 0}")
            
            // 转换为Course对象
            val courses = CourseConverter.groupAndConvert(
                parsedList = parsedCourses,
                semesterId = semester.id,
                semesterStartDate = semester.startDate,
                primaryColor = primaryColor,
                strategy = strategy,
                existingPeriods = existingPeriods,
                generatedPeriods = generatedPeriods ?: emptyList()
            )
            android.util.Log.d("IcsImportUseCase", "Converted to ${courses.size} courses")
            
            // 收集匹配警告
            val warnings = mutableListOf<String>()
            courses.forEach { course ->
                if (course.isCustomTime) {
                    warnings.add("${course.name}: 无法匹配到节次，使用自定义时间")
                }
            }
            
            val result = ImportPreviewResult(
                courses = courses,
                generatedPeriods = generatedPeriods,
                matchingWarnings = warnings,
                suggestedSemesterStartDate = detectedSemesterStartDate
            )
            
            Result.success(result)
        } catch (e: IcsException) {
            android.util.Log.e("IcsImportUseCase", "ICS parsing error", e)
            Result.failure(e)
        } catch (e: Exception) {
            android.util.Log.e("IcsImportUseCase", "Unexpected error", e)
            Result.failure(Exception("解析失败: ${e.message}"))
        }
    }
    
    /**
     * 确认导入选中的课程
     */
    suspend fun confirmImport(
        courses: List<Course>,
        generatedPeriods: List<CoursePeriod>? = null,
        suggestedSemesterStartDate: java.time.LocalDate? = null,
        targetSemesterId: Long? = null
    ): Result<Int> = withContext(Dispatchers.IO) {
        try {
            // 如果有建议的学期开始日期，且提供了目标学期ID，更新学期
            if (suggestedSemesterStartDate != null && targetSemesterId != null) {
                try {
                    val semester = semesterRepository.getSemesterByIdSync(targetSemesterId)
                    if (semester != null && semester.startDate != suggestedSemesterStartDate) {
                        android.util.Log.d("IcsImportUseCase", "Updating semester start date to $suggestedSemesterStartDate")
                        semesterRepository.updateSemester(semester.copy(startDate = suggestedSemesterStartDate))
                        
                        // 同时更新CourseSettings中的学期开始日期
                        val currentSettings = courseSettingsRepository.getSettings().first()
                        val newSettings = currentSettings.copy(semesterStartDate = suggestedSemesterStartDate)
                        courseSettingsRepository.updateSettings(newSettings)
                    }
                } catch (e: Exception) {
                    android.util.Log.e("IcsImportUseCase", "Failed to update semester start date", e)
                }
            }

            // 如果有生成的节次，尝试保存配置
            if (generatedPeriods != null && generatedPeriods.isNotEmpty()) {
                try {
                    val settings = PeriodGenerator.convertToSettings(generatedPeriods)
                    // 保留原有的学期开始日期（如果上面已经更新了，这里应该使用最新的，或者合并）
                    // 由于convertToSettings返回的settings中semesterStartDate是null，我们需要合并
                    val currentSettings = courseSettingsRepository.getSettings().first()
                    
                    // 如果上面已经更新了日期，这里应该保留那个日期
                    // 如果上面没更新（比如没有建议日期），则保留currentSettings的日期
                    val finalStartDate = suggestedSemesterStartDate ?: currentSettings.semesterStartDate
                    
                    val newSettings = settings.copy(
                        semesterStartDate = finalStartDate,
                        // 保留其他可能不想覆盖的设置
                        showWeekends = currentSettings.showWeekends,
                        autoScrollToCurrentTime = currentSettings.autoScrollToCurrentTime,
                        highlightCurrentPeriod = currentSettings.highlightCurrentPeriod
                    )
                    
                    android.util.Log.d("IcsImportUseCase", "Saving generated course settings: $newSettings")
                    courseSettingsRepository.updateSettings(newSettings)
                } catch (e: Exception) {
                    android.util.Log.e("IcsImportUseCase", "Failed to save course settings", e)
                }
            }
            
            courseRepository.insertCourses(courses)
            Result.success(courses.size)
        } catch (e: Exception) {
            Result.failure(Exception("保存失败: ${e.message}"))
        }
    }
    
    /**
     * 检测重复课程
     * @param newCourses 要导入的课程
     * @param existingCourses 已存在的课程
     * @return 重复课程的警告信息列表
     */
    fun detectDuplicates(
        newCourses: List<Course>,
        existingCourses: List<Course>
    ): List<String> {
        val warnings = mutableListOf<String>()
        
        newCourses.forEach { newCourse ->
            val duplicate = existingCourses.find { existing ->
                existing.name == newCourse.name &&
                existing.dayOfWeek == newCourse.dayOfWeek &&
                existing.startTime == newCourse.startTime &&
                existing.endTime == newCourse.endTime
            }
            
            if (duplicate != null) {
                warnings.add("${newCourse.name} (${newCourse.dayOfWeek} ${newCourse.startTime}-${newCourse.endTime})")
            }
        }
        
        return warnings
    }
}

    /**
     * 应用自动生成的节次配置
     * 注意：这个方法目前只是一个占位符
     * 实际的节次配置保存需要通过CourseSettings来实现
     */
    suspend fun applyGeneratedPeriods(
        periods: List<CoursePeriod>
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // TODO: 实现节次配置的保存逻辑
            // 这需要将生成的节次转换为CourseSettings格式
            // 或者创建一个新的存储机制来保存自定义节次列表
            android.util.Log.d("IcsImportUseCase", "Generated periods to apply: ${periods.size}")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("保存节次配置失败: ${e.message}"))
        }
    }
