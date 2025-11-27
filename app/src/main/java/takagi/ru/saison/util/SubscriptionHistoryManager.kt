package takagi.ru.saison.util

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import takagi.ru.saison.data.local.database.entities.HistoryOperationType
import takagi.ru.saison.data.local.database.entities.SubscriptionEntity
import takagi.ru.saison.data.local.database.entities.SubscriptionHistoryEntity
import takagi.ru.saison.data.repository.SubscriptionHistoryRepository
import takagi.ru.saison.domain.model.SkipDuration
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class SubscriptionSnapshot(
    val name: String,
    val price: Double,
    val cycleType: String,
    val nextRenewalDate: Long,
    val isActive: Boolean,
    val isPaused: Boolean
)

@Serializable
data class SkipMetadata(
    val skipAmount: Int,
    val skipUnit: String,
    val oldRenewalDate: Long,
    val newRenewalDate: Long
)

@Singleton
class SubscriptionHistoryManager @Inject constructor(
    private val historyRepository: SubscriptionHistoryRepository
) {
    private val json = Json { ignoreUnknownKeys = true }
    
    suspend fun recordCreated(subscription: SubscriptionEntity) {
        val snapshot = createSnapshot(subscription)
        val entry = SubscriptionHistoryEntity(
            subscriptionId = subscription.id,
            operationType = HistoryOperationType.CREATED.name,
            timestamp = subscription.createdAt,
            newValue = json.encodeToString(snapshot),
            description = "订阅创建"
        )
        historyRepository.addHistoryEntry(entry)
    }
    
    suspend fun recordRenewed(subscription: SubscriptionEntity, isAuto: Boolean) {
        val snapshot = createSnapshot(subscription)
        val operationType = if (isAuto) HistoryOperationType.RENEWED_AUTO else HistoryOperationType.RENEWED_MANUAL
        val entry = SubscriptionHistoryEntity(
            subscriptionId = subscription.id,
            operationType = operationType.name,
            timestamp = System.currentTimeMillis(),
            newValue = json.encodeToString(snapshot),
            description = if (isAuto) "自动续订" else "手动续订"
        )
        historyRepository.addHistoryEntry(entry)
    }
    
    suspend fun recordSkipped(
        subscription: SubscriptionEntity,
        skipDuration: SkipDuration,
        oldRenewalDate: Long,
        newRenewalDate: Long
    ) {
        val metadata = SkipMetadata(
            skipAmount = skipDuration.amount,
            skipUnit = skipDuration.unit.name,
            oldRenewalDate = oldRenewalDate,
            newRenewalDate = newRenewalDate
        )
        val entry = SubscriptionHistoryEntity(
            subscriptionId = subscription.id,
            operationType = HistoryOperationType.SKIPPED.name,
            timestamp = System.currentTimeMillis(),
            metadata = json.encodeToString(metadata),
            description = "跳过 ${skipDuration.amount} ${getUnitText(skipDuration.unit)}（此期间不计费）"
        )
        historyRepository.addHistoryEntry(entry)
    }
    
    suspend fun recordPaused(subscription: SubscriptionEntity, reason: String? = null) {
        val snapshot = createSnapshot(subscription)
        val entry = SubscriptionHistoryEntity(
            subscriptionId = subscription.id,
            operationType = HistoryOperationType.PAUSED.name,
            timestamp = System.currentTimeMillis(),
            oldValue = json.encodeToString(snapshot),
            description = reason ?: "订阅中断"
        )
        historyRepository.addHistoryEntry(entry)
    }
    
    suspend fun recordResumed(subscription: SubscriptionEntity) {
        val snapshot = createSnapshot(subscription)
        val entry = SubscriptionHistoryEntity(
            subscriptionId = subscription.id,
            operationType = HistoryOperationType.RESUMED.name,
            timestamp = System.currentTimeMillis(),
            newValue = json.encodeToString(snapshot),
            description = "订阅恢复"
        )
        historyRepository.addHistoryEntry(entry)
    }
    
    suspend fun recordModified(oldSubscription: SubscriptionEntity, newSubscription: SubscriptionEntity) {
        val oldSnapshot = createSnapshot(oldSubscription)
        val newSnapshot = createSnapshot(newSubscription)
        val entry = SubscriptionHistoryEntity(
            subscriptionId = newSubscription.id,
            operationType = HistoryOperationType.MODIFIED.name,
            timestamp = System.currentTimeMillis(),
            oldValue = json.encodeToString(oldSnapshot),
            newValue = json.encodeToString(newSnapshot),
            description = "订阅信息修改"
        )
        historyRepository.addHistoryEntry(entry)
    }
    
    private fun createSnapshot(subscription: SubscriptionEntity): SubscriptionSnapshot {
        return SubscriptionSnapshot(
            name = subscription.name,
            price = subscription.price,
            cycleType = subscription.cycleType,
            nextRenewalDate = subscription.nextRenewalDate,
            isActive = subscription.isActive,
            isPaused = subscription.isPaused
        )
    }
    
    private fun getUnitText(unit: takagi.ru.saison.domain.model.SkipUnit): String {
        return when (unit) {
            takagi.ru.saison.domain.model.SkipUnit.DAYS -> "天"
            takagi.ru.saison.domain.model.SkipUnit.MONTHS -> "个月"
            takagi.ru.saison.domain.model.SkipUnit.YEARS -> "年"
        }
    }
}
