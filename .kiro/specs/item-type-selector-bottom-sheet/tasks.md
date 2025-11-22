# 实现计划

- [x] 1. 更新 ItemType 枚举定义


  - 将 `MILESTONE` 重命名为 `SCHEDULE`，将 `COUNTDOWN` 重命名为 `EVENT`
  - 添加 `getDisplayNameResId()` 方法返回字符串资源 ID
  - 添加 `getIcon()` 方法返回对应的 Material Icon
  - _需求: 1.1, 2.2, 3.4, 3.5, 3.6_

- [x] 2. 添加国际化字符串资源


  - 在 `values/strings.xml` 中添加 `item_type_task`, `item_type_schedule`, `item_type_event`, `item_type_selector_title` 等字符串
  - 在 `values-zh-rCN/strings.xml` 中添加对应的中文翻译
  - 在 `values-ja/strings.xml` 中添加对应的日语翻译
  - 在 `values-vi/strings.xml` 中添加对应的越南语翻译
  - _需求: 5.1, 5.2, 5.3, 5.4_

- [x] 3. 扩展 PreferencesManager 支持项目类型持久化


  - 添加 `SELECTED_ITEM_TYPE` preferences key
  - 实现 `selectedItemType: Flow<ItemType>` 用于读取保存的项目类型
  - 实现 `setSelectedItemType(type: ItemType)` 方法用于保存项目类型
  - 添加默认值处理逻辑（默认为 `ItemType.TASK`）
  - _需求: 4.1, 4.2, 4.3_

- [x] 4. 创建 ItemTypeSelectorBottomSheet 组件


  - 创建新文件 `ui/components/ItemTypeSelectorBottomSheet.kt`
  - 使用 `ModalBottomSheet` 实现 Material 3 风格的 Bottom Sheet
  - 添加拖动手柄（Drag Handle）
  - 显示标题"选择类型"
  - 实现三个选项（任务、日程、事件）的列表
  - 为每个选项添加图标、标签文本和选中指示器
  - 实现选项点击逻辑：触发回调并关闭 Bottom Sheet
  - 当前选中的类型使用不同的视觉样式（primary 颜色）
  - _需求: 2.1, 2.2, 2.3, 2.4, 2.5, 3.1, 3.3_

- [x] 5. 修改 TaskViewModel 添加项目类型状态管理


  - 添加 `_currentItemType: MutableStateFlow<ItemType>` 私有状态
  - 暴露 `currentItemType: StateFlow<ItemType>` 公开状态
  - 在 `init` 块中从 `PreferencesManager` 加载保存的项目类型
  - 实现 `setItemType(type: ItemType)` 方法更新状态并保存到 PreferencesManager
  - _需求: 3.1, 3.2, 4.1, 4.2_

- [x] 6. 修改 TaskListTopBar 使标题可点击


  - 添加 `currentItemType: ItemType` 参数
  - 添加 `onItemTypeSelectorClick: () -> Unit` 回调参数
  - 将标题文本包装在可点击的 `Surface` 或 `TextButton` 中
  - 根据 `currentItemType` 显示对应的标题文本（使用 `stringResource`）
  - 在标题旁边添加下拉箭头图标（`Icons.Default.ArrowDropDown`）
  - 实现点击事件触发 `onItemTypeSelectorClick` 回调
  - 应用 Material 3 样式和 Ripple 效果
  - _需求: 1.1, 1.2, 1.3, 1.4, 3.2_

- [x] 7. 集成 Bottom Sheet 到 TaskListScreen


  - 在 `TaskListScreen` 中添加 `showItemTypeSelector` 状态变量
  - 从 `viewModel.currentItemType` 收集当前项目类型状态
  - 将 `currentItemType` 和 `onItemTypeSelectorClick` 传递给 `TaskListTopBar`
  - 当 `showItemTypeSelector` 为 true 时显示 `ItemTypeSelectorBottomSheet`
  - 实现 `onTypeSelected` 回调：调用 `viewModel.setItemType()` 并关闭 Bottom Sheet
  - 实现 `onDismiss` 回调：关闭 Bottom Sheet
  - _需求: 1.3, 2.1, 2.5, 3.1, 3.2_

- [x] 8. 添加错误处理和日志


  - 在 `PreferencesManager` 的读写操作中添加 try-catch 错误处理
  - 在 `TaskViewModel.setItemType()` 中添加错误处理
  - 使用 Timber 或 Log 记录错误信息
  - 在保存失败时显示 Snackbar 提示用户
  - _需求: 4.1, 4.2_

- [x] 9. 编写单元测试

  - 为 `ItemType` 枚举编写测试：测试 `fromValue()`, `getDisplayNameResId()`, `getIcon()` 方法
  - 为 `PreferencesManager` 编写测试：测试 `setSelectedItemType()` 和 `selectedItemType` Flow
  - 为 `TaskViewModel` 编写测试：测试 `setItemType()` 方法和初始化逻辑
  - _需求: 所有需求_

- [x] 10. 编写 UI 测试


  - 测试点击标题后 Bottom Sheet 正确显示
  - 测试 Bottom Sheet 包含三个选项且当前选中的类型有正确的视觉指示
  - 测试点击选项后 Bottom Sheet 关闭且 TopAppBar 标题更新
  - 测试应用重启后选中的类型被正确恢复
  - 测试不同语言下标签显示正确
  - _需求: 所有需求_
