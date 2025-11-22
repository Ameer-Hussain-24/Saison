package takagi.ru.saison.util

import androidx.compose.ui.graphics.Color
import org.junit.Assert.*
import org.junit.Test
import takagi.ru.saison.domain.model.Course
import takagi.ru.saison.domain.model.WeekPattern
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

/**
 * CourseColorAssigner 单元测试
 * 测试颜色分配逻辑和索引编码
 */
class CourseColorAssignerTest {
    
    private val testPrimaryColor = Color(0xFFE91E63)  // 粉红色
    private val testStartDate = LocalDate.of(2024, 9, 1)
    private val testEndDate = LocalDate.of(2025, 1, 31)
    
    private fun createTestCourse(
        id: Long = 1,
        name: String = "Test Course",
        dayOfWeek: DayOfWeek = DayOfWeek.MONDAY,
        startTime: LocalTime = LocalTime.of(8, 0),
        endTime: LocalTime = LocalTime.of(10, 0),
        color: Int = 0
    ): Course {
        return Course(
            id = id,
            name = name,
            dayOfWeek = dayOfWeek,
            startTime = startTime,
            endTime = endTime,
            color = color,
            startDate = testStartDate,
            endDate = testEndDate,
            weekPattern = WeekPattern.ALL
        )
    }
    
    @Test
    fun `assignColor should return encoded color with valid index`() {
        val existingCourses = emptyList<Course>()
        val assignedColor = CourseColorAssigner.assignColor(
            existingCourses,
            DayOfWeek.MONDAY,
            LocalTime.of(8, 0),
            LocalTime.of(10, 0),
            testPrimaryColor
        )
        
        // 验证返回的颜色包含有效的索引
        val index = CourseColorMapper.extractColorIndex(assignedColor)
        assertTrue("Assigned color should have valid index", index in 0..11)
    }
    
    @Test
    fun `assignColor should assign index 0 for first course`() {
        val existingCourses = emptyList<Course>()
        val assignedColor = CourseColorAssigner.assignColor(
            existingCourses,
            DayOfWeek.MONDAY,
            LocalTime.of(8, 0),
            LocalTime.of(10, 0),
            testPrimaryColor
        )
        
        val index = CourseColorMapper.extractColorIndex(assignedColor)
        assertEquals("First course should get index 0", 0, index)
    }
    
    @Test
    fun `assignColor should avoid adjacent course colors vertically`() {
        // 创建一个已存在的课程（同一天，时间相邻）
        val existingCourse = createTestCourse(
            id = 1,
            dayOfWeek = DayOfWeek.MONDAY,
            startTime = LocalTime.of(10, 0),
            endTime = LocalTime.of(12, 0),
            color = CourseColorMapper.encodeColor(0, 0xFF000000.toInt())  // 索引 0
        )
        
        val assignedColor = CourseColorAssigner.assignColor(
            listOf(existingCourse),
            DayOfWeek.MONDAY,
            startTime = LocalTime.of(8, 0),
            endTime = LocalTime.of(10, 0),
            testPrimaryColor
        )
        
        val assignedIndex = CourseColorMapper.extractColorIndex(assignedColor)
        val existingIndex = CourseColorMapper.extractColorIndex(existingCourse.color)
        
        assertNotEquals("Adjacent courses should have different color indices", 
            existingIndex, assignedIndex)
    }
    
    @Test
    fun `assignColor should avoid adjacent course colors horizontally`() {
        // 创建一个已存在的课程（不同天，时间重叠）
        val existingCourse = createTestCourse(
            id = 1,
            dayOfWeek = DayOfWeek.TUESDAY,
            startTime = LocalTime.of(8, 0),
            endTime = LocalTime.of(10, 0),
            color = CourseColorMapper.encodeColor(0, 0xFF000000.toInt())  // 索引 0
        )
        
        val assignedColor = CourseColorAssigner.assignColor(
            listOf(existingCourse),
            DayOfWeek.MONDAY,
            startTime = LocalTime.of(8, 0),
            endTime = LocalTime.of(10, 0),
            testPrimaryColor
        )
        
        val assignedIndex = CourseColorMapper.extractColorIndex(assignedColor)
        val existingIndex = CourseColorMapper.extractColorIndex(existingCourse.color)
        
        assertNotEquals("Horizontally adjacent courses should have different color indices", 
            existingIndex, assignedIndex)
    }
    
    @Test
    fun `assignColor should handle all indices being used`() {
        // 创建12个课程，占用所有索引
        val existingCourses = (0..11).map { index ->
            createTestCourse(
                id = index.toLong(),
                dayOfWeek = DayOfWeek.MONDAY,
                startTime = LocalTime.of(8 + index, 0),
                endTime = LocalTime.of(9 + index, 0),
                color = CourseColorMapper.encodeColor(index, 0xFF000000.toInt())
            )
        }
        
        // 尝试分配一个与所有课程相邻的新课程
        val assignedColor = CourseColorAssigner.assignColor(
            existingCourses,
            DayOfWeek.MONDAY,
            startTime = LocalTime.of(8, 0),
            endTime = LocalTime.of(10, 0),
            testPrimaryColor
        )
        
        // 应该使用基于哈希的索引选择
        val assignedIndex = CourseColorMapper.extractColorIndex(assignedColor)
        assertTrue("Should assign a valid index even when all are used", assignedIndex in 0..11)
    }
    
    @Test
    fun `assignColor should not consider non-adjacent courses`() {
        // 创建一个不相邻的课程（同一天，但时间不相邻）
        val existingCourse = createTestCourse(
            id = 1,
            dayOfWeek = DayOfWeek.MONDAY,
            startTime = LocalTime.of(14, 0),  // 远离新课程时间
            endTime = LocalTime.of(16, 0),
            color = CourseColorMapper.encodeColor(0, 0xFF000000.toInt())  // 索引 0
        )
        
        val assignedColor = CourseColorAssigner.assignColor(
            listOf(existingCourse),
            DayOfWeek.MONDAY,
            startTime = LocalTime.of(8, 0),
            endTime = LocalTime.of(10, 0),
            testPrimaryColor
        )
        
        val assignedIndex = CourseColorMapper.extractColorIndex(assignedColor)
        
        // 由于不相邻，可能会分配相同的索引
        // 这里只验证分配了有效的索引
        assertTrue("Should assign a valid index", assignedIndex in 0..11)
    }
    
    @Test
    fun `assignColor should handle courses on different days without time overlap`() {
        // 创建一个不同天且时间不重叠的课程
        val existingCourse = createTestCourse(
            id = 1,
            dayOfWeek = DayOfWeek.TUESDAY,
            startTime = LocalTime.of(14, 0),
            endTime = LocalTime.of(16, 0),
            color = CourseColorMapper.encodeColor(0, 0xFF000000.toInt())
        )
        
        val assignedColor = CourseColorAssigner.assignColor(
            listOf(existingCourse),
            DayOfWeek.MONDAY,
            startTime = LocalTime.of(8, 0),
            endTime = LocalTime.of(10, 0),
            testPrimaryColor
        )
        
        val assignedIndex = CourseColorMapper.extractColorIndex(assignedColor)
        
        // 由于不相邻，可能会分配相同的索引
        assertTrue("Should assign a valid index", assignedIndex in 0..11)
    }
    
    @Test
    fun `assignColor should handle multiple adjacent courses`() {
        // 创建多个相邻的课程
        val existingCourses = listOf(
            createTestCourse(
                id = 1,
                dayOfWeek = DayOfWeek.MONDAY,
                startTime = LocalTime.of(10, 0),
                endTime = LocalTime.of(12, 0),
                color = CourseColorMapper.encodeColor(0, 0xFF000000.toInt())
            ),
            createTestCourse(
                id = 2,
                dayOfWeek = DayOfWeek.TUESDAY,
                startTime = LocalTime.of(8, 0),
                endTime = LocalTime.of(10, 0),
                color = CourseColorMapper.encodeColor(1, 0xFF000000.toInt())
            ),
            createTestCourse(
                id = 3,
                dayOfWeek = DayOfWeek.MONDAY,
                startTime = LocalTime.of(6, 0),
                endTime = LocalTime.of(8, 0),
                color = CourseColorMapper.encodeColor(2, 0xFF000000.toInt())
            )
        )
        
        val assignedColor = CourseColorAssigner.assignColor(
            existingCourses,
            DayOfWeek.MONDAY,
            startTime = LocalTime.of(8, 0),
            endTime = LocalTime.of(10, 0),
            testPrimaryColor
        )
        
        val assignedIndex = CourseColorMapper.extractColorIndex(assignedColor)
        val usedIndices = existingCourses.map { CourseColorMapper.extractColorIndex(it.color) }
        
        // 应该分配一个未被相邻课程使用的索引
        assertFalse("Should not use indices from adjacent courses", 
            assignedIndex in usedIndices)
    }
    
    @Test
    fun `assignColor should be deterministic for same input`() {
        val existingCourses = listOf(
            createTestCourse(
                id = 1,
                dayOfWeek = DayOfWeek.MONDAY,
                startTime = LocalTime.of(10, 0),
                endTime = LocalTime.of(12, 0),
                color = CourseColorMapper.encodeColor(0, 0xFF000000.toInt())
            )
        )
        
        val color1 = CourseColorAssigner.assignColor(
            existingCourses,
            DayOfWeek.MONDAY,
            LocalTime.of(8, 0),
            LocalTime.of(10, 0),
            testPrimaryColor
        )
        
        val color2 = CourseColorAssigner.assignColor(
            existingCourses,
            DayOfWeek.MONDAY,
            LocalTime.of(8, 0),
            LocalTime.of(10, 0),
            testPrimaryColor
        )
        
        assertEquals("Same input should produce same color", color1, color2)
    }
}
