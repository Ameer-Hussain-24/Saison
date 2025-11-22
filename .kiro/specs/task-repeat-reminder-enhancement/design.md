# Design Document

## Overview

本设计文档描述了任务重复和提醒功能的UI增强方案。通过改进交互设计和视觉呈现，使用户能够更直观地设置任务的重复规则和提醒选项。设计遵循Material Design 3规范，提供清晰的视觉反馈和信息提示。

## Architecture

### Component Structure

```
AddTaskSheet
├── 基础信息区域
│   ├── 标题输入
│   ├── 快捷日期按钮
│   ├── 已选日期显示
│   └── 优先级选择
├── 更多选项区域（可展开）
│   ├── 重复设置 (RepeatSection) ← 新设计
│   ├── 提醒设置 (ReminderSection) ← 新设计
│   └── 描述输入
└── 底部操作按钮
```

### State Management

```kotlin
// 现有状态
var showMoreOptions by remember { mutableStateOf(false) }
var selectedDate by remember { mutableStateOf<LocalDateTime?>(null) }

// 新增/修改状态
var selectedRepeatType by remember { mutableStateOf("不重复") }  // 替代 repeatEnabled
var reminderEnabled by remember { mutableStateOf(false) }
```

## Components and Interfaces

### 1. RepeatSection（重复设置区域）

#### UI结构

```
Column (fillMaxWidth, spacing: 8dp)
├── Row (标题行)
│   ├── Icon (Repeat, 20dp, 动态颜色)
│   └── Text ("重复", titleSmall, Medium)
└── Row (选项行, spacing: 8dp)
    ├── FilterChip ("不重复", weight: 1f)
    ├── FilterChip ("每天", weight: 1f)
    ├── FilterChip ("每周", weight: 1f)
    ├── FilterChip ("每月", weight: 1f)
    └── FilterChip ("每年", weight: 1f)
```

#### 视觉设计

**图标颜色逻辑：**
- 当 `selectedRepeatType != "不重复"` 时：使用 `primary` 色
- 当 `selectedRepeatType == "不重复"` 时：使用 `onSurfaceVariant` 色

**FilterChip样式：**
- 选中状态：
  - 背景色：`primaryContainer`
  - 文字色：`onPrimaryContainer`
- 未选中状态：
  - 背景色：默认
  - 文字色：默认
- 所有选项平均分配宽度（使用 `weight(1f)`）

#### 交互行为

1. 用户点击任意FilterChip
2. 更新 `selectedRepeatType` 状态
3. 图标颜色自动更新
4. 选中的FilterChip高亮显示

### 2. ReminderSection（提醒设置区域）

#### UI结构

```
Card (fillMaxWidth, 可点击, 动态背景色)
└── Column (padding: 16dp, spacing: 4dp)
    ├── Row (主要内容行)
    │   ├── Row (左侧)
    │   │   ├── Icon (Notifications, 20dp, 动态颜色)
    │   │   └── Text ("提醒我", titleSmall, Medium)
    │   └── Switch (checked: reminderEnabled)
    └── Text (提醒说明, 条件显示)
```

#### 视觉设计

**Card背景色逻辑：**
- 当 `reminderEnabled == true` 时：`primaryContainer.copy(alpha = 0.3f)`
- 当 `reminderEnabled == false` 时：`surfaceContainerLow`

**图标颜色逻辑：**
- 当 `reminderEnabled == true` 时：使用 `primary` 色
- 当 `reminderEnabled == false` 时：使用 `onSurfaceVariant` 色

**提醒说明文字样式：**
- 字体：`bodySmall`
- 颜色：`onSurfaceVariant`
- 显示条件：仅当 `reminderEnabled == true` 时显示

#### 交互行为

1. 用户点击Card的任意区域
2. 切换 `reminderEnabled` 状态
3. Card背景色平滑过渡
4. 图标颜色更新
5. 提醒说明文字淡入/淡出

### 3. ReminderTextLogic（提醒说明文字逻辑）

#### 决策树

```
IF reminderEnabled == true THEN
    IF selectedDate != null THEN
        IF selectedRepeatType != "不重复" THEN
            显示: "将在每次重复时提醒"
        ELSE
            显示: "将在 [MM月dd日] 提醒"
        END IF
    ELSE
        IF selectedRepeatType != "不重复" THEN
            显示: "将在每次重复时提醒"
        ELSE
            显示: "将在任务当天提醒"
        END IF
    END IF
END IF
```

#### 实现代码结构

```kotlin
if (reminderEnabled) {
    Text(
        text = if (selectedDate != null) {
            if (selectedRepeatType != "不重复") {
                "将在每次重复时提醒"
            } else {
                "将在 ${selectedDate!!.format(DateTimeFormatter.ofPattern("MM月dd日"))} 提醒"
            }
        } else {
            if (selectedRepeatType != "不重复") {
                "将在每次重复时提醒"
            } else {
                "将在任务当天提醒"
            }
        },
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}
```

## Data Models

### RepeatType映射

UI选项到RecurrenceRule的映射关系：

| UI选项 | Frequency | Interval | 说明 |
|--------|-----------|----------|------|
| 不重复 | null | - | 不创建RecurrenceRule |
| 每天 | DAILY | 1 | 每天重复 |
| 每周 | WEEKLY | 1 | 每周重复 |
| 每月 | MONTHLY | 1 | 每月重复 |
| 每年 | YEARLY | 1 | 每年重复 |

### 基准日期计算

```kotlin
fun getBaseDate(selectedDate: LocalDateTime?): LocalDate {
    return selectedDate?.toLocalDate() ?: LocalDate.now()
}
```

## Error Handling

### 边界情况处理

1. **未设置日期但启用重复**
   - 使用当前日期作为基准
   - 在UI上不显示错误，自动处理

2. **快速切换重复选项**
   - 使用 `remember` 状态管理，确保UI响应流畅
   - 不需要防抖处理

3. **提醒开关状态同步**
   - Card点击和Switch点击都能正确切换状态
   - 使用同一个状态变量，避免不一致

## Testing Strategy

### UI测试要点

1. **重复选项测试**
   - 验证5个选项都能正确显示
   - 验证选中状态正确切换
   - 验证图标颜色根据选择正确变化

2. **提醒设置测试**
   - 验证Card背景色根据开关状态正确变化
   - 验证点击Card能切换开关
   - 验证提醒说明文字根据不同条件正确显示

3. **集成测试**
   - 验证重复和提醒设置的组合场景
   - 验证与日期选择的联动
   - 验证状态在配置变更后保持

### 测试场景矩阵

| 场景 | 日期设置 | 重复设置 | 提醒开启 | 预期提醒文字 |
|------|---------|---------|---------|-------------|
| 1 | 无 | 不重复 | 是 | "将在任务当天提醒" |
| 2 | 有 | 不重复 | 是 | "将在 [日期] 提醒" |
| 3 | 无 | 每天 | 是 | "将在每次重复时提醒" |
| 4 | 有 | 每天 | 是 | "将在每次重复时提醒" |
| 5 | - | - | 否 | 不显示文字 |

## Implementation Notes

### 布局规范

- 重复和提醒区域之间的间距：12dp
- FilterChip之间的间距：8dp
- 图标尺寸：20dp
- Card内边距：16dp
- 提醒说明文字与主内容的间距：4dp

### 动画效果

- 提醒说明文字的显示/隐藏：使用淡入淡出动画
- Card背景色变化：使用Material默认的颜色过渡动画
- FilterChip选中状态：使用Material默认的状态动画

### 可访问性

- 所有交互元素都有合适的点击区域（最小48dp）
- 图标都有contentDescription（可为null，因为有文字标签）
- 颜色对比度符合WCAG AA标准
- Switch和Card都能独立操作，提供多种交互方式

## Future Enhancements

1. **高级重复选项**
   - 添加"自定义"选项，打开详细的重复规则设置对话框
   - 支持设置重复间隔（如每2天、每3周等）
   - 支持设置重复结束条件

2. **提醒时间设置**
   - 允许用户选择具体的提醒时间
   - 支持多个提醒时间点
   - 支持提前提醒（如提前1小时、1天等）

3. **视觉增强**
   - 为不同重复类型添加专属图标
   - 添加微动画效果提升交互体验
   - 支持自定义提醒铃声选择
