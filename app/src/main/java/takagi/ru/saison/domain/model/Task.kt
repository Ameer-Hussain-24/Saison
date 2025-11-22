package takagi.ru.saison.domain.model

import java.time.LocalDateTime

data class Task(
    val id: Long = 0,
    val title: String,
    val description: String? = null,
    val dueDate: LocalDateTime? = null,
    val reminderTime: LocalDateTime? = null,
    val location: String? = null,
    val priority: Priority = Priority.MEDIUM,
    val isCompleted: Boolean = false,
    val completedAt: LocalDateTime? = null,
    val category: Tag? = null,
    val subtasks: List<Task> = emptyList(),
    val repeatRule: RecurrenceRule? = null,
    val pomodoroCount: Int = 0,
    val estimatedPomodoros: Int? = null,
    val metronomeBpm: Int? = null,
    val attachments: List<Attachment> = emptyList(),
    val isFavorite: Boolean = false,
    val sortOrder: Int = 0,
    val tags: List<Tag> = emptyList(),
    val itemType: ItemType = ItemType.TASK,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)
