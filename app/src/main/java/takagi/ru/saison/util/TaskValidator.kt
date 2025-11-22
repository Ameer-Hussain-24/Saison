package takagi.ru.saison.util

import takagi.ru.saison.domain.model.Task
import java.time.LocalDateTime

object TaskValidator {
    
    sealed class ValidationResult {
        object Success : ValidationResult()
        data class Error(val message: String) : ValidationResult()
    }
    
    /**
     * 验证任务标题
     * 规则：非空，长度在1-100之间
     */
    fun validateTitle(title: String): ValidationResult {
        return when {
            title.isBlank() -> ValidationResult.Error("任务标题不能为空")
            title.length > 100 -> ValidationResult.Error("任务标题不能超过100个字符")
            else -> ValidationResult.Success
        }
    }
    
    /**
     * 验证截止日期
     * 规则：不能早于当前时间
     */
    fun validateDueDate(dueDate: LocalDateTime?): ValidationResult {
        if (dueDate == null) return ValidationResult.Success
        
        val now = LocalDateTime.now()
        return if (dueDate.isBefore(now)) {
            ValidationResult.Error("截止日期不能早于当前时间")
        } else {
            ValidationResult.Success
        }
    }
    
    /**
     * 验证附件大小
     * 规则：不超过10MB
     */
    fun validateAttachmentSize(sizeInBytes: Long): ValidationResult {
        val maxSize = 10 * 1024 * 1024 // 10MB
        return if (sizeInBytes > maxSize) {
            ValidationResult.Error("附件大小不能超过10MB")
        } else {
            ValidationResult.Success
        }
    }
    
    /**
     * 验证描述长度
     * 规则：不超过500字符
     */
    fun validateDescription(description: String?): ValidationResult {
        if (description == null) return ValidationResult.Success
        
        return if (description.length > 500) {
            ValidationResult.Error("描述不能超过500个字符")
        } else {
            ValidationResult.Success
        }
    }
    
    /**
     * 验证整个任务
     */
    fun validateTask(task: Task): ValidationResult {
        validateTitle(task.title).let {
            if (it is ValidationResult.Error) return it
        }
        
        validateDescription(task.description).let {
            if (it is ValidationResult.Error) return it
        }
        
        validateDueDate(task.dueDate).let {
            if (it is ValidationResult.Error) return it
        }
        
        return ValidationResult.Success
    }
}
