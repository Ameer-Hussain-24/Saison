package takagi.ru.saison.domain.model.notification

/**
 * 通知偏好设置数据类
 * 存储用户的通知相关设置
 * 
 * @property notificationsEnabled 是否启用通知
 * @property taskRemindersEnabled 是否启用任务提醒
 * @property courseRemindersEnabled 是否启用课程提醒
 * @property pomodoroRemindersEnabled 是否启用番茄钟提醒
 * @property taskReminderAdvanceMinutes 任务提醒提前时间（分钟）
 * @property courseReminderAdvanceMinutes 课程提醒提前时间（分钟）
 * @property enableSummaryNotifications 是否启用摘要通知
 */
data class NotificationPreferences(
    val notificationsEnabled: Boolean = true,
    val taskRemindersEnabled: Boolean = true,
    val courseRemindersEnabled: Boolean = true,
    val pomodoroRemindersEnabled: Boolean = true,
    val taskReminderAdvanceMinutes: Int = 60,
    val courseReminderAdvanceMinutes: Int = 10,
    val enableSummaryNotifications: Boolean = true
)
