package takagi.ru.saison.domain.model

data class PomodoroSession(
    val id: Long = 0,
    val taskId: Long? = null,
    val routineTaskId: Long? = null,  // 关联的日程任务ID
    val startTime: Long,
    val endTime: Long? = null,
    val duration: Int, // 计划时长（分钟）
    val actualDuration: Int? = null,  // 实际时长（分钟）
    val isCompleted: Boolean = false,
    val isBreak: Boolean = false,
    val isLongBreak: Boolean = false,
    val isEarlyFinish: Boolean = false,  // 是否提前结束
    val interruptions: Int = 0,
    val notes: String? = null
)
