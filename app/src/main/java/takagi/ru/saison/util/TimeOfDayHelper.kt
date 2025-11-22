package takagi.ru.saison.util

import takagi.ru.saison.data.local.datastore.SeasonalTheme
import java.util.Calendar

/**
 * 时间段辅助工具
 * 根据当前时间自动判断时段并返回对应的主题和深色模式
 */
object TimeOfDayHelper {
    
    /**
     * 时间段枚举
     */
    enum class TimeOfDay {
        DAWN,      // 黎明 (5:00-7:00)
        MORNING,   // 早晨 (7:00-11:00)
        NOON,      // 正午 (11:00-14:00)
        AFTERNOON, // 下午 (14:00-17:00)
        DUSK,      // 黄昏 (17:00-19:00)
        EVENING,   // 傍晚 (19:00-22:00)
        NIGHT      // 夜晚 (22:00-5:00)
    }
    
    /**
     * 时段配置
     */
    data class TimeOfDayConfig(
        val theme: SeasonalTheme,
        val isDark: Boolean,
        val displayName: String
    )
    
    /**
     * 获取当前时段
     */
    fun getCurrentTimeOfDay(): TimeOfDay {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        
        return when (hour) {
            in 5..6 -> TimeOfDay.DAWN
            in 7..10 -> TimeOfDay.MORNING
            in 11..13 -> TimeOfDay.NOON
            in 14..16 -> TimeOfDay.AFTERNOON
            in 17..18 -> TimeOfDay.DUSK
            in 19..21 -> TimeOfDay.EVENING
            else -> TimeOfDay.NIGHT
        }
    }
    
    /**
     * 根据时段获取配置
     */
    fun getConfigForTimeOfDay(timeOfDay: TimeOfDay): TimeOfDayConfig {
        return when (timeOfDay) {
            TimeOfDay.DAWN -> TimeOfDayConfig(
                theme = SeasonalTheme.AURORA,  // 黎明 - 极光色
                isDark = false,
                displayName = "黎明"
            )
            TimeOfDay.MORNING -> TimeOfDayConfig(
                theme = SeasonalTheme.SAKURA,  // 早晨 - 樱花色
                isDark = false,
                displayName = "早晨"
            )
            TimeOfDay.NOON -> TimeOfDayConfig(
                theme = SeasonalTheme.AMBER,   // 正午 - 琥珀色
                isDark = false,
                displayName = "正午"
            )
            TimeOfDay.AFTERNOON -> TimeOfDayConfig(
                theme = SeasonalTheme.OCEAN,   // 下午 - 海洋色
                isDark = false,
                displayName = "下午"
            )
            TimeOfDay.DUSK -> TimeOfDayConfig(
                theme = SeasonalTheme.SUNSET,  // 黄昏 - 日落色
                isDark = false,
                displayName = "黄昏"
            )
            TimeOfDay.EVENING -> TimeOfDayConfig(
                theme = SeasonalTheme.LAVENDER, // 傍晚 - 薰衣草色
                isDark = true,
                displayName = "傍晚"
            )
            TimeOfDay.NIGHT -> TimeOfDayConfig(
                theme = SeasonalTheme.RAIN,    // 夜晚 - 雨夜色
                isDark = true,
                displayName = "夜晚"
            )
        }
    }
    
    /**
     * 获取当前时段的配置
     */
    fun getCurrentConfig(): TimeOfDayConfig {
        return getConfigForTimeOfDay(getCurrentTimeOfDay())
    }
}
