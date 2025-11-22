# 实现计划

- [x] 1. 创建 ThemeBottomSheet 组件


  - 在 SettingsScreen.kt 文件中创建新的 ThemeBottomSheet Composable 函数
  - 使用 @OptIn(ExperimentalMaterial3Api::class) 注解
  - 实现 ModalBottomSheet 容器，配置 onDismissRequest、sheetState、containerColor 和 shape
  - 添加 Column 布局，包含标题和 LazyColumn
  - 使用 stringResource 获取标题文本
  - _需求: 1.1, 1.2, 2.1, 8.1, 8.2, 8.3_


- [ ] 2. 实现 Bottom Sheet 内容布局
  - 在 Column 中添加标题 Text，使用 headlineSmall 样式
  - 实现 LazyColumn 显示主题列表
  - 使用 items() 函数遍历 SeasonalTheme.values()
  - 为每个 item 设置 key 参数为 theme.name
  - 配置 verticalArrangement 为 spacedBy(8.dp)
  - 配置 contentPadding 为 PaddingValues(bottom = 16.dp)
  - 复用现有的 ThemePreviewCard 组件

  - _需求: 2.2, 2.3, 2.5, 2.6_

- [ ] 3. 配置 Bottom Sheet 样式和动画
  - 设置 ModalBottomSheet 的 shape 为 RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
  - 使用 rememberModalBottomSheetState() 创建 sheetState
  - 确保使用 M3 默认的滑入滑出动画
  - 设置背景遮罩效果

  - 添加拖动手柄（ModalBottomSheet 默认提供）
  - _需求: 1.3, 1.4, 1.5, 8.2, 8.3, 8.4_

- [ ] 4. 实现主题选择交互逻辑
  - 在 ThemePreviewCard 的 onClick 中调用 onThemeSelected(theme)
  - 主题选择后立即调用 onDismiss() 关闭 Bottom Sheet

  - 确保点击背景遮罩可以关闭 Bottom Sheet
  - 确保向下滑动可以关闭 Bottom Sheet
  - _需求: 4.1, 4.2, 4.3, 4.4, 4.5_

- [ ] 5. 更新 SettingsScreen 集成
  - 将 showThemeDialog 状态变量重命名为 showThemeBottomSheet
  - 更新主题设置项的 onClick 为 { showThemeBottomSheet = true }

  - 替换 ThemeSelectionDialog 的条件渲染为 ThemeBottomSheet
  - 传递正确的参数：currentTheme, onThemeSelected, onDismiss
  - 在 onThemeSelected 中调用 viewModel.setTheme(theme)
  - _需求: 1.1, 4.1_

- [ ] 6. 优化 ThemePreviewCard 在 Bottom Sheet 中的显示
  - 确保 ThemePreviewCard 使用正确的选中状态背景色（primaryContainer）

  - 确保选中时显示 2dp 的 primary 颜色边框
  - 保持现有的颜色预览条设计
  - 保持 RadioButton 标识
  - 确保点击涟漪效果正常工作
  - _需求: 3.1, 3.2, 3.3, 3.4, 3.5_

- [ ] 7. 实现响应式设计
  - 使用 LocalConfiguration 获取屏幕配置

  - 判断是否为平板设备（screenWidthDp >= 600）
  - 在平板上限制 Bottom Sheet 最大宽度为 600dp
  - 根据屏幕方向调整最大高度（竖屏 80%，横屏 90%）
  - 确保内容可滚动当主题选项超过可见区域
  - 添加底部安全区域内边距
  - _需求: 6.1, 6.2, 6.3, 6.4, 6.5_


- [ ] 8. 添加无障碍支持
  - 为 ModalBottomSheet 添加 semantics 修饰符，设置 contentDescription 和 role
  - 为 ThemePreviewCard 确保有正确的 contentDescription
  - 使用 LaunchedEffect 在 Bottom Sheet 打开时宣布"主题选择已打开"
  - 在主题被选中时宣布主题名称
  - 确保所有可点击元素使用 minimumInteractiveComponentSize()
  - _需求: 5.1, 5.2, 5.3, 5.4, 5.5_


- [ ] 9. 实现性能优化
  - 在 ThemePreviewCard 中使用 remember(theme) 缓存主题预览颜色
  - 为 LazyColumn 的 items 设置 key 参数
  - 使用 derivedStateOf 优化 isSelected 状态
  - 确保动画流畅（60fps）


  - 在 Bottom Sheet 关闭后正确释放资源
  - _需求: 7.1, 7.2, 7.3, 7.4, 7.5_

- [ ] 10. 添加错误处理
  - 在 onThemeSelected 中添加 try-catch 处理主题应用失败
  - 主题应用失败时显示错误 Snackbar
  - 使用 LaunchedEffect 监听 sheetState.isVisible 处理状态错误
  - 确保 Bottom Sheet 状态异常时能正确恢复
  - _需求: 设计文档中的错误处理部分_




- [ ] 11. 测试和验证
  - 测试 Bottom Sheet 打开和关闭动画
  - 测试主题选择功能
  - 测试点击背景遮罩关闭
  - 测试向下滑动关闭
  - 测试在不同屏幕尺寸上的显示效果
  - 测试深色模式和浅色模式
  - 测试无障碍功能（TalkBack）
  - 测试性能（帧率）
  - _需求: 所有需求的综合验证_

- [ ] 12. 清理和文档
  - 删除或注释掉旧的 ThemeSelectionDialog 代码
  - 添加代码注释说明 Bottom Sheet 的实现
  - 更新相关文档（如果有）
  - 确保代码符合项目编码规范
  - _需求: 代码质量和可维护性_
