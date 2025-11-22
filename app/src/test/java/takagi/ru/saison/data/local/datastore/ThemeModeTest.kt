package takagi.ru.saison.data.local.datastore

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * ThemeMode 枚举测试
 */
class ThemeModeTest {
    
    @Test
    fun `fromString should return correct ThemeMode`() {
        assertEquals(ThemeMode.FOLLOW_SYSTEM, ThemeMode.fromString("FOLLOW_SYSTEM"))
        assertEquals(ThemeMode.LIGHT, ThemeMode.fromString("LIGHT"))
        assertEquals(ThemeMode.DARK, ThemeMode.fromString("DARK"))
    }
    
    @Test
    fun `fromString should return FOLLOW_SYSTEM for invalid string`() {
        assertEquals(ThemeMode.FOLLOW_SYSTEM, ThemeMode.fromString("INVALID"))
        assertEquals(ThemeMode.FOLLOW_SYSTEM, ThemeMode.fromString(""))
    }
    
    @Test
    fun `all ThemeMode values should have valid resource IDs`() {
        ThemeMode.values().forEach { mode ->
            assert(mode.displayNameRes > 0) { "displayNameRes should be valid for $mode" }
            assert(mode.descriptionRes > 0) { "descriptionRes should be valid for $mode" }
        }
    }
}
