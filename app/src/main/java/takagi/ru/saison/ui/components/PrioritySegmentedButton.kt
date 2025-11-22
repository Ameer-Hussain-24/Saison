package takagi.ru.saison.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import takagi.ru.saison.domain.model.Priority

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrioritySegmentedButton(
    selectedPriority: Priority,
    onPrioritySelected: (Priority) -> Unit,
    modifier: Modifier = Modifier
) {
    SingleChoiceSegmentedButtonRow(
        modifier = modifier
    ) {
        Priority.values().forEachIndexed { index, priority ->
            SegmentedButton(
                selected = selectedPriority == priority,
                onClick = { onPrioritySelected(priority) },
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = Priority.values().size
                ),
                icon = {
                    if (selectedPriority == priority) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    } else {
                        Icon(
                            imageVector = getPriorityIcon(priority),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = getPriorityColor(priority)
                        )
                    }
                }
            ) {
                Text(getPriorityLabel(priority))
            }
        }
    }
}

@Composable
private fun getPriorityIcon(priority: Priority): androidx.compose.ui.graphics.vector.ImageVector {
    return when (priority) {
        Priority.LOW -> Icons.Default.ArrowDownward
        Priority.MEDIUM -> Icons.Default.Remove
        Priority.HIGH -> Icons.Default.ArrowUpward
        Priority.URGENT -> Icons.Default.PriorityHigh
    }
}

@Composable
private fun getPriorityColor(priority: Priority): Color {
    return when (priority) {
        Priority.LOW -> Color(0xFF4CAF50)
        Priority.MEDIUM -> Color(0xFF2196F3)
        Priority.HIGH -> Color(0xFFFF9800)
        Priority.URGENT -> Color(0xFFF44336)
    }
}

private fun getPriorityLabel(priority: Priority): String {
    return when (priority) {
        Priority.LOW -> "低"
        Priority.MEDIUM -> "中"
        Priority.HIGH -> "高"
        Priority.URGENT -> "紧急"
    }
}
