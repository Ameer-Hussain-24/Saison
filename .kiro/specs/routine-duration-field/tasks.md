# 实施计划

- [x] 1. 修改数据模型和数据库




  - 在 `RoutineTask` 数据类中添加 `durationMinutes: Int?` 字段
  - 创建数据库迁移脚本，为 `routine_tasks` 表添加 `duration_minutes` 列
  - 更新 `RoutineDao` 的查询和插入方法以支持新字段
  - 更新 `RoutineTaskSerializer` 以支持序列化和反序列化 `durationMinutes` 字段
  - _需求: 1.5, 3.3, 3.4_

- [x] 2. 创建时长工具函数



  - 创建 `DurationFormatter.kt` 文件
  - 实现 `formatDuration(minutes: Int): String` 函数，将分钟数格式化为易读字符串
  - 实现 `toMinutes(hours: Int, minutes: Int): Int` 函数
  - 实现 `fromMinutes(totalMinutes: Int): Pair<Int, Int>` 函数
  - _需求: 2.2, 3.1_

- [x] 3. 创建 DurationPicker 组件


  - 创建 `DurationPicker.kt` 文件
  - 实现小时选择器（0-23小时）
  - 实现分钟选择器（0、15、30、45分钟）
  - 添加常用时长快捷按钮（15分钟、30分钟、45分钟、1小时、1.5小时、2小时）
  - 添加清除时长按钮
  - 实现 `DurationPickerDialog` 组件包装选择器
  - _需求: 1.3, 1.4, 4.1, 4.2, 4.3, 4.4, 4.5_

- [x] 4. 修改 CreateRoutineSheet 组件


  - 添加 `durationMinutes` 状态变量
  - 添加 `showDurationPicker` 状态变量
  - 添加时长输入字段（OutlinedTextField，只读）
  - 添加时长选择图标按钮（Schedule 图标）
  - 集成 `DurationPickerDialog` 组件
  - 在保存时将 `durationMinutes` 传递给 `RoutineTask`
  - _需求: 1.1, 1.2, 1.3, 1.4, 1.5_

- [x] 5. 修改 RoutineCard 组件


  - 检查 `task.durationMinutes` 是否有值
  - 如果有值，显示时长信息行（Schedule 图标 + 格式化的时长文本）
  - 使用 `MaterialTheme.typography.bodySmall` 和 `onSurfaceVariant` 颜色
  - 如果没有值，不显示时长信息
  - _需求: 2.1, 2.2, 2.3_

- [x] 6. 修改 RoutineDetailScreen 组件


  - 添加时长信息显示行（DetailRow）
  - 显示当前设置的时长或"未设置"
  - 添加编辑时长功能（点击打开 DurationPickerDialog）
  - 在 ViewModel 中添加更新时长的方法
  - _需求: 3.1, 3.2, 3.3, 3.4_

- [x] 7. 添加字符串资源


  - 在 `values/strings.xml` 中添加英文字符串资源
  - 在 `values-zh-rCN/strings.xml` 中添加中文字符串资源
  - 在 `values-ja/strings.xml` 中添加日文字符串资源
  - 在 `values-vi/strings.xml` 中添加越南文字符串资源
  - 包括：标签、提示文本、快捷按钮文本、格式化字符串等
  - _需求: 1.1, 2.2, 3.1, 4.4_

- [x] 8. 更新 Repository 和 ViewModel



  - 更新 `RoutineRepository` 的创建和更新方法以支持 `durationMinutes`
  - 更新 `RoutineViewModel` 的 `createRoutineTask` 方法
  - 更新 `RoutineDetailViewModel` 添加 `updateDuration` 方法
  - 确保数据正确保存和读取
  - _需求: 1.5, 3.4_
