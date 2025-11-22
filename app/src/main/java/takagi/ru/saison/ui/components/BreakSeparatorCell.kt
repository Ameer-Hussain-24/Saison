package takagi.ru.saison.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * 休息时段分隔单元格组件
 * 在时间列中显示休息时段名称
 *
 * @param breakName 休息时段名称（如"午休"、"晚修"）
 * @param modifier 修饰符
 */
@Composable
fun BreakSeparatorCell(
    breakName: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.height(32.dp),
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = breakName,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
