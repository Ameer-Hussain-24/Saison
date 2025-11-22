# 实现计划

- [x] 1. 扩展 PreferencesManager 支持新的设置项





  - 添加通知设置相关的 Keys 和函数（notificationsEnabled, taskRemindersEnabled, courseRemindersEnabled, pomodoroRemindersEnabled）
  - 添加同步设置相关的 Keys 和函数（syncOnWifiOnly）
  - 为所有新增设置提供 Flow 和 suspend 函数
  - _需求: 1.1, 11.1, 11.2_

- [x] 2. 实现完整的 SettingsViewModel


  - [x] 2.1 定义 UI 状态和事件类（SettingsUiState, SettingsUiEvent, SyncStatus, NotificationSettings）


    - 创建 sealed class SettingsUiState（Idle, Loading, Success, Error）
    - 创建 sealed class SettingsUiEvent（ShowSnackbar, ShowError, NavigateToSystemSettings, RestartRequired）
    - 创建 data class SyncStatus 和 NotificationSettings
    - _需求: 1.3, 1.4_
  
  - [x] 2.2 实现所有状态 Flow 的初始化和订阅


    - 从 PreferencesManager 订阅主题、语言、通知、同步等设置
    - 使用 stateIn 转换为 StateFlow
    - 实现 SyncStatus 的状态管理
    - _需求: 1.1, 1.3_
  
  - [x] 2.3 实现设置更新函数


    - 实现主题、深色模式、动态颜色设置函数
    - 实现语言设置函数
    - 实现通知设置函数（总开关和各项提醒）
    - 实现同步设置函数（自动同步、仅 WiFi）
    - 实现 WebDAV 配置函数
    - 实现番茄钟和节拍器设置函数
    - 每个函数都要处理错误并更新 UI 状态
    - _需求: 1.2, 1.4, 11.1_
  
  - [x] 2.4 实现 WebDAV 连接测试功能


    - 创建 testWebDavConnection 函数
    - 调用 SyncRepository 测试连接
    - 处理测试结果并发出相应的 UI 事件
    - _需求: 6.4, 6.5_
  
  - [x] 2.5 实现手动同步触发功能


    - 创建 triggerManualSync 函数
    - 更新 SyncStatus 状态
    - 调用 SyncRepository 执行同步
    - 处理同步结果
    - _需求: 8.5, 8.6_

- [x] 3. 创建可复用的设置 UI 组件


  - [x] 3.1 实现 SettingsSection 组件

    - 使用 Column 布局
    - 应用 M3 Typography.titleSmall 样式
    - 使用 primary 颜色
    - 设置适当的内边距（horizontal: 16dp, vertical: 8dp）
    - _需求: 2.2, 3.2_

  

  - [x] 3.2 实现 SettingsItem 组件
    - 使用 M3 ListItem 组件
    - 支持图标、标题、副标题
    - 右侧显示 ChevronRight 图标
    - 添加点击涟漪效果
    - 最小高度 56dp
    - 提供 contentDescription 支持无障碍
    - _需求: 2.3, 2.8, 10.1, 10.3_
  
  - [x] 3.3 实现 SettingsSwitchItem 组件
    - 使用 M3 ListItem 组件
    - 支持图标、标题、副标题
    - 右侧显示 Switch 组件
    - 支持启用/禁用状态
    - 添加状态变化动画（150ms）
    - 提供语义化标签
    - _需求: 2.4, 9.6, 10.2_
  
  - [x] 3.4 实现 SettingsSliderItem 组件


    - 使用 M3 Slider 组件
    - 显示当前值标签
    - 支持自定义值范围
    - 支持自定义值格式化
    - 实时更新值
    - _需求: 2.4_

- [x] 4. 实现主题选择功能



  - [x] 4.1 创建 ThemePreviewCard 组件

    - 显示主题名称
    - 显示主题颜色预览条（primary, secondary, tertiary）
    - 选中时显示边框高亮
    - 添加点击涟漪效果
    - _需求: 4.2_
  


  - [x] 4.2 实现 ThemeSelectionDialog 对话框

    - 使用 M3 AlertDialog
    - 使用 LazyColumn 显示所有主题选项
    - 每个主题使用 ThemePreviewCard 显示
    - 使用 RadioButton 标识当前选中主题
    - Android 12+ 显示动态颜色选项
    - 点击主题立即应用并关闭对话框
    - 添加淡入淡出动画（200ms）
    - _需求: 4.1, 4.3, 4.4, 4.5, 9.5_
  

  - [x] 4.3 在 SettingsScreen 中集成主题选择


    - 添加主题设置项
    - 显示当前主题名称
    - 点击打开 ThemeSelectionDialog
    - 观察主题变化并更新显示
    - _需求: 4.6_

- [x] 5. 实现语言选择功能
  - [x] 5.1 实现 LanguageSelectionDialog 对话框

    - 使用 M3 AlertDialog
    - 显示四种语言选项（简体中文、English、日本語、Tiếng Việt）
    - 使用 RadioButton 标识当前选中语言
    - 点击语言更新设置
    - _需求: 5.1, 5.2, 5.4_
  

  - [x] 5.2 在 SettingsScreen 中集成语言选择
    - 添加语言设置项
    - 显示当前语言的本地化名称
    - 点击打开 LanguageSelectionDialog
    - 语言更改后显示重启提示 Snackbar
    - _需求: 5.3, 5.5_

- [x] 6. 实现 WebDAV 配置功能


  - [x] 6.1 实现 WebDavConfigDialog 对话框


    - 使用 M3 AlertDialog
    - 添加三个 OutlinedTextField（服务器地址、用户名、密码）
    - 密码字段使用密码输入类型
    - 添加显示/隐藏密码的 IconButton
    - 实现服务器地址格式验证（URL 格式）
    - 服务器地址或用户名为空时禁用保存按钮
    - 添加测试连接按钮
    - 显示测试结果（成功/失败消息）
    - _需求: 6.1, 6.2, 6.3, 6.4, 6.5, 6.6, 6.7_
  
  - [x] 6.2 在 SettingsScreen 中集成 WebDAV 配置

    - 添加 WebDAV 配置设置项
    - 显示配置状态（已配置/未配置）
    - 点击打开 WebDavConfigDialog
    - 保存成功后显示确认 Snackbar
    - _需求: 6.1_

- [x] 7. 实现通知设置功能


  - [x] 7.1 在 SettingsScreen 中添加通知设置分组


    - 添加总通知开关
    - 添加任务提醒开关
    - 添加课程提醒开关
    - 添加番茄钟提醒开关
    - 总开关关闭时禁用具体通知选项
    - _需求: 7.1, 7.2_
  
  - [x] 7.2 实现通知权限请求逻辑

    - 首次启用通知时请求系统权限（Android 13+）
    - 权限被拒绝时显示引导对话框
    - 提供跳转到系统设置的按钮
    - _需求: 7.3, 7.4_

- [x] 8. 实现数据同步设置功能



  - [x] 8.1 在 SettingsScreen 中添加同步设置分组

    - 添加自动同步开关
    - 添加仅 WiFi 同步开关
    - 显示上次同步时间
    - 显示同步状态（同步中/成功/失败）
    - 添加立即同步按钮
    - _需求: 8.1, 8.2, 8.4_
  
  - [x] 8.2 实现手动同步功能

    - 点击立即同步按钮触发同步
    - 显示同步进度指示器（CircularProgressIndicator）
    - 同步完成后更新状态和时间
    - 同步失败时显示错误信息
    - _需求: 8.5, 8.6_

- [x] 9. 实现番茄钟和节拍器设置
  - [x] 9.1 实现 PomodoroSettingsDialog 对话框
    - 使用 M3 AlertDialog
    - 添加三个 Slider（工作时长、短休息、长休息）
    - 显示当前值（分钟）
    - 值范围：工作 15-60 分钟，短休息 3-15 分钟，长休息 10-30 分钟
    - 实时预览时间
    - _需求: 需求文档未明确要求，但设计文档中包含_

  
  - [x] 9.2 实现 MetronomeSettingsDialog 对话框
    - 使用 M3 AlertDialog
    - 添加 BPM Slider（40-240）
    - 添加声音选择下拉菜单（ExposedDropdownMenuBox）
    - 添加预览按钮播放节拍声音
    - _需求: 需求文档未明确要求，但设计文档中包含_
  
  - [x] 9.3 在 SettingsScreen 中集成番茄钟和节拍器设置
    - 添加番茄钟设置分组和设置项
    - 添加节拍器设置分组和设置项
    - 点击打开对应的设置对话框
    - 显示当前配置值

- [x] 10. 实现关于页面

  - [x] 10.1 实现 AboutDialog 对话框

    - 使用 M3 AlertDialog
    - 显示应用图标（使用 Image 组件）
    - 显示应用名称（使用 Typography.headlineSmall）
    - 显示版本号
    - 显示简短描述
    - 显示版权信息
    - 添加开源许可链接（TextButton）
    - 添加反馈链接（TextButton）
    - _需求: 12.1, 12.2, 12.3, 12.4, 12.5, 12.6_
  

  - [x] 10.2 在 SettingsScreen 中集成关于页面



    - 添加关于分组
    - 添加"关于 Saison"设置项
    - 添加"开源许可"设置项
    - 点击打开 AboutDialog
    - _需求: 12.1_

- [x] 11. 完善 SettingsScreen 主界面

  - [x] 11.1 实现 Scaffold 和 TopAppBar

    - 使用 M3 Scaffold 组件
    - 使用 M3 TopAppBar 显示标题
    - 添加返回按钮（IconButton + ArrowBack）
    - 设置透明状态栏
    - _需求: 2.1, 2.2_
  
  - [x] 11.2 实现设置列表布局

    - 使用 Column + verticalScroll
    - 按分组组织设置项（外观、语言、通知、同步、番茄钟、节拍器、关于）
    - 使用 Divider 分隔不同分组
    - 应用适当的内边距
    - _需求: 3.1, 3.3_
  
  - [x] 11.3 实现外观设置分组

    - 添加主题选择项
    - 添加深色模式开关
    - 添加动态颜色开关（Android 12+ 显示）
    - _需求: 3.4_
  
  - [x] 11.4 实现所有对话框的状态管理

    - 使用 remember { mutableStateOf(false) } 管理对话框显示状态
    - 实现对话框的打开和关闭逻辑
    - _需求: 2.1_

- [x] 12. 实现交互反馈和动画



  - [x] 12.1 添加 Snackbar 支持

    - 在 Scaffold 中添加 SnackbarHost
    - 创建 SnackbarHostState
    - 观察 ViewModel 的 uiEvent 并显示 Snackbar
    - 成功操作显示绿色 Snackbar
    - 错误操作显示红色 Snackbar 并提供重试按钮
    - _需求: 9.3, 9.4_
  
  - [x] 12.2 添加涟漪效果

    - 为所有可点击元素添加 clickable 修饰符
    - 使用 M3 的默认涟漪效果
    - _需求: 9.1_
  
  - [x] 12.3 添加状态变化动画

    - Switch 切换使用 150ms 动画
    - 对话框使用 200ms 淡入淡出动画
    - Snackbar 使用 250ms 滑入滑出动画
    - _需求: 9.2, 9.5, 9.6_

- [x] 13. 实现无障碍支持

  - [x] 13.1 添加 contentDescription

    - 为所有 Icon 添加 contentDescription
    - 为所有 IconButton 添加 contentDescription
    - _需求: 10.1_
  
  - [x] 13.2 添加语义化标签

    - 为 Switch 添加 stateDescription
    - 为可点击元素添加 role 语义
    - _需求: 10.2, 10.5_
  
  - [x] 13.3 确保触摸目标大小

    - 检查所有可点击元素的最小触摸目标为 48dp
    - 使用 Modifier.size(48.dp) 或 Modifier.minimumInteractiveComponentSize()
    - _需求: 10.3_
  
  - [x] 13.4 添加状态变化宣告

    - 使用 LiveRegion 宣告重要的状态变化
    - 设置保存成功时宣告
    - 错误发生时宣告
    - _需求: 10.6_

- [x] 14. 添加字符串资源

  - [x] 14.1 添加中文字符串资源

    - 在 values/strings.xml 中添加所有设置相关的字符串
    - 包括标题、设置项名称、描述、对话框文本等
    - _需求: 需求文档未明确要求，但设计文档中包含_
  
  - [x] 14.2 添加其他语言字符串资源

    - 在 values-en/strings.xml 中添加英文翻译
    - 在 values-ja/strings.xml 中添加日文翻译
    - 在 values-vi/strings.xml 中添加越南文翻译
    - _需求: 需求文档未明确要求，但设计文档中包含_

- [x] 15. 实现错误处理
  - [x] 15.1 在 ViewModel 中实现错误处理函数

    - 创建 handleError 函数处理各种异常
    - 区分不同错误类型（IOException, SecurityException 等）
    - 发出相应的 UI 事件
    - _需求: 1.4_
  
  - [x] 15.2 在 UI 中处理错误事件
    - 观察 ViewModel 的 uiEvent
    - 显示错误 Snackbar
    - 提供重试选项
    - 权限错误时显示引导对话框
    - _需求: 9.4_

- [x] 16. 编写单元测试
  - [ ] 16.1 编写 SettingsViewModel 测试
    - 测试状态初始化
    - 测试设置更新函数
    - 测试错误处理
    - 测试 WebDAV 连接测试
    - _需求: 需求文档未明确要求，但设计文档中包含_
  
  - [ ] 16.2 编写 PreferencesManager 测试
    - 测试数据读写
    - 测试默认值
    - 测试错误恢复
    - _需求: 需求文档未明确要求，但设计文档中包含_

- [x] 17. 编写 UI 测试
  - [ ] 17.1 编写 SettingsScreen 测试
    - 测试设置项显示
    - 测试用户交互
    - 测试对话框显示
    - 测试状态更新
    - _需求: 需求文档未明确要求，但设计文档中包含_
  
  - [ ] 17.2 编写对话框测试
    - 测试输入验证
    - 测试保存操作
    - 测试取消操作
    - _需求: 需求文档未明确要求，但设计文档中包含_

- [x] 18. 性能优化和最终调整
  - [ ] 18.1 优化状态管理
    - 检查并优化不必要的重组
    - 使用 derivedStateOf 优化计算密集型状态
    - 使用 remember 缓存计算结果
    - _需求: 需求文档未明确要求，但设计文档中包含_
  
  - [ ] 18.2 优化 UI 性能
    - 检查并优化过度重组
    - 为列表项添加 key 参数
    - 提取常用 Modifier 到变量
    - _需求: 需求文档未明确要求，但设计文档中包含_
  
  - [ ] 18.3 最终 UI 调整
    - 检查所有间距和对齐
    - 确保符合 M3 设计规范
    - 测试深色模式和浅色模式
    - 测试所有主题
    - 测试不同屏幕尺寸
    - _需求: 2.8_


- [x] 19. UI 卡片式布局增强（参考 Material Design 示例）
  - [x] 19.1 更新设计文档以反映卡片式布局
    - 更新 SettingsItem 和 SettingsSwitchItem 的设计说明
    - 更新间距系统规范
    - 更新 SettingsSection 的样式规范
    - _需求: 2.8_
  
  - [x] 19.2 实现卡片式布局
    - 为 SettingsItem 添加 Card 包装
    - 为 SettingsSwitchItem 添加 Card 包装
    - 为同步状态 ListItem 添加 Card 包装
    - 使用 surfaceVariant 作为卡片背景色
    - 设置卡片圆角为 medium（12dp）
    - _需求: 2.3, 2.4, 2.8_
  
  - [x] 19.3 优化间距和布局
    - 添加页面水平边距 16dp
    - 设置卡片垂直间距 4dp
    - 设置分组间距 16dp
    - 移除所有 Divider 分隔线
    - _需求: 2.8_
  
  - [x] 19.4 更新分组标题样式
    - 使用 labelLarge 替代 titleSmall
    - 使用 onSurfaceVariant 颜色
    - 标题文本全部大写
    - 调整标题内边距
    - _需求: 3.2_
  
  - [x] 19.5 创建文档总结
    - 创建 settings-ui-enhancement.md 文档
    - 记录所有 UI 改进
    - 提供代码示例
    - 记录视觉效果对比
