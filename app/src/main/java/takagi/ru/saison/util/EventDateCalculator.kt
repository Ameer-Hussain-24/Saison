package takagi.ru.saison.util

import android.content.Context
import takagi.ru.saison.R
import takagi.ru.saison.domain.model.EventCategory
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

object EventDateCalculator {
    /**
     * 计算事件距离今天的天数
     * 返回正数表示未来，负数表示过去，0 表示今天
     */
    fun calculateDaysUntil(eventDate: LocalDateTime): Int {
        val today = LocalDate.now()
        val eventDay = eventDate.toLocalDate()
        return ChronoUnit.DAYS.between(today, eventDay).toInt()
    }
    
    /**
     * 计算生日距离今天的天数（总是计算到下一个生日）
     * 生日是每年重复的，所以总是返回到下一个生日的天数
     */
    fun calculateDaysUntilBirthday(eventDate: LocalDateTime): Int {
        val today = LocalDate.now()
        val birthDate = eventDate.toLocalDate()
        
        // 获取今年的生日日期
        var nextBirthday = LocalDate.of(today.year, birthDate.month, birthDate.dayOfMonth)
        
        // 如果今年的生日已经过了，计算明年的生日
        if (nextBirthday.isBefore(today)) {
            nextBirthday = nextBirthday.plusYears(1)
        }
        
        return ChronoUnit.DAYS.between(today, nextBirthday).toInt()
    }
    
    /**
     * 根据事件类别计算天数
     */
    fun calculateDaysUntilForCategory(eventDate: LocalDateTime, category: EventCategory): Int {
        return when (category) {
            EventCategory.BIRTHDAY -> calculateDaysUntilBirthday(eventDate)
            else -> calculateDaysUntil(eventDate)
        }
    }
    
    /**
     * 格式化天数显示文本
     */
    fun formatDaysText(
        daysUntil: Int,
        context: Context
    ): String {
        return when {
            daysUntil == 0 -> context.getString(R.string.event_today)
            daysUntil > 0 -> context.getString(
                R.string.event_days_remaining,
                daysUntil
            )
            else -> context.getString(
                R.string.event_days_passed,
                Math.abs(daysUntil)
            )
        }
    }
    
    /**
     * 根据事件类别格式化天数显示文本
     */
    fun formatDaysTextForCategory(
        eventDate: LocalDateTime,
        category: EventCategory,
        context: Context
    ): String {
        val daysUntil = calculateDaysUntilForCategory(eventDate, category)
        return formatDaysText(daysUntil, context)
    }
    
    /**
     * 判断事件是否在过去（生日除外，生日永远不在过去）
     */
    fun isPastEvent(eventDate: LocalDateTime, category: EventCategory): Boolean {
        return when (category) {
            EventCategory.BIRTHDAY -> false // 生日永远不在过去
            else -> calculateDaysUntil(eventDate) < 0
        }
    }
    
    /**
     * 判断事件是否是今天
     */
    fun isToday(eventDate: LocalDateTime, category: EventCategory): Boolean {
        val daysUntil = calculateDaysUntilForCategory(eventDate, category)
        return daysUntil == 0
    }
    
    /**
     * 判断事件是否在未来（生日永远在未来）
     */
    fun isFutureEvent(eventDate: LocalDateTime, category: EventCategory): Boolean {
        return when (category) {
            EventCategory.BIRTHDAY -> !isToday(eventDate, category) // 生日如果不是今天就在未来
            else -> calculateDaysUntil(eventDate) > 0
        }
    }
}
