# 课程表网格布局重构 - 实现任务

## 任务列表

- [x] 1. 创建数据模型和配置




  - 创建 `BreakPeriod` 数据类用于表示休息时段
  - 扩展 `GridLayoutConfig` 添加 `headerHeight` 和 `showBreakSeparators` 字段
  - 扩展 `CourseSettings` 添加 `breakPeriods` 字段，默认包含午休和晚修
  - _Requirements: 4.1, 4.2, 4.3, 4.4_

- [x] 2. 实现周次选择器组件


- [x] 2.1 创建 WeekSelectorButton 组件


  - 实现显示当前周次的按钮（格式："X周"）
  - 使用 Material 3 FilledTonalButton 样式
  - 添加点击事件处理
  - _Requirements: 1.1, 1.5_

- [x] 2.2 创建 WeekSelectorBottomSheet 组件


  - 实现 ModalBottomSheet 容器
  - 使用 LazyColumn 显示周次列表（阿拉伯数字）
  - 高亮显示当前周次
  - 实现选择周次后自动关闭
  - _Requirements: 1.2, 1.3, 1.4, 1.5_

- [x] 3. 实现日期头部组件


- [x] 3.1 创建 WeekDateHeader 组件


  - 实现水平排列的日期列
  - 显示星期几（一、二、三...）
  - 显示具体日期（11/3, 11/4...）
  - 实现当天高亮显示
  - _Requirements: 2.1, 2.2, 2.3_

- [x] 3.2 实现日期计算工具函数


  - 创建 `getDateForDayInWeek` 函数计算指定周和星期的日期
  - 创建 `getDayShortName` 函数获取星期简称
  - 处理跨月和跨年的日期计算
  - _Requirements: 2.1, 2.4_

- [x] 4. 实现时间轴组件


- [x] 4.1 创建 PeriodTimeCell 组件


  - 显示节次号、开始时间、结束时间
  - 实现当前节次高亮
  - 使用固定宽度（60dp）
  - 设置为不可编辑
  - _Requirements: 3.1, 3.2, 3.3, 3.4_

- [x] 4.2 创建 PeriodTimeColumn 组件


  - 垂直排列所有节次单元格
  - 集成休息时段分隔单元格
  - 实现固定位置（滚动时保持在左侧）
  - _Requirements: 3.1, 3.4, 6.3_

- [x] 5. 实现休息时段分隔组件


- [x] 5.1 创建 BreakSeparatorCell 组件

  - 显示休息时段名称（午休/晚修）
  - 使用 surfaceContainer 背景色
  - 设置固定高度（32dp）
  - _Requirements: 4.1, 4.2, 4.4_

- [x] 5.2 创建 BreakSeparatorRow 组件


  - 横跨所有日期列显示分隔行
  - 居中显示休息时段名称
  - 与课程卡片使用不同的视觉样式
  - _Requirements: 4.1, 4.2, 4.3, 4.4_

- [x] 6. 实现课程网格单元格组件


- [x] 6.1 创建 CourseCardCompact 组件


  - 显示课程名称（粗体）
  - 显示地点信息（小字）
  - 使用课程主题色作为背景
  - 实现文字自动截断
  - 添加点击事件处理
  - _Requirements: 5.2, 5.3, 7.1, 7.2_

- [x] 6.2 创建 CourseGridCell 组件


  - 根据是否有课程显示不同内容
  - 有课程时显示 CourseCardCompact
  - 无课程时显示空白可点击区域
  - 实现点击回调（课程点击和空白点击）
  - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.5_

- [x] 7. 重构 GridTimetableView 组件


- [x] 7.1 实现固定头部布局


  - 创建包含周次选择器和日期头部的固定区域
  - 确保头部在滚动时保持固定
  - 实现左上角占位符与时间列对齐
  - _Requirements: 6.2, 6.3, 6.4_

- [x] 7.2 实现可滚动网格内容

  - 创建包含时间列和课程网格的可滚动区域
  - 使用共享 ScrollState 实现同步滚动
  - 实现时间列固定在左侧
  - _Requirements: 6.1, 6.3, 6.4_

- [x] 7.3 集成休息时段分隔逻辑

  - 在正确的节次位置插入分隔行
  - 确保分隔行在时间列和课程网格中都显示
  - 处理分隔行的间距和对齐
  - _Requirements: 4.1, 4.2, 4.3_

- [x] 7.4 实现课程网格渲染

  - 遍历所有节次和星期创建网格单元格
  - 根据课程数据填充对应单元格
  - 处理跨多个节次的课程显示
  - 确保网格单元格对齐且大小一致
  - _Requirements: 5.1, 5.2, 6.5_

- [x] 8. 更新 CourseScreen 集成新组件


- [x] 8.1 添加周次选择器状态管理


  - 在 CourseViewModel 中添加 `selectedWeek` 状态
  - 实现周次选择逻辑
  - 添加显示/隐藏底部抽屉的状态
  - _Requirements: 1.1, 1.2, 1.4_

- [x] 8.2 集成 WeekSelectorButton 和 BottomSheet


  - 在 CourseScreen 顶部添加 WeekSelectorButton
  - 添加 WeekSelectorBottomSheet 并连接状态
  - 实现周次选择后更新课程表显示
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5_

- [x] 8.3 替换现有 GridTimetableView

  - 使用重构后的 GridTimetableView 替换现有实现
  - 传递所有必需的参数（包括 breakPeriods）
  - 确保所有现有功能正常工作
  - _Requirements: 2.4, 5.1, 5.2, 5.3, 5.4, 5.5_

- [x] 9. 实现主题适配



- [x] 9.1 应用 Material Design 3 颜色系统

  - 确保所有组件使用 Material 3 颜色令牌
  - 实现课程卡片颜色与主题的协调
  - 确保网格线使用适当的分隔线颜色
  - _Requirements: 7.1, 7.2, 7.3, 7.4_

- [x] 9.2 验证可访问性对比度

  - 检查所有文本颜色与背景的对比度
  - 确保符合 WCAG 标准
  - 调整不符合标准的颜色组合
  - _Requirements: 7.5_

- [ ]* 10. 添加国际化支持
  - 将所有硬编码文本移至 strings.xml
  - 添加星期名称的多语言资源
  - 添加休息时段名称的多语言资源
  - 确保日期格式根据系统语言自动调整
  - _Requirements: 2.1, 4.1, 4.2_

- [ ]* 11. 性能优化
  - 使用 `remember` 缓存日期计算结果
  - 优化课程过滤逻辑避免重复计算
  - 确保滚动流畅无卡顿
  - 测试大量课程数据下的性能
  - _Requirements: 6.1, 6.5_

- [ ]* 12. 添加可访问性支持
  - 为所有可点击元素添加 contentDescription
  - 确保所有触摸目标至少 48dp
  - 测试 TalkBack 导航
  - 添加语义标签
  - _Requirements: 5.3, 5.4, 5.5_
