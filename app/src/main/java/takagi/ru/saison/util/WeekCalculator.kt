package takagi.ru.saison.util

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 周数计算工具类
 * 用于计算当前周数、日期范围转周数等功能
 */
@Singleton
class WeekCalculator @Inject constructor() {
    
    /**
     * 计算当前是第几周
     * @param semesterStartDate 学期开始日期
     * @param currentDate 当前日期
     * @return 当前周数（1-based）
     */
    fun calculateCurrentWeek(semesterStartDate: LocalDate, currentDate: LocalDate): Int {
        val daysBetween = ChronoUnit.DAYS.between(semesterStartDate, currentDate)
        return (daysBetween / 7).toInt() + 1
    }
    
    /**
     * 根据日期范围计算周数列表
     * @param semesterStartDate 学期开始日期
     * @param rangeStartDate 范围开始日期
     * @param rangeEndDate 范围结束日期
     * @return 周数列表
     */
    fun calculateWeeksFromDateRange(
        semesterStartDate: LocalDate,
        rangeStartDate: LocalDate,
        rangeEndDate: LocalDate
    ): List<Int> {
        val startWeek = calculateCurrentWeek(semesterStartDate, rangeStartDate)
        val endWeek = calculateCurrentWeek(semesterStartDate, rangeEndDate)
        return (startWeek..endWeek).toList()
    }
    
    /**
     * 获取某周的日期范围
     * @param semesterStartDate 学期开始日期
     * @param weekNumber 周数（1-based）
     * @return 该周的开始日期和结束日期
     */
    fun getWeekDateRange(semesterStartDate: LocalDate, weekNumber: Int): Pair<LocalDate, LocalDate> {
        val weekStartDate = semesterStartDate.plusWeeks((weekNumber - 1).toLong())
        val weekEndDate = weekStartDate.plusDays(6)
        return weekStartDate to weekEndDate
    }
    
    /**
     * 判断某个日期是否在指定的周数列表中
     * @param date 要判断的日期
     * @param semesterStartDate 学期开始日期
     * @param weeks 周数列表
     * @return 是否在指定周数中
     */
    fun isDateInWeeks(date: LocalDate, semesterStartDate: LocalDate, weeks: List<Int>): Boolean {
        val currentWeek = calculateCurrentWeek(semesterStartDate, date)
        return weeks.contains(currentWeek)
    }
}
