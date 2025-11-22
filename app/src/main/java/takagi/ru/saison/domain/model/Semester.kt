package takagi.ru.saison.domain.model

import java.time.LocalDate

data class Semester(
    val id: Long = 0,
    val name: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val totalWeeks: Int = 18,
    val isArchived: Boolean = false,
    val isDefault: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
