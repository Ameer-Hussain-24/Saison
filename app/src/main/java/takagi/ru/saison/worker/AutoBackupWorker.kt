package takagi.ru.saison.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import takagi.ru.saison.data.repository.backup.WebDavBackupRepository
import takagi.ru.saison.domain.usecase.backup.CreateBackupUseCase
import java.util.concurrent.TimeUnit

@HiltWorker
class AutoBackupWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val backupRepository: WebDavBackupRepository,
    private val createBackupUseCase: CreateBackupUseCase
) : CoroutineWorker(appContext, workerParams) {
    
    override suspend fun doWork(): Result {
        return try {
            // 检查 WebDAV 是否已配置
            if (!backupRepository.isConfigured()) {
                return Result.success()
            }
            
            // 检查自动备份是否启用
            if (!backupRepository.isAutoBackupEnabled()) {
                return Result.success()
            }
            
            // 检查是否需要备份
            if (!backupRepository.shouldAutoBackup()) {
                return Result.success()
            }
            
            // 获取备份偏好设置
            val preferences = backupRepository.getBackupPreferences()
            
            // 创建备份
            val result = createBackupUseCase(preferences)
            
            if (result.isSuccess) {
                Result.success()
            } else {
                Result.retry()
            }
        } catch (e: Exception) {
            Result.retry()
        }
    }
    
    companion object {
        private const val WORK_NAME = "auto_backup_work"
        private const val REPEAT_INTERVAL_HOURS = 12L
        
        fun schedule(context: Context) {
            val workRequest = PeriodicWorkRequestBuilder<AutoBackupWorker>(
                REPEAT_INTERVAL_HOURS,
                TimeUnit.HOURS
            ).build()
            
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        }
        
        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}
