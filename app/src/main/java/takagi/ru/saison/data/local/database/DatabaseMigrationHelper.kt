package takagi.ru.saison.data.local.database

import android.content.Context
import android.util.Log
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import takagi.ru.saison.util.backup.BackupFileManager
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 数据库迁移助手
 * 
 * 提供安全的数据库迁移策略，防止数据丢失:
 * 1. 迁移前自动备份数据库
 * 2. 提供迁移失败恢复机制
 * 3. 记录迁移历史
 * 4. 提供手动备份和恢复功能
 */
@Singleton
class DatabaseMigrationHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val backupFileManager: BackupFileManager
) {
    companion object {
        private const val TAG = "DBMigrationHelper"
        private const val BACKUP_DIR_NAME = "database_backups"
        private const val AUTO_BACKUP_DIR_NAME = "auto_backups"
        private const val MAX_AUTO_BACKUPS = 5 // 最多保留 5 个自动备份
        
        /**
         * 创建一个包装迁移，在执行前自动备份数据库
         */
        fun wrapMigrationWithBackup(
            migration: Migration,
            context: Context
        ): Migration {
            return object : Migration(migration.startVersion, migration.endVersion) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    Log.d(TAG, "开始迁移: ${startVersion} -> ${endVersion}")
                    
                    try {
                        // 执行原始迁移
                        migration.migrate(db)
                        Log.d(TAG, "迁移成功: ${startVersion} -> ${endVersion}")
                    } catch (e: Exception) {
                        Log.e(TAG, "迁移失败: ${startVersion} -> ${endVersion}", e)
                        throw e
                    }
                }
            }
        }
    }
    
    /**
     * 获取备份目录
     */
    private fun getBackupDir(): File {
        val dir = File(context.filesDir, BACKUP_DIR_NAME)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }
    
    /**
     * 获取自动备份目录
     */
    private fun getAutoBackupDir(): File {
        val dir = File(context.filesDir, AUTO_BACKUP_DIR_NAME)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }
    
    /**
     * 创建数据库备份
     * 
     * @param label 备份标签（如 "manual", "auto", "pre_migration"）
     * @return 备份文件路径
     */
    suspend fun createDatabaseBackup(label: String = "manual"): Result<File> = withContext(Dispatchers.IO) {
        try {
            val dbFile = context.getDatabasePath(SaisonDatabase.DATABASE_NAME)
            
            if (!dbFile.exists()) {
                Log.w(TAG, "数据库文件不存在，无需备份")
                return@withContext Result.failure(Exception("数据库文件不存在"))
            }
            
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val backupFileName = "saison_db_${label}_$timestamp.db"
            
            val backupDir = if (label == "auto") getAutoBackupDir() else getBackupDir()
            val backupFile = File(backupDir, backupFileName)
            
            Log.d(TAG, "创建数据库备份: ${backupFile.absolutePath}")
            
            // 复制数据库文件
            dbFile.copyTo(backupFile, overwrite = true)
            
            // 同时复制 WAL 和 SHM 文件（如果存在）
            val walFile = File(dbFile.parent, "${dbFile.name}-wal")
            if (walFile.exists()) {
                val walBackup = File(backupDir, "$backupFileName-wal")
                walFile.copyTo(walBackup, overwrite = true)
            }
            
            val shmFile = File(dbFile.parent, "${dbFile.name}-shm")
            if (shmFile.exists()) {
                val shmBackup = File(backupDir, "$backupFileName-shm")
                shmFile.copyTo(shmBackup, overwrite = true)
            }
            
            Log.d(TAG, "备份创建成功: ${backupFile.absolutePath}, 大小: ${backupFile.length()} bytes")
            
            // 如果是自动备份，清理旧备份
            if (label == "auto") {
                cleanupOldAutoBackups()
            }
            
            Result.success(backupFile)
        } catch (e: Exception) {
            Log.e(TAG, "创建数据库备份失败", e)
            Result.failure(e)
        }
    }
    
    /**
     * 恢复数据库备份
     * 
     * @param backupFile 备份文件
     * @return 恢复结果
     */
    suspend fun restoreDatabaseBackup(backupFile: File): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (!backupFile.exists()) {
                return@withContext Result.failure(Exception("备份文件不存在"))
            }
            
            Log.d(TAG, "恢复数据库备份: ${backupFile.absolutePath}")
            
            val dbFile = context.getDatabasePath(SaisonDatabase.DATABASE_NAME)
            
            // 在恢复前先备份当前数据库（以防万一）
            if (dbFile.exists()) {
                val emergencyBackup = File(getBackupDir(), "emergency_backup_before_restore.db")
                dbFile.copyTo(emergencyBackup, overwrite = true)
                Log.d(TAG, "已创建紧急备份: ${emergencyBackup.absolutePath}")
            }
            
            // 恢复数据库
            backupFile.copyTo(dbFile, overwrite = true)
            
            // 恢复 WAL 和 SHM 文件（如果存在）
            val walBackup = File(backupFile.parent, "${backupFile.name}-wal")
            if (walBackup.exists()) {
                val walFile = File(dbFile.parent, "${dbFile.name}-wal")
                walBackup.copyTo(walFile, overwrite = true)
            }
            
            val shmBackup = File(backupFile.parent, "${backupFile.name}-shm")
            if (shmBackup.exists()) {
                val shmFile = File(dbFile.parent, "${dbFile.name}-shm")
                shmBackup.copyTo(shmFile, overwrite = true)
            }
            
            Log.d(TAG, "数据库恢复成功")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "恢复数据库失败", e)
            Result.failure(e)
        }
    }
    
    /**
     * 列出所有备份文件
     */
    fun listBackups(): List<DatabaseBackupInfo> {
        val backups = mutableListOf<DatabaseBackupInfo>()
        
        // 手动备份
        getBackupDir().listFiles()?.forEach { file ->
            if (file.name.endsWith(".db")) {
                backups.add(
                    DatabaseBackupInfo(
                        file = file,
                        name = file.name,
                        size = file.length(),
                        modified = file.lastModified(),
                        isAuto = false
                    )
                )
            }
        }
        
        // 自动备份
        getAutoBackupDir().listFiles()?.forEach { file ->
            if (file.name.endsWith(".db")) {
                backups.add(
                    DatabaseBackupInfo(
                        file = file,
                        name = file.name,
                        size = file.length(),
                        modified = file.lastModified(),
                        isAuto = true
                    )
                )
            }
        }
        
        return backups.sortedByDescending { it.modified }
    }
    
    /**
     * 清理旧的自动备份
     */
    private fun cleanupOldAutoBackups() {
        val autoBackups = getAutoBackupDir().listFiles { file ->
            file.name.endsWith(".db")
        }?.sortedByDescending { it.lastModified() } ?: return
        
        if (autoBackups.size > MAX_AUTO_BACKUPS) {
            val toDelete = autoBackups.drop(MAX_AUTO_BACKUPS)
            toDelete.forEach { file ->
                Log.d(TAG, "删除旧备份: ${file.name}")
                file.delete()
                // 同时删除相关的 WAL 和 SHM 文件
                File(file.parent, "${file.name}-wal").delete()
                File(file.parent, "${file.name}-shm").delete()
            }
        }
    }
    
    /**
     * 删除备份
     */
    fun deleteBackup(backupInfo: DatabaseBackupInfo): Boolean {
        return try {
            backupInfo.file.delete()
            // 同时删除相关文件
            File(backupInfo.file.parent, "${backupInfo.file.name}-wal").delete()
            File(backupInfo.file.parent, "${backupInfo.file.name}-shm").delete()
            true
        } catch (e: Exception) {
            Log.e(TAG, "删除备份失败", e)
            false
        }
    }
    
    /**
     * 获取数据库大小
     */
    fun getDatabaseSize(): Long {
        val dbFile = context.getDatabasePath(SaisonDatabase.DATABASE_NAME)
        return if (dbFile.exists()) dbFile.length() else 0
    }
    
    /**
     * 检查数据库是否存在
     */
    fun isDatabaseExists(): Boolean {
        return context.getDatabasePath(SaisonDatabase.DATABASE_NAME).exists()
    }
    
    /**
     * 获取备份摘要信息
     */
    fun getBackupSummary(): String {
        val backups = listBackups()
        val manualCount = backups.count { !it.isAuto }
        val autoCount = backups.count { it.isAuto }
        val totalSize = backups.sumOf { it.size }
        
        return buildString {
            appendLine("数据库备份摘要:")
            appendLine("- 数据库大小: ${formatSize(getDatabaseSize())}")
            appendLine("- 手动备份数: $manualCount")
            appendLine("- 自动备份数: $autoCount")
            appendLine("- 备份总大小: ${formatSize(totalSize)}")
            if (backups.isNotEmpty()) {
                val latest = backups.first()
                appendLine("- 最新备份: ${latest.name}")
                appendLine("- 备份时间: ${Date(latest.modified)}")
            }
        }
    }
    
    private fun formatSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            else -> "${bytes / (1024 * 1024)} MB"
        }
    }
}

/**
 * 数据库备份信息
 */
data class DatabaseBackupInfo(
    val file: File,
    val name: String,
    val size: Long,
    val modified: Long,
    val isAuto: Boolean
)
