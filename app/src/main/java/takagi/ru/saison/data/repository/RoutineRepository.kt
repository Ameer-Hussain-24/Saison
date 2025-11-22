package takagi.ru.saison.data.repository

import kotlinx.coroutines.flow.Flow
import takagi.ru.saison.domain.model.routine.CheckInRecord
import takagi.ru.saison.domain.model.routine.RoutineTask
import takagi.ru.saison.domain.model.routine.RoutineTaskWithStats
import java.time.LocalDate

/**
 * 周期性任务仓库接口
 */
interface RoutineRepository {
    
    // ========== 任务管理 ==========
    
    /**
     * 创建周期性任务
     * @return 创建的任务 ID
     */
    suspend fun createRoutineTask(task: RoutineTask): Long
    
    /**
     * 更新周期性任务
     */
    suspend fun updateRoutineTask(task: RoutineTask)
    
    /**
     * 删除周期性任务
     */
    suspend fun deleteRoutineTask(taskId: Long)
    
    /**
     * 根据 ID 获取周期性任务
     */
    suspend fun getRoutineTask(taskId: Long): RoutineTask?
    
    /**
     * 获取所有活跃的周期性任务
     */
    fun getAllRoutineTasks(): Flow<List<RoutineTask>>
    
    /**
     * 获取设置了活动时长的周期性任务（用于番茄钟选择）
     */
    fun getRoutineTasksWithDuration(): Flow<List<RoutineTask>>
    
    // ========== 打卡管理 ==========
    
    /**
     * 打卡
     * @param taskId 任务 ID
     * @param note 可选的打卡备注
     * @return 创建的打卡记录
     */
    suspend fun checkIn(taskId: Long, note: String? = null): CheckInRecord
    
    /**
     * 带备注的打卡（用于番茄钟自动打卡）
     * @param taskId 任务 ID
     * @param note 打卡备注
     * @return 创建的打卡记录
     */
    suspend fun checkInWithNote(taskId: Long, note: String): CheckInRecord
    
    /**
     * 删除打卡记录
     */
    suspend fun deleteCheckIn(checkInId: Long)
    
    /**
     * 获取任务的所有打卡记录
     */
    fun getCheckInRecords(taskId: Long): Flow<List<CheckInRecord>>
    
    /**
     * 获取任务的所有打卡记录（一次性查询，不使用 Flow）
     */
    suspend fun getCheckInRecordsOnce(taskId: Long): List<CheckInRecord>
    
    /**
     * 获取指定周期内的打卡记录
     */
    fun getCheckInRecordsInCycle(
        taskId: Long,
        cycleStart: LocalDate,
        cycleEnd: LocalDate
    ): Flow<List<CheckInRecord>>
    
    // ========== 统计查询 ==========
    
    /**
     * 获取所有周期性任务及其统计信息
     */
    fun getRoutineTasksWithStats(): Flow<List<RoutineTaskWithStats>>
    
    /**
     * 获取指定周期内的打卡次数
     */
    suspend fun getCheckInCountInCycle(
        taskId: Long,
        cycleStart: LocalDate,
        cycleEnd: LocalDate
    ): Int
}
