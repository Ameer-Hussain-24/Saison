# 任务详情与添加页面 Material 3 增强需求文档

## 简介

本文档定义了对Saison应用任务详情页面和添加任务功能的Material 3设计增强需求。目标是打造一个优雅、直观、功能完善的任务编辑界面，提升用户在创建和编辑任务时的体验，使其符合Material 3设计规范。

## 术语表

- **TaskDetailScreen**: 任务详情/编辑屏幕组件
- **AddTaskSheet**: 快速添加任务底部表单组件
- **M3**: Material Design 3设计规范
- **ModalBottomSheet**: Material 3模态底部表单
- **DateTimePicker**: 日期时间选择器
- **AttachmentCard**: 附件卡片组件
- **SubtaskItem**: 子任务项组件
- **RichTextEditor**: 富文本编辑器

## 需求

### 需求 1: 任务详情页面视觉优化

**用户故事:** 作为用户，我希望任务详情页面具有清晰的视觉层次和优雅的设计，以便舒适地查看和编辑任务信息

#### 验收标准

1. THE TaskDetailScreen SHALL 使用Material 3的表面容器系统组织不同信息区块
2. THE TaskDetailScreen SHALL 为每个编辑区域使用OutlinedCard或FilledCard容器
3. THE TaskDetailScreen SHALL 使用16dp的统一内边距和8dp的组件间距
4. THE TaskDetailScreen SHALL 在顶部显示任务完成状态的大型复选框
5. THE TaskDetailScreen SHALL 使用渐进式信息展示，重要信息在上方

### 需求 2: 标题和描述编辑优化

**用户故事:** 作为用户，我希望能够流畅地编辑任务标题和描述，以便快速记录任务信息

#### 验收标准

1. THE TaskDetailScreen SHALL 使用大号TextField显示任务标题，支持多行输入
2. THE TaskDetailScreen SHALL 为标题TextField使用headlineSmall字体样式
3. THE TaskDetailScreen SHALL 使用OutlinedTextField显示任务描述，支持最少3行输入
4. THE TaskDetailScreen SHALL 在描述框中显示字符计数器
5. WHEN 用户输入时，THE TaskDetailScreen SHALL 自动保存草稿到本地

### 需求 3: 日期时间选择器增强

**用户故事:** 作为用户，我希望能够方便地设置任务的截止日期和提醒时间，以便更好地管理时间

#### 验收标准

1. THE TaskDetailScreen SHALL 使用Material 3 DatePicker组件选择日期
2. THE TaskDetailScreen SHALL 使用Material 3 TimePicker组件选择时间
3. THE TaskDetailScreen SHALL 提供快捷日期选项（今天、明天、下周、自定义）
4. WHEN 用户选择日期时，THE TaskDetailScreen SHALL 显示相对时间提示（例如"3天后"）
5. THE TaskDetailScreen SHALL 支持清除日期和时间的操作

### 需求 4: 优先级选择优化

**用户故事:** 作为用户，我希望能够直观地设置任务优先级，以便快速标识重要任务

#### 验收标准

1. THE TaskDetailScreen SHALL 使用SegmentedButton组件显示优先级选项
2. THE TaskDetailScreen SHALL 为每个优先级使用对应的颜色和图标
3. THE TaskDetailScreen SHALL 在选中时显示填充样式的按钮
4. THE TaskDetailScreen SHALL 使用平滑的动画过渡优先级变化
5. THE TaskDetailScreen SHALL 在优先级区域显示标签文字

### 需求 5: 子任务管理增强

**用户故事:** 作为用户，我希望能够方便地添加和管理子任务，以便将复杂任务分解为小步骤

#### 验收标准

1. THE TaskDetailScreen SHALL 在独立区域显示子任务列表
2. THE TaskDetailScreen SHALL 提供快速添加子任务的输入框
3. WHEN 用户按回车键时，THE TaskDetailScreen SHALL 自动添加子任务并清空输入框
4. THE TaskDetailScreen SHALL 支持拖拽排序子任务
5. THE TaskDetailScreen SHALL 显示子任务完成进度条

### 需求 6: 附件管理功能

**用户故事:** 作为用户，我希望能够为任务添加附件，以便保存相关文档和图片

#### 验收标准

1. THE TaskDetailScreen SHALL 提供添加附件的按钮
2. THE TaskDetailScreen SHALL 支持从相册选择图片
3. THE TaskDetailScreen SHALL 支持从文件管理器选择文档
4. THE TaskDetailScreen SHALL 使用卡片形式显示附件缩略图
5. THE TaskDetailScreen SHALL 支持删除和预览附件

### 需求 7: 标签和分类管理

**用户故事:** 作为用户，我希望能够为任务添加标签和分类，以便更好地组织任务

#### 验收标准

1. THE TaskDetailScreen SHALL 提供标签选择器
2. THE TaskDetailScreen SHALL 使用FilterChip显示已选标签
3. THE TaskDetailScreen SHALL 支持创建新标签
4. THE TaskDetailScreen SHALL 使用不同颜色区分标签
5. THE TaskDetailScreen SHALL 支持多选标签

### 需求 8: 重复任务设置

**用户故事:** 作为用户，我希望能够设置任务的重复规则，以便自动创建周期性任务

#### 验收标准

1. THE TaskDetailScreen SHALL 提供重复规则选择器
2. THE TaskDetailScreen SHALL 支持常用重复模式（每天、每周、每月）
3. THE TaskDetailScreen SHALL 支持自定义重复规则
4. THE TaskDetailScreen SHALL 显示下次重复时间预览
5. THE TaskDetailScreen SHALL 支持设置重复结束日期

### 需求 9: 位置信息功能

**用户故事:** 作为用户，我希望能够为任务添加位置信息，以便基于地点管理任务

#### 验收标准

1. THE TaskDetailScreen SHALL 提供位置输入框
2. THE TaskDetailScreen SHALL 支持手动输入位置名称
3. THE TaskDetailScreen SHALL 显示位置图标和文字
4. THE TaskDetailScreen SHALL 支持清除位置信息
5. THE TaskDetailScreen SHALL 在位置区域使用OutlinedCard容器

### 需求 10: 快速添加任务底部表单

**用户故事:** 作为用户，我希望能够通过简洁的底部表单快速添加任务，以便提高创建效率

#### 验收标准

1. THE AddTaskSheet SHALL 使用ModalBottomSheet组件
2. THE AddTaskSheet SHALL 在顶部显示拖动手柄和项目类型选择器
3. THE AddTaskSheet SHALL 提供标题输入框和快捷操作按钮
4. THE AddTaskSheet SHALL 支持展开为完整编辑模式
5. THE AddTaskSheet SHALL 在添加成功后自动关闭

### 需求 11: 自然语言解析

**用户故事:** 作为用户，我希望能够使用自然语言输入任务，系统自动识别日期和优先级，以便快速创建任务

#### 验收标准

1. THE AddTaskSheet SHALL 解析输入文本中的日期关键词（今天、明天、下周一）
2. THE AddTaskSheet SHALL 解析输入文本中的时间关键词（上午、下午、晚上）
3. THE AddTaskSheet SHALL 解析输入文本中的优先级关键词（重要、紧急）
4. THE AddTaskSheet SHALL 在解析后显示识别结果的预览
5. THE AddTaskSheet SHALL 允许用户修改自动识别的信息

### 需求 12: 项目类型选择

**用户故事:** 作为用户，我希望在创建时能够选择项目类型（任务、里程碑、倒计时），以便创建不同类型的待办项

#### 验收标准

1. THE AddTaskSheet SHALL 在顶部显示项目类型选择器
2. THE AddTaskSheet SHALL 提供三种类型选项：任务、里程碑、倒计时
3. THE AddTaskSheet SHALL 默认选中"任务"类型
4. WHEN 用户选择不同类型时，THE AddTaskSheet SHALL 使用SegmentedButton显示选中状态
5. THE AddTaskSheet SHALL 为未来功能预留里程碑和倒计时的UI接口

### 需求 13: 任务模板功能

**用户故事:** 作为用户，我希望能够使用任务模板快速创建常用任务，以便节省重复输入时间

#### 验收标准

1. THE AddTaskSheet SHALL 提供模板选择按钮
2. THE AddTaskSheet SHALL 显示常用任务模板列表
3. WHEN 用户选择模板时，THE AddTaskSheet SHALL 自动填充任务信息
4. THE TaskDetailScreen SHALL 支持将当前任务保存为模板
5. THE AddTaskSheet SHALL 支持编辑和删除模板

### 需求 14: 保存和取消操作优化

**用户故事:** 作为用户，我希望编辑操作有明确的保存和取消反馈，以便避免意外丢失数据

#### 验收标准

1. THE TaskDetailScreen SHALL 在检测到更改时显示保存按钮
2. THE TaskDetailScreen SHALL 支持自动保存功能
3. WHEN 用户点击返回时，THE TaskDetailScreen SHALL 提示保存未保存的更改
4. THE TaskDetailScreen SHALL 使用Snackbar显示保存成功提示
5. THE TaskDetailScreen SHALL 提供撤销更改的选项

### 需求 15: 动画和微交互

**用户故事:** 作为用户，我希望界面操作具有流畅的动画反馈，以便获得愉悦的使用体验

#### 验收标准

1. WHEN 任务详情页面打开时，THE TaskDetailScreen SHALL 使用淡入动画
2. WHEN 添加子任务时，THE SubtaskItem SHALL 使用滑入动画
3. WHEN 删除附件时，THE AttachmentCard SHALL 使用收缩动画
4. THE TaskDetailScreen SHALL 在字段获得焦点时显示高亮动画
5. THE AddTaskSheet SHALL 使用弹簧动画展开和收起

### 需求 16: 错误处理和验证

**用户故事:** 作为用户，我希望在输入错误时得到清晰的提示，以便正确填写任务信息

#### 验收标准

1. WHEN 标题为空时，THE TaskDetailScreen SHALL 显示错误提示
2. WHEN 日期早于当前时间时，THE TaskDetailScreen SHALL 显示警告
3. THE TaskDetailScreen SHALL 在保存失败时显示错误Snackbar
4. THE TaskDetailScreen SHALL 验证附件大小限制
5. THE TaskDetailScreen SHALL 在网络错误时提供重试选项

### 需求 17: 无障碍支持

**用户故事:** 作为视障用户，我希望能够使用屏幕阅读器操作任务详情页面，以便无障碍地管理任务

#### 验收标准

1. THE TaskDetailScreen SHALL 为所有交互元素提供contentDescription
2. THE TaskDetailScreen SHALL 确保所有触摸目标≥48dp
3. THE TaskDetailScreen SHALL 支持键盘导航
4. THE TaskDetailScreen SHALL 使用语义化的标签和角色
5. THE TaskDetailScreen SHALL 确保颜色对比度符合WCAG标准

### 需求 18: 响应式布局

**用户故事:** 作为用户，我希望在不同屏幕尺寸上都能获得良好的编辑体验

#### 验收标准

1. WHEN 屏幕宽度大于600dp时，THE TaskDetailScreen SHALL 使用双列布局
2. WHEN 屏幕宽度大于840dp时，THE TaskDetailScreen SHALL 显示侧边栏预览
3. THE AddTaskSheet SHALL 在平板上使用对话框替代底部表单
4. THE TaskDetailScreen SHALL 在横屏模式下优化布局
5. THE TaskDetailScreen SHALL 支持分屏模式

