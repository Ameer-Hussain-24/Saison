package takagi.ru.saison.data.local.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "subscription_history",
    foreignKeys = [
        ForeignKey(
            entity = SubscriptionEntity::class,
            parentColumns = ["id"],
            childColumns = ["subscriptionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("subscriptionId"),
        Index("timestamp")
    ]
)
data class SubscriptionHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val subscriptionId: Long,
    val operationType: String, // CREATED, RENEWED_AUTO, RENEWED_MANUAL, SKIPPED, PAUSED, RESUMED, MODIFIED, DELETED
    val timestamp: Long = System.currentTimeMillis(),
    val oldValue: String? = null,  // JSON格式的旧值
    val newValue: String? = null,  // JSON格式的新值
    val description: String? = null,
    val metadata: String? = null  // 额外的元数据（JSON格式）
)

enum class HistoryOperationType {
    CREATED,
    RENEWED_AUTO,
    RENEWED_MANUAL,
    SKIPPED,
    PAUSED,
    RESUMED,
    MODIFIED,
    DELETED
}
