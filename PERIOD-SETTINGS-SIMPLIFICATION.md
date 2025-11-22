# 课程节次设置简化实现总结

## 改动概述

将课程设置中的"上午节次"、"下午节次"、"晚上节次"三个分开的设置合并为一个统一的"每天节次"设置。

## 主要改动

### 1. 数据模型简化 (CourseSettings.kt)
- 移除 `morningPeriods`, `afternoonPeriods`, `eveningPeriods` 字段
- 改为使用单一的 `totalPeriods` 字段
- 保留已弃用的属性用于向后兼容

### 2. UI 简化 (CourseSettingsSheet.kt)
- 移除三个分开的滑块（上午/下午/晚上节次）
- 改为一个统一的"每天节次"滑块（范围：4-15节）
- 移除"总节次数"显示卡片（因为现在直接设置总节次）

### 3. 模板更新 (ScheduleTemplate.kt)
- 更新模板数据结构，使用 `totalPeriods` 替代分时段配置
- 更新预设模板：
  - 小学：6节课
  - 初中：8节课
  - 高中：10节课
  - 大学：9节课

### 4. 节次生成逻辑 (PeriodGenerator.kt)
- 新增 `generatePeriods()` 方法，接受 `totalPeriods` 参数
- 保留 `generatePeriodsFromSegments()` 方法并标记为已弃用
- 节次的时段（上午/下午/晚上）现在根据开始时间自动判断

### 5. 数据存储和迁移 (PreferencesManager.kt)
- 更新数据读取逻辑，支持从旧的分时段配置迁移
- 更新数据保存逻辑，使用 `COURSE_PERIODS_PER_DAY` 存储总节次
- 自动清理旧的分时段数据

### 6. 验证逻辑更新 (CourseSettingsValidator.kt)
- 简化验证逻辑，只验证总节次数（1-15节）
- 移除对分时段节次的验证

## 向后兼容性

- 旧数据会自动迁移：分时段配置会合并为总节次数
- 保留已弃用的属性，避免破坏现有代码
- 旧的 API 方法标记为 `@Deprecated` 但仍可使用

## 用户体验改进

- 设置更简单直观，一次性设置所有节次
- 不再需要考虑上午/下午/晚上的划分
- 午休时间仍然可以通过"午休在第几节课后"设置

## 构建状态

✅ 构建成功 - 所有编译错误已修复

## 测试建议

1. 测试新用户创建课程设置
2. 测试从旧版本升级的数据迁移
3. 测试模板应用功能
4. 测试节次生成和显示
5. 测试 ICS 导入功能

## 额外修复

在实现过程中，还修复了与 `BottomNavTab.SUBSCRIPTION` 相关的编译错误：
- 更新了 `PreferencesManager.updateBottomNavVisibility()`
- 更新了 `BottomNavSettingsScreen` 中的扩展函数
- 更新了 `SettingsScreen` 中的扩展函数
