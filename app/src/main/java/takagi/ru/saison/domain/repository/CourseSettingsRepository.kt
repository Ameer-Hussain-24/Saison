package takagi.ru.saison.domain.repository

import kotlinx.coroutines.flow.Flow
import takagi.ru.saison.domain.model.CourseSettings

/**
 * 课程设置仓库接口
 * 负责管理课程设置的持久化
 */
interface CourseSettingsRepository {
    /**
     * 获取课程设置
     */
    fun getSettings(): Flow<CourseSettings>
    
    /**
     * 更新课程设置
     */
    suspend fun updateSettings(settings: CourseSettings)
    
    /**
     * 重置为默认设置
     */
    suspend fun resetToDefault()
}
