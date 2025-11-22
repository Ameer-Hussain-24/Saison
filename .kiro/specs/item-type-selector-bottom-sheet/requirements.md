# 需求文档

## 简介

本功能在任务列表页面添加一个 Material 3 风格的 Bottom Sheet 选择器，允许用户在不同的项目类型（任务、日程、事件）之间切换。通过点击顶部应用栏的标题来触发 Bottom Sheet。这为多类型项目管理系统奠定了基础，每种类型都有自己的数据库和 UI，但最初只实现 Bottom Sheet 选择器。

## 术语表

- **项目类型选择器**：允许用户在不同类型的项目（任务、日程、事件）之间进行选择的 UI 组件
- **Bottom Sheet**：Material 3 组件，从屏幕底部向上滑动以显示内容
- **任务列表页面**：显示任务列表的主屏幕，带有顶部应用栏
- **顶部应用栏**：任务列表页面顶部的导航栏，包含标题和操作按钮
- **项目类型**：可以管理的项目类别（任务、日程或事件）

## 需求

### 需求 1

**用户故事：** 作为用户，我希望在顶部应用栏看到可点击的标题，以便访问项目类型选择器

#### 验收标准

1. WHEN 任务列表页面显示时，THE 顶部应用栏 SHALL 将当前项目类型显示为可点击的文本元素
2. THE 顶部应用栏 SHALL 在项目类型文本旁边显示下拉指示图标
3. WHEN 用户点击项目类型文本或下拉指示图标时，THE 项目类型选择器 SHALL 以 Bottom Sheet 形式打开
4. THE 可点击标题 SHALL 使用与应用主题一致的 Material 3 样式

### 需求 2

**用户故事：** 作为用户，我希望看到带有项目类型选项的 Material 3 Bottom Sheet，以便了解有哪些类型可用

#### 验收标准

1. WHEN 用户点击项目类型标题时，THE 项目类型选择器 SHALL 以 Material 3 Bottom Sheet 形式从底部向上滑动显示
2. THE Bottom Sheet SHALL 显示三个选项："任务"、"日程"和"事件"
3. THE Bottom Sheet SHALL 使用 Material 3 样式，包括适当的高度、圆角半径和表面颜色
4. THE Bottom Sheet SHALL 在顶部包含一个拖动手柄以便关闭
5. WHEN 用户点击 Bottom Sheet 外部或向下滑动时，THE Bottom Sheet SHALL 关闭

### 需求 3

**用户故事：** 作为用户，我希望从 Bottom Sheet 中选择项目类型，以便表明我的偏好

#### 验收标准

1. WHEN 用户点击 Bottom Sheet 中的项目类型选项时，THE Bottom Sheet SHALL 关闭
2. THE 顶部应用栏标题 SHALL 更新以反映所选的项目类型
3. THE 所选项目类型 SHALL 在 Bottom Sheet 关闭前以视觉方式指示
4. WHEN 选择"任务"时，THE 顶部应用栏 SHALL 显示"任务"
5. WHEN 选择"日程"时，THE 顶部应用栏 SHALL 显示"日程"
6. WHEN 选择"事件"时，THE 顶部应用栏 SHALL 显示"事件"

### 需求 4

**用户故事：** 作为用户，我希望项目类型选择被持久化，以便在应用会话之间记住我的选择

#### 验收标准

1. WHEN 用户选择项目类型时，THE 项目类型选择器 SHALL 将选择保存到本地存储
2. WHEN 应用重启时，THE 顶部应用栏 SHALL 显示之前选择的项目类型
3. IF 之前没有选择过项目类型，THE 顶部应用栏 SHALL 默认显示"任务"

### 需求 5

**用户故事：** 作为用户，我希望 Bottom Sheet 支持国际化，以便以我的首选语言查看选项

#### 验收标准

1. THE Bottom Sheet SHALL 使用字符串资源显示项目类型标签以支持国际化
2. THE Bottom Sheet SHALL 支持简体中文、日语、越南语和英语
3. WHEN 应用语言更改时，THE Bottom Sheet 标签 SHALL 更新为相应的语言
4. THE 顶部应用栏标题 SHALL 也使用国际化字符串资源
