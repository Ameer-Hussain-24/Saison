package takagi.ru.saison.data.repository

import kotlinx.coroutines.flow.Flow
import takagi.ru.saison.data.local.database.dao.SubscriptionDao
import takagi.ru.saison.data.local.database.entities.SubscriptionEntity
import takagi.ru.saison.domain.model.SkipDuration
import takagi.ru.saison.domain.model.SkipUnit
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubscriptionRepository @Inject constructor(
    private val subscriptionDao: SubscriptionDao
) {
    fun getAllSubscriptions(): Flow<List<SubscriptionEntity>> = subscriptionDao.getAllSubscriptions()

    fun getActiveSubscriptions(): Flow<List<SubscriptionEntity>> = subscriptionDao.getActiveSubscriptions()

    suspend fun getSubscriptionById(id: Long): SubscriptionEntity? = subscriptionDao.getSubscriptionById(id)

    suspend fun insertSubscription(subscription: SubscriptionEntity): Long = subscriptionDao.insertSubscription(subscription)

    suspend fun updateSubscription(subscription: SubscriptionEntity) = subscriptionDao.updateSubscription(subscription)

    suspend fun deleteSubscription(subscription: SubscriptionEntity) = subscriptionDao.deleteSubscription(subscription)
    
    suspend fun deleteSubscriptionById(id: Long) = subscriptionDao.deleteSubscriptionById(id)
    
    fun getSubscriptionsRequiringManualRenewal(currentTime: Long): Flow<List<SubscriptionEntity>> = 
        subscriptionDao.getSubscriptionsRequiringManualRenewal(currentTime)
    
    suspend fun updateRenewalDate(id: Long, newDate: Long) = 
        subscriptionDao.updateRenewalDate(id, newDate, System.currentTimeMillis())
    
    // 新增方法
    suspend fun pauseSubscription(id: Long, reason: String? = null) {
        val subscription = getSubscriptionById(id) ?: return
        val updated = subscription.copy(
            isPaused = true,
            isActive = false,
            autoRenewal = false,
            updatedAt = System.currentTimeMillis()
        )
        updateSubscription(updated)
    }
    
    suspend fun resumeSubscription(id: Long, newRenewalDate: Long) {
        val subscription = getSubscriptionById(id) ?: return
        val updated = subscription.copy(
            isPaused = false,
            isActive = true,
            nextRenewalDate = newRenewalDate,
            updatedAt = System.currentTimeMillis()
        )
        updateSubscription(updated)
    }
    
    suspend fun skipSubscription(id: Long, skipDuration: SkipDuration) {
        val subscription = getSubscriptionById(id) ?: return
        val currentRenewalDate = Instant.ofEpochMilli(subscription.nextRenewalDate)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        
        val newRenewalDate = when (skipDuration.unit) {
            SkipUnit.DAYS -> currentRenewalDate.plusDays(skipDuration.amount.toLong())
            SkipUnit.MONTHS -> currentRenewalDate.plusMonths(skipDuration.amount.toLong())
            SkipUnit.YEARS -> currentRenewalDate.plusYears(skipDuration.amount.toLong())
        }
        
        val newRenewalTimestamp = newRenewalDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val updated = subscription.copy(
            nextRenewalDate = newRenewalTimestamp,
            updatedAt = System.currentTimeMillis()
        )
        updateSubscription(updated)
    }
    
    suspend fun updateEndDate(id: Long, endDate: Long?) {
        val subscription = getSubscriptionById(id) ?: return
        val updated = subscription.copy(
            endDate = endDate,
            updatedAt = System.currentTimeMillis()
        )
        updateSubscription(updated)
    }
}
