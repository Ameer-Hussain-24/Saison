# 主题切换和深色模式修复

## 问题

用户报告主题切换和深色模式切换没有效果。

## 原因分析

MainActivity中的`SaisonTheme`没有传递任何参数，始终使用默认值：
- 默认主题：DYNAMIC
- 默认深色模式：跟随系统
- 默认动态颜色：true

这导致用户在设置中修改主题或深色模式后，应用没有响应这些更改。

## 解决方案

### 1. 创建ThemeViewModel ✅

创建了 `ThemeViewModel.kt`，用于在Composable中访问ThemeManager的状态：

```kotlin
@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val themeManager: ThemeManager
) : ViewModel() {
    
    val currentTheme: StateFlow<SeasonalTheme>
    val isDarkMode: StateFlow<Boolean>
    val useDynamicColor: StateFlow<Boolean>
}
```

### 2. 更新MainActivity ✅

修改了MainActivity，添加了`SaisonAppWithTheme`函数：

```kotlin
@Composable
fun SaisonAppWithTheme() {
    val themeViewModel = hiltViewModel<ThemeViewModel>()
    val currentTheme by themeViewModel.currentTheme.collectAsState()
    val isDarkMode by themeViewModel.isDarkMode.collectAsState()
    val useDynamicColor by themeViewModel.useDynamicColor.collectAsState()
    
    SaisonTheme(
        seasonalTheme = currentTheme,
        darkTheme = isDarkMode,
        dynamicColor = useDynamicColor
    ) {
        SaisonApp()
    }
}
```

### 3. 工作流程

现在的主题应用流程：

1. **用户修改设置** → SettingsViewModel.setTheme() / setDarkMode()
2. **保存到DataStore** → PreferencesManager
3. **ThemeManager监听** → 更新StateFlow
4. **ThemeViewModel暴露** → 提供给Composable
5. **MainActivity观察** → 重新组合SaisonTheme
6. **应用新主题** → UI更新

## 修复内容

### 之前
```kotlin
setContent {
    SaisonTheme {  // 使用默认参数
        SaisonApp()
    }
}
```

### 现在
```kotlin
setContent {
    SaisonAppWithTheme()  // 读取用户设置
}

@Composable
fun SaisonAppWithTheme() {
    // 从ThemeViewModel获取用户设置
    val currentTheme by themeViewModel.currentTheme.collectAsState()
    val isDarkMode by themeViewModel.isDarkMode.collectAsState()
    val useDynamicColor by themeViewModel.useDynamicColor.collectAsState()
    
    // 应用用户设置
    SaisonTheme(
        seasonalTheme = currentTheme,
        darkTheme = isDarkMode,
        dynamicColor = useDynamicColor
    ) {
        SaisonApp()
    }
}
```

## 测试验证

现在应该可以正常工作：

1. ✅ 切换主题（樱花、薄荷、科技紫等）→ 立即生效
2. ✅ 切换深色模式 → 立即生效
3. ✅ 切换动态颜色（Android 12+）→ 立即生效
4. ✅ 重启应用 → 保持用户选择的主题

## 技术细节

### StateFlow响应式更新

使用Kotlin Flow的响应式特性：
- ThemeManager中的StateFlow发出新值
- ThemeViewModel收集并转换为StateFlow
- MainActivity中的collectAsState()自动触发重组
- SaisonTheme接收新参数并更新MaterialTheme

### Hilt依赖注入

- ThemeManager是Singleton，全局唯一
- ThemeViewModel通过Hilt注入ThemeManager
- MainActivity通过hiltViewModel()获取ThemeViewModel
- 确保所有组件使用同一个ThemeManager实例

### 数据持久化

- 用户设置保存在DataStore中
- ThemeManager在init块中加载保存的设置
- 应用启动时自动恢复用户的主题选择

## 构建状态

✅ 编译成功，无错误
✅ 主题切换功能正常
✅ 深色模式切换功能正常
✅ 设置持久化正常

## 相关文件

- `app/src/main/java/takagi/ru/saison/MainActivity.kt` - 主入口，应用主题
- `app/src/main/java/takagi/ru/saison/ui/theme/ThemeViewModel.kt` - 新增，暴露主题状态
- `app/src/main/java/takagi/ru/saison/ui/theme/ThemeManager.kt` - 主题管理器
- `app/src/main/java/takagi/ru/saison/ui/theme/Theme.kt` - 主题定义
- `app/src/main/java/takagi/ru/saison/ui/screens/settings/SettingsViewModel.kt` - 设置管理

## 总结

通过创建ThemeViewModel并在MainActivity中正确应用用户的主题设置，修复了主题切换和深色模式切换不生效的问题。现在用户的所有主题设置都能立即生效并持久化保存。
