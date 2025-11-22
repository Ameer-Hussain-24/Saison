package takagi.ru.saison.util

/**
 * 临时文件内容缓存
 * 用于在导航过程中传递大文件内容
 */
object TempFileCache {
    private var cachedContent: String? = null
    
    fun store(content: String) {
        cachedContent = content
    }
    
    fun retrieve(): String? {
        val content = cachedContent
        cachedContent = null // 读取后清除
        return content
    }
    
    fun clear() {
        cachedContent = null
    }
}
