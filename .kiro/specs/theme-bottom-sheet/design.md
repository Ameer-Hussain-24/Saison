# 设计文档

## 概述

本文档描述了将 Saison 任务管理应用设置页面中的主题选择对话框改造为 Material 3 Extended (M3E) 风格 Bottom Sheet 的设计方案。设计目标是提供更现代化、更流畅的主题选择交互体验，完全符合 Material 3 设计规范。

## 架构

### 组件结构

```
SettingsScreen
    └── showThemeBottomSheet (State)
        └── ThemeBottomSheet (ModalBottomSheet)
            ├── Drag Handle
            ├── Title
            └── LazyColumn
                └── ThemePreviewCard (多个)
                    ├── RadioButton
                    ├── Theme Name
                    └── Color Preview Bar
```

### 状态管理

```kotlin
// 在 SettingsScreen 中
var showThemeBottomSheet by remember { mutableStateOf(false) }

// 替换原有的
var showThemeDialog by remember { mutableStateOf(false) }
```

## 组件和接口

### 1. ThemeBottomSheet

#### 组件签名

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeBottomSheet(
    currentTheme: SeasonalTheme,
    onThemeSelected: (SeasonalTheme) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
)
```

#### 实现细节

**容器：**
- 使用 `ModalBottomSheet` 组件
- 设置 `onDismissRequest = onDismiss`
- 使用 `sheetState = rememberModalBottomSheetState()`
- 设置 `containerColor = MaterialTheme.colorScheme.surface`
- 设置 `shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)`

**内容布局：**
```kotlin
Column(
    modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp)
) {
    // 标题
    Text(
        text = stringResource(R.string.settings_theme_dialog_title),
        style = MaterialTheme.typography.headlineSmall,
        modifier = Modifier.padding(vertical = 16.dp)
    )
    
    // 主题列表
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        items(
            items = SeasonalTheme.values(),
            key = { it.name }
        ) { theme ->
            ThemePreviewCard(
                theme = theme,
                themeName = getThemeName(theme),
                isSelected = theme == currentTheme,
                onClick = {
                    onThemeSelected(theme)
                    onDismiss()
                }
            )
        }
    }
}
```

### 2. ThemePreviewCard (保持现有设计)

#### 组件签名

```kotlin
@Composable
private fun ThemePreviewCard(
    theme: SeasonalTheme,
    themeName: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
)
```

#### 设计规范

**卡片样式：**
- 使用 `Card` 组件
- 背景色：`if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant`
- 边框：`if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null`
- 圆角：`MaterialTheme.shapes.medium` (12dp)
- 点击效果：`clickable(onClick = onClick)`

**内容布局：**
```kotlin
Row(
    modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
    verticalAlignment = Alignment.CenterVertically
) {
    RadioButton(
        selected = isSelected,
        onClick = onClick
    )
    Spacer(modifier = Modifier.width(12.dp))
    Column(modifier = Modifier.weight(1f)) {
        Text(
            text = themeName,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
        Spacer(modifier = Modifier.height(8.dp))
        // 颜色预览条
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(24.dp)
                    .background(
                        themeColors.primary,
                        shape = MaterialTheme.shapes.small
                    )
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(24.dp)
                    .background(
                        themeColors.secondary,
                        shape = MaterialTheme.shapes.small
                    )
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(24.dp)
                    .background(
                        themeColors.tertiary,
                        shape = MaterialTheme.shapes.small
                    )
            )
        }
    }
}
```

### 3. SettingsScreen 集成

#### 修改点

**状态变量：**
```kotlin
// 替换
var showThemeDialog by remember { mutableStateOf(false) }
// 为
var showThemeBottomSheet by remember { mutableStateOf(false) }
```

**触发显示：**
```kotlin
SettingsItem(
    icon = Icons.Default.Palette,
    title = stringResource(R.string.settings_theme_title),
    subtitle = getThemeName(currentTheme),
    onClick = { showThemeBottomSheet = true }  // 修改这里
)
```

**显示 Bottom Sheet：**
```kotlin
// 替换原有的 ThemeSelectionDialog
if (showThemeBottomSheet) {
    ThemeBottomSheet(
        currentTheme = currentTheme,
        onThemeSelected = { theme ->
            viewModel.setTheme(theme)
        },
        onDismiss = { showThemeBottomSheet = false }
    )
}
```

## 数据模型

### 无需修改

现有的数据模型完全适用：
- `SeasonalTheme` 枚举
- `ThemePreviewColors` 数据类
- `getThemePreviewColors()` 函数
- `getThemeName()` 函数

## Material 3 设计规范

### Bottom Sheet 规范

**尺寸：**
- 最大高度：屏幕高度的 80%（竖屏）/ 90%（横屏）
- 最大宽度：600dp（平板）
- 顶部圆角：28dp
- 拖动手柄：32dp x 4dp

**间距：**
- 水平内边距：16dp
- 标题垂直内边距：16dp
- 卡片间距：8dp
- 底部内边距：16dp + 系统导航栏高度

**颜色：**
- 背景：`surface`
- 遮罩：`scrim` (alpha 0.32)
- 拖动手柄：`onSurfaceVariant` (alpha 0.4)

**动画：**
- 滑入时长：300ms
- 滑出时长：250ms
- 缓动函数：`FastOutSlowInEasing`

### 主题预览卡片规范

**尺寸：**
- 最小高度：88dp
- 内边距：16dp
- RadioButton 大小：24dp
- 颜色预览条高度：24dp

**间距：**
- RadioButton 到文本：12dp
- 文本到颜色条：8dp
- 颜色条之间：4dp

**颜色：**
- 未选中背景：`surfaceVariant`
- 选中背景：`primaryContainer`
- 选中边框：`primary` (2dp)
- 文本：`onSurface`
- 选中文本：`onPrimaryContainer` (加粗)

## 错误处理

### 主题应用失败

```kotlin
onThemeSelected = { theme ->
    try {
        viewModel.setTheme(theme)
        onDismiss()
    } catch (e: Exception) {
        // 显示错误 Snackbar
        scope.launch {
            snackbarHostState.showSnackbar(
                message = "主题应用失败：${e.message}",
                duration = SnackbarDuration.Short
            )
        }
    }
}
```

### Bottom Sheet 状态错误

```kotlin
val sheetState = rememberModalBottomSheetState(
    skipPartiallyExpanded = false
)

// 处理状态错误
LaunchedEffect(sheetState.isVisible) {
    if (!sheetState.isVisible && showThemeBottomSheet) {
        showThemeBottomSheet = false
    }
}
```

## 性能优化

### 1. 延迟加载

```kotlin
// 使用 LazyColumn 实现虚拟化
LazyColumn {
    items(
        items = SeasonalTheme.values(),
        key = { it.name }  // 使用 key 优化重组
    ) { theme ->
        ThemePreviewCard(...)
    }
}
```

### 2. 颜色缓存

```kotlin
// 在 ThemePreviewCard 中缓存颜色
val themeColors = remember(theme) {
    getThemePreviewColors(theme)
}
```

### 3. 状态优化

```kotlin
// 使用 derivedStateOf 避免不必要的重组
val isSelected by remember {
    derivedStateOf { theme == currentTheme }
}
```

## 无障碍设计

### 语义化标签

```kotlin
ModalBottomSheet(
    modifier = Modifier.semantics {
        contentDescription = "主题选择面板"
        role = Role.Dialog
    }
)
```

### 状态宣告

```kotlin
LaunchedEffect(showThemeBottomSheet) {
    if (showThemeBottomSheet) {
        // 宣告 Bottom Sheet 打开
        announceForAccessibility("主题选择已打开")
    }
}
```

### 触摸目标

```kotlin
// 确保 RadioButton 和卡片的触摸目标足够大
RadioButton(
    modifier = Modifier.minimumInteractiveComponentSize()
)
```

## 响应式设计

### 屏幕尺寸适配

```kotlin
@Composable
fun ThemeBottomSheet(...) {
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600
    
    ModalBottomSheet(
        modifier = Modifier.then(
            if (isTablet) {
                Modifier.widthIn(max = 600.dp)
            } else {
                Modifier.fillMaxWidth()
            }
        )
    ) {
        // 内容
    }
}
```

### 横屏适配

```kotlin
val maxHeight = if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
    0.9f  // 横屏占 90%
} else {
    0.8f  // 竖屏占 80%
}

ModalBottomSheet(
    modifier = Modifier.fillMaxHeight(maxHeight)
) {
    // 内容
}
```

## 测试策略

### UI 测试

```kotlin
@Test
fun themeBottomSheet_opensAndCloses() {
    composeTestRule.setContent {
        var showSheet by remember { mutableStateOf(false) }
        
        Button(onClick = { showSheet = true }) {
            Text("Open")
        }
        
        if (showSheet) {
            ThemeBottomSheet(
                currentTheme = SeasonalTheme.SAKURA,
                onThemeSelected = {},
                onDismiss = { showSheet = false }
            )
        }
    }
    
    // 点击按钮打开
    composeTestRule.onNodeWithText("Open").performClick()
    
    // 验证 Bottom Sheet 显示
    composeTestRule.onNodeWithText("选择主题").assertIsDisplayed()
    
    // 点击背景关闭
    composeTestRule.onNodeWithTag("scrim").performClick()
    
    // 验证 Bottom Sheet 关闭
    composeTestRule.onNodeWithText("选择主题").assertDoesNotExist()
}

@Test
fun themeBottomSheet_selectsTheme() {
    var selectedTheme: SeasonalTheme? = null
    
    composeTestRule.setContent {
        ThemeBottomSheet(
            currentTheme = SeasonalTheme.SAKURA,
            onThemeSelected = { selectedTheme = it },
            onDismiss = {}
        )
    }
    
    // 点击主题
    composeTestRule.onNodeWithText("薄荷").performClick()
    
    // 验证主题被选中
    assertEquals(SeasonalTheme.MINT, selectedTheme)
}
```

## 迁移步骤

### 1. 保留原有代码

在修改前，保留原有的 `ThemeSelectionDialog` 代码作为备份。

### 2. 创建新组件

创建 `ThemeBottomSheet` 组件，复用 `ThemePreviewCard`。

### 3. 更新 SettingsScreen

修改状态变量和显示逻辑。

### 4. 测试验证

- 测试打开和关闭
- 测试主题选择
- 测试动画效果
- 测试无障碍功能
- 测试不同屏幕尺寸

### 5. 清理代码

确认新实现稳定后，删除旧的 `ThemeSelectionDialog` 代码。

## 设计决策

### 为什么使用 ModalBottomSheet 而非 BottomSheet？

- **模态行为**：主题选择需要用户专注，模态 Bottom Sheet 提供更好的焦点管理
- **背景遮罩**：模态 Bottom Sheet 自动提供背景遮罩，增强视觉层次
- **手势支持**：模态 Bottom Sheet 内置下滑关闭手势

### 为什么保留 ThemePreviewCard？

- **代码复用**：现有的卡片设计已经很好，无需重新实现
- **一致性**：保持与其他设置项的视觉一致性
- **维护性**：减少代码重复，便于维护

### 为什么不使用 BottomSheetScaffold？

- **使用场景**：BottomSheetScaffold 适合持久化的 Bottom Sheet，而主题选择是临时的
- **复杂度**：ModalBottomSheet 更简单，满足需求
- **性能**：ModalBottomSheet 按需加载，性能更好

## 未来扩展

### 1. 主题预览

在 Bottom Sheet 中添加实时主题预览：
- 显示应用界面的小型预览
- 用户可以在选择前预览效果

### 2. 主题分组

将主题按类别分组：
- 季节主题
- 专业配色
- 自定义主题

### 3. 主题搜索

添加搜索功能：
- 快速查找主题
- 按颜色筛选

### 4. 主题收藏

允许用户收藏常用主题：
- 收藏的主题显示在顶部
- 快速切换常用主题

## 技术栈

- **UI**: Jetpack Compose
- **组件**: Material 3 ModalBottomSheet
- **状态管理**: Compose State
- **动画**: Compose Animation
- **测试**: Compose Testing

## 实现优先级

### 高优先级（MVP）

1. 创建 ThemeBottomSheet 组件
2. 集成到 SettingsScreen
3. 基础动画和交互
4. 主题选择功能

### 中优先级

1. 响应式设计适配
2. 无障碍支持
3. 性能优化
4. 错误处理

### 低优先级

1. 高级动画效果
2. 主题预览
3. 主题分组
4. 主题搜索
