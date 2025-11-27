package takagi.ru.saison.notification

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput
import dagger.hilt.android.qualifiers.ApplicationContext
import takagi.ru.saison.R
import takagi.ru.saison.domain.model.notification.NotificationChannels
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 快捷输入通知管理器
 * 负责管理通知栏快捷输入通知的生命周期
 */
@Singleton
class QuickInputNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val notificationManager: NotificationManagerCompat,
    private val channelManager: NotificationChannelManager,
    private val permissionManager: NotificationPermissionManager
) {
    companion object {
        const val NOTIFICATION_ID = 1001
        const val ACTION_QUICK_INPUT = "takagi.ru.saison.ACTION_QUICK_INPUT"
        const val KEY_TEXT_REPLY = "key_text_reply"
    }
    
    /**
     * 显示快捷输入通知
     */
    fun showQuickInputNotification() {
        // 检查通知权限
        if (!permissionManager.checkNotificationPermission()) {
            return
        }
        
        // 确保通知渠道已创建
        if (!channelManager.isChannelCreated(NotificationChannels.QUICK_INPUT)) {
            channelManager.initializeChannels()
        }
        
        val notification = buildNotification()
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
    
    /**
     * 关闭快捷输入通知
     */
    fun dismissQuickInputNotification() {
        notificationManager.cancel(NOTIFICATION_ID)
    }
    
    /**
     * 更新快捷输入通知（清空输入框）
     */
    fun updateQuickInputNotification() {
        showQuickInputNotification()
    }
    
    /**
     * 构建通知对象
     */
    private fun buildNotification(): Notification {
        val remoteInput = createRemoteInput()
        val pendingIntent = createPendingIntent()
        
        val action = NotificationCompat.Action.Builder(
            R.drawable.ic_launcher_foreground,
            context.getString(R.string.quick_input_action_label),
            pendingIntent
        )
            .addRemoteInput(remoteInput)
            .build()
        
        return NotificationCompat.Builder(context, NotificationChannels.QUICK_INPUT)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(context.getString(R.string.quick_input_notification_title))
            .setContentText(context.getString(R.string.quick_input_notification_content))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)  // 常驻通知
            .setOnlyAlertOnce(true)  // 只在首次显示时提醒
            .addAction(action)
            .build()
    }
    
    /**
     * 创建 RemoteInput 对象
     */
    private fun createRemoteInput(): RemoteInput {
        return RemoteInput.Builder(KEY_TEXT_REPLY)
            .setLabel(context.getString(R.string.quick_input_hint))
            .build()
    }
    
    /**
     * 创建 PendingIntent
     */
    private fun createPendingIntent(): PendingIntent {
        val intent = Intent(context, QuickInputActionReceiver::class.java).apply {
            action = ACTION_QUICK_INPUT
        }
        
        return PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
    }
}
