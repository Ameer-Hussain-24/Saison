package takagi.ru.saison.domain.model

import java.time.LocalTime

/**
 * 课程表模板数据模型
 * 预定义的课程时间配置方案
 */
data class ScheduleTemplate(
    val id: String,
    val name: String,
    val description: String,
    val totalPeriods: Int,
    val periodDuration: Int,
    val breakDuration: Int,
    val firstPeriodStartTime: LocalTime,
    val lunchBreakDuration: Int,
    val dinnerBreakDuration: Int
)

/**
 * 预设的课程表模板
 */
object ScheduleTemplates {
    val PRIMARY_SCHOOL = ScheduleTemplate(
        id = "primary",
        name = "小学模板",
        description = "6节课，40分钟/节",
        totalPeriods = 6,
        periodDuration = 40,
        breakDuration = 10,
        firstPeriodStartTime = LocalTime.of(8, 0),
        lunchBreakDuration = 120,
        dinnerBreakDuration = 60
    )
    
    val MIDDLE_SCHOOL = ScheduleTemplate(
        id = "middle",
        name = "初中模板",
        description = "8节课，45分钟/节",
        totalPeriods = 8,
        periodDuration = 45,
        breakDuration = 10,
        firstPeriodStartTime = LocalTime.of(8, 0),
        lunchBreakDuration = 90,
        dinnerBreakDuration = 60
    )
    
    val HIGH_SCHOOL = ScheduleTemplate(
        id = "high",
        name = "高中模板",
        description = "10节课，45分钟/节",
        totalPeriods = 10,
        periodDuration = 45,
        breakDuration = 10,
        firstPeriodStartTime = LocalTime.of(8, 0),
        lunchBreakDuration = 90,
        dinnerBreakDuration = 60
    )
    
    val UNIVERSITY = ScheduleTemplate(
        id = "university",
        name = "大学模板",
        description = "9节课，45分钟/节",
        totalPeriods = 9,
        periodDuration = 45,
        breakDuration = 10,
        firstPeriodStartTime = LocalTime.of(8, 0),
        lunchBreakDuration = 120,
        dinnerBreakDuration = 60
    )
    
    val all = listOf(PRIMARY_SCHOOL, MIDDLE_SCHOOL, HIGH_SCHOOL, UNIVERSITY)
}
