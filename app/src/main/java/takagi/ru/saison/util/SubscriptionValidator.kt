package takagi.ru.saison.util

import java.time.LocalDate

object SubscriptionValidator {
    
    sealed class ValidationResult {
        object Valid : ValidationResult()
        data class Invalid(val message: String) : ValidationResult()
    }
    
    /**
     * 验证结束日期是否有效
     */
    fun validateEndDate(startDate: LocalDate, endDate: LocalDate?): ValidationResult {
        if (endDate == null) {
            return ValidationResult.Valid
        }
        
        return if (endDate.isAfter(startDate)) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid("结束日期必须晚于开始日期")
        }
    }
    
    /**
     * 验证订阅名称
     */
    fun validateName(name: String): ValidationResult {
        return when {
            name.isBlank() -> ValidationResult.Invalid("订阅名称不能为空")
            name.length > 100 -> ValidationResult.Invalid("订阅名称过长")
            else -> ValidationResult.Valid
        }
    }
    
    /**
     * 验证价格
     */
    fun validatePrice(price: Double): ValidationResult {
        return when {
            price < 0 -> ValidationResult.Invalid("价格不能为负数")
            price > 999999.99 -> ValidationResult.Invalid("价格超出范围")
            else -> ValidationResult.Valid
        }
    }
    
    /**
     * 验证跳过时长
     */
    fun validateSkipDuration(amount: Int): ValidationResult {
        return when {
            amount <= 0 -> ValidationResult.Invalid("跳过时长必须大于0")
            amount > 365 -> ValidationResult.Invalid("跳过时长过长")
            else -> ValidationResult.Valid
        }
    }
}
