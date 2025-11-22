package takagi.ru.saison.data.ics

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import takagi.ru.saison.domain.model.ics.RecurrenceInfo
import java.time.LocalDateTime

class IcsParserTest {
    
    private lateinit var parser: IcsParser
    
    @Before
    fun setup() {
        parser = IcsParser()
    }
    
    @Test
    fun `parse valid ICS file with single event`() {
        val icsContent = """
            BEGIN:VCALENDAR
            VERSION:2.0
            PRODID:-//Test//Test//EN
            BEGIN:VEVENT
            SUMMARY:测试课程
            DTSTART:20250827T153000
            DTEND:20250827T170000
            LOCATION:测试教室
            DESCRIPTION:第7-8节
            END:VEVENT
            END:VCALENDAR
        """.trimIndent()
        
        val result = parser.parse(icsContent)
        
        assertEquals(1, result.size)
        val course = result[0]
        assertEquals("测试课程", course.summary)
        assertEquals("测试教室", course.location)
        assertEquals(LocalDateTime.of(2025, 8, 27, 15, 30), course.dtStart)
        assertEquals(LocalDateTime.of(2025, 8, 27, 17, 0), course.dtEnd)
    }
    
    @Test
    fun `parse ICS file with RRULE`() {
        val icsContent = """
            BEGIN:VCALENDAR
            VERSION:2.0
            BEGIN:VEVENT
            SUMMARY:周课程
            DTSTART:20250827T153000
            DTEND:20250827T170000
            RRULE:FREQ=WEEKLY;UNTIL=20251231T160000Z;INTERVAL=1
            END:VEVENT
            END:VCALENDAR
        """.trimIndent()
        
        val result = parser.parse(icsContent)
        
        assertEquals(1, result.size)
        val course = result[0]
        assertNotNull(course.rrule)
        assertEquals("WEEKLY", course.rrule?.frequency)
        assertEquals(1, course.rrule?.interval)
        assertNotNull(course.rrule?.until)
    }
    
    @Test
    fun `parse ICS file with VALARM`() {
        val icsContent = """
            BEGIN:VCALENDAR
            VERSION:2.0
            BEGIN:VEVENT
            SUMMARY:有提醒的课程
            DTSTART:20250827T153000
            DTEND:20250827T170000
            BEGIN:VALARM
            ACTION:DISPLAY
            TRIGGER;RELATED=START:-PT20M
            END:VALARM
            END:VEVENT
            END:VCALENDAR
        """.trimIndent()
        
        val result = parser.parse(icsContent)
        
        assertEquals(1, result.size)
        val course = result[0]
        assertEquals(20, course.alarmMinutes)
    }
    
    @Test(expected = IcsException.EmptyFile::class)
    fun `parse empty ICS file throws exception`() {
        parser.parse("")
    }
    
    @Test(expected = IcsException.EmptyFile::class)
    fun `parse ICS file without events throws exception`() {
        val icsContent = """
            BEGIN:VCALENDAR
            VERSION:2.0
            END:VCALENDAR
        """.trimIndent()
        
        parser.parse(icsContent)
    }
    
    @Test
    fun `parse ICS file with multiple events`() {
        val icsContent = """
            BEGIN:VCALENDAR
            VERSION:2.0
            BEGIN:VEVENT
            SUMMARY:课程1
            DTSTART:20250827T080000
            DTEND:20250827T093000
            END:VEVENT
            BEGIN:VEVENT
            SUMMARY:课程2
            DTSTART:20250827T100000
            DTEND:20250827T113000
            END:VEVENT
            END:VCALENDAR
        """.trimIndent()
        
        val result = parser.parse(icsContent)
        
        assertEquals(2, result.size)
        assertEquals("课程1", result[0].summary)
        assertEquals("课程2", result[1].summary)
    }
    
    @Test
    fun `parse ICS file with timezone`() {
        val icsContent = """
            BEGIN:VCALENDAR
            VERSION:2.0
            BEGIN:VEVENT
            SUMMARY:带时区的课程
            DTSTART;TZID=Asia/Shanghai:20250827T153000
            DTEND;TZID=Asia/Shanghai:20250827T170000
            END:VEVENT
            END:VCALENDAR
        """.trimIndent()
        
        val result = parser.parse(icsContent)
        
        assertEquals(1, result.size)
        assertEquals(LocalDateTime.of(2025, 8, 27, 15, 30), result[0].dtStart)
    }
}
