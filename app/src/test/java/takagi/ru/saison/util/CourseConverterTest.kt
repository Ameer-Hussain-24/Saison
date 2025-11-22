package takagi.ru.saison.util

import androidx.compose.ui.graphics.Color
import org.junit.Assert.*
import org.junit.Test
import takagi.ru.saison.domain.model.WeekPattern
import takagi.ru.saison.domain.model.ics.ParsedCourse
import takagi.ru.saison.domain.model.ics.RecurrenceInfo
import java.time.LocalDate
import java.time.LocalDateTime

class CourseConverterTest {
    
    @Test
    fun `toCourse converts ParsedCourse correctly`() {
        val parsed = ParsedCourse(
            summary = "测试课程",
            location = "测试教室",
            description = "第7-8节\n测试教室",
            dtStart = LocalDateTime.of(2025, 8, 27, 15, 30),
            dtEnd = LocalDateTime.of(2025, 8, 27, 17, 0),
            rrule = RecurrenceInfo(
                frequency = "WEEKLY",
                interval = 1,
                until = LocalDateTime.of(2025, 12, 31, 16, 0),
                byDay = null
            ),
            alarmMinutes = 20
        )
        
        val course = CourseConverter.toCourse(
            parsed = parsed,
            semesterId = 1L,
            semesterStartDate = LocalDate.of(2025, 8, 25),
            primaryColor = Color.Blue,
            existingCourses = emptyList()
        )
        
        assertEquals("测试课程", course.name)
        assertEquals("测试教室", course.location)
        assertEquals(7, course.periodStart)
        assertEquals(8, course.periodEnd)
        assertEquals(20, course.notificationMinutes)
    }
    
    @Test
    fun `groupAndConvert merges same courses with custom weeks`() {
        val parsed1 = ParsedCourse(
            summary = "数学",
            location = "教室A",
            description = null,
            dtStart = LocalDateTime.of(2025, 8, 27, 8, 0),
            dtEnd = LocalDateTime.of(2025, 8, 27, 9, 30),
            rrule = RecurrenceInfo("WEEKLY", 1, LocalDateTime.of(2025, 9, 2, 16, 0), null),
            alarmMinutes = 10
        )
        
        val parsed2 = ParsedCourse(
            summary = "数学",
            location = "教室A",
            description = null,
            dtStart = LocalDateTime.of(2025, 9, 10, 8, 0),
            dtEnd = LocalDateTime.of(2025, 9, 10, 9, 30),
            rrule = RecurrenceInfo("WEEKLY", 1, LocalDateTime.of(2025, 9, 16, 16, 0), null),
            alarmMinutes = 10
        )
        
        val courses = CourseConverter.groupAndConvert(
            parsedList = listOf(parsed1, parsed2),
            semesterId = 1L,
            semesterStartDate = LocalDate.of(2025, 8, 25),
            primaryColor = Color.Blue
        )
        
        // 应该合并为一个课程
        assertEquals(1, courses.size)
        val course = courses[0]
        assertEquals("数学", course.name)
        assertEquals(WeekPattern.CUSTOM, course.weekPattern)
        assertNotNull(course.customWeeks)
        assertTrue(course.customWeeks!!.isNotEmpty())
    }
    
    @Test
    fun `groupAndConvert keeps separate courses with different names`() {
        val parsed1 = ParsedCourse(
            summary = "数学",
            location = "教室A",
            description = null,
            dtStart = LocalDateTime.of(2025, 8, 27, 8, 0),
            dtEnd = LocalDateTime.of(2025, 8, 27, 9, 30),
            rrule = null,
            alarmMinutes = 10
        )
        
        val parsed2 = ParsedCourse(
            summary = "英语",
            location = "教室B",
            description = null,
            dtStart = LocalDateTime.of(2025, 8, 27, 10, 0),
            dtEnd = LocalDateTime.of(2025, 8, 27, 11, 30),
            rrule = null,
            alarmMinutes = 10
        )
        
        val courses = CourseConverter.groupAndConvert(
            parsedList = listOf(parsed1, parsed2),
            semesterId = 1L,
            semesterStartDate = LocalDate.of(2025, 8, 25),
            primaryColor = Color.Blue
        )
        
        // 应该保持为两个独立的课程
        assertEquals(2, courses.size)
        assertEquals("数学", courses[0].name)
        assertEquals("英语", courses[1].name)
    }
}
