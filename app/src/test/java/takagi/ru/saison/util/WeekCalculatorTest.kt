package takagi.ru.saison.util

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

/**
 * WeekCalculator 单元测试
 */
class WeekCalculatorTest {
    
    private lateinit var weekCalculator: WeekCalculator
    
    @Before
    fun setup() {
        weekCalculator = WeekCalculator()
    }
    
    @Test
    fun `calculateCurrentWeek returns 1 for semester start date`() {
        val semesterStart = LocalDate.of(2024, 9, 1)
        val currentDate = LocalDate.of(2024, 9, 1)
        
        val result = weekCalculator.calculateCurrentWeek(semesterStart, currentDate)
        
        assertEquals(1, result)
    }
    
    @Test
    fun `calculateCurrentWeek returns correct week number`() {
        val semesterStart = LocalDate.of(2024, 9, 1)
        val currentDate = LocalDate.of(2024, 9, 15) // 14 days later
        
        val result = weekCalculator.calculateCurrentWeek(semesterStart, currentDate)
        
        assertEquals(3, result) // 14 days / 7 = 2 weeks, so week 3
    }
    
    @Test
    fun `calculateCurrentWeek handles exact week boundaries`() {
        val semesterStart = LocalDate.of(2024, 9, 1)
        val currentDate = LocalDate.of(2024, 9, 8) // Exactly 7 days later
        
        val result = weekCalculator.calculateCurrentWeek(semesterStart, currentDate)
        
        assertEquals(2, result)
    }
    
    @Test
    fun `calculateCurrentWeek handles mid-week dates`() {
        val semesterStart = LocalDate.of(2024, 9, 1)
        val currentDate = LocalDate.of(2024, 9, 10) // 9 days later (mid-week 2)
        
        val result = weekCalculator.calculateCurrentWeek(semesterStart, currentDate)
        
        assertEquals(2, result)
    }
    
    @Test
    fun `calculateWeeksFromDateRange returns correct week list`() {
        val semesterStart = LocalDate.of(2024, 9, 1)
        val rangeStart = LocalDate.of(2024, 9, 1)
        val rangeEnd = LocalDate.of(2024, 9, 21) // 3 weeks
        
        val result = weekCalculator.calculateWeeksFromDateRange(
            semesterStart,
            rangeStart,
            rangeEnd
        )
        
        assertEquals(listOf(1, 2, 3), result)
    }
    
    @Test
    fun `calculateWeeksFromDateRange handles single week`() {
        val semesterStart = LocalDate.of(2024, 9, 1)
        val rangeStart = LocalDate.of(2024, 9, 8)
        val rangeEnd = LocalDate.of(2024, 9, 14)
        
        val result = weekCalculator.calculateWeeksFromDateRange(
            semesterStart,
            rangeStart,
            rangeEnd
        )
        
        assertEquals(listOf(2), result)
    }
    
    @Test
    fun `calculateWeeksFromDateRange handles non-contiguous weeks`() {
        val semesterStart = LocalDate.of(2024, 9, 1)
        val rangeStart = LocalDate.of(2024, 9, 15) // Week 3
        val rangeEnd = LocalDate.of(2024, 10, 5) // Week 5
        
        val result = weekCalculator.calculateWeeksFromDateRange(
            semesterStart,
            rangeStart,
            rangeEnd
        )
        
        assertEquals(listOf(3, 4, 5), result)
    }
    
    @Test
    fun `getWeekDateRange returns correct date range for week 1`() {
        val semesterStart = LocalDate.of(2024, 9, 1)
        
        val (start, end) = weekCalculator.getWeekDateRange(semesterStart, 1)
        
        assertEquals(LocalDate.of(2024, 9, 1), start)
        assertEquals(LocalDate.of(2024, 9, 7), end)
    }
    
    @Test
    fun `getWeekDateRange returns correct date range for week 3`() {
        val semesterStart = LocalDate.of(2024, 9, 1)
        
        val (start, end) = weekCalculator.getWeekDateRange(semesterStart, 3)
        
        assertEquals(LocalDate.of(2024, 9, 15), start)
        assertEquals(LocalDate.of(2024, 9, 21), end)
    }
    
    @Test
    fun `getWeekDateRange handles large week numbers`() {
        val semesterStart = LocalDate.of(2024, 9, 1)
        
        val (start, end) = weekCalculator.getWeekDateRange(semesterStart, 18)
        
        assertEquals(LocalDate.of(2024, 12, 29), start)
        assertEquals(LocalDate.of(2025, 1, 4), end)
    }
    
    @Test
    fun `isDateInWeeks returns true when date is in specified weeks`() {
        val semesterStart = LocalDate.of(2024, 9, 1)
        val date = LocalDate.of(2024, 9, 10) // Week 2
        val weeks = listOf(1, 2, 3)
        
        val result = weekCalculator.isDateInWeeks(date, semesterStart, weeks)
        
        assertTrue(result)
    }
    
    @Test
    fun `isDateInWeeks returns false when date is not in specified weeks`() {
        val semesterStart = LocalDate.of(2024, 9, 1)
        val date = LocalDate.of(2024, 9, 22) // Week 4
        val weeks = listOf(1, 2, 3)
        
        val result = weekCalculator.isDateInWeeks(date, semesterStart, weeks)
        
        assertFalse(result)
    }
    
    @Test
    fun `isDateInWeeks handles empty week list`() {
        val semesterStart = LocalDate.of(2024, 9, 1)
        val date = LocalDate.of(2024, 9, 10)
        val weeks = emptyList<Int>()
        
        val result = weekCalculator.isDateInWeeks(date, semesterStart, weeks)
        
        assertFalse(result)
    }
    
    @Test
    fun `calculateCurrentWeek handles dates before semester start`() {
        val semesterStart = LocalDate.of(2024, 9, 1)
        val currentDate = LocalDate.of(2024, 8, 25) // 7 days before
        
        val result = weekCalculator.calculateCurrentWeek(semesterStart, currentDate)
        
        assertEquals(0, result) // Week 0 (before semester)
    }
    
    @Test
    fun `calculateWeeksFromDateRange handles cross-year boundary`() {
        val semesterStart = LocalDate.of(2024, 9, 1)
        val rangeStart = LocalDate.of(2024, 12, 29) // Week 18
        val rangeEnd = LocalDate.of(2025, 1, 11) // Week 19
        
        val result = weekCalculator.calculateWeeksFromDateRange(
            semesterStart,
            rangeStart,
            rangeEnd
        )
        
        assertEquals(listOf(18, 19), result)
    }
}
