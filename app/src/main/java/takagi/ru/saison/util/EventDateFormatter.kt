package takagi.ru.saison.util

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object EventDateFormatter {
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日", Locale.getDefault())
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm", Locale.getDefault())
    private val shortDateFormatter = DateTimeFormatter.ofPattern("MM月dd日", Locale.getDefault())
    private val monthDayFormatter = DateTimeFormatter.ofPattern("M月d日", Locale.getDefault())
    
    /**
     * 格式化日期为 "yyyy年MM月dd日" 格式
     */
    fun formatDate(date: LocalDate): String {
        return date.format(dateFormatter)
    }
    
    /**
     * 格式化时间为 "HH:mm" 格式
     */
    fun formatTime(time: LocalTime): String {
        return time.format(timeFormatter)
    }
    
    /**
     * 格式化日期时间为 "yyyy年MM月dd日 HH:mm" 格式
     */
    fun formatDateTime(dateTime: LocalDateTime): String {
        return dateTime.format(dateTimeFormatter)
    }
    
    /**
     * 格式化日期为短格式 "MM月dd日"
     */
    fun formatShortDate(date: LocalDate): String {
        return date.format(shortDateFormatter)
    }
    
    /**
     * 格式化日期为月日格式 "M月d日"
     */
    fun formatMonthDay(date: LocalDate): String {
        return date.format(monthDayFormatter)
    }
    
    /**
     * 格式化 LocalDateTime 为日期字符串
     */
    fun formatEventDate(dateTime: LocalDateTime): String {
        return formatDate(dateTime.toLocalDate())
    }
    
    /**
     * 格式化 LocalDateTime 为短日期字符串
     */
    fun formatEventShortDate(dateTime: LocalDateTime): String {
        return formatShortDate(dateTime.toLocalDate())
    }
}
