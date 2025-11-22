package takagi.ru.saison.domain.usecase

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import takagi.ru.saison.data.ics.IcsException
import takagi.ru.saison.data.ics.IcsGenerator
import takagi.ru.saison.data.repository.CourseRepository
import takagi.ru.saison.data.repository.SemesterRepository
import takagi.ru.saison.domain.model.Course
import javax.inject.Inject

/**
 * ICS导出用例
 */
class IcsExportUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val icsGenerator: IcsGenerator,
    private val courseRepository: CourseRepository,
    private val semesterRepository: SemesterRepository
) {
    
    /**
     * 导出学期的所有课程
     * @param semesterId 学期ID
     * @param fileName 文件名（可选）
     * @return 导出文件的Uri
     */
    suspend fun exportSemester(
        semesterId: Long,
        fileName: String? = null
    ): Result<Uri> = withContext(Dispatchers.IO) {
        try {
            // 获取学期信息
            val semester = semesterRepository.getSemesterByIdSync(semesterId)
                ?: return@withContext Result.failure(Exception("学期不存在"))
            
            // 获取该学期的所有课程
            val courses = courseRepository.getCoursesBySemester(semesterId).first()
            
            if (courses.isEmpty()) {
                return@withContext Result.failure(Exception("该学期没有课程"))
            }
            
            // 生成文件名
            val finalFileName = fileName ?: "courses_${semester.name}"
            
            // 生成并写入ICS文件
            val uri = icsGenerator.writeToFile(
                context = context,
                courses = courses,
                semesterName = semester.name,
                fileName = finalFileName
            )
            
            Result.success(uri)
        } catch (e: IcsException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(Exception("导出失败: ${e.message}"))
        }
    }
    
    /**
     * 导出选中的课程
     */
    suspend fun exportCourses(
        courses: List<Course>,
        fileName: String
    ): Result<Uri> = withContext(Dispatchers.IO) {
        try {
            if (courses.isEmpty()) {
                return@withContext Result.failure(Exception("没有选中的课程"))
            }
            
            // 获取学期名称
            val semesterId = courses.first().semesterId
            val semester = semesterRepository.getSemesterByIdSync(semesterId)
            val semesterName = semester?.name ?: "课程表"
            
            // 生成并写入ICS文件
            val uri = icsGenerator.writeToFile(
                context = context,
                courses = courses,
                semesterName = semesterName,
                fileName = fileName
            )
            
            Result.success(uri)
        } catch (e: IcsException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(Exception("导出失败: ${e.message}"))
        }
    }
    
    /**
     * 生成ICS内容（用于分享）
     */
    suspend fun generateIcsContent(
        semesterId: Long
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            // 获取学期信息
            val semester = semesterRepository.getSemesterByIdSync(semesterId)
                ?: return@withContext Result.failure(Exception("学期不存在"))
            
            // 获取该学期的所有课程
            val courses = courseRepository.getCoursesBySemester(semesterId).first()
            
            if (courses.isEmpty()) {
                return@withContext Result.failure(Exception("该学期没有课程"))
            }
            
            // 生成ICS内容
            val content = icsGenerator.generate(
                courses = courses,
                semesterName = semester.name
            )
            
            Result.success(content)
        } catch (e: Exception) {
            Result.failure(Exception("生成失败: ${e.message}"))
        }
    }
}
