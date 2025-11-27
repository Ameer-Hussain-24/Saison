package takagi.ru.saison.util

import takagi.ru.saison.domain.model.SkipDuration
import takagi.ru.saison.domain.model.SkipUnit
import java.time.LocalDate
import java.time.temporal.ChronoUnit

object SubscriptionDateCalculator {
    
    /**
     * 根据开始日期和结束日期计算订阅周期
     * @return Pair<周期时长, 周期类型>
     */
    fun calculateCycleDuration(startDate: LocalDate, endDate: LocalDate): Pair<Int, String> {
        val days = ChronoUnit.DAYS.between(startDate, endDate)
        val months = ChronoUnit.MONTHS.between(startDate, endDate)
        val years = ChronoUnit.YEARS.between(startDate, endDate)
        
        return when {
            years > 0 && months % 12 == 0L -> years.toInt() to "YEARLY"
            months > 0 && days % 30 < 5 -> months.toInt() to "MONTHLY"
            else -> days.toInt() to "DAILY"
        }
    }
    
    /**
     * 根据周期类型计算下次续订日期
     */
    fun calculateNextRenewalDate(
        startDate: LocalDate,
        cycleType: String,
        cycleDuration: Int = 1
    ): LocalDate {
        return when (cycleType) {
            "MONTHLY" -> startDate.plusMonths(cycleDuration.toLong())
            "QUARTERLY" -> startDate.plusMonths(3L * cycleDuration)
            "YEARLY" -> startDate.plusYears(cycleDuration.toLong())
            "WEEKLY" -> startDate.plusWeeks(cycleDuration.toLong())
            "DAILY" -> startDate.plusDays(cycleDuration.toLong())
            else -> startDate.plusMonths(1) // 默认月度
        }
    }
    
    /**
     * 计算跳过后的续订日期
     */
    fun calculateSkippedRenewalDate(
        currentRenewalDate: LocalDate,
        skipDuration: SkipDuration
    ): LocalDate {
        return when (skipDuration.unit) {
            SkipUnit.DAYS -> currentRenewalDate.plusDays(skipDuration.amount.toLong())
            SkipUnit.MONTHS -> currentRenewalDate.plusMonths(skipDuration.amount.toLong())
            SkipUnit.YEARS -> currentRenewalDate.plusYears(skipDuration.amount.toLong())
        }
    }
}
