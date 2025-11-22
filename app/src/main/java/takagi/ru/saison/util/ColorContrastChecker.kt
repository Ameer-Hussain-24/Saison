package takagi.ru.saison.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import kotlin.math.pow

/**
 * 颜色对比度检查工具
 * 
 * 用于验证颜色组合是否符合 WCAG 2.1 无障碍标准
 */
object ColorContrastChecker {
    
    /**
     * 计算两个颜色之间的对比度
     * 
     * @param foreground 前景色（文本颜色）
     * @param background 背景色
     * @return 对比度值（1.0 到 21.0）
     */
    fun calculateContrast(foreground: Color, background: Color): Float {
        val fgLuminance = foreground.luminance()
        val bgLuminance = background.luminance()
        
        val lighter = maxOf(fgLuminance, bgLuminance)
        val darker = minOf(fgLuminance, bgLuminance)
        
        return (lighter + 0.05f) / (darker + 0.05f)
    }
    
    /**
     * 检查对比度是否符合 WCAG AA 标准
     * 
     * @param foreground 前景色（文本颜色）
     * @param background 背景色
     * @param isLargeText 是否为大文本（18pt+ 或 14pt+ 粗体）
     * @return true 如果符合 WCAG AA 标准
     */
    fun meetsWCAGAA(
        foreground: Color,
        background: Color,
        isLargeText: Boolean = false
    ): Boolean {
        val contrast = calculateContrast(foreground, background)
        return if (isLargeText) contrast >= 3.0f else contrast >= 4.5f
    }
    
    /**
     * 检查对比度是否符合 WCAG AAA 标准
     * 
     * @param foreground 前景色（文本颜色）
     * @param background 背景色
     * @param isLargeText 是否为大文本（18pt+ 或 14pt+ 粗体）
     * @return true 如果符合 WCAG AAA 标准
     */
    fun meetsWCAGAAA(
        foreground: Color,
        background: Color,
        isLargeText: Boolean = false
    ): Boolean {
        val contrast = calculateContrast(foreground, background)
        return if (isLargeText) contrast >= 4.5f else contrast >= 7.0f
    }
    
    /**
     * 获取对比度等级描述
     * 
     * @param contrast 对比度值
     * @param isLargeText 是否为大文本
     * @return 对比度等级描述
     */
    fun getContrastLevel(contrast: Float, isLargeText: Boolean = false): ContrastLevel {
        return when {
            isLargeText -> when {
                contrast >= 4.5f -> ContrastLevel.AAA
                contrast >= 3.0f -> ContrastLevel.AA
                else -> ContrastLevel.FAIL
            }
            else -> when {
                contrast >= 7.0f -> ContrastLevel.AAA
                contrast >= 4.5f -> ContrastLevel.AA
                else -> ContrastLevel.FAIL
            }
        }
    }
    
    /**
     * 验证颜色方案的对比度
     * 
     * @param primary 主色
     * @param onPrimary 主色上的文本颜色
     * @param background 背景色
     * @param onBackground 背景上的文本颜色
     * @param surface 表面色
     * @param onSurface 表面上的文本颜色
     * @return 验证结果
     */
    fun validateColorScheme(
        primary: Color,
        onPrimary: Color,
        background: Color,
        onBackground: Color,
        surface: Color,
        onSurface: Color
    ): ColorSchemeValidationResult {
        val results = mutableListOf<ContrastCheckResult>()
        
        // 检查主色对比度
        results.add(
            ContrastCheckResult(
                name = "Primary / OnPrimary",
                foreground = onPrimary,
                background = primary,
                contrast = calculateContrast(onPrimary, primary),
                meetsAA = meetsWCAGAA(onPrimary, primary),
                meetsAAA = meetsWCAGAAA(onPrimary, primary)
            )
        )
        
        // 检查背景对比度
        results.add(
            ContrastCheckResult(
                name = "Background / OnBackground",
                foreground = onBackground,
                background = background,
                contrast = calculateContrast(onBackground, background),
                meetsAA = meetsWCAGAA(onBackground, background),
                meetsAAA = meetsWCAGAAA(onBackground, background)
            )
        )
        
        // 检查表面对比度
        results.add(
            ContrastCheckResult(
                name = "Surface / OnSurface",
                foreground = onSurface,
                background = surface,
                contrast = calculateContrast(onSurface, surface),
                meetsAA = meetsWCAGAA(onSurface, surface),
                meetsAAA = meetsWCAGAAA(onSurface, surface)
            )
        )
        
        val allMeetAA = results.all { it.meetsAA }
        val allMeetAAA = results.all { it.meetsAAA }
        
        return ColorSchemeValidationResult(
            results = results,
            overallMeetsAA = allMeetAA,
            overallMeetsAAA = allMeetAAA
        )
    }
    
    /**
     * 对比度等级
     */
    enum class ContrastLevel {
        /** 符合 WCAG AAA 标准 */
        AAA,
        /** 符合 WCAG AA 标准 */
        AA,
        /** 不符合标准 */
        FAIL
    }
    
    /**
     * 对比度检查结果
     */
    data class ContrastCheckResult(
        val name: String,
        val foreground: Color,
        val background: Color,
        val contrast: Float,
        val meetsAA: Boolean,
        val meetsAAA: Boolean
    ) {
        val level: ContrastLevel
            get() = when {
                meetsAAA -> ContrastLevel.AAA
                meetsAA -> ContrastLevel.AA
                else -> ContrastLevel.FAIL
            }
    }
    
    /**
     * 颜色方案验证结果
     */
    data class ColorSchemeValidationResult(
        val results: List<ContrastCheckResult>,
        val overallMeetsAA: Boolean,
        val overallMeetsAAA: Boolean
    ) {
        val failedChecks: List<ContrastCheckResult>
            get() = results.filter { !it.meetsAA }
        
        val warningChecks: List<ContrastCheckResult>
            get() = results.filter { it.meetsAA && !it.meetsAAA }
    }
}
