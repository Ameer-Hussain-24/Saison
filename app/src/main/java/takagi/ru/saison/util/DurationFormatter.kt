package takagi.ru.saison.util

/**
 * 时长格式化工具类
 * 用于处理时长的格式化、转换等操作
 */
object DurationFormatter {
    
    /**
     * 将分钟数格式化为易读字符串
     * 
     * @param minutes 总分钟数
     * @return 格式化后的字符串，例如 "1小时30分钟"、"45分钟"
     */
    fun formatDuration(minutes: Int): String {
        if (minutes <= 0) return "0分钟"
        
        val hours = minutes / 60
        val remainingMinutes = minutes % 60
        
        return when {
            hours > 0 && remainingMinutes > 0 -> "${hours}小时${remainingMinutes}分钟"
            hours > 0 -> "${hours}小时"
            else -> "${remainingMinutes}分钟"
        }
    }
    
    /**
     * 将小时和分钟转换为总分钟数
     * 
     * @param hours 小时数
     * @param minutes 分钟数
     * @return 总分钟数
     */
    fun toMinutes(hours: Int, minutes: Int): Int {
        return hours * 60 + minutes
    }
    
    /**
     * 将总分钟数转换为小时和分钟
     * 
     * @param totalMinutes 总分钟数
     * @return Pair<小时数, 分钟数>
     */
    fun fromMinutes(totalMinutes: Int): Pair<Int, Int> {
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return Pair(hours, minutes)
    }
}
