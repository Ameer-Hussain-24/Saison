package takagi.ru.saison.data.local.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subscriptions")
data class SubscriptionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val category: String, // e.g., "数字产品", "生活服务"
    val price: Double,
    val currency: String = "CNY",
    val cycleType: String, // "MONTHLY", "QUARTERLY", "YEARLY", "CUSTOM"
    val cycleDuration: Int = 1,
    val startDate: Long, // Timestamp
    val endDate: Long? = null, // NEW: 可选的结束日期
    val nextRenewalDate: Long, // Timestamp
    val autoRenewal: Boolean = true, // Auto-renewal enabled by default
    val reminderEnabled: Boolean = false,
    val reminderDaysBefore: Int = 1,
    val note: String? = null,
    val isActive: Boolean = true,
    val isPaused: Boolean = false, // NEW: 中断状态
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
