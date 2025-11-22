package takagi.ru.saison.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.roundToInt

enum class SwipeDirection {
    LEFT,   // 向左滑动（显示编辑/删除）
    RIGHT,  // 向右滑动（完成任务）
    NONE
}

@Composable
fun SwipeableTaskCard(
    onSwipeToComplete: () -> Unit,
    onSwipeToEdit: () -> Unit,
    onSwipeToDelete: () -> Unit,
    modifier: Modifier = Modifier,
    swipeThreshold: Float = 0.3f,
    content: @Composable () -> Unit
) {
    var offsetX by remember { mutableStateOf(0f) }
    var swipeDirection by remember { mutableStateOf(SwipeDirection.NONE) }
    
    val density = LocalDensity.current
    val maxSwipeDistance = with(density) { 200.dp.toPx() }
    val cardShape = MaterialTheme.shapes.medium
    
    val animatedOffsetX by animateFloatAsState(
        targetValue = offsetX,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "swipe"
    )
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(cardShape)
    ) {
        // 背景内容 - 向右滑动（完成）
        Box(
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer {
                    alpha = if (animatedOffsetX > 0) 1f else 0f
                }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 16.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 完成按钮
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            color = Color(0xFF4CAF50),
                            shape = cardShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "完成",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
        
        // 背景内容 - 向左滑动（编辑/删除）
        Box(
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer {
                    alpha = if (animatedOffsetX < 0) 1f else 0f
                }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(end = 16.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 编辑按钮
                IconButton(
                    onClick = {
                        onSwipeToEdit()
                        offsetX = 0f
                    },
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                shape = cardShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "编辑",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // 删除按钮
                IconButton(
                    onClick = {
                        onSwipeToDelete()
                        offsetX = 0f
                    },
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                color = MaterialTheme.colorScheme.error,
                                shape = cardShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "删除",
                            tint = MaterialTheme.colorScheme.onError,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
        
        // 前景内容（任务卡片）
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(animatedOffsetX.roundToInt(), 0) }
                .draggable(
                    orientation = Orientation.Horizontal,
                    state = rememberDraggableState { delta ->
                        offsetX = (offsetX + delta).coerceIn(-maxSwipeDistance, maxSwipeDistance)
                    },
                    onDragStopped = { velocity ->
                        val threshold = maxSwipeDistance * swipeThreshold
                        val buttonWidth = with(density) { 136.dp.toPx() } // 两个按钮 + 间距 (56 + 8 + 56 + 16)
                        
                        when {
                            // 向右滑动超过阈值 - 完成任务
                            offsetX > threshold -> {
                                onSwipeToComplete()
                                offsetX = 0f
                            }
                            // 向左滑动超过阈值 - 保持显示操作按钮
                            offsetX < -threshold -> {
                                offsetX = -buttonWidth
                            }
                            // 未超过阈值 - 回弹
                            else -> {
                                offsetX = 0f
                            }
                        }
                    }
                )
        ) {
            content()
        }
    }
}
