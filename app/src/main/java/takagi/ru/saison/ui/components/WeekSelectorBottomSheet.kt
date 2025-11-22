package takagi.ru.saison.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * 周次选择底部抽屉组件
 * 提供周次选择界面
 *
 * @param currentWeek 当前周次
 * @param totalWeeks 学期总周数
 * @param onWeekSelected 周次选择回调
 * @param onDismiss 关闭抽屉回调
 * @param modifier 修饰符
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeekSelectorBottomSheet(
    currentWeek: Int,
    totalWeeks: Int,
    onWeekSelected: (Int) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            items(totalWeeks) { index ->
                val week = index + 1
                ListItem(
                    headlineContent = {
                        Text(
                            text = "第 $week 周",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (week == currentWeek) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onWeekSelected(week)
                            onDismiss()
                        },
                    colors = ListItemDefaults.colors(
                        containerColor = if (week == currentWeek) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            Color.Transparent
                        }
                    )
                )
            }
        }
    }
}
