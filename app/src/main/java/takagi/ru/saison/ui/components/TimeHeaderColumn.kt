package takagi.ru.saison.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import takagi.ru.saison.domain.model.CoursePeriod
import java.time.format.DateTimeFormatter

/**
 * 时间标题列组件
 * 显示每节课的时间范围("08:00-08:45"等)
 * 
 * @param periods 节次列表
 * @param cellHeight 单元格高度
 * @param modifier 修饰符
 */
@Composable
fun TimeHeaderColumn(
    periods: List<CoursePeriod>,
    cellHeight: Dp,
    modifier: Modifier = Modifier
) {
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    
    Column(
        modifier = modifier.width(80.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        periods.forEach { period ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(cellHeight)
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(4.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${period.startTime.format(timeFormatter)}-${period.endTime.format(timeFormatter)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
