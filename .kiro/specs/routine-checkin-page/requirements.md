# 需求文档 - 日程打卡页面

## 简介

日程打卡页面是一个专注于周期性任务管理和打卡的功能模块。用户可以创建具有重复周期的任务（如每日、每周），并在周期内多次完成打卡。页面采用 Material 3 设计风格，通过视觉层次突出显示当前周期内的活跃任务和完成次数。

## 术语表

- **System**: 日程打卡系统（Routine Check-in System）
- **User**: 使用应用的用户
- **Routine Task**: 周期性任务，具有重复规则的任务
- **Check-in**: 打卡操作，标记任务在某个时间点的完成
- **Cycle**: 周期，任务重复的时间范围（如每日、每周、每月）
- **Active Task**: 活跃任务，当前处于周期内的任务
- **Inactive Task**: 非活跃任务，不在当前周期内的任务
- **Check-in Count**: 打卡次数，周期内完成任务的次数
- **Routine Card**: 日程卡片，显示周期性任务信息的 UI 组件

## 需求

### 需求 1：周期性任务创建

**用户故事：** 作为用户，我想要创建具有重复周期的任务，以便管理日常例行事项

#### 验收标准

1. THE System SHALL 提供创建周期性任务的界面
2. WHEN 用户创建任务时，THE System SHALL 允许设置任务名称、描述和图标
3. WHEN 用户创建任务时，THE System SHALL 提供周期类型选择（每日、每周、每月、自定义）
4. WHEN 用户选择每周周期时，THE System SHALL 允许选择一周中的特定日期
5. WHEN 用户选择每月周期时，THE System SHALL 允许选择月份中的特定日期
6. THE System SHALL 保存周期性任务到本地数据库

### 需求 2：任务打卡功能

**用户故事：** 作为用户，我想要对周期性任务进行打卡，以便记录任务完成情况

#### 验收标准

1. WHEN 任务处于活跃周期内时，THE System SHALL 允许用户进行打卡操作
2. WHEN 用户点击打卡按钮时，THE System SHALL 记录打卡时间戳
3. WHEN 用户在同一周期内多次打卡时，THE System SHALL 累计打卡次数
4. THE System SHALL 在任务卡片上显示当前周期的打卡次数
5. WHEN 打卡成功时，THE System SHALL 提供视觉反馈（动画或提示）
6. THE System SHALL 将打卡记录持久化到数据库

### 需求 3：日程页面显示

**用户故事：** 作为用户，我想要在日程页面查看所有周期性任务，以便了解当前需要完成的任务

#### 验收标准

1. THE System SHALL 使用 Material 3 设计规范显示日程页面
2. THE System SHALL 在页面顶部显示当前日期和星期
3. THE System SHALL 以卡片形式展示所有周期性任务
4. WHEN 任务处于活跃周期内时，THE System SHALL 以正常颜色和样式显示任务卡片
5. WHEN 任务不在活跃周期内时，THE System SHALL 以灰色样式显示任务卡片并置于底部
6. THE System SHALL 在任务卡片上突出显示打卡次数
7. THE System SHALL 按活跃状态和打卡次数对任务进行排序

### 需求 4：打卡次数可视化

**用户故事：** 作为用户，我想要清晰地看到每个任务的打卡次数，以便了解完成进度

#### 验收标准

1. THE System SHALL 在任务卡片的显著位置显示打卡次数
2. THE System SHALL 使用大号字体显示打卡次数数字
3. WHEN 打卡次数大于零时，THE System SHALL 使用主题色高亮显示次数
4. THE System SHALL 显示周期信息（如"今日"、"本周"、"本月"）
5. THE System SHALL 在卡片上显示任务名称和图标
6. WHEN 用户点击卡片时，THE System SHALL 显示详细的打卡历史记录

### 需求 5：活跃与非活跃任务区分

**用户故事：** 作为用户，我想要清楚地区分当前周期内和周期外的任务，以便专注于当前需要完成的任务

#### 验收标准

1. THE System SHALL 将活跃任务显示在列表顶部
2. THE System SHALL 将非活跃任务显示在列表底部
3. WHEN 任务为非活跃状态时，THE System SHALL 降低卡片的不透明度至 60%
4. WHEN 任务为非活跃状态时，THE System SHALL 使用灰色调显示卡片内容
5. WHEN 任务为非活跃状态时，THE System SHALL 禁用打卡按钮
6. THE System SHALL 在非活跃任务卡片上显示下次活跃时间

### 需求 6：打卡历史查看

**用户故事：** 作为用户，我想要查看任务的打卡历史，以便回顾完成情况

#### 验收标准

1. WHEN 用户点击任务卡片时，THE System SHALL 导航到详情页面
2. THE System SHALL 在详情页面显示所有打卡记录
3. THE System SHALL 按时间倒序显示打卡记录
4. THE System SHALL 显示每次打卡的日期和时间
5. THE System SHALL 按周期分组显示打卡记录
6. THE System SHALL 显示每个周期的打卡统计信息

### 需求 7：周期状态计算

**用户故事：** 作为系统，我需要准确计算任务的周期状态，以便正确显示活跃和非活跃任务

#### 验收标准

1. THE System SHALL 根据当前日期和任务周期规则计算活跃状态
2. WHEN 任务为每日周期时，THE System SHALL 在每天 00:00 重置打卡次数
3. WHEN 任务为每周周期时，THE System SHALL 根据选定的星期几判断活跃状态
4. WHEN 任务为每月周期时，THE System SHALL 根据选定的日期判断活跃状态
5. THE System SHALL 在日期变更时自动更新任务的活跃状态
6. THE System SHALL 缓存周期状态计算结果以提高性能

### 需求 8：Material 3 设计实现

**用户故事：** 作为用户，我想要使用符合 Material 3 设计规范的界面，以便获得现代化的视觉体验

#### 验收标准

1. THE System SHALL 使用 Material 3 的 Card 组件显示任务
2. THE System SHALL 使用 Material 3 的颜色系统（主题色、表面色、轮廓色）
3. THE System SHALL 使用 Material 3 的圆角规范（12dp 或 16dp）
4. THE System SHALL 使用 Material 3 的阴影和高度系统
5. THE System SHALL 使用 Material 3 的图标和字体规范
6. THE System SHALL 支持动态颜色主题
7. THE System SHALL 在深色模式下正确显示所有元素
