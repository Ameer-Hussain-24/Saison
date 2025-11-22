package takagi.ru.saison.domain.model.routine

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * 打卡记录数据模型
 */
data class CheckInRecord(
    val id: Long = 0,
    val routineTaskId: Long,
    val checkInTime: LocalDateTime,
    val note: String? = null,
    val cycleStartDate: LocalDate,  // 所属周期的开始日期
    val cycleEndDate: LocalDate     // 所属周期的结束日期
)
