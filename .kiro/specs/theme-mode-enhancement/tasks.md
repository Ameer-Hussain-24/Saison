# 实现计划

- [x] 1. 创建 ThemeMode 枚举类


  - 创建 `ThemeMode.kt` 文件在 `data/local/datastore` 包中
  - 定义三个枚举值：FOLLOW_SYSTEM、LIGHT、DARK
  - 为每个枚举值添加 displayNameRes 和 descriptionRes 属性
  - 实现 fromString 伴生对象方法用于反序列化
  - _需求: 1.1, 1.2, 1.3, 1.4_



- [ ] 2. 更新 ThemePreferences 数据类
  - 打开 `ThemePreferences.kt` 文件
  - 移除 `isDarkMode: Boolean` 属性




  - 添加 `themeMode: ThemeMode` 属性，默认值为 `ThemeMode.FOLLOW_SYSTEM`
  - _需求: 2.6_



- [ ] 3. 更新 PreferencesManager 数据持久化
  - [ ] 3.1 更新 PreferencesKeys
    - 添加 `THEME_MODE = stringPreferencesKey("theme_mode")`
    - 保留旧的 `IS_DARK_MODE` 键用于迁移（不删除定义）
    - _需求: 2.1, 2.2_


  
  - [ ] 3.2 实现数据迁移逻辑
    - 在 `themePreferences` Flow 的 map 中添加迁移逻辑
    - 优先读取 THEME_MODE，如果不存在则读取 IS_DARK_MODE


    - 将 IS_DARK_MODE 的 true/false 映射到 DARK/LIGHT
    - 如果都不存在，使用 FOLLOW_SYSTEM 作为默认值
    - _需求: 2.5, 9.1, 9.2, 9.3, 9.4_

  
  - [x] 3.3 添加 setThemeMode 函数




    - 创建 `suspend fun setThemeMode(mode: ThemeMode)` 函数
    - 保存 mode.name 到 THEME_MODE 键
    - 同时删除旧的 IS_DARK_MODE 键（清理旧数据）
    - _需求: 2.3, 9.5_


  
  - [ ] 3.4 添加 themeMode Flow
    - 创建 `val themeMode: Flow<ThemeMode>` 属性
    - 从 themePreferences 中提取 themeMode


    - _需求: 2.4_
  
  - [ ] 3.5 移除旧的 setDarkMode 函数
    - 删除 `suspend fun setDarkMode(enabled: Boolean)` 函数

    - _需求: 2.1_



- [ ] 4. 更新 ThemeManager
  - [ ] 4.1 更新状态 Flow
    - 移除 `_isDarkMode` 和 `isDarkMode` StateFlow
    - 添加 `_themeMode` MutableStateFlow 和 `themeMode` StateFlow




    - 初始值为 `ThemeMode.FOLLOW_SYSTEM`
    - _需求: 3.1, 3.2_
  
  - [x] 4.2 更新 init 订阅逻辑

    - 在 preferencesManager.themePreferences.collect 中
    - 更新 `_themeMode.value = prefs.themeMode`
    - 移除 `_isDarkMode.value = prefs.isDarkMode`
    - _需求: 3.5_
  
  - [x] 4.3 添加 setThemeMode 函数




    - 创建 `suspend fun setThemeMode(mode: ThemeMode)` 函数
    - 更新本地 StateFlow
    - 调用 preferencesManager.setThemeMode(mode)


    - _需求: 3.3_
  
  - [ ] 4.4 移除 setDarkMode 函数
    - 删除 `suspend fun setDarkMode(enabled: Boolean)` 函数
    - _需求: 3.4_


- [x] 5. 更新 ThemeViewModel




  - 移除 `isDarkMode` StateFlow
  - 添加 `themeMode` StateFlow，从 themeManager.themeMode 映射
  - 使用 stateIn 转换，默认值为 ThemeMode.FOLLOW_SYSTEM
  - _需求: 3.1, 3.2_

- [x] 6. 更新 MainActivity 主题应用逻辑

  - [ ] 6.1 更新 SaisonAppWithTheme Composable
    - 移除 `val isDarkMode by themeViewModel.isDarkMode.collectAsState()`
    - 添加 `val themeMode by themeViewModel.themeMode.collectAsState()`
    - 添加 `val systemInDarkTheme = isSystemInDarkTheme()`
    - _需求: 4.1, 4.2, 4.3_
  
  - [x] 6.2 实现主题模式逻辑

    - 使用 when 表达式计算 darkTheme 值
    - FOLLOW_SYSTEM → systemInDarkTheme
    - LIGHT → false
    - DARK → true




    - 将计算后的 darkTheme 传递给 SaisonTheme
    - _需求: 4.1, 4.2, 4.3, 4.4, 4.5_


- [ ] 7. 更新 SettingsViewModel
  - [ ] 7.1 更新状态 Flow
    - 移除 `isDarkMode` StateFlow
    - 添加 `themeMode` StateFlow，从 themeManager.themeMode 映射
    - _需求: 7.1, 7.2_
  

  - [ ] 7.2 添加 setThemeMode 函数
    - 创建 `fun setThemeMode(mode: ThemeMode)` 函数
    - 在 viewModelScope 中调用 themeManager.setThemeMode(mode)
    - 根据 mode 发出不同的成功 Snackbar 消息

    - 添加 try-catch 错误处理
    - _需求: 7.4, 7.5, 7.6, 12.1, 12.2, 12.3_
  
  - [ ] 7.3 移除 setDarkMode 函数
    - 删除 `fun setDarkMode(enabled: Boolean)` 函数
    - _需求: 7.3_


- [ ] 8. 创建 ThemeModeSelectionDialog UI 组件
  - [ ] 8.1 创建 ThemeModeOptionCard 组件
    - 使用 Card 组件包裹
    - 左侧显示 RadioButton
    - 右侧显示名称和描述（Column 布局）
    - 选中时显示 primaryContainer 背景和 primary 边框
    - 添加点击涟漪效果
    - _需求: 6.6, 6.7_
  
  - [ ] 8.2 创建 ThemeModeSelectionDialog 组件
    - 使用 Material 3 AlertDialog
    - 标题显示"主题模式"
    - 使用 Column 垂直排列三个 ThemeModeOptionCard
    - 每个选项间距 8dp
    - 提供 confirmButton（关闭按钮）
    - _需求: 6.1, 6.2, 6.3_




  
  - [ ] 8.3 实现选择交互逻辑
    - 点击选项时调用 onModeSelected 回调
    - 立即关闭对话框（调用 onDismiss）
    - 使用 RadioButton 标识当前选中项
    - _需求: 6.4, 6.5_



- [ ] 9. 更新 SettingsScreen
  - [x] 9.1 移除深色模式开关



    - 在外观设置分组中删除 SettingsSwitchItem（深色模式）
    - _需求: 5.1_



  
  - [x] 9.2 添加主题模式选择项



    - 在外观设置分组中添加 SettingsItem
    - 使用 DarkMode 图标
    - 标题为"主题模式"

    - 副标题显示当前选中的模式名称
    - 点击打开 ThemeModeSelectionDialog
    - _需求: 5.2, 5.3, 5.4, 5.5_
  
  - [x] 9.3 添加对话框状态管理

    - 添加 `var showThemeModeDialog by remember { mutableStateOf(false) }`
    - 在 SettingsItem 的 onClick 中设置为 true
    - _需求: 5.3_
  
  - [ ] 9.4 集成 ThemeModeSelectionDialog
    - 使用 if 条件渲染对话框

    - 传递 currentMode 为 themeMode

    - onModeSelected 调用 viewModel.setThemeMode(mode)
    - onDismiss 设置 showThemeModeDialog 为 false
    - _需求: 6.1, 6.5_
  

  - [ ] 9.5 添加辅助函数
    - 创建 `getThemeModeName(mode: ThemeMode): Int` 函数
    - 返回 mode.displayNameRes
    - _需求: 5.4_

- [x] 10. 添加字符串资源

  - [ ] 10.1 添加中文字符串资源
    - 在 `values-zh-rCN/strings.xml` 中添加
    - settings_theme_mode_title: "主题模式"
    - theme_mode_follow_system: "跟随系统"

    - theme_mode_follow_system_desc: "根据系统设置自动切换深色/浅色模式"
    - theme_mode_light: "白天模式"
    - theme_mode_light_desc: "始终使用浅色主题"
    - theme_mode_dark: "夜间模式"

    - theme_mode_dark_desc: "始终使用深色主题"

    - _需求: 8.1_
  
  - [ ] 10.2 添加英文字符串资源
    - 在 `values/strings.xml` 中添加对应的英文翻译
    - _需求: 8.2_

  
  - [ ] 10.3 添加日文字符串资源
    - 在 `values-ja/strings.xml` 中添加对应的日文翻译
    - _需求: 8.3_

  

  - [ ] 10.4 添加越南文字符串资源
    - 在 `values-vi/strings.xml` 中添加对应的越南文翻译
    - _需求: 8.4, 8.5_

- [ ] 11. 实现无障碍支持
  - [ ] 11.1 添加 ContentDescription
    - 为主题模式选择项的 Icon 添加 contentDescription

    - 为 RadioButton 添加 contentDescription
    - _需求: 10.1_
  
  - [x] 11.2 添加语义化标签

    - 为 ThemeModeOptionCard 添加 semantics 修饰符


    - 为 RadioButton 添加 stateDescription（已选中/未选中）
    - _需求: 10.2, 10.3_
  


  - [ ] 11.3 添加状态变化宣告
    - 在 setThemeMode 成功时通过 Snackbar 宣告
    - 确保 TalkBack 能够读取状态变化

    - _需求: 10.4_
  
  - [ ] 11.4 确保键盘导航支持
    - 验证对话框支持 Tab 键导航
    - 验证 Enter 键可以选择选项

    - _需求: 10.5_

- [ ] 12. 实现实时预览功能
  - 在 ThemeModeSelectionDialog 中点击选项时立即应用主题


  - 对话框保持打开状态直到用户点击关闭

  - 使用平滑的过渡动画
  - _需求: 11.1, 11.2, 11.3, 11.4_

- [ ] 13. 实现错误处理
  - 在 SettingsViewModel.setThemeMode 中添加 try-catch


  - 捕获异常时发出错误 Snackbar
  - 错误消息清晰说明失败原因
  - 提供重试选项（通过 Snackbar action）
  - _需求: 12.1, 12.2, 12.3, 12.4_

- [ ] 14. 编写单元测试
  - [ ] 14.1 编写 ThemeMode 测试
    - 测试 fromString 方法
    - 测试所有枚举值的属性
    - _需求: 测试策略_
  
  - [ ] 14.2 编写 PreferencesManager 测试
    - 测试数据迁移逻辑（true → DARK, false → LIGHT, null → FOLLOW_SYSTEM）
    - 测试 setThemeMode 函数
    - 测试 themeMode Flow
    - 测试旧数据清理
    - _需求: 测试策略_
  
  - [ ] 14.3 编写 ThemeManager 测试
    - 测试 setThemeMode 函数
    - 测试状态更新
    - _需求: 测试策略_
  
  - [ ] 14.4 编写 SettingsViewModel 测试
    - 测试 setThemeMode 函数
    - 测试错误处理
    - 测试 Snackbar 消息
    - _需求: 测试策略_

- [ ] 15. 编写 UI 测试
  - [ ] 15.1 编写 ThemeModeSelectionDialog 测试
    - 测试三个选项正确显示
    - 测试选择交互
    - 测试对话框关闭
    - _需求: 测试策略_
  
  - [ ] 15.2 编写 SettingsScreen 测试
    - 测试主题模式选择项显示
    - 测试点击打开对话框
    - 测试主题模式更新后副标题变化
    - _需求: 测试策略_

- [ ] 16. 编写集成测试
  - [ ] 16.1 端到端主题切换测试
    - 测试选择 FOLLOW_SYSTEM 模式
    - 测试选择 LIGHT 模式
    - 测试选择 DARK 模式
    - 验证主题实际应用
    - 验证数据持久化
    - _需求: 测试策略_
  
  - [ ] 16.2 数据迁移测试
    - 模拟旧版本数据（IS_DARK_MODE = true/false）
    - 验证迁移到正确的 ThemeMode
    - 验证旧数据被清理
    - _需求: 测试策略_

- [ ] 17. 代码清理和优化
  - [ ] 17.1 搜索并移除所有 isDarkMode 引用
    - 使用全局搜索查找 `isDarkMode`
    - 确认所有引用都已更新或移除
    - _需求: 向后兼容_
  
  - [ ] 17.2 搜索并移除所有 setDarkMode 引用
    - 使用全局搜索查找 `setDarkMode`
    - 确认所有引用都已更新或移除
    - _需求: 向后兼容_
  
  - [ ] 17.3 性能优化
    - 检查不必要的重组
    - 优化 StateFlow 订阅
    - 使用 derivedStateOf 优化计算
    - _需求: 性能考虑_
  
  - [ ] 17.4 代码审查
    - 检查代码风格一致性
    - 确保符合项目规范
    - 添加必要的注释
    - _需求: 代码质量_

- [ ] 18. 文档和总结
  - [ ] 18.1 更新相关文档
    - 更新 README 如果需要
    - 更新开发者文档
    - 记录 API 变更
    - _需求: 文档_
  
  - [ ] 18.2 创建实现总结文档
    - 记录所有变更
    - 记录遇到的问题和解决方案
    - 提供迁移指南
    - _需求: 文档_
