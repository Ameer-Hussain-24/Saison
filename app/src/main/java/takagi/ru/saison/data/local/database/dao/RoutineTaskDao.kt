package takagi.ru.saison.data.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import takagi.ru.saison.data.local.database.entity.RoutineTaskEntity

/**
 * 周期性任务数据访问对象
 */
@Dao
interface RoutineTaskDao {
    
    /**
     * 插入新的周期性任务
     * @return 插入的任务 ID
     */
    @Insert
    suspend fun insert(task: RoutineTaskEntity): Long
    
    /**
     * 更新周期性任务
     */
    @Update
    suspend fun update(task: RoutineTaskEntity)
    
    /**
     * 删除周期性任务
     */
    @Delete
    suspend fun delete(task: RoutineTaskEntity)
    
    /**
     * 根据 ID 获取周期性任务
     */
    @Query("SELECT * FROM routine_tasks WHERE id = :id")
    suspend fun getById(id: Long): RoutineTaskEntity?
    
    /**
     * 获取所有活跃的周期性任务
     * 使用 Flow 实现响应式数据流
     */
    @Query("SELECT * FROM routine_tasks WHERE is_active = 1 ORDER BY created_at DESC")
    fun getAllActive(): Flow<List<RoutineTaskEntity>>
    
    /**
     * 获取所有周期性任务（包括非活跃的）
     */
    @Query("SELECT * FROM routine_tasks ORDER BY created_at DESC")
    fun getAll(): Flow<List<RoutineTaskEntity>>
    
    /**
     * 根据 ID 删除周期性任务
     */
    @Query("DELETE FROM routine_tasks WHERE id = :id")
    suspend fun deleteById(id: Long)
}
