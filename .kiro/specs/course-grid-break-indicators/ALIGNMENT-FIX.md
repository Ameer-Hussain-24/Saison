# 课表休息标识对齐修复

## 问题
之前的实现中，休息标识（午休、晚休）只在课程网格中显示，但左侧的节次时间列没有对应的空白行，导致课程和节次无法对齐。

## 解决方案

### 1. 创建共享的 GridRowBuilder 工具类
- 文件: pp/src/main/java/takagi/ru/saison/util/GridRowBuilder.kt
- 功能: 根据节次的 	imeOfDay 字段自动检测时段变化，在上午→下午之间插入午休标识，在下午→晚上之间插入晚休标识

### 2. 更新 PeriodTimeColumn 组件
- 使用共享的 uildGridRows 函数
- 添加 showBreakIndicators 参数来控制是否显示休息标识
- 当检测到 BreakRow 时，渲染 BreakSeparatorCell 组件（高度32dp）

### 3. 更新 GridTimetableView 组件
- 导入并使用共享的 uildGridRows 函数
- 删除本地的重复实现
- 确保左侧时间列和右侧课程网格使用相同的行列表

## 效果
- 左侧节次时间列和右侧课程网格完全对齐
- 午休和晚休标识横跨整个课表宽度
- 基于 GridLayoutConfig.showBreakSeparators 配置可以开关此功能

## 相关文件
- pp/src/main/java/takagi/ru/saison/util/GridRowBuilder.kt (新建)
- pp/src/main/java/takagi/ru/saison/ui/components/PeriodTimeColumn.kt (更新)
- pp/src/main/java/takagi/ru/saison/ui/components/GridTimetableView.kt (更新)
- pp/src/main/java/takagi/ru/saison/domain/model/BreakType.kt (新建)
- pp/src/main/java/takagi/ru/saison/domain/model/GridRow.kt (新建)
- pp/src/main/java/takagi/ru/saison/ui/components/BreakIndicatorRow.kt (新建)
