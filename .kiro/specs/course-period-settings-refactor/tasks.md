# 课程节次设置重构任务列表

- [x] 1. 更新数据模型




  - 在CourseSettings中添加morningPeriods、afternoonPeriods、eveningPeriods字段
  - 移除timelineCompactness字段
  - 添加totalPeriods计算属性
  - 添加dinnerBreakDuration字段
  - _Requirements: 1.2, 1.3, 2.1, 2.2, 2.3_

- [x] 2. 添加TimeOfDay枚举



  - 创建TimeOfDay枚举类（MORNING, AFTERNOON, EVENING）
  - 在CoursePeriod中添加timeOfDay字段
  - _Requirements: 4.1, 4.2, 4.3_

- [x] 3. 实现节次生成器



  - 在PeriodGenerator中添加generatePeriodsFromSegments方法
  - 实现上午节次生成逻辑
  - 实现下午节次生成逻辑
  - 实现晚上节次生成逻辑
  - 自动插入午休和晚休时段
  - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5_

- [x] 4. 更新CourseSettingsSheet UI



  - 移除时间轴紧凑度滑块
  - 添加上午节次数选择器（1-6节）
  - 添加下午节次数选择器（1-6节）
  - 添加晚上节次数选择器（0-3节）
  - 添加节次预览列表
  - _Requirements: 1.1, 2.4, 2.5_

- [x] 5. 更新课程模板



  - 更新大学模板为morningPeriods=4, afternoonPeriods=4, eveningPeriods=1
  - 更新高中模板配置
  - 更新初中模板配置
  - _Requirements: 3.1, 3.2, 3.3, 3.4_

- [x] 6. 实现数据迁移




  - 添加migrateOldSettings方法
  - 在PreferencesManager中处理旧数据读取
  - 确保向后兼容性
  - _Requirements: 2.1, 2.2, 2.3_

- [x] 7. 更新相关组件



  - 更新PeriodPreviewList以显示时段信息
  - 更新CourseViewModel以使用新的配置
  - 更新所有引用periodsPerDay的代码
  - 移除所有引用timelineCompactness的代码
  - _Requirements: 1.1, 1.2, 1.3, 2.4_

- [x] 8. 添加验证逻辑



  - 实现节次数范围验证
  - 实现总节次数验证
  - 添加错误提示
  - _Requirements: 2.4, 2.5_

- [x] 9. 更新国际化资源



  - 添加"上午节次"、"下午节次"、"晚上节次"字符串
  - 添加验证错误提示字符串
  - 更新所有语言的翻译
  - _Requirements: 2.4_

- [ ]* 10. 测试
  - 编写CourseSettings.totalPeriods单元测试
  - 编写PeriodGenerator单元测试
  - 测试UI交互和数据保存
  - 测试数据迁移逻辑
  - 测试模板应用
  - _Requirements: 所有需求_
