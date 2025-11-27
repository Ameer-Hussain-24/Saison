package takagi.ru.saison.data.repository.backup

import takagi.ru.saison.domain.model.backup.BackupContent
import takagi.ru.saison.domain.model.backup.BackupFile
import takagi.ru.saison.domain.model.backup.BackupPreferences
import takagi.ru.saison.domain.model.backup.WebDavConfig

interface WebDavBackupRepository {
    suspend fun configure(url: String, username: String, password: String)
    suspend fun testConnection(): Result<Boolean>
    suspend fun isConfigured(): Boolean
    suspend fun getCurrentConfig(): WebDavConfig?
    suspend fun clearConfig()
    
    suspend fun createAndUploadBackup(preferences: BackupPreferences, dataFiles: Map<String, String>): Result<String>
    suspend fun listBackups(): Result<List<BackupFile>>
    suspend fun downloadAndRestoreBackup(backupFile: BackupFile): Result<BackupContent>
    suspend fun deleteBackup(backupFile: BackupFile): Result<Unit>
    
    suspend fun configureAutoBackup(enabled: Boolean)
    suspend fun isAutoBackupEnabled(): Boolean
    suspend fun shouldAutoBackup(): Boolean
    suspend fun updateLastBackupTime()
    suspend fun getLastBackupTime(): Long
    
    suspend fun saveBackupPreferences(preferences: BackupPreferences)
    suspend fun getBackupPreferences(): BackupPreferences
}
