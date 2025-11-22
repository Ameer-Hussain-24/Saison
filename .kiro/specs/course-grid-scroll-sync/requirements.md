# 需求文档

## 简介

课程表网格视图中，左侧的时间列和右侧的课程网格应该同步滚动，但目前网格滚动时，左侧的时间列没有跟随滚动，导致时间和课程不对齐。

## 术语表

- **GridTimetableView**: 网格课程表视图组件，以网格形式显示课程表
- **TimeColumn**: 左侧显示节次时间的列（如"08:00-08:45"）
- **CourseGrid**: 右侧显示课程的网格区域
- **LazyColumn**: Jetpack Compose 的可滚动列表组件
- **ScrollState**: 滚动状态，用于同步多个可滚动组件

## 需求

### 需求 1

**用户故事:** 作为用户，我希望在课程表页面滚动时，左侧的时间列能够跟随课程网格一起滚动，以便我能清楚地看到每个课程对应的时间段。

#### 验收标准

1. WHEN 用户在课程表网格区域向上或向下滚动时，THE GridTimetableView SHALL 同步滚动左侧的时间列
2. WHEN 用户滚动课程网格时，THE GridTimetableView SHALL 保持时间列与对应的课程行完全对齐
3. THE GridTimetableView SHALL 确保时间列和课程网格使用相同的滚动状态
4. THE GridTimetableView SHALL 防止时间列独立滚动，只能通过课程网格的滚动来驱动

### 需求 2

**用户故事:** 作为用户，我希望课程表的滚动体验流畅自然，没有延迟或卡顿，以便获得良好的使用体验。

#### 验收标准

1. THE GridTimetableView SHALL 在滚动时保持至少 60fps 的帧率
2. THE GridTimetableView SHALL 在滚动过程中不出现明显的延迟或不同步现象
3. THE GridTimetableView SHALL 使用高效的布局方案避免不必要的重组
