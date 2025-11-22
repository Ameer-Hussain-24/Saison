package takagi.ru.saison.domain.model

import java.time.LocalDateTime

data class CalendarEvent(
    val id: Long,
    val title: String,
    val description: String? = null,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val type: EventType,
    val color: Int,
    val isAllDay: Boolean = false,
    val location: String? = null,
    val relatedTaskId: Long? = null,
    val relatedCourseId: Long? = null,
    val isCompleted: Boolean = false
)

enum class EventType {
    TASK,
    COURSE,
    HOLIDAY
}

data class LunarDate(
    val year: Int,
    val month: Int,
    val day: Int,
    val isLeapMonth: Boolean = false,
    val displayText: String
)
