package takagi.ru.saison.util

import androidx.compose.ui.graphics.Color
import takagi.ru.saison.data.local.datastore.SeasonalTheme
import takagi.ru.saison.ui.theme.ThemeCoursePalettes

/**
 * 课程颜色映射器
 * 将存储的颜色整数值映射到当前主题的对应颜色
 * 
 * 颜色编码格式：
 * - 最高4位（28-31位）：颜色索引 (0-11)
 * - 其余28位：原始颜色值（用于向后兼容）
 * 
 * 示例：
 * ```
 * val colorInt = (5 shl 28) or 0x0FFFFFFF  // 索引5，原始颜色值
 * val index = CourseColorMapper.extractColorIndex(colorInt)  // 返回 5
 * ```
 */
object CourseColorMapper {
    
    /**
     * 从颜色整数值提取颜色索引
     * 
     * @param colorInt 存储的颜色整数值
     * @return 颜色索引 (0-11)，如果无效则返回0
     */
    fun extractColorIndex(colorInt: Int): Int {
        // 提取最高4位作为索引
        val index = (colorInt ushr 28) and 0xF
        
        // 验证索引有效性（0-11）
        return if (index in 0..11) {
            index
        } else {
            // 旧数据或无效索引，返回默认值0
            0
        }
    }
    
    /**
     * 获取当前主题的课程调色板
     * 
     * @param theme 当前主题
     * @param isDark 是否为深色模式
     * @return 12种协调的课程颜色列表
     */
    fun getThemeCoursePalette(
        theme: SeasonalTheme,
        isDark: Boolean
    ): List<Color> {
        return when (theme) {
            SeasonalTheme.SAKURA -> if (isDark) ThemeCoursePalettes.sakuraDark else ThemeCoursePalettes.sakuraLight
            SeasonalTheme.MINT -> if (isDark) ThemeCoursePalettes.mintDark else ThemeCoursePalettes.mintLight
            SeasonalTheme.AMBER -> if (isDark) ThemeCoursePalettes.amberDark else ThemeCoursePalettes.amberLight
            SeasonalTheme.SNOW -> if (isDark) ThemeCoursePalettes.snowDark else ThemeCoursePalettes.snowLight
            SeasonalTheme.RAIN -> if (isDark) ThemeCoursePalettes.rainDark else ThemeCoursePalettes.rainLight
            SeasonalTheme.MAPLE -> if (isDark) ThemeCoursePalettes.mapleDark else ThemeCoursePalettes.mapleLight
            SeasonalTheme.OCEAN -> if (isDark) ThemeCoursePalettes.oceanDark else ThemeCoursePalettes.oceanLight
            SeasonalTheme.SUNSET -> if (isDark) ThemeCoursePalettes.sunsetDark else ThemeCoursePalettes.sunsetLight
            SeasonalTheme.FOREST -> if (isDark) ThemeCoursePalettes.forestDark else ThemeCoursePalettes.forestLight
            SeasonalTheme.LAVENDER -> if (isDark) ThemeCoursePalettes.lavenderDark else ThemeCoursePalettes.lavenderLight
            SeasonalTheme.DESERT -> if (isDark) ThemeCoursePalettes.desertDark else ThemeCoursePalettes.desertLight
            SeasonalTheme.AURORA -> if (isDark) ThemeCoursePalettes.auroraDark else ThemeCoursePalettes.auroraLight
            SeasonalTheme.DYNAMIC -> if (isDark) ThemeCoursePalettes.dynamicDark else ThemeCoursePalettes.dynamicLight
            SeasonalTheme.AUTO_SEASONAL -> {
                // AUTO_SEASONAL 应该在调用前被解析为具体主题
                // 这里作为后备，使用樱花主题
                if (isDark) ThemeCoursePalettes.sakuraDark else ThemeCoursePalettes.sakuraLight
            }
        }
    }
    
    /**
     * 将课程颜色整数值映射到当前主题的颜色
     * 
     * @param colorInt 存储的颜色整数值
     * @param theme 当前主题
     * @param isDark 是否为深色模式
     * @return 主题感知的 Color 对象
     */
    fun mapToThemeColor(
        colorInt: Int,
        theme: SeasonalTheme,
        isDark: Boolean
    ): Color {
        // 提取颜色索引
        val index = extractColorIndex(colorInt)
        
        // 获取当前主题的调色板
        val palette = getThemeCoursePalette(theme, isDark)
        
        // 返回对应索引的颜色
        return palette.getOrElse(index) {
            // 如果索引超出范围（理论上不应该发生），返回第一个颜色
            palette.firstOrNull() ?: Color.Gray
        }
    }
    
    /**
     * 编码颜色索引和颜色值到整数
     * 用于创建新课程时存储颜色
     * 
     * @param colorIndex 颜色索引 (0-11)
     * @param colorValue 原始颜色值（用于向后兼容）
     * @return 编码后的颜色整数值
     */
    fun encodeColor(colorIndex: Int, colorValue: Int): Int {
        // 确保索引在有效范围内
        val validIndex = colorIndex.coerceIn(0, 11)
        
        // 将索引编码到最高4位，保留原始颜色值的低28位
        return (validIndex shl 28) or (colorValue and 0x0FFFFFFF)
    }
    
    /**
     * 从编码的颜色值中提取原始颜色（用于向后兼容）
     * 
     * @param colorInt 编码的颜色整数值
     * @return 原始颜色值
     */
    fun extractOriginalColor(colorInt: Int): Int {
        // 提取低28位作为原始颜色值
        return colorInt and 0x0FFFFFFF
    }
}
