package takagi.ru.saison.util

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

/**
 * 周次日期计算工具类
 * 提供课程表相关的日期计算功能
 */
object WeekDateCalculator {
    
    /**
     * 获取指定周和星期的日期
     *
     * @param semesterStartDate 学期第一周开始日期
     * @param week 周次（1-based）
     * @param day 星期几
     * @return 对应的日期
     */
    fun getDateForDayInWeek(
        semesterStartDate: LocalDate,
        week: Int,
        day: DayOfWeek
    ): LocalDate {
        // 计算指定周的开始日期
        val weekStartDate = semesterStartDate.plusWeeks((week - 1).toLong())
        
        // 获取该周的周一
        val mondayOfWeek = weekStartDate.with(
            TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)
        )
        
        // 计算指定星期几的日期
        return mondayOfWeek.plusDays((day.value - 1).toLong())
    }
    
    /**
     * 获取星期几的简称（中文）
     *
     * @param day 星期几
     * @return 星期简称（一、二、三...）
     */
    fun getDayShortName(day: DayOfWeek): String {
        return when (day) {
            DayOfWeek.MONDAY -> "一"
            DayOfWeek.TUESDAY -> "二"
            DayOfWeek.WEDNESDAY -> "三"
            DayOfWeek.THURSDAY -> "四"
            DayOfWeek.FRIDAY -> "五"
            DayOfWeek.SATURDAY -> "六"
            DayOfWeek.SUNDAY -> "日"
        }
    }
}
