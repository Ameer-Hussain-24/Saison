package takagi.ru.saison.domain.model.notification

/**
 * 通知数据类
 * 包含创建和显示通知所需的所有信息
 * 
 * @property id 通知的唯一标识符
 * @property type 通知类型
 * @property title 通知标题
 * @property content 通知内容
 * @property triggerTime 通知触发时间（毫秒时间戳）
 * @property itemId 关联的项目ID（任务ID、课程ID等）
 * @property priority 通知优先级
 * @property actions 通知操作列表
 * @property extras 额外的键值对数据
 */
data class NotificationData(
    val id: String,
    val type: NotificationType,
    val title: String,
    val content: String,
    val triggerTime: Long,
    val itemId: Long,
    val priority: NotificationPriority = NotificationPriority.DEFAULT,
    val actions: List<NotificationAction> = emptyList(),
    val extras: Map<String, String> = emptyMap()
)
