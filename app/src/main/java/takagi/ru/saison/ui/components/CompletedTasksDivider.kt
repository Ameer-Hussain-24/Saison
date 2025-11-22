package takagi.ru.saison.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CompletedTasksDivider(
    completedCount: Int,
    isExpanded: Boolean,
    onToggleExpanded: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outlineVariant
        )
        
        TextButton(
            onClick = onToggleExpanded
        ) {
            Text("已完成 ($completedCount)")
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = if (isExpanded) 
                    Icons.Default.ExpandLess 
                else 
                    Icons.Default.ExpandMore,
                contentDescription = if (isExpanded) "收起" else "展开"
            )
        }
        
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outlineVariant
        )
    }
}
