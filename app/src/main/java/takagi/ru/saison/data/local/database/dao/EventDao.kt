package takagi.ru.saison.data.local.database.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import takagi.ru.saison.data.local.database.entities.EventEntity

@Dao
interface EventDao {
    
    @Query("SELECT * FROM events ORDER BY eventDate ASC")
    fun getAllEvents(): Flow<List<EventEntity>>
    
    @Query("SELECT * FROM events WHERE id = :id")
    fun getEventById(id: Long): Flow<EventEntity?>
    
    @Query("SELECT * FROM events WHERE id = :id")
    suspend fun getEventByIdSync(id: Long): EventEntity?
    
    @Query("SELECT * FROM events WHERE category = :category ORDER BY eventDate ASC")
    fun getEventsByCategory(category: Int): Flow<List<EventEntity>>
    
    @Query("SELECT * FROM events WHERE isCompleted = 0 ORDER BY eventDate ASC")
    fun getIncompleteEvents(): Flow<List<EventEntity>>
    
    @Query("SELECT * FROM events WHERE isCompleted = 1 ORDER BY eventDate DESC")
    fun getCompletedEvents(): Flow<List<EventEntity>>
    
    @Query("SELECT * FROM events WHERE eventDate BETWEEN :startDate AND :endDate ORDER BY eventDate ASC")
    fun getEventsByDateRange(startDate: Long, endDate: Long): Flow<List<EventEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: EventEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<EventEntity>): List<Long>
    
    @Update
    suspend fun updateEvent(event: EventEntity)
    
    @Delete
    suspend fun deleteEvent(event: EventEntity)
    
    @Query("DELETE FROM events WHERE id = :id")
    suspend fun deleteEventById(id: Long)
    
    @Query("DELETE FROM events")
    suspend fun deleteAllEvents()
    
    @Query("SELECT COUNT(*) FROM events")
    fun getEventCount(): Flow<Int>
    
    @Query("SELECT COUNT(*) FROM events WHERE category = :category")
    fun getEventCountByCategory(category: Int): Flow<Int>
}
