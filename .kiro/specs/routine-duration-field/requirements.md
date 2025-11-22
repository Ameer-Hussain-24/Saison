# 需求文档

## 简介

为日程（Routine）功能添加"活动时长"字段，允许用户为日程任务设置预期的持续时间。例如"每周一跑步30分钟"、"每天阅读1小时"等。这个字段是可选的，用户可以选择设置或不设置。

## 术语表

- **System**: Saison 任务管理应用
- **Routine**: 日程任务，周期性重复的活动
- **Duration**: 活动时长，表示完成该活动预期需要的时间
- **CreateRoutineSheet**: 创建日程任务的底部弹窗
- **RoutineDetailScreen**: 日程任务详情页面
- **RoutineCard**: 日程任务卡片组件

## 需求

### 需求 1

**用户故事：** 作为用户，我希望在创建日程任务时能够设置活动时长，以便记录和追踪每个活动需要花费的时间。

#### 验收标准

1. WHEN 用户打开创建日程任务的底部弹窗，THE System SHALL 显示"活动时长"输入字段
2. THE System SHALL 将"活动时长"字段设置为可选字段
3. WHEN 用户点击"活动时长"字段，THE System SHALL 显示时长选择器
4. THE System SHALL 允许用户选择小时和分钟
5. WHEN 用户保存日程任务，THE System SHALL 保存用户设置的活动时长

### 需求 2

**用户故事：** 作为用户，我希望在日程任务卡片上看到活动时长信息，以便快速了解该活动需要多长时间。

#### 验收标准

1. WHEN 日程任务设置了活动时长，THE System SHALL 在任务卡片上显示时长信息
2. THE System SHALL 使用清晰的图标和文本格式显示时长（如"30分钟"、"1小时30分钟"）
3. WHEN 日程任务未设置活动时长，THE System SHALL 不显示时长信息

### 需求 3

**用户故事：** 作为用户，我希望在日程任务详情页面能够查看和编辑活动时长，以便根据实际情况调整时间安排。

#### 验收标准

1. WHEN 用户打开日程任务详情页面，THE System SHALL 显示当前设置的活动时长
2. WHEN 用户点击编辑按钮，THE System SHALL 允许用户修改活动时长
3. WHEN 用户清空活动时长，THE System SHALL 允许保存空值
4. WHEN 用户保存修改，THE System SHALL 更新数据库中的活动时长

### 需求 4

**用户故事：** 作为用户，我希望活动时长的输入界面简单易用，以便快速设置常用的时间值。

#### 验收标准

1. THE System SHALL 提供小时和分钟的独立选择器
2. THE System SHALL 支持0-23小时的选择范围
3. THE System SHALL 支持0-59分钟的选择范围
4. THE System SHALL 提供常用时长的快捷选项（如15分钟、30分钟、1小时）
5. WHEN 用户选择0小时0分钟，THE System SHALL 视为未设置时长
