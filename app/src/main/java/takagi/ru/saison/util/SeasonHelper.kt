package takagi.ru.saison.util

import takagi.ru.saison.data.local.datastore.SeasonalTheme
import java.util.Calendar

/**
 * 季节辅助工具
 * 根据当前日期自动判断季节并返回对应的主题
 */
object SeasonHelper {
    
    /**
     * 根据当前日期获取对应的季节主题
     * 
     * 季节划分（北半球）：
     * - 春季 (3-5月): SAKURA (樱花)
     * - 夏季 (6-8月): OCEAN (海洋)
     * - 秋季 (9-11月): MAPLE (枫叶)
     * - 冬季 (12-2月): SNOW (雪)
     */
    fun getCurrentSeasonTheme(): SeasonalTheme {
        val calendar = Calendar.getInstance()
        val month = calendar.get(Calendar.MONTH) + 1 // Calendar.MONTH 是 0-based
        
        return when (month) {
            3, 4, 5 -> SeasonalTheme.SAKURA    // 春季：樱花
            6, 7, 8 -> SeasonalTheme.OCEAN     // 夏季：海洋
            9, 10, 11 -> SeasonalTheme.MAPLE   // 秋季：枫叶
            12, 1, 2 -> SeasonalTheme.SNOW     // 冬季：雪
            else -> SeasonalTheme.SAKURA       // 默认春季
        }
    }
    
    /**
     * 获取季节名称（用于显示）
     */
    fun getCurrentSeasonName(): String {
        val calendar = Calendar.getInstance()
        val month = calendar.get(Calendar.MONTH) + 1
        
        return when (month) {
            3, 4, 5 -> "春"
            6, 7, 8 -> "夏"
            9, 10, 11 -> "秋"
            12, 1, 2 -> "冬"
            else -> "春"
        }
    }
}
