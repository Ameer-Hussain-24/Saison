package takagi.ru.saison.domain.mapper

import takagi.ru.saison.data.local.database.entities.AttachmentEntity
import takagi.ru.saison.domain.model.Attachment

fun AttachmentEntity.toDomain(): Attachment {
    return Attachment(
        id = id,
        taskId = taskId,
        fileName = fileName,
        mimeType = mimeType,
        fileSize = fileSize,
        filePath = filePath,
        thumbnailPath = thumbnailPath,
        isEncrypted = isEncrypted,
        uploadedToWebDav = uploadedToWebDav,
        createdAt = createdAt
    )
}

fun Attachment.toEntity(): AttachmentEntity {
    return AttachmentEntity(
        id = id,
        taskId = taskId,
        fileName = fileName,
        mimeType = mimeType,
        fileSize = fileSize,
        filePath = filePath,
        thumbnailPath = thumbnailPath,
        isEncrypted = isEncrypted,
        uploadedToWebDav = uploadedToWebDav,
        createdAt = createdAt
    )
}
