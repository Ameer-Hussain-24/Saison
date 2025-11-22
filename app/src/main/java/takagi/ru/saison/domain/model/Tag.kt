package takagi.ru.saison.domain.model

data class Tag(
    val id: Long = 0,
    val name: String,
    val path: String,
    val parentId: Long? = null,
    val icon: String? = null,
    val color: Int,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun getPathComponents(): List<String> {
        return path.split("/")
    }
    
    fun getDepth(): Int {
        return getPathComponents().size
    }
    
    fun isChildOf(other: Tag): Boolean {
        return path.startsWith(other.path + "/")
    }
}
