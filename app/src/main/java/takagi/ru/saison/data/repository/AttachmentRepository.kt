package takagi.ru.saison.data.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import takagi.ru.saison.data.local.database.dao.AttachmentDao
import takagi.ru.saison.data.local.encryption.EncryptionManager
import takagi.ru.saison.domain.mapper.toDomain
import takagi.ru.saison.domain.mapper.toEntity
import takagi.ru.saison.domain.model.Attachment
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AttachmentRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val attachmentDao: AttachmentDao,
    private val encryptionManager: EncryptionManager
) {
    
    private val attachmentsDir: File by lazy {
        File(context.filesDir, "attachments").apply {
            if (!exists()) mkdirs()
        }
    }
    
    fun getAttachmentsByTask(taskId: Long): Flow<List<Attachment>> {
        return attachmentDao.getAttachmentsByTask(taskId).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    suspend fun getAttachmentById(attachmentId: Long): Attachment? {
        return attachmentDao.getAttachmentById(attachmentId)?.toDomain()
    }
    
    suspend fun insertAttachment(
        taskId: Long,
        fileName: String,
        mimeType: String,
        sourceFile: File
    ): Long {
        // Generate unique file name
        val uniqueFileName = "${System.currentTimeMillis()}_$fileName"
        val encryptedFile = File(attachmentsDir, "$uniqueFileName.enc")
        
        // Encrypt and save file
        encryptionManager.encryptFile(sourceFile, encryptedFile)
        
        // Create attachment entity
        val attachment = Attachment(
            taskId = taskId,
            fileName = fileName,
            mimeType = mimeType,
            fileSize = sourceFile.length(),
            filePath = encryptedFile.absolutePath,
            thumbnailPath = null,
            isEncrypted = true,
            uploadedToWebDav = false
        )
        
        return attachmentDao.insert(attachment.toEntity())
    }
    
    suspend fun deleteAttachment(attachmentId: Long) {
        val attachment = getAttachmentById(attachmentId)
        if (attachment != null) {
            // Delete encrypted file
            val file = File(attachment.filePath)
            if (file.exists()) {
                file.delete()
            }
            
            // Delete thumbnail if exists
            attachment.thumbnailPath?.let { thumbnailPath ->
                val thumbnail = File(thumbnailPath)
                if (thumbnail.exists()) {
                    thumbnail.delete()
                }
            }
            
            // Delete from database
            attachmentDao.deleteById(attachmentId)
        }
    }
    
    suspend fun getDecryptedFile(attachmentId: Long): File? {
        val attachment = getAttachmentById(attachmentId) ?: return null
        
        val encryptedFile = File(attachment.filePath)
        if (!encryptedFile.exists()) return null
        
        // Create temporary decrypted file
        val tempFile = File(context.cacheDir, attachment.fileName)
        encryptionManager.decryptFile(encryptedFile, tempFile)
        
        return tempFile
    }
    
    suspend fun markAsUploaded(attachmentId: Long) {
        attachmentDao.markAsUploaded(attachmentId)
    }
    
    suspend fun getPendingUploads(): List<Attachment> {
        return attachmentDao.getPendingUploads().map { it.toDomain() }
    }
}
