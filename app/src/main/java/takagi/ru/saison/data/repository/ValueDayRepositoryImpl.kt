package takagi.ru.saison.data.repository

import kotlinx.coroutines.flow.Flow
import takagi.ru.saison.data.local.database.dao.ValueDayDao
import takagi.ru.saison.data.local.database.entities.ValueDayEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ValueDayRepositoryImpl @Inject constructor(
    private val valueDayDao: ValueDayDao
) : ValueDayRepository {
    
    override fun getAllValueDays(): Flow<List<ValueDayEntity>> {
        return valueDayDao.getAllValueDays()
    }
    
    override fun getValueDaysByCategory(category: String): Flow<List<ValueDayEntity>> {
        return valueDayDao.getValueDaysByCategory(category)
    }
    
    override fun getAllCategories(): Flow<List<String>> {
        return valueDayDao.getAllCategories()
    }
    
    override suspend fun getValueDayById(id: Long): ValueDayEntity? {
        return valueDayDao.getValueDayById(id)
    }
    
    override suspend fun insertValueDay(valueDayEntity: ValueDayEntity): Long {
        return valueDayDao.insert(valueDayEntity)
    }
    
    override suspend fun updateValueDay(valueDayEntity: ValueDayEntity) {
        valueDayDao.update(valueDayEntity.copy(updatedAt = System.currentTimeMillis()))
    }
    
    override suspend fun deleteValueDay(valueDayEntity: ValueDayEntity) {
        valueDayDao.delete(valueDayEntity)
    }
    
    override suspend fun deleteValueDayById(id: Long) {
        valueDayDao.deleteById(id)
    }
    
    override suspend fun deleteAllValueDays() {
        valueDayDao.deleteAll()
    }
    
    override fun getCount(): Flow<Int> {
        return valueDayDao.getCount()
    }
    
    override fun getTotalCost(): Flow<Double?> {
        return valueDayDao.getTotalCost()
    }
}
