package takagi.ru.saison.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

/**
 * 星期标题行组件
 * 显示星期名称和对应日期
 * 
 * 设计规范:
 * - 高度: 56dp
 * - 显示星期名称（如"周一"）和日期（如"11/3"）
 * - 当前日期高亮显示
 * - 使用weight(1f)平分宽度
 * - 使用中等粗细字体（FontWeight.Medium）
 * 
 * @param weekDays 要显示的星期列表
 * @param semesterStartDate 学期开始日期
 * @param currentWeek 当前周次（1-based）
 * @param currentDay 当前日期(用于高亮)
 * @param modifier 修饰符
 */
@Composable
fun DayHeaderRow(
    weekDays: List<DayOfWeek>,
    semesterStartDate: LocalDate,
    currentWeek: Int,
    currentDay: DayOfWeek? = null,
    modifier: Modifier = Modifier
) {
    // 计算当前周的周一日期
    val weekStartDate = semesterStartDate.plusWeeks((currentWeek - 1).toLong())
    val dateFormatter = DateTimeFormatter.ofPattern("M/d")
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        weekDays.forEach { day ->
            // 计算该星期对应的日期
            val dayDate = weekStartDate.plusDays((day.value - DayOfWeek.MONDAY.value).toLong())
            val isCurrentDay = currentDay == day && dayDate == LocalDate.now()
            
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(
                        color = if (isCurrentDay) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surface
                        },
                        shape = RoundedCornerShape(4.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // 星期名称
                    Text(
                        text = day.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (isCurrentDay) FontWeight.Bold else FontWeight.Medium,
                        color = if (isCurrentDay) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(2.dp))
                    
                    // 日期
                    Text(
                        text = dayDate.format(dateFormatter),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal,
                        color = if (isCurrentDay) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
