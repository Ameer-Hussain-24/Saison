# 主题预览颜色修复

## 问题

主题选择对话框中，所有主题的颜色预览条都显示相同的颜色（黄色、紫色、粉色），无法区分不同主题。

## 原因分析

`ThemePreviewCard`组件使用的是`MaterialTheme.colorScheme.primary/secondary/tertiary`，这些是**当前应用主题**的颜色，而不是**预览主题**的实际颜色。

### 问题代码

```kotlin
Box(
    modifier = Modifier
        .background(
            MaterialTheme.colorScheme.primary,  // ❌ 当前主题的颜色
            shape = MaterialTheme.shapes.small
        )
)
```

## 解决方案

### 1. 创建ThemePreviewColors数据类 ✅

```kotlin
private data class ThemePreviewColors(
    val primary: Color,
    val secondary: Color,
    val tertiary: Color
)
```

### 2. 创建getThemePreviewColors函数 ✅

为每个主题返回其实际的颜色值：

```kotlin
private fun getThemePreviewColors(theme: SeasonalTheme): ThemePreviewColors {
    return when (theme) {
        SeasonalTheme.SAKURA -> ThemePreviewColors(
            primary = SakuraPrimary,      // 粉色
            secondary = SakuraSecondary,   // 深粉色
            tertiary = SakuraPrimary
        )
        SeasonalTheme.MINT -> ThemePreviewColors(
            primary = MintPrimary,         // 薄荷绿
            secondary = MintSecondary,     // 浅绿色
            tertiary = MintPrimary
        )
        // ... 其他主题
    }
}
```

### 3. 更新ThemePreviewCard ✅

使用预览主题的实际颜色：

```kotlin
val themeColors = getThemePreviewColors(theme)
Row {
    Box(
        modifier = Modifier.background(
            themeColors.primary,  // ✅ 预览主题的实际颜色
            shape = MaterialTheme.shapes.small
        )
    )
    Box(
        modifier = Modifier.background(
            themeColors.secondary,
            shape = MaterialTheme.shapes.small
        )
    )
    Box(
        modifier = Modifier.background(
            themeColors.tertiary,
            shape = MaterialTheme.shapes.small
        )
    )
}
```

### 4. 添加默认颜色 ✅

在Color.kt中添加Material Design 3默认颜色：

```kotlin
val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)
```

## 支持的主题颜色

现在每个主题都显示其独特的颜色：

### 季节性主题
1. **动态** - 紫色系（Material Design 3默认）
2. **樱花** - 粉色系
3. **薄荷** - 绿色系
4. **琥珀** - 黄色系
5. **雪** - 蓝色系
6. **雨** - 灰蓝色系
7. **枫叶** - 橙红色系
8. **海洋** - 深蓝色系
9. **日落** - 橙色系
10. **森林** - 绿色系
11. **薰衣草** - 紫色系
12. **沙漠** - 棕色系
13. **极光** - 青绿色系

### 专业配色
14. **科技紫** - 深紫色系
15. **黑曼巴** - 金色+紫色（Lakers配色）
16. **小黑紫** - 灰紫色系

## 视觉效果

### 之前
- 所有主题显示相同的颜色（黄、紫、粉）
- 无法通过颜色区分主题
- 用户体验差

### 现在
- 每个主题显示其独特的颜色
- 一眼就能看出主题风格
- 更直观的主题选择体验

## 技术细节

### 颜色映射

每个主题的颜色预览使用该主题的Light模式颜色：
- Primary: 主要颜色
- Secondary: 次要颜色
- Tertiary: 第三颜色（如果没有定义，使用Primary）

### 为什么不使用MaterialTheme？

不能在预览中使用`MaterialTheme.colorScheme`，因为：
1. MaterialTheme反映的是**当前应用的主题**
2. 预览需要显示**其他主题的颜色**
3. 必须直接引用Color常量

### 性能考虑

- `getThemePreviewColors`是纯函数，无副作用
- 颜色值是编译时常量，无运行时开销
- when表达式在编译时优化

## 构建状态

✅ 编译成功，无错误
✅ 所有16个主题都有正确的颜色预览
✅ 颜色显示清晰可辨

## 用户体验改进

1. **更直观** - 用户可以通过颜色快速识别主题
2. **更美观** - 每个主题卡片都展示其独特的配色
3. **更专业** - 符合Material Design 3的设计规范
4. **更易用** - 减少选择主题的试错成本

## 相关文件

- `app/src/main/java/takagi/ru/saison/ui/screens/settings/SettingsScreen.kt` - 主题预览卡片
- `app/src/main/java/takagi/ru/saison/ui/theme/Color.kt` - 颜色定义

## 总结

通过创建`getThemePreviewColors`函数并直接引用每个主题的颜色常量，成功修复了主题预览颜色显示问题。现在用户可以在主题选择对话框中看到每个主题的真实颜色，大大提升了用户体验。
