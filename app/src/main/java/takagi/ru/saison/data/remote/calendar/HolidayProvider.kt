package takagi.ru.saison.data.remote.calendar

import java.time.LocalDate
import java.time.Month
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HolidayProvider @Inject constructor() {
    
    private val holidays = mapOf(
        // 中国节假日
        "zh" to mapOf(
            LocalDate.of(2024, 1, 1) to "元旦",
            LocalDate.of(2024, 5, 1) to "劳动节",
            LocalDate.of(2024, 10, 1) to "国庆节",
            LocalDate.of(2025, 1, 1) to "元旦",
            LocalDate.of(2025, 5, 1) to "劳动节",
            LocalDate.of(2025, 10, 1) to "国庆节"
        ),
        // 日本节假日
        "ja" to mapOf(
            LocalDate.of(2024, 1, 1) to "元日",
            LocalDate.of(2024, 5, 3) to "憲法記念日",
            LocalDate.of(2024, 11, 3) to "文化の日",
            LocalDate.of(2025, 1, 1) to "元日",
            LocalDate.of(2025, 5, 3) to "憲法記念日",
            LocalDate.of(2025, 11, 3) to "文化の日"
        ),
        // 越南节假日
        "vi" to mapOf(
            LocalDate.of(2024, 1, 1) to "Tết Dương lịch",
            LocalDate.of(2024, 4, 30) to "Ngày Giải phóng miền Nam",
            LocalDate.of(2024, 9, 2) to "Quốc khánh",
            LocalDate.of(2025, 1, 1) to "Tết Dương lịch",
            LocalDate.of(2025, 4, 30) to "Ngày Giải phóng miền Nam",
            LocalDate.of(2025, 9, 2) to "Quốc khánh"
        ),
        // 国际节假日
        "en" to mapOf(
            LocalDate.of(2024, 1, 1) to "New Year's Day",
            LocalDate.of(2024, 12, 25) to "Christmas",
            LocalDate.of(2025, 1, 1) to "New Year's Day",
            LocalDate.of(2025, 12, 25) to "Christmas"
        )
    )
    
    /**
     * 获取指定日期的节假日名称
     */
    fun getHoliday(date: LocalDate, locale: String = "zh"): String? {
        val localeHolidays = holidays[locale] ?: holidays["en"]
        return localeHolidays?.get(date)
    }
    
    /**
     * 检查是否为节假日
     */
    fun isHoliday(date: LocalDate, locale: String = "zh"): Boolean {
        return getHoliday(date, locale) != null
    }
    
    /**
     * 获取月份内的所有节假日
     */
    fun getHolidaysInMonth(year: Int, month: Month, locale: String = "zh"): Map<LocalDate, String> {
        val localeHolidays = holidays[locale] ?: holidays["en"] ?: emptyMap()
        return localeHolidays.filter { (date, _) ->
            date.year == year && date.month == month
        }
    }
}
