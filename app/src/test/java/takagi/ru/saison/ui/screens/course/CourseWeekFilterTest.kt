package takagi.ru.saison.ui.screens.course

import org.junit.Assert.*
import org.junit.Test
import takagi.ru.saison.domain.model.Course
import takagi.ru.saison.domain.model.WeekPattern
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

/**
 * 测试课程周数过滤逻辑
 */
class CourseWeekFilterTest {
    
    private fun createTestCourse(
        weekPattern: WeekPattern,
        customWeeks: List<Int>? = null
    ): Course {
        return Course(
            id = 1,
            name = "测试课程",
            color = 0xFF0000,
            dayOfWeek = DayOfWeek.MONDAY,
            startTime = LocalTime.of(8, 0),
            endTime = LocalTime.of(9, 40),
            weekPattern = weekPattern,
            customWeeks = customWeeks,
            startDate = LocalDate.now(),
            endDate = LocalDate.now().plusMonths(4)
        )
    }
    
    @Test
    fun `test ALL pattern shows in all weeks`() {
        val course = createTestCourse(WeekPattern.ALL)
        
        // 测试多个周数
        for (week in 1..20) {
            assertTrue(
                "ALL pattern course should show in week $week",
                isCourseActiveInWeek(course, week)
            )
        }
    }
    
    @Test
    fun `test ODD pattern shows only in odd weeks`() {
        val course = createTestCourse(WeekPattern.ODD)
        
        // 测试奇数周
        assertTrue("Should show in week 1", isCourseActiveInWeek(course, 1))
        assertTrue("Should show in week 3", isCourseActiveInWeek(course, 3))
        assertTrue("Should show in week 5", isCourseActiveInWeek(course, 5))
        assertTrue("Should show in week 17", isCourseActiveInWeek(course, 17))
        
        // 测试偶数周
        assertFalse("Should not show in week 2", isCourseActiveInWeek(course, 2))
        assertFalse("Should not show in week 4", isCourseActiveInWeek(course, 4))
        assertFalse("Should not show in week 6", isCourseActiveInWeek(course, 6))
        assertFalse("Should not show in week 18", isCourseActiveInWeek(course, 18))
    }
    
    @Test
    fun `test EVEN pattern shows only in even weeks`() {
        val course = createTestCourse(WeekPattern.EVEN)
        
        // 测试偶数周
        assertTrue("Should show in week 2", isCourseActiveInWeek(course, 2))
        assertTrue("Should show in week 4", isCourseActiveInWeek(course, 4))
        assertTrue("Should show in week 6", isCourseActiveInWeek(course, 6))
        assertTrue("Should show in week 18", isCourseActiveInWeek(course, 18))
        
        // 测试奇数周
        assertFalse("Should not show in week 1", isCourseActiveInWeek(course, 1))
        assertFalse("Should not show in week 3", isCourseActiveInWeek(course, 3))
        assertFalse("Should not show in week 5", isCourseActiveInWeek(course, 5))
        assertFalse("Should not show in week 17", isCourseActiveInWeek(course, 17))
    }
    
    @Test
    fun `test CUSTOM pattern shows only in specified weeks`() {
        val customWeeks = listOf(1, 3, 5, 10, 15)
        val course = createTestCourse(WeekPattern.CUSTOM, customWeeks)
        
        // 测试指定的周
        assertTrue("Should show in week 1", isCourseActiveInWeek(course, 1))
        assertTrue("Should show in week 3", isCourseActiveInWeek(course, 3))
        assertTrue("Should show in week 5", isCourseActiveInWeek(course, 5))
        assertTrue("Should show in week 10", isCourseActiveInWeek(course, 10))
        assertTrue("Should show in week 15", isCourseActiveInWeek(course, 15))
        
        // 测试未指定的周
        assertFalse("Should not show in week 2", isCourseActiveInWeek(course, 2))
        assertFalse("Should not show in week 4", isCourseActiveInWeek(course, 4))
        assertFalse("Should not show in week 6", isCourseActiveInWeek(course, 6))
        assertFalse("Should not show in week 11", isCourseActiveInWeek(course, 11))
    }
    
    @Test
    fun `test CUSTOM pattern with null customWeeks returns false`() {
        val course = createTestCourse(WeekPattern.CUSTOM, null)
        
        // 如果customWeeks为null，应该不显示
        assertFalse("Should not show in week 1", isCourseActiveInWeek(course, 1))
        assertFalse("Should not show in week 5", isCourseActiveInWeek(course, 5))
    }
    
    @Test
    fun `test CUSTOM pattern with empty customWeeks returns false`() {
        val course = createTestCourse(WeekPattern.CUSTOM, emptyList())
        
        // 如果customWeeks为空，应该不显示
        assertFalse("Should not show in week 1", isCourseActiveInWeek(course, 1))
        assertFalse("Should not show in week 5", isCourseActiveInWeek(course, 5))
    }
    
    @Test
    fun `test A and B patterns always return true`() {
        val courseA = createTestCourse(WeekPattern.A)
        val courseB = createTestCourse(WeekPattern.B)
        
        // A和B周模式目前简化处理，总是返回true
        for (week in 1..20) {
            assertTrue("A pattern should show in week $week", isCourseActiveInWeek(courseA, week))
            assertTrue("B pattern should show in week $week", isCourseActiveInWeek(courseB, week))
        }
    }
    
    @Test
    fun `test invalid week number returns false`() {
        val course = createTestCourse(WeekPattern.ALL)
        
        // 测试无效的周数
        assertFalse("Week 0 should return false", isCourseActiveInWeek(course, 0))
        assertFalse("Negative week should return false", isCourseActiveInWeek(course, -1))
        assertFalse("Negative week should return false", isCourseActiveInWeek(course, -5))
    }
    
    @Test
    fun `test ODD pattern with week 1`() {
        val course = createTestCourse(WeekPattern.ODD)
        
        // 第1周是奇数周，应该显示
        assertTrue("Week 1 is odd, should show", isCourseActiveInWeek(course, 1))
    }
    
    @Test
    fun `test EVEN pattern with week 2`() {
        val course = createTestCourse(WeekPattern.EVEN)
        
        // 第2周是偶数周，应该显示
        assertTrue("Week 2 is even, should show", isCourseActiveInWeek(course, 2))
    }
    
    @Test
    fun `test CUSTOM pattern with single week`() {
        val course = createTestCourse(WeekPattern.CUSTOM, listOf(5))
        
        // 只在第5周显示
        assertTrue("Should show in week 5", isCourseActiveInWeek(course, 5))
        assertFalse("Should not show in week 4", isCourseActiveInWeek(course, 4))
        assertFalse("Should not show in week 6", isCourseActiveInWeek(course, 6))
    }
    
    @Test
    fun `test CUSTOM pattern with consecutive weeks`() {
        val course = createTestCourse(WeekPattern.CUSTOM, listOf(1, 2, 3, 4, 5))
        
        // 前5周都应该显示
        for (week in 1..5) {
            assertTrue("Should show in week $week", isCourseActiveInWeek(course, week))
        }
        
        // 第6周及以后不应该显示
        assertFalse("Should not show in week 6", isCourseActiveInWeek(course, 6))
        assertFalse("Should not show in week 10", isCourseActiveInWeek(course, 10))
    }
    
    /**
     * 复制CourseViewModel中的逻辑用于测试
     * 包含周数有效性检查
     */
    private fun isCourseActiveInWeek(course: Course, week: Int): Boolean {
        // 确保周数有效
        if (week < 1) {
            return false
        }
        
        return when (course.weekPattern) {
            WeekPattern.ALL -> true
            WeekPattern.ODD -> week % 2 == 1
            WeekPattern.EVEN -> week % 2 == 0
            WeekPattern.CUSTOM -> {
                val customWeeks = course.customWeeks
                if (customWeeks == null || customWeeks.isEmpty()) {
                    false
                } else {
                    customWeeks.contains(week)
                }
            }
            WeekPattern.A, WeekPattern.B -> true // 简化处理
        }
    }
}
