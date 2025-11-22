package takagi.ru.saison.data.local.database.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import takagi.ru.saison.data.local.database.entities.PomodoroSessionEntity

@Dao
interface PomodoroDao {
    
    @Query("SELECT * FROM pomodoro_sessions ORDER BY startTime DESC")
    fun getAllSessionsFlow(): Flow<List<PomodoroSessionEntity>>
    
    @Query("SELECT * FROM pomodoro_sessions WHERE id = :sessionId")
    suspend fun getSessionById(sessionId: Long): PomodoroSessionEntity?
    
    @Query("SELECT * FROM pomodoro_sessions WHERE taskId = :taskId ORDER BY startTime DESC")
    fun getSessionsByTask(taskId: Long): Flow<List<PomodoroSessionEntity>>
    
    @Query("SELECT * FROM pomodoro_sessions WHERE startTime BETWEEN :startDate AND :endDate ORDER BY startTime DESC")
    fun getSessionsByDateRange(startDate: Long, endDate: Long): Flow<List<PomodoroSessionEntity>>
    
    @Query("SELECT * FROM pomodoro_sessions WHERE isCompleted = 1 AND startTime BETWEEN :startDate AND :endDate")
    fun getCompletedSessionsByDateRange(startDate: Long, endDate: Long): Flow<List<PomodoroSessionEntity>>
    
    @Query("SELECT COUNT(*) FROM pomodoro_sessions WHERE isCompleted = 1 AND startTime BETWEEN :startDate AND :endDate")
    fun getCompletedSessionCount(startDate: Long, endDate: Long): Flow<Int>
    
    @Query("SELECT SUM(duration) FROM pomodoro_sessions WHERE isCompleted = 1 AND startTime BETWEEN :startDate AND :endDate")
    fun getTotalFocusTime(startDate: Long, endDate: Long): Flow<Int?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: PomodoroSessionEntity): Long
    
    @Update
    suspend fun update(session: PomodoroSessionEntity)
    
    @Delete
    suspend fun delete(session: PomodoroSessionEntity)
    
    @Query("DELETE FROM pomodoro_sessions WHERE id = :sessionId")
    suspend fun deleteById(sessionId: Long)
    
    @Query("DELETE FROM pomodoro_sessions WHERE startTime < :timestamp")
    suspend fun deleteSessionsBefore(timestamp: Long)
    
    @Query("UPDATE pomodoro_sessions SET isCompleted = 1, endTime = :endTime WHERE id = :sessionId")
    suspend fun completeSession(sessionId: Long, endTime: Long)
}
