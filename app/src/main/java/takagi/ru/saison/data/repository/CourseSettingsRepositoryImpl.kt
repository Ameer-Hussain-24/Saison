package takagi.ru.saison.data.repository

import kotlinx.coroutines.flow.Flow
import takagi.ru.saison.data.local.datastore.PreferencesManager
import takagi.ru.saison.domain.model.CourseSettings
import takagi.ru.saison.domain.repository.CourseSettingsRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 课程设置仓库实现
 * 使用 PreferencesManager 管理课程设置的持久化
 */
@Singleton
class CourseSettingsRepositoryImpl @Inject constructor(
    private val preferencesManager: PreferencesManager
) : CourseSettingsRepository {
    
    override fun getSettings(): Flow<CourseSettings> {
        return preferencesManager.courseSettings
    }
    
    override suspend fun updateSettings(settings: CourseSettings) {
        preferencesManager.setCourseSettings(settings)
    }
    
    override suspend fun resetToDefault() {
        preferencesManager.setCourseSettings(CourseSettings())
    }
}
