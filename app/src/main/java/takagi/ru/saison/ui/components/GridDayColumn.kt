package takagi.ru.saison.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import takagi.ru.saison.domain.model.Course
import takagi.ru.saison.domain.model.CoursePeriod
import takagi.ru.saison.util.CoursePositionCalculator
import java.time.DayOfWeek

/**
 * 网格日期列组件
 * 显示单个工作日的所有节次和课程
 * 
 * @param day 星期
 * @param courses 该天的课程列表
 * @param periods 节次列表
 * @param cellHeight 单元格高度
 * @param onCourseClick 课程点击回调
 * @param onEmptyCellClick 空白单元格点击回调
 * @param currentPeriod 当前节次
 * @param modifier 修饰符
 */
@Composable
fun GridDayColumn(
    day: DayOfWeek,
    courses: List<Course>,
    periods: List<CoursePeriod>,
    cellHeight: Dp,
    onCourseClick: (Long) -> Unit,
    onEmptyCellClick: (Int) -> Unit,
    currentPeriod: Int? = null,
    modifier: Modifier = Modifier
) {
    // 缓存课程位置计算结果
    val coursePositions = remember(courses, periods, cellHeight) {
        courses.associateWith { course ->
            CoursePositionCalculator.calculateCoursePosition(course, periods, cellHeight)
        }
    }
    
    // 检测每个节次的课程数量(用于冲突检测)
    val coursesPerPeriod = remember(courses, periods) {
        periods.associate { period ->
            period.periodNumber to CoursePositionCalculator.getCoursesInPeriod(
                courses,
                period.periodNumber
            )
        }
    }
    
    Box(
        modifier = modifier.fillMaxWidth()
    ) {
        // 背景层: 绘制所有网格单元格
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            periods.forEach { period ->
                val coursesInPeriod = coursesPerPeriod[period.periodNumber] ?: emptyList()
                val isEmpty = coursesInPeriod.isEmpty()
                
                GridCell(
                    period = period,
                    isEmpty = isEmpty,
                    onClick = { onEmptyCellClick(period.periodNumber) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(cellHeight)
                )
            }
        }
        
        // 前景层: 使用绝对定位放置课程卡片
        Box(modifier = Modifier.fillMaxWidth()) {
            courses.forEachIndexed { index, course ->
                val position = coursePositions[course] ?: return@forEachIndexed
                
                // 检测冲突
                val conflictingCourses = coursesPerPeriod.values.firstOrNull { 
                    it.size > 1 && course in it 
                }
                val hasConflict = conflictingCourses != null
                
                // 如果有冲突,调整宽度和偏移以实现并排或重叠显示
                val width = if (hasConflict) {
                    val conflictCount = conflictingCourses?.size ?: 1
                    if (conflictCount == 2) 0.48f else 0.9f
                } else {
                    1f
                }
                
                val offsetX = if (hasConflict) {
                    val conflictIndex = conflictingCourses?.indexOf(course) ?: 0
                    (conflictIndex * 8).dp
                } else {
                    0.dp
                }
                
                // 检查是否为当前正在进行的课程
                val isCurrentlyActive = currentPeriod != null && 
                    CoursePositionCalculator.isCourseInPeriod(course, currentPeriod)
                
                CourseGridCard(
                    course = course,
                    position = position,
                    isCurrentlyActive = isCurrentlyActive,
                    hasConflict = hasConflict,
                    onClick = { onCourseClick(course.id) },
                    modifier = Modifier
                        .fillMaxWidth(width)
                        .offset(x = offsetX, y = position.offsetY)
                )
            }
        }
    }
}
