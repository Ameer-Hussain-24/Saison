package takagi.ru.saison.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import takagi.ru.saison.domain.model.notification.NotificationChannels
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 通知渠道管理器
 * 负责创建和管理通知渠道
 */
@Singleton
class NotificationChannelManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val notificationManager = NotificationManagerCompat.from(context)
    
    /**
     * 初始化所有通知渠道
     * 在 Android 8.0 (API 26) 及以上版本创建通知渠道
     */
    fun initializeChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channels = NotificationChannels.getAllChannels()
            
            channels.forEach { config ->
                val channel = NotificationChannel(
                    config.id,
                    config.name,
                    config.importance
                ).apply {
                    description = config.description
                    enableVibration(config.enableVibration)
                    if (config.enableSound) {
                        setSound(
                            android.provider.Settings.System.DEFAULT_NOTIFICATION_URI,
                            null
                        )
                    }
                }
                
                notificationManager.createNotificationChannel(channel)
            }
        }
    }
    
    /**
     * 检查指定渠道是否已创建
     * 
     * @param channelId 渠道ID
     * @return 渠道是否存在
     */
    fun isChannelCreated(channelId: String): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return notificationManager.getNotificationChannel(channelId) != null
        }
        return true // Android 8.0 以下版本不需要渠道
    }
    
    /**
     * 获取渠道状态
     * 
     * @param channelId 渠道ID
     * @return 渠道是否启用
     */
    fun isChannelEnabled(channelId: String): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = notificationManager.getNotificationChannel(channelId)
            return channel?.importance != NotificationManager.IMPORTANCE_NONE
        }
        return true
    }
    
    /**
     * 删除指定渠道
     * 
     * @param channelId 渠道ID
     */
    fun deleteChannel(channelId: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.deleteNotificationChannel(channelId)
        }
    }
}
