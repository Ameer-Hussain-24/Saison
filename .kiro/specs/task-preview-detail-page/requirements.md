# 任务预览详情页面需求文档

## 简介

本文档定义了为Saison应用创建全新任务预览详情页面的需求。该页面用于直观地查看任务的所有信息，采用Material Design 3设计规范，与现有的任务编辑页面分离。用户通过点击任务卡片进入预览页面，通过侧滑编辑按钮进入编辑页面。

## 术语表

- **TaskPreviewScreen**: 任务预览详情屏幕组件，用于只读查看任务信息
- **TaskEditScreen**: 任务编辑屏幕组件（原TaskDetailScreen），用于编辑任务
- **TaskCard**: 任务列表中的任务卡片组件
- **SwipeAction**: 卡片侧滑操作
- **M3**: Material Design 3设计规范
- **ReadOnlyView**: 只读视图，不包含编辑控件

## 需求

### 需求 1: 任务卡片交互分离

**用户故事:** 作为用户，我希望点击任务卡片和侧滑编辑按钮有不同的行为，以便快速预览或编辑任务

#### 验收标准

1. WHEN 用户点击任务卡片时，THE TaskCard SHALL 导航到TaskPreviewScreen
2. WHEN 用户侧滑任务卡片并点击编辑按钮时，THE TaskCard SHALL 导航到TaskEditScreen
3. THE TaskCard SHALL 保持现有的侧滑操作功能不变
4. THE TaskCard SHALL 在侧滑菜单中显示"编辑"文字而非"详情"
5. THE TaskCard SHALL 确保两种导航方式传递相同的任务ID参数

### 需求 2: 任务预览页面整体布局

**用户故事:** 作为用户，我希望预览页面具有清晰的信息层次和优雅的视觉设计，以便快速了解任务全貌

#### 验收标准

1. THE TaskPreviewScreen SHALL 使用Material 3的表面容器系统组织信息
2. THE TaskPreviewScreen SHALL 采用只读视图，不包含任何编辑控件
3. THE TaskPreviewScreen SHALL 使用16dp的统一内边距和12dp的组件间距
4. THE TaskPreviewScreen SHALL 使用可滚动的Column布局
5. THE TaskPreviewScreen SHALL 在页面打开时使用淡入动画

### 需求 3: 顶部栏设计

**用户故事:** 作为用户，我希望预览页面的顶部栏提供返回和编辑功能，以便快速切换到编辑模式

#### 验收标准

1. THE TaskPreviewScreen SHALL 在顶部栏左侧显示返回按钮
2. THE TaskPreviewScreen SHALL 在顶部栏右侧显示编辑按钮
3. WHEN 用户点击编辑按钮时，THE TaskPreviewScreen SHALL 导航到TaskEditScreen
4. THE TaskPreviewScreen SHALL 在顶部栏显示"任务详情"标题
5. THE TaskPreviewScreen SHALL 在顶部栏提供更多操作菜单（分享、删除等）

### 需求 4: 任务标题和状态展示

**用户故事:** 作为用户，我希望清晰地看到任务标题和完成状态，以便快速识别任务

#### 验收标准

1. THE TaskPreviewScreen SHALL 在顶部显示大号任务标题
2. THE TaskPreviewScreen SHALL 使用headlineMedium字体样式显示标题
3. THE TaskPreviewScreen SHALL 在标题旁显示完成状态图标
4. WHEN 任务已完成时，THE TaskPreviewScreen SHALL 在标题上显示删除线
5. THE TaskPreviewScreen SHALL 使用不同颜色区分已完成和未完成状态

### 需求 5: 任务描述展示

**用户故事:** 作为用户，我希望完整地查看任务描述，以便了解任务详细信息

#### 验收标准

1. THE TaskPreviewScreen SHALL 在独立卡片中显示任务描述
2. THE TaskPreviewScreen SHALL 使用bodyLarge字体样式显示描述文本
3. WHEN 描述为空时，THE TaskPreviewScreen SHALL 显示"暂无描述"提示
4. THE TaskPreviewScreen SHALL 支持描述文本的自动换行
5. THE TaskPreviewScreen SHALL 为描述卡片使用surfaceVariant背景色

### 需求 6: 优先级和日期信息展示

**用户故事:** 作为用户，我希望直观地看到任务的优先级和截止日期，以便评估任务紧急程度

#### 验收标准

1. THE TaskPreviewScreen SHALL 使用带颜色的标签显示优先级
2. THE TaskPreviewScreen SHALL 为不同优先级使用对应的颜色和图标
3. THE TaskPreviewScreen SHALL 在独立行显示截止日期和时间
4. THE TaskPreviewScreen SHALL 显示相对时间提示（"还有3天"、"已逾期2天"）
5. WHEN 任务逾期时，THE TaskPreviewScreen SHALL 使用error颜色高亮显示

### 需求 7: 重复规则展示

**用户故事:** 作为用户，我希望清楚地看到任务的重复规则，以便了解任务的周期性

#### 验收标准

1. THE TaskPreviewScreen SHALL 在独立卡片中显示重复规则
2. THE TaskPreviewScreen SHALL 使用图标和文字描述重复模式
3. THE TaskPreviewScreen SHALL 显示下次重复时间
4. WHEN 任务无重复规则时，THE TaskPreviewScreen SHALL 隐藏重复规则区域
5. THE TaskPreviewScreen SHALL 使用Repeat图标标识重复任务

### 需求 8: 位置信息展示

**用户故事:** 作为用户，我希望看到任务关联的位置信息，以便了解任务地点

#### 验收标准

1. THE TaskPreviewScreen SHALL 在独立行显示位置信息
2. THE TaskPreviewScreen SHALL 使用LocationOn图标标识位置
3. WHEN 任务无位置信息时，THE TaskPreviewScreen SHALL 隐藏位置区域
4. THE TaskPreviewScreen SHALL 使用bodyMedium字体样式显示位置文本
5. THE TaskPreviewScreen SHALL 为位置信息使用OutlinedCard容器

### 需求 9: 标签展示

**用户故事:** 作为用户，我希望看到任务的所有标签，以便了解任务分类

#### 验收标准

1. THE TaskPreviewScreen SHALL 使用FlowRow布局显示标签
2. THE TaskPreviewScreen SHALL 使用AssistChip显示每个标签
3. THE TaskPreviewScreen SHALL 为标签应用对应的颜色
4. WHEN 任务无标签时，THE TaskPreviewScreen SHALL 隐藏标签区域
5. THE TaskPreviewScreen SHALL 支持标签的自动换行

### 需求 10: 子任务展示

**用户故事:** 作为用户，我希望看到所有子任务及其完成状态，以便了解任务进度

#### 验收标准

1. THE TaskPreviewScreen SHALL 在独立卡片中显示子任务列表
2. THE TaskPreviewScreen SHALL 显示子任务完成进度（X/Y）
3. THE TaskPreviewScreen SHALL 使用LinearProgressIndicator显示进度条
4. THE TaskPreviewScreen SHALL 为每个子任务显示复选框图标和文本
5. WHEN 子任务已完成时，THE TaskPreviewScreen SHALL 显示删除线

### 需求 11: 附件展示

**用户故事:** 作为用户，我希望看到任务的所有附件，以便查看相关文档和图片

#### 验收标准

1. THE TaskPreviewScreen SHALL 使用网格布局显示附件缩略图
2. THE TaskPreviewScreen SHALL 使用3列网格布局
3. WHEN 用户点击附件时，THE TaskPreviewScreen SHALL 打开附件预览
4. THE TaskPreviewScreen SHALL 显示附件数量
5. WHEN 任务无附件时，THE TaskPreviewScreen SHALL 隐藏附件区域

### 需求 12: 创建和修改时间展示

**用户故事:** 作为用户，我希望看到任务的创建和最后修改时间，以便了解任务历史

#### 验收标准

1. THE TaskPreviewScreen SHALL 在页面底部显示创建时间
2. THE TaskPreviewScreen SHALL 在页面底部显示最后修改时间
3. THE TaskPreviewScreen SHALL 使用labelSmall字体样式显示时间信息
4. THE TaskPreviewScreen SHALL 使用onSurfaceVariant颜色显示时间文本
5. THE TaskPreviewScreen SHALL 使用相对时间格式（"创建于3天前"）

### 需求 13: 快速操作按钮

**用户故事:** 作为用户，我希望在预览页面快速完成常用操作，以便提高效率

#### 验收标准

1. THE TaskPreviewScreen SHALL 在底部显示浮动操作按钮
2. THE TaskPreviewScreen SHALL 提供"标记完成/未完成"按钮
3. THE TaskPreviewScreen SHALL 提供"编辑"按钮
4. WHEN 用户点击完成按钮时，THE TaskPreviewScreen SHALL 切换任务完成状态
5. THE TaskPreviewScreen SHALL 使用ExtendedFloatingActionButton显示操作按钮

### 需求 14: 页面导航和路由

**用户故事:** 作为开发者，我需要正确配置导航路由，以便用户能够在预览和编辑页面之间切换

#### 验收标准

1. THE SaisonNavHost SHALL 添加taskPreview路由
2. THE SaisonNavHost SHALL 保持taskDetail路由用于编辑
3. THE TaskListScreen SHALL 在点击卡片时导航到taskPreview
4. THE TaskListScreen SHALL 在侧滑编辑时导航到taskDetail
5. THE TaskPreviewScreen SHALL 在点击编辑按钮时导航到taskDetail

### 需求 15: 编辑页面重命名

**用户故事:** 作为用户，我希望编辑页面的标题清楚地表明其用途，以便区分预览和编辑模式

#### 验收标准

1. THE TaskEditScreen SHALL 在顶部栏显示"任务编辑"标题
2. THE TaskEditScreen SHALL 保持现有的所有编辑功能不变
3. THE TaskEditScreen SHALL 在保存后返回到任务列表或预览页面
4. THE strings.xml SHALL 添加"任务编辑"和"任务详情"字符串资源
5. THE TaskEditScreen SHALL 使用与TaskDetailScreen相同的实现

### 需求 16: 响应式布局

**用户故事:** 作为用户，我希望在不同屏幕尺寸上都能获得良好的预览体验

#### 验收标准

1. WHEN 屏幕宽度大于600dp时，THE TaskPreviewScreen SHALL 使用双列布局
2. WHEN 屏幕宽度大于840dp时，THE TaskPreviewScreen SHALL 使用对话框模式
3. THE TaskPreviewScreen SHALL 在平板上优化信息密度
4. THE TaskPreviewScreen SHALL 在横屏模式下调整布局
5. THE TaskPreviewScreen SHALL 支持分屏模式

### 需求 17: 动画和过渡效果

**用户故事:** 作为用户，我希望页面切换和操作具有流畅的动画效果，以便获得愉悦的使用体验

#### 验收标准

1. WHEN 打开预览页面时，THE TaskPreviewScreen SHALL 使用淡入动画
2. WHEN 切换完成状态时，THE TaskPreviewScreen SHALL 使用缩放动画
3. WHEN 导航到编辑页面时，THE TaskPreviewScreen SHALL 使用滑动过渡动画
4. THE TaskPreviewScreen SHALL 为所有状态变化使用平滑动画
5. THE TaskPreviewScreen SHALL 使用300ms的动画时长

### 需求 18: 无障碍支持

**用户故事:** 作为视障用户，我希望能够使用屏幕阅读器操作预览页面，以便无障碍地查看任务信息

#### 验收标准

1. THE TaskPreviewScreen SHALL 为所有信息元素提供contentDescription
2. THE TaskPreviewScreen SHALL 确保所有触摸目标≥48dp
3. THE TaskPreviewScreen SHALL 支持键盘导航
4. THE TaskPreviewScreen SHALL 使用语义化的标签和角色
5. THE TaskPreviewScreen SHALL 确保颜色对比度符合WCAG标准

### 需求 19: 性能优化

**用户故事:** 作为用户，我希望预览页面快速加载和流畅滚动，以便高效查看任务信息

#### 验收标准

1. THE TaskPreviewScreen SHALL 在100ms内完成初始渲染
2. THE TaskPreviewScreen SHALL 使用LazyColumn优化长列表性能
3. THE TaskPreviewScreen SHALL 使用Coil缓存优化图片加载
4. THE TaskPreviewScreen SHALL 避免不必要的重组
5. THE TaskPreviewScreen SHALL 使用derivedStateOf优化计算

### 需求 20: 错误处理

**用户故事:** 作为用户，我希望在加载失败时得到清晰的提示，以便了解问题所在

#### 验收标准

1. WHEN 任务加载失败时，THE TaskPreviewScreen SHALL 显示错误提示
2. THE TaskPreviewScreen SHALL 提供重试按钮
3. WHEN 任务不存在时，THE TaskPreviewScreen SHALL 显示"任务不存在"提示
4. THE TaskPreviewScreen SHALL 在网络错误时显示离线提示
5. THE TaskPreviewScreen SHALL 使用Snackbar显示错误信息
