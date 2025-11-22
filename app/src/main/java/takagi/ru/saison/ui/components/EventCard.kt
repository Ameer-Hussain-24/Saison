package takagi.ru.saison.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import takagi.ru.saison.domain.model.Event
import takagi.ru.saison.util.EventDateCalculator
import takagi.ru.saison.util.EventDateFormatter

@Composable
fun EventCard(
    event: Event,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val daysUntil = EventDateCalculator.calculateDaysUntilForCategory(event.eventDate, event.category)
    val isPast = EventDateCalculator.isPastEvent(event.eventDate, event.category)
    val isToday = EventDateCalculator.isToday(event.eventDate, event.category)
    
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isToday -> MaterialTheme.colorScheme.primaryContainer
                isPast -> MaterialTheme.colorScheme.surfaceVariant
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左侧：类别图标和信息
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = event.category.getIcon(),
                    contentDescription = null,
                    tint = when {
                        isToday -> MaterialTheme.colorScheme.onPrimaryContainer
                        else -> MaterialTheme.colorScheme.primary
                    },
                    modifier = Modifier.size(32.dp)
                )
                
                Column {
                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            isToday -> MaterialTheme.colorScheme.onPrimaryContainer
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                    )
                    Text(
                        text = EventDateFormatter.formatEventDate(event.eventDate),
                        style = MaterialTheme.typography.bodyMedium,
                        color = when {
                            isToday -> MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
            
            // 右侧：天数显示
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = EventDateCalculator.formatDaysText(daysUntil, context),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        isToday -> MaterialTheme.colorScheme.onPrimaryContainer
                        isPast -> MaterialTheme.colorScheme.onSurfaceVariant
                        else -> MaterialTheme.colorScheme.tertiary
                    }
                )
                
                Text(
                    text = stringResource(event.category.getDisplayNameResId()),
                    style = MaterialTheme.typography.labelSmall,
                    color = when {
                        isToday -> MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}
