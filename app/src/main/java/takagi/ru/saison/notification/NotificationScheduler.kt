package takagi.ru.saison.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.work.*
import dagger.hilt.android.qualifiers.ApplicationContext
import takagi.ru.saison.domain.model.notification.NotificationData
import takagi.ru.saison.domain.model.notification.NotificationType
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 通知调度器
 * 负责使用 WorkManager 和 AlarmManager 调度通知
 */
@Singleton
class NotificationScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val workManager = WorkManager.getInstance(context)
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    
    /**
     * 调度单次通知
     * 
     * @param notificationId 通知ID
     * @param triggerTime 触发时间（毫秒时间戳）
     * @param data 通知数据
     */
    fun scheduleOneTime(
        notificationId: String,
        triggerTime: Long,
        data: NotificationData
    ) {
        val delay = triggerTime - System.currentTimeMillis()
        
        if (delay <= 0) {
            // 时间已过，不调度
            return
        }
        
        // 对于番茄钟通知，使用 AlarmManager 确保精确时间
        if (data.type == NotificationType.POMODORO_WORK_END || 
            data.type == NotificationType.POMODORO_BREAK_END) {
            scheduleWithAlarmManager(notificationId, triggerTime, data)
        } else {
            // 其他通知使用 WorkManager
            scheduleWithWorkManager(notificationId, delay, data)
        }
    }
    
    /**
     * 使用 WorkManager 调度通知
     */
    private fun scheduleWithWorkManager(
        notificationId: String,
        delay: Long,
        data: NotificationData
    ) {
        val inputData = workDataOf(
            "notification_id" to notificationId,
            "notification_type" to data.type.name,
            "item_id" to data.itemId,
            "title" to data.title,
            "content" to data.content,
            "priority" to data.priority.name
        )
        
        val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(inputData)
            .addTag(notificationId)
            .addTag("notification_${data.type.name}")
            .build()
        
        workManager.enqueueUniqueWork(
            notificationId,
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }
    
    /**
     * 使用 AlarmManager 调度精确时间通知
     */
    private fun scheduleWithAlarmManager(
        notificationId: String,
        triggerTime: Long,
        data: NotificationData
    ) {
        val intent = Intent(context, NotificationAlarmReceiver::class.java).apply {
            putExtra("notification_id", notificationId)
            putExtra("notification_type", data.type.name)
            putExtra("item_id", data.itemId)
            putExtra("title", data.title)
            putExtra("content", data.content)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // 使用精确闹钟
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }
    }
    
    /**
     * 取消通知调度
     * 
     * @param notificationId 通知ID
     */
    fun cancel(notificationId: String) {
        // 取消 WorkManager 任务
        workManager.cancelUniqueWork(notificationId)
        
        // 取消 AlarmManager 闹钟
        val intent = Intent(context, NotificationAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId.hashCode(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        
        pendingIntent?.let {
            alarmManager.cancel(it)
            it.cancel()
        }
    }
    
    /**
     * 取消指定类型的所有通知
     * 
     * @param type 通知类型
     */
    fun cancelAll(type: NotificationType) {
        workManager.cancelAllWorkByTag("notification_${type.name}")
    }
    

}

/**
 * 通知 Worker
 * 用于在指定时间显示通知
 */
class NotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        val notificationId = inputData.getString("notification_id") ?: return Result.failure()
        val type = inputData.getString("notification_type") ?: return Result.failure()
        val itemId = inputData.getLong("item_id", 0)
        val title = inputData.getString("title") ?: ""
        val content = inputData.getString("content") ?: ""
        
        // 显示通知
        // 这里需要调用 NotificationManager 来显示通知
        // 具体实现将在后续任务中完成
        
        return Result.success()
    }
}

/**
 * 通知闹钟接收器
 * 用于接收 AlarmManager 触发的通知
 */
class NotificationAlarmReceiver : android.content.BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val notificationId = intent.getStringExtra("notification_id") ?: return
        val type = intent.getStringExtra("notification_type") ?: return
        val itemId = intent.getLongExtra("item_id", 0)
        val title = intent.getStringExtra("title") ?: ""
        val content = intent.getStringExtra("content") ?: ""
        
        // 显示通知
        // 这里需要调用 NotificationManager 来显示通知
        // 具体实现将在后续任务中完成
    }
}
