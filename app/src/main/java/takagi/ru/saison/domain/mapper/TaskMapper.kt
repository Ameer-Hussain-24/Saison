package takagi.ru.saison.domain.mapper

import takagi.ru.saison.data.local.database.entities.TaskEntity
import takagi.ru.saison.domain.model.Priority
import takagi.ru.saison.domain.model.RecurrenceRule
import takagi.ru.saison.domain.model.Task
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

fun TaskEntity.toDomain(): Task {
    return Task(
        id = id,
        title = title,
        description = description,
        dueDate = dueDate?.let { LocalDateTime.ofInstant(Instant.ofEpochMilli(it), ZoneId.systemDefault()) },
        reminderTime = reminderTime?.let { LocalDateTime.ofInstant(Instant.ofEpochMilli(it), ZoneId.systemDefault()) },
        location = location,
        priority = Priority.fromValue(priority),
        isCompleted = isCompleted,
        completedAt = completedAt?.let { LocalDateTime.ofInstant(Instant.ofEpochMilli(it), ZoneId.systemDefault()) },
        category = null, // Will be populated by repository
        subtasks = emptyList(), // Will be populated by repository
        repeatRule = repeatRule?.let { RecurrenceRule.fromRRule(it) },
        pomodoroCount = pomodoroCount,
        estimatedPomodoros = estimatedPomodoros,
        metronomeBpm = metronomeBpm,
        attachments = emptyList(), // Will be populated by repository
        isFavorite = isFavorite,
        createdAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(createdAt), ZoneId.systemDefault()),
        updatedAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(updatedAt), ZoneId.systemDefault())
    )
}

fun Task.toEntity(categoryId: Long, parentTaskId: Long? = null): TaskEntity {
    return TaskEntity(
        id = id,
        title = title,
        description = description,
        dueDate = dueDate?.atZone(ZoneId.systemDefault())?.toInstant()?.toEpochMilli(),
        reminderTime = reminderTime?.atZone(ZoneId.systemDefault())?.toInstant()?.toEpochMilli(),
        location = location,
        priority = priority.value,
        isCompleted = isCompleted,
        completedAt = completedAt?.atZone(ZoneId.systemDefault())?.toInstant()?.toEpochMilli(),
        categoryId = categoryId,
        parentTaskId = parentTaskId,
        repeatRule = repeatRule?.toRRule(),
        repeatEndDate = null,
        pomodoroCount = pomodoroCount,
        estimatedPomodoros = estimatedPomodoros,
        metronomeBpm = metronomeBpm,
        isFavorite = isFavorite,
        isEncrypted = false,
        encryptedData = null,
        createdAt = createdAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
        updatedAt = updatedAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
        syncStatus = 0
    )
}
