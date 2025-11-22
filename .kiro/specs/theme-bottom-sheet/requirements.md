# 需求文档

## 简介

本文档定义了将 Saison 任务管理应用设置页面中的主题选择对话框改造为 Material 3 Extended (M3E) 风格 Bottom Sheet 的需求。当前主题选择使用 AlertDialog 实现，用户体验较为传统。改用 Bottom Sheet 可以提供更现代化、更流畅的交互体验，符合 Material 3 设计规范。

## 术语表

- **Bottom Sheet**: Material 3 中的底部弹出面板组件，从屏幕底部滑出显示内容
- **ModalBottomSheet**: 模态底部面板，显示时会遮罩背景内容
- **ThemeSelectionDialog**: 当前的主题选择对话框组件
- **ThemePreviewCard**: 主题预览卡片组件，显示主题名称和颜色预览
- **SeasonalTheme**: 应用的季节性主题枚举
- **M3E (Material 3 Extended)**: Material 3 扩展设计规范

## 需求

### 需求 1: Bottom Sheet 基础实现

**用户故事:** 作为用户，我希望主题选择界面从底部滑出，以便获得更流畅的交互体验

#### 验收标准

1. THE SettingsScreen SHALL 使用 ModalBottomSheet 替代 AlertDialog 显示主题选择界面
2. WHEN 用户点击主题设置项时，THE ModalBottomSheet SHALL 从屏幕底部滑入
3. WHEN 用户关闭 Bottom Sheet 时，THE ModalBottomSheet SHALL 滑出到屏幕底部
4. THE ModalBottomSheet SHALL 使用 M3 的标准滑入滑出动画
5. THE ModalBottomSheet SHALL 显示半透明背景遮罩

### 需求 2: Bottom Sheet 内容布局

**用户故事:** 作为用户，我希望 Bottom Sheet 中的主题选项清晰易读，以便快速选择喜欢的主题

#### 验收标准

1. THE ThemeBottomSheet SHALL 在顶部显示标题"选择主题"
2. THE ThemeBottomSheet SHALL 使用 LazyColumn 显示所有主题选项
3. THE ThemeBottomSheet SHALL 为每个主题使用 ThemePreviewCard 组件
4. THE ThemeBottomSheet SHALL 在内容区域使用 16dp 水平内边距
5. THE ThemeBottomSheet SHALL 在主题卡片之间使用 8dp 垂直间距
6. THE ThemeBottomSheet SHALL 在标题和内容之间使用 16dp 间距

### 需求 3: 主题预览卡片优化

**用户故事:** 作为用户，我希望主题预览卡片在 Bottom Sheet 中显示效果更好，以便更直观地了解每个主题

#### 验收标准

1. THE ThemePreviewCard SHALL 保持当前的颜色预览条设计
2. THE ThemePreviewCard SHALL 使用 RadioButton 标识当前选中的主题
3. WHEN 主题被选中时，THE ThemePreviewCard SHALL 使用 primaryContainer 背景色
4. WHEN 主题被选中时，THE ThemePreviewCard SHALL 显示 2dp 的 primary 颜色边框
5. THE ThemePreviewCard SHALL 支持点击涟漪效果

### 需求 4: 交互行为

**用户故事:** 作为用户，我希望选择主题后 Bottom Sheet 自动关闭，以便快速完成主题切换

#### 验收标准

1. WHEN 用户点击任意主题卡片时，THE ThemeBottomSheet SHALL 立即应用该主题
2. WHEN 主题应用成功后，THE ThemeBottomSheet SHALL 自动关闭
3. WHEN 用户点击背景遮罩时，THE ThemeBottomSheet SHALL 关闭
4. WHEN 用户向下滑动 Bottom Sheet 时，THE ThemeBottomSheet SHALL 关闭
5. THE ThemeBottomSheet SHALL 在关闭时使用滑出动画

### 需求 5: 无障碍支持

**用户故事:** 作为有无障碍需求的用户，我希望 Bottom Sheet 支持辅助功能，以便我能够独立使用

#### 验收标准

1. THE ThemeBottomSheet SHALL 为所有交互元素提供 contentDescription
2. THE ThemeBottomSheet SHALL 支持 TalkBack 屏幕阅读器
3. THE ThemeBottomSheet SHALL 确保所有可点击元素的最小触摸目标为 48dp
4. WHEN Bottom Sheet 打开时，THE ThemeBottomSheet SHALL 通过无障碍服务宣布"主题选择已打开"
5. WHEN 主题被选中时，THE ThemeBottomSheet SHALL 通过无障碍服务宣布主题名称

### 需求 6: 响应式设计

**用户故事:** 作为使用不同设备的用户，我希望 Bottom Sheet 在各种屏幕尺寸上都能正常显示

#### 验收标准

1. THE ThemeBottomSheet SHALL 在手机竖屏模式下占据屏幕高度的最多 80%
2. THE ThemeBottomSheet SHALL 在手机横屏模式下占据屏幕高度的最多 90%
3. THE ThemeBottomSheet SHALL 在平板设备上使用最大宽度 600dp 并居中显示
4. THE ThemeBottomSheet SHALL 支持内容滚动当主题选项超过可见区域时
5. THE ThemeBottomSheet SHALL 在底部保留安全区域内边距

### 需求 7: 性能优化

**用户故事:** 作为用户，我希望 Bottom Sheet 打开和关闭流畅，以便获得良好的使用体验

#### 验收标准

1. THE ThemeBottomSheet SHALL 在打开时使用延迟加载内容
2. THE ThemeBottomSheet SHALL 缓存主题预览颜色避免重复计算
3. THE ThemeBottomSheet SHALL 使用 key 参数优化 LazyColumn 性能
4. THE ThemeBottomSheet SHALL 在动画期间保持 60fps 帧率
5. THE ThemeBottomSheet SHALL 在关闭后释放资源

### 需求 8: 视觉效果

**用户故事:** 作为用户，我希望 Bottom Sheet 的视觉效果符合 Material 3 设计规范

#### 验收标准

1. THE ThemeBottomSheet SHALL 使用 M3 的 surface 颜色作为背景
2. THE ThemeBottomSheet SHALL 使用 28dp 的顶部圆角
3. THE ThemeBottomSheet SHALL 在顶部显示拖动手柄（drag handle）
4. THE ThemeBottomSheet SHALL 使用 M3 的标准阴影效果
5. THE ThemeBottomSheet SHALL 在深色模式和浅色模式下都有良好的视觉效果
