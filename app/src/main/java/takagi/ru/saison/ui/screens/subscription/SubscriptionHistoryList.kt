package takagi.ru.saison.ui.screens.subscription

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import takagi.ru.saison.R
import takagi.ru.saison.data.local.database.entities.HistoryOperationType
import takagi.ru.saison.data.local.database.entities.SubscriptionHistoryEntity
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun SubscriptionHistoryList(
    history: List<SubscriptionHistoryEntity>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(R.string.subscription_history_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        if (history.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.subscription_history_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                history.forEach { item ->
                    HistoryItemCard(item)
                }
            }
        }
    }
}

@Composable
private fun HistoryItemCard(item: SubscriptionHistoryEntity) {
    val operationType = try {
        HistoryOperationType.valueOf(item.operationType)
    } catch (e: Exception) {
        null
    }
    
    val (icon, iconColor, label) = getOperationTypeInfo(operationType)
    
    val timestamp = Instant.ofEpochMilli(item.timestamp)
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()
    
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 图标
            Surface(
                shape = MaterialTheme.shapes.small,
                color = iconColor.copy(alpha = 0.1f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            // 内容
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                
                if (!item.description.isNullOrBlank()) {
                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Text(
                    text = timestamp.format(dateFormatter),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun getOperationTypeInfo(type: HistoryOperationType?): Triple<ImageVector, androidx.compose.ui.graphics.Color, String> {
    val colorScheme = MaterialTheme.colorScheme
    
    return when (type) {
        HistoryOperationType.CREATED -> Triple(
            Icons.Default.Add,
            colorScheme.primary,
            stringResource(R.string.subscription_history_created)
        )
        HistoryOperationType.RENEWED_AUTO -> Triple(
            Icons.Default.Autorenew,
            colorScheme.tertiary,
            stringResource(R.string.subscription_history_renewed_auto)
        )
        HistoryOperationType.RENEWED_MANUAL -> Triple(
            Icons.Default.Refresh,
            colorScheme.tertiary,
            stringResource(R.string.subscription_history_renewed_manual)
        )
        HistoryOperationType.SKIPPED -> Triple(
            Icons.Default.SkipNext,
            colorScheme.secondary,
            stringResource(R.string.subscription_history_skipped)
        )
        HistoryOperationType.PAUSED -> Triple(
            Icons.Default.Pause,
            colorScheme.error,
            stringResource(R.string.subscription_history_paused)
        )
        HistoryOperationType.RESUMED -> Triple(
            Icons.Default.PlayArrow,
            colorScheme.primary,
            stringResource(R.string.subscription_history_resumed)
        )
        HistoryOperationType.MODIFIED -> Triple(
            Icons.Default.Edit,
            colorScheme.secondary,
            stringResource(R.string.subscription_history_modified)
        )
        HistoryOperationType.DELETED -> Triple(
            Icons.Default.Delete,
            colorScheme.error,
            stringResource(R.string.subscription_history_deleted)
        )
        null -> Triple(
            Icons.Default.Info,
            colorScheme.onSurfaceVariant,
            "未知操作"
        )
    }
}
