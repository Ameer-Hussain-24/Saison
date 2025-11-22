# 实现计划 - 事件页面

- [x] 1. 创建数据层基础结构

  - 创建 Event 数据模型、EventCategory 枚举和 EventEntity 数据库实体
  - 实现数据库 DAO 和 Repository 接口
  - _需求: 1.1, 2.1, 8.1_

- [x] 1.1 创建事件数据模型和枚举


  - 在 `domain/model` 包中创建 `Event.kt` 数据类
  - 在 `domain/model` 包中创建 `EventCategory.kt` 枚举，包含生日、纪念日、倒数日三种类型
  - 为 EventCategory 添加图标和显示名称资源 ID 方法
  - _需求: 1.1, 2.1, 8.1, 8.5_

- [x] 1.2 创建数据库实体和 DAO


  - 在 `data/local/database/entity` 包中创建 `EventEntity.kt`
  - 在 `data/local/database/dao` 包中创建 `EventDao.kt`，实现查询、插入、更新、删除操作
  - 为 eventDate 和 category 字段添加数据库索引
  - 更新 AppDatabase 添加 EventEntity 表
  - _需求: 1.1, 1.2_

- [x] 1.3 实现 Repository 层


  - 在 `domain/repository` 包中创建 `EventRepository.kt` 接口
  - 在 `data/repository` 包中创建 `EventRepositoryImpl.kt` 实现类
  - 实现 Entity 和 Domain 模型之间的映射
  - 使用 Flow 返回响应式数据流
  - _需求: 1.1, 1.2_

- [x] 2. 创建工具类和辅助函数

  - 实现天数计算工具和日期格式化工具
  - _需求: 6.1, 6.2, 6.3, 6.4, 6.5, 6.6_

- [x] 2.1 实现天数计算工具


  - 在 `util` 包中创建 `EventDateCalculator.kt`
  - 实现 `calculateDaysUntil()` 方法计算事件距今天数
  - 实现 `formatDaysText()` 方法格式化天数显示文本
  - 处理未来、过去和今天三种情况
  - _需求: 6.1, 6.2, 6.3, 6.4, 6.5_

- [x] 2.2 实现日期格式化工具


  - 在 `util` 包中创建 `EventDateFormatter.kt`
  - 实现日期、时间和日期时间的格式化方法
  - 支持本地化格式
  - _需求: 9.4_

- [x] 3. 创建 ViewModel 层

  - 实现事件页面的状态管理和业务逻辑
  - _需求: 1.1, 1.2, 8.4_

- [x] 3.1 创建 EventViewModel


  - 在 `ui/screens/event` 包中创建 `EventViewModel.kt`
  - 实现事件列表加载和类别筛选逻辑
  - 实现事件创建、更新、删除方法
  - 使用 StateFlow 管理 UI 状态
  - 实现事件按日期排序（未来事件在前）
  - _需求: 1.1, 1.2, 8.4_

- [x] 3.2 定义 UI 状态类

  - 创建 `EventUiState` 密封类，包含 Loading、Empty、Success、Error 状态
  - _需求: 1.4_

- [x] 4. 创建 UI 组件

  - 实现事件卡片和创建表单组件
  - _需求: 2.1, 2.2, 2.3, 2.4, 2.5, 2.6, 4.1, 4.2, 4.3, 4.4, 4.5, 4.6, 4.7, 4.8_

- [x] 4.1 创建 EventCard 组件


  - 在 `ui/components` 包中创建 `EventCard.kt`
  - 使用 Material 3 Card 组件
  - 显示事件标题、日期、类别图标和天数信息
  - 使用不同颜色区分未来和过去事件
  - 集成 EventDateCalculator 计算天数
  - _需求: 2.1, 2.2, 2.3, 2.4, 2.5, 2.6_

- [x] 4.2 创建 CreateEventSheet 组件


  - 在 `ui/components` 包中创建 `CreateEventSheet.kt`
  - 使用 ModalBottomSheet 实现底部表单
  - 添加标题、描述输入字段
  - 添加事件类别选择（使用 FilterChip）
  - 添加日期和时间选择功能
  - 添加保存和取消按钮
  - 实现表单验证（标题和日期必填）
  - _需求: 4.1, 4.2, 4.3, 4.4, 4.5, 4.6, 4.7, 4.8_

- [x] 4.3 创建日期和时间选择器对话框

  - 创建 `DatePickerDialog.kt` 使用 Material 3 DatePicker
  - 创建 `TimePickerDialog.kt` 使用 Material 3 TimePicker
  - _需求: 5.1, 5.2, 5.4, 5.5_

- [x] 5. 创建事件列表页面

  - 实现主页面布局和交互
  - _需求: 1.1, 1.2, 1.3, 1.4, 1.5, 3.1, 3.2, 3.3, 3.4, 3.5_

- [x] 5.1 创建 EventScreen


  - 在 `ui/screens/event` 包中创建 `EventScreen.kt`
  - 使用 Scaffold 布局，包含 TopBar 和 FAB
  - 使用 LazyColumn 显示事件列表
  - 实现空状态显示
  - 实现加载状态显示
  - 集成 EventViewModel
  - _需求: 1.1, 1.2, 1.3, 1.4, 1.5_

- [x] 5.2 创建 EventTopBar 组件

  - 创建顶部栏，显示标题和类别筛选
  - 使用 FilterChip 实现类别筛选
  - _需求: 8.4_

- [x] 5.3 创建空状态组件

  - 创建 `EmptyEventState.kt` 显示无事件提示
  - 使用 Material 3 设计规范
  - _需求: 1.4_

- [x] 5.4 集成 FAB 创建事件功能

  - 在右下角添加 FloatingActionButton
  - 点击时打开 CreateEventSheet
  - 使用动画效果
  - _需求: 3.1, 3.2, 3.3, 3.4, 3.5_

- [x] 6. 实现事件详情和编辑功能

  - 添加事件点击导航和编辑删除功能
  - _需求: 7.1, 7.2, 7.3, 7.4, 7.5, 7.6_

- [x] 6.1 创建 EventDetailScreen


  - 在 `ui/screens/event` 包中创建 `EventDetailScreen.kt`
  - 显示事件完整信息
  - 添加编辑和删除按钮
  - _需求: 7.1, 7.2, 7.3_

- [x] 6.2 实现事件编辑功能

  - 复用 CreateEventSheet 组件进行编辑
  - 预填充现有事件数据
  - _需求: 7.4_

- [x] 6.3 实现事件删除功能

  - 添加删除确认对话框
  - 实现删除操作
  - _需求: 7.5, 7.6_

- [x] 7. 添加导航集成

  - 将事件页面集成到应用导航系统
  - _需求: 1.1_

- [x] 7.1 更新导航图


  - 在 `SaisonNavHost.kt` 中添加事件页面路由
  - 添加事件详情页面路由
  - 配置页面间导航
  - _需求: 1.1_

- [x] 7.2 更新底部导航栏


  - 在 MainActivity 或底部导航配置中添加事件入口（如需要）
  - _需求: 1.1_

- [x] 8. 添加国际化支持

  - 添加多语言字符串资源
  - _需求: 9.1, 9.2, 9.3, 9.4, 9.5, 9.6_

- [x] 8.1 添加中文字符串资源


  - 在 `values-zh-rCN/strings.xml` 中添加所有事件相关字符串
  - 包括页面标题、按钮文本、类别名称、天数显示等
  - _需求: 9.1, 9.2, 9.3, 9.6_

- [x] 8.2 添加英文字符串资源


  - 在 `values/strings.xml` 中添加英文翻译
  - _需求: 9.1, 9.2, 9.3, 9.6_

- [x] 8.3 添加日文字符串资源


  - 在 `values-ja/strings.xml` 中添加日文翻译
  - _需求: 9.1, 9.2, 9.3, 9.6_

- [x] 8.4 添加越南文字符串资源


  - 在 `values-vi/strings.xml` 中添加越南文翻译
  - _需求: 9.1, 9.2, 9.3, 9.6_

- [x] 9. 编写测试


  - 为核心功能编写单元测试
  - _需求: 6.1, 6.2, 6.3, 6.4_

- [x] 9.1 编写 EventDateCalculator 测试


  - 测试未来日期、过去日期和今天的计算
  - 测试边界情况
  - _需求: 6.1, 6.2, 6.3, 6.4_

- [x] 9.2 编写 EventViewModel 测试

  - 测试事件加载、筛选、创建、更新、删除
  - 测试状态管理
  - _需求: 1.1, 1.2, 8.4_

- [x] 9.3 编写 EventRepository 测试

  - 测试数据库操作和数据映射
  - _需求: 1.1, 1.2_
