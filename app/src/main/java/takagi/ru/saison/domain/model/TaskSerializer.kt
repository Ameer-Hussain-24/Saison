package takagi.ru.saison.domain.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Serializable
data class TaskDto(
    val id: Long = 0,
    val title: String,
    val description: String? = null,
    val dueDate: String? = null,
    val reminderTime: String? = null,
    val location: String? = null,
    val priority: Int = 1,
    val isCompleted: Boolean = false,
    val completedAt: String? = null,
    val categoryId: Long? = null,
    val repeatRule: String? = null,
    val pomodoroCount: Int = 0,
    val estimatedPomodoros: Int? = null,
    val metronomeBpm: Int? = null,
    val createdAt: String,
    val updatedAt: String
)

fun Task.toDto(): TaskDto {
    val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    return TaskDto(
        id = id,
        title = title,
        description = description,
        dueDate = dueDate?.format(formatter),
        reminderTime = reminderTime?.format(formatter),
        location = location,
        priority = priority.value,
        isCompleted = isCompleted,
        completedAt = completedAt?.format(formatter),
        categoryId = category?.id,
        repeatRule = repeatRule?.toRRule(),
        pomodoroCount = pomodoroCount,
        estimatedPomodoros = estimatedPomodoros,
        metronomeBpm = metronomeBpm,
        createdAt = createdAt.format(formatter),
        updatedAt = updatedAt.format(formatter)
    )
}

fun TaskDto.toDomain(): Task {
    val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    return Task(
        id = id,
        title = title,
        description = description,
        dueDate = dueDate?.let { LocalDateTime.parse(it, formatter) },
        reminderTime = reminderTime?.let { LocalDateTime.parse(it, formatter) },
        location = location,
        priority = Priority.fromValue(priority),
        isCompleted = isCompleted,
        completedAt = completedAt?.let { LocalDateTime.parse(it, formatter) },
        category = null, // Will be populated separately
        subtasks = emptyList(),
        repeatRule = repeatRule?.let { RecurrenceRule.fromRRule(it) },
        pomodoroCount = pomodoroCount,
        estimatedPomodoros = estimatedPomodoros,
        metronomeBpm = metronomeBpm,
        attachments = emptyList(),
        createdAt = LocalDateTime.parse(createdAt, formatter),
        updatedAt = LocalDateTime.parse(updatedAt, formatter)
    )
}
