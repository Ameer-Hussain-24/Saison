package takagi.ru.saison.util

import takagi.ru.saison.domain.model.BreakType
import takagi.ru.saison.domain.model.CoursePeriod
import takagi.ru.saison.domain.model.GridRow
import takagi.ru.saison.domain.model.TimeOfDay

/**
 * 构建包含休息标识的行列表
 * 检测相邻节次的timeOfDay变化，在时段切换处插入休息标识行
 *
 * @param periods 节次列表
 * @return 包含PeriodRow和BreakRow的混合列表
 */
fun buildGridRows(periods: List<CoursePeriod>): List<GridRow> {
    if (periods.isEmpty()) return emptyList()
    
    val rows = mutableListOf<GridRow>()
    
    periods.forEachIndexed { index, period ->
        // 添加节次行
        rows.add(GridRow.PeriodRow(period))
        
        // 检查是否需要添加休息标识
        if (index < periods.size - 1) {
            val nextPeriod = periods[index + 1]
            
            // 当timeOfDay发生变化时，插入对应的休息标识
            if (period.timeOfDay != nextPeriod.timeOfDay) {
                when (nextPeriod.timeOfDay) {
                    TimeOfDay.AFTERNOON -> {
                        rows.add(
                            GridRow.BreakRow(
                                breakType = BreakType.LUNCH,
                                afterPeriod = period.periodNumber
                            )
                        )
                    }
                    TimeOfDay.EVENING -> {
                        rows.add(
                            GridRow.BreakRow(
                                breakType = BreakType.DINNER,
                                afterPeriod = period.periodNumber
                            )
                        )
                    }
                    else -> {
                        // MORNING 不需要前置休息标识
                    }
                }
            }
        }
    }
    
    return rows
}
