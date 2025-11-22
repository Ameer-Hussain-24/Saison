# 设计文档

## 概述

本设计文档详细说明如何改进 Saison 应用的主题配色系统，确保所有16个主题（1个动态主题 + 15个季节性/专业主题）都提供完整、一致且视觉效果良好的配色方案。

当前问题：
- 许多主题只定义了部分颜色角色（primary、secondary），缺少完整的 Material Design 3 颜色方案
- 主题预览只显示3种颜色，不够全面
- 某些主题的颜色对比度不足，影响可读性
- 特殊主题（黑曼巴、科技紫）的配色不够协调

## 架构

### 1. 颜色定义层次

```
Color.kt (颜色常量定义)
    ↓
Theme.kt (ColorScheme 组装)
    ↓
SaisonTheme (主题应用)
    ↓
UI Components (组件使用)
```

### 2. 颜色角色映射

Material Design 3 定义了以下颜色角色，每个主题都必须完整定义：

**核心颜色（Primary）**
- primary: 主要品牌色
- onPrimary: primary 上的文本/图标颜色
- primaryContainer: primary 的容器色（较浅）
- onPrimaryContainer: primaryContainer 上的文本/图标颜色

**次要颜色（Secondary）**
- secondary: 次要强调色
- onSecondary: secondary 上的文本/图标颜色
- secondaryContainer: secondary 的容器色
- onSecondaryContainer: secondaryContainer 上的文本/图标颜色

**第三颜色（Tertiary）**
- tertiary: 第三强调色，用于平衡和对比
- onTertiary: tertiary 上的文本/图标颜色
- tertiaryContainer: tertiary 的容器色
- onTertiaryContainer: tertiaryContainer 上的文本/图标颜色

**错误颜色（Error）**
- error: 错误状态颜色
- onError: error 上的文本/图标颜色
- errorContainer: error 的容器色
- onErrorContainer: errorContainer 上的文本/图标颜色

**背景和表面（Background & Surface）**
- background: 应用背景色
- onBackground: background 上的文本/图标颜色
- surface: 表面颜色（卡片、对话框等）
- onSurface: surface 上的文本/图标颜色
- surfaceVariant: 表面变体色（用于区分不同层级）
- onSurfaceVariant: surfaceVariant 上的文本/图标颜色
- surfaceTint: 表面着色（用于高度效果）

**轮廓（Outline）**
- outline: 边框和分隔线颜色
- outlineVariant: 轮廓变体色（较浅的边框）

**其他**
- scrim: 遮罩层颜色
- inverseSurface: 反转表面色（用于 Snackbar 等）
- inverseOnSurface: inverseSurface 上的文本颜色
- inversePrimary: 反转主色

## 组件和接口

### 1. Color.kt 增强

为每个主题添加完整的颜色定义：

```kotlin
// 示例：Sakura 主题完整颜色定义
// Light Mode
val SakuraPrimary = Color(0xFFE91E63)
val SakuraOnPrimary = Color(0xFFFFFFFF)
val SakuraPrimaryContainer = Color(0xFFFCE4EC)
val SakuraOnPrimaryContainer = Color(0xFF880E4F)

val SakuraSecondary = Color(0xFFFF4081)
val SakuraOnSecondary = Color(0xFFFFFFFF)
val SakuraSecondaryContainer = Color(0xFFFFCDD2)
val SakuraOnSecondaryContainer = Color(0xFFC2185B)

val SakuraTertiary = Color(0xFFF06292)
val SakuraOnTertiary = Color(0xFFFFFFFF)
val SakuraTertiaryContainer = Color(0xFFFFF0F3)
val SakuraOnTertiaryContainer = Color(0xFFAD1457)

val SakuraError = Color(0xFFB00020)
val SakuraOnError = Color(0xFFFFFFFF)
val SakuraErrorContainer = Color(0xFFFDEDED)
val SakuraOnErrorContainer = Color(0xFF8C0009)

val SakuraBackground = Color(0xFFFFFBFE)
val SakuraOnBackground = Color(0xFF1C1B1F)
val SakuraSurface = Color(0xFFFFFBFE)
val SakuraOnSurface = Color(0xFF1C1B1F)
val SakuraSurfaceVariant = Color(0xFFF3E0E6)
val SakuraOnSurfaceVariant = Color(0xFF4A4458)

val SakuraOutline = Color(0xFF7A757F)
val SakuraOutlineVariant = Color(0xFFCAC4D0)

// Dark Mode
val SakuraDarkPrimary = Color(0xFFF48FB1)
val SakuraDarkOnPrimary = Color(0xFF880E4F)
// ... 其他深色模式颜色
```

### 2. Theme.kt 增强

为每个主题创建完整的 ColorScheme：

```kotlin
private val SakuraLightScheme = lightColorScheme(
    primary = SakuraPrimary,
    onPrimary = SakuraOnPrimary,
    primaryContainer = SakuraPrimaryContainer,
    onPrimaryContainer = SakuraOnPrimaryContainer,
    secondary = SakuraSecondary,
    onSecondary = SakuraOnSecondary,
    secondaryContainer = SakuraSecondaryContainer,
    onSecondaryContainer = SakuraOnSecondaryContainer,
    tertiary = SakuraTertiary,
    onTertiary = SakuraOnTertiary,
    tertiaryContainer = SakuraTertiaryContainer,
    onTertiaryContainer = SakuraOnTertiaryContainer,
    error = SakuraError,
    onError = SakuraOnError,
    errorContainer = SakuraErrorContainer,
    onErrorContainer = SakuraOnErrorContainer,
    background = SakuraBackground,
    onBackground = SakuraOnBackground,
    surface = SakuraSurface,
    onSurface = SakuraOnSurface,
    surfaceVariant = SakuraSurfaceVariant,
    onSurfaceVariant = SakuraOnSurfaceVariant,
    outline = SakuraOutline,
    outlineVariant = SakuraOutlineVariant
)
```

### 3. 主题预览增强

更新 `ThemePreviewColors` 数据类以显示更多颜色：

```kotlin
private data class ThemePreviewColors(
    val primary: Color,
    val secondary: Color,
    val tertiary: Color,
    val surface: Color
)
```

更新 `ThemePreviewCard` 组件以显示4种颜色：

```kotlin
@Composable
private fun ThemePreviewCard(
    theme: SeasonalTheme,
    themeName: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(...) {
        Row(...) {
            RadioButton(...)
            Column {
                Text(themeName)
                // 4色预览条
                Row {
                    ColorBox(color = themeColors.primary, label = "主色")
                    ColorBox(color = themeColors.secondary, label = "次色")
                    ColorBox(color = themeColors.tertiary, label = "第三色")
                    ColorBox(color = themeColors.surface, label = "表面")
                }
            }
        }
    }
}
```

## 数据模型

### 主题颜色配置

每个主题需要定义以下颜色集合：

```kotlin
data class ThemeColorSet(
    // Light mode colors
    val lightPrimary: Color,
    val lightOnPrimary: Color,
    val lightPrimaryContainer: Color,
    val lightOnPrimaryContainer: Color,
    val lightSecondary: Color,
    val lightOnSecondary: Color,
    val lightSecondaryContainer: Color,
    val lightOnSecondaryContainer: Color,
    val lightTertiary: Color,
    val lightOnTertiary: Color,
    val lightTertiaryContainer: Color,
    val lightOnTertiaryContainer: Color,
    val lightError: Color,
    val lightOnError: Color,
    val lightErrorContainer: Color,
    val lightOnErrorContainer: Color,
    val lightBackground: Color,
    val lightOnBackground: Color,
    val lightSurface: Color,
    val lightOnSurface: Color,
    val lightSurfaceVariant: Color,
    val lightOnSurfaceVariant: Color,
    val lightOutline: Color,
    val lightOutlineVariant: Color,
    
    // Dark mode colors
    val darkPrimary: Color,
    val darkOnPrimary: Color,
    // ... 其他深色模式颜色
)
```

## 错误处理

### 1. 颜色对比度验证

实现颜色对比度检查工具：

```kotlin
object ColorContrastChecker {
    /**
     * 计算两个颜色之间的对比度
     * @return 对比度值（1.0 到 21.0）
     */
    fun calculateContrast(foreground: Color, background: Color): Float {
        val fgLuminance = calculateLuminance(foreground)
        val bgLuminance = calculateLuminance(background)
        
        val lighter = maxOf(fgLuminance, bgLuminance)
        val darker = minOf(fgLuminance, bgLuminance)
        
        return (lighter + 0.05f) / (darker + 0.05f)
    }
    
    /**
     * 检查对比度是否符合 WCAG AA 标准
     * @param isLargeText 是否为大文本（18pt+ 或 14pt+ 粗体）
     */
    fun meetsWCAGAA(foreground: Color, background: Color, isLargeText: Boolean = false): Boolean {
        val contrast = calculateContrast(foreground, background)
        return if (isLargeText) contrast >= 3.0f else contrast >= 4.5f
    }
    
    private fun calculateLuminance(color: Color): Float {
        // 实现相对亮度计算
        // https://www.w3.org/TR/WCAG20/#relativeluminancedef
        fun adjustChannel(channel: Float): Float {
            return if (channel <= 0.03928f) {
                channel / 12.92f
            } else {
                ((channel + 0.055f) / 1.055f).pow(2.4f)
            }
        }
        
        val r = adjustChannel(color.red)
        val g = adjustChannel(color.green)
        val b = adjustChannel(color.blue)
        
        return 0.2126f * r + 0.7152f * g + 0.0722f * b
    }
}
```

### 2. 主题应用失败处理

在 `SaisonTheme` 中添加错误处理：

```kotlin
@Composable
fun SaisonTheme(
    seasonalTheme: SeasonalTheme = SeasonalTheme.DYNAMIC,
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = try {
        when {
            dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S 
                && seasonalTheme == SeasonalTheme.DYNAMIC -> {
                val context = LocalContext.current
                if (darkTheme) dynamicDarkColorScheme(context) 
                else dynamicLightColorScheme(context)
            }
            else -> getSeasonalColorScheme(seasonalTheme, darkTheme)
        }
    } catch (e: Exception) {
        // 回退到默认主题
        Log.e("SaisonTheme", "Failed to apply theme: ${seasonalTheme.name}", e)
        if (darkTheme) darkColorScheme() else lightColorScheme()
    }
    
    // ... 其余代码
}
```

## 测试策略

### 1. 单元测试

测试颜色对比度计算：

```kotlin
class ColorContrastCheckerTest {
    @Test
    fun `test contrast calculation for black and white`() {
        val contrast = ColorContrastChecker.calculateContrast(
            Color.Black,
            Color.White
        )
        assertEquals(21.0f, contrast, 0.1f)
    }
    
    @Test
    fun `test WCAG AA compliance for normal text`() {
        val meetsStandard = ColorContrastChecker.meetsWCAGAA(
            Color(0xFF000000),
            Color(0xFFFFFFFF),
            isLargeText = false
        )
        assertTrue(meetsStandard)
    }
}
```

### 2. UI 测试

测试主题切换：

```kotlin
@Test
fun testThemeSwitching() {
    composeTestRule.setContent {
        var currentTheme by remember { mutableStateOf(SeasonalTheme.SAKURA) }
        
        SaisonTheme(seasonalTheme = currentTheme) {
            Button(onClick = { currentTheme = SeasonalTheme.MINT }) {
                Text("Switch Theme")
            }
        }
    }
    
    // 验证主题切换后颜色变化
    composeTestRule.onNodeWithText("Switch Theme").performClick()
    // 验证新主题的颜色已应用
}
```

### 3. 视觉回归测试

使用截图对比工具验证每个主题的视觉效果：

```kotlin
@Test
fun testAllThemesVisualAppearance() {
    SeasonalTheme.values().forEach { theme ->
        composeTestRule.setContent {
            SaisonTheme(seasonalTheme = theme) {
                SampleScreen()
            }
        }
        
        // 截图并与基准对比
        composeTestRule.onRoot().captureToImage()
            .assertAgainstGolden("theme_${theme.name}")
    }
}
```

## 特殊主题设计

### 1. 黑曼巴主题（Black Mamba）

灵感来源：洛杉矶湖人队配色（金色 + 紫色 + 黑色）

**配色策略：**
- Primary: 湖人金色 (#FDB927) - 用于主要操作和强调
- Secondary: 湖人紫色 (#552583) - 用于次要元素
- Tertiary: 黑色 (#000000) - 用于对比和深度
- Surface: 深灰色 - 提供良好的背景

**对比度优化：**
- 金色背景上使用黑色文本（对比度 > 7:1）
- 紫色背景上使用白色文本（对比度 > 4.5:1）
- 黑色背景上使用白色文本（对比度 = 21:1）

### 2. 科技紫主题（Tech Purple）

现代科技感的紫色渐变主题

**配色策略：**
- Primary: 深紫色 (#7B1FA2) - 专业、科技感
- Secondary: 中紫色 (#BA68C8) - 柔和过渡
- Tertiary: 浅紫色 (#CE93D8) - 轻盈点缀
- Surface: 极浅紫色 (#F3E5F5) - 统一色调

**渐变层次：**
- 使用5个层次的紫色创建深度感
- 确保每个层次之间有足够的对比度
- 深色模式反转亮度但保持色相

### 3. 小黑紫主题（Grey Style）

紫色与灰色的优雅组合

**配色策略：**
- Primary: 柔和紫色 (#9575CD) - 优雅主色
- Secondary: 蓝灰色 (#78909C) - 中性平衡
- Tertiary: 浅紫色 (#B39DDB) - 轻盈点缀
- Surface: 浅灰色 - 专业背景

**平衡原则：**
- 紫色用于强调和交互元素
- 灰色用于信息展示和背景
- 保持60-30-10配色比例

## 实现优先级

### 阶段1：核心颜色定义（高优先级）
1. 为所有16个主题定义完整的颜色常量
2. 更新所有 ColorScheme 定义
3. 验证颜色对比度

### 阶段2：主题预览优化（中优先级）
1. 更新 ThemePreviewColors 数据类
2. 重新设计 ThemePreviewCard 组件
3. 添加颜色标签和说明

### 阶段3：测试和验证（中优先级）
1. 实现颜色对比度检查工具
2. 编写单元测试
3. 进行视觉回归测试

### 阶段4：文档和优化（低优先级）
1. 更新主题使用文档
2. 性能优化
3. 无障碍功能增强

## 性能考虑

### 1. 颜色缓存

使用 `remember` 缓存颜色计算：

```kotlin
@Composable
private fun getThemePreviewColors(theme: SeasonalTheme): ThemePreviewColors {
    return remember(theme) {
        // 颜色计算只在主题变化时执行
        calculateThemeColors(theme)
    }
}
```

### 2. 延迟加载

主题预览列表使用 LazyColumn 实现虚拟滚动：

```kotlin
LazyColumn {
    items(
        items = SeasonalTheme.values().toList(),
        key = { it.name }
    ) { theme ->
        ThemePreviewCard(theme = theme, ...)
    }
}
```

### 3. 状态优化

使用 `derivedStateOf` 优化状态计算：

```kotlin
val isSelected by remember {
    derivedStateOf { theme == currentTheme }
}
```

## 无障碍支持

### 1. 颜色对比度

确保所有主题符合 WCAG 2.1 AA 标准：
- 正常文本：对比度 ≥ 4.5:1
- 大文本：对比度 ≥ 3:1
- UI 组件：对比度 ≥ 3:1

### 2. 语义化标签

为主题预览添加完整的语义化描述：

```kotlin
ThemePreviewCard(
    modifier = Modifier.semantics {
        contentDescription = "主题：${themeName}，" +
            "主色：${colorToDescription(primary)}，" +
            "次色：${colorToDescription(secondary)}，" +
            if (isSelected) "已选中" else "未选中"
    }
)
```

### 3. TalkBack 支持

在主题切换时提供语音反馈：

```kotlin
LaunchedEffect(currentTheme) {
    val accessibilityManager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) 
        as? AccessibilityManager
    if (accessibilityManager?.isEnabled == true) {
        val event = AccessibilityEvent.obtain().apply {
            eventType = AccessibilityEvent.TYPE_ANNOUNCEMENT
            text.add("已切换到${getThemeName(currentTheme)}主题")
        }
        accessibilityManager.sendAccessibilityEvent(event)
    }
}
```
