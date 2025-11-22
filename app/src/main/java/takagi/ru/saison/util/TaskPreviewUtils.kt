package takagi.ru.saison.util

import takagi.ru.saison.domain.model.Frequency
import takagi.ru.saison.domain.model.RecurrenceRule
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * 获取相对时间字符串
 * 例如："还有3天"、"已逾期2天"、"今天"
 */
fun getRelativeTimeString(dateTime: LocalDateTime): String {
    val now = LocalDateTime.now()
    val duration = Duration.between(now, dateTime)
    
    return when {
        duration.isNegative -> {
            val absDuration = duration.abs()
            when {
                absDuration.toDays() > 0 -> "已逾期${absDuration.toDays()}天"
                absDuration.toHours() > 0 -> "已逾期${absDuration.toHours()}小时"
                else -> "已逾期"
            }
        }
        duration.toDays() == 0L -> "今天"
        duration.toDays() == 1L -> "明天"
        duration.toDays() < 7 -> "还有${duration.toDays()}天"
        duration.toDays() < 30 -> "还有${duration.toDays() / 7}周"
        else -> "还有${duration.toDays() / 30}个月"
    }
}

/**
 * 格式化重复规则为可读文本
 * 例如："每天"、"每2周"
 */
fun formatRecurrenceRule(rule: RecurrenceRule): String {
    return when (rule.frequency) {
        Frequency.DAILY -> if (rule.interval == 1) "每天" else "每${rule.interval}天"
        Frequency.WEEKLY -> if (rule.interval == 1) "每周" else "每${rule.interval}周"
        Frequency.MONTHLY -> if (rule.interval == 1) "每月" else "每${rule.interval}月"
        Frequency.YEARLY -> if (rule.interval == 1) "每年" else "每${rule.interval}年"
    }
}

/**
 * 格式化日期为"MM月dd日"
 */
fun formatDate(date: LocalDateTime): String {
    return date.format(DateTimeFormatter.ofPattern("MM月dd日"))
}

/**
 * 格式化日期时间为完整格式
 */
fun formatDateTime(dateTime: LocalDateTime): String {
    return dateTime.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm"))
}
