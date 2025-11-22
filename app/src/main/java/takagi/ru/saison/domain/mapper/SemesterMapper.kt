package takagi.ru.saison.domain.mapper

import takagi.ru.saison.data.local.database.entities.SemesterEntity
import takagi.ru.saison.domain.model.Semester
import java.time.LocalDate

fun SemesterEntity.toDomain(): Semester {
    return Semester(
        id = id,
        name = name,
        startDate = LocalDate.ofEpochDay(startDate / 86400000L),
        endDate = LocalDate.ofEpochDay(endDate / 86400000L),
        totalWeeks = totalWeeks,
        isArchived = isArchived,
        isDefault = isDefault,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun Semester.toEntity(): SemesterEntity {
    return SemesterEntity(
        id = id,
        name = name,
        startDate = startDate.toEpochDay() * 86400000L,
        endDate = endDate.toEpochDay() * 86400000L,
        totalWeeks = totalWeeks,
        isArchived = isArchived,
        isDefault = isDefault,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
