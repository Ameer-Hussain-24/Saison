package takagi.ru.saison.data.repository

import takagi.ru.saison.data.local.database.dao.TaskDao
import takagi.ru.saison.data.local.datastore.PreferencesManager
import takagi.ru.saison.data.remote.webdav.ConflictResolver
import takagi.ru.saison.data.remote.webdav.ConflictResolutionStrategy
import takagi.ru.saison.data.remote.webdav.WebDavClient
import takagi.ru.saison.data.remote.webdav.WebDavCredentials
import takagi.ru.saison.domain.mapper.toDomain
import takagi.ru.saison.domain.mapper.toEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncRepository @Inject constructor(
    private val webDavClient: WebDavClient,
    private val taskDao: TaskDao,
    private val preferencesManager: PreferencesManager,
    private val conflictResolver: ConflictResolver
) {
    
    suspend fun syncTasks(): SyncResult {
        val config = preferencesManager.getWebDavConfig() ?: return SyncResult.NotConfigured
        val credentials = WebDavCredentials(config.username, config.password)
        val tasksUrl = "${config.url}/tasks.json"
        
        return try {
            // 1. Check connection
            if (!webDavClient.checkConnection(config.url, credentials)) {
                return SyncResult.Error("Cannot connect to WebDAV server")
            }
            
            // 2. Get remote ETag
            val remoteETag = webDavClient.getETag(tasksUrl, credentials)
            val localETag = preferencesManager.getLastSyncETag()
            
            if (remoteETag == localETag && remoteETag != null) {
                return SyncResult.NoChanges
            }
            
            // 3. Download remote tasks
            val remoteTasks = try {
                webDavClient.downloadTasks(tasksUrl, credentials)
            } catch (e: Exception) {
                // File might not exist yet, create it
                emptyList()
            }
            
            // 4. Get local tasks
            val localTaskEntities = taskDao.getAllTasksList()
            val localTasks = localTaskEntities.map { it.toDomain() }
            
            // 5. Detect and resolve conflicts
            val conflicts = conflictResolver.detectConflicts(localTasks, remoteTasks)
            val resolved = conflictResolver.resolveConflicts(
                conflicts,
                ConflictResolutionStrategy.LATEST_WINS
            )
            
            // 6. Merge tasks
            val mergedTasks = conflictResolver.mergeTasks(localTasks, remoteTasks, resolved)
            
            // 7. Update local database
            val mergedEntities = mergedTasks.map { task ->
                task.toEntity(task.category?.id ?: 1L)
            }
            taskDao.insertAll(mergedEntities)
            
            // 8. Upload merged tasks to server
            webDavClient.uploadTasks(tasksUrl, credentials, mergedTasks)
            
            // 9. Update local ETag
            val newETag = webDavClient.getETag(tasksUrl, credentials)
            if (newETag != null) {
                preferencesManager.setLastSyncETag(newETag)
            }
            
            SyncResult.Success(
                syncedCount = mergedTasks.size,
                conflictsResolved = conflicts.size
            )
        } catch (e: Exception) {
            SyncResult.Error(e.message ?: "Unknown sync error")
        }
    }
    
    suspend fun testConnection(url: String, username: String, password: String): Boolean {
        val credentials = WebDavCredentials(username, password)
        return webDavClient.checkConnection(url, credentials)
    }
    
    suspend fun markForSync(entityType: SyncEntity, entityId: Long) {
        // Mark entity as needing sync
        when (entityType) {
            SyncEntity.TASK -> taskDao.updateSyncStatus(entityId, 1) // 1 = Pending
        }
    }
    
    suspend fun markForDeletion(entityType: SyncEntity, entityId: Long) {
        // For now, just delete locally
        // In a full implementation, we'd mark for deletion and sync that
        when (entityType) {
            SyncEntity.TASK -> taskDao.deleteById(entityId)
        }
    }
}

sealed class SyncResult {
    data class Success(val syncedCount: Int, val conflictsResolved: Int = 0) : SyncResult()
    object NoChanges : SyncResult()
    object NotConfigured : SyncResult()
    data class Error(val message: String) : SyncResult()
}

enum class SyncEntity {
    TASK
}
