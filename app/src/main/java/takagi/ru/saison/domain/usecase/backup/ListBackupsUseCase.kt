package takagi.ru.saison.domain.usecase.backup

import takagi.ru.saison.data.repository.backup.WebDavBackupRepository
import takagi.ru.saison.domain.model.backup.BackupFile
import javax.inject.Inject

class ListBackupsUseCase @Inject constructor(
    private val backupRepository: WebDavBackupRepository
) {
    
    suspend operator fun invoke(): Result<List<BackupFile>> {
        return try {
            backupRepository.listBackups()
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
