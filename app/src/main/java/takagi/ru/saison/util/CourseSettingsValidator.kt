package takagi.ru.saison.util

import takagi.ru.saison.domain.model.CourseSettings

/**
 * 课程设置验证工具类
 */
object CourseSettingsValidator {
    
    /**
     * 验证每天课程节数（保留用于向后兼容）
     * @return true 如果值在有效范围内（1-12）
     */
    fun validatePeriodsPerDay(value: Int): Boolean {
        return value in 1..12
    }
    
    /**
     * 验证上午节次数
     * @return true 如果值在有效范围内（1-6）
     */
    fun validateMorningPeriods(value: Int): Boolean {
        return value in 1..6
    }
    
    /**
     * 验证下午节次数
     * @return true 如果值在有效范围内（1-6）
     */
    fun validateAfternoonPeriods(value: Int): Boolean {
        return value in 1..6
    }
    
    /**
     * 验证晚上节次数
     * @return true 如果值在有效范围内（0-3）
     */
    fun validateEveningPeriods(value: Int): Boolean {
        return value in 0..3
    }
    
    /**
     * 验证总节次数
     * @return true 如果总节次数在有效范围内（1-12）
     */
    fun validateTotalPeriods(morningPeriods: Int, afternoonPeriods: Int, eveningPeriods: Int): Boolean {
        val total = morningPeriods + afternoonPeriods + eveningPeriods
        return total in 1..12
    }
    
    /**
     * 验证课程时长
     * @return true 如果值在有效范围内（30-120分钟，且为5的倍数）
     */
    fun validatePeriodDuration(value: Int): Boolean {
        return value in 30..120 && value % 5 == 0
    }
    
    /**
     * 验证课间休息时长
     * @return true 如果值在有效范围内（5-30分钟，且为5的倍数）
     */
    fun validateBreakDuration(value: Int): Boolean {
        return value in 5..30 && value % 5 == 0
    }
    
    /**
     * 验证午休时长
     * @return true 如果值在有效范围内（30-180分钟，且为5的倍数）
     */
    fun validateLunchBreakDuration(value: Int): Boolean {
        return value in 30..180 && value % 5 == 0
    }
    
    /**
     * 验证晚休时长
     * @return true 如果值在有效范围内（30-120分钟，且为5的倍数）
     */
    fun validateDinnerBreakDuration(value: Int): Boolean {
        return value in 30..120 && value % 5 == 0
    }
    
    /**
     * 验证午休位置
     * @return true 如果值在有效范围内（1到periodsPerDay-1）
     */
    fun validateLunchBreakAfter(value: Int?, periodsPerDay: Int): Boolean {
        return value == null || value in 1 until periodsPerDay
    }
    
    /**
     * 验证节次范围
     * @return true 如果范围有效（start <= end 且都在有效范围内）
     */
    fun validatePeriodRange(start: Int, end: Int, maxPeriods: Int): Boolean {
        return start in 1..maxPeriods && 
               end in 1..maxPeriods && 
               start <= end
    }
    
    /**
     * 验证完整的课程设置
     * @return 验证结果，包含是否有效和错误消息
     */
    fun validateSettings(settings: CourseSettings): ValidationResult {
        // 验证总节次数
        if (settings.totalPeriods < 1 || settings.totalPeriods > 15) {
            return ValidationResult(false, "总节次数必须在1-15之间")
        }
        
        // 验证课程时长
        if (!validatePeriodDuration(settings.periodDuration)) {
            return ValidationResult(false, "课程时长必须在30-120分钟之间，且为5的倍数")
        }
        
        // 验证课间休息
        if (!validateBreakDuration(settings.breakDuration)) {
            return ValidationResult(false, "课间休息必须在5-30分钟之间，且为5的倍数")
        }
        
        // 验证午休时长
        if (!validateLunchBreakDuration(settings.lunchBreakDuration)) {
            return ValidationResult(false, "午休时长必须在30-180分钟之间，且为5的倍数")
        }
        
        // 验证晚休时长
        if (!validateDinnerBreakDuration(settings.dinnerBreakDuration)) {
            return ValidationResult(false, "晚休时长必须在30-120分钟之间，且为5的倍数")
        }
        
        // 验证午休位置（如果设置了）
        if (!validateLunchBreakAfter(settings.lunchBreakAfterPeriod, settings.totalPeriods)) {
            return ValidationResult(false, "午休位置必须在第1节到第${settings.totalPeriods - 1}节之间")
        }
        
        return ValidationResult(true, null)
    }
    
    /**
     * 验证结果数据类
     */
    data class ValidationResult(
        val isValid: Boolean,
        val errorMessage: String?
    )
}
