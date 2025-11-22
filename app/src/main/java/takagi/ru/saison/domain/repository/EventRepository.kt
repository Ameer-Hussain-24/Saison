package takagi.ru.saison.domain.repository

import kotlinx.coroutines.flow.Flow
import takagi.ru.saison.domain.model.Event
import takagi.ru.saison.domain.model.EventCategory

interface EventRepository {
    fun getAllEvents(): Flow<List<Event>>
    fun getEventById(id: Long): Flow<Event?>
    suspend fun getEventByIdSync(id: Long): Event?
    fun getEventsByCategory(category: EventCategory): Flow<List<Event>>
    fun getIncompleteEvents(): Flow<List<Event>>
    fun getCompletedEvents(): Flow<List<Event>>
    fun getEventsByDateRange(startDate: Long, endDate: Long): Flow<List<Event>>
    suspend fun insertEvent(event: Event): Long
    suspend fun insertEvents(events: List<Event>): List<Long>
    suspend fun updateEvent(event: Event)
    suspend fun deleteEvent(eventId: Long)
    suspend fun deleteAllEvents()
    fun getEventCount(): Flow<Int>
    fun getEventCountByCategory(category: EventCategory): Flow<Int>
}
