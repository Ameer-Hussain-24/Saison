# 课程单双周显示修复 - 任务列表

- [x] 1. 添加诊断日志


  - 在 CourseScreen 的 HorizontalPager 中添加日志输出
  - 记录 page、centerPage、currentWeek、pageWeek 的值
  - 记录过滤前后的课程数量
  - 记录每个课程的名称、周模式、是否被过滤
  - _Requirements: 6.1, 6.2, 6.3_

- [x] 2. 在 CourseViewModel 中添加日志


  - 在 isCourseActiveInWeek 方法中添加日志输出
  - 记录课程名称、周模式、周数、返回值
  - 在 currentWeek 计算中添加日志输出
  - 记录 baseWeek、offset、最终周数
  - _Requirements: 6.1, 6.2, 6.3_

- [x] 3. 运行应用并收集日志

  - 启动应用并打开课程表页面
  - 查看 Logcat 输出
  - 记录当前周数和显示的课程
  - 左右滑动切换周数，观察日志变化
  - 分析日志，确定问题所在
  - _Requirements: 6.1, 6.2, 6.3_

- [x] 4. 修复 isCourseActiveInWeek 方法（如果需要）


  - 验证 WeekPattern.ODD 的判断逻辑（week % 2 == 1）
  - 验证 WeekPattern.EVEN 的判断逻辑（week % 2 == 0）
  - 验证 WeekPattern.CUSTOM 的判断逻辑
  - 添加周数有效性检查（week >= 1）
  - 添加 customWeeks 空值检查
  - _Requirements: 1.1, 1.2, 1.3, 2.1, 2.2, 2.3, 4.1, 4.2, 4.3, 4.4_


- [x] 5. 修复周数计算（如果需要）

  - 验证 getCurrentWeekNumber 方法的实现
  - 确保学期开始日期正确设置
  - 验证 WeekCalculator 的计算逻辑
  - 添加日志输出帮助调试
  - _Requirements: 1.1, 1.2, 1.3, 2.1, 2.2, 2.3_


- [ ] 6. 修复过滤逻辑（如果需要）
  - 验证 CourseScreen 中的过滤代码
  - 确保 filteredCoursesByDay 正确传递给 ContinuousTimelineView
  - 确保没有重复过滤
  - _Requirements: 1.1, 1.2, 1.3, 2.1, 2.2, 2.3, 3.1, 3.2, 4.1, 4.2, 4.3, 4.4_

- [x] 7. 增强周模式标识显示


  - 确保 CourseBlock 组件显示周模式标识
  - 为 ODD 模式显示"单周"
  - 为 EVEN 模式显示"双周"
  - 为 A 模式显示"A周"
  - 为 B 模式显示"B周"
  - 为 CUSTOM 模式显示"自定义"
  - 仅在 weekPattern 不为 ALL 时显示标识
  - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.5_


- [ ] 8. 手动测试验证
  - 创建测试课程（全周、单周、双周、自定义周）
  - 验证第1周（单周）显示正确的课程
  - 验证第2周（双周）显示正确的课程
  - 验证第3周（单周）显示正确的课程
  - 验证第4周（双周）显示正确的课程
  - 验证周模式标识正确显示
  - _Requirements: 1.1, 1.2, 1.3, 2.1, 2.2, 2.3, 3.1, 3.2, 4.1, 4.2, 4.3, 4.4, 5.1, 5.2, 5.3, 5.4, 5.5_


- [x] 9. 编写单元测试

  - 测试 isCourseActiveInWeek 方法的 ALL 模式
  - 测试 isCourseActiveInWeek 方法的 ODD 模式
  - 测试 isCourseActiveInWeek 方法的 EVEN 模式
  - 测试 isCourseActiveInWeek 方法的 CUSTOM 模式
  - 测试边界情况（week=0, week<0, customWeeks=null）
  - _Requirements: 1.1, 1.2, 1.3, 2.1, 2.2, 2.3, 4.1, 4.2, 4.3, 4.4_


- [x] 10. 清理和文档


  - 移除或注释掉调试日志（保留关键日志）
  - 更新代码注释
  - 记录修复的问题和解决方案
  - _Requirements: All_
