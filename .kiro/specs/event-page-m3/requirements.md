# 需求文档 - 事件页面

## 简介

本功能为 Saison 应用添加一个专门的"事件"页面，采用 Material 3 设计风格。用户可以查看所有事件，创建新事件，并通过时间计算功能查看事件距离当前的天数（未来事件显示"还有XXX天"，过去事件显示"已过去XXX天"）。

## 术语表

- **EventScreen**: 事件列表页面，显示所有类型为 EVENT 的项目
- **EventItem**: 单个事件项，包含标题、日期和时间计算信息
- **CreateEventButton**: 浮动操作按钮（FAB），用于创建新事件
- **EventCreationSheet**: 创建事件的底部表单
- **TimeInputMethod**: 时间输入方式，包括手动输入和日期选择器
- **DayCalculator**: 计算事件与当前日期之间天数差的工具
- **EventCategory**: 事件类别，包括生日、纪念日、倒数日等类型

## 需求

### 需求 1：事件列表页面

**用户故事：** 作为用户，我希望有一个专门的事件页面，以便我可以集中查看和管理所有事件

#### 验收标准

1. THE EventScreen SHALL 显示所有 itemType 为 EVENT 的项目
2. WHEN 用户打开事件页面时，THE EventScreen SHALL 按照事件日期排序显示事件列表
3. THE EventScreen SHALL 使用 Material 3 设计规范，包括适当的间距、圆角和颜色方案
4. WHEN 事件列表为空时，THE EventScreen SHALL 显示空状态提示信息
5. THE EventScreen SHALL 在右下角显示创建事件的浮动操作按钮

### 需求 2：事件项显示

**用户故事：** 作为用户，我希望每个事件都能清晰显示时间信息和距离当前的天数，以便我可以快速了解事件的时间状态

#### 验收标准

1. THE EventItem SHALL 显示事件标题、日期和时间
2. WHEN 事件日期在未来时，THE EventItem SHALL 显示"还有 X 天"的文本
3. WHEN 事件日期在过去时，THE EventItem SHALL 显示"已过去 X 天"的文本
4. WHEN 事件日期是今天时，THE EventItem SHALL 显示"今天"的文本
5. THE EventItem SHALL 使用 Material 3 Card 组件，具有适当的阴影和圆角
6. THE EventItem SHALL 使用不同的颜色或样式区分未来事件和过去事件

### 需求 3：创建事件按钮

**用户故事：** 作为用户，我希望能够通过明显的按钮快速创建新事件，以便我可以方便地添加事件

#### 验收标准

1. THE CreateEventButton SHALL 位于屏幕右下角
2. THE CreateEventButton SHALL 使用 Material 3 FloatingActionButton 组件
3. THE CreateEventButton SHALL 显示"添加"图标（Icons.Default.Add）
4. WHEN 用户点击创建事件按钮时，THE EventScreen SHALL 打开事件创建表单
5. THE CreateEventButton SHALL 在用户滚动列表时保持可见

### 需求 4：事件创建表单

**用户故事：** 作为用户，我希望能够通过简单的表单创建事件并设定时间，以便我可以记录重要的日期

#### 验收标准

1. THE EventCreationSheet SHALL 使用 ModalBottomSheet 组件显示
2. THE EventCreationSheet SHALL 包含事件标题输入字段
3. THE EventCreationSheet SHALL 包含事件描述输入字段（可选）
4. THE EventCreationSheet SHALL 包含日期和时间选择功能
5. THE EventCreationSheet SHALL 提供"保存"和"取消"按钮
6. WHEN 用户点击保存按钮且标题和日期已填写时，THE EventCreationSheet SHALL 创建新事件并关闭表单
7. WHEN 用户点击取消按钮时，THE EventCreationSheet SHALL 关闭表单而不保存
8. THE EventCreationSheet SHALL 使用 Material 3 设计规范

### 需求 5：时间输入方式

**用户故事：** 作为用户，我希望能够通过多种方式输入事件时间，以便我可以灵活地设定日期

#### 验收标准

1. THE TimeInputMethod SHALL 支持通过日期选择器选择日期
2. THE TimeInputMethod SHALL 支持通过时间选择器选择具体时间
3. THE TimeInputMethod SHALL 支持手动输入日期和时间
4. WHEN 用户选择日期时，THE EventCreationSheet SHALL 使用 Material 3 DatePicker 组件
5. WHEN 用户选择时间时，THE EventCreationSheet SHALL 使用 Material 3 TimePicker 组件
6. THE TimeInputMethod SHALL 验证输入的日期和时间格式是否正确

### 需求 6：天数计算

**用户故事：** 作为用户，我希望系统能够自动计算事件距离当前的天数，以便我可以直观地了解时间距离

#### 验收标准

1. THE DayCalculator SHALL 计算事件日期与当前日期之间的天数差
2. WHEN 事件日期在未来时，THE DayCalculator SHALL 返回正数天数
3. WHEN 事件日期在过去时，THE DayCalculator SHALL 返回负数天数
4. WHEN 事件日期是今天时，THE DayCalculator SHALL 返回零
5. THE DayCalculator SHALL 只计算日期差异，忽略具体时间
6. THE DayCalculator SHALL 在每次显示事件列表时重新计算天数

### 需求 7：事件编辑和删除

**用户故事：** 作为用户，我希望能够编辑和删除已创建的事件，以便我可以管理事件信息

#### 验收标准

1. WHEN 用户点击事件项时，THE EventScreen SHALL 打开事件详情页面
2. THE EventScreen SHALL 在事件详情页面提供编辑功能
3. THE EventScreen SHALL 在事件详情页面提供删除功能
4. WHEN 用户编辑事件时，THE EventScreen SHALL 使用与创建事件相同的表单
5. WHEN 用户删除事件时，THE EventScreen SHALL 显示确认对话框
6. WHEN 用户确认删除时，THE EventScreen SHALL 从列表中移除该事件

### 需求 8：事件类别

**用户故事：** 作为用户，我希望能够为事件设置类别（生日、纪念日、倒数日），以便我可以更好地组织和识别不同类型的事件

#### 验收标准

1. THE EventCreationSheet SHALL 提供事件类别选择功能
2. THE EventCategory SHALL 包括"生日"、"纪念日"和"倒数日"三种类型
3. THE EventItem SHALL 显示事件类别的图标或标签
4. THE EventScreen SHALL 支持按事件类别筛选显示
5. WHEN 事件类别为"生日"时，THE EventItem SHALL 显示生日图标
6. WHEN 事件类别为"纪念日"时，THE EventItem SHALL 显示纪念日图标
7. WHEN 事件类别为"倒数日"时，THE EventItem SHALL 显示倒数日图标

### 需求 9：国际化支持

**用户故事：** 作为用户，我希望事件页面支持多语言，以便我可以使用我熟悉的语言

#### 验收标准

1. THE EventScreen SHALL 支持中文、英文、日文和越南文
2. THE EventScreen SHALL 使用字符串资源文件存储所有文本
3. THE DayCalculator SHALL 根据当前语言环境显示"还有 X 天"或"已过去 X 天"
4. THE EventScreen SHALL 根据当前语言环境格式化日期和时间显示
5. THE EventCreationSheet SHALL 使用本地化的日期和时间选择器
6. THE EventCategory SHALL 根据当前语言环境显示本地化的类别名称
