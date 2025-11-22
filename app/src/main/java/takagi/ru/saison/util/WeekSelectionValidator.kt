package takagi.ru.saison.util

import java.time.LocalDate

/**
 * 周数选择验证器
 * 用于验证周数选择的有效性
 */
object WeekSelectionValidator {
    
    /**
     * 验证周数列表
     * @param weeks 周数列表
     * @param totalWeeks 学期总周数
     * @return 验证结果
     */
    fun validateWeekNumbers(weeks: List<Int>, totalWeeks: Int): ValidationResult {
        if (weeks.isEmpty()) {
            return ValidationResult.Error("请至少选择一周")
        }
        
        val invalidWeeks = weeks.filter { it < 1 || it > totalWeeks }
        return if (invalidWeeks.isEmpty()) {
            ValidationResult.Success
        } else {
            ValidationResult.Error("周数 ${invalidWeeks.joinToString(",")} 超出范围（1-$totalWeeks）")
        }
    }
    
    /**
     * 验证日期范围
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 验证结果
     */
    fun validateDateRange(startDate: LocalDate?, endDate: LocalDate?): ValidationResult {
        if (startDate == null || endDate == null) {
            return ValidationResult.Error("请选择开始和结束日期")
        }
        
        return if (endDate.isAfter(startDate) || endDate.isEqual(startDate)) {
            ValidationResult.Success
        } else {
            ValidationResult.Error("结束日期必须晚于或等于开始日期")
        }
    }
}

/**
 * 验证结果
 */
sealed class ValidationResult {
    object Success : ValidationResult()
    data class Error(val message: String) : ValidationResult()
}
