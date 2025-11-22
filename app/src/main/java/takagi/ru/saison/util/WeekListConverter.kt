package takagi.ru.saison.util

/**
 * 周数列表转换工具
 * 用于在 List<Int> 和 JSON 字符串之间转换
 */
object WeekListConverter {
    
    /**
     * 将周数列表转换为 JSON 字符串
     * 格式: "[1,3,5,7]"
     */
    fun toJson(weeks: List<Int>?): String? {
        return weeks?.let { list ->
            "[${list.joinToString(",")}]"
        }
    }
    
    /**
     * 将 JSON 字符串转换为周数列表
     * 格式: "[1,3,5,7]"
     */
    fun fromJson(json: String?): List<Int>? {
        return json?.let {
            try {
                // 移除方括号并分割
                val cleaned = it.trim().removeSurrounding("[", "]")
                if (cleaned.isEmpty()) {
                    emptyList()
                } else {
                    cleaned.split(",")
                        .map { num -> num.trim().toInt() }
                }
            } catch (e: Exception) {
                null
            }
        }
    }
}
