package takagi.ru.saison.ui.components

import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * 周次选择按钮组件
 * 显示当前周次并触发周次选择抽屉
 *
 * @param currentWeek 当前周次
 * @param totalWeeks 学期总周数
 * @param onClick 点击事件回调
 * @param modifier 修饰符
 */
@Composable
fun WeekSelectorButton(
    currentWeek: Int,
    totalWeeks: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Text(
            text = "${currentWeek}周",
            style = MaterialTheme.typography.labelLarge
        )
    }
}
