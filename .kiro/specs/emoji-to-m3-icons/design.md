# Design Document - Emoji to Material 3 Icons Migration

## Overview

本设计文档描述了如何将Saison应用中的emoji图标系统地替换为Material 3图标。该迁移将提高应用的视觉一致性、专业性，并确保在所有Android设备上的显示效果一致。

## Architecture

### 图标替换策略

采用直接替换策略，将每个emoji字符替换为语义上等价的Material Icon：

```
Emoji → Material Icon
✨ → Icons.Default.AutoAwesome / Icons.Outlined.Celebration
🔍 → Icons.Default.SearchOff / Icons.Outlined.SearchOff  
📚 → Icons.Default.MenuBook / Icons.Outlined.MenuBook
```

### 组件层级

```
UI Screen (TaskListScreen, CourseScreen)
    ↓
Empty State Component
    ↓
Material Icon (替代 Emoji Text)
```

## Components and Interfaces

### 1. TaskListScreen 空状态组件

**当前实现：**
```kotlin
@Composable
private fun EmptyTaskList(filterMode: TaskFilterMode) {
    // 使用 emoji "✨"
    Text(
        text = "✨",
        style = MaterialTheme.typography.displayLarge
    )
}
```

**新设计：**
```kotlin
@Composable
private fun EmptyTaskList(filterMode: TaskFilterMode) {
    Icon(
        imageVector = Icons.Outlined.AutoAwesome,
        contentDescription = null,
        modifier = Modifier.size(64.dp),
        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
    )
}
```

**设计决策：**
- 使用 `Icons.Outlined.AutoAwesome` 表达"闪亮/特殊"的含义，与✨语义相近
- 图标尺寸设为64dp，与displayLarge文本大小协调
- 应用主题色并降低透明度，保持柔和的视觉效果
- contentDescription设为null，因为这是装饰性图标

### 2. TaskListScreen 搜索无结果组件

**当前实现：**
```kotlin
@Composable
private fun EmptySearchResult(query: String) {
    Text(
        text = "🔍",
        style = MaterialTheme.typography.displayLarge
    )
}
```

**新设计：**
```kotlin
@Composable
private fun EmptySearchResult(query: String) {
    Icon(
        imageVector = Icons.Outlined.SearchOff,
        contentDescription = null,
        modifier = Modifier.size(64.dp),
        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
    )
}
```

**设计决策：**
- 使用 `Icons.Outlined.SearchOff` 明确表达"搜索无结果"
- 使用onSurfaceVariant颜色，表示次要信息
- 保持与EmptyTaskList相同的尺寸一致性

### 3. CourseScreen 空状态组件

**当前实现：**
```kotlin
@Composable
private fun EmptyCourseList() {
    Text(
        text = "📚",
        style = MaterialTheme.typography.displayLarge
    )
}
```

**新设计：**
```kotlin
@Composable
private fun EmptyCourseList() {
    Icon(
        imageVector = Icons.Outlined.MenuBook,
        contentDescription = null,
        modifier = Modifier.size(64.dp),
        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
    )
}
```

**设计决策：**
- 使用 `Icons.Outlined.MenuBook` 表达"书籍/课程"概念
- 与其他空状态保持一致的视觉风格
- 使用Outlined风格，更轻量、现代

## Data Models

无需新增数据模型，这是纯UI层面的修改。

## Error Handling

### 图标导入错误

如果Material Icons未正确导入：
```kotlin
// 确保在文件顶部导入
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
```

### 编译时检查

- 确保所有使用的图标都在Material Icons库中存在
- 如果图标不存在，编译器会报错，需要选择替代图标

## Testing Strategy

### 视觉回归测试

1. **截图对比测试**
   - 在不同主题（浅色/深色）下截图
   - 对比emoji版本和Material Icon版本
   - 确保视觉效果改进

2. **设备兼容性测试**
   - 在不同Android版本（API 21-34）上测试
   - 在不同屏幕密度（mdpi, hdpi, xhdpi, xxhdpi）上测试
   - 验证图标清晰度和大小适配

### 功能测试

1. **空状态显示测试**
   - 验证无任务时显示正确图标
   - 验证搜索无结果时显示正确图标
   - 验证无课程时显示正确图标

2. **主题切换测试**
   - 切换到深色模式，验证图标颜色自动适配
   - 切换不同季节主题，验证图标颜色协调

3. **无障碍测试**
   - 使用TalkBack验证图标不会被朗读（contentDescription为null）
   - 确保图标周围的文本提供足够的上下文

### 代码审查检查点

- [ ] 所有emoji字符已移除
- [ ] 所有图标使用Material Icons
- [ ] 图标尺寸符合M3规范
- [ ] 图标颜色使用MaterialTheme.colorScheme
- [ ] 图标有适当的contentDescription（装饰性图标为null）
- [ ] 代码可读性提高

## Design Rationale

### 为什么选择Outlined风格？

1. **现代感**：Outlined图标更轻量、现代，符合Material 3的设计趋势
2. **一致性**：应用中其他图标主要使用Outlined风格
3. **视觉平衡**：在空状态这种大尺寸场景下，Outlined风格不会过于厚重

### 为什么使用64dp尺寸？

1. **与文本协调**：displayLarge文本样式通常对应较大的视觉元素
2. **视觉层级**：64dp是Material 3推荐的大型图标尺寸
3. **一致性**：与应用中其他大型图标保持一致

### 为什么降低透明度？

1. **视觉柔和**：全不透明的图标在空状态下可能过于突出
2. **层级表达**：降低透明度表明这是次要的装饰性元素
3. **M3规范**：Material 3建议对装饰性元素使用适当的透明度

## Implementation Notes

### 需要修改的文件

1. `app/src/main/java/takagi/ru/saison/ui/screens/task/TaskListScreen.kt`
   - EmptyTaskList 函数
   - EmptySearchResult 函数

2. `app/src/main/java/takagi/ru/saison/ui/screens/course/CourseScreen.kt`
   - EmptyCourseList 函数

### 导入语句

确保在每个文件顶部添加：
```kotlin
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material.icons.outlined.MenuBook
```

### 向后兼容性

此更改不影响应用的功能逻辑，仅改变视觉呈现，因此：
- 无需数据库迁移
- 无需API版本更新
- 无需用户数据迁移
- 可以安全地在任何版本中部署
