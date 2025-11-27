package takagi.ru.saison.domain.model

import java.time.LocalDate

/**
 * 订阅服务数据模型
 */
data class Subscription(
    val id: Long = 0,
    val name: String,
    val description: String? = null,
    val price: Double,
    val currency: String = "CNY",
    val billingCycle: BillingCycle,
    val startDate: LocalDate,
    val nextBillingDate: LocalDate,
    val reminderDaysBefore: Int = 3,
    val isActive: Boolean = true,
    val category: String? = null,
    val icon: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class BillingCycle {
    MONTHLY,
    QUARTERLY,
    YEARLY,
    WEEKLY
}
