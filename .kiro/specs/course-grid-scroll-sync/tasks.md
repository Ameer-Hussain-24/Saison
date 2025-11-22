# 实现计划

- [x] 1. 重构 GridTimetableView 为单 LazyColumn 结构





  - 修改 `GridTimetableView.kt` 文件，将双 LazyColumn 结构改为单个 LazyColumn
  - 在 LazyColumn 的每个 item 中创建包含时间单元格和课程单元格的完整行
  - 确保时间单元格固定宽度为 60.dp
  - 确保课程单元格行使用 `weight(1f)` 占据剩余空间
  - 保持现有的 `autoScrollToCurrentTime` 功能
  - 保持所有现有的参数和公共 API 不变
  - _Requirements: 1.1, 1.2, 1.3, 1.4_

- [x] 2. 验证滚动同步和性能






  - 在真机或模拟器上测试基本滚动同步
  - 测试快速滚动场景
  - 验证自动滚动到当前时间功能
  - 确认当前节次高亮效果正常
  - 检查课程点击和空单元格点击功能
  - _Requirements: 1.1, 1.2, 2.1, 2.2, 2.3_
