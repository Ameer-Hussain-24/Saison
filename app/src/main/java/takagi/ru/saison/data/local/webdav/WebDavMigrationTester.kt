package takagi.ru.saison.data.local.webdav

import android.content.Context
import android.content.SharedPreferences
import javax.inject.Inject

/**
 * WebDAV 配置迁移测试工具
 * 
 * 用于验证从旧版本配置到新版本配置的迁移是否正确
 */
class WebDavMigrationTester @Inject constructor(
    private val context: Context,
    private val configStorage: WebDavConfigStorage
) {
    
    companion object {
        private const val LEGACY_PREFS_NAME = "webdav_backup_config"
        private const val TAG = "WebDavMigrationTester"
    }
    
    /**
     * 测试数据迁移
     * 
     * @return 测试结果报告
     */
    fun testMigration(): MigrationTestResult {
        val legacyPrefs = context.getSharedPreferences(LEGACY_PREFS_NAME, Context.MODE_PRIVATE)
        
        // 检查是否有旧配置
        val hasLegacyConfig = legacyPrefs.contains("server_url")
        
        if (!hasLegacyConfig) {
            return MigrationTestResult(
                success = true,
                message = "没有检测到旧配置，无需迁移",
                details = emptyMap()
            )
        }
        
        // 读取旧配置
        val legacyData = readLegacyConfig(legacyPrefs)
        
        // 读取新配置
        val newData = readNewConfig()
        
        // 比较配置
        val comparison = compareConfigs(legacyData, newData)
        
        return if (comparison.isEmpty()) {
            MigrationTestResult(
                success = true,
                message = "配置迁移成功，所有数据已正确迁移",
                details = newData
            )
        } else {
            MigrationTestResult(
                success = false,
                message = "配置迁移存在差异: ${comparison.size} 项不匹配",
                details = comparison
            )
        }
    }
    
    /**
     * 读取旧配置
     */
    private fun readLegacyConfig(prefs: SharedPreferences): Map<String, Any> {
        return mapOf(
            "server_url" to (prefs.getString("server_url", "") ?: ""),
            "username" to (prefs.getString("username", "") ?: ""),
            "has_password" to prefs.contains("password"),
            "auto_backup_enabled" to prefs.getBoolean("auto_backup_enabled", false),
            "last_backup_time" to prefs.getLong("last_backup_time", 0),
            "include_tasks" to prefs.getBoolean("backup_include_tasks", true),
            "include_courses" to prefs.getBoolean("backup_include_courses", true),
            "include_events" to prefs.getBoolean("backup_include_events", true),
            "include_routines" to prefs.getBoolean("backup_include_routines", true),
            "include_subscriptions" to prefs.getBoolean("backup_include_subscriptions", true),
            "include_pomodoro" to prefs.getBoolean("backup_include_pomodoro", true),
            "include_semesters" to prefs.getBoolean("backup_include_semesters", true),
            "include_preferences" to prefs.getBoolean("backup_include_preferences", true)
        )
    }
    
    /**
     * 读取新配置
     */
    private fun readNewConfig(): Map<String, Any> {
        val config = configStorage.getServerConfig()
        val prefs = configStorage.getBackupPreferences()
        
        return mapOf(
            "server_url" to (config?.serverUrl ?: ""),
            "username" to (config?.username ?: ""),
            "has_password" to configStorage.isServerConfigured(),
            "auto_backup_enabled" to configStorage.isAutoBackupEnabled(),
            "last_backup_time" to configStorage.getLastBackupTime(),
            "include_tasks" to prefs.includeTasks,
            "include_courses" to prefs.includeCourses,
            "include_events" to prefs.includeEvents,
            "include_routines" to prefs.includeRoutines,
            "include_subscriptions" to prefs.includeSubscriptions,
            "include_pomodoro" to prefs.includePomodoroSessions,
            "include_semesters" to prefs.includeSemesters,
            "include_preferences" to prefs.includePreferences
        )
    }
    
    /**
     * 比较新旧配置
     * 
     * @return 不匹配的项
     */
    private fun compareConfigs(legacy: Map<String, Any>, new: Map<String, Any>): Map<String, Pair<Any, Any>> {
        val differences = mutableMapOf<String, Pair<Any, Any>>()
        
        for ((key, legacyValue) in legacy) {
            val newValue = new[key]
            
            // 跳过密码检查（只检查是否存在）
            if (key == "has_password") continue
            
            // 确保 newValue 不为 null
            if (newValue != null && legacyValue != newValue) {
                differences[key] = Pair(legacyValue, newValue)
                android.util.Log.w(TAG, "配置不匹配: $key, 旧值=$legacyValue, 新值=$newValue")
            }
        }
        
        return differences
    }
    
    /**
     * 生成迁移报告
     */
    fun generateMigrationReport(): String {
        val result = testMigration()
        
        return buildString {
            appendLine("=" .repeat(60))
            appendLine("WebDAV 配置迁移测试报告")
            appendLine("=" .repeat(60))
            appendLine()
            appendLine("测试结果: ${if (result.success) "✅ 成功" else "❌ 失败"}")
            appendLine("说明: ${result.message}")
            appendLine()
            
            if (result.details.isNotEmpty()) {
                appendLine("详细信息:")
                appendLine("-" .repeat(60))
                result.details.forEach { (key, value) ->
                    when (value) {
                        is Pair<*, *> -> {
                            appendLine("  $key:")
                            appendLine("    旧值: ${value.first}")
                            appendLine("    新值: ${value.second}")
                        }
                        else -> {
                            appendLine("  $key: $value")
                        }
                    }
                }
            }
            
            appendLine()
            appendLine("=" .repeat(60))
            appendLine("配置摘要:")
            appendLine("-" .repeat(60))
            appendLine(configStorage.getConfigSummary())
            appendLine("=" .repeat(60))
        }
    }
}

/**
 * 迁移测试结果
 */
data class MigrationTestResult(
    val success: Boolean,
    val message: String,
    val details: Map<String, Any>
)
