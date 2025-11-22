package takagi.ru.saison.domain.model

enum class AttachmentType {
    IMAGE,
    DOCUMENT,
    AUDIO,
    VIDEO,
    OTHER;
    
    companion object {
        fun fromMimeType(mimeType: String): AttachmentType {
            return when {
                mimeType.startsWith("image/") -> IMAGE
                mimeType.startsWith("audio/") -> AUDIO
                mimeType.startsWith("video/") -> VIDEO
                mimeType == "application/pdf" || 
                mimeType.startsWith("application/vnd.") ||
                mimeType.startsWith("application/msword") ||
                mimeType.startsWith("text/") -> DOCUMENT
                else -> OTHER
            }
        }
    }
}
