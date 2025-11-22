package takagi.ru.saison.domain.model.routine

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * 带统计信息的周期性任务
 */
data class RoutineTaskWithStats(
    val task: RoutineTask,
    val checkInCount: Int,              // 当前周期打卡次数
    val isInActiveCycle: Boolean,       // 是否在活跃周期内
    val currentCycleStart: LocalDate?,  // 当前周期开始日期
    val currentCycleEnd: LocalDate?,    // 当前周期结束日期
    val nextActiveDate: LocalDate?,     // 下次活跃日期（非活跃任务）
    val lastCheckInTime: LocalDateTime? // 最后打卡时间
)
