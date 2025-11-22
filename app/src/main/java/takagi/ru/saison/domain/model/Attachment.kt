package takagi.ru.saison.domain.model

data class Attachment(
    val id: Long = 0,
    val taskId: Long,
    val fileName: String,
    val mimeType: String,
    val fileSize: Long,
    val filePath: String,
    val type: AttachmentType = AttachmentType.fromMimeType(mimeType),
    val thumbnailPath: String? = null,
    val isEncrypted: Boolean = true,
    val uploadedToWebDav: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun getFileExtension(): String {
        return fileName.substringAfterLast('.', "")
    }
    
    fun isImage(): Boolean {
        return type == AttachmentType.IMAGE
    }
    
    fun isPdf(): Boolean {
        return mimeType == "application/pdf"
    }
    
    fun isAudio(): Boolean {
        return type == AttachmentType.AUDIO
    }
    
    fun isVideo(): Boolean {
        return type == AttachmentType.VIDEO
    }
    
    fun isDocument(): Boolean {
        return type == AttachmentType.DOCUMENT
    }
}
