# 课程表单双周显示修复 - 任务列表

- [x] 1. 修复周数过滤逻辑




  - 在 CourseScreen 的 HorizontalPager 中实现统一的周数过滤
  - 确保每一页只显示对应周数的课程
  - 移除 ContinuousTimelineView 中的重复过滤逻辑
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 5.1, 5.2, 5.3, 5.4, 5.5_

- [x] 2. 实现智能紧凑显示算法



- [x] 2.1 创建 TimeSlot 数据模型


  - 定义 TimeSlot 数据类，包含 hour、hasAnyCourse、courseCount、displayHeight 字段
  - _Requirements: 6.3_

- [x] 2.2 实现时间段分析函数


  - 实现 analyzeTimeSlots 函数，分析每个小时在一周内的课程分布
  - 根据是否有课程决定显示高度（有课程 60dp，无课程 20dp）
  - _Requirements: 6.1, 6.2, 6.3, 6.4, 6.5_

- [x] 2.3 创建 CoursePosition 数据模型


  - 定义 CoursePosition 数据类，包含 offsetFromTop 和 height 字段
  - _Requirements: 6.4_

- [x] 2.4 实现课程位置计算函数


  - 实现 calculateCoursePosition 函数，计算课程在紧凑布局中的位置
  - 考虑压缩时间段对课程位置的影响
  - 正确计算跨多个时间段的课程高度
  - _Requirements: 6.1, 6.2, 6.4_

- [x] 3. 重构 ContinuousTimelineView 组件




- [x] 3.1 修改主组件结构

  - 更新 ContinuousTimelineView 接收已过滤的课程数据
  - 集成时间段分析和课程位置计算
  - 使用新的紧凑布局算法
  - _Requirements: 5.4, 6.1, 6.2, 6.3, 6.4_

- [x] 3.2 实现 CompactTimelineContent 组件

  - 创建新的紧凑时间轴内容组件
  - 使用 timeSlots 控制布局
  - 协调时间列、课程列和网格线的显示
  - _Requirements: 6.1, 6.2, 6.4_

- [x] 3.3 实现 CompactTimeColumn 组件

  - 创建紧凑时间列组件
  - 根据 timeSlots 的 displayHeight 动态调整每个时间标签的高度
  - _Requirements: 6.5, 7.5_

- [x] 3.4 实现 CompactDayColumn 组件

  - 创建紧凑日期列组件
  - 使用 calculateCoursePosition 计算每个课程的位置
  - 渲染课程卡片
  - _Requirements: 6.1, 6.2, 6.4_

- [x] 3.5 实现 CompactTimeGrid 组件

  - 创建紧凑时间网格线组件
  - 根据 timeSlots 绘制分隔线
  - _Requirements: 7.5_

- [x] 4. 优化课程卡片视觉效果

- [x] 4.1 重构 CourseBlock 组件

  - 重命名为 CompactCourseBlock
  - 优化信息层次和布局
  - 改进字体大小和间距
  - _Requirements: 8.1, 8.2, 8.3, 8.4_

- [x] 4.2 添加周模式标识

  - 在课程卡片右上角显示周模式标识（单周/双周/A周/B周）
  - 使用半透明背景和小字体
  - 仅在非全部周模式时显示
  - _Requirements: 8.5_

- [x] 4.3 优化颜色对比度

  - 确保课程卡片背景色和文字颜色有足够的对比度
  - 优化半透明效果
  - _Requirements: 8.4_

- [x] 5. 优化时间轴范围计算

- [x] 5.1 创建 TimeRange 数据模型

  - 定义 TimeRange 数据类，包含 startHour 和 endHour 字段
  - _Requirements: 7.1, 7.2_

- [x] 5.2 实现时间范围计算函数

  - 实现 calculateOptimalTimeRange 函数
  - 根据实际课程时间动态计算时间轴范围
  - 考虑第一节课开始时间设置
  - 仅显示有课程的时间范围
  - _Requirements: 7.1, 7.2, 7.3, 7.4_

- [x] 5.3 集成时间范围计算

  - 在 ContinuousTimelineView 中使用 calculateOptimalTimeRange
  - 替换固定的时间范围
  - _Requirements: 7.1, 7.2, 7.3, 7.4_

- [x] 6. 更新 CourseScreen 过滤逻辑

- [x] 6.1 修改 HorizontalPager 内容

  - 在每一页中根据 pageWeek 过滤课程
  - 使用 viewModel.isCourseActiveInWeek 方法
  - 传递已过滤的 coursesByDay 给 ContinuousTimelineView
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 4.1, 4.2, 4.3, 5.1, 5.2, 5.3_

- [x] 6.2 验证空状态处理

  - 确保当某一周没有课程时显示 EmptyCourseList
  - _Requirements: 4.3_

- [x] 7. 测试和验证

- [x] 7.1 编写单元测试

  - 测试 isCourseActiveInWeek 方法的所有 WeekPattern 类型
  - 测试 analyzeTimeSlots 函数
  - 测试 calculateCoursePosition 函数
  - 测试 calculateOptimalTimeRange 函数
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 2.1, 2.2, 2.3, 6.1, 6.2, 6.3, 6.4, 6.5, 7.1, 7.2, 7.3, 7.4_

- [x] 7.2 进行 UI 测试

  - 测试单周课程只在单周显示
  - 测试双周课程只在双周显示
  - 测试自定义周课程在指定周显示
  - 测试空白时间段被正确压缩
  - 测试课程位置和高度正确
  - 测试周模式标识显示正确
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 2.1, 2.2, 2.3, 4.1, 4.2, 4.3, 4.4, 6.1, 6.2, 6.3, 6.4, 6.5, 8.1, 8.2, 8.3, 8.4, 8.5_

- [x] 7.3 性能测试

  - 测试大量课程时的渲染性能
  - 测试滑动切换周数的流畅度
  - 优化必要的性能瓶颈
  - _Requirements: 4.1, 4.2, 4.3_

- [x] 8. 文档和清理


- [x] 8.1 更新代码注释

  - 为新增的函数和组件添加详细注释
  - 说明算法逻辑和参数含义
  - _Requirements: All_

- [x] 8.2 清理旧代码

  - 移除 ContinuousTimelineView 中的旧过滤逻辑
  - 移除不再使用的组件和函数
  - _Requirements: 5.5_
