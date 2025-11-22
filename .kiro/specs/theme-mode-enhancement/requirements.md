# 需求文档

## 简介

本文档定义了 Saison 任务管理应用主题模式选择功能的增强需求。当前应用使用简单的深色模式开关（开/关），用户希望升级为更灵活的主题模式选择器，支持"跟随系统"、"白天模式"和"夜间模式"三个选项，并且需要完整的多语言支持。

## 术语表

- **ThemeMode**: 主题模式枚举，定义应用的显示模式（跟随系统、白天、夜间）
- **PreferencesManager**: 数据存储管理器，负责持久化用户偏好设置
- **ThemeManager**: 主题管理器，管理应用的主题状态
- **SettingsScreen**: 设置界面，用户可以在此选择主题模式
- **System Theme**: 系统主题，指 Android 系统级别的深色/浅色模式设置
- **isSystemInDarkTheme**: Compose 函数，用于检测系统当前是否处于深色模式

## 需求

### 需求 1: 主题模式枚举定义

**用户故事:** 作为开发者，我希望定义清晰的主题模式枚举，以便在代码中统一管理主题模式选项

#### 验收标准

1. THE System SHALL 定义 ThemeMode 枚举类，包含三个值：FOLLOW_SYSTEM（跟随系统）、LIGHT（白天模式）、DARK（夜间模式）
2. THE ThemeMode 枚举 SHALL 提供 displayNameRes 属性，返回对应的字符串资源 ID
3. THE ThemeMode 枚举 SHALL 提供 descriptionRes 属性，返回对应的描述字符串资源 ID
4. THE ThemeMode 枚举 SHALL 可序列化为字符串以便存储到 DataStore

### 需求 2: 数据持久化更新

**用户故事:** 作为用户，我希望我选择的主题模式能够被保存，以便下次打开应用时保持我的选择

#### 验收标准

1. THE PreferencesManager SHALL 移除旧的 IS_DARK_MODE 布尔值键
2. THE PreferencesManager SHALL 添加新的 THEME_MODE 字符串键用于存储 ThemeMode
3. THE PreferencesManager SHALL 提供 setThemeMode 函数接受 ThemeMode 参数
4. THE PreferencesManager SHALL 提供 themeMode Flow 返回当前的 ThemeMode
5. WHEN 读取旧数据时，THE PreferencesManager SHALL 自动迁移 IS_DARK_MODE 布尔值到 ThemeMode（true → DARK, false → LIGHT）
6. THE ThemePreferences 数据类 SHALL 使用 themeMode: ThemeMode 替代 isDarkMode: Boolean

### 需求 3: 主题管理器更新

**用户故事:** 作为开发者，我希望 ThemeManager 能够正确处理新的主题模式逻辑，以便应用能够根据用户选择和系统设置显示正确的主题

#### 验收标准

1. THE ThemeManager SHALL 移除 isDarkMode StateFlow
2. THE ThemeManager SHALL 添加 themeMode StateFlow 暴露当前的 ThemeMode
3. THE ThemeManager SHALL 提供 setThemeMode 函数更新主题模式
4. THE ThemeManager SHALL 移除 setDarkMode 函数
5. THE ThemeManager SHALL 从 PreferencesManager 订阅 themeMode 并更新本地状态

### 需求 4: 主题应用逻辑

**用户故事:** 作为用户，我希望应用能够根据我的主题模式选择正确显示深色或浅色主题

#### 验收标准

1. WHEN themeMode 为 FOLLOW_SYSTEM 时，THE SaisonTheme SHALL 使用 isSystemInDarkTheme() 判断是否使用深色主题
2. WHEN themeMode 为 LIGHT 时，THE SaisonTheme SHALL 始终使用浅色主题
3. WHEN themeMode 为 DARK 时，THE SaisonTheme SHALL 始终使用深色主题
4. THE MainActivity SHALL 传递正确的 darkTheme 参数给 SaisonTheme
5. THE SaisonTheme SHALL 根据 darkTheme 参数选择对应的颜色方案

### 需求 5: 设置界面更新

**用户故事:** 作为用户，我希望在设置页面能够选择主题模式，以便控制应用的显示效果

#### 验收标准

1. THE SettingsScreen SHALL 移除深色模式开关（SettingsSwitchItem）
2. THE SettingsScreen SHALL 添加主题模式选择项（SettingsItem）
3. WHEN 用户点击主题模式选择项时，THE SettingsScreen SHALL 打开主题模式选择对话框
4. THE 主题模式选择项 SHALL 显示当前选中的主题模式名称作为副标题
5. THE 主题模式选择项 SHALL 使用 DarkMode 图标

### 需求 6: 主题模式选择对话框

**用户故事:** 作为用户，我希望有一个清晰的对话框让我选择主题模式，以便我能够轻松切换不同的显示模式

#### 验收标准

1. THE ThemeModeSelectionDialog SHALL 使用 Material 3 AlertDialog 组件
2. THE ThemeModeSelectionDialog SHALL 显示三个主题模式选项：跟随系统、白天模式、夜间模式
3. WHEN 显示主题模式选项时，THE ThemeModeSelectionDialog SHALL 为每个选项显示名称和描述
4. THE ThemeModeSelectionDialog SHALL 使用 RadioButton 标识当前选中的主题模式
5. WHEN 用户选择主题模式时，THE ThemeModeSelectionDialog SHALL 立即应用新模式并关闭对话框
6. THE ThemeModeSelectionDialog SHALL 使用卡片式布局显示每个选项
7. THE ThemeModeSelectionDialog SHALL 为选中的选项显示高亮边框

### 需求 7: ViewModel 更新

**用户故事:** 作为开发者，我希望 SettingsViewModel 能够管理新的主题模式状态，以便 UI 能够正确响应用户操作

#### 验收标准

1. THE SettingsViewModel SHALL 移除 isDarkMode StateFlow
2. THE SettingsViewModel SHALL 添加 themeMode StateFlow 暴露当前的 ThemeMode
3. THE SettingsViewModel SHALL 移除 setDarkMode 函数
4. THE SettingsViewModel SHALL 添加 setThemeMode 函数接受 ThemeMode 参数
5. WHEN setThemeMode 被调用时，THE SettingsViewModel SHALL 调用 ThemeManager.setThemeMode
6. WHEN 主题模式更改成功时，THE SettingsViewModel SHALL 发出成功的 Snackbar 消息

### 需求 8: 多语言支持

**用户故事:** 作为多语言用户，我希望主题模式的所有文本都能正确显示我选择的语言

#### 验收标准

1. THE System SHALL 为主题模式相关文本提供简体中文翻译
2. THE System SHALL 为主题模式相关文本提供英语翻译
3. THE System SHALL 为主题模式相关文本提供日语翻译
4. THE System SHALL 为主题模式相关文本提供越南语翻译
5. THE 字符串资源 SHALL 包括：主题模式标题、跟随系统、白天模式、夜间模式及其描述

### 需求 9: 向后兼容

**用户故事:** 作为现有用户，我希望升级后我之前的深色模式设置能够被正确迁移

#### 验收标准

1. WHEN 应用首次启动新版本时，THE PreferencesManager SHALL 检查是否存在旧的 IS_DARK_MODE 设置
2. IF IS_DARK_MODE 为 true，THEN THE PreferencesManager SHALL 将 themeMode 设置为 DARK
3. IF IS_DARK_MODE 为 false，THEN THE PreferencesManager SHALL 将 themeMode 设置为 LIGHT
4. IF IS_DARK_MODE 不存在，THEN THE PreferencesManager SHALL 使用默认值 FOLLOW_SYSTEM
5. WHEN 迁移完成后，THE PreferencesManager SHALL 删除旧的 IS_DARK_MODE 键

### 需求 10: 无障碍支持

**用户故事:** 作为有无障碍需求的用户，我希望主题模式选择功能支持辅助功能

#### 验收标准

1. THE 主题模式选择项 SHALL 提供完整的 contentDescription
2. THE ThemeModeSelectionDialog SHALL 为每个选项提供语义化标签
3. THE RadioButton SHALL 提供正确的选中状态描述
4. WHEN 主题模式改变时，THE System SHALL 通过无障碍服务宣布更改
5. THE 对话框 SHALL 支持键盘导航

### 需求 11: 实时预览

**用户故事:** 作为用户，我希望在选择主题模式时能够立即看到效果，以便确认我的选择

#### 验收标准

1. WHEN 用户在对话框中选择主题模式时，THE System SHALL 立即应用新的主题模式
2. THE 对话框 SHALL 在主题切换时保持打开状态，直到用户点击确认或取消
3. THE 主题切换 SHALL 使用平滑的过渡动画
4. THE 对话框背景和内容 SHALL 随主题切换而更新颜色

### 需求 12: 错误处理

**用户故事:** 作为用户，我希望在主题模式设置失败时能够得到清晰的反馈

#### 验收标准

1. WHEN 保存主题模式失败时，THE SettingsViewModel SHALL 发出错误 Snackbar
2. WHEN 保存失败时，THE System SHALL 回滚到之前的主题模式
3. THE 错误消息 SHALL 清晰说明失败原因
4. THE System SHALL 提供重试选项
