package takagi.ru.saison.data.local.database.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import takagi.ru.saison.data.local.database.entities.ValueDayEntity

@Dao
interface ValueDayDao {
    
    @Query("SELECT * FROM value_days ORDER BY purchaseDate DESC")
    fun getAllValueDays(): Flow<List<ValueDayEntity>>
    
    @Query("SELECT * FROM value_days WHERE category = :category ORDER BY purchaseDate DESC")
    fun getValueDaysByCategory(category: String): Flow<List<ValueDayEntity>>
    
    @Query("SELECT DISTINCT category FROM value_days ORDER BY category ASC")
    fun getAllCategories(): Flow<List<String>>
    
    @Query("SELECT * FROM value_days WHERE id = :id")
    suspend fun getValueDayById(id: Long): ValueDayEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(valueDayEntity: ValueDayEntity): Long
    
    @Update
    suspend fun update(valueDayEntity: ValueDayEntity)
    
    @Delete
    suspend fun delete(valueDayEntity: ValueDayEntity)
    
    @Query("DELETE FROM value_days WHERE id = :id")
    suspend fun deleteById(id: Long)
    
    @Query("DELETE FROM value_days")
    suspend fun deleteAll()
    
    @Query("SELECT COUNT(*) FROM value_days")
    fun getCount(): Flow<Int>
    
    @Query("SELECT SUM(purchasePrice) FROM value_days")
    fun getTotalCost(): Flow<Double?>
}
