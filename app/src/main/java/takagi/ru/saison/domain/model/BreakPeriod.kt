package takagi.ru.saison.domain.model

/**
 * 休息时段数据模型
 * 表示课程表中的休息时段分隔（如午休、晚修）
 * 
 * @property name 休息时段名称（如"午休"、"晚修"）
 * @property afterPeriod 在第几节课后显示此休息时段
 */
data class BreakPeriod(
    val name: String,           // "午休" 或 "晚修"
    val afterPeriod: Int        // 在第几节课后显示
)
