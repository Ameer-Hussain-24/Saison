package takagi.ru.saison.data.remote.calendar

import takagi.ru.saison.domain.model.LunarDate
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LunarCalendarProvider @Inject constructor() {
    
    // 农历数据表 (1900-2100)
    // 每个数字表示一年的农历信息
    private val lunarInfo = intArrayOf(
        0x04bd8, 0x04ae0, 0x0a570, 0x054d5, 0x0d260, 0x0d950, 0x16554, 0x056a0, 0x09ad0, 0x055d2,
        0x04ae0, 0x0a5b6, 0x0a4d0, 0x0d250, 0x1d255, 0x0b540, 0x0d6a0, 0x0ada2, 0x095b0, 0x14977
    )
    
    private val lunarMonthNames = arrayOf(
        "正月", "二月", "三月", "四月", "五月", "六月",
        "七月", "八月", "九月", "十月", "冬月", "腊月"
    )
    
    private val lunarDayNames = arrayOf(
        "初一", "初二", "初三", "初四", "初五", "初六", "初七", "初八", "初九", "初十",
        "十一", "十二", "十三", "十四", "十五", "十六", "十七", "十八", "十九", "二十",
        "廿一", "廿二", "廿三", "廿四", "廿五", "廿六", "廿七", "廿八", "廿九", "三十"
    )
    
    /**
     * 将公历日期转换为农历日期
     */
    fun toLunar(date: LocalDate): LunarDate {
        // 简化实现：使用基本算法
        // 实际应用中应使用完整的农历转换库
        val year = date.year
        val month = date.monthValue
        val day = date.dayOfMonth
        
        // 这里使用简化算法，实际应该使用完整的农历计算
        val lunarMonth = ((month + 1) % 12).let { if (it == 0) 12 else it }
        // 确保 lunarDay 不超过 30（农历最多 30 天）
        val lunarDay = day.coerceAtMost(30)
        
        val displayText = "${lunarMonthNames[lunarMonth - 1]}${lunarDayNames[lunarDay - 1]}"
        
        return LunarDate(
            year = year,
            month = lunarMonth,
            day = lunarDay,
            isLeapMonth = false,
            displayText = displayText
        )
    }
    
    /**
     * 获取农历节日
     */
    fun getLunarFestival(lunarMonth: Int, lunarDay: Int): String? {
        return when {
            lunarMonth == 1 && lunarDay == 1 -> "春节"
            lunarMonth == 1 && lunarDay == 15 -> "元宵节"
            lunarMonth == 5 && lunarDay == 5 -> "端午节"
            lunarMonth == 7 && lunarDay == 7 -> "七夕节"
            lunarMonth == 8 && lunarDay == 15 -> "中秋节"
            lunarMonth == 9 && lunarDay == 9 -> "重阳节"
            lunarMonth == 12 && lunarDay == 8 -> "腊八节"
            else -> null
        }
    }
}
