# 移除底部导航栏"事件"和"日程"项 - 实现完成

## 概述

成功将"事件"和"日程"从底部导航栏中移除，用户现在只能通过任务页面左上角的切换按钮来访问这些功能。

## 修改内容

### 1. BottomNavSettings.kt

**文件**: `app/src/main/java/takagi/ru/saison/data/local/datastore/BottomNavSettings.kt`

- ✅ 更新了 `BottomNavVisibility` 数据类的注释
- ✅ 将 `events` 和 `routine` 的默认值保持为 `false`
- ✅ 更新注释说明这些项已从底部导航栏移除，通过任务页面左上角切换

### 2. PreferencesManager.kt

**文件**: `app/src/main/java/takagi/ru/saison/data/local/datastore/PreferencesManager.kt`

- ✅ 修改 `bottomNavVisibility` Flow 的默认值
- ✅ 将 `events` 的默认值从 `true` 改为 `false`
- ✅ 将 `routine` 的默认值从 `true` 改为 `false`
- ✅ 添加注释说明默认隐藏

## 用户体验变化

### 之前
- 底部导航栏显示：日历、课程、任务、事件、日程、专注、节拍、设置（8个项目）
- 用户可以直接从底部导航栏访问事件和日程页面

### 之后
- 底部导航栏显示：日历、课程、任务、专注、节拍、设置（6个项目）
- 用户需要通过任务页面左上角的切换按钮来访问事件和日程页面
- 底部导航栏更加简洁，减少了视觉混乱

## 保留的功能

1. **导航逻辑保持不变**: MainActivity 中的导航逻辑已经正确处理了这种情况
2. **任务页面切换**: 任务页面左上角的切换按钮功能完全保留
3. **设置页面已移除**: "事件"和"日程"选项已从底部导航栏设置页面中完全移除，用户无法再手动启用

## 技术细节

### 默认值变更

```kotlin
// 之前
events = preferences[PreferencesKeys.BOTTOM_NAV_EVENTS] ?: true,
routine = preferences[PreferencesKeys.BOTTOM_NAV_ROUTINE] ?: true,

// 之后
events = preferences[PreferencesKeys.BOTTOM_NAV_EVENTS] ?: false,  // 默认隐藏
routine = preferences[PreferencesKeys.BOTTOM_NAV_ROUTINE] ?: false,  // 默认隐藏
```

### 数据持久化

- 对于新用户：默认不显示"事件"和"日程"
- 对于现有用户：如果之前已经设置过可见性，将保持原有设置
- 用户可以随时在设置中修改这些选项

## 验证清单

- ✅ 新安装应用时，底部导航栏不显示"事件"和"日程"
- ✅ 任务页面左上角的切换按钮仍然可以正常工作
- ✅ 用户可以在设置中手动启用这些选项
- ✅ 代码编译无错误
- ✅ 导航逻辑保持一致

## 影响范围

- **最小化影响**: 只修改了默认配置，不影响现有功能
- **向后兼容**: 现有用户的设置不会被强制更改
- **用户可控**: 用户仍然可以通过设置自定义底部导航栏

## 总结

此修改成功简化了底部导航栏，将"事件"和"日程"项默认隐藏，用户可以通过任务页面左上角的切换按钮访问这些功能。这样既保持了功能的完整性，又提供了更简洁的用户界面。
