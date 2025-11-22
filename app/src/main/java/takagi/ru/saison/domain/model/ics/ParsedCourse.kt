package takagi.ru.saison.domain.model.ics

import java.time.LocalDateTime

/**
 * 从ICS文件解析出的课程数据
 */
data class ParsedCourse(
    val summary: String,
    val location: String?,
    val description: String?,
    val dtStart: LocalDateTime,
    val dtEnd: LocalDateTime,
    val rrule: RecurrenceInfo?,
    val alarmMinutes: Int?
)

/**
 * 重复规则信息
 */
data class RecurrenceInfo(
    val frequency: String,  // WEEKLY, DAILY, etc.
    val interval: Int,
    val until: LocalDateTime?,
    val byDay: List<String>?  // MO, TU, WE, etc.
)
