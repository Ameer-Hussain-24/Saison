package takagi.ru.saison.domain.mapper

import takagi.ru.saison.data.local.database.entities.CourseEntity
import takagi.ru.saison.domain.model.Course
import takagi.ru.saison.domain.model.WeekPattern
import takagi.ru.saison.util.WeekListConverter
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

fun CourseEntity.toDomain(): Course {
    return Course(
        id = id,
        name = name,
        instructor = instructor,
        location = location,
        color = color,
        semesterId = semesterId,
        dayOfWeek = DayOfWeek.of(dayOfWeek),
        startTime = LocalTime.parse(startTime),
        endTime = LocalTime.parse(endTime),
        weekPattern = WeekPattern.fromString(weekPattern),
        customWeeks = WeekListConverter.fromJson(customWeeks),
        startDate = LocalDate.ofInstant(Instant.ofEpochMilli(startDate), ZoneId.systemDefault()),
        endDate = LocalDate.ofInstant(Instant.ofEpochMilli(endDate), ZoneId.systemDefault()),
        notificationMinutes = notificationMinutes,
        autoSilent = autoSilent,
        periodStart = periodStart,
        periodEnd = periodEnd,
        isCustomTime = isCustomTime,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun Course.toEntity(): CourseEntity {
    return CourseEntity(
        id = id,
        name = name,
        instructor = instructor,
        location = location,
        color = color,
        semesterId = semesterId,
        dayOfWeek = dayOfWeek.value,
        startTime = startTime.toString(),
        endTime = endTime.toString(),
        weekPattern = weekPattern.name,
        customWeeks = WeekListConverter.toJson(customWeeks),
        startDate = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
        endDate = endDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
        notificationMinutes = notificationMinutes,
        autoSilent = autoSilent,
        periodStart = periodStart,
        periodEnd = periodEnd,
        isCustomTime = isCustomTime,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
