package takagi.ru.saison.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import takagi.ru.saison.data.local.datastore.PreferencesManager
import takagi.ru.saison.data.local.datastore.SeasonalTheme
import takagi.ru.saison.data.local.datastore.ThemeMode
import takagi.ru.saison.data.local.datastore.ThemePreferences
import takagi.ru.saison.util.CourseColorMapper

/**
 * 记住主题感知的课程颜色
 * 
 * 这个 Composable 函数会自动响应主题变化，将存储的颜色整数值
 * 映射到当前主题的对应颜色。
 * 
 * @param colorInt 存储的颜色整数值
 * @return 主题感知的 Color 对象
 * 
 * 示例：
 * ```kotlin
 * @Composable
 * fun CourseCard(course: Course) {
 *     val courseColor = rememberThemeAwareCourseColor(course.color)
 *     Card(colors = CardDefaults.cardColors(
 *         containerColor = courseColor.copy(alpha = 0.15f)
 *     )) {
 *         // ...
 *     }
 * }
 * ```
 */
@Composable
fun rememberThemeAwareCourseColor(colorInt: Int): Color {
    val context = LocalContext.current
    val systemInDarkTheme = isSystemInDarkTheme()
    
    // 获取当前主题设置
    val themePreferences by remember(context) {
        val prefsManager = PreferencesManager(context)
        prefsManager.themePreferences
    }.collectAsState(initial = ThemePreferences())
    
    // 确定是否使用深色模式
    val isDark = when (themePreferences.themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.FOLLOW_SYSTEM -> systemInDarkTheme
        ThemeMode.AUTO_TIME -> {
            // AUTO_TIME 模式根据时间段决定
            val currentHour = java.time.LocalTime.now().hour
            currentHour < 6 || currentHour >= 18  // 6:00-18:00 为浅色，其他时间为深色
        }
    }
    
    // 解析实际主题（处理 AUTO_SEASONAL）
    val actualTheme = if (themePreferences.theme == SeasonalTheme.AUTO_SEASONAL) {
        takagi.ru.saison.util.SeasonHelper.getCurrentSeasonTheme()
    } else {
        themePreferences.theme
    }
    
    // 使用 remember 缓存颜色计算结果
    // 依赖于 colorInt、actualTheme 和 isDark
    return remember(colorInt, actualTheme, isDark) {
        CourseColorMapper.mapToThemeColor(colorInt, actualTheme, isDark)
    }
}

/**
 * 获取当前主题的课程调色板
 * 
 * 这个 Composable 函数返回当前主题下的12种课程颜色列表，
 * 可用于颜色选择器等场景。
 * 
 * @return 当前主题的课程颜色列表
 */
@Composable
fun rememberThemeCoursePalette(): List<Color> {
    val context = LocalContext.current
    val systemInDarkTheme = isSystemInDarkTheme()
    
    // 获取当前主题设置
    val themePreferences by remember(context) {
        val prefsManager = PreferencesManager(context)
        prefsManager.themePreferences
    }.collectAsState(initial = ThemePreferences())
    
    // 确定是否使用深色模式
    val isDark = when (themePreferences.themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.FOLLOW_SYSTEM -> systemInDarkTheme
        ThemeMode.AUTO_TIME -> {
            // AUTO_TIME 模式根据时间段决定
            val currentHour = java.time.LocalTime.now().hour
            currentHour < 6 || currentHour >= 18  // 6:00-18:00 为浅色，其他时间为深色
        }
    }
    
    // 解析实际主题
    val actualTheme = if (themePreferences.theme == SeasonalTheme.AUTO_SEASONAL) {
        takagi.ru.saison.util.SeasonHelper.getCurrentSeasonTheme()
    } else {
        themePreferences.theme
    }
    
    // 使用 remember 缓存调色板
    return remember(actualTheme, isDark) {
        CourseColorMapper.getThemeCoursePalette(actualTheme, isDark)
    }
}

/**
 * 从颜色整数值提取颜色索引
 * 
 * 这是一个便捷函数，用于在 Composable 中提取颜色索引。
 * 
 * @param colorInt 存储的颜色整数值
 * @return 颜色索引 (0-11)
 */
fun extractCourseColorIndex(colorInt: Int): Int {
    return CourseColorMapper.extractColorIndex(colorInt)
}

/**
 * 编码颜色索引和颜色值
 * 
 * 这是一个便捷函数，用于在创建新课程时编码颜色。
 * 
 * @param colorIndex 颜色索引 (0-11)
 * @param colorValue 原始颜色值
 * @return 编码后的颜色整数值
 */
fun encodeCourseColor(colorIndex: Int, colorValue: Int): Int {
    return CourseColorMapper.encodeColor(colorIndex, colorValue)
}
