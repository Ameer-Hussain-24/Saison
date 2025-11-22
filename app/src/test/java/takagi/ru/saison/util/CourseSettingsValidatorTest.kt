package takagi.ru.saison.util

import org.junit.Assert.*
import org.junit.Test
import takagi.ru.saison.domain.model.CourseSettings
import java.time.LocalTime

class CourseSettingsValidatorTest {
    
    @Test
    fun `validatePeriodsPerDay returns true for valid values`() {
        assertTrue(CourseSettingsValidator.validatePeriodsPerDay(1))
        assertTrue(CourseSettingsValidator.validatePeriodsPerDay(8))
        assertTrue(CourseSettingsValidator.validatePeriodsPerDay(12))
    }
    
    @Test
    fun `validatePeriodsPerDay returns false for invalid values`() {
        assertFalse(CourseSettingsValidator.validatePeriodsPerDay(0))
        assertFalse(CourseSettingsValidator.validatePeriodsPerDay(13))
        assertFalse(CourseSettingsValidator.validatePeriodsPerDay(-1))
    }
    
    @Test
    fun `validateMorningPeriods returns true for valid values`() {
        assertTrue(CourseSettingsValidator.validateMorningPeriods(1))
        assertTrue(CourseSettingsValidator.validateMorningPeriods(4))
        assertTrue(CourseSettingsValidator.validateMorningPeriods(6))
    }
    
    @Test
    fun `validateMorningPeriods returns false for invalid values`() {
        assertFalse(CourseSettingsValidator.validateMorningPeriods(0))
        assertFalse(CourseSettingsValidator.validateMorningPeriods(7))
        assertFalse(CourseSettingsValidator.validateMorningPeriods(-1))
    }
    
    @Test
    fun `validateAfternoonPeriods returns true for valid values`() {
        assertTrue(CourseSettingsValidator.validateAfternoonPeriods(1))
        assertTrue(CourseSettingsValidator.validateAfternoonPeriods(4))
        assertTrue(CourseSettingsValidator.validateAfternoonPeriods(6))
    }
    
    @Test
    fun `validateAfternoonPeriods returns false for invalid values`() {
        assertFalse(CourseSettingsValidator.validateAfternoonPeriods(0))
        assertFalse(CourseSettingsValidator.validateAfternoonPeriods(7))
        assertFalse(CourseSettingsValidator.validateAfternoonPeriods(-1))
    }
    
    @Test
    fun `validateEveningPeriods returns true for valid values`() {
        assertTrue(CourseSettingsValidator.validateEveningPeriods(0))
        assertTrue(CourseSettingsValidator.validateEveningPeriods(2))
        assertTrue(CourseSettingsValidator.validateEveningPeriods(3))
    }
    
    @Test
    fun `validateEveningPeriods returns false for invalid values`() {
        assertFalse(CourseSettingsValidator.validateEveningPeriods(-1))
        assertFalse(CourseSettingsValidator.validateEveningPeriods(4))
        assertFalse(CourseSettingsValidator.validateEveningPeriods(5))
    }
    
    @Test
    fun `validateTotalPeriods returns true for valid combinations`() {
        assertTrue(CourseSettingsValidator.validateTotalPeriods(4, 4, 0))  // 8 total
        assertTrue(CourseSettingsValidator.validateTotalPeriods(4, 4, 2))  // 10 total
        assertTrue(CourseSettingsValidator.validateTotalPeriods(1, 1, 0))  // 2 total
        assertTrue(CourseSettingsValidator.validateTotalPeriods(6, 6, 0))  // 12 total
    }
    
    @Test
    fun `validateTotalPeriods returns false for invalid combinations`() {
        assertFalse(CourseSettingsValidator.validateTotalPeriods(6, 6, 3))  // 15 total (too many)
        assertFalse(CourseSettingsValidator.validateTotalPeriods(0, 0, 0))  // 0 total (too few)
    }
    
    @Test
    fun `validatePeriodDuration returns true for valid values`() {
        assertTrue(CourseSettingsValidator.validatePeriodDuration(30))
        assertTrue(CourseSettingsValidator.validatePeriodDuration(45))
        assertTrue(CourseSettingsValidator.validatePeriodDuration(120))
    }
    
    @Test
    fun `validatePeriodDuration returns false for invalid values`() {
        assertFalse(CourseSettingsValidator.validatePeriodDuration(25))
        assertFalse(CourseSettingsValidator.validatePeriodDuration(43))
        assertFalse(CourseSettingsValidator.validatePeriodDuration(125))
    }
    
    @Test
    fun `validateBreakDuration returns true for valid values`() {
        assertTrue(CourseSettingsValidator.validateBreakDuration(5))
        assertTrue(CourseSettingsValidator.validateBreakDuration(10))
        assertTrue(CourseSettingsValidator.validateBreakDuration(30))
    }
    
    @Test
    fun `validateBreakDuration returns false for invalid values`() {
        assertFalse(CourseSettingsValidator.validateBreakDuration(3))
        assertFalse(CourseSettingsValidator.validateBreakDuration(12))
        assertFalse(CourseSettingsValidator.validateBreakDuration(35))
    }
    
    @Test
    fun `validateDinnerBreakDuration returns true for valid values`() {
        assertTrue(CourseSettingsValidator.validateDinnerBreakDuration(30))
        assertTrue(CourseSettingsValidator.validateDinnerBreakDuration(60))
        assertTrue(CourseSettingsValidator.validateDinnerBreakDuration(120))
    }
    
    @Test
    fun `validateDinnerBreakDuration returns false for invalid values`() {
        assertFalse(CourseSettingsValidator.validateDinnerBreakDuration(25))
        assertFalse(CourseSettingsValidator.validateDinnerBreakDuration(43))
        assertFalse(CourseSettingsValidator.validateDinnerBreakDuration(125))
    }
    
    @Test
    fun `validatePeriodRange returns true for valid ranges`() {
        assertTrue(CourseSettingsValidator.validatePeriodRange(1, 1, 8))
        assertTrue(CourseSettingsValidator.validatePeriodRange(1, 4, 8))
        assertTrue(CourseSettingsValidator.validatePeriodRange(3, 5, 8))
    }
    
    @Test
    fun `validatePeriodRange returns false for invalid ranges`() {
        assertFalse(CourseSettingsValidator.validatePeriodRange(0, 1, 8))
        assertFalse(CourseSettingsValidator.validatePeriodRange(1, 9, 8))
        assertFalse(CourseSettingsValidator.validatePeriodRange(5, 3, 8))
    }
    
    @Test
    fun `validateSettings returns valid for default settings`() {
        val settings = CourseSettings()
        val result = CourseSettingsValidator.validateSettings(settings)
        assertTrue(result.isValid)
        assertNull(result.errorMessage)
    }
    
    @Test
    fun `validateSettings returns invalid for bad morningPeriods`() {
        val settings = CourseSettings(morningPeriods = 7, afternoonPeriods = 4, eveningPeriods = 0)
        val result = CourseSettingsValidator.validateSettings(settings)
        assertFalse(result.isValid)
        assertNotNull(result.errorMessage)
        assertTrue(result.errorMessage!!.contains("上午节次数"))
    }
    
    @Test
    fun `validateSettings returns invalid for bad afternoonPeriods`() {
        val settings = CourseSettings(morningPeriods = 4, afternoonPeriods = 7, eveningPeriods = 0)
        val result = CourseSettingsValidator.validateSettings(settings)
        assertFalse(result.isValid)
        assertNotNull(result.errorMessage)
        assertTrue(result.errorMessage!!.contains("下午节次数"))
    }
    
    @Test
    fun `validateSettings returns invalid for bad eveningPeriods`() {
        val settings = CourseSettings(morningPeriods = 4, afternoonPeriods = 4, eveningPeriods = 4)
        val result = CourseSettingsValidator.validateSettings(settings)
        assertFalse(result.isValid)
        assertNotNull(result.errorMessage)
        assertTrue(result.errorMessage!!.contains("晚上节次数"))
    }
    
    @Test
    fun `validateSettings returns invalid for too many total periods`() {
        val settings = CourseSettings(morningPeriods = 6, afternoonPeriods = 6, eveningPeriods = 3)
        val result = CourseSettingsValidator.validateSettings(settings)
        assertFalse(result.isValid)
        assertNotNull(result.errorMessage)
        assertTrue(result.errorMessage!!.contains("总节次数"))
    }
    
    @Test
    fun `validateSettings returns invalid for bad periodDuration`() {
        val settings = CourseSettings(periodDuration = 43)
        val result = CourseSettingsValidator.validateSettings(settings)
        assertFalse(result.isValid)
        assertNotNull(result.errorMessage)
    }
    
    @Test
    fun `validateSettings returns invalid for bad dinnerBreakDuration`() {
        val settings = CourseSettings(dinnerBreakDuration = 125)
        val result = CourseSettingsValidator.validateSettings(settings)
        assertFalse(result.isValid)
        assertNotNull(result.errorMessage)
        assertTrue(result.errorMessage!!.contains("晚休时长"))
    }
    
    @Test
    fun `validateSettings returns valid for valid segmented periods`() {
        val settings = CourseSettings(
            morningPeriods = 4,
            afternoonPeriods = 4,
            eveningPeriods = 2
        )
        val result = CourseSettingsValidator.validateSettings(settings)
        assertTrue(result.isValid)
        assertNull(result.errorMessage)
    }
}
