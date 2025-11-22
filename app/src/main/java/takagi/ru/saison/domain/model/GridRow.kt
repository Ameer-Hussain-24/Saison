package takagi.ru.saison.domain.model

/**
 * 网格行密封类
 * 表示课程表网格中的一行，可以是节次行或休息行
 */
sealed class GridRow {
    /**
     * 节次行
     * @param period 节次信息
     */
    data class PeriodRow(val period: CoursePeriod) : GridRow()
    
    /**
     * 休息行
     * @param breakType 休息类型
     * @param afterPeriod 在哪个节次之后
     */
    data class BreakRow(val breakType: BreakType, val afterPeriod: Int) : GridRow()
}
