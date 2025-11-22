# 实施计划

- [x] 1. 修改 EventScreen 添加跨页面导航功能


  - 在 EventScreen 函数签名中添加 `onNavigateToTasks` 和 `onNavigateToRoutine` 导航回调参数
  - 添加 `showItemTypeSelector` 状态变量来控制底部弹窗的显示
  - 修改 EventTopBar 的标题部分，将其包装在可点击的 Surface 中，并添加下拉箭头图标
  - 在 EventScreen 的 Scaffold 内容区域添加 ItemTypeSelectorBottomSheet 组件
  - 在 ItemTypeSelectorBottomSheet 的 onTypeSelected 回调中实现导航逻辑：TASK 类型调用 onNavigateToTasks，SCHEDULE 类型调用 onNavigateToRoutine，EVENT 类型保持在当前页面
  - _需求: 1.1, 1.2, 1.3, 1.4, 3.2, 4.1, 4.2, 4.3_

- [x] 2. 修改 RoutineScreen 添加跨页面导航功能


  - 在 RoutineScreen 函数签名中添加 `onNavigateToTasks` 和 `onNavigateToEvents` 导航回调参数
  - 添加 `showItemTypeSelector` 状态变量来控制底部弹窗的显示
  - 修改 TopAppBar 的标题部分，将其包装在可点击的 Surface 中，并添加下拉箭头图标
  - 在 RoutineScreen 的 Scaffold 内容区域添加 ItemTypeSelectorBottomSheet 组件
  - 在 ItemTypeSelectorBottomSheet 的 onTypeSelected 回调中实现导航逻辑：TASK 类型调用 onNavigateToTasks，EVENT 类型调用 onNavigateToEvents，SCHEDULE 类型保持在当前页面
  - _需求: 2.1, 2.2, 2.3, 2.4, 3.3, 4.1, 4.2, 4.3_

- [x] 3. 更新 SaisonNavHost 导航配置


  - 在 EventScreen 的 composable 配置中添加 onNavigateToTasks 和 onNavigateToRoutine 回调实现
  - 在 RoutineScreen 的 composable 配置中添加 onNavigateToTasks 和 onNavigateToEvents 回调实现
  - 使用 navController.navigate() 方法导航到对应的路由（Screen.Tasks.route, Screen.Events.route, Screen.Routine.route）
  - _需求: 1.2, 1.3, 2.2, 2.3_

- [x] 4. 验证字符串资源



  - 检查 strings.xml 文件中是否存在 cd_dropdown_icon 字符串资源
  - 如果缺失，添加 cd_dropdown_icon 字符串资源及其多语言翻译（中文、日文、越南文）
  - 验证 item_type_task, item_type_schedule, item_type_event 等字符串资源是否已定义
  - _需求: 4.1, 4.2, 4.3_
