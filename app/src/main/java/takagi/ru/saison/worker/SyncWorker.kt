package takagi.ru.saison.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import takagi.ru.saison.data.repository.SyncRepository
import takagi.ru.saison.data.repository.SyncResult

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val syncRepository: SyncRepository
) : CoroutineWorker(appContext, workerParams) {
    
    override suspend fun doWork(): Result {
        return try {
            when (val result = syncRepository.syncTasks()) {
                is SyncResult.Success -> {
                    Result.success()
                }
                is SyncResult.NoChanges -> {
                    Result.success()
                }
                is SyncResult.NotConfigured -> {
                    // Not configured, don't retry
                    Result.success()
                }
                is SyncResult.Error -> {
                    // Retry on error
                    if (runAttemptCount < 3) {
                        Result.retry()
                    } else {
                        Result.failure()
                    }
                }
            }
        } catch (e: Exception) {
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }
    
    companion object {
        const val WORK_NAME = "sync_worker"
    }
}
