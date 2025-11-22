package takagi.ru.saison.domain.mapper

import takagi.ru.saison.data.local.database.entities.PomodoroSessionEntity
import takagi.ru.saison.domain.model.PomodoroSession

fun PomodoroSessionEntity.toDomain(): PomodoroSession {
    return PomodoroSession(
        id = id,
        taskId = taskId,
        routineTaskId = routineTaskId,
        startTime = startTime,
        endTime = endTime,
        duration = duration,
        actualDuration = actualDuration,
        isCompleted = isCompleted,
        isBreak = isBreak,
        isLongBreak = isLongBreak,
        isEarlyFinish = isEarlyFinish,
        interruptions = interruptions,
        notes = notes
    )
}

fun PomodoroSession.toEntity(): PomodoroSessionEntity {
    return PomodoroSessionEntity(
        id = id,
        taskId = taskId,
        routineTaskId = routineTaskId,
        startTime = startTime,
        endTime = endTime,
        duration = duration,
        actualDuration = actualDuration,
        isCompleted = isCompleted,
        isBreak = isBreak,
        isLongBreak = isLongBreak,
        isEarlyFinish = isEarlyFinish,
        interruptions = interruptions,
        notes = notes
    )
}
