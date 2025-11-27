package takagi.ru.saison.domain.usecase

import takagi.ru.saison.data.repository.TaskRepository
import takagi.ru.saison.domain.model.Priority
import takagi.ru.saison.domain.model.Task
import javax.inject.Inject

/**
 * 快速创建任务用例
 * 用于从通知栏快捷输入创建任务
 */
class CreateQuickTaskUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    /**
     * 执行快速任务创建
     * 
     * @param title 任务标题
     * @return Result<Long> 成功返回任务ID，失败返回错误信息
     */
    suspend operator fun invoke(title: String): Result<Long> {
        // 验证输入
        val trimmedTitle = title.trim()
        
        // 检查空值和纯空白字符
        if (trimmedTitle.isEmpty() || trimmedTitle.isBlank()) {
            return Result.failure(IllegalArgumentException("Task title cannot be empty"))
        }
        
        // 检查长度限制
        if (trimmedTitle.length > 500) {
            return Result.failure(IllegalArgumentException("Task title too long (max 500 characters)"))
        }
        
        // 创建任务对象，设置默认属性
        val currentTime = java.time.LocalDateTime.now()
        val task = Task(
            id = 0,
            title = trimmedTitle,
            description = "",
            isCompleted = false,
            priority = Priority.MEDIUM,  // 默认中等优先级
            createdAt = currentTime,
            updatedAt = currentTime,
            dueDate = null,  // 无截止时间
            tags = emptyList(),  // 无标签
            subtasks = emptyList(),  // 无子任务
            attachments = emptyList(),  // 无附件
            category = null  // 使用默认分类
        )
        
        // 插入任务
        return try {
            val taskId = taskRepository.insertTask(task)
            Result.success(taskId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
