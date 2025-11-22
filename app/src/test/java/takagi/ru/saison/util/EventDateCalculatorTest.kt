package takagi.ru.saison.util

import android.content.Context
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import takagi.ru.saison.R
import java.time.LocalDate
import java.time.LocalDateTime

class EventDateCalculatorTest {
    
    @Mock
    private lateinit var mockContext: Context
    
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        
        // Mock string resources
        `when`(mockContext.getString(R.string.event_today)).thenReturn("今天")
        `when`(mockContext.getString(R.string.event_days_remaining, 5)).thenReturn("还有 5 天")
        `when`(mockContext.getString(R.string.event_days_passed, 3)).thenReturn("已过去 3 天")
    }
    
    @Test
    fun `calculateDaysUntil returns 0 for today`() {
        val today = LocalDateTime.now()
        val result = EventDateCalculator.calculateDaysUntil(today)
        assertEquals(0, result)
    }
    
    @Test
    fun `calculateDaysUntil returns positive number for future date`() {
        val futureDate = LocalDateTime.now().plusDays(5)
        val result = EventDateCalculator.calculateDaysUntil(futureDate)
        assertEquals(5, result)
    }
    
    @Test
    fun `calculateDaysUntil returns negative number for past date`() {
        val pastDate = LocalDateTime.now().minusDays(3)
        val result = EventDateCalculator.calculateDaysUntil(pastDate)
        assertEquals(-3, result)
    }
    
    @Test
    fun `calculateDaysUntil ignores time component`() {
        val today = LocalDate.now()
        val morningTime = today.atTime(8, 0)
        val eveningTime = today.atTime(20, 0)
        
        assertEquals(0, EventDateCalculator.calculateDaysUntil(morningTime))
        assertEquals(0, EventDateCalculator.calculateDaysUntil(eveningTime))
    }
    
    @Test
    fun `formatDaysText returns correct text for today`() {
        val result = EventDateCalculator.formatDaysText(0, mockContext)
        assertEquals("今天", result)
    }
    
    @Test
    fun `formatDaysText returns correct text for future date`() {
        val result = EventDateCalculator.formatDaysText(5, mockContext)
        assertEquals("还有 5 天", result)
    }
    
    @Test
    fun `formatDaysText returns correct text for past date`() {
        val result = EventDateCalculator.formatDaysText(-3, mockContext)
        assertEquals("已过去 3 天", result)
    }
    
    @Test
    fun `isPastEvent returns true for past date`() {
        val pastDate = LocalDateTime.now().minusDays(1)
        assertTrue(EventDateCalculator.isPastEvent(pastDate))
    }
    
    @Test
    fun `isPastEvent returns false for today`() {
        val today = LocalDateTime.now()
        assertFalse(EventDateCalculator.isPastEvent(today))
    }
    
    @Test
    fun `isPastEvent returns false for future date`() {
        val futureDate = LocalDateTime.now().plusDays(1)
        assertFalse(EventDateCalculator.isPastEvent(futureDate))
    }
    
    @Test
    fun `isToday returns true for today`() {
        val today = LocalDateTime.now()
        assertTrue(EventDateCalculator.isToday(today))
    }
    
    @Test
    fun `isToday returns false for past date`() {
        val pastDate = LocalDateTime.now().minusDays(1)
        assertFalse(EventDateCalculator.isToday(pastDate))
    }
    
    @Test
    fun `isToday returns false for future date`() {
        val futureDate = LocalDateTime.now().plusDays(1)
        assertFalse(EventDateCalculator.isToday(futureDate))
    }
    
    @Test
    fun `isFutureEvent returns true for future date`() {
        val futureDate = LocalDateTime.now().plusDays(1)
        assertTrue(EventDateCalculator.isFutureEvent(futureDate))
    }
    
    @Test
    fun `isFutureEvent returns false for today`() {
        val today = LocalDateTime.now()
        assertFalse(EventDateCalculator.isFutureEvent(today))
    }
    
    @Test
    fun `isFutureEvent returns false for past date`() {
        val pastDate = LocalDateTime.now().minusDays(1)
        assertFalse(EventDateCalculator.isFutureEvent(pastDate))
    }
    
    @Test
    fun `calculateDaysUntil handles large positive numbers`() {
        val farFuture = LocalDateTime.now().plusDays(365)
        val result = EventDateCalculator.calculateDaysUntil(farFuture)
        assertEquals(365, result)
    }
    
    @Test
    fun `calculateDaysUntil handles large negative numbers`() {
        val farPast = LocalDateTime.now().minusDays(365)
        val result = EventDateCalculator.calculateDaysUntil(farPast)
        assertEquals(-365, result)
    }
}
