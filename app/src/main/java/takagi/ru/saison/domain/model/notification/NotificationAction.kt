package takagi.ru.saison.domain.model.notification

import android.app.PendingIntent

/**
 * 通知操作数据类
 * 定义通知上的操作按钮
 * 
 * @property id 操作的唯一标识符
 * @property title 操作按钮显示的文本
 * @property icon 操作按钮的图标资源ID
 * @property intent 点击操作时触发的 PendingIntent
 */
data class NotificationAction(
    val id: String,
    val title: String,
    val icon: Int,
    val intent: PendingIntent
)
