package takagi.ru.saison.domain.model.routine

import java.time.LocalDateTime

/**
 * 周期性任务数据模型
 */
data class RoutineTask(
    val id: Long = 0,
    val title: String,
    val description: String? = null,
    val icon: String? = null,  // Material Icon name
    val cycleType: CycleType,
    val cycleConfig: CycleConfig,
    val durationMinutes: Int? = null,  // 活动时长（分钟）
    val isActive: Boolean = true,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)
