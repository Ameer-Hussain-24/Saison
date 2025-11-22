package takagi.ru.saison.domain.usecase

import takagi.ru.saison.domain.model.Task
import takagi.ru.saison.util.NaturalLanguageParser
import java.time.LocalDateTime
import javax.inject.Inject

class ParseNaturalLanguageUseCase @Inject constructor(
    private val parser: NaturalLanguageParser
) {
    operator fun invoke(input: String): Task {
        val parsed = parser.parse(input)
        
        val dueDateTime = if (parsed.dueDate != null && parsed.time != null) {
            LocalDateTime.of(parsed.dueDate, parsed.time)
        } else if (parsed.dueDate != null) {
            LocalDateTime.of(parsed.dueDate, java.time.LocalTime.of(23, 59))
        } else {
            null
        }
        
        val reminderTime = if (dueDateTime != null) {
            dueDateTime.minusHours(1) // Default: 1 hour before due
        } else {
            null
        }
        
        return Task(
            title = parsed.title,
            description = null,
            dueDate = dueDateTime,
            reminderTime = reminderTime,
            location = null,
            priority = parsed.priority,
            isCompleted = false,
            completedAt = null,
            category = null, // Will be set by repository
            subtasks = emptyList(),
            repeatRule = null,
            pomodoroCount = 0,
            estimatedPomodoros = null,
            metronomeBpm = null,
            attachments = emptyList(),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
    }
}
