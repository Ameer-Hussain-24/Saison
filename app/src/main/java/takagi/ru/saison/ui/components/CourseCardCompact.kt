package takagi.ru.saison.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import takagi.ru.saison.domain.model.Course
import takagi.ru.saison.ui.theme.rememberThemeAwareCourseColor

/**
 * 紧凑课程卡片组件
 * 在网格中显示紧凑的课程信息,支持跨节次显示
 *
 * @param course 课程信息
 * @param cellHeight 单元格高度
 * @param periodSpan 跨越的节次数量
 * @param onClick 点击事件回调
 * @param modifier 修饰符
 */
@Composable
fun CourseCardCompact(
    course: Course,
    cellHeight: Dp,
    periodSpan: Int = 1,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalHeight = cellHeight * periodSpan + 4.dp * (periodSpan - 1) // 包含间距
    
    // 使用主题感知的颜色
    val courseColor = rememberThemeAwareCourseColor(course.color)
    
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(totalHeight)
            .clickable(onClick = onClick),
        color = courseColor.copy(alpha = 0.9f),
        shape = MaterialTheme.shapes.small
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = course.name,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = if (periodSpan > 1) 3 else 2,
                overflow = TextOverflow.Ellipsis
            )
            if (!course.location.isNullOrBlank()) {
                Text(
                    text = course.location,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.9f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
