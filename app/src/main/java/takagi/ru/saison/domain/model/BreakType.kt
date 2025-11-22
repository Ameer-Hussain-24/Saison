package takagi.ru.saison.domain.model

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color

/**
 * 休息类型枚举
 * 定义课程表中的休息时段类型
 */
enum class BreakType {
    LUNCH,    // 午休
    DINNER;   // 晚休
    
    /**
     * 获取显示名称
     */
    fun getDisplayName(): String {
        return when (this) {
            LUNCH -> "午休"
            DINNER -> "晚休"
        }
    }
    
    /**
     * 获取主题颜色
     */
    fun getColor(colorScheme: ColorScheme): Color {
        return when (this) {
            LUNCH -> colorScheme.tertiary
            DINNER -> colorScheme.secondary
        }
    }
}
