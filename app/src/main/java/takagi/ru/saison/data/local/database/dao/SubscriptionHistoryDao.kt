package takagi.ru.saison.data.local.database.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import takagi.ru.saison.data.local.database.entities.SubscriptionHistoryEntity

@Dao
interface SubscriptionHistoryDao {
    @Query("SELECT * FROM subscription_history WHERE subscriptionId = :subscriptionId ORDER BY timestamp DESC")
    fun getHistoryForSubscription(subscriptionId: Long): Flow<List<SubscriptionHistoryEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: SubscriptionHistoryEntity): Long
    
    @Query("DELETE FROM subscription_history WHERE subscriptionId = :subscriptionId")
    suspend fun deleteHistoryForSubscription(subscriptionId: Long)
    
    @Query("SELECT * FROM subscription_history WHERE id = :id")
    suspend fun getHistoryById(id: Long): SubscriptionHistoryEntity?
}
