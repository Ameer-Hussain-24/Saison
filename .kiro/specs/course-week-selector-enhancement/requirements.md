# 课程周数选择器增强需求文档

## 简介

本功能旨在增强课程表的周数选择功能，允许用户精确选择课程在哪些周上课，提供更灵活的课程安排方式。用户可以通过可视化的周数选择器选择具体的周数，也可以使用快捷模式（全周、单周、双周）快速配置。

## 术语表

- **CourseWeekSelector**: 课程周数选择器，用于选择课程在哪些周上课的UI组件
- **WeekSelectionMode**: 周数选择模式，包括"周数模式"和"日期模式"
- **CustomWeekPattern**: 自定义周数模式，允许用户选择任意周数组合
- **QuickSelectionMode**: 快捷选择模式，包括全周、单周、双周
- **WeekNumber**: 周数编号，表示学期中的第几周（1-based）
- **TotalWeeks**: 总周数，表示一个学期的总周数（通常为16-20周）

## 需求

### 需求 1：周数选择器UI

**用户故事：** 作为学生用户，我希望能够通过可视化的界面选择课程在哪些周上课，以便精确控制课程安排。

#### 验收标准

1. WHEN 用户点击"查看详情"按钮，THE CourseWeekSelector SHALL 显示周数选择对话框
2. THE CourseWeekSelector SHALL 以网格布局显示所有周数（1-20周）
3. THE CourseWeekSelector SHALL 使用圆形按钮展示每个周数
4. WHEN 用户点击某个周数按钮，THE CourseWeekSelector SHALL 切换该周的选中状态
5. THE CourseWeekSelector SHALL 使用填充样式显示已选中的周数
6. THE CourseWeekSelector SHALL 使用轮廓样式显示未选中的周数

### 需求 2：快捷选择模式

**用户故事：** 作为学生用户，我希望能够快速选择常见的周数模式，以便节省配置时间。

#### 验收标准

1. THE CourseWeekSelector SHALL 提供三个快捷选择按钮："全周"、"单周"、"双周"
2. WHEN 用户点击"全周"按钮，THE CourseWeekSelector SHALL 选中所有周数
3. WHEN 用户点击"单周"按钮，THE CourseWeekSelector SHALL 选中所有奇数周（1、3、5...）
4. WHEN 用户点击"双周"按钮，THE CourseWeekSelector SHALL 选中所有偶数周（2、4、6...）
5. THE CourseWeekSelector SHALL 使用填充样式显示当前激活的快捷模式
6. WHEN 用户手动修改周数选择，THE CourseWeekSelector SHALL 取消快捷模式的激活状态

### 需求 3：选择模式切换

**用户故事：** 作为学生用户，我希望能够在"周数模式"和"日期模式"之间切换，以便使用不同的方式配置课程时间。

#### 验收标准

1. THE CourseWeekSelector SHALL 在对话框顶部显示"请选择周数"和"日期模式"两个标签页
2. WHEN 用户点击"请选择周数"标签，THE CourseWeekSelector SHALL 显示周数选择界面
3. WHEN 用户点击"日期模式"标签，THE CourseWeekSelector SHALL 显示日期范围选择界面
4. THE CourseWeekSelector SHALL 使用 TabRow 组件实现模式切换
5. THE CourseWeekSelector SHALL 保持两种模式的选择状态独立

### 需求 4：自定义周数模式数据模型

**用户故事：** 作为开发者，我需要扩展数据模型以支持自定义周数选择，以便存储用户的精确周数配置。

#### 验收标准

1. THE CourseWeekSelector SHALL 扩展 Course 数据模型，添加 customWeeks 字段存储选中的周数列表
2. THE CourseWeekSelector SHALL 扩展 WeekPattern 枚举，添加 CUSTOM 选项
3. WHEN weekPattern 为 CUSTOM，THE CourseWeekSelector SHALL 使用 customWeeks 字段判断课程是否在某周上课
4. THE CourseWeekSelector SHALL 在数据库中存储 customWeeks 为 JSON 字符串
5. THE CourseWeekSelector SHALL 提供序列化和反序列化方法处理 customWeeks 数据

### 需求 5：周数显示优化

**用户故事：** 作为学生用户，我希望在课程卡片和详情页面能够清楚地看到课程的周数安排，以便快速了解课程时间。

#### 验收标准

1. WHEN 课程使用全周模式，THE CourseWeekSelector SHALL 在课程卡片显示"全周"标签
2. WHEN 课程使用单周模式，THE CourseWeekSelector SHALL 在课程卡片显示"单周"标签
3. WHEN 课程使用双周模式，THE CourseWeekSelector SHALL 在课程卡片显示"双周"标签
4. WHEN 课程使用自定义周数，THE CourseWeekSelector SHALL 在课程卡片显示"第X,Y,Z周"或"自定义"标签
5. THE CourseWeekSelector SHALL 在课程详情页面显示完整的周数列表

### 需求 6：日期模式支持

**用户故事：** 作为学生用户，我希望能够通过选择开始和结束日期来配置课程时间，以便更直观地设置课程范围。

#### 验收标准

1. WHEN 用户切换到日期模式，THE CourseWeekSelector SHALL 显示开始日期和结束日期选择器
2. THE CourseWeekSelector SHALL 使用 DatePicker 组件选择日期
3. WHEN 用户选择日期范围，THE CourseWeekSelector SHALL 根据学期开始日期自动计算对应的周数
4. THE CourseWeekSelector SHALL 在日期模式下显示"每周重复"选项
5. THE CourseWeekSelector SHALL 支持在日期模式下设置重复模式（每周、单周、双周）

### 需求 7：学期设置集成

**用户故事：** 作为学生用户，我希望能够在课程设置中配置学期的开始日期和总周数，以便周数选择器能够正确计算周数。

#### 验收标准

1. THE CourseWeekSelector SHALL 在课程设置中添加"学期开始日期"配置项
2. THE CourseWeekSelector SHALL 在课程设置中添加"学期总周数"配置项（默认18周）
3. THE CourseWeekSelector SHALL 允许用户设置 16-24 周的学期长度
4. WHEN 用户修改学期设置，THE CourseWeekSelector SHALL 更新所有周数选择器的显示范围
5. THE CourseWeekSelector SHALL 将学期设置保存到 DataStore

### 需求 8：周数验证和提示

**用户故事：** 作为学生用户，我希望系统能够验证我的周数选择，并在有问题时给出提示，以便避免配置错误。

#### 验收标准

1. WHEN 用户未选择任何周数，THE CourseWeekSelector SHALL 禁用"确定"按钮
2. WHEN 用户选择的周数超出学期范围，THE CourseWeekSelector SHALL 显示警告提示
3. THE CourseWeekSelector SHALL 在对话框底部显示已选择的周数统计（如"已选择 8 周"）
4. WHEN 用户点击"取消"按钮，THE CourseWeekSelector SHALL 恢复到打开对话框前的选择状态
5. WHEN 用户点击"确定"按钮，THE CourseWeekSelector SHALL 保存选择并关闭对话框

### 需求 9：课程过滤和显示

**用户故事：** 作为学生用户，我希望课程表能够根据当前周数只显示本周有课的课程，以便专注于当前的课程安排。

#### 验收标准

1. THE CourseWeekSelector SHALL 在课程表顶部显示当前周数（如"第 5 周"）
2. THE CourseWeekSelector SHALL 提供"显示所有课程"和"仅显示本周课程"的切换选项
3. WHEN 用户选择"仅显示本周课程"，THE CourseWeekSelector SHALL 过滤掉本周不上的课程
4. THE CourseWeekSelector SHALL 根据学期开始日期和当前日期自动计算当前周数
5. THE CourseWeekSelector SHALL 在课程卡片上标记本周是否有课

### 需求 10：数据迁移

**用户故事：** 作为开发者，我需要确保现有课程数据能够平滑迁移到新的周数选择系统，以便用户不会丢失数据。

#### 验收标准

1. THE CourseWeekSelector SHALL 提供数据库迁移脚本添加 customWeeks 字段
2. WHEN 迁移现有课程数据，THE CourseWeekSelector SHALL 保持原有的 weekPattern 设置
3. THE CourseWeekSelector SHALL 将现有的 WeekPattern 值正确映射到新系统
4. THE CourseWeekSelector SHALL 为现有课程设置合理的默认 customWeeks 值
5. THE CourseWeekSelector SHALL 确保迁移后的数据与原有行为一致

