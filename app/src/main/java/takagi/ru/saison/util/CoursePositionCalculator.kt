package takagi.ru.saison.util

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import takagi.ru.saison.domain.model.Course
import takagi.ru.saison.domain.model.CourseGridPosition
import takagi.ru.saison.domain.model.CoursePeriod

/**
 * 课程位置计算工具类
 * 用于计算课程在网格布局中的位置和高度
 */
object CoursePositionCalculator {
    
    /**
     * 计算课程在网格中的位置
     * 
     * @param course 课程信息
     * @param periods 节次列表
     * @param cellHeight 单元格高度
     * @return 课程网格位置(偏移和高度)
     */
    fun calculateCoursePosition(
        course: Course,
        periods: List<CoursePeriod>,
        cellHeight: Dp
    ): CourseGridPosition {
        val startPeriod = course.periodStart ?: 1
        val endPeriod = course.periodEnd ?: 1
        
        // 边界检查
        if (startPeriod < 1 || endPeriod < startPeriod || periods.isEmpty()) {
            return CourseGridPosition(
                offsetY = 0.dp,
                height = cellHeight
            )
        }
        
        // 计算偏移量(从第几节开始)
        // 第1节的偏移为0,第2节的偏移为cellHeight,以此类推
        val offsetY = cellHeight * (startPeriod - 1) + (2.dp * (startPeriod - 1))
        
        // 计算高度(跨越几节课)
        val spanCount = endPeriod - startPeriod + 1
        val height = cellHeight * spanCount + (2.dp * (spanCount - 1))
        
        return CourseGridPosition(
            offsetY = offsetY,
            height = height
        )
    }
    
    /**
     * 检查课程是否在指定节次范围内
     * 
     * @param course 课程信息
     * @param periodNumber 节次编号
     * @return 是否在该节次
     */
    fun isCourseInPeriod(course: Course, periodNumber: Int): Boolean {
        val start = course.periodStart ?: return false
        val end = course.periodEnd ?: return false
        return periodNumber in start..end
    }
    
    /**
     * 获取指定节次的所有课程
     * 
     * @param courses 课程列表
     * @param periodNumber 节次编号
     * @return 该节次的课程列表
     */
    fun getCoursesInPeriod(courses: List<Course>, periodNumber: Int): List<Course> {
        return courses.filter { course ->
            isCourseInPeriod(course, periodNumber)
        }
    }
}
