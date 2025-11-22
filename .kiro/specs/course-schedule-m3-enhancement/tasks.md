# 实现任务列表

- [x] 1. 数据模型和设置存储




- [x] 1.1 创建 CourseSettings 数据模型


  - 在 `domain/model` 包中创建 `CourseSettings.kt`
  - 定义所有配置字段（节数、时长、开始时间等）
  - 添加默认值
  - _需求: 1.1, 2.1, 4.1, 7.2_

- [x] 1.2 创建 CoursePeriod 数据模型


  - 在 `domain/model` 包中创建 `CoursePeriod.kt`
  - 定义节次编号、开始时间、结束时间等字段
  - _需求: 1.1, 4.2_

- [x] 1.3 创建 ScheduleTemplate 数据模型和预设模板


  - 在 `domain/model` 包中创建 `ScheduleTemplate.kt`
  - 定义模板数据结构
  - 创建 `ScheduleTemplates` 对象，包含小学、中学、大学三个预设模板
  - _需求: 3.1, 3.2_

- [x] 1.4 扩展 Course 数据模型


  - 在 `Course.kt` 中添加 `periodStart`、`periodEnd`、`isCustomTime` 字段
  - 更新数据库 Entity 注解
  - _需求: 5.3, 5.5_

- [x] 1.5 实现数据库迁移


  - 在 `SaisonDatabase.kt` 中添加新的 Migration
  - 为 Course 表添加新字段
  - 设置现有课程为自定义时间模式
  - _需求: 5.3_

- [x] 1.6 扩展 PreferencesManager


  - 添加课程设置相关的 PreferencesKeys
  - 实现 `courseSettings` Flow
  - 实现 `setCourseSettings` 方法
  - _需求: 7.1, 7.2, 7.3_

- [x] 1.7 创建 CourseSettingsRepository


  - 在 `data/repository` 包中创建接口和实现
  - 实现 `getSettings()`、`updateSettings()`、`resetToDefault()` 方法
  - 使用 Hilt 注入
  - _需求: 7.1, 7.2_

- [x] 2. 节次计算和验证逻辑


- [x] 2.1 实现节次计算算法


  - 在 CourseViewModel 中实现 `calculatePeriods()` 方法
  - 处理基本节次计算
  - 处理午休时间计算
  - 返回 CoursePeriod 列表
  - _需求: 1.3, 2.4, 4.2_

- [x] 2.2 实现节次查询方法


  - 实现 `getPeriodByNumber()` 方法
  - 实现 `getAvailablePeriods()` 方法（按星期过滤）
  - _需求: 5.2, 5.3_

- [x] 2.3 实现冲突检测逻辑


  - 实现 `checkPeriodConflict()` 方法
  - 检测节次范围重叠
  - 支持排除特定课程（编辑时）
  - _需求: 5.3_

- [x] 2.4 实现数据验证


  - 创建 `CourseSettingsValidator` 工具类
  - 实现节数、时长、节次范围验证
  - _需求: 1.2, 2.2, 2.3_

- [x] 2.5 编写节次计算单元测试


  - 测试基本节次计算
  - 测试包含午休的计算
  - 测试边界情况
  - 测试冲突检测
  - _需求: 1.3, 2.4_

- [x] 3. 课程设置界面


- [x] 3.1 创建 TemplateSelector 组件


  - 在 `ui/components` 包中创建 `TemplateSelector.kt`
  - 使用 LazyRow 展示模板卡片
  - 实现选中状态显示
  - 应用 M3 Card 样式
  - _需求: 3.1, 3.3, 8.1, 8.2, 8.3_

- [x] 3.2 创建时间预览列表组件


  - 创建 `PeriodPreviewList` 组件
  - 使用 LazyColumn 显示所有节次时间
  - 显示节次编号和时间范围
  - 标识午休位置
  - _需求: 4.4_

- [x] 3.3 创建 CourseSettingsSheet 组件


  - 在 `ui/components` 包中创建 `CourseSettingsSheet.kt`
  - 使用 ModalBottomSheet 实现
  - 添加模板选择区域（使用 TemplateSelector）
  - 添加自定义配置区域（Slider 和 TimePicker）
  - 添加时间预览区域
  - 添加保存按钮
  - _需求: 1.1, 1.2, 2.2, 2.3, 3.2, 4.1, 4.4, 8.5_

- [x] 3.4 实现设置界面交互逻辑

  - 实现 Slider 值变化处理
  - 实现 TimePicker 交互
  - 实现模板选择处理
  - 实现实时预览更新
  - _需求: 1.3, 2.4, 3.2, 4.2_

- [x] 3.5 实现模板应用警告对话框

  - 检测受影响的课程
  - 显示警告对话框
  - 列出受影响的课程
  - 提供继续/取消选项
  - _需求: 3.4_

- [x] 3.6 集成设置界面到 CourseScreen


  - 在 TopAppBar 添加设置按钮
  - 管理 BottomSheet 显示状态
  - 处理设置保存
  - _需求: 1.1, 7.1_

- [x] 4. 节次选择器组件



- [x] 4.1 创建 PeriodSelector 组件


  - 在 `ui/components` 包中创建 `PeriodSelector.kt`
  - 使用 FlowRow 布局
  - 使用 FilterChip 显示每个节次
  - 显示节次编号和时间范围
  - _需求: 5.2, 5.3_

- [x] 4.2 实现节次选择交互

  - 实现单个节次选择
  - 实现连续多节次选择
  - 显示已占用节次为禁用状态
  - 应用 M3 Chip 样式
  - _需求: 5.2, 5.3, 5.5_

- [x] 4.3 实现节次可用性检查

  - 根据当前星期和已有课程过滤可用节次
  - 高亮显示冲突节次
  - _需求: 5.3_

- [x] 5. 课程添加增强


- [x] 5.1 扩展 AddCourseSheet 组件


  - 添加时间输入模式切换（SegmentedButton）
  - 添加"按节次"和"自定义时间"两个模式
  - 重构布局以容纳新元素
  - _需求: 5.1, 5.4_

- [x] 5.2 集成 PeriodSelector 到添加课程界面

  - 在"按节次"模式下显示 PeriodSelector
  - 在"自定义时间"模式下显示 TimePicker
  - 实现模式切换逻辑
  - _需求: 5.2, 5.4_

- [x] 5.3 实现按节次添加课程逻辑

  - 在 CourseViewModel 中添加 `addCourseByPeriod()` 方法
  - 根据选中的节次自动填充开始/结束时间
  - 保存节次信息到数据库
  - _需求: 5.3, 5.5_

- [x] 5.4 实现节次选择自动填充时间

  - 监听节次选择变化
  - 自动更新时间字段
  - 支持跨节次时间计算
  - _需求: 5.3_

- [x] 5.5 添加冲突检测和提示

  - 在保存前检测节次冲突
  - 显示冲突错误对话框
  - 阻止保存冲突课程
  - _需求: 5.3_

- [x] 6. 课程表视图优化

- [x] 6.1 更新 CourseCard 组件

  - 添加节次信息显示（使用 AssistChip 或 Badge）
  - 显示单节次（如"第1节"）或节次范围（如"第3-4节"）
  - 调整布局以容纳节次标签
  - 应用 M3 Card 新样式
  - _需求: 6.1, 6.2, 6.3, 8.1, 8.2, 8.3_

- [x] 6.2 优化 CourseScheduleView 布局

  - 实现按节次分组显示
  - 添加节次时间轴
  - 显示空节次占位符
  - 按节次顺序排列课程
  - _需求: 6.4, 6.5_

- [x] 6.3 实现节次分组逻辑

  - 在 CourseViewModel 中添加按节次分组方法
  - 处理跨节次课程
  - 处理自定义时间课程
  - _需求: 6.4_

- [x] 6.4 应用 M3 视觉层次

  - 使用颜色区分不同节次
  - 使用 elevation 区分层次
  - 添加过渡动画
  - _需求: 6.5, 8.1, 8.2, 8.3, 8.4_

- [x] 7. 国际化和无障碍

- [x] 7.1 添加中文字符串资源

  - 在 `values-zh-rCN/strings.xml` 中添加所有新字符串
  - 包括设置、模板、节次、错误提示等
  - _需求: 所有_

- [x] 7.2 添加其他语言字符串资源

  - 在 `values/strings.xml`（英文）中添加字符串
  - 在 `values-ja/strings.xml`（日文）中添加字符串
  - 在 `values-vi/strings.xml`（越南文）中添加字符串
  - _需求: 所有_

- [x] 7.3 添加无障碍支持

  - 为所有图标按钮添加 contentDescription
  - 为 Slider 添加值描述
  - 为节次按钮添加语义化标签
  - 确保触摸目标至少 48dp
  - _需求: 所有_

- [x] 8. 测试和优化


- [x] 8.1 编写 ViewModel 单元测试

  - 测试设置加载和保存
  - 测试节次计算
  - 测试冲突检测
  - 测试模板应用
  - _需求: 所有_

- [x] 8.2 编写 UI 测试

  - 测试设置界面交互
  - 测试课程添加流程
  - 测试节次选择
  - 测试模式切换
  - _需求: 所有_

- [x] 8.3 性能优化

  - 使用 remember 缓存节次计算
  - 优化 LazyColumn 渲染
  - 使用 derivedStateOf 计算派生状态
  - _需求: 所有_

- [x] 8.4 最终验证和调整

  - 验证所有功能正常工作
  - 检查 M3 设计规范应用
  - 测试不同配置场景
  - 修复发现的问题
  - _需求: 所有_
