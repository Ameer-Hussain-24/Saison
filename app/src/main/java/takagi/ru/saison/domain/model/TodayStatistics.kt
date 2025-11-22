package takagi.ru.saison.domain.model

import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * 今日统计数据模型
 * 包含今日的任务、课程统计信息和计算属性
 */
data class TodayStatistics(
    val date: LocalDate,
    val totalTasks: Int = 0,
    val completedTasks: Int = 0,
    val pendingTasks: Int = 0,
    val totalCourses: Int = 0,
    val completedCourses: Int = 0,
    val upcomingCourse: Course? = null,
    val nextCourseStartTime: LocalDateTime? = null
) {
    /**
     * 任务完成率 (0.0 - 1.0)
     */
    val taskCompletionRate: Float
        get() = if (totalTasks > 0) completedTasks.toFloat() / totalTasks else 0f

    /**
     * 任务完成百分比 (0 - 100)
     */
    val taskCompletionPercentage: Int
        get() = (taskCompletionRate * 100).toInt()

    /**
     * 距离下一节课程的分钟数
     */
    val minutesUntilNextCourse: Long?
        get() = nextCourseStartTime?.let {
            Duration.between(LocalDateTime.now(), it).toMinutes()
        }

    /**
     * 距离下一节课程的小时数
     */
    val hoursUntilNextCourse: Long?
        get() = minutesUntilNextCourse?.let { it / 60 }

    /**
     * 是否有即将开始的课程（1小时内）
     */
    val hasUpcomingCourse: Boolean
        get() = minutesUntilNextCourse?.let { it in 0..60 } ?: false

    /**
     * 是否所有任务都已完成
     */
    val isAllTasksCompleted: Boolean
        get() = totalTasks > 0 && completedTasks == totalTasks

    /**
     * 是否有未完成的任务
     */
    val hasPendingTasks: Boolean
        get() = pendingTasks > 0

    /**
     * 是否有课程
     */
    val hasCourses: Boolean
        get() = totalCourses > 0

    /**
     * 课程完成率 (0.0 - 1.0)
     */
    val courseCompletionRate: Float
        get() = if (totalCourses > 0) completedCourses.toFloat() / totalCourses else 0f

    /**
     * 格式化的下一节课程倒计时文本
     * 例如: "1小时30分钟", "30分钟", "即将开始"
     */
    fun getFormattedCountdown(): String? {
        val minutes = minutesUntilNextCourse ?: return null
        
        return when {
            minutes < 0 -> null // 课程已开始
            minutes < 5 -> "即将开始"
            minutes < 60 -> "${minutes}分钟"
            else -> {
                val hours = minutes / 60
                val remainingMinutes = minutes % 60
                if (remainingMinutes == 0L) {
                    "${hours}小时"
                } else {
                    "${hours}小时${remainingMinutes}分钟"
                }
            }
        }
    }
}
