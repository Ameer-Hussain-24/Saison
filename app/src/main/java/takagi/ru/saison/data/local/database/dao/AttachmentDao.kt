package takagi.ru.saison.data.local.database.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import takagi.ru.saison.data.local.database.entities.AttachmentEntity

@Dao
interface AttachmentDao {
    
    @Query("SELECT * FROM attachments WHERE taskId = :taskId ORDER BY createdAt DESC")
    fun getAttachmentsByTask(taskId: Long): Flow<List<AttachmentEntity>>
    
    @Query("SELECT * FROM attachments WHERE id = :attachmentId")
    suspend fun getAttachmentById(attachmentId: Long): AttachmentEntity?
    
    @Query("SELECT * FROM attachments WHERE uploadedToWebDav = 0")
    suspend fun getPendingUploads(): List<AttachmentEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(attachment: AttachmentEntity): Long
    
    @Update
    suspend fun update(attachment: AttachmentEntity)
    
    @Delete
    suspend fun delete(attachment: AttachmentEntity)
    
    @Query("DELETE FROM attachments WHERE id = :attachmentId")
    suspend fun deleteById(attachmentId: Long)
    
    @Query("UPDATE attachments SET uploadedToWebDav = 1 WHERE id = :attachmentId")
    suspend fun markAsUploaded(attachmentId: Long)
}
