# 任务预览详情页面实现计划

- [x] 1. 创建 TaskPreviewViewModel


  - [x] 1.1 创建 TaskPreviewViewModel 类


    - 添加 taskId 参数和 SavedStateHandle
    - 创建 task StateFlow
    - 创建 uiState StateFlow
    - 实现 loadTask 方法
    - 实现 toggleCompletion 方法
    - 实现 deleteTask 方法
    - _需求: 14.1, 20.1_
  
  - [x] 1.2 创建 TaskPreviewUiState 密封类

    - 定义 Loading 状态
    - 定义 Success 状态
    - 定义 Error 状态（包含错误消息）
    - _需求: 20.1, 20.2, 20.3_

- [x] 2. 创建基础 UI 组件


  - [x] 2.1 创建 TaskHeader 组件

    - 显示完成状态图标
    - 显示任务标题（大号字体）
    - 已完成任务显示删除线
    - 使用不同颜色区分完成状态
    - _需求: 4.1, 4.2, 4.3, 4.4, 4.5_
  
  - [x] 2.2 创建 DescriptionCard 组件

    - 使用 Card 容器
    - 显示描述图标和标签
    - 显示描述文本（bodyLarge 字体）
    - 支持文本自动换行
    - _需求: 5.1, 5.2, 5.3, 5.4, 5.5_
  
  - [x] 2.3 创建 PriorityBadge 组件

    - 使用 AssistChip 显示优先级
    - 为不同优先级使用对应颜色
    - 添加优先级图标
    - _需求: 6.1, 6.2_
  
  - [x] 2.4 创建 DueDateInfo 组件

    - 显示截止日期和时间
    - 显示相对时间提示
    - 逾期任务使用 error 颜色高亮
    - 添加日期图标
    - _需求: 6.3, 6.4, 6.5_

- [x] 3. 创建信息展示组件

  - [x] 3.1 创建 RecurrenceInfo 组件

    - 使用 Card 容器
    - 显示重复规则图标和文字
    - 显示下次重复时间
    - 无重复规则时隐藏
    - _需求: 7.1, 7.2, 7.3, 7.4, 7.5_
  
  - [x] 3.2 创建 LocationInfo 组件

    - 使用 OutlinedCard 容器
    - 显示位置图标
    - 显示位置文本
    - 无位置信息时隐藏
    - _需求: 8.1, 8.2, 8.3, 8.4, 8.5_
  
  - [x] 3.3 创建 InfoSection 组件

    - 组合 PriorityBadge 和 DueDateInfo
    - 包含 RecurrenceInfo
    - 包含 LocationInfo
    - 使用统一的间距布局
    - _需求: 6.1, 6.2, 6.3, 6.4, 6.5, 7.1, 8.1_

- [x] 4. 创建标签和子任务组件

  - [x] 4.1 创建 TagsSection 组件

    - 使用 FlowRow 布局
    - 使用 AssistChip 显示标签
    - 为标签应用对应颜色
    - 支持标签自动换行
    - 无标签时隐藏
    - _需求: 9.1, 9.2, 9.3, 9.4, 9.5_
  
  - [x] 4.2 创建 SubtaskPreviewItem 组件

    - 显示复选框图标
    - 显示子任务文本
    - 已完成子任务显示删除线
    - 使用不同颜色区分完成状态
    - _需求: 10.5_
  
  - [x] 4.3 创建 SubtasksCard 组件

    - 使用 Card 容器
    - 显示子任务标题和完成进度
    - 显示进度条
    - 显示子任务列表
    - 无子任务时隐藏
    - _需求: 10.1, 10.2, 10.3, 10.4, 10.5_

- [x] 5. 创建附件展示组件

  - [x] 5.1 创建 AttachmentPreviewCard 组件

    - 图片类型显示缩略图
    - 文档类型显示图标和文件名
    - 使用 Card 容器
    - 支持点击预览
    - _需求: 11.1, 11.3_
  
  - [x] 5.2 创建 AttachmentsGrid 组件

    - 使用 LazyVerticalGrid（3列）
    - 显示附件标题和数量
    - 显示附件网格
    - 无附件时隐藏
    - _需求: 11.1, 11.2, 11.3, 11.4, 11.5_

- [x] 6. 创建元数据和工具函数


  - [x] 6.1 创建 MetadataFooter 组件

    - 显示创建时间
    - 显示最后修改时间
    - 使用相对时间格式
    - 使用 labelSmall 字体
    - _需求: 12.1, 12.2, 12.3, 12.4, 12.5_
  
  - [x] 6.2 实现 getRelativeTimeString 函数

    - 计算时间差
    - 返回相对时间字符串（"还有X天"、"已逾期X天"）
    - 支持多种时间单位
    - _需求: 6.4, 12.5_
  
  - [x] 6.3 实现 formatRecurrenceRule 函数

    - 格式化重复规则为可读文本
    - 支持不同重复模式
    - 支持间隔显示
    - _需求: 7.2_
  
  - [x] 6.4 实现 formatDate 函数

    - 格式化日期为"MM月dd日"
    - 支持本地化
    - _需求: 7.3_

- [x] 7. 创建 TaskPreviewScreen 主屏幕


  - [x] 7.1 实现 TaskPreviewScreen 基础结构

    - 创建 Scaffold 布局
    - 添加 TopAppBar
    - 添加可滚动内容区域
    - 添加 FloatingActionButton
    - _需求: 2.1, 2.2, 2.3, 2.4, 2.5_
  
  - [x] 7.2 实现 TopAppBar

    - 添加返回按钮
    - 显示"任务详情"标题
    - 添加编辑按钮
    - 添加更多操作菜单（分享、删除）
    - _需求: 3.1, 3.2, 3.3, 3.4, 3.5_
  
  - [x] 7.3 实现内容区域布局

    - 组合所有子组件
    - 设置统一的间距（12dp）
    - 设置内边距（16dp）
    - 使用 Column + verticalScroll
    - _需求: 2.1, 2.3, 2.4_
  
  - [x] 7.4 实现 FloatingActionButton

    - 显示切换完成状态按钮
    - 根据完成状态显示不同图标和文字
    - 点击时调用 ViewModel 方法
    - _需求: 13.1, 13.2, 13.3, 13.4, 13.5_
  
  - [x] 7.5 实现加载和错误状态

    - 加载状态显示 CircularProgressIndicator
    - 错误状态显示错误消息和重试按钮
    - 任务不存在显示提示
    - _需求: 20.1, 20.2, 20.3, 20.4, 20.5_
  
  - [x] 7.6 实现删除确认对话框

    - 创建 AlertDialog
    - 显示删除确认消息
    - 提供删除和取消按钮
    - 删除后返回上一页面
    - _需求: 3.5, 20.5_

- [x] 8. 更新导航配置


  - [x] 8.1 在 Screen 类中添加 TaskPreview 路由

    - 定义 TaskPreview 路由（"taskPreview/{taskId}"）
    - 创建 createRoute 方法
    - _需求: 14.1, 14.2_
  
  - [x] 8.2 重命名 TaskDetail 为 TaskEdit

    - 将 TaskDetail 路由重命名为 TaskEdit
    - 更新路由字符串为"taskEdit/{taskId}"
    - 保持现有功能不变
    - _需求: 14.3, 15.1, 15.2, 15.3, 15.5_
  
  - [x] 8.3 在 NavHost 中添加 TaskPreview 路由

    - 添加 composable 配置
    - 配置路由参数（taskId）
    - 实现导航回调（onNavigateBack, onNavigateToEdit）
    - _需求: 14.1, 14.4_
  
  - [x] 8.4 更新 TaskEdit 路由配置

    - 保持现有 TaskDetailScreen 组件
    - 更新路由为 TaskEdit
    - 确保所有功能正常
    - _需求: 14.3, 15.3_

- [x] 9. 更新 TaskListScreen


  - [x] 9.1 更新任务卡片点击行为

    - 点击卡片导航到 TaskPreview
    - 保持现有的 onTaskClick 回调
    - _需求: 1.1, 14.4_
  
  - [x] 9.2 更新侧滑编辑行为

    - 侧滑编辑按钮导航到 TaskEdit
    - 更新按钮文字为"编辑"
    - 保持现有的侧滑功能
    - _需求: 1.2, 1.3, 1.4, 14.5_

- [x] 10. 更新 TaskDetailScreen 标题


  - [x] 10.1 更新 TopAppBar 标题

    - 将标题改为"任务编辑"
    - 使用字符串资源
    - _需求: 15.1, 15.2_
  
  - [x] 10.2 保持所有编辑功能不变

    - 确保所有输入框正常工作
    - 确保保存功能正常
    - 确保所有现有功能不受影响
    - _需求: 15.2, 15.3, 15.5_

- [x] 11. 添加动画效果

  - [x] 11.1 实现页面进入动画

    - 使用 fadeIn + slideInVertically
    - 设置 300ms 动画时长
    - 在 NavHost 中配置 enterTransition
    - _需求: 17.1, 17.5_
  
  - [x] 11.2 实现完成状态切换动画

    - 使用 AnimatedContent
    - 使用 scaleIn/scaleOut 动画
    - 应用到 FAB 图标
    - _需求: 17.2_
  
  - [x] 11.3 实现导航过渡动画

    - 配置 exitTransition
    - 使用 fadeOut 动画
    - _需求: 17.3, 17.4_

- [x] 12. 实现响应式布局

  - [x] 12.1 创建 WindowSize 枚举和检测函数

    - 定义 Compact、Medium、Expanded
    - 实现 rememberWindowSize 函数
    - 根据屏幕宽度返回窗口尺寸
    - _需求: 16.1, 16.2_
  
  - [x] 12.2 实现响应式 TaskPreviewScreen

    - Compact: 全屏显示
    - Medium/Expanded: 对话框模式
    - 调整布局和间距
    - _需求: 16.2, 16.3, 16.4, 16.5_

- [x] 13. 添加无障碍支持

  - [x] 13.1 添加语义标签

    - 为所有图标添加 contentDescription
    - 使用 semantics 修饰符
    - 添加 stateDescription
    - 设置正确的 role
    - _需求: 18.1, 18.4_
  
  - [x] 13.2 确保触摸目标尺寸

    - 验证所有按钮≥48dp
    - 调整过小的触摸目标
    - _需求: 18.2_
  
  - [x] 13.3 支持键盘导航

    - 设置正确的 focusable
    - 支持 Tab 键切换焦点
    - _需求: 18.3_
  
  - [x] 13.4 验证颜色对比度

    - 使用 ColorContrastChecker 验证
    - 确保所有文字对比度≥4.5:1
    - 调整不符合标准的颜色
    - _需求: 18.5_

- [x] 14. 性能优化

  - [x] 14.1 优化状态管理

    - 使用 derivedStateOf 计算进度
    - 避免不必要的重组
    - 优化 ViewModel 中的 Flow
    - _需求: 19.1, 19.4, 19.5_
  
  - [x] 14.2 优化图片加载

    - 配置 Coil 缓存策略
    - 设置缩略图尺寸限制
    - 添加占位符和错误图标
    - _需求: 19.3_
  
  - [x] 14.3 优化列表性能

    - 为 LazyVerticalGrid 添加稳定的 key
    - 使用 contentType
    - _需求: 19.2_

- [x] 15. 添加国际化支持


  - [x] 15.1 添加字符串资源

    - 在 strings.xml 中添加所有 UI 文字
    - 支持中文、英文、日文、越南文
    - 添加相对时间格式化字符串
    - _需求: 所有需求_
  
  - [x] 15.2 实现日期格式化

    - 支持本地化日期格式
    - 实现相对时间格式化
    - _需求: 6.4, 12.5_

- [x] 16. 编写测试


  - [x] 16.1 编写 UI 测试

    - 测试 TaskPreviewScreen 显示任务信息
    - 测试切换完成状态
    - 测试导航到编辑页面
    - 测试删除任务
    - _需求: 所有需求_
  
  - [x] 16.2 编写单元测试

    - 测试 TaskPreviewViewModel.loadTask
    - 测试 TaskPreviewViewModel.toggleCompletion
    - 测试 TaskPreviewViewModel.deleteTask
    - 测试工具函数（getRelativeTimeString 等）
    - _需求: 所有需求_
