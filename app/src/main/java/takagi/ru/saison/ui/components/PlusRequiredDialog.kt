package takagi.ru.saison.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import takagi.ru.saison.R

/**
 * Plus 会员要求对话框
 * 
 * 当用户尝试使用会员专属功能时显示此对话框
 * 
 * @param themeName 主题名称
 * @param onNavigateToPlus 导航到 Plus 页面的回调
 * @param onDismiss 关闭对话框的回调
 */
@Composable
fun PlusRequiredDialog(
    themeName: String,
    onNavigateToPlus: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(R.string.plus_required_title))
        },
        text = {
            Text(text = stringResource(R.string.plus_required_message, themeName))
        },
        confirmButton = {
            TextButton(onClick = {
                onDismiss()
                onNavigateToPlus()
            }) {
                Text(text = stringResource(R.string.plus_required_action_go))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.plus_required_action_cancel))
            }
        }
    )
}
