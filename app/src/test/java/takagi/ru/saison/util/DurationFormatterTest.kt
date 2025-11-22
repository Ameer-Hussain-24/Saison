package takagi.ru.saison.util

import org.junit.Assert.*
import org.junit.Test

class DurationFormatterTest {
    
    @Test
    fun `formatDuration formats hours and minutes correctly`() {
        assertEquals("1小时30分钟", DurationFormatter.formatDuration(90))
        assertEquals("2小时15分钟", DurationFormatter.formatDuration(135))
    }
    
    @Test
    fun `formatDuration formats hours only`() {
        assertEquals("1小时", DurationFormatter.formatDuration(60))
        assertEquals("2小时", DurationFormatter.formatDuration(120))
    }
    
    @Test
    fun `formatDuration formats minutes only`() {
        assertEquals("15分钟", DurationFormatter.formatDuration(15))
        assertEquals("45分钟", DurationFormatter.formatDuration(45))
    }
    
    @Test
    fun `formatDuration handles zero and negative values`() {
        assertEquals("0分钟", DurationFormatter.formatDuration(0))
        assertEquals("0分钟", DurationFormatter.formatDuration(-10))
    }
    
    @Test
    fun `toMinutes converts hours and minutes correctly`() {
        assertEquals(90, DurationFormatter.toMinutes(1, 30))
        assertEquals(135, DurationFormatter.toMinutes(2, 15))
        assertEquals(60, DurationFormatter.toMinutes(1, 0))
        assertEquals(45, DurationFormatter.toMinutes(0, 45))
    }
    
    @Test
    fun `fromMinutes converts total minutes correctly`() {
        assertEquals(Pair(1, 30), DurationFormatter.fromMinutes(90))
        assertEquals(Pair(2, 15), DurationFormatter.fromMinutes(135))
        assertEquals(Pair(1, 0), DurationFormatter.fromMinutes(60))
        assertEquals(Pair(0, 45), DurationFormatter.fromMinutes(45))
        assertEquals(Pair(0, 0), DurationFormatter.fromMinutes(0))
    }
    
    @Test
    fun `toMinutes and fromMinutes are inverse operations`() {
        val testCases = listOf(
            Pair(0, 0),
            Pair(0, 15),
            Pair(1, 0),
            Pair(1, 30),
            Pair(2, 45),
            Pair(5, 15)
        )
        
        testCases.forEach { (hours, minutes) ->
            val totalMinutes = DurationFormatter.toMinutes(hours, minutes)
            val (h, m) = DurationFormatter.fromMinutes(totalMinutes)
            assertEquals(hours, h)
            assertEquals(minutes, m)
        }
    }
}
