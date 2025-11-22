package takagi.ru.saison.domain.mapper

import takagi.ru.saison.data.local.database.entities.EventEntity
import takagi.ru.saison.domain.model.Event
import takagi.ru.saison.domain.model.EventCategory
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

fun EventEntity.toDomain(): Event {
    return Event(
        id = id,
        title = title,
        description = description,
        eventDate = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(eventDate),
            ZoneId.systemDefault()
        ),
        category = EventCategory.fromValue(category),
        isCompleted = isCompleted,
        reminderEnabled = reminderEnabled,
        reminderTime = reminderTime?.let {
            LocalDateTime.ofInstant(
                Instant.ofEpochMilli(it),
                ZoneId.systemDefault()
            )
        },
        createdAt = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(createdAt),
            ZoneId.systemDefault()
        ),
        updatedAt = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(updatedAt),
            ZoneId.systemDefault()
        )
    )
}

fun Event.toEntity(): EventEntity {
    return EventEntity(
        id = id,
        title = title,
        description = description,
        eventDate = eventDate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
        category = category.value,
        isCompleted = isCompleted,
        reminderEnabled = reminderEnabled,
        reminderTime = reminderTime?.atZone(ZoneId.systemDefault())?.toInstant()?.toEpochMilli(),
        createdAt = createdAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
        updatedAt = updatedAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    )
}
