# Implementation Plan

- [x] 1. 实现核心时间匹配逻辑




  - 创建PeriodMatcher对象，实现节次匹配算法
  - 实现从描述中提取节次信息的功能（支持"第7-8节"格式）
  - 实现基于时间重叠度的智能匹配算法
  - 实现跨节次课程的处理逻辑
  - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5_

- [x] 2. 实现节次自动生成功能



  - 创建PeriodGenerator对象
  - 实现从ParsedCourse列表提取唯一时间段的功能
  - 实现时间段排序和节次编号分配
  - 实现午休时间识别逻辑
  - 实现节次去重和合并逻辑
  - _Requirements: 3.1, 3.2, 3.3, 3.4_

- [x] 3. 创建时间匹配选项对话框


  - 创建TimeMatchingDialog组件
  - 实现"使用当前课表时间"选项UI
  - 实现"根据导入文件自动匹配时间"选项UI
  - 添加选项说明文字和图标
  - 实现用户选择回调处理
  - _Requirements: 1.1, 1.2, 1.3_


- [x] 4. 增强CourseConverter

  - 修改toCourse方法，添加strategy参数
  - 实现UseExistingPeriods策略的转换逻辑
  - 实现AutoCreatePeriods策略的转换逻辑
  - 集成PeriodMatcher进行时间匹配
  - 处理自定义时间课程的情况
  - _Requirements: 1.4, 1.5, 2.1, 2.2, 2.3_


- [x] 5. 增强IcsImportUseCase

  - 修改previewImport方法，添加strategy和existingPeriods参数
  - 实现AutoCreatePeriods模式下的节次生成
  - 创建ImportPreviewResult数据类
  - 实现applyGeneratedPeriods方法保存节次配置
  - 添加匹配警告信息收集
  - _Requirements: 3.5, 4.1, 4.2, 4.4_


- [x] 6. 更新ImportPreviewViewModel

  - 添加timeMatchingStrategy状态
  - 添加generatedPeriods状态
  - 修改loadPreview方法，传递strategy参数
  - 实现节次配置应用逻辑
  - 添加匹配结果显示逻辑
  - _Requirements: 1.4, 1.5, 4.4, 4.5_


- [x] 7. 更新ImportPreviewScreen UI

  - 显示时间匹配策略信息
  - 显示生成的节次配置（AutoCreatePeriods模式）
  - 显示每门课程的匹配结果和置信度
  - 添加匹配警告提示
  - 优化课程预览卡片，显示节次信息
  - _Requirements: 4.4, 4.5_


- [x] 8. 集成时间匹配对话框到导入流程

  - 修改CourseScreen的导入触发逻辑
  - 在文件选择后显示TimeMatchingDialog
  - 根据用户选择调用不同的导入逻辑
  - 传递选择的策略到ImportPreviewScreen
  - _Requirements: 1.1, 1.4, 1.5_


- [x] 9. 实现错误处理和用户反馈


  - 创建TimeMatchingException类型
  - 实现时间格式错误处理
  - 实现节次冲突检测和提示
  - 实现无匹配节次的降级处理
  - 在UI中显示具体的错误信息
  - _Requirements: 4.1, 4.2, 4.3_

- [ ]* 10. 添加单元测试
  - 编写PeriodMatcher测试用例
  - 编写PeriodGenerator测试用例
  - 编写CourseConverter增强逻辑测试
  - 编写时间重叠算法测试
  - 编写节次提取正则表达式测试
  - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5, 3.1, 3.2, 3.3, 3.4_

- [ ]* 11. 添加集成测试
  - 测试完整的导入流程（两种策略）
  - 测试节次配置的保存和应用
  - 测试错误场景的处理
  - 测试实际ICS文件的导入
  - _Requirements: 1.4, 1.5, 3.5, 4.1, 4.2, 4.3_
