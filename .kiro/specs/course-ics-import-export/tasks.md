# 课程表导入导出功能实施任务

- [x] 1. 创建数据模型和转换器


  - 创建ParsedCourse、RecurrenceInfo等中间数据类
  - 实现CourseConverter对象，包含toCourse和groupAndConvert方法
  - 实现自定义周次识别算法detectWeekPattern
  - 实现周数计算方法calculateWeekNumber
  - _需求: 2.1, 2.2, 2.3, 2.4, 2.5, 2.6, 3.1, 3.2, 3.3, 3.4_

- [x] 2. 实现ICS解析器


  - [x] 2.1 创建IcsParser类和基础结构


    - 定义IcsParser类和主要方法签名
    - 创建IcsException异常类层次结构
    - 实现基础的文件读取和行分割逻辑
    - _需求: 1.2, 7.1, 7.2_
  
  - [x] 2.2 实现VEVENT解析

    - 实现parseVEvent方法，解析单个VEVENT组件
    - 解析SUMMARY、LOCATION、DESCRIPTION字段
    - 解析DTSTART和DTEND时间字段
    - 处理时区信息（TZID参数）
    - _需求: 1.3, 2.1, 2.2, 2.3_
  
  - [x] 2.3 实现RRULE解析

    - 实现parseRRule方法，解析重复规则
    - 解析FREQ、INTERVAL、UNTIL、BYDAY参数
    - 创建RecurrenceInfo数据结构
    - _需求: 2.4, 2.5, 3.1, 3.4_
  
  - [x] 2.4 实现节次和提醒解析

    - 实现extractPeriodInfo方法，从DESCRIPTION提取节次
    - 解析VALARM组件，提取提醒时间
    - 处理各种TRIGGER格式（PT20M等）
    - _需求: 2.6, 2.7_
  
  - [x] 2.5 实现parseFromUri方法

    - 使用ContentResolver读取Uri内容
    - 调用parse方法解析内容
    - 添加文件大小验证（限制10MB）
    - 实现错误处理和异常转换
    - _需求: 1.1, 1.2, 7.1, 7.3_

- [x] 3. 实现ICS生成器


  - [x] 3.1 创建IcsGenerator类和基础结构


    - 定义IcsGenerator类和主要方法签名
    - 实现generateVCalendar方法框架
    - 实现formatDateTime时间格式化方法
    - _需求: 4.3, 4.7_
  
  - [x] 3.2 实现VEVENT生成

    - 实现generateVEvent方法，生成单个VEVENT
    - 生成SUMMARY、LOCATION、DESCRIPTION字段
    - 生成DTSTART、DTEND时间字段
    - 添加VALARM提醒组件
    - _需求: 4.3, 4.4_
  
  - [x] 3.3 实现RRULE生成

    - 实现generateRRule方法
    - 根据WeekPattern生成对应的RRULE
    - 处理自定义周次（生成多个VEVENT）
    - 生成UNTIL日期限制
    - _需求: 4.4, 4.5_
  
  - [x] 3.4 实现VTIMEZONE生成

    - 实现generateVTimezone方法
    - 生成Asia/Shanghai时区信息
    - 确保时区格式符合RFC 5545标准
    - _需求: 4.7_
  
  - [x] 3.5 实现文件写入

    - 实现writeToFile方法
    - 创建导出目录结构
    - 生成文件名（包含学期名称和时间戳）
    - 使用FileOutputStream写入内容
    - 返回文件Uri
    - _需求: 4.6_

- [x] 4. 实现业务用例层


  - [x] 4.1 创建IcsImportUseCase


    - 定义IcsImportUseCase类，注入依赖
    - 实现previewImport方法（解析但不保存）
    - 实现confirmImport方法（批量保存课程）
    - 实现importFromUri方法（完整导入流程）
    - 集成CourseColorAssigner分配颜色
    - 添加重复课程检测逻辑
    - _需求: 1.1, 1.4, 1.5, 5.6, 6.1, 6.2, 6.3_
  
  - [x] 4.2 创建IcsExportUseCase


    - 定义IcsExportUseCase类，注入依赖
    - 实现exportSemester方法（导出整个学期）
    - 实现exportCourses方法（导出选中课程）
    - 实现generateIcsContent方法（生成内容用于分享）
    - 从SemesterRepository获取学期信息
    - _需求: 4.1, 4.2, 4.6_

- [x] 5. 实现ViewModel层


  - [x] 5.1 创建ImportPreviewViewModel


    - 定义ImportPreviewViewModel类，注入IcsImportUseCase
    - 定义ImportPreviewState密封类
    - 实现loadPreview方法，加载预览数据
    - 实现toggleCourseSelection方法，切换选择状态
    - 实现selectAll和deselectAll方法
    - 实现confirmImport方法，执行导入
    - 添加重复课程警告逻辑
    - _需求: 5.1, 5.2, 5.3, 5.4, 5.5, 5.6_
  
  - [x] 5.2 扩展CourseViewModel


    - 添加exportCourses方法
    - 添加exportCurrentSemester方法
    - 处理导出成功/失败状态
    - _需求: 4.1, 4.6, 7.5_

- [x] 6. 实现UI组件


  - [x] 6.1 创建ImportPreviewScreen


    - 创建ImportPreviewScreen Composable
    - 实现顶部AppBar，显示标题和操作按钮
    - 实现CoursePreviewList，显示课程列表
    - 实现CoursePreviewItem，显示单个课程卡片
    - 添加复选框交互
    - 显示课程统计信息（总数、选中数）
    - 实现全选/取消全选按钮
    - 显示重复课程警告
    - _需求: 5.1, 5.2, 5.3, 5.4, 5.5, 5.6_
  
  - [x] 6.2 创建导入入口UI

    - 在CourseScreen添加"导入课程表"菜单项
    - 实现文件选择器启动逻辑
    - 使用ActivityResultContract处理文件选择结果
    - 导航到ImportPreviewScreen
    - _需求: 1.1_
  
  - [x] 6.3 创建ExportCoursesDialog


    - 创建ExportCoursesDialog Composable
    - 显示学期选择器
    - 显示文件名输入框
    - 实现导出按钮和取消按钮
    - 显示导出进度指示器
    - _需求: 4.1, 4.2_
  
  - [x] 6.4 创建导出入口UI

    - 在CourseScreen添加"导出课程表"菜单项
    - 显示ExportCoursesDialog
    - 处理导出成功后的分享操作
    - 显示成功/失败Toast提示
    - _需求: 4.6, 7.5_

- [x] 7. 添加字符串资源


  - 在strings.xml添加中文字符串
  - 在strings.xml (en)添加英文字符串
  - 在strings.xml (ja)添加日文字符串
  - 在strings.xml (vi)添加越南文字符串
  - _需求: 7.4, 7.5_

- [x] 8. 实现错误处理和用户反馈

  - 实现IcsException异常类层次
  - 在Parser中添加try-catch和错误转换
  - 在Generator中添加IO错误处理
  - 在ViewModel中处理Result类型
  - 显示用户友好的错误消息
  - 实现Toast和Snackbar提示
  - _需求: 1.6, 7.1, 7.2, 7.3, 7.4, 7.5, 7.6_

- [x] 9. 实现性能优化

  - 在IcsParser中实现时区缓存
  - 使用CourseDao.insertAll批量插入
  - 在数据库操作中使用事务
  - 在ViewModel中使用viewModelScope
  - 在UI中使用LazyColumn分页加载
  - 添加后台线程调度（Dispatchers.IO）
  - _需求: 8.1, 8.2, 8.4_

- [x] 10. 编写单元测试


  - [x] 10.1 测试IcsParser

    - 测试标准ICS格式解析
    - 测试各种RRULE模式解析
    - 测试时区处理
    - 测试异常情况处理
    - _需求: 1.2, 1.3, 2.4, 2.5, 3.4_
  
  - [x] 10.2 测试IcsGenerator

    - 测试生成的ICS格式正确性
    - 测试各种WeekPattern的RRULE生成
    - 测试自定义周次的多VEVENT生成
    - 测试VTIMEZONE生成
    - _需求: 4.3, 4.4, 4.5, 4.7_
  
  - [x] 10.3 测试CourseConverter

    - 测试ParsedCourse到Course的转换
    - 测试自定义周次识别算法
    - 测试颜色分配逻辑
    - _需求: 2.1, 2.2, 2.3, 3.2, 3.3, 6.1, 6.2, 6.3_
  
  - [x] 10.4 测试UseCase层

    - 测试IcsImportUseCase的各个方法
    - 测试IcsExportUseCase的各个方法
    - 使用MockK模拟依赖
    - _需求: 1.4, 1.5, 4.1, 4.2_

- [x] 11. 编写集成测试


  - 使用真实ICS文件测试完整导入流程
  - 测试导入导出往返一致性
  - 验证数据库中的数据正确性
  - _需求: 1.1, 1.2, 1.3, 1.4, 1.5, 4.1, 4.2, 4.3, 4.4, 4.5, 4.6_

- [x] 12. 集成和测试


  - 在Hilt模块中注册新的依赖
  - 更新导航图，添加ImportPreviewScreen路由
  - 进行端到端测试
  - 测试文件选择和权限处理
  - 测试各种ICS文件格式
  - 验证导出的ICS文件可被其他应用识别
  - 性能测试（大量课程导入导出）
  - _需求: 1.1, 1.2, 1.3, 1.4, 1.5, 1.6, 4.1, 4.2, 4.3, 4.4, 4.5, 4.6, 4.7, 8.1, 8.2, 8.3, 8.4_
