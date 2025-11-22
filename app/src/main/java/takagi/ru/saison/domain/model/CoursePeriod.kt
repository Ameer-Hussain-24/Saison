package takagi.ru.saison.domain.model

import java.time.LocalTime

/**
 * 时段枚举
 * 表示一天中的不同时段
 */
enum class TimeOfDay {
    MORNING,    // 上午
    AFTERNOON,  // 下午
    EVENING     // 晚上
}

/**
 * 课程节次数据模型
 * 表示一天中的某一节课的时间信息
 */
data class CoursePeriod(
    val periodNumber: Int,                   // 节次编号（1-based）
    val startTime: LocalTime,                // 开始时间
    val endTime: LocalTime,                  // 结束时间
    val timeOfDay: TimeOfDay,                // 所属时段
    val isAfterLunchBreak: Boolean = false   // 是否在午休后（保留用于向后兼容）
)
