# 实现计划

- [x] 1. 创建主题调色板定义


  - 为所有14个主题（樱花、薄荷、琥珀、雪、雨、枫叶、海洋、日落、森林、薰衣草、沙漠、极光、科技紫、黑曼巴）定义浅色和深色模式的12种课程颜色
  - 确保每个主题的颜色与主题整体色调协调
  - 验证所有颜色组合满足 WCAG 对比度要求
  - _Requirements: 3.2, 3.4, 1.3_

- [x] 2. 实现 CourseColorMapper 工具类



  - [x] 2.1 实现颜色索引提取函数

    - 编写 `extractColorIndex(colorInt: Int): Int` 函数
    - 从颜色整数值的最高4位提取索引
    - 处理旧数据（没有编码索引）的情况，返回默认索引0
    - _Requirements: 3.1, 4.2, 4.3_
  

  - [ ] 2.2 实现主题调色板获取函数
    - 编写 `getThemeCoursePalette(theme: SeasonalTheme, isDark: Boolean): List<Color>` 函数
    - 根据主题和深色模式返回对应的12种颜色列表
    - 处理未知主题类型，返回默认调色板
    - _Requirements: 3.2, 3.3_

  
  - [ ] 2.3 实现主题颜色映射函数
    - 编写 `mapToThemeColor(colorInt: Int, theme: SeasonalTheme, isDark: Boolean): Color` 函数
    - 提取颜色索引并从当前主题调色板中获取对应颜色
    - 确保函数性能优化，避免重复计算
    - _Requirements: 1.1, 1.2, 3.1, 3.3_

- [x] 3. 创建 Composable 颜色辅助函数


  - 实现 `rememberThemeAwareCourseColor(colorInt: Int): Color` Composable 函数
  - 从 PreferencesManager 获取当前主题设置
  - 使用 `remember` 缓存颜色计算结果，依赖于 colorInt、theme 和 isDark
  - 确保主题切换时自动触发重组
  - _Requirements: 1.1, 1.2, 3.3_

- [x] 4. 更新 CourseCard 组件



  - [x] 4.1 替换硬编码颜色为主题感知颜色

    - 将所有 `Color(course.color)` 替换为 `rememberThemeAwareCourseColor(course.color)`
    - 更新卡片背景色、边框色和颜色条
    - 更新 WeekPatternChip 中的颜色使用
    - _Requirements: 1.1, 1.2, 1.4_
  

  - [ ] 4.2 验证颜色对比度
    - 测试所有主题下的文字可读性
    - 确保浅色和深色模式下都有足够的对比度
    - 调整透明度值以优化视觉效果
    - _Requirements: 1.3, 1.5_

- [x] 5. 更新 CourseColorAssigner



  - [x] 5.1 实现颜色索引编码

    - 修改 `assignColor` 函数，将颜色索引编码到返回值中
    - 使用公式：`(colorIndex shl 28) or (selectedColor and 0x0FFFFFFF)`
    - 保持现有的颜色选择逻辑（避免相邻课程颜色重复）
    - _Requirements: 3.1, 4.1, 4.5_
  
  - [x] 5.2 重构颜色选择逻辑

    - 将颜色选择逻辑改为返回索引而不是颜色值
    - 创建 `selectColorIndex` 私有函数
    - 确保索引选择考虑纵向和横向相邻课程
    - _Requirements: 1.4, 3.1_

- [x] 6. 更新其他使用课程颜色的组件


  - 搜索代码库中所有使用 `Color(course.color)` 的地方
  - 更新 CourseTimelineView、WeeklyTimelineView、ContinuousTimelineView 等组件
  - 更新 AddCourseSheet 和 CourseSettingsSheet 中的颜色预览
  - 确保所有课程颜色显示都使用主题感知函数
  - _Requirements: 1.1, 1.2_

- [x] 7. 验证数据同步机制



  - [x] 7.1 检查 CourseRepository 的 Flow 实现

    - 确认 `getAllCourses()` 和 `getCoursesBySemester()` 返回的 Flow 正确发射更新
    - 验证数据库操作后 Flow 自动发射新数据
    - 添加日志以跟踪数据流
    - _Requirements: 2.1, 2.2_
  

  - [x] 7.2 优化 CourseViewModel 的数据流

    - 检查 StateFlow 的配置，确保使用 `distinctUntilChanged()`
    - 验证 `SharingStarted.WhileSubscribed(5000)` 的超时设置合理
    - 确保 ViewModel 正确处理数据更新
    - _Requirements: 2.1, 2.3, 2.5_

  

  - [ ] 7.3 测试 Compose 重组触发
    - 验证数据变化时 CourseCard 正确重组
    - 测试导入课程后列表立即更新
    - 测试修改课程后卡片立即刷新
    - _Requirements: 2.2, 2.3, 2.4_

- [x] 8. 编写单元测试



  - [x] 8.1 CourseColorMapper 测试

    - 测试 `extractColorIndex` 函数的各种输入
    - 测试所有主题的调色板获取
    - 测试 `mapToThemeColor` 的颜色映射正确性
    - 测试向后兼容性（旧颜色值处理）
    - _Requirements: 4.2, 4.3_
  
  - [x] 8.2 CourseColorAssigner 测试

    - 测试颜色索引编码和解码
    - 测试相邻课程颜色不重复的逻辑
    - 测试边界情况（空课程列表、所有颜色都被占用等）
    - _Requirements: 1.4, 3.1_
  
  - [x] 8.3 数据同步测试

    - 测试 CourseRepository 的 Flow 发射行为
    - 测试 CourseViewModel 的数据流转换
    - 测试数据更新后 StateFlow 的变化
    - _Requirements: 2.1, 2.5_

- [x] 9. 编写 UI 测试

  - [x] 9.1 CourseCard 主题响应测试

    - 测试主题切换时颜色更新
    - 测试深色/浅色模式切换
    - 测试所有主题下的颜色显示
    - _Requirements: 1.1, 1.2, 1.3_
  

  - [x] 9.2 数据同步 UI 测试

    - 测试导入课程后 UI 立即更新
    - 测试修改课程后卡片立即刷新
    - 测试删除课程后卡片立即移除
    - _Requirements: 2.2, 2.3, 2.4_

  

  - [ ] 9.3 可访问性测试
    - 测试所有颜色组合的对比度
    - 测试屏幕阅读器兼容性
    - 测试大字体模式下的显示
    - _Requirements: 1.5_




- [ ] 10. 性能优化和验证
  - [ ] 10.1 颜色映射性能优化
    - 使用 Profiler 测量颜色计算的性能影响
    - 优化 `remember` 的依赖项，避免不必要的重组

    - 考虑使用 `derivedStateOf` 进一步优化

    - _Requirements: 3.5_
  
  - [ ] 10.2 数据同步性能优化
    - 测试大量课程（100+）时的性能

    - 确保 Flow 不会导致内存泄漏
    - 优化数据库查询和 Flow 转换
    - _Requirements: 2.5, 3.5_
  
  - [x] 10.3 整体性能验证


    - 在低端设备上测试应用性能
    - 确保主题切换流畅无卡顿
    - 确保课程列表滚动流畅
    - _Requirements: 3.5_

- [x] 11. 集成测试和用户验证

  - 测试完整的用户流程：导入课程 → 切换主题 → 修改课程 → 验证颜色
  - 测试向后兼容性：使用旧版本创建的课程数据
  - 测试边界情况：空课程列表、单个课程、大量课程
  - 进行用户体验测试，收集反馈
  - _Requirements: 4.1, 4.2, 4.4, 4.5_
