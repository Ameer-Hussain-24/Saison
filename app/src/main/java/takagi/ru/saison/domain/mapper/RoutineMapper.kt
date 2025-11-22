package takagi.ru.saison.domain.mapper

import takagi.ru.saison.data.local.database.entity.CheckInRecordEntity
import takagi.ru.saison.data.local.database.entity.RoutineTaskEntity
import takagi.ru.saison.domain.model.routine.CheckInRecord
import takagi.ru.saison.domain.model.routine.CycleConfigSerializer
import takagi.ru.saison.domain.model.routine.CycleType
import takagi.ru.saison.domain.model.routine.RoutineTask
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Routine 模型映射器
 */
object RoutineMapper {
    
    /**
     * Entity 转 Domain Model
     */
    fun toDomain(entity: RoutineTaskEntity): RoutineTask {
        return RoutineTask(
            id = entity.id,
            title = entity.title,
            description = entity.description,
            icon = entity.icon,
            cycleType = CycleType.valueOf(entity.cycleType),
            cycleConfig = CycleConfigSerializer.deserialize(entity.cycleConfig),
            durationMinutes = entity.durationMinutes,
            isActive = entity.isActive,
            createdAt = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(entity.createdAt),
                ZoneId.systemDefault()
            ),
            updatedAt = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(entity.updatedAt),
                ZoneId.systemDefault()
            )
        )
    }
    
    /**
     * Domain Model 转 Entity
     */
    fun toEntity(domain: RoutineTask): RoutineTaskEntity {
        return RoutineTaskEntity(
            id = domain.id,
            title = domain.title,
            description = domain.description,
            icon = domain.icon,
            cycleType = domain.cycleType.name,
            cycleConfig = CycleConfigSerializer.serialize(domain.cycleConfig),
            durationMinutes = domain.durationMinutes,
            isActive = domain.isActive,
            createdAt = domain.createdAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
            updatedAt = domain.updatedAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
    }
    
    /**
     * CheckInRecord Entity 转 Domain Model
     */
    fun toDomain(entity: CheckInRecordEntity): CheckInRecord {
        return CheckInRecord(
            id = entity.id,
            routineTaskId = entity.routineTaskId,
            checkInTime = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(entity.checkInTime),
                ZoneId.systemDefault()
            ),
            note = entity.note,
            cycleStartDate = LocalDate.ofEpochDay(entity.cycleStartDate),
            cycleEndDate = LocalDate.ofEpochDay(entity.cycleEndDate)
        )
    }
    
    /**
     * CheckInRecord Domain Model 转 Entity
     */
    fun toEntity(domain: CheckInRecord): CheckInRecordEntity {
        return CheckInRecordEntity(
            id = domain.id,
            routineTaskId = domain.routineTaskId,
            checkInTime = domain.checkInTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
            note = domain.note,
            cycleStartDate = domain.cycleStartDate.toEpochDay(),
            cycleEndDate = domain.cycleEndDate.toEpochDay()
        )
    }
}
