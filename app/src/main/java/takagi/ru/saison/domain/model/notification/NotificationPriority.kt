package takagi.ru.saison.domain.model.notification

import androidx.core.app.NotificationCompat

/**
 * 通知优先级枚举
 * 定义通知的重要性级别
 */
enum class NotificationPriority(val value: Int) {
    /** 低优先级 */
    LOW(NotificationCompat.PRIORITY_LOW),
    
    /** 默认优先级 */
    DEFAULT(NotificationCompat.PRIORITY_DEFAULT),
    
    /** 高优先级 */
    HIGH(NotificationCompat.PRIORITY_HIGH),
    
    /** 最高优先级 */
    MAX(NotificationCompat.PRIORITY_MAX);
    
    companion object {
        /**
         * 从整数值获取优先级
         */
        fun fromValue(value: Int): NotificationPriority {
            return entries.find { it.value == value } ?: DEFAULT
        }
    }
}
