package takagi.ru.saison.data.repository

import kotlinx.coroutines.flow.Flow
import takagi.ru.saison.data.local.database.dao.SubscriptionHistoryDao
import takagi.ru.saison.data.local.database.entities.SubscriptionHistoryEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubscriptionHistoryRepository @Inject constructor(
    private val historyDao: SubscriptionHistoryDao
) {
    fun getHistoryForSubscription(subscriptionId: Long): Flow<List<SubscriptionHistoryEntity>> =
        historyDao.getHistoryForSubscription(subscriptionId)
    
    suspend fun addHistoryEntry(entry: SubscriptionHistoryEntity): Long =
        historyDao.insertHistory(entry)
    
    suspend fun deleteHistoryForSubscription(subscriptionId: Long) =
        historyDao.deleteHistoryForSubscription(subscriptionId)
}
