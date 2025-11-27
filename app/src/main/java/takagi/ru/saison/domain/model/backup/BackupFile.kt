package takagi.ru.saison.domain.model.backup

import java.util.Date

data class BackupFile(
    val name: String,
    val size: Long,
    val modified: Date,
    val url: String
)
