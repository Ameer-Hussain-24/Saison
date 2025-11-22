package takagi.ru.saison.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import takagi.ru.saison.domain.model.Course
import takagi.ru.saison.domain.model.CoursePeriod
import java.time.DayOfWeek

/**
 * 课程网格单元格组件
 * 根据是否有课程显示不同内容,支持跨节次课程
 *
 * @param course 课程信息（null表示无课程）
 * @param period 课程节次信息
 * @param day 星期几
 * @param cellHeight 单元格高度
 * @param periodSpan 课程跨越的节次数量
 * @param onCourseClick 课程点击回调
 * @param onEmptyCellClick 空白单元格点击回调
 * @param modifier 修饰符
 */
@Composable
fun CourseGridCell(
    course: Course?,
    period: CoursePeriod,
    day: DayOfWeek,
    cellHeight: Dp,
    periodSpan: Int = 1,
    onCourseClick: (Long) -> Unit,
    onEmptyCellClick: (DayOfWeek, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (course != null) {
        // 有课程时显示课程卡片
        CourseCardCompact(
            course = course,
            cellHeight = cellHeight,
            periodSpan = periodSpan,
            onClick = { onCourseClick(course.id) },
            modifier = modifier
        )
    } else {
        // 无课程时显示空白可点击区域
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(cellHeight)
                .border(
                    width = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = MaterialTheme.shapes.extraSmall
                )
                .clickable {
                    onEmptyCellClick(day, period.periodNumber)
                }
        ) {
            // 空白单元格
        }
    }
}
