package takagi.ru.saison.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import takagi.ru.saison.data.local.database.dao.PomodoroDao
import takagi.ru.saison.domain.mapper.toDomain
import takagi.ru.saison.domain.mapper.toEntity
import takagi.ru.saison.domain.model.PomodoroSession
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PomodoroRepository @Inject constructor(
    private val pomodoroDao: PomodoroDao
) {
    
    fun getAllSessions(): Flow<List<PomodoroSession>> {
        return pomodoroDao.getAllSessionsFlow().map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    fun getSessionsByTask(taskId: Long): Flow<List<PomodoroSession>> {
        return pomodoroDao.getSessionsByTask(taskId).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    fun getSessionsByDateRange(startDate: Long, endDate: Long): Flow<List<PomodoroSession>> {
        return pomodoroDao.getSessionsByDateRange(startDate, endDate).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    fun getCompletedSessionsByDateRange(startDate: Long, endDate: Long): Flow<List<PomodoroSession>> {
        return pomodoroDao.getCompletedSessionsByDateRange(startDate, endDate).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    fun getCompletedSessionCount(startDate: Long, endDate: Long): Flow<Int> {
        return pomodoroDao.getCompletedSessionCount(startDate, endDate)
    }
    
    fun getTotalFocusTime(startDate: Long, endDate: Long): Flow<Int> {
        return pomodoroDao.getTotalFocusTime(startDate, endDate).map { it ?: 0 }
    }
    
    suspend fun getSessionById(sessionId: Long): PomodoroSession? {
        return pomodoroDao.getSessionById(sessionId)?.toDomain()
    }
    
    suspend fun insertSession(session: PomodoroSession): Long {
        return pomodoroDao.insert(session.toEntity())
    }
    
    suspend fun updateSession(session: PomodoroSession) {
        pomodoroDao.update(session.toEntity())
    }
    
    suspend fun deleteSession(sessionId: Long) {
        pomodoroDao.deleteById(sessionId)
    }
    
    suspend fun completeSession(sessionId: Long) {
        pomodoroDao.completeSession(sessionId, System.currentTimeMillis())
    }
    
    suspend fun deleteOldSessions(days: Int) {
        val timestamp = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L)
        pomodoroDao.deleteSessionsBefore(timestamp)
    }
    
    suspend fun markSessionInterrupted(sessionId: Long) {
        val session = getSessionById(sessionId)
        session?.let {
            updateSession(it.copy(interruptions = it.interruptions + 1))
        }
    }
    
    fun getTodayStats(): Flow<List<PomodoroSession>> {
        val startOfDay = java.time.LocalDate.now().atStartOfDay()
            .atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endOfDay = java.time.LocalDate.now().atTime(23, 59, 59)
            .atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        return getSessionsByDateRange(startOfDay, endOfDay)
    }
}
