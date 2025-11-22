# 课程周数选择器增强实现任务

## 任务列表

- [x] 1. 扩展数据模型和数据库


  - 在 Course 数据模型中添加 customWeeks 字段（List<Int>?）
  - 在 WeekPattern 枚举中添加 CUSTOM 选项
  - 创建数据库迁移脚本添加 custom_weeks 列
  - 实现 Room TypeConverter 用于 List<Int> 与 JSON 的转换
  - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5, 10.1, 10.2, 10.3, 10.4, 10.5_

- [x] 2. 创建学期设置功能


  - 创建 SemesterSettings 数据模型
  - 在 PreferencesManager 中添加学期设置的存储和读取方法
  - 创建 SemesterSettingsRepository 接口和实现
  - 在 CourseViewModel 中集成学期设置的 StateFlow
  - _Requirements: 7.1, 7.2, 7.3, 7.4, 7.5_

- [x] 3. 实现周数计算工具类




  - 创建 WeekCalculator 类
  - 实现 calculateCurrentWeek 方法（根据学期开始日期计算当前周数）
  - 实现 calculateWeeksFromDateRange 方法（根据日期范围计算周数列表）
  - 实现 getWeekDateRange 方法（获取某周的日期范围）
  - _Requirements: 6.3, 9.4_

- [x] 3.1 编写 WeekCalculator 单元测试


  - 测试当前周数计算的正确性
  - 测试日期范围转周数的边界情况
  - 测试跨学期的情况
  - _Requirements: 6.3, 9.4_

- [x] 4. 实现周数选择器UI组件




- [x] 4.1 创建 WeekNumberButton 组件



  - 实现圆形按钮样式（48dp，CircleShape）
  - 实现选中和未选中状态的颜色切换
  - 添加点击动画效果
  - _Requirements: 1.3, 1.4, 1.5, 1.6_

- [x] 4.2 创建 WeekNumberGrid 组件


  - 使用 FlowRow 实现网格布局（每行6个）
  - 集成 WeekNumberButton 组件
  - 实现周数点击切换逻辑
  - _Requirements: 1.2, 1.4_

- [x] 4.3 创建 QuickSelectionBar 组件


  - 实现三个 FilterChip（全周、单周、双周）
  - 实现快捷模式选择逻辑
  - 实现选中状态的视觉反馈
  - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5_

- [x] 4.4 创建 WeekNumberSelectionContent 组件


  - 集成 QuickSelectionBar 和 WeekNumberGrid
  - 实现快捷模式与手动选择的联动逻辑
  - 添加选择统计显示
  - _Requirements: 2.6, 8.3_

- [x] 5. 实现日期模式UI组件



- [x] 5.1 创建 DateRangeSelectionContent 组件


  - 实现开始日期和结束日期选择卡片
  - 集成 Material 3 DatePicker
  - 实现重复模式选择（每周、单周、双周）
  - _Requirements: 6.1, 6.2, 6.5_

- [x] 5.2 实现日期转周数逻辑


  - 在 ViewModel 中集成 WeekCalculator
  - 实现日期范围选择后自动计算周数
  - 实现周数与日期的双向同步
  - _Requirements: 6.3_

- [x] 6. 创建周数选择器对话框



- [x] 6.1 创建 WeekSelectorDialog 组件


  - 实现 AlertDialog 容器和布局
  - 添加 TabRow 实现模式切换（周数模式/日期模式）
  - 集成 WeekNumberSelectionContent 和 DateRangeSelectionContent
  - 实现选择统计显示
  - 添加确定和取消按钮
  - _Requirements: 1.1, 3.1, 3.2, 3.3, 3.4, 3.5, 8.1, 8.3, 8.4, 8.5_

- [x] 6.2 实现周数验证逻辑


  - 创建 WeekSelectionValidator 工具类
  - 实现周数范围验证
  - 实现日期范围验证
  - 在对话框中集成验证逻辑
  - _Requirements: 8.1, 8.2_

- [x] 7. 扩展 CourseViewModel




- [x] 7.1 添加当前周数计算


  - 使用 combine 操作符结合学期设置和当前日期
  - 创建 currentWeek StateFlow
  - 实现自动更新逻辑
  - _Requirements: 9.4_

- [x] 7.2 实现课程过滤逻辑


  - 创建 isCourseActiveInWeek 方法
  - 实现 filteredCourses StateFlow
  - 支持全周、单周、双周、自定义周数的过滤
  - 添加显示模式切换功能
  - _Requirements: 9.2, 9.3_

- [x] 7.3 添加自定义周数课程创建方法


  - 扩展 addCourse 方法支持 customWeeks 参数
  - 实现 WeekPattern 和 customWeeks 的联动逻辑
  - _Requirements: 4.3_

- [-] 8. 集成到现有组件


- [x] 8.1 更新 AddCourseSheet




  - 添加 customWeeks 状态管理
  - 替换现有的 WeekDetailDialog 为 WeekSelectorDialog
  - 实现周数选择结果的保存
  - _Requirements: 1.1, 4.3_

- [x] 8.2 更新 CourseCard 显示







  - 创建 WeekPatternChip 组件
  - 实现不同周数模式的标签显示
  - 添加本周是否有课的视觉指示
  - _Requirements: 5.1, 5.2, 5.3, 5.4, 9.5_

- [x] 8.3 创建 CurrentWeekIndicator 组件



  - 显示当前周数和总周数
  - 添加显示模式切换按钮
  - 使用 Card 和 primaryContainer 颜色
  - _Requirements: 9.1, 9.2_

- [x] 8.4 更新 CourseScreen


  - 在顶部添加 CurrentWeekIndicator
  - 使用 filteredCourses 替代原有的 courses
  - 实现显示模式切换功能
  - _Requirements: 9.1, 9.2, 9.3_

- [x] 9. 添加学期设置界面


  - 创建 SemesterSettingsSheet 组件
  - 添加学期开始日期选择器
  - 添加学期总周数 Slider（16-24周）
  - 在设置页面添加入口
  - _Requirements: 7.1, 7.2, 7.3, 7.4_

- [x] 10. 国际化支持



  - 在 strings.xml 中添加周数选择器相关字符串
  - 添加日期模式相关字符串
  - 添加当前周指示器相关字符串
  - 添加学期设置相关字符串
  - 添加错误提示字符串
  - 为所有支持的语言添加翻译（中文、英文、日文、越南文）
  - _Requirements: 所有需求_

- [ ] 11. 编写单元测试
  - 测试 isCourseActiveInWeek 方法的各种周数模式
  - 测试课程过滤逻辑
  - 测试 WeekSelectionValidator
  - 测试 Room TypeConverter
  - _Requirements: 所有需求_

- [ ] 12. UI测试和优化
  - 测试周数选择器的交互
  - 测试快捷模式切换
  - 测试过滤功能
  - 性能优化（使用 remember 和 derivedStateOf）
  - _Requirements: 所有需求_

