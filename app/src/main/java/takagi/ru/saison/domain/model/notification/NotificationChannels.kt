package takagi.ru.saison.domain.model.notification

import android.app.NotificationManager

/**
 * 通知渠道配置对象
 * 定义所有通知渠道的ID和配置
 */
object NotificationChannels {
    /** 任务提醒渠道ID */
    const val TASK_REMINDERS = "task_reminders"
    
    /** 课程提醒渠道ID */
    const val COURSE_REMINDERS = "course_reminders"
    
    /** 番茄钟提醒渠道ID */
    const val POMODORO_REMINDERS = "pomodoro_reminders"
    
    /**
     * 通知渠道配置数据类
     * 
     * @property id 渠道ID
     * @property name 渠道名称
     * @property description 渠道描述
     * @property importance 重要性级别
     * @property enableVibration 是否启用振动
     * @property enableSound 是否启用声音
     */
    data class ChannelConfig(
        val id: String,
        val name: String,
        val description: String,
        val importance: Int = NotificationManager.IMPORTANCE_DEFAULT,
        val enableVibration: Boolean = true,
        val enableSound: Boolean = true
    )
    
    /**
     * 获取所有渠道配置
     */
    fun getAllChannels(): List<ChannelConfig> = listOf(
        ChannelConfig(
            id = TASK_REMINDERS,
            name = "任务提醒",
            description = "任务截止时间提醒通知",
            importance = NotificationManager.IMPORTANCE_HIGH,
            enableVibration = true,
            enableSound = true
        ),
        ChannelConfig(
            id = COURSE_REMINDERS,
            name = "课程提醒",
            description = "课程开始时间提醒通知",
            importance = NotificationManager.IMPORTANCE_HIGH,
            enableVibration = true,
            enableSound = true
        ),
        ChannelConfig(
            id = POMODORO_REMINDERS,
            name = "番茄钟提醒",
            description = "番茄钟工作和休息时段提醒",
            importance = NotificationManager.IMPORTANCE_MAX,
            enableVibration = true,
            enableSound = true
        )
    )
}
