# 任务详情与添加页面 Material 3 增强实现计划

- [x] 1. 扩展数据模型




  - [x] 1.1 更新Task数据类


    - 添加tags字段（List<Tag>）
    - 添加attachments字段（List<Attachment>）
    - 添加recurrenceRule字段（RecurrenceRule?）
    - 添加itemType字段（ItemType，默认为TASK）
    - 确保所有字段都有合理的默认值
    - _需求: 1.1, 6.1, 7.1, 8.1, 12.1_
  
  - [x] 1.2 创建新的数据类


    - 创建Tag数据类（id, name, color）
    - 创建Attachment数据类（id, name, uri, type, size, createdAt）
    - 创建AttachmentType枚举（IMAGE, DOCUMENT, AUDIO, VIDEO, OTHER）
    - 创建RecurrenceRule数据类（pattern, interval, endDate）
    - 创建RecurrencePattern枚举（DAILY, WEEKLY, MONTHLY, YEARLY）
    - 创建ItemType枚举（TASK, MILESTONE, COUNTDOWN）
    - _需求: 6.1, 7.1, 8.1, 12.1_
  
  - [x] 1.3 更新TaskDetailUiState


    - 添加hasUnsavedChanges字段
    - 添加showDatePicker、showTimePicker、showRecurrenceDialog字段
    - 添加availableTags字段
    - 添加templates字段
    - _需求: 14.1, 14.2_

- [x] 2. 优化TaskDetailScreen整体布局


  - [x] 2.1 重构TaskDetailContent组件


    - 使用Column + verticalScroll布局
    - 设置统一的内边距（16dp）和区域间距（24dp）
    - 添加页面进入淡入动画
    - _需求: 1.1, 1.2, 1.3, 15.1_
  
  - [x] 2.2 优化TopAppBar


    - 添加保存按钮（仅在有未保存更改时显示）
    - 添加更多操作菜单（删除、分享等）
    - 实现返回时的未保存更改提示
    - _需求: 14.1, 14.3_

- [x] 3. 实现标题和描述编辑区域


  - [x] 3.1 创建TitleSection组件


    - 使用无边框TextField
    - 应用headlineSmall字体样式
    - 支持最多3行显示
    - 添加占位符提示
    - _需求: 2.1, 2.2_
  
  - [x] 3.2 优化DescriptionSection组件


    - 使用OutlinedTextField
    - 设置minLines=3, maxLines=8
    - 添加字符计数器（supportingText）
    - 限制最大字符数为500
    - _需求: 2.3, 2.4_

- [x] 4. 增强日期时间选择器


  - [x] 4.1 创建DateTimeSection组件


    - 实现快捷日期按钮（今天、明天、下周）
    - 使用OutlinedCard显示当前日期时间
    - 显示相对时间提示（"3天后"）
    - 添加清除日期按钮
    - _需求: 3.1, 3.2, 3.3, 3.4, 3.5_
  
  - [x] 4.2 集成Material 3 DatePicker

    - 使用DatePickerDialog
    - 实现日期选择后自动打开TimePicker
    - 保存选择的日期到ViewModel
    - _需求: 3.1_
  
  - [x] 4.3 集成Material 3 TimePicker

    - 使用TimePickerDialog
    - 实现时间选择确认
    - 合并日期和时间为LocalDateTime
    - _需求: 3.1_
  
  - [x] 4.4 实现相对时间格式化

    - 创建getRelativeTimeString函数
    - 支持"今天"、"明天"、"X天后"等格式
    - 支持多语言
    - _需求: 3.4_

- [x] 5. 优化优先级选择器


  - [x] 5.1 创建PrioritySection组件


    - 包装现有的PrioritySegmentedButton
    - 添加区域标题
    - 优化布局和间距
    - _需求: 4.1, 4.2_
  
  - [x] 5.2 增强PrioritySegmentedButton样式

    - 为每个优先级添加图标
    - 优化选中状态的视觉反馈
    - 添加优先级颜色映射
    - 实现平滑的动画过渡
    - _需求: 4.2, 4.3, 4.4_

- [x] 6. 实现子任务管理功能


  - [x] 6.1 创建SubtasksSection组件


    - 显示标题和完成进度（X/Y）
    - 添加LinearProgressIndicator进度条
    - 使用LazyColumn显示子任务列表
    - 添加快速添加子任务输入框
    - _需求: 5.1, 5.2, 5.5_
  
  - [x] 6.2 优化SubtaskItem组件

    - 使用Row布局（Checkbox + Text + DeleteButton）
    - 添加surfaceVariant背景色
    - 实现已完成子任务的删除线样式
    - 添加删除确认对话框
    - _需求: 5.2, 5.3_
  
  - [x] 6.3 实现子任务添加功能

    - 支持回车键快速添加
    - 添加后自动清空输入框
    - 使用滑入动画
    - 更新ViewModel中的子任务列表
    - _需求: 5.2, 5.3_
  
  - [x] 6.4 实现子任务拖拽排序

    - 集成拖拽手势检测
    - 实现拖拽时的视觉反馈
    - 保存新的排序到数据库
    - _需求: 5.4_

- [x] 7. 实现附件管理功能


  - [x] 7.1 创建AttachmentsSection组件


    - 显示标题和附件数量
    - 使用LazyVerticalGrid（3列）显示附件
    - 添加"添加附件"按钮卡片
    - _需求: 6.1, 6.2, 6.3_
  
  - [x] 7.2 创建AttachmentCard组件

    - 使用AsyncImage显示缩略图
    - 添加删除按钮（右上角）
    - 实现点击预览功能
    - 添加加载占位符和错误图标
    - _需求: 6.4, 6.5_
  
  - [x] 7.3 实现附件选择功能

    - 集成ActivityResultContracts.GetContent
    - 支持选择图片（image/*）
    - 支持选择文档（*/*）
    - 验证附件大小限制（10MB）
    - _需求: 6.2, 6.3, 16.4_
  
  - [x] 7.4 实现附件预览功能

    - 创建AttachmentPreviewDialog
    - 支持图片全屏预览
    - 支持文档打开（使用Intent）
    - 添加分享和删除操作
    - _需求: 6.5_

- [x] 8. 实现标签管理功能



  - [x] 8.1 创建TagsSection组件


    - 使用FlowRow布局显示标签
    - 使用FilterChip显示已选标签
    - 添加"添加标签"按钮
    - _需求: 7.1, 7.2, 7.3_
  
  - [x] 8.2 创建TagSelectorDialog

    - 显示可用标签列表
    - 支持多选标签
    - 添加创建新标签功能
    - 实现标签颜色选择器
    - _需求: 7.3, 7.4_
  
  - [x] 8.3 实现标签颜色系统

    - 定义预设标签颜色列表
    - 为标签应用颜色背景
    - 确保颜色对比度符合无障碍标准
    - _需求: 7.4, 17.5_

- [x] 9. 实现重复任务功能


  - [x] 9.1 创建RecurrenceSection组件


    - 使用OutlinedCard显示重复规则
    - 显示重复规则摘要
    - 显示下次重复时间预览
    - 添加清除重复规则按钮
    - _需求: 8.1, 8.2, 8.4_
  
  - [x] 9.2 创建RecurrenceDialog

    - 显示重复模式选择（每天、每周、每月、每年）
    - 实现间隔设置（+/- 按钮）
    - 添加结束日期选择
    - 实时预览下次重复时间
    - _需求: 8.2, 8.3, 8.5_
  
  - [x] 9.3 实现RecurrenceRule逻辑

    - 实现getNextOccurrence方法
    - 支持不同重复模式的计算
    - 处理结束日期逻辑
    - _需求: 8.4_

- [x] 10. 实现位置信息功能



  - [x] 10.1 优化LocationSection


    - 使用OutlinedTextField
    - 添加位置图标（LocationOn）
    - 支持手动输入位置名称
    - 添加清除位置按钮
    - _需求: 9.1, 9.2, 9.3, 9.4, 9.5_

- [x] 11. 创建快速添加任务底部表单


  - [x] 11.1 创建AddTaskSheet组件

    - 使用ModalBottomSheet容器
    - 添加拖动手柄
    - 添加项目类型选择器
    - 实现标题和输入框
    - 添加取消和添加按钮
    - _需求: 10.1, 10.2, 10.3, 10.4, 10.5_
  
  - [x] 11.2 实现快捷操作行

    - 添加日期快捷按钮（FilterChip）
    - 添加优先级快捷按钮（FilterChip）
    - 添加展开/收起按钮
    - 仅在有输入时显示
    - _需求: 10.3, 10.4_
  
  - [x] 11.3 实现展开模式

    - 添加描述输入框
    - 添加位置输入框
    - 使用AnimatedVisibility控制显示
    - 调整输入框行数
    - _需求: 10.4_
  
  - [x] 11.4 实现项目类型选择器

    - 创建ItemType枚举（TASK, MILESTONE, COUNTDOWN）
    - 创建ItemTypeSelector组件
    - 使用SingleChoiceSegmentedButtonRow
    - 为每个类型添加图标（CheckCircle, Flag, Timer）
    - 暂时只启用TASK类型，其他禁用
    - _需求: 12.1, 12.2, 12.3, 12.4, 12.5_
  
  - [x] 11.5 实现模板选择功能

    - 添加模板图标按钮
    - 创建TemplateSelector对话框
    - 显示常用任务模板列表
    - 选择模板后自动填充信息
    - _需求: 13.1, 13.2, 13.3_

- [x] 12. 实现自然语言解析



  - [x] 12.1 创建NaturalLanguageParser对象


    - 实现parse方法
    - 定义日期关键词映射（今天、明天、下周X）
    - 定义时间关键词映射（上午、中午、下午、晚上）
    - 定义优先级关键词映射（紧急、重要、普通、低）
    - _需求: 11.1, 11.2, 11.3_
  
  - [x] 12.2 实现日期解析逻辑

    - 解析相对日期（今天、明天、后天）
    - 解析星期（下周一至下周日）
    - 解析时间段（上午、下午等）
    - 从输入文本中移除已解析的关键词
    - _需求: 11.1, 11.2_
  
  - [x] 12.3 实现优先级解析逻辑

    - 识别优先级关键词
    - 支持中英文关键词
    - 从输入文本中移除已解析的关键词
    - _需求: 11.3_
  
  - [x] 12.4 集成解析到AddTaskSheet


    - 在输入变化时自动解析
    - 显示解析结果预览
    - 允许用户修改解析结果
    - _需求: 11.4, 11.5_

- [x] 13. 实现保存和自动保存功能



  - [x] 13.1 实现自动保存逻辑


    - 在TaskDetailViewModel中添加防抖逻辑
    - 检测任务更改时设置hasUnsavedChanges
    - 延迟1秒后自动保存
    - _需求: 14.2_
  
  - [x] 13.2 实现手动保存

    - 在TopAppBar添加保存按钮
    - 仅在有未保存更改时显示
    - 点击后立即保存
    - 显示保存成功提示
    - _需求: 14.1, 14.4_
  
  - [x] 13.3 实现未保存更改提示

    - 创建UnsavedChangesDialog
    - 在返回时检查hasUnsavedChanges
    - 提供保存、放弃、取消三个选项
    - _需求: 14.3_
  
  - [x] 13.4 实现保存成功指示器

    - 创建SaveSuccessIndicator组件
    - 使用scaleIn/scaleOut动画
    - 显示勾选图标和文字
    - 自动消失
    - _需求: 14.4_

- [x] 14. 实现动画和微交互


  - [x] 14.1 添加页面进入动画

    - 使用fadeIn + slideInVertically
    - 设置300ms动画时长
    - _需求: 15.1_
  
  - [x] 14.2 添加子任务动画

    - 添加时使用slideInVertically
    - 删除时使用shrinkVertically + fadeOut
    - 使用animateItemPlacement
    - _需求: 15.2_
  
  - [x] 14.3 添加附件动画

    - 删除时使用收缩动画
    - 添加时使用淡入动画
    - _需求: 15.3_
  
  - [x] 14.4 添加字段焦点动画

    - 创建AnimatedTextField组件
    - 实现焦点时的缩放效果（1.02f）
    - 使用Spring动画
    - _需求: 15.4_
  
  - [x] 14.5 优化ModalBottomSheet动画

    - 使用弹簧动画展开
    - 优化拖动手势
    - _需求: 15.5_

- [x] 15. 实现错误处理和验证


  - [x] 15.1 创建TaskValidator对象


    - 实现validateTitle方法（非空、长度限制）
    - 实现validateDueDate方法（不早于当前时间）
    - 实现validateAttachmentSize方法（10MB限制）
    - _需求: 16.1, 16.2, 16.4_
  
  - [x] 15.2 创建ErrorSnackbar组件

    - 使用errorContainer背景色
    - 添加重试按钮（可选）
    - 添加关闭按钮
    - _需求: 16.3, 16.5_
  
  - [x] 15.3 集成验证到UI

    - 在保存时验证所有字段
    - 显示验证错误提示
    - 阻止保存无效数据
    - _需求: 16.1, 16.2_

- [ ] 16. 实现响应式布局
  - [ ] 16.1 创建WindowSize检测
    - 实现rememberWindowSize函数
    - 定义COMPACT、MEDIUM、EXPANDED断点
    - _需求: 18.1, 18.2_
  
  - [ ] 16.2 实现ResponsiveTaskDetailScreen
    - COMPACT: 单列布局
    - MEDIUM: 双列布局（任务详情 + 预览）
    - EXPANDED: 对话框模式
    - _需求: 18.1, 18.2, 18.3_
  
  - [ ] 16.3 创建TaskDetailDialog
    - 使用Dialog容器
    - 设置合适的宽度和高度（80% x 90%）
    - 使用圆角Surface
    - _需求: 18.3_
  
  - [ ] 16.4 优化横屏和分屏布局
    - 调整内边距和间距
    - 优化信息展示密度
    - _需求: 18.4, 18.5_

- [x] 17. 添加无障碍支持


  - [x] 17.1 添加语义标签

    - 为所有交互元素添加contentDescription
    - 使用semantics修饰符
    - 添加stateDescription
    - 设置正确的role
    - _需求: 17.1, 17.4_
  
  - [x] 17.2 确保触摸目标尺寸

    - 验证所有按钮≥48dp
    - 调整过小的触摸目标
    - _需求: 17.2_
  
  - [x] 17.3 实现键盘导航

    - 设置正确的ImeAction
    - 实现KeyboardActions
    - 支持Tab键切换焦点
    - _需求: 17.3_
  
  - [x] 17.4 验证颜色对比度

    - 使用ColorContrastChecker验证
    - 确保所有文字对比度≥4.5:1
    - 调整不符合标准的颜色
    - _需求: 17.5_

- [x] 18. 优化性能


  - [x] 18.1 优化LazyColumn性能

    - 为items添加稳定的key
    - 设置contentType
    - 使用derivedStateOf避免重组
    - _需求: 所有需求_
  
  - [x] 18.2 优化图片加载

    - 配置Coil缓存策略
    - 设置缩略图尺寸
    - 添加占位符和错误图标
    - _需求: 6.5_
  
  - [x] 18.3 优化状态管理

    - 使用remember缓存计算结果
    - 使用derivedStateOf避免不必要的重组
    - 优化ViewModel中的Flow
    - _需求: 所有需求_

- [x] 19. 添加国际化支持


  - [x] 19.1 添加字符串资源


    - 在strings.xml中添加所有UI文字
    - 支持中文、英文、日文、越南文
    - 添加日期格式化字符串
    - _需求: 所有需求_
  
  - [x] 19.2 实现日期格式化

    - 创建formatDateTime函数
    - 支持本地化日期格式
    - 实现相对时间格式化
    - _需求: 3.4_
  
  - [x] 19.3 支持RTL布局

    - 测试阿拉伯语等RTL语言
    - 调整布局方向
    - _需求: 所有需求_

- [x] 20. 编写测试


  - [x] 20.1 编写UI测试

    - 测试TaskDetailScreen显示任务信息
    - 测试AddTaskSheet创建任务
    - 测试自然语言解析
    - 测试子任务添加和删除
    - _需求: 所有需求_
  
  - [x] 20.2 编写单元测试

    - 测试NaturalLanguageParser
    - 测试TaskValidator
    - 测试RecurrenceRule.getNextOccurrence
    - 测试日期格式化函数
    - _需求: 所有需求_

