package takagi.ru.saison.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import takagi.ru.saison.domain.model.Course
import java.time.DayOfWeek
import java.time.LocalTime

/**
 * 课程颜色智能分配器
 * 确保纵向和横向相邻的课程使用不同的颜色
 * 颜色基于用户选择的主题色动态生成变种
 */
object CourseColorAssigner {
    
    /**
     * 根据主题色生成调色板
     * @param primaryColor 主题色
     * @return 12种基于主题色的变种色
     */
    fun generateColorPalette(primaryColor: Color): List<Int> {
        val baseColor = primaryColor
        
        return listOf(
            baseColor.toArgb(),                                    // 原色
            adjustColor(baseColor, hueShift = 15f).toArgb(),      // 色相偏移+15°
            adjustColor(baseColor, hueShift = -15f).toArgb(),     // 色相偏移-15°
            adjustColor(baseColor, saturation = 0.8f).toArgb(),   // 降低饱和度
            adjustColor(baseColor, saturation = 1.2f).toArgb(),   // 提高饱和度
            adjustColor(baseColor, lightness = 0.85f).toArgb(),   // 变亮
            adjustColor(baseColor, lightness = 1.15f).toArgb(),   // 变暗
            adjustColor(baseColor, hueShift = 30f, saturation = 0.9f).toArgb(),
            adjustColor(baseColor, hueShift = -30f, saturation = 0.9f).toArgb(),
            adjustColor(baseColor, hueShift = 20f, lightness = 0.9f).toArgb(),
            adjustColor(baseColor, hueShift = -20f, lightness = 0.9f).toArgb(),
            adjustColor(baseColor, saturation = 0.85f, lightness = 1.1f).toArgb()
        )
    }
    
    /**
     * 调整颜色的HSL值
     */
    private fun adjustColor(
        color: Color,
        hueShift: Float = 0f,
        saturation: Float = 1f,
        lightness: Float = 1f
    ): Color {
        val hsl = FloatArray(3)
        rgbToHsl(color.red, color.green, color.blue, hsl)
        
        // 调整色相 (0-360度)
        hsl[0] = (hsl[0] + hueShift + 360f) % 360f
        
        // 调整饱和度 (0-1)
        hsl[1] = (hsl[1] * saturation).coerceIn(0f, 1f)
        
        // 调整亮度 (0-1)
        hsl[2] = (hsl[2] * lightness).coerceIn(0f, 1f)
        
        return hslToRgb(hsl[0], hsl[1], hsl[2])
    }
    
    /**
     * RGB转HSL
     */
    private fun rgbToHsl(r: Float, g: Float, b: Float, hsl: FloatArray) {
        val max = maxOf(r, g, b)
        val min = minOf(r, g, b)
        val delta = max - min
        
        // 亮度
        hsl[2] = (max + min) / 2f
        
        if (delta == 0f) {
            // 灰色
            hsl[0] = 0f
            hsl[1] = 0f
        } else {
            // 饱和度
            hsl[1] = if (hsl[2] < 0.5f) {
                delta / (max + min)
            } else {
                delta / (2f - max - min)
            }
            
            // 色相
            hsl[0] = when (max) {
                r -> ((g - b) / delta + (if (g < b) 6f else 0f)) * 60f
                g -> ((b - r) / delta + 2f) * 60f
                else -> ((r - g) / delta + 4f) * 60f
            }
        }
    }
    
    /**
     * HSL转RGB
     */
    private fun hslToRgb(h: Float, s: Float, l: Float): Color {
        val c = (1f - kotlin.math.abs(2f * l - 1f)) * s
        val x = c * (1f - kotlin.math.abs((h / 60f) % 2f - 1f))
        val m = l - c / 2f
        
        val (r, g, b) = when {
            h < 60f -> Triple(c, x, 0f)
            h < 120f -> Triple(x, c, 0f)
            h < 180f -> Triple(0f, c, x)
            h < 240f -> Triple(0f, x, c)
            h < 300f -> Triple(x, 0f, c)
            else -> Triple(c, 0f, x)
        }
        
        return Color(r + m, g + m, b + m)
    }
    
    /**
     * 为新课程分配颜色
     * @param existingCourses 已存在的课程列表
     * @param dayOfWeek 新课程的星期
     * @param startTime 新课程的开始时间
     * @param endTime 新课程的结束时间
     * @param primaryColor 用户选择的主题色
     * @return 分配的颜色值（编码了索引）
     */
    fun assignColor(
        existingCourses: List<Course>,
        dayOfWeek: DayOfWeek,
        startTime: LocalTime,
        endTime: LocalTime,
        primaryColor: Color
    ): Int {
        // 根据主题色生成调色板
        val colorPalette = generateColorPalette(primaryColor)
        
        // 选择颜色索引
        val colorIndex = selectColorIndex(existingCourses, dayOfWeek, startTime, endTime)
        
        // 获取对应的颜色值
        val selectedColor = colorPalette[colorIndex]
        
        // 编码索引和颜色值
        return CourseColorMapper.encodeColor(colorIndex, selectedColor)
    }
    
    /**
     * 选择颜色索引
     * 确保纵向和横向相邻的课程使用不同的颜色索引
     * 
     * @param existingCourses 已存在的课程列表
     * @param dayOfWeek 新课程的星期
     * @param startTime 新课程的开始时间
     * @param endTime 新课程的结束时间
     * @return 颜色索引 (0-11)
     */
    private fun selectColorIndex(
        existingCourses: List<Course>,
        dayOfWeek: DayOfWeek,
        startTime: LocalTime,
        endTime: LocalTime
    ): Int {
        if (existingCourses.isEmpty()) {
            return 0
        }
        
        // 找出纵向相邻的课程(同一天,时间相邻)
        val verticalAdjacentCourses = existingCourses.filter { course ->
            course.dayOfWeek == dayOfWeek && 
            isTimeAdjacent(course.startTime, course.endTime, startTime, endTime)
        }
        
        // 找出横向相邻的课程(不同天,但时间重叠)
        val horizontalAdjacentCourses = existingCourses.filter { course ->
            course.dayOfWeek != dayOfWeek && 
            isTimeOverlapping(course.startTime, course.endTime, startTime, endTime)
        }
        
        // 收集所有相邻课程使用的颜色索引
        val usedIndices = (verticalAdjacentCourses + horizontalAdjacentCourses)
            .map { CourseColorMapper.extractColorIndex(it.color) }
            .toSet()
        
        // 找出第一个未被使用的索引
        val availableIndex = (0..11).firstOrNull { it !in usedIndices }
        
        // 如果所有索引都被使用了,使用基于哈希的索引选择
        return availableIndex ?: ((dayOfWeek.value * 7 + startTime.hour) % 12)
    }
    
    /**
     * 判断两个时间段是否纵向相邻(有重叠或间隔很小)
     */
    private fun isTimeAdjacent(
        time1Start: LocalTime,
        time1End: LocalTime,
        time2Start: LocalTime,
        time2End: LocalTime
    ): Boolean {
        // 时间重叠
        if (time1Start < time2End && time2Start < time1End) {
            return true
        }
        
        // 间隔小于30分钟
        val gap1 = if (time1End <= time2Start) {
            java.time.Duration.between(time1End, time2Start).toMinutes()
        } else {
            Long.MAX_VALUE
        }
        
        val gap2 = if (time2End <= time1Start) {
            java.time.Duration.between(time2End, time1Start).toMinutes()
        } else {
            Long.MAX_VALUE
        }
        
        return gap1 < 30 || gap2 < 30
    }
    
    /**
     * 判断两个时间段是否横向重叠(时间有交集)
     */
    private fun isTimeOverlapping(
        time1Start: LocalTime,
        time1End: LocalTime,
        time2Start: LocalTime,
        time2End: LocalTime
    ): Boolean {
        return time1Start < time2End && time2Start < time1End
    }
    
}
