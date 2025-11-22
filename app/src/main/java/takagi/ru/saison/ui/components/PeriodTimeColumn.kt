package takagi.ru.saison.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import takagi.ru.saison.domain.model.BreakPeriod
import takagi.ru.saison.domain.model.CoursePeriod
import takagi.ru.saison.domain.model.GridRow
import takagi.ru.saison.util.buildGridRows

/**
 * 节次时间列组件
 * 垂直排列所有节次单元格,并集成休息时段分隔单元格
 *
 * @param periods 课程节次列表
 * @param breakPeriods 休息时段列表（用于兼容旧版本）
 * @param currentPeriod 当前节次号
 * @param cellHeight 单元格高度
 * @param showBreakIndicators 是否显示休息标识（基于timeOfDay自动检测）
 * @param modifier 修饰符
 */
@Composable
fun PeriodTimeColumn(
    periods: List<CoursePeriod>,
    breakPeriods: List<BreakPeriod> = emptyList(),
    currentPeriod: Int?,
    cellHeight: Dp,
    showBreakIndicators: Boolean = true,
    modifier: Modifier = Modifier
) {
    // 构建包含休息标识的行列表
    val gridRows = if (showBreakIndicators) {
        buildGridRows(periods)
    } else {
        periods.map { GridRow.PeriodRow(it) }
    }
    
    Column(modifier = modifier.width(60.dp)) {
        gridRows.forEach { row ->
            when (row) {
                is GridRow.PeriodRow -> {
                    PeriodTimeCell(
                        period = row.period,
                        cellHeight = cellHeight,
                        isCurrentPeriod = currentPeriod == row.period.periodNumber,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp)
                    )
                }
                is GridRow.BreakRow -> {
                    BreakSeparatorCell(
                        breakName = row.breakType.getDisplayName(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(32.dp)
                    )
                }
            }
        }
    }
}


