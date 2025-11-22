# 设计文档

## 概述

本设计文档描述了如何在事件页面（EventScreen）和日程页面（RoutineScreen）中实现与任务页面（TaskListScreen）相同的快速跳转功能。通过在顶部应用栏添加可点击的标题区域和项目类型选择器，用户可以在任务、日程和事件三个页面之间无缝切换。

## 架构

### 现有架构分析

当前任务页面已经实现了以下功能：
- 在 `TaskListTopBar` 中使用 `Surface` 包裹标题，使其可点击
- 点击标题后显示 `ItemTypeSelectorBottomSheet`
- 通过 `onNavigateToEvents` 回调函数导航到事件页面
- 使用 `currentItemType` 状态来显示当前选中的类型

### 设计方案

我们将采用相同的设计模式，为事件页面和日程页面添加类似的功能：

1. **修改顶部应用栏**：将标题改为可点击的 Surface 组件，添加下拉箭头图标
2. **添加状态管理**：添加 `showItemTypeSelector` 状态来控制底部弹窗的显示
3. **集成 ItemTypeSelectorBottomSheet**：复用现有的项目类型选择器组件
4. **添加导航回调**：为每个页面添加必要的导航回调函数
5. **更新导航配置**：在 `SaisonNavHost` 中为事件和日程页面添加导航回调

## 组件和接口

### 1. EventScreen 修改

#### 新增参数
```kotlin
@Composable
fun EventScreen(
    viewModel: EventViewModel = hiltViewModel(),
    onEventClick: (Long) -> Unit,
    onNavigateToTasks: () -> Unit = {},      // 新增
    onNavigateToRoutine: () -> Unit = {},    // 新增
    modifier: Modifier = Modifier
)
```

#### 状态管理
```kotlin
var showItemTypeSelector by remember { mutableStateOf(false) }
```

#### TopAppBar 修改
将 `EventTopBar` 的标题部分改为可点击的 Surface：
```kotlin
title = {
    if (isSearchActive) {
        // 搜索框保持不变
    } else {
        Surface(
            onClick = { showItemTypeSelector = true },
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.wrapContentSize()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = stringResource(R.string.event_screen_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = stringResource(R.string.cd_dropdown_icon),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
```

#### 添加 ItemTypeSelectorBottomSheet
```kotlin
if (showItemTypeSelector) {
    ItemTypeSelectorBottomSheet(
        currentType = ItemType.EVENT,
        onDismiss = { showItemTypeSelector = false },
        onTypeSelected = { type ->
            showItemTypeSelector = false
            when (type) {
                ItemType.TASK -> onNavigateToTasks()
                ItemType.SCHEDULE -> onNavigateToRoutine()
                ItemType.EVENT -> {} // 保持在当前页面
            }
        }
    )
}
```

### 2. RoutineScreen 修改

#### 新增参数
```kotlin
@Composable
fun RoutineScreen(
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToCreate: () -> Unit,
    onNavigateToTasks: () -> Unit = {},      // 新增
    onNavigateToEvents: () -> Unit = {},     // 新增
    modifier: Modifier = Modifier,
    viewModel: RoutineViewModel = hiltViewModel()
)
```

#### 状态管理
```kotlin
var showItemTypeSelector by remember { mutableStateOf(false) }
```

#### TopAppBar 修改
将标题改为可点击的 Surface：
```kotlin
TopAppBar(
    title = {
        Surface(
            onClick = { showItemTypeSelector = true },
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.wrapContentSize()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "日程打卡",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = stringResource(R.string.cd_dropdown_icon),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    },
    actions = {
        IconButton(onClick = { showCreateSheet = true }) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "添加日程"
            )
        }
    }
)
```

#### 添加 ItemTypeSelectorBottomSheet
```kotlin
if (showItemTypeSelector) {
    ItemTypeSelectorBottomSheet(
        currentType = ItemType.SCHEDULE,
        onDismiss = { showItemTypeSelector = false },
        onTypeSelected = { type ->
            showItemTypeSelector = false
            when (type) {
                ItemType.TASK -> onNavigateToTasks()
                ItemType.EVENT -> onNavigateToEvents()
                ItemType.SCHEDULE -> {} // 保持在当前页面
            }
        }
    )
}
```

### 3. SaisonNavHost 修改

#### EventScreen 导航配置
```kotlin
composable(Screen.Events.route) {
    takagi.ru.saison.ui.screens.event.EventScreen(
        onEventClick = { eventId ->
            navController.navigate(Screen.EventDetail.createRoute(eventId))
        },
        onNavigateToTasks = {
            navController.navigate(Screen.Tasks.route)
        },
        onNavigateToRoutine = {
            navController.navigate(Screen.Routine.route)
        }
    )
}
```

#### RoutineScreen 导航配置
```kotlin
composable(Screen.Routine.route) {
    takagi.ru.saison.ui.screens.routine.RoutineScreen(
        onNavigateToDetail = { taskId ->
            navController.navigate(Screen.RoutineDetail.createRoute(taskId))
        },
        onNavigateToCreate = {
            // 创建任务通过 BottomSheet 实现，不需要导航
        },
        onNavigateToTasks = {
            navController.navigate(Screen.Tasks.route)
        },
        onNavigateToEvents = {
            navController.navigate(Screen.Events.route)
        }
    )
}
```

## 数据模型

本功能不需要新的数据模型，完全复用现有的 `ItemType` 枚举：

```kotlin
enum class ItemType(val value: Int) {
    TASK(0),
    SCHEDULE(1),
    EVENT(2)
}
```

## 错误处理

### 导航错误
- 在导航回调中使用 try-catch 块捕获可能的导航异常
- 参考任务页面的实现方式

### 状态管理错误
- 使用 `remember` 确保状态在配置更改时正确保存
- 在组件销毁时自动清理状态

## 测试策略

### 单元测试
不需要额外的单元测试，因为：
- 复用现有的 `ItemTypeSelectorBottomSheet` 组件
- 导航逻辑简单，由 Navigation Compose 库处理

### UI 测试
建议进行以下手动测试：
1. 在事件页面点击标题，验证底部弹窗显示
2. 在底部弹窗中选择不同类型，验证导航正确
3. 在日程页面重复上述测试
4. 验证当前页面类型在底部弹窗中正确高亮显示
5. 验证三个页面的视觉样式一致

### 集成测试
1. 测试从任务页面导航到事件页面，再导航到日程页面的完整流程
2. 测试在搜索状态下标题区域的行为（事件页面）
3. 测试快速连续点击标题的情况

## 视觉设计

### 一致性原则
- 三个页面使用相同的标题样式
- 下拉箭头图标大小统一为 20.dp
- 使用相同的 padding 值（horizontal = 8.dp, vertical = 4.dp）
- Surface 颜色使用 MaterialTheme.colorScheme.surface

### 交互反馈
- Surface 组件自动提供涟漪效果
- 点击后立即显示底部弹窗
- 选择类型后底部弹窗关闭并执行导航

## 国际化

需要确保以下字符串资源已定义：
- `R.string.cd_dropdown_icon`: 下拉图标的内容描述
- `R.string.item_type_task`: 任务类型显示名称
- `R.string.item_type_schedule`: 日程类型显示名称
- `R.string.item_type_event`: 事件类型显示名称
- `R.string.item_type_selector_title`: 项目类型选择器标题

## 性能考虑

- 底部弹窗使用 `ModalBottomSheet`，由 Material 3 优化
- 导航使用 Navigation Compose 的标准机制，性能良好
- 状态管理使用 `remember`，避免不必要的重组

## 依赖关系

- 依赖现有的 `ItemTypeSelectorBottomSheet` 组件
- 依赖 Navigation Compose 库
- 依赖 Material 3 组件库
- 不引入新的外部依赖
