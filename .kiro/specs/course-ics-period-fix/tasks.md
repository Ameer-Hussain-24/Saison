# Implementation Plan

- [x] 1. 实现自动学期开始日期检测




- [x] 1.1 在IcsImportUseCase中检测最早的课程日期


  - 从所有ParsedCourse中提取dtStart日期
  - 找到最早的日期
  - 计算该日期所在周的周一作为学期开始日期
  - 添加日志记录检测到的日期
  - _Requirements: 3.1, 3.2, 3.5_

- [x] 1.2 自动设置或更新学期开始日期


  - 如果学期不存在，创建新学期并使用检测到的开始日期
  - 如果学期已存在但开始日期不同，更新学期开始日期
  - 记录学期开始日期和当前周次
  - _Requirements: 3.3, 3.4, 3.5_

- [x] 2. 添加调试日志以诊断问题


  - 在IcsParser中添加日志记录description字段内容
  - 在PeriodMatcher中添加详细的日志记录
  - 在CourseConverter中添加日志记录periodStart和periodEnd的值
  - 在IcsImportUseCase中记录学期开始日期和周次计算
  - _Requirements: 2.1, 2.2, 2.3, 2.4_

- [x] 3. 修复PeriodMatcher的节次提取逻辑



- [x] 3.1 增强extractPeriodFromDescription的正则表达式

  - 支持多种连字符格式（-、~、－、—）
  - 支持带空格和不带空格的格式（如"第7 - 8节"）
  - 支持带"第"和不带"第"的格式
  - 添加详细的日志记录每个正则表达式的匹配结果
  - _Requirements: 1.1, 1.2, 2.2_


- [x] 3.2 移除或放宽节次范围验证

  - 允许提取的节次号超出existingPeriods的范围
  - 只验证节次号是否为正数和顺序是否正确
  - _Requirements: 1.3_

- [x] 4. 修复CourseConverter的节次分配逻辑



- [x] 4.1 优先使用描述中的节次信息

  - 确保extractPeriodFromDescription的结果被正确使用
  - 只在提取失败时才使用时间匹配算法
  - _Requirements: 1.3, 1.4_


- [x] 4.2 正确处理时间匹配失败的情况

  - 当时间匹配也失败时，设置isCustomTime为true
  - 记录警告日志
  - _Requirements: 1.5, 2.3_

- [x] 5. 修复周次显示和滑动边界



- [x] 5.1 修复周次计算逻辑

  - 确保getCurrentWeekNumber正确使用学期开始日期
  - 添加日志记录周次计算过程
  - 验证baseWeek的计算结果
  - _Requirements: 4.1, 4.2_


- [x] 5.2 限制HorizontalPager的滑动范围

  - 在CourseScreen中添加滑动边界检查
  - 当到达第1周时禁止向左滑动
  - 当到达最后一周时禁止向右滑动
  - _Requirements: 4.3, 4.4_


- [x] 5.3 确保"回到当前周"功能正常工作

  - 验证weekOffset的计算和更新
  - 确保点击按钮后正确滑动到当前周
  - _Requirements: 4.5, 4.6_

- [x] 6. 验证和测试



- [x] 6.1 使用真实ICS文件测试导入功能

  - 导入提供的"日历-25下.ics"文件
  - 检查导入后的课程数据
  - 验证periodStart和periodEnd字段的值
  - 验证学期开始日期是否正确设置为2025年8月25日（周一）
  - 验证当前周次计算是否正确
  - _Requirements: 1.1, 1.2, 1.3, 3.1, 3.2, 3.3, 3.4_



- [x] 6.2 验证UI显示

  - 检查课程是否显示在正确的节次位置
  - 验证课程卡片的高度和位置
  - 验证周次显示是否正确（如"第11/18周"）
  - 验证左右滑动时周次是否正确更新
  - 验证滑动边界是否生效（不能滑到第0周或第19周）
  - _Requirements: 4.1, 4.2, 4.3, 4.4, 5.1, 5.2_

- [ ]* 6.3 添加单元测试
  - 测试extractPeriodFromDescription对各种格式的支持
  - 测试toCourse的节次分配逻辑
  - 测试学期开始日期检测逻辑
  - _Requirements: 1.1, 1.2, 1.3, 3.1, 3.2_
