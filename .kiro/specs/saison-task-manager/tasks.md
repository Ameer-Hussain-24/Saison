# Saison 任务管理应用 - 实施计划

## 任务列表

- [x] 1. 项目初始化和基础架构



  - 创建 Android 项目，配置包名 `takagi.ru.saison`
  - 配置 `build.gradle.kts` 添加所有必需依赖（Compose、Hilt、Room、DataStore、WorkManager、ML Kit）
  - 设置 Hilt 依赖注入框架
  - 配置 ProGuard 混淆规则



  - _需求: 1.1, 1.2, 1.3, 1.4, 1.5_

- [x] 2. 数据层实现 - Room 数据库

  - 创建 `SaisonDatabase` 类和数据库配置
  - 实现 `TaskEntity`、`CourseEntity`、`TagEntity`、`PomodoroSessionEntity`、`AttachmentEntity` 实体类




  - 创建对应的 DAO 接口（TaskDao、CourseDao、TagDao、PomodoroDao、AttachmentDao）
  - 定义数据库关系和外键约束
  - 添加数据库索引优化查询性能



  - _需求: 15.1, 15.2, 15.3, 15.4, 15.5_






- [x] 3. 数据层实现 - DataStore 和加密

  - 实现 `PreferencesManager` 使用 DataStore 存储用户偏好
  - 创建 `ThemePreferences` 数据类
  - 实现 `EncryptionManager` 使用 AES-256-GCM 和 Android Keystore
  - 实现 `KeystoreHelper` 管理加密密钥
  - _需求: 14.2, 15.2_

- [x] 4. Domain 层 - 数据模型和映射

  - 创建 Domain 模型类（Task、Course、Tag、PomodoroSession、Attachment）
  - 实现 Entity 到 Domain 模型的映射扩展函数
  - 创建 `RecurrenceRule` 类和 RRULE 解析器
  - 定义 `Priority`、`CalendarViewMode`、`SeasonalTheme` 等枚举
  - _需求: 7.2, 7.3_

- [x] 5. Repository 层实现


  - 实现 `TaskRepository` 包含 CRUD 操作和 Flow 数据流
  - 实现 `CourseRepository` 处理课程表逻辑
  - 实现 `TagRepository` 支持层级标签
  - 实现 `PomodoroRepository` 管理番茄钟会话
  - 实现 `AttachmentRepository` 处理加密文件
  - _需求: 7.1, 7.4, 7.5, 8.1, 8.2, 8.3, 8.4, 8.5, 11.1, 11.4, 12.1, 12.2, 12.4_

- [x] 6. WebDAV 同步功能



  - 实现 `WebDavClient` 使用 OkHttp 进行 WebDAV 通信
  - 实现 `WebDavSyncRepository` 处理增量同步逻辑
  - 实现 `ConflictResolver` 处理同步冲突
  - 创建 `SyncWorker` 使用 WorkManager 实现定时同步
  - 实现 ETag 和 Last-Modified 头部处理
  - _需求: 10.1, 10.2, 10.3, 10.4, 10.5_

- [x] 7. 自然语言解析器




  - 实现 `NaturalLanguageParser` 解析日期表达式
  - 添加时间表达式解析（9am、14:30、noon）
  - 添加优先级关键词识别（urgent、important）
  - 添加标签提取（#work、#personal）
  - 创建 `ParseNaturalLanguageUseCase` 用例
  - _需求: 17.1, 17.2, 17.3, 17.4, 17.5_

- [x] 8. M3E 主题系统





  - 创建 `Theme.kt` 定义 Compose 主题
  - 实现 12 季主题调色盘（Sakura、Mint、Amber、Snow、Rain、Maple 等）
  - 实现 `ThemeManager` 管理主题切换
  - 支持 Android 12+ 动态颜色提取
  - 实现深色模式支持
  - _需求: 2.2, 2.3_

- [x] 9. 多语言国际化



  - 创建 `strings.xml` 资源文件（英语、简体中文、日语、越南语）
  - 实现 `LocaleHelper` 支持动态语言切换
  - 配置 Android 13+ Per-App Language 支持
  - 实现日期时间本地化格式化
  - _需求: 4.1, 4.2, 4.3, 4.4, 4.5_

- [x] 10. 导航和主界面框架



  - 创建 `SaisonNavHost` 定义应用导航图
  - 实现 `MainActivity` 配置 Edge-to-Edge 和 Predictive Back
  - 创建主 `Scaffold` 布局包含 TopBar、BottomBar、FAB
  - 实现 `NavigationBar` 使用 M3E 组件
  - 实现 `SaisonTopAppBar` 和 `SaisonSearchBar`
  - _需求: 2.1, 2.4, 3.1, 3.5_

- [x] 11. 日历视图功能






  - 实现 `CalendarViewModel` 管理日历状态
  - 创建 `MonthView` 月视图 Composable
  - 创建 `WeekView` 周视图 Composable
  - 创建 `DayView` 日视图 Composable
  - 创建 `AgendaView` 议程视图 Composable
  - 实现视图切换的 `SegmentedButton`
  - 集成 `LunarCalendarProvider` 显示农历
  - 集成 `HolidayProvider` 显示节假日
  - 实现拖拽调整事件时长功能
  - _需求: 5.1, 5.2, 5.3, 5.4, 5.5_

- [x] 12. 任务列表和详情界面



  - 实现 `TaskViewModel` 管理任务状态
  - 创建 `TaskListScreen` 显示任务列表
  - 创建 `TaskCard` M3E 风格任务卡片组件
  - 创建 `TaskDetailScreen` 任务详情编辑界面
  - 实现 `SubtaskList` 子任务列表组件
  - 实现 `NaturalLanguageInput` 自然语言输入组件
  - 实现 `PrioritySegmentedButton` 优先级选择器（Eisenhower Matrix）
  - 集成 `DatePicker` 和 `TimePicker` M3E 组件
  - 实现任务完成状态切换和动画
  - _需求: 7.1, 7.2, 7.3, 7.4, 7.5, 17.5_

- [x] 13. 课程表功能



  - 实现 `CourseViewModel` 管理课程数据
  - 创建 `CourseScreen` 课程表主界面
  - 创建 `CourseCard` 课程卡片组件
  - 实现周循环、A/B 周、单双周模式切换
  - 实现 `CourseOcrImport` 使用 ML Kit 识别课程表图片
  - 配置课程提醒通知（上课前 10 分钟）
  - 实现自动静音功能
  - _需求: 6.1, 6.2, 6.3, 6.4, 6.5_

- [x] 14. 番茄钟功能



  - 实现 `PomodoroViewModel` 管理计时器状态
  - 创建 `PomodoroScreen` 番茄钟主界面
  - 实现 `CircularTimer` 圆形进度条组件
  - 创建 `PomodoroTimerService` 前台服务
  - 实现 Focus Mode（锁屏 + DND）
  - 实现长休息逻辑（每 4 个番茄钟）
  - 创建 `PomodoroStats` 统计图表界面
  - 实现番茄钟完成通知和震动反馈
  - _需求: 8.1, 8.2, 8.3, 8.4, 8.5_

- [x] 15. 节拍器功能





  -参考项目tack-android的UI实现，可以照搬过来
  - 实现 `MetronomeViewModel` 管理节拍器状态
  - 创建 `MetronomeScreen` 节拍器主界面
  - 实现 `BeatVisualizer` 视觉节拍指示器
  - 创建 `MetronomeService` 后台服务
  - 使用 `SoundPool` 加载音效（Woodblock、Click、Beep）
  - 实现 BPM 调节（30-240）
  - 实现细分节拍（1/4、1/8、Triplets）
  - 集成 `MediaSession` 支持锁屏控制
  - 实现节拍震动反馈
  - _需求: 9.1, 9.2, 9.3, 9.4, 9.5_

- [x] 16. 标签和分类管理


  - 实现层级标签解析（#study/university/math）
  - 创建标签选择器 `ModalBottomSheet`
  - 实现标签图标选择（MaterialIcons.Extended）
  - 实现智能标签建议算法
  - 创建标签统计界面
  - _需求: 11.1, 11.2, 11.3, 11.4, 11.5_

- [ ] 17. 附件管理
  - 实现图片附件上传和预览
  - 实现 PDF 附件查看
  - 实现语音备忘录录制和播放
  - 实现手写笔记功能
  - 集成加密存储（AES-256-GCM）
  - 实现离线缓存机制
  - _需求: 12.1, 12.2, 12.3, 12.4, 12.5_

- [ ] 18. 通知系统
  - 创建 `NotificationService` 管理所有通知
  - 实现任务提醒通知
  - 实现课程提醒通知
  - 实现番茄钟完成通知
  - 配置通知渠道和优先级
  - 实现通知点击跳转
  - _需求: 16.1, 16.2, 16.3, 16.4, 16.5_

- [ ] 19. 桌面小部件
  - 创建 `TodayTasksWidget` (4×2) 显示今日任务
  - 创建 `CourseScheduleWidget` (4×4) 显示课程表
  - 创建 `PomodoroWidget` (2×2) 显示番茄钟倒计时
  - 实现小部件数据更新机制
  - 实现小部件点击交互
  - _需求: 13.1, 13.2, 13.3, 13.4, 13.5_

- [ ] 20. 安全和认证
  - 实现 6 位 PIN 码设置和验证
  - 集成生物识别认证（指纹/面部）
  - 实现敏感任务加密标记
  - 实现截图保护功能
  - 实现 3 分钟自动锁定
  - 集成 Pwned Passwords API 检查
  - _需求: 14.1, 14.2, 14.3, 14.4, 14.5_

- [x] 21. 设置界面



  - 创建 `SettingsScreen` 设置主界面
  - 实现 `ThemeSelector` 主题选择器
  - 实现 `LanguageSelector` 语言选择器
  - 实现 `WebDavSettings` WebDAV 配置界面
  - 添加通知设置选项
  - 添加安全设置选项
  - 添加关于页面（版本、许可证）
  - _需求: 4.3, 10.1_

- [x] 22. 无障碍支持

  - 为所有 Composable 添加语义标签
  - 测试 TalkBack 屏幕阅读器兼容性
  - 实现动态文本大小支持
  - 实现高对比度模式
  - 添加触摸目标最小尺寸（48dp）
  - _需求: 3.2, 3.3, 3.4_

- [x] 23. 响应式布局适配

  - 使用 `WindowSizeClass` 检测设备类型
  - 实现手机布局（Compact）
  - 实现平板布局（Medium/Expanded）使用 `NavigationRail`
  - 实现折叠屏适配
  - 实现桌面模式布局
  - _需求: 3.1_

- [ ] 24. 性能优化
  - 优化 LazyColumn 使用 key 参数
  - 实现数据库分页加载
  - 添加图片缓存（Coil）
  - 优化 Compose 重组性能
  - 实现内存泄漏检测
  - _需求: 18.1, 18.2, 18.3, 18.4, 18.5_

- [ ] 25. 集成测试和调试
  - 编写 Repository 单元测试
  - 编写 ViewModel 单元测试
  - 编写 Compose UI 测试
  - 编写 WebDAV 同步集成测试
  - 配置 CI/CD 流程
  - _需求: 所有需求的验证_

- [ ] 26. 最终打包和发布准备
  - 配置签名密钥
  - 生成 Release APK/AAB
  - 测试混淆后的应用
  - 准备应用商店素材（截图、描述）
  - 编写用户文档
  - _需求: 1.4_
