package takagi.ru.saison.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import takagi.ru.saison.data.local.database.dao.EventDao
import takagi.ru.saison.domain.mapper.toDomain
import takagi.ru.saison.domain.mapper.toEntity
import takagi.ru.saison.domain.model.Event
import takagi.ru.saison.domain.model.EventCategory
import takagi.ru.saison.domain.repository.EventRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventRepositoryImpl @Inject constructor(
    private val eventDao: EventDao
) : EventRepository {
    
    override fun getAllEvents(): Flow<List<Event>> {
        return eventDao.getAllEvents().map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override fun getEventById(id: Long): Flow<Event?> {
        return eventDao.getEventById(id).map { it?.toDomain() }
    }
    
    override suspend fun getEventByIdSync(id: Long): Event? {
        return eventDao.getEventByIdSync(id)?.toDomain()
    }
    
    override fun getEventsByCategory(category: EventCategory): Flow<List<Event>> {
        return eventDao.getEventsByCategory(category.value).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override fun getIncompleteEvents(): Flow<List<Event>> {
        return eventDao.getIncompleteEvents().map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override fun getCompletedEvents(): Flow<List<Event>> {
        return eventDao.getCompletedEvents().map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override fun getEventsByDateRange(startDate: Long, endDate: Long): Flow<List<Event>> {
        return eventDao.getEventsByDateRange(startDate, endDate).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override suspend fun insertEvent(event: Event): Long {
        return eventDao.insertEvent(event.toEntity())
    }
    
    override suspend fun insertEvents(events: List<Event>): List<Long> {
        return eventDao.insertEvents(events.map { it.toEntity() })
    }
    
    override suspend fun updateEvent(event: Event) {
        eventDao.updateEvent(event.toEntity())
    }
    
    override suspend fun deleteEvent(eventId: Long) {
        eventDao.deleteEventById(eventId)
    }
    
    override suspend fun deleteAllEvents() {
        eventDao.deleteAllEvents()
    }
    
    override fun getEventCount(): Flow<Int> {
        return eventDao.getEventCount()
    }
    
    override fun getEventCountByCategory(category: EventCategory): Flow<Int> {
        return eventDao.getEventCountByCategory(category.value)
    }
}
