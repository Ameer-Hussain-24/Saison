# Saison 配色方案整合方案

## 概述

参考Monica for Android的配色方案，为Saison添加更多专业的Material Design 3配色选项。

## 当前状态

Saison目前使用 `SeasonalTheme` 枚举，包含13种季节性主题：
- DYNAMIC（动态颜色）
- SAKURA（樱花）
- MINT（薄荷）
- AMBER（琥珀）
- SNOW（雪）
- RAIN（雨）
- MAPLE（枫叶）
- OCEAN（海洋）
- SUNSET（日落）
- FOREST（森林）
- LAVENDER（薰衣草）
- DESERT（沙漠）
- AURORA（极光）

## Monica的配色方案

Monica使用 `ColorScheme` 枚举，包含8种配色：
1. **DEFAULT** - Material Design 3 默认紫色
2. **OCEAN_BLUE** - 海洋蓝（专业、冷静）
3. **SUNSET_ORANGE** - 日落橙（温暖、活力）
4. **FOREST_GREEN** - 森林绿（自然、平和）
5. **TECH_PURPLE** - 科技紫（现代、科技感）
6. **BLACK_MAMBA** - 黑曼巴（Kobe Lakers风格）
7. **GREY_STYLE** - 小黑紫（蔡徐坤风格）
8. **CUSTOM** - 自定义（用户自选三色）

## 整合方案

### 方案A：扩展现有SeasonalTheme（推荐）

保留Saison的季节性主题概念，添加Monica风格的配色作为新的季节主题：

```kotlin
enum class SeasonalTheme {
    // 现有季节性主题
    DYNAMIC,
    SAKURA,
    MINT,
    AMBER,
    SNOW,
    RAIN,
    MAPLE,
    OCEAN,
    SUNSET,
    FOREST,
    LAVENDER,
    DESERT,
    AURORA,
    
    // 新增Monica风格主题
    TECH_PURPLE,      // 科技紫
    BLACK_MAMBA,      // 黑曼巴
    GREY_STYLE,       // 小黑紫
    CUSTOM            // 自定义
}
```

**优点：**
- 保持现有代码结构
- 无需大规模重构
- 用户体验连贯

**缺点：**
- 主题数量较多（17个）
- 命名风格不完全统一

### 方案B：双主题系统

同时保留两种主题系统，让用户选择：

```kotlin
enum class ThemeCategory {
    SEASONAL,    // 季节性主题
    PROFESSIONAL // 专业配色
}

// 用户可以选择类别，然后选择具体主题
```

**优点：**
- 清晰的分类
- 易于扩展

**缺点：**
- 增加复杂度
- UI需要两级选择

### 方案C：替换为Monica风格（不推荐）

完全替换为Monica的配色方案。

**优点：**
- 更专业的配色
- 完整的M3规范

**缺点：**
- 失去Saison的特色
- 需要大量重构

## 推荐实现：方案A

### 1. 扩展SeasonalTheme枚举

```kotlin
enum class SeasonalTheme {
    // ... 现有主题 ...
    
    // 新增专业配色
    TECH_PURPLE,      // 科技紫 - 现代科技感
    BLACK_MAMBA,      // 黑曼巴 - Lakers风格
    GREY_STYLE,       // 小黑紫 - 优雅灰紫
    CUSTOM            // 自定义 - 用户自选
}
```

### 2. 添加颜色定义

在 `Color.kt` 中添加Monica风格的颜色值（从Monica项目复制）：

```kotlin
// 科技紫
val TechPurplePrimary = Color(0xFF7B1FA2)
val TechPurpleOnPrimary = Color(0xFFFFFFFF)
// ... 完整的颜色定义

// 黑曼巴
val BlackMambaPrimary = Color(0xFFFDB927)  // Lakers金色
val BlackMambaSecondary = Color(0xFF552583) // Lakers紫色
// ... 完整的颜色定义

// 小黑紫
val GreyStylePrimary = Color(0xFF9575CD)
// ... 完整的颜色定义
```

### 3. 更新Theme.kt

在 `SaisonTheme` 中添加新主题的ColorScheme：

```kotlin
@Composable
fun SaisonTheme(
    theme: SeasonalTheme = SeasonalTheme.DYNAMIC,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when (theme) {
        // ... 现有主题 ...
        
        SeasonalTheme.TECH_PURPLE -> {
            if (darkTheme) TechPurpleDarkColorScheme 
            else TechPurpleLightColorScheme
        }
        
        SeasonalTheme.BLACK_MAMBA -> {
            if (darkTheme) BlackMambaDarkColorScheme 
            else BlackMambaLightColorScheme
        }
        
        SeasonalTheme.GREY_STYLE -> {
            if (darkTheme) GreyStyleDarkColorScheme 
            else GreyStyleLightColorScheme
        }
        
        SeasonalTheme.CUSTOM -> {
            // 从PreferencesManager读取自定义颜色
            customColorScheme(darkTheme)
        }
        
        else -> // ... 现有逻辑
    }
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
```

### 4. 添加自定义颜色支持

在 `PreferencesManager` 中添加：

```kotlin
// 自定义颜色设置
val CUSTOM_PRIMARY_COLOR = longPreferencesKey("custom_primary_color")
val CUSTOM_SECONDARY_COLOR = longPreferencesKey("custom_secondary_color")
val CUSTOM_TERTIARY_COLOR = longPreferencesKey("custom_tertiary_color")

val customPrimaryColor: Flow<Long>
val customSecondaryColor: Flow<Long>
val customTertiaryColor: Flow<Long>

suspend fun setCustomColors(primary: Long, secondary: Long, tertiary: Long)
```

### 5. 更新主题选择UI

在 `ThemeSelectionDialog` 中：
- 添加新主题的预览卡片
- 为CUSTOM主题添加颜色选择器
- 使用分组显示（季节性 vs 专业配色）

### 6. 字符串资源

添加新主题的名称：

```xml
<!-- values/strings.xml -->
<string name="theme_tech_purple">科技紫</string>
<string name="theme_black_mamba">黑曼巴</string>
<string name="theme_grey_style">小黑紫</string>
<string name="theme_custom">自定义</string>
```

## 实现优先级

### 高优先级
1. ✅ 添加TECH_PURPLE主题（最通用）
2. ✅ 添加颜色定义到Color.kt
3. ✅ 更新Theme.kt支持新主题
4. ✅ 更新主题选择UI

### 中优先级
5. ⏳ 添加BLACK_MAMBA主题
6. ⏳ 添加GREY_STYLE主题
7. ⏳ 添加主题分组显示

### 低优先级
8. ⏳ 实现CUSTOM自定义主题
9. ⏳ 添加颜色选择器UI
10. ⏳ 添加主题预览功能

## 技术细节

### Material Design 3 颜色系统

每个主题需要定义完整的颜色角色：
- **Primary**: 主要颜色（按钮、重要元素）
- **Secondary**: 次要颜色（辅助元素）
- **Tertiary**: 第三颜色（强调元素）
- **Surface**: 表面颜色（卡片、对话框）
- **Background**: 背景颜色
- **Error**: 错误颜色
- **Outline**: 边框颜色

每个角色都需要：
- 基础颜色
- On颜色（在该颜色上的文字颜色）
- Container颜色（容器颜色）
- OnContainer颜色（容器上的文字颜色）

### 深色主题支持

每个主题都需要提供：
- Light ColorScheme
- Dark ColorScheme

确保在深色模式下有足够的对比度和可读性。

## 用户体验

### 主题选择流程

1. 用户打开设置 → 外观 → 主题
2. 看到主题列表（可选分组显示）
3. 点击主题查看预览
4. 选择主题立即应用
5. 如果选择CUSTOM，进入颜色选择器

### 主题预览

每个主题卡片显示：
- 主题名称
- 三色预览条（Primary, Secondary, Tertiary）
- 选中状态指示器

## 兼容性

- ✅ Android 5.0+ (API 21+)
- ✅ 动态颜色支持 (Android 12+)
- ✅ 深色主题支持
- ✅ 向后兼容现有主题

## 参考

- Monica for Android 配色方案
- Material Design 3 Color System
- Material Theme Builder
