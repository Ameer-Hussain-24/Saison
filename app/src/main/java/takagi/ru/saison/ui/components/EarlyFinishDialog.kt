package takagi.ru.saison.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import takagi.ru.saison.R

/**
 * 提前结束对话框
 * 用于确认是否要提前结束番茄钟并标记任务完成
 */
@Composable
fun EarlyFinishDialog(
    usedMinutes: Int,
    onMarkComplete: () -> Unit,
    onJustStop: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(R.string.pomodoro_early_finish))
        },
        text = {
            Text(
                text = stringResource(
                    R.string.pomodoro_early_finish_message,
                    usedMinutes
                )
            )
        },
        confirmButton = {
            TextButton(onClick = onMarkComplete) {
                Text(text = stringResource(R.string.pomodoro_mark_complete))
            }
        },
        dismissButton = {
            TextButton(onClick = onJustStop) {
                Text(text = stringResource(R.string.pomodoro_just_stop))
            }
        }
    )
}
