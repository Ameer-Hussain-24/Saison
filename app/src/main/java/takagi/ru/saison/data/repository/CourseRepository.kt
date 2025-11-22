package takagi.ru.saison.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import takagi.ru.saison.data.local.database.dao.CourseDao
import takagi.ru.saison.domain.mapper.toDomain
import takagi.ru.saison.domain.mapper.toEntity
import takagi.ru.saison.domain.model.Course
import java.time.DayOfWeek
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CourseRepository @Inject constructor(
    private val courseDao: CourseDao
) {
    
    fun getAllCourses(): Flow<List<Course>> {
        return courseDao.getAllCoursesFlow().map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    fun getCoursesByDay(dayOfWeek: DayOfWeek): Flow<List<Course>> {
        return courseDao.getCoursesByDay(dayOfWeek.value).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    fun getActiveCourses(currentDate: Long): Flow<List<Course>> {
        return courseDao.getActiveCourses(currentDate).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    suspend fun getCourseById(courseId: Long): Course? {
        return courseDao.getCourseById(courseId)?.toDomain()
    }
    
    suspend fun insertCourse(course: Course): Long {
        return courseDao.insert(course.toEntity())
    }
    
    suspend fun insertCourses(courses: List<Course>): List<Long> {
        return courseDao.insertAll(courses.map { it.toEntity() })
    }
    
    suspend fun updateCourse(course: Course) {
        courseDao.update(course.toEntity())
    }
    
    suspend fun deleteCourse(courseId: Long) {
        courseDao.deleteById(courseId)
    }
    
    suspend fun deleteExpiredCourses() {
        val currentTime = System.currentTimeMillis()
        courseDao.deleteExpiredCourses(currentTime)
    }
    
    // Semester-related methods
    fun getCoursesBySemester(semesterId: Long): Flow<List<Course>> {
        return courseDao.getCoursesBySemester(semesterId).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    suspend fun getCourseCountBySemester(semesterId: Long): Int {
        return courseDao.getCourseCountBySemester(semesterId)
    }
    
    suspend fun moveCourseToSemester(courseId: Long, semesterId: Long) {
        courseDao.moveCourseToSemester(courseId, semesterId)
    }
    
    suspend fun deleteCoursesBySemester(semesterId: Long) {
        courseDao.deleteCoursesBySemester(semesterId)
    }
}
