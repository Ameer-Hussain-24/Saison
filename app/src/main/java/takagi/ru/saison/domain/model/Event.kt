package takagi.ru.saison.domain.model

import java.time.LocalDateTime

data class Event(
    val id: Long = 0,
    val title: String,
    val description: String? = null,
    val eventDate: LocalDateTime,
    val category: EventCategory,
    val isCompleted: Boolean = false,
    val reminderEnabled: Boolean = false,
    val reminderTime: LocalDateTime? = null,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)
