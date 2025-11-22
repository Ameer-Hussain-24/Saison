# Implementation Plan

- [x] 1. 更新AddTaskSheet状态管理


  - 将 `repeatEnabled` 状态变量替换为 `selectedRepeatType`，类型为String，默认值为"不重复"
  - 保留 `reminderEnabled` 状态变量
  - 确保状态变量使用 `remember` 和 `mutableStateOf` 正确管理
  - _Requirements: 1.1, 1.2, 1.3_

- [x] 2. 实现重复设置区域UI


  - [x] 2.1 创建重复设置的Column布局


    - 设置fillMaxWidth和8dp的垂直间距
    - 添加标题行，包含Repeat图标和"重复"文字
    - 图标尺寸设置为20dp
    - 文字使用titleSmall样式和Medium字重
    - _Requirements: 1.1, 5.3, 5.4_

  - [x] 2.2 实现重复选项的FilterChip行

    - 创建包含5个FilterChip的Row，使用8dp水平间距
    - 每个FilterChip使用weight(1f)平均分配宽度
    - 选项包括："不重复"、"每天"、"每周"、"每月"、"每年"
    - 设置selected属性为 `selectedRepeatType == option`
    - 配置onClick更新selectedRepeatType状态
    - _Requirements: 1.2, 1.3, 5.5_

  - [x] 2.3 配置FilterChip的视觉样式

    - 设置selectedContainerColor为primaryContainer
    - 设置selectedLabelColor为onPrimaryContainer
    - 确保选中和未选中状态有明显的视觉区别
    - _Requirements: 1.4_

  - [x] 2.4 实现图标颜色动态变化

    - 当selectedRepeatType不等于"不重复"时，图标颜色为primary
    - 当selectedRepeatType等于"不重复"时，图标颜色为onSurfaceVariant
    - _Requirements: 1.5_

- [x] 3. 实现提醒设置区域UI


  - [x] 3.1 创建提醒Card组件

    - 设置fillMaxWidth
    - 配置onClick切换reminderEnabled状态
    - 实现动态背景色：启用时为primaryContainer.copy(alpha = 0.3f)，未启用时为surfaceContainerLow
    - _Requirements: 3.1, 3.2, 3.4_

  - [x] 3.2 实现Card内部布局

    - 创建Column，设置16dp内边距和4dp垂直间距
    - 添加主要内容Row，包含图标、文字和Switch
    - 图标尺寸设置为20dp
    - 文字使用titleSmall样式和Medium字重
    - _Requirements: 3.3, 5.3_

  - [x] 3.3 实现图标颜色动态变化

    - 当reminderEnabled为true时，图标颜色为primary
    - 当reminderEnabled为false时，图标颜色为onSurfaceVariant
    - _Requirements: 3.3_

  - [x] 3.4 实现Switch组件

    - 绑定checked属性到reminderEnabled状态
    - 配置onCheckedChange更新reminderEnabled状态
    - 确保Switch和Card点击都能正确切换状态
    - _Requirements: 3.3, 3.4_

- [x] 4. 实现智能提醒说明文字


  - [x] 4.1 实现提醒说明文字的显示逻辑

    - 仅当reminderEnabled为true时显示
    - 使用bodySmall样式和onSurfaceVariant颜色
    - _Requirements: 3.5, 4.4_

  - [x] 4.2 实现提醒文字的条件判断逻辑

    - 场景1：已设置日期且未设置重复 → "将在 [MM月dd日] 提醒"
    - 场景2：已设置重复（无论是否设置日期）→ "将在每次重复时提醒"
    - 场景3：未设置日期且未设置重复 → "将在任务当天提醒"
    - 使用DateTimeFormatter.ofPattern("MM月dd日")格式化日期
    - _Requirements: 4.1, 4.2, 4.3_

- [x] 5. 更新更多选项区域布局

  - 调整更多选项区域的内容顺序：重复设置、提醒设置、描述输入
  - 设置各项之间的垂直间距为12dp
  - 移除旧的重复和提醒Card组件
  - 确保新组件正确集成到AnimatedVisibility中
  - _Requirements: 5.1, 5.2_

- [x] 6. 验证和测试



  - [x] 6.1 测试重复选项功能

    - 验证5个选项都能正确显示和选择
    - 验证选中状态的视觉反馈正确
    - 验证图标颜色根据选择正确变化
    - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5_

  - [x] 6.2 测试提醒设置功能

    - 验证Card背景色根据开关状态正确变化
    - 验证点击Card和Switch都能切换状态
    - 验证图标颜色正确变化
    - _Requirements: 3.1, 3.2, 3.3, 3.4_

  - [x] 6.3 测试提醒说明文字

    - 验证4种不同场景的文字显示正确
    - 验证日期格式化正确
    - 验证文字样式和颜色正确
    - _Requirements: 4.1, 4.2, 4.3, 4.4_

  - [x] 6.4 测试整体布局和交互

    - 验证更多选项区域的展开/收起动画流畅
    - 验证各组件之间的间距符合设计规范
    - 验证在不同屏幕尺寸下的显示效果
    - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.5_
