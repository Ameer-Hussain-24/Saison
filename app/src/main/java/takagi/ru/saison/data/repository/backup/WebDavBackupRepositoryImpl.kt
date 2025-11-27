package takagi.ru.saison.data.repository.backup

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import takagi.ru.saison.data.remote.webdav.WebDavClient
import takagi.ru.saison.data.remote.webdav.WebDavCredentials
import takagi.ru.saison.domain.model.backup.BackupContent
import takagi.ru.saison.domain.model.backup.BackupFile
import takagi.ru.saison.domain.model.backup.BackupPreferences
import takagi.ru.saison.domain.model.backup.WebDavConfig
import takagi.ru.saison.util.backup.BackupFileManager
import takagi.ru.saison.util.backup.DataExporter
import takagi.ru.saison.util.backup.DataImporter
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebDavBackupRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val webDavClient: WebDavClient,
    private val backupFileManager: BackupFileManager,
    private val dataExporter: DataExporter,
    private val dataImporter: DataImporter
) : WebDavBackupRepository {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    companion object {
        private const val PREFS_NAME = "webdav_backup_config"
        private const val KEY_SERVER_URL = "server_url"
        private const val KEY_USERNAME = "username"
        private const val KEY_PASSWORD = "password"
        private const val KEY_AUTO_BACKUP_ENABLED = "auto_backup_enabled"
        private const val KEY_LAST_BACKUP_TIME = "last_backup_time"
        
        // Backup preferences keys
        private const val KEY_BACKUP_INCLUDE_TASKS = "backup_include_tasks"
        private const val KEY_BACKUP_INCLUDE_COURSES = "backup_include_courses"
        private const val KEY_BACKUP_INCLUDE_EVENTS = "backup_include_events"
        private const val KEY_BACKUP_INCLUDE_ROUTINES = "backup_include_routines"
        private const val KEY_BACKUP_INCLUDE_SUBSCRIPTIONS = "backup_include_subscriptions"
        private const val KEY_BACKUP_INCLUDE_POMODORO = "backup_include_pomodoro"
        private const val KEY_BACKUP_INCLUDE_SEMESTERS = "backup_include_semesters"
        private const val KEY_BACKUP_INCLUDE_PREFERENCES = "backup_include_preferences"
    }
    
    override suspend fun configure(url: String, username: String, password: String) {
        prefs.edit().apply {
            putString(KEY_SERVER_URL, url.trimEnd('/'))
            putString(KEY_USERNAME, username)
            // 只有当密码不为空时才更新密码，这样编辑时可以保持原密码
            if (password.isNotBlank()) {
                putString(KEY_PASSWORD, password)
            }
            apply()
        }
    }
    
    override suspend fun testConnection(): Result<Boolean> {
        return try {
            android.util.Log.d("WebDavBackupRepo", "开始测试连接")
            
            val config = getCurrentConfig() ?: return Result.failure(Exception("未配置 WebDAV"))
            val credentials = WebDavCredentials(config.username, getPassword())
            
            android.util.Log.d("WebDavBackupRepo", "测试基本连接: ${config.serverUrl}")
            val connected = webDavClient.checkConnection(config.serverUrl, credentials)
            if (!connected) {
                android.util.Log.e("WebDavBackupRepo", "基本连接失败")
                return Result.failure(Exception("连接失败，请检查服务器地址和网络"))
            }
            
            android.util.Log.d("WebDavBackupRepo", "基本连接成功，创建备份目录")
            // 尝试创建备份目录
            val backupDir = "${config.serverUrl}/saison_backups"
            val dirCreated = webDavClient.createDirectory(backupDir, credentials)
            if (!dirCreated) {
                android.util.Log.e("WebDavBackupRepo", "创建备份目录失败")
                return Result.failure(Exception("无法创建备份目录"))
            }
            
            android.util.Log.d("WebDavBackupRepo", "测试写权限")
            // 测试写权限
            val writeTest = webDavClient.testWritePermission(backupDir, credentials)
            writeTest.fold(
                onSuccess = {
                    android.util.Log.d("WebDavBackupRepo", "连接测试成功")
                    Result.success(true)
                },
                onFailure = { error ->
                    android.util.Log.e("WebDavBackupRepo", "写权限测试失败", error)
                    Result.failure(Exception("没有写入权限: ${error.message}"))
                }
            )
        } catch (e: Exception) {
            android.util.Log.e("WebDavBackupRepo", "连接测试异常", e)
            Result.failure(e)
        }
    }
    
    override suspend fun isConfigured(): Boolean {
        val url = prefs.getString(KEY_SERVER_URL, null)
        val username = prefs.getString(KEY_USERNAME, null)
        val password = prefs.getString(KEY_PASSWORD, null)
        return !url.isNullOrEmpty() && !username.isNullOrEmpty() && !password.isNullOrEmpty()
    }
    
    override suspend fun getCurrentConfig(): WebDavConfig? {
        val url = prefs.getString(KEY_SERVER_URL, null)
        val username = prefs.getString(KEY_USERNAME, null)
        return if (!url.isNullOrEmpty() && !username.isNullOrEmpty()) {
            WebDavConfig(url, username)
        } else {
            null
        }
    }
    
    override suspend fun clearConfig() {
        prefs.edit().clear().apply()
    }
    
    private fun getPassword(): String {
        return prefs.getString(KEY_PASSWORD, "") ?: ""
    }
    
    override suspend fun configureAutoBackup(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_BACKUP_ENABLED, enabled).apply()
    }
    
    override suspend fun isAutoBackupEnabled(): Boolean {
        return prefs.getBoolean(KEY_AUTO_BACKUP_ENABLED, false)
    }
    
    override suspend fun shouldAutoBackup(): Boolean {
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
    
    override suspend fun updateLastBackupTime() {
        prefs.edit().putLong(KEY_LAST_BACKUP_TIME, System.currentTimeMillis()).apply()
    }
    
    override suspend fun getLastBackupTime(): Long {
        return prefs.getLong(KEY_LAST_BACKUP_TIME, 0)
    }
    
    override suspend fun saveBackupPreferences(preferences: BackupPreferences) {
        prefs.edit().apply {
            putBoolean(KEY_BACKUP_INCLUDE_TASKS, preferences.includeTasks)
            putBoolean(KEY_BACKUP_INCLUDE_COURSES, preferences.includeCourses)
            putBoolean(KEY_BACKUP_INCLUDE_EVENTS, preferences.includeEvents)
            putBoolean(KEY_BACKUP_INCLUDE_ROUTINES, preferences.includeRoutines)
            putBoolean(KEY_BACKUP_INCLUDE_SUBSCRIPTIONS, preferences.includeSubscriptions)
            putBoolean(KEY_BACKUP_INCLUDE_POMODORO, preferences.includePomodoroSessions)
            putBoolean(KEY_BACKUP_INCLUDE_SEMESTERS, preferences.includeSemesters)
            putBoolean(KEY_BACKUP_INCLUDE_PREFERENCES, preferences.includePreferences)
            apply()
        }
    }
    
    override suspend fun getBackupPreferences(): BackupPreferences {
        return BackupPreferences(
            includeTasks = prefs.getBoolean(KEY_BACKUP_INCLUDE_TASKS, true),
            includeCourses = prefs.getBoolean(KEY_BACKUP_INCLUDE_COURSES, true),
            includeEvents = prefs.getBoolean(KEY_BACKUP_INCLUDE_EVENTS, true),
            includeRoutines = prefs.getBoolean(KEY_BACKUP_INCLUDE_ROUTINES, true),
            includeSubscriptions = prefs.getBoolean(KEY_BACKUP_INCLUDE_SUBSCRIPTIONS, true),
            includePomodoroSessions = prefs.getBoolean(KEY_BACKUP_INCLUDE_POMODORO, true),
            includeSemesters = prefs.getBoolean(KEY_BACKUP_INCLUDE_SEMESTERS, true),
            includePreferences = prefs.getBoolean(KEY_BACKUP_INCLUDE_PREFERENCES, true)
        )
    }
    
    override suspend fun createAndUploadBackup(preferences: BackupPreferences, dataFiles: Map<String, String>): Result<String> {
        return try {
            android.util.Log.d("WebDavBackupRepo", "开始创建并上传备份")
            
            if (!preferences.hasAnyEnabled()) {
                android.util.Log.e("WebDavBackupRepo", "没有启用任何备份类型")
                return Result.failure(Exception("请至少选择一种备份内容"))
            }
            
            val config = getCurrentConfig()
            if (config == null) {
                android.util.Log.e("WebDavBackupRepo", "WebDAV 未配置")
                return Result.failure(Exception("未配置 WebDAV"))
            }
            android.util.Log.d("WebDavBackupRepo", "配置: ${config.serverUrl}, ${config.username}")
            
            val credentials = WebDavCredentials(config.username, getPassword())
            
            // 生成备份文件名
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "saison_backup_$timestamp.zip"
            android.util.Log.d("WebDavBackupRepo", "备份文件名: $fileName")
            
            // 创建 ZIP 文件
            val zipFile = File(context.cacheDir, fileName)
            android.util.Log.d("WebDavBackupRepo", "创建 ZIP 文件: ${zipFile.absolutePath}")
            android.util.Log.d("WebDavBackupRepo", "数据文件数量: ${dataFiles.size}")
            
            backupFileManager.createZipArchive(dataFiles, zipFile)
            android.util.Log.d("WebDavBackupRepo", "ZIP 文件创建完成，大小: ${zipFile.length()} bytes")
            
            // 确保备份目录存在
            val backupDir = "${config.serverUrl}/saison_backups"
            android.util.Log.d("WebDavBackupRepo", "确保备份目录存在: $backupDir")
            val dirCreated = webDavClient.createDirectory(backupDir, credentials)
            if (!dirCreated) {
                android.util.Log.w("WebDavBackupRepo", "创建目录失败或目录已存在")
            }
            
            // 上传到 WebDAV
            val uploadUrl = "$backupDir/$fileName"
            android.util.Log.d("WebDavBackupRepo", "上传 URL: $uploadUrl")
            
            val uploadResult = webDavClient.uploadFile(uploadUrl, credentials, zipFile)
            
            // 清理临时文件
            zipFile.delete()
            
            uploadResult.fold(
                onSuccess = {
                    updateLastBackupTime()
                    android.util.Log.d("WebDavBackupRepo", "备份成功")
                    Result.success(fileName)
                },
                onFailure = { error ->
                    android.util.Log.e("WebDavBackupRepo", "上传失败", error)
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            android.util.Log.e("WebDavBackupRepo", "备份异常", e)
            Result.failure(e)
        }
    }
    
    override suspend fun listBackups(): Result<List<BackupFile>> {
        return try {
            val config = getCurrentConfig() ?: return Result.failure(Exception("未配置 WebDAV"))
            val credentials = WebDavCredentials(config.username, getPassword())
            
            val backupDir = "${config.serverUrl}/saison_backups"
            val files = webDavClient.listFiles(backupDir, credentials)
            
            val backupFiles = files.map { fileInfo ->
                BackupFile(
                    name = fileInfo.name,
                    size = fileInfo.size,
                    modified = Date(fileInfo.modified),
                    url = fileInfo.url
                )
            }.sortedByDescending { it.modified }
            
            Result.success(backupFiles)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun downloadAndRestoreBackup(backupFile: BackupFile): Result<BackupContent> {
        return try {
            val config = getCurrentConfig() ?: return Result.failure(Exception("未配置 WebDAV"))
            val credentials = WebDavCredentials(config.username, getPassword())
            
            // 下载备份文件
            val downloadedFile = File(context.cacheDir, "restore_${backupFile.name}")
            val downloaded = webDavClient.downloadFile(backupFile.url, credentials, downloadedFile)
            
            if (!downloaded) {
                return Result.failure(Exception("下载失败"))
            }
            
            // 解压备份文件
            val extractedFiles = backupFileManager.extractZipArchive(downloadedFile)
            
            // 导入数据
            val tasks = extractedFiles["tasks.json"]?.let { dataImporter.importTasks(it) } ?: emptyList()
            val courses = extractedFiles["courses.json"]?.let { dataImporter.importCourses(it) } ?: emptyList()
            val events = extractedFiles["events.json"]?.let { dataImporter.importEvents(it) } ?: emptyList()
            val routines = extractedFiles["routines.json"]?.let { dataImporter.importRoutines(it) } ?: emptyList()
            val subscriptions = extractedFiles["subscriptions.json"]?.let { dataImporter.importSubscriptions(it) } ?: emptyList()
            val pomodoroSessions = extractedFiles["pomodoro_sessions.json"]?.let { dataImporter.importPomodoroSessions(it) } ?: emptyList()
            val semesters = extractedFiles["semesters.json"]?.let { dataImporter.importSemesters(it) } ?: emptyList()
            val preferences = extractedFiles["preferences.json"]?.let { dataImporter.importPreferences(it) } ?: emptyMap()
            
            // 清理临时文件
            downloadedFile.delete()
            
            val content = BackupContent(
                tasks = tasks,
                courses = courses,
                events = events,
                routines = routines,
                subscriptions = subscriptions,
                pomodoroSessions = pomodoroSessions,
                semesters = semesters,
                preferences = preferences
            )
            
            Result.success(content)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteBackup(backupFile: BackupFile): Result<Unit> {
        return try {
            val config = getCurrentConfig() ?: return Result.failure(Exception("未配置 WebDAV"))
            val credentials = WebDavCredentials(config.username, getPassword())
            
            val deleted = webDavClient.deleteFile(backupFile.url, credentials)
            if (deleted) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("删除失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
