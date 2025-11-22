package takagi.ru.saison.domain.model.routine

import java.time.DayOfWeek
import java.time.LocalTime

/**
 * 周期配置密封类
 */
sealed class CycleConfig {
    /**
     * 每日周期配置
     * @param time 可选的提醒时间
     */
    data class Daily(val time: LocalTime? = null) : CycleConfig()
    
    /**
     * 每周周期配置
     * @param daysOfWeek 一周中的哪几天（如 [MONDAY, WEDNESDAY, FRIDAY]）
     */
    data class Weekly(val daysOfWeek: List<DayOfWeek>) : CycleConfig()
    
    /**
     * 每月周期配置
     * @param daysOfMonth 每月的哪几天（如 [1, 15] 表示每月1号和15号）
     */
    data class Monthly(val daysOfMonth: List<Int>) : CycleConfig()
    
    /**
     * 自定义周期配置
     * @param rrule RFC 5545 RRULE 字符串
     */
    data class Custom(val rrule: String) : CycleConfig()
}
