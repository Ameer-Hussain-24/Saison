# Requirements Document

## Introduction

本文档定义了任务重复和提醒功能的UI增强需求。当前的重复和提醒设置使用简单的开关控件，用户体验不够直观和美观。本次增强将改进这两个功能的交互设计，使其更加符合Material Design 3规范，并提供更清晰的视觉反馈和功能说明。

## Glossary

- **AddTaskSheet**: 添加任务的底部弹出表单组件
- **RecurrenceRule**: 任务重复规则的数据模型
- **FilterChip**: Material Design 3的筛选芯片组件
- **Switch**: Material Design 3的开关组件
- **Card**: Material Design 3的卡片组件

## Requirements

### Requirement 1

**User Story:** 作为用户，我希望能够通过直观的选项选择任务的重复频率，以便快速设置重复任务

#### Acceptance Criteria

1. WHEN THE User 打开"更多选项"区域, THE AddTaskSheet SHALL 显示重复设置区域，包含标题"重复"和图标
2. THE AddTaskSheet SHALL 提供五个重复选项："不重复"、"每天"、"每周"、"每月"、"每年"
3. THE AddTaskSheet SHALL 使用FilterChip组件展示所有重复选项，每个选项占据相等的宽度
4. WHEN THE User 选择某个重复选项, THE AddTaskSheet SHALL 高亮显示该选项，使用primaryContainer背景色
5. WHEN THE User 选择"不重复"以外的选项, THE AddTaskSheet SHALL 将重复图标颜色改为primary色调

### Requirement 2

**User Story:** 作为用户，我希望重复规则能够基于我设置的日期或当前日期，以便重复任务从正确的时间点开始

#### Acceptance Criteria

1. WHEN THE User 已设置任务日期且选择重复选项, THE AddTaskSheet SHALL 以设置的日期作为重复基准日期
2. WHEN THE User 未设置任务日期且选择重复选项, THE AddTaskSheet SHALL 以当前日期作为重复基准日期
3. THE AddTaskSheet SHALL 在内部逻辑中正确计算基于基准日期的重复日期序列

### Requirement 3

**User Story:** 作为用户，我希望提醒设置更加美观和信息丰富，以便清楚了解提醒的触发时机

#### Acceptance Criteria

1. THE AddTaskSheet SHALL 将提醒设置显示为可点击的Card组件
2. WHEN THE User 启用提醒, THE AddTaskSheet SHALL 将Card背景色改为primaryContainer的半透明色
3. THE AddTaskSheet SHALL 在Card中显示提醒图标、标题"提醒我"和Switch开关
4. WHEN THE User 点击Card的任意区域, THE AddTaskSheet SHALL 切换提醒开关状态
5. WHEN 提醒已启用, THE AddTaskSheet SHALL 在Card中显示提醒说明文字

### Requirement 4

**User Story:** 作为用户，我希望看到智能的提醒说明文字，以便了解何时会收到提醒

#### Acceptance Criteria

1. WHEN 提醒已启用且已设置日期且未设置重复, THE AddTaskSheet SHALL 显示"将在 [具体日期] 提醒"
2. WHEN 提醒已启用且已设置重复（无论是否设置日期）, THE AddTaskSheet SHALL 显示"将在每次重复时提醒"
3. WHEN 提醒已启用且未设置日期且未设置重复, THE AddTaskSheet SHALL 显示"将在任务当天提醒"
4. THE AddTaskSheet SHALL 使用bodySmall样式和onSurfaceVariant颜色显示提醒说明文字

### Requirement 5

**User Story:** 作为用户，我希望重复和提醒设置的布局清晰美观，以便快速理解和操作

#### Acceptance Criteria

1. THE AddTaskSheet SHALL 在"更多选项"区域中按顺序显示：重复设置、提醒设置、描述输入
2. THE AddTaskSheet SHALL 在各个设置项之间使用12dp的垂直间距
3. THE AddTaskSheet SHALL 为重复和提醒的图标设置20dp的固定尺寸
4. THE AddTaskSheet SHALL 使用titleSmall样式和Medium字重显示"重复"标题
5. THE AddTaskSheet SHALL 在重复选项的FilterChip之间使用8dp的水平间距
