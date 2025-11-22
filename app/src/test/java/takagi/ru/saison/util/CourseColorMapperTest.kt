package takagi.ru.saison.util

import androidx.compose.ui.graphics.Color
import org.junit.Assert.*
import org.junit.Test
import takagi.ru.saison.data.local.datastore.SeasonalTheme

/**
 * CourseColorMapper 单元测试
 * 测试颜色索引提取、主题调色板获取和颜色映射功能
 */
class CourseColorMapperTest {
    
    @Test
    fun `extractColorIndex should extract correct index from encoded color`() {
        // 测试索引 0-11
        for (index in 0..11) {
            val encodedColor = (index shl 28) or 0x0FFFFFFF
            val extractedIndex = CourseColorMapper.extractColorIndex(encodedColor)
            assertEquals("Index $index should be extracted correctly", index, extractedIndex)
        }
    }
    
    @Test
    fun `extractColorIndex should return 0 for invalid index`() {
        // 测试无效索引（12-15）
        for (index in 12..15) {
            val encodedColor = (index shl 28) or 0x0FFFFFFF
            val extractedIndex = CourseColorMapper.extractColorIndex(encodedColor)
            assertEquals("Invalid index should return 0", 0, extractedIndex)
        }
    }
    
    @Test
    fun `extractColorIndex should return 0 for old format color without index`() {
        // 旧格式颜色值（没有编码索引）
        val oldColorValue = 0x0FFFFFFF
        val extractedIndex = CourseColorMapper.extractColorIndex(oldColorValue)
        assertEquals("Old format color should return default index 0", 0, extractedIndex)
    }
    
    @Test
    fun `getThemeCoursePalette should return 12 colors for each theme`() {
        val themes = listOf(
            SeasonalTheme.SAKURA,
            SeasonalTheme.MINT,
            SeasonalTheme.AMBER,
            SeasonalTheme.SNOW,
            SeasonalTheme.RAIN,
            SeasonalTheme.MAPLE,
            SeasonalTheme.OCEAN,
            SeasonalTheme.SUNSET,
            SeasonalTheme.FOREST,
            SeasonalTheme.LAVENDER,
            SeasonalTheme.DESERT,
            SeasonalTheme.AURORA,
            SeasonalTheme.DYNAMIC
        )
        
        for (theme in themes) {
            // 测试浅色模式
            val lightPalette = CourseColorMapper.getThemeCoursePalette(theme, isDark = false)
            assertEquals("$theme light palette should have 12 colors", 12, lightPalette.size)
            
            // 测试深色模式
            val darkPalette = CourseColorMapper.getThemeCoursePalette(theme, isDark = true)
            assertEquals("$theme dark palette should have 12 colors", 12, darkPalette.size)
        }
    }
    
    @Test
    fun `getThemeCoursePalette should return different colors for light and dark mode`() {
        val theme = SeasonalTheme.SAKURA
        val lightPalette = CourseColorMapper.getThemeCoursePalette(theme, isDark = false)
        val darkPalette = CourseColorMapper.getThemeCoursePalette(theme, isDark = true)
        
        // 至少应该有一些颜色不同
        val differentColors = lightPalette.zip(darkPalette).count { (light, dark) -> light != dark }
        assertTrue("Light and dark palettes should have different colors", differentColors > 0)
    }
    
    @Test
    fun `mapToThemeColor should return correct color for valid index`() {
        val theme = SeasonalTheme.SAKURA
        val isDark = false
        val palette = CourseColorMapper.getThemeCoursePalette(theme, isDark)
        
        for (index in 0..11) {
            val encodedColor = (index shl 28) or 0x0FFFFFFF
            val mappedColor = CourseColorMapper.mapToThemeColor(encodedColor, theme, isDark)
            assertEquals("Mapped color should match palette color at index $index", 
                palette[index], mappedColor)
        }
    }
    
    @Test
    fun `mapToThemeColor should handle theme changes correctly`() {
        val encodedColor = (5 shl 28) or 0x0FFFFFFF  // 索引 5
        
        // 测试不同主题返回不同颜色
        val sakuraColor = CourseColorMapper.mapToThemeColor(encodedColor, SeasonalTheme.SAKURA, false)
        val mintColor = CourseColorMapper.mapToThemeColor(encodedColor, SeasonalTheme.MINT, false)
        
        // 不同主题的同一索引应该返回不同的颜色
        assertNotEquals("Different themes should return different colors", sakuraColor, mintColor)
    }
    
    @Test
    fun `mapToThemeColor should handle dark mode changes correctly`() {
        val encodedColor = (5 shl 28) or 0x0FFFFFFF  // 索引 5
        val theme = SeasonalTheme.SAKURA
        
        val lightColor = CourseColorMapper.mapToThemeColor(encodedColor, theme, isDark = false)
        val darkColor = CourseColorMapper.mapToThemeColor(encodedColor, theme, isDark = true)
        
        // 同一主题的浅色和深色模式应该返回不同的颜色
        assertNotEquals("Light and dark modes should return different colors", lightColor, darkColor)
    }
    
    @Test
    fun `encodeColor should correctly encode index and color value`() {
        val colorIndex = 7
        val colorValue = 0x0FFFFFFF
        
        val encodedColor = CourseColorMapper.encodeColor(colorIndex, colorValue)
        
        // 验证索引被正确编码
        val extractedIndex = CourseColorMapper.extractColorIndex(encodedColor)
        assertEquals("Encoded index should match original", colorIndex, extractedIndex)
        
        // 验证颜色值被保留
        val extractedColor = CourseColorMapper.extractOriginalColor(encodedColor)
        assertEquals("Encoded color value should match original", colorValue, extractedColor)
    }
    
    @Test
    fun `encodeColor should coerce index to valid range`() {
        // 测试超出范围的索引
        val tooLargeIndex = 15
        val tooSmallIndex = -1
        val colorValue = 0x0FFFFFFF
        
        val encodedLarge = CourseColorMapper.encodeColor(tooLargeIndex, colorValue)
        val extractedLarge = CourseColorMapper.extractColorIndex(encodedLarge)
        assertTrue("Too large index should be coerced to valid range", extractedLarge in 0..11)
        
        val encodedSmall = CourseColorMapper.encodeColor(tooSmallIndex, colorValue)
        val extractedSmall = CourseColorMapper.extractColorIndex(encodedSmall)
        assertTrue("Too small index should be coerced to valid range", extractedSmall in 0..11)
    }
    
    @Test
    fun `extractOriginalColor should extract correct color value`() {
        val colorIndex = 3
        val originalColorValue = 0x0ABCDEF0
        
        val encodedColor = CourseColorMapper.encodeColor(colorIndex, originalColorValue)
        val extractedColor = CourseColorMapper.extractOriginalColor(encodedColor)
        
        // 由于只保留低28位，需要比较低28位
        val expectedColor = originalColorValue and 0x0FFFFFFF
        assertEquals("Extracted color should match original (low 28 bits)", 
            expectedColor, extractedColor)
    }
    
    @Test
    fun `mapToThemeColor should handle AUTO_SEASONAL theme`() {
        val encodedColor = (5 shl 28) or 0x0FFFFFFF
        
        // AUTO_SEASONAL 应该回退到樱花主题
        val autoSeasonalColor = CourseColorMapper.mapToThemeColor(
            encodedColor, SeasonalTheme.AUTO_SEASONAL, false
        )
        val sakuraColor = CourseColorMapper.mapToThemeColor(
            encodedColor, SeasonalTheme.SAKURA, false
        )
        
        assertEquals("AUTO_SEASONAL should fallback to SAKURA", sakuraColor, autoSeasonalColor)
    }
    
    @Test
    fun `palette colors should be valid Color objects`() {
        val theme = SeasonalTheme.SAKURA
        val palette = CourseColorMapper.getThemeCoursePalette(theme, isDark = false)
        
        for ((index, color) in palette.withIndex()) {
            // 验证颜色的 ARGB 值在有效范围内
            assertTrue("Color at index $index should have valid alpha", 
                color.alpha in 0f..1f)
            assertTrue("Color at index $index should have valid red", 
                color.red in 0f..1f)
            assertTrue("Color at index $index should have valid green", 
                color.green in 0f..1f)
            assertTrue("Color at index $index should have valid blue", 
                color.blue in 0f..1f)
        }
    }
}
