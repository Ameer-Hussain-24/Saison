package takagi.ru.saison.data.local.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "attachments",
    foreignKeys = [
        ForeignKey(
            entity = TaskEntity::class,
            parentColumns = ["id"],
            childColumns = ["taskId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("taskId")]
)
data class AttachmentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val taskId: Long,
    val fileName: String,
    val mimeType: String,
    val fileSize: Long,
    
    val filePath: String, // Encrypted file path
    val thumbnailPath: String? = null,
    
    val isEncrypted: Boolean = true,
    val uploadedToWebDav: Boolean = false,
    
    val createdAt: Long = System.currentTimeMillis()
)
