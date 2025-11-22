package takagi.ru.saison.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import takagi.ru.saison.domain.model.routine.RoutineTaskWithStats
import takagi.ru.saison.util.CycleCalculator
import java.time.format.DateTimeFormatter

/**
 * 日程任务卡片组件
 */
@Composable
fun RoutineCard(
    taskWithStats: RoutineTaskWithStats,
    onCheckIn: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isActive = taskWithStats.isInActiveCycle
    val alpha = if (isActive) 1f else 0.6f
    
    // 打卡次数动画
    var previousCount by remember { mutableStateOf(taskWithStats.checkInCount) }
    val scale by animateFloatAsState(
        targetValue = if (taskWithStats.checkInCount > previousCount) 1.3f else 1f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        finishedListener = {
            previousCount = taskWithStats.checkInCount
        },
        label = "checkInCountScale"
    )
    
    LaunchedEffect(taskWithStats.checkInCount) {
        if (taskWithStats.checkInCount != previousCount) {
            previousCount = taskWithStats.checkInCount
        }
    }
    
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .alpha(alpha)
            .semantics {
                contentDescription = if (isActive) {
                    "活跃任务：${taskWithStats.task.title}，已打卡 ${taskWithStats.checkInCount} 次"
                } else {
                    "非活跃任务：${taskWithStats.task.title}"
                }
            },
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) {
                MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isActive) 2.dp else 0.dp
        ),
        shape = MaterialTheme.shapes.large,
        border = if (isActive) {
            CardDefaults.outlinedCardBorder()
        } else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 顶部：图标、标题、打卡次数
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // 左侧：图标和标题
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 图标
                    Icon(
                        imageVector = getIconForTask(taskWithStats.task.icon),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = if (isActive) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                    
                    // 标题
                    Text(
                        text = taskWithStats.task.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = if (isActive) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
                
                // 右侧：打卡次数
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "${taskWithStats.checkInCount}",
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = if (isActive && taskWithStats.checkInCount > 0) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.semantics {
                            contentDescription = "已打卡 ${taskWithStats.checkInCount} 次"
                        }
                    )
                    Text(
                        text = "次",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isActive) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 周期信息
            val cycleCalculator = remember { CycleCalculator() }
            val cycleDescription = remember(taskWithStats.task) {
                cycleCalculator.getCycleDescription(taskWithStats.task)
            }
            
            Text(
                text = cycleDescription,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // 时长信息
            taskWithStats.task.durationMinutes?.let { duration ->
                if (duration > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = takagi.ru.saison.util.DurationFormatter.formatDuration(duration),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // 非活跃任务显示下次活跃时间
            if (!isActive && taskWithStats.nextActiveDate != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "下次：${formatDate(taskWithStats.nextActiveDate)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Divider(color = MaterialTheme.colorScheme.outlineVariant)
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 打卡按钮
            FilledTonalButton(
                onClick = onCheckIn,
                enabled = isActive,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .semantics {
                        contentDescription = if (isActive) "可打卡" else "不可打卡"
                    }
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "打卡")
            }
        }
    }
}

/**
 * 根据图标名称获取对应的 Material Icon
 */
private fun getIconForTask(iconName: String?): ImageVector {
    return when (iconName) {
        "fitness" -> Icons.Default.FitnessCenter
        "book" -> Icons.Default.MenuBook
        "water" -> Icons.Default.WaterDrop
        "meditation" -> Icons.Default.SelfImprovement
        "run" -> Icons.Default.DirectionsRun
        "music" -> Icons.Default.MusicNote
        "code" -> Icons.Default.Code
        "coffee" -> Icons.Default.Coffee
        else -> Icons.Default.Task
    }
}

/**
 * 格式化日期
 */
private fun formatDate(date: java.time.LocalDate): String {
    val formatter = DateTimeFormatter.ofPattern("M月d日")
    return date.format(formatter)
}
