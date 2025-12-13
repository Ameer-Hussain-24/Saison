package takagi.ru.saison.data.repository

import kotlinx.coroutines.flow.Flow
import takagi.ru.saison.data.local.database.entities.ValueDayEntity

interface ValueDayRepository {
    fun getAllValueDays(): Flow<List<ValueDayEntity>>
    fun getValueDaysByCategory(category: String): Flow<List<ValueDayEntity>>
    fun getAllCategories(): Flow<List<String>>
    suspend fun getValueDayById(id: Long): ValueDayEntity?
    suspend fun insertValueDay(valueDayEntity: ValueDayEntity): Long
    suspend fun updateValueDay(valueDayEntity: ValueDayEntity)
    suspend fun deleteValueDay(valueDayEntity: ValueDayEntity)
    suspend fun deleteValueDayById(id: Long)
    suspend fun deleteAllValueDays()
    fun getCount(): Flow<Int>
    fun getTotalCost(): Flow<Double?>
}
