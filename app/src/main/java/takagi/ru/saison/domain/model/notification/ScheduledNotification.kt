package takagi.ru.saison.domain.model.notification

/**
 * 已调度通知数据类
 * 用于持久化已调度的通知信息
 * 
 * @property id 通知的唯一标识符
 * @property type 通知类型
 * @property itemId 关联的项目ID
 * @property triggerTime 触发时间（毫秒时间戳）
 * @property workRequestId WorkManager 工作请求ID（如果使用 WorkManager）
 * @property alarmRequestCode AlarmManager 请求码（如果使用 AlarmManager）
 */
data class ScheduledNotification(
    val id: String,
    val type: NotificationType,
    val itemId: Long,
    val triggerTime: Long,
    val workRequestId: String? = null,
    val alarmRequestCode: Int? = null
)
