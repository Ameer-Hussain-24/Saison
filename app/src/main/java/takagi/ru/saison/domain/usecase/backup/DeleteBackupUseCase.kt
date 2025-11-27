package takagi.ru.saison.domain.usecase.backup

import takagi.ru.saison.data.repository.backup.WebDavBackupRepository
import takagi.ru.saison.domain.model.backup.BackupFile
import javax.inject.Inject

class DeleteBackupUseCase @Inject constructor(
    private val backupRepository: WebDavBackupRepository
) {
    
    suspend operator fun invoke(backupFile: BackupFile): Result<Unit> {
        return try {
            backupRepository.deleteBackup(backupFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
