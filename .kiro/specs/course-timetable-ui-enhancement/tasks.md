# 课程表UI优化实现任务

## 任务列表

- [x] 1. 创建PeriodTimeColumn组件





  - 创建新的Composable组件用于显示节次和时间
  - 实现紧凑的布局设计（宽度60dp）
  - 显示节次编号（12sp）和时间范围（10sp）
  - 实现当前节次高亮效果
  - 使用8dp内边距
  - _需求: 1.1, 1.2, 1.3, 1.4, 1.5_

- [x] 2. 优化DayHeaderRow组件



  - 修改组件以显示星期名称和日期
  - 实现日期计算逻辑（基于学期开始日期和当前周）
  - 调整高度为56dp
  - 实现当前日期高亮效果
  - 确保使用weight(1f)平分宽度
  - _需求: 3.1, 3.2, 3.3, 3.4, 3.5_

- [x] 3. 优化CourseGridCard组件



  - 调整圆角为10dp
  - 调整内边距为8dp
  - 优化文字大小（课程名13sp，地点11sp，时间10sp）
  - 调整阴影为1dp（普通）/3dp（当前课程）
  - 优化布局间距（4dp和2dp）
  - _需求: 2.1, 2.2, 2.3, 2.4, 2.5_

- [x] 4. 优化GridCell组件



  - 调整边框为0.5dp
  - 调整圆角为4dp
  - 优化背景色透明度
  - 确保涟漪效果正常工作
  - _需求: 4.2, 4.3, 5.1, 5.4_

- [x] 5. 重构GridTimetableView组件



- [x] 5.1 移除水平滚动功能


  - 移除horizontalScroll修饰符
  - 确保课程网格使用fillMaxWidth()
  - _需求: 4.1, 4.6, 7.7_

- [x] 5.2 实现紧凑布局


  - 更新GridLayoutConfig使用紧凑尺寸
  - 将节次列宽度改为60dp
  - 将单元格高度改为70dp
  - 将间距改为2dp
  - _需求: 4.1, 4.2, 4.7_

- [x] 5.3 修改GridDayColumn使用weight


  - 将所有GridDayColumn改为使用Modifier.weight(1f)
  - 移除固定宽度设置
  - 确保7天平分可用宽度
  - _需求: 4.6, 7.7_

- [x] 5.4 集成PeriodTimeColumn


  - 替换PeriodHeaderColumn和TimeHeaderColumn
  - 使用新的PeriodTimeColumn组件
  - 调整左侧列宽度为60dp
  - _需求: 1.1, 1.2, 1.3_

- [x] 6. 在CourseScreen中实现HorizontalPager




- [x] 6.1 添加HorizontalPager依赖

  - 确保项目使用Compose Foundation 1.5+
  - 添加必要的导入
  - _需求: 7.2, 7.3_


- [x] 6.2 实现HorizontalPager布局

  - 创建PagerState（初始页设置为当前周）
  - 包装GridTimetableView在HorizontalPager中
  - 实现页面到周次的转换逻辑
  - _需求: 7.1, 7.2, 7.3, 7.4_


- [x] 6.3 连接ViewModel

  - 监听pagerState.currentPage变化
  - 调用viewModel.setWeekOffset更新周次
  - 确保课程数据根据周次正确过滤
  - _需求: 7.1, 7.2, 7.3_


- [x] 6.4 优化Pager性能

  - 设置合理的pageCount（如总周数+缓冲）
  - 实现页面预加载策略
  - 确保滑动流畅
  - _需求: 7.4_

- [x] 7. 更新GridLayoutConfig数据模型


  - 修改默认值为紧凑尺寸
  - cellHeight = 70.dp
  - periodColumnWidth = 60.dp
  - headerHeight = 56.dp
  - cardCornerRadius = 10.dp
  - cellCornerRadius = 4.dp
  - cardElevation = 1.dp
  - spacing = 2.dp
  - _需求: 4.1, 4.2_

- [x] 8. 添加空状态视图组件


  - 创建EmptyStateView Composable
  - 使用Icons.Outlined.EventNote图标
  - 显示友好的提示文字
  - 提供"添加课程"操作按钮
  - _需求: 8.1, 8.2, 8.3, 8.4_

- [x] 9. 更新字符串资源







  - 添加period_number_format字符串
  - 添加time_range_format字符串
  - 添加week_number_format字符串
  - 添加empty_timetable_message字符串
  - 添加empty_timetable_action字符串
  - 添加back_to_current_week字符串
  - 为所有支持的语言添加翻译（中文、日文、越南文）
  - _需求: 所有需求_

- [x] 10. 优化颜色方案



  - 确保课程卡片使用柔和色调
  - 验证文字对比度符合WCAG AA标准
  - 优化当前节次高亮色
  - 确保主题切换时颜色正确适配
  - _需求: 6.1, 6.2, 6.3, 6.4, 6.5_

- [x] 11. 实现交互反馈



  - 确保课程卡片点击有涟漪效果
  - 实现空白单元格长按高亮
  - 优化卡片按下时的阴影变化
  - 确保所有交互动画流畅（<200ms）
  - _需求: 5.1, 5.2, 5.3, 5.5_

- [ ] 12. 测试和验证
- [ ] 12.1 测试紧凑布局
  - 验证全周课程在标准屏幕上可见
  - 测试不同屏幕尺寸的显示效果
  - 确认文字可读性
  - _需求: 4.6, 4.7_

- [ ] 12.2 测试周切换功能
  - 验证左滑切换到下一周
  - 验证右滑切换到上一周
  - 测试滑动动画流畅性
  - 验证课程数据正确更新
  - _需求: 7.2, 7.3, 7.4_

- [ ] 12.3 测试垂直滚动
  - 验证LazyColumn滚动流畅
  - 测试自动滚动到当前节次
  - 确认大量节次时的性能
  - _需求: 4.1, 4.2_

- [ ] 12.4 测试主题适配
  - 验证浅色主题显示效果
  - 验证深色主题显示效果
  - 测试主题切换时的颜色变化
  - _需求: 6.5_

- [ ] 12.5 测试空状态
  - 验证无课程时显示空状态视图
  - 测试"添加课程"按钮功能
  - _需求: 8.1, 8.2, 8.4_
