package takagi.ru.saison.domain.model.notification

/**
 * 通知类型枚举
 * 定义系统支持的所有通知类型
 */
enum class NotificationType {
    /** 任务提醒通知 */
    TASK_REMINDER,
    
    /** 课程提醒通知 */
    COURSE_REMINDER,
    
    /** 番茄钟工作时段结束通知 */
    POMODORO_WORK_END,
    
    /** 番茄钟休息时段结束通知 */
    POMODORO_BREAK_END,
    
    /** 任务摘要通知（多个任务到期） */
    TASK_SUMMARY
}
