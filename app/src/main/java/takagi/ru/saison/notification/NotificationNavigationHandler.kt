package takagi.ru.saison.notification

import android.content.Context
import android.content.Intent
import dagger.hilt.android.qualifiers.ApplicationContext
import takagi.ru.saison.domain.model.notification.NotificationType
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 通知导航处理器
 * 处理通知点击后的导航逻辑
 */
@Singleton
class NotificationNavigationHandler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    companion object {
        const val EXTRA_NAVIGATION_TYPE = "extra_navigation_type"
        const val EXTRA_ITEM_ID = "extra_item_id"
        
        const val NAV_TASK_DETAIL = "nav_task_detail"
        const val NAV_COURSE_DETAIL = "nav_course_detail"
        const val NAV_POMODORO = "nav_pomodoro"
        const val NAV_TASK_LIST = "nav_task_list"
    }
    
    /**
     * 创建任务详情导航 Intent
     * 
     * @param taskId 任务ID
     * @return Intent
     */
    fun createTaskDetailIntent(taskId: Long): Intent {
        return Intent(context, takagi.ru.saison.MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_NAVIGATION_TYPE, NAV_TASK_DETAIL)
            putExtra(EXTRA_ITEM_ID, taskId)
        }
    }
    
    /**
     * 创建课程详情导航 Intent
     * 
     * @param courseId 课程ID
     * @return Intent
     */
    fun createCourseDetailIntent(courseId: Long): Intent {
        return Intent(context, takagi.ru.saison.MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_NAVIGATION_TYPE, NAV_COURSE_DETAIL)
            putExtra(EXTRA_ITEM_ID, courseId)
        }
    }
    
    /**
     * 创建番茄钟页面导航 Intent
     * 
     * @return Intent
     */
    fun createPomodoroIntent(): Intent {
        return Intent(context, takagi.ru.saison.MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_NAVIGATION_TYPE, NAV_POMODORO)
        }
    }
    
    /**
     * 创建任务列表导航 Intent（用于摘要通知）
     * 
     * @return Intent
     */
    fun createTaskListIntent(): Intent {
        return Intent(context, takagi.ru.saison.MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_NAVIGATION_TYPE, NAV_TASK_LIST)
        }
    }
    
    /**
     * 根据通知类型创建导航 Intent
     * 
     * @param type 通知类型
     * @param itemId 项目ID
     * @return Intent
     */
    fun createNavigationIntent(type: NotificationType, itemId: Long): Intent {
        return when (type) {
            NotificationType.TASK_REMINDER -> createTaskDetailIntent(itemId)
            NotificationType.COURSE_REMINDER -> createCourseDetailIntent(itemId)
            NotificationType.POMODORO_WORK_END,
            NotificationType.POMODORO_BREAK_END -> createPomodoroIntent()
            NotificationType.TASK_SUMMARY -> createTaskListIntent()
        }
    }
}
