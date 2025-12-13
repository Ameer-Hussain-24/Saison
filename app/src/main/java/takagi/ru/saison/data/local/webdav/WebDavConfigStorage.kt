package takagi.ru.saison.data.local.webdav

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import takagi.ru.saison.domain.model.backup.BackupPreferences
import takagi.ru.saison.domain.model.backup.WebDavConfig
import javax.inject.Inject
import javax.inject.Singleton

/**
 * WebDAV 配置存储管理器
 * 
 * 统一管理所有 WebDAV 相关的配置存储，包括:
 * - 服务器配置 (URL, 用户名, 密码)
 * - 备份偏好设置
 * - 自动备份设置
 * - 最后备份时间等元数据
 * 
 * 支持从旧版本配置自动迁移，确保向后兼容性
 */
@Singleton
class WebDavConfigStorage @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    companion object {
        // 新的统一配置文件名
        private const val PREFS_NAME = "webdav_config_v2"
        
        // 旧版本配置文件名（用于兼容性迁移）
        private const val LEGACY_PREFS_NAME = "webdav_backup_config"
        
        // 服务器配置相关键
        private object ServerKeys {
            const val URL = "server_url"
            const val USERNAME = "username"
            const val PASSWORD = "password"
        }
        
        // 备份偏好设置相关键
        private object BackupPreferenceKeys {
            const val INCLUDE_TASKS = "backup_include_tasks"
            const val INCLUDE_COURSES = "backup_include_courses"
            const val INCLUDE_EVENTS = "backup_include_events"
            const val INCLUDE_ROUTINES = "backup_include_routines"
            const val INCLUDE_SUBSCRIPTIONS = "backup_include_subscriptions"
            const val INCLUDE_VALUE_DAYS = "backup_include_value_days"
            const val INCLUDE_POMODORO = "backup_include_pomodoro"
            const val INCLUDE_SEMESTERS = "backup_include_semesters"
            const val INCLUDE_PREFERENCES = "backup_include_preferences"
        }
        
        // 自动备份相关键
        private object AutoBackupKeys {
            const val ENABLED = "auto_backup_enabled"
            const val LAST_BACKUP_TIME = "last_backup_time"
        }
        
        // 元数据相关键
        private object MetadataKeys {
            const val CONFIG_VERSION = "config_version"
            const val MIGRATED_FROM_LEGACY = "migrated_from_legacy"
        }
        
        private const val CURRENT_CONFIG_VERSION = 2
    }
    
    init {
        // 自动检查并执行数据迁移
        migrateFromLegacyIfNeeded()
    }
    
    // ==================== 服务器配置管理 ====================
    
    /**
     * 保存服务器配置
     * @param url 服务器 URL（会自动去除尾部斜杠）
     * @param username 用户名
     * @param password 密码（如果为空则不更新现有密码）
     */
    fun saveServerConfig(url: String, username: String, password: String) {
        prefs.edit {
            putString(ServerKeys.URL, url.trimEnd('/'))
            putString(ServerKeys.USERNAME, username)
            // 只有当密码不为空时才更新密码，允许在编辑时保留原密码
            if (password.isNotBlank()) {
                putString(ServerKeys.PASSWORD, password)
            }
        }
    }
    
    /**
     * 获取当前服务器配置
     * @return WebDavConfig 或 null（如果未配置）
     */
    fun getServerConfig(): WebDavConfig? {
        val url = prefs.getString(ServerKeys.URL, null)
        val username = prefs.getString(ServerKeys.USERNAME, null)
        return if (!url.isNullOrEmpty() && !username.isNullOrEmpty()) {
            WebDavConfig(url, username)
        } else {
            null
        }
    }
    
    /**
     * 获取服务器 URL
     */
    fun getServerUrl(): String? = prefs.getString(ServerKeys.URL, null)
    
    /**
     * 获取用户名
     */
    fun getUsername(): String? = prefs.getString(ServerKeys.USERNAME, null)
    
    /**
     * 获取密码（内部使用）
     */
    internal fun getPassword(): String = prefs.getString(ServerKeys.PASSWORD, "") ?: ""
    
    /**
     * 检查是否已配置服务器
     */
    fun isServerConfigured(): Boolean {
        val url = prefs.getString(ServerKeys.URL, null)
        val username = prefs.getString(ServerKeys.USERNAME, null)
        val password = prefs.getString(ServerKeys.PASSWORD, null)
        return !url.isNullOrEmpty() && !username.isNullOrEmpty() && !password.isNullOrEmpty()
    }
    
    // ==================== 备份偏好设置管理 ====================
    
    /**
     * 保存备份偏好设置
     */
    fun saveBackupPreferences(preferences: BackupPreferences) {
        prefs.edit {
            putBoolean(BackupPreferenceKeys.INCLUDE_TASKS, preferences.includeTasks)
            putBoolean(BackupPreferenceKeys.INCLUDE_COURSES, preferences.includeCourses)
            putBoolean(BackupPreferenceKeys.INCLUDE_EVENTS, preferences.includeEvents)
            putBoolean(BackupPreferenceKeys.INCLUDE_ROUTINES, preferences.includeRoutines)
            putBoolean(BackupPreferenceKeys.INCLUDE_SUBSCRIPTIONS, preferences.includeSubscriptions)
            putBoolean(BackupPreferenceKeys.INCLUDE_VALUE_DAYS, preferences.includeValueDays)
            putBoolean(BackupPreferenceKeys.INCLUDE_POMODORO, preferences.includePomodoroSessions)
            putBoolean(BackupPreferenceKeys.INCLUDE_SEMESTERS, preferences.includeSemesters)
            putBoolean(BackupPreferenceKeys.INCLUDE_PREFERENCES, preferences.includePreferences)
        }
    }
    
    /**
     * 获取备份偏好设置
     */
    fun getBackupPreferences(): BackupPreferences {
        return BackupPreferences(
            includeTasks = prefs.getBoolean(BackupPreferenceKeys.INCLUDE_TASKS, true),
            includeCourses = prefs.getBoolean(BackupPreferenceKeys.INCLUDE_COURSES, true),
            includeEvents = prefs.getBoolean(BackupPreferenceKeys.INCLUDE_EVENTS, true),
            includeRoutines = prefs.getBoolean(BackupPreferenceKeys.INCLUDE_ROUTINES, true),
            includeSubscriptions = prefs.getBoolean(BackupPreferenceKeys.INCLUDE_SUBSCRIPTIONS, true),
            includeValueDays = prefs.getBoolean(BackupPreferenceKeys.INCLUDE_VALUE_DAYS, true),
            includePomodoroSessions = prefs.getBoolean(BackupPreferenceKeys.INCLUDE_POMODORO, true),
            includeSemesters = prefs.getBoolean(BackupPreferenceKeys.INCLUDE_SEMESTERS, true),
            includePreferences = prefs.getBoolean(BackupPreferenceKeys.INCLUDE_PREFERENCES, true)
        )
    }
    
    // ==================== 自动备份设置管理 ====================
    
    /**
     * 设置自动备份开关
     */
    fun setAutoBackupEnabled(enabled: Boolean) {
        prefs.edit {
            putBoolean(AutoBackupKeys.ENABLED, enabled)
        }
    }
    
    /**
     * 检查自动备份是否启用
     */
    fun isAutoBackupEnabled(): Boolean = prefs.getBoolean(AutoBackupKeys.ENABLED, false)
    
    /**
     * 更新最后备份时间
     */
    fun updateLastBackupTime(timestamp: Long = System.currentTimeMillis()) {
        prefs.edit {
            putLong(AutoBackupKeys.LAST_BACKUP_TIME, timestamp)
        }
    }
    
    /**
     * 获取最后备份时间
     */
    fun getLastBackupTime(): Long = prefs.getLong(AutoBackupKeys.LAST_BACKUP_TIME, 0)
    
    /**
     * 检查是否应该执行自动备份
     * 
     * 条件:
     * 1. 自动备份已启用
     * 2. 从未备份过，或者
     * 3. 距离上次备份已经是新的一天，或者
     * 4. 距离上次备份超过 12 小时
     */
    fun shouldAutoBackup(): Boolean {
        if (!isAutoBackupEnabled()) return false
        
        val lastBackupTime = getLastBackupTime()
        if (lastBackupTime == 0L) return true
        
        val currentTime = System.currentTimeMillis()
        val hoursSinceLastBackup = (currentTime - lastBackupTime) / (1000 * 60 * 60)
        
        // 检查是否是新的一天
        val calendar = java.util.Calendar.getInstance()
        calendar.timeInMillis = lastBackupTime
        val lastBackupDay = calendar.get(java.util.Calendar.DAY_OF_YEAR)
        val lastBackupYear = calendar.get(java.util.Calendar.YEAR)
        
        calendar.timeInMillis = currentTime
        val currentDay = calendar.get(java.util.Calendar.DAY_OF_YEAR)
        val currentYear = calendar.get(java.util.Calendar.YEAR)
        
        val isNewDay = (currentYear > lastBackupYear) || 
                      (currentYear == lastBackupYear && currentDay > lastBackupDay)
        
        return isNewDay || hoursSinceLastBackup >= 12
    }
    
    // ==================== 配置管理 ====================
    
    /**
     * 清除所有配置
     */
    fun clearAll() {
        prefs.edit().clear().apply()
    }
    
    /**
     * 获取配置版本
     */
    private fun getConfigVersion(): Int = prefs.getInt(MetadataKeys.CONFIG_VERSION, 1)
    
    /**
     * 检查是否已从旧版本迁移
     */
    private fun isMigratedFromLegacy(): Boolean = 
        prefs.getBoolean(MetadataKeys.MIGRATED_FROM_LEGACY, false)
    
    // ==================== 数据迁移 ====================
    
    /**
     * 从旧版本配置自动迁移
     * 
     * 兼容旧的 SharedPreferences 配置文件，自动将数据迁移到新的存储结构
     */
    private fun migrateFromLegacyIfNeeded() {
        // 如果已经迁移过或者配置版本已经是最新，则跳过
        if (getConfigVersion() >= CURRENT_CONFIG_VERSION || isMigratedFromLegacy()) {
            return
        }
        
        val legacyPrefs = context.getSharedPreferences(LEGACY_PREFS_NAME, Context.MODE_PRIVATE)
        
        // 检查旧配置是否存在
        if (!legacyPrefs.contains("server_url")) {
            // 没有旧配置，标记为最新版本
            prefs.edit {
                putInt(MetadataKeys.CONFIG_VERSION, CURRENT_CONFIG_VERSION)
            }
            return
        }
        
        android.util.Log.d("WebDavConfigStorage", "检测到旧版本配置，开始迁移...")
        
        try {
            prefs.edit {
                // 迁移服务器配置
                legacyPrefs.getString("server_url", null)?.let {
                    putString(ServerKeys.URL, it)
                }
                legacyPrefs.getString("username", null)?.let {
                    putString(ServerKeys.USERNAME, it)
                }
                legacyPrefs.getString("password", null)?.let {
                    putString(ServerKeys.PASSWORD, it)
                }
                
                // 迁移备份偏好设置
                putBoolean(BackupPreferenceKeys.INCLUDE_TASKS, 
                    legacyPrefs.getBoolean("backup_include_tasks", true))
                putBoolean(BackupPreferenceKeys.INCLUDE_COURSES, 
                    legacyPrefs.getBoolean("backup_include_courses", true))
                putBoolean(BackupPreferenceKeys.INCLUDE_EVENTS, 
                    legacyPrefs.getBoolean("backup_include_events", true))
                putBoolean(BackupPreferenceKeys.INCLUDE_ROUTINES, 
                    legacyPrefs.getBoolean("backup_include_routines", true))
                putBoolean(BackupPreferenceKeys.INCLUDE_SUBSCRIPTIONS, 
                    legacyPrefs.getBoolean("backup_include_subscriptions", true))
                putBoolean(BackupPreferenceKeys.INCLUDE_POMODORO, 
                    legacyPrefs.getBoolean("backup_include_pomodoro", true))
                putBoolean(BackupPreferenceKeys.INCLUDE_SEMESTERS, 
                    legacyPrefs.getBoolean("backup_include_semesters", true))
                putBoolean(BackupPreferenceKeys.INCLUDE_PREFERENCES, 
                    legacyPrefs.getBoolean("backup_include_preferences", true))
                
                // 迁移自动备份设置
                putBoolean(AutoBackupKeys.ENABLED, 
                    legacyPrefs.getBoolean("auto_backup_enabled", false))
                putLong(AutoBackupKeys.LAST_BACKUP_TIME, 
                    legacyPrefs.getLong("last_backup_time", 0))
                
                // 标记迁移完成
                putInt(MetadataKeys.CONFIG_VERSION, CURRENT_CONFIG_VERSION)
                putBoolean(MetadataKeys.MIGRATED_FROM_LEGACY, true)
            }
            
            android.util.Log.d("WebDavConfigStorage", "配置迁移成功")
            
            // 可选：清理旧的配置文件（保留以防万一）
            // legacyPrefs.edit().clear().apply()
            
        } catch (e: Exception) {
            android.util.Log.e("WebDavConfigStorage", "配置迁移失败", e)
            // 迁移失败不影响程序运行，只是可能需要用户重新配置
        }
    }
    
    /**
     * 获取配置摘要（用于调试）
     */
    fun getConfigSummary(): String {
        return buildString {
            appendLine("WebDAV 配置摘要:")
            appendLine("- 配置版本: ${getConfigVersion()}")
            appendLine("- 已从旧版本迁移: ${isMigratedFromLegacy()}")
            appendLine("- 服务器已配置: ${isServerConfigured()}")
            appendLine("- 服务器 URL: ${getServerUrl() ?: "未设置"}")
            appendLine("- 用户名: ${getUsername() ?: "未设置"}")
            appendLine("- 自动备份: ${if (isAutoBackupEnabled()) "启用" else "禁用"}")
            appendLine("- 最后备份: ${getLastBackupTime()}")
            appendLine("- 备份偏好: ${getBackupPreferences()}")
        }
    }
}
