# 课程表 M3 增强需求文档

## 简介

本功能旨在使用 Material Design 3 设计规范改进现有的课程表功能，增加灵活的课程配置选项，让用户可以自定义每天的课程节数和每节课的持续时间，提供更符合不同学校和个人需求的课程管理体验。

## 术语表

- **CourseScheduleSystem**: 课程表系统，负责管理和显示用户的课程安排
- **CourseSettings**: 课程设置，包含课程节数、课程时长等配置信息
- **CoursePeriod**: 课程节次，表示一天中的第几节课
- **PeriodDuration**: 课程时长，表示每节课的持续分钟数
- **BreakDuration**: 课间休息时长，表示课程之间的休息分钟数
- **ScheduleTemplate**: 课程表模板，预定义的课程时间配置方案
- **M3Components**: Material Design 3 组件，包括 Card、BottomSheet、SegmentedButton 等

## 需求

### 需求 1：课程节数配置

**用户故事：** 作为学生用户，我希望能够设置每天有多少节课，以便课程表能够适应我的学校课程安排。

#### 验收标准

1. WHEN 用户打开课程设置界面，THE CourseScheduleSystem SHALL 显示当前配置的每天课程节数
2. THE CourseScheduleSystem SHALL 允许用户选择每天 1 到 12 节课程
3. WHEN 用户修改课程节数，THE CourseScheduleSystem SHALL 立即更新课程表的显示结构
4. THE CourseScheduleSystem SHALL 使用 M3 Slider 组件展示课程节数选择器
5. WHEN 用户减少课程节数且存在超出新节数范围的课程，THE CourseScheduleSystem SHALL 显示警告对话框并要求用户确认

### 需求 2：课程时长配置

**用户故事：** 作为学生用户，我希望能够设置每节课的持续时间，以便系统能够自动计算课程的开始和结束时间。

#### 验收标准

1. WHEN 用户打开课程设置界面，THE CourseScheduleSystem SHALL 显示当前配置的课程时长
2. THE CourseScheduleSystem SHALL 允许用户设置 30 到 120 分钟的课程时长，以 5 分钟为步进单位
3. THE CourseScheduleSystem SHALL 允许用户设置 5 到 30 分钟的课间休息时长，以 5 分钟为步进单位
4. WHEN 用户修改课程时长，THE CourseScheduleSystem SHALL 重新计算所有基于节次的课程时间
5. THE CourseScheduleSystem SHALL 使用 M3 Slider 组件展示时长选择器

### 需求 3：课程表模板

**用户故事：** 作为学生用户，我希望能够使用预设的课程表模板，以便快速配置符合常见学校安排的课程时间。

#### 验收标准

1. THE CourseScheduleSystem SHALL 提供至少 3 种预设模板：小学模板（6节课/天，40分钟/节）、中学模板（8节课/天，45分钟/节）、大学模板（5节课/天，50分钟/节）
2. WHEN 用户选择一个模板，THE CourseScheduleSystem SHALL 应用该模板的所有配置参数
3. THE CourseScheduleSystem SHALL 使用 M3 Card 组件展示每个模板选项
4. WHEN 用户应用模板且当前存在课程数据，THE CourseScheduleSystem SHALL 显示确认对话框说明可能的影响

### 需求 4：上课时间配置

**用户故事：** 作为学生用户，我希望能够设置第一节课的开始时间，以便系统根据课程时长自动计算后续课程的时间。

#### 验收标准

1. THE CourseScheduleSystem SHALL 允许用户设置第一节课的开始时间
2. WHEN 用户设置第一节课开始时间，THE CourseScheduleSystem SHALL 根据课程时长和课间休息自动计算每节课的时间段
3. THE CourseScheduleSystem SHALL 使用 M3 TimePicker 组件展示时间选择器
4. THE CourseScheduleSystem SHALL 在课程设置界面显示所有课程节次的计算时间预览

### 需求 5：课程添加增强

**用户故事：** 作为学生用户，我希望在添加课程时可以选择课程节次而不是手动输入时间，以便更快速地创建课程。

#### 验收标准

1. WHEN 用户添加新课程，THE CourseScheduleSystem SHALL 提供"按节次选择"和"自定义时间"两种输入模式
2. WHEN 用户选择"按节次选择"模式，THE CourseScheduleSystem SHALL 显示可选的课程节次列表
3. WHEN 用户选择一个课程节次，THE CourseScheduleSystem SHALL 自动填充该节次对应的开始和结束时间
4. THE CourseScheduleSystem SHALL 使用 M3 SegmentedButton 组件切换输入模式
5. THE CourseScheduleSystem SHALL 支持选择连续的多个节次（如第3-4节）

### 需求 6：课程表视图优化

**用户故事：** 作为学生用户，我希望课程表能够清晰地显示每节课的节次信息，以便快速了解课程安排。

#### 验收标准

1. THE CourseScheduleSystem SHALL 在课程卡片上显示课程节次（如"第1节"）
2. THE CourseScheduleSystem SHALL 使用 M3 Card 组件的新设计规范展示课程信息
3. WHEN 课程跨越多个节次，THE CourseScheduleSystem SHALL 显示节次范围（如"第3-4节"）
4. THE CourseScheduleSystem SHALL 在课程表视图中按节次顺序排列课程
5. THE CourseScheduleSystem SHALL 使用视觉层次区分不同节次的课程

### 需求 7：设置持久化

**用户故事：** 作为学生用户，我希望我的课程配置能够被保存，以便下次打开应用时不需要重新设置。

#### 验收标准

1. WHEN 用户修改课程配置，THE CourseScheduleSystem SHALL 将配置保存到本地存储
2. WHEN 应用启动，THE CourseScheduleSystem SHALL 加载用户上次保存的课程配置
3. THE CourseScheduleSystem SHALL 使用 DataStore 存储课程配置数据
4. THE CourseScheduleSystem SHALL 在配置加载失败时使用默认配置（8节课/天，45分钟/节）

### 需求 8：M3 设计规范应用

**用户故事：** 作为学生用户，我希望课程表界面遵循 Material Design 3 设计规范，以便获得现代化和一致的用户体验。

#### 验收标准

1. THE CourseScheduleSystem SHALL 使用 M3 动态颜色系统展示课程颜色
2. THE CourseScheduleSystem SHALL 使用 M3 Typography 系统定义文本样式
3. THE CourseScheduleSystem SHALL 使用 M3 Elevation 和 Shadow 系统定义卡片层次
4. THE CourseScheduleSystem SHALL 使用 M3 Motion 规范实现界面过渡动画
5. THE CourseScheduleSystem SHALL 在设置界面使用 M3 BottomSheet 组件展示配置选项
