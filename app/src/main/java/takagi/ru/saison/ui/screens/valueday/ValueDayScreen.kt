package takagi.ru.saison.ui.screens.valueday

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import takagi.ru.saison.data.local.database.entities.ValueDayEntity
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun ValueDayScreen(
    viewModel: ValueDayViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val valueDays by viewModel.valueDays.collectAsState()
    val statistics by viewModel.statistics.collectAsState()
    val filterMode by viewModel.filterMode.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    var showAddSheet by remember { mutableStateOf(false) }
    var editingValueDay by remember { mutableStateOf<ValueDayEntity?>(null) }
    var isSearchActive by remember { mutableStateOf(false) }
    var showCategoryDrawer by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            ValueDayTopBar(
                isSearchActive = isSearchActive,
                searchQuery = searchQuery,
                selectedCategory = selectedCategory,
                onSearchQueryChange = { viewModel.setSearchQuery(it) },
                onSearchToggle = {
                    isSearchActive = !isSearchActive
                    if (!isSearchActive) viewModel.setSearchQuery("")
                },
                onFilterClick = { showCategoryDrawer = true }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddSheet = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("添加买断") }
            )
        }
    ) { paddingValues ->
        ValueDayContent(
            paddingValues = paddingValues,
            valueDays = valueDays,
            statistics = statistics,
            filterMode = filterMode,
            onEdit = { editingValueDay = it },
            onDelete = { viewModel.deleteValueDay(it) },
            onFilterModeChange = { viewModel.setFilterMode(it) }
        )
    }

    if (showAddSheet) {
        AddValueDaySheet(
            categories = categories,
            onDismiss = { showAddSheet = false },
            onAdd = { itemName, price, date, category, warrantyEndDate ->
                viewModel.addValueDay(itemName, price, date, category, warrantyEndDate)
                showAddSheet = false
            },
            onAddCategory = { categoryName ->
                viewModel.addCategory(categoryName)
            }
        )
    }

    editingValueDay?.let { valueDay ->
        AddValueDaySheet(
            isEdit = true,
            initialValueDay = valueDay,
            categories = categories,
            onDismiss = { editingValueDay = null },
            onAdd = { itemName, price, date, category, warrantyEndDate ->
                viewModel.updateValueDay(
                    valueDay.copy(
                        itemName = itemName,
                        purchasePrice = price,
                        purchaseDate = date.toEpochDay(),
                        category = category,
                        warrantyEndDate = warrantyEndDate?.toEpochDay(),
                        updatedAt = System.currentTimeMillis()
                    )
                )
                editingValueDay = null
            },
            onAddCategory = { categoryName ->
                viewModel.addCategory(categoryName)
            }
        )
    }

    if (showCategoryDrawer) {
        Box(modifier = Modifier.fillMaxSize()) {
            ValueDayCategoryDrawer(
                visible = showCategoryDrawer,
                categories = categories,
                selectedCategory = selectedCategory,
                onDismiss = { showCategoryDrawer = false },
                onCategorySelected = { category ->
                    viewModel.setSelectedCategory(category)
                    showCategoryDrawer = false
                },
                onAddCategory = { categoryName ->
                    viewModel.addCategory(categoryName)
                },
                onDeleteCategory = { category ->
                    viewModel.deleteCategory(category)
                },
                onRenameCategory = { oldName, newName ->
                    viewModel.renameCategory(oldName, newName)
                }
            )
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
internal fun ValueDayContent(
    paddingValues: PaddingValues,
    valueDays: List<ValueDayEntity>,
    statistics: ValueDayStatistics,
    filterMode: ValueDayFilterMode,
    onEdit: (ValueDayEntity) -> Unit,
    onDelete: (ValueDayEntity) -> Unit,
    onFilterModeChange: (ValueDayFilterMode) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(
            top = 8.dp,
            bottom = 88.dp
        )
    ) {
        item {
            StatisticsCard(
                statistics = statistics,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        stickyHeader {
            ValueDayFilterChips(
                selectedMode = filterMode,
                onModeSelected = onFilterModeChange
            )
        }
        if (valueDays.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "暂无买断记录\n点击右下角添加按钮开始记录",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(
                items = valueDays,
                key = { it.id }
            ) { valueDay ->
                ValueDayCard(
                    valueDay = valueDay,
                    onEdit = { onEdit(valueDay) },
                    onDelete = { onDelete(valueDay) },
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ValueDayTopBar(
    isSearchActive: Boolean,
    searchQuery: String,
    selectedCategory: String?,
    onSearchQueryChange: (String) -> Unit,
    onSearchToggle: () -> Unit,
    onFilterClick: () -> Unit
) {
    TopAppBar(
        title = {
            if (isSearchActive) {
                TextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text("搜索买断记录") },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "买断",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        },
        actions = {
            IconButton(onClick = onSearchToggle) {
                Icon(
                    imageVector = if (isSearchActive) Icons.Default.Close else Icons.Default.Search,
                    contentDescription = if (isSearchActive) "关闭搜索" else "搜索"
                )
            }
            Card(
                onClick = onFilterClick,
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Text(
                    text = selectedCategory ?: "全部",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    )
}

@Composable
private fun StatisticsCard(
    statistics: ValueDayStatistics,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    icon = Icons.Default.AttachMoney,
                    label = "商品数量",
                    value = "${statistics.totalItems}",
                    modifier = Modifier.weight(1f)
                )
                
                StatItem(
                    icon = Icons.AutoMirrored.Filled.TrendingDown,
                    label = "总支出",
                    value = "¥${String.format("%.0f", statistics.totalCost)}",
                    modifier = Modifier.weight(1f)
                )
                
                StatItem(
                    icon = Icons.Default.Schedule,
                    label = "每日总值",
                    value = "¥${String.format("%.2f", statistics.averageDailyCost)}",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(48.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(28.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ValueDayFilterChips(
    selectedMode: ValueDayFilterMode,
    onModeSelected: (ValueDayFilterMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background
    ) {
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            SegmentedButton(
                selected = selectedMode == ValueDayFilterMode.ALL,
                onClick = { onModeSelected(ValueDayFilterMode.ALL) },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 4)
            ) {
                Text("全部")
            }
            
            SegmentedButton(
                selected = selectedMode == ValueDayFilterMode.WITH_WARRANTY,
                onClick = { onModeSelected(ValueDayFilterMode.WITH_WARRANTY) },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 4)
            ) {
                Text("有保修")
            }
            
            SegmentedButton(
                selected = selectedMode == ValueDayFilterMode.WARRANTY_EXPIRED,
                onClick = { onModeSelected(ValueDayFilterMode.WARRANTY_EXPIRED) },
                shape = SegmentedButtonDefaults.itemShape(index = 2, count = 4)
            ) {
                Text("已过期")
            }
            
            SegmentedButton(
                selected = selectedMode == ValueDayFilterMode.NO_WARRANTY,
                onClick = { onModeSelected(ValueDayFilterMode.NO_WARRANTY) },
                shape = SegmentedButtonDefaults.itemShape(index = 3, count = 4)
            ) {
                Text("无保修")
            }
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "暂无买断记录",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "点击右下角添加按钮开始记录",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ValueDayCard(
    valueDay: ValueDayEntity,
    onEdit: (ValueDayEntity) -> Unit,
    onDelete: (ValueDayEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormatter = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd") }
    val purchaseDate = valueDay.getPurchaseDateAsLocalDate()
    val daysSince = valueDay.getDaysSincePurchase()
    val dailyCost = valueDay.getDailyAverageCost()
    
    var showDeleteDialog by remember { mutableStateOf(false) }
    var offsetX by remember { mutableStateOf(0f) }
    val density = LocalDensity.current
    val maxSwipeDistance = with(density) { 152.dp.toPx() }
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
        // 左滑显示删除按钮
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
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        showDeleteDialog = true
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
        
        // 右滑显示编辑按钮
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
                IconButton(
                    onClick = {
                        onEdit(valueDay)
                        offsetX = 0f
                    },
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                color = MaterialTheme.colorScheme.tertiary,
                                shape = cardShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "编辑",
                            tint = MaterialTheme.colorScheme.onTertiary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(animatedOffsetX.roundToInt(), 0) }
                .draggable(
                    orientation = Orientation.Horizontal,
                    state = rememberDraggableState { delta ->
                        offsetX = (offsetX + delta).coerceIn(-maxSwipeDistance, maxSwipeDistance)
                    },
                    onDragStopped = {
                        val threshold = maxSwipeDistance * 0.3f
                        val buttonWidth = with(density) { 72.dp.toPx() }
                        offsetX = when {
                            offsetX < -threshold -> -buttonWidth
                            offsetX > threshold -> buttonWidth
                            else -> 0f
                        }
                    }
                ),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = valueDay.itemName,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = valueDay.category,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Text(
                        text = "¥${String.format("%.0f", valueDay.purchasePrice)}",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = purchaseDate.format(dateFormatter),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "已使用 $daysSince 天 · ¥${String.format("%.2f", dailyCost)}/天",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                valueDay.getWarrantyEndDateAsLocalDate()?.let { warrantyEndDate ->
                    Spacer(modifier = Modifier.height(8.dp))
                    val daysRemaining = valueDay.getWarrantyDaysRemaining() ?: 0
                    val isExpired = valueDay.isWarrantyExpired()
                    
                    Surface(
                        color = if (isExpired) {
                            MaterialTheme.colorScheme.errorContainer
                        } else {
                            MaterialTheme.colorScheme.tertiaryContainer
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = if (isExpired) {
                                    MaterialTheme.colorScheme.onErrorContainer
                                } else {
                                    MaterialTheme.colorScheme.onTertiaryContainer
                                }
                            )
                            Text(
                                text = if (isExpired) {
                                    "保修已过期"
                                } else {
                                    "保修剩余 $daysRemaining 天"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isExpired) {
                                    MaterialTheme.colorScheme.onErrorContainer
                                } else {
                                    MaterialTheme.colorScheme.onTertiaryContainer
                                }
                            )
                        }
                    }
                }
            }
        }
    }
    
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("确认删除") },
            text = { Text("确定要删除「${valueDay.itemName}」吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete(valueDay)
                        showDeleteDialog = false
                    }
                ) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}
