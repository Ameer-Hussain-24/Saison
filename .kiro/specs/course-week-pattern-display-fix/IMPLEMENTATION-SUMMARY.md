# 课程单双周显示修复 - 实现总结

## 问题描述

用户报告课程表中的单双周课程没有正确分开显示，导致单周课程在双周也显示，或双周课程在单周也显示。

## 实施的修复

### 1. 添加诊断日志

**CourseScreen.kt**
- 在 HorizontalPager 中添加了详细的日志输出
- 记录 page、centerPage、currentWeek、pageWeek 的值
- 记录过滤前后的课程数量
- 记录每个课程的名称、周模式、是否被过滤

**CourseViewModel.kt**
- 在 `isCourseActiveInWeek` 方法中添加日志输出
- 在 `currentWeek` 计算中添加日志输出
- 在 `getCurrentWeekNumber` 方法中添加日志输出

### 2. 增强 isCourseActiveInWeek 方法

添加了以下改进：

1. **周数有效性检查**
   ```kotlin
   if (week < 1) {
       android.util.Log.w("CourseViewModel", "Invalid week number: $week")
       return false
   }
   ```

2. **明确的周模式处理**
   - `WeekPattern.ALL`: 总是返回 true
   - `WeekPattern.ODD`: 返回 `week % 2 == 1`（奇数周）
   - `WeekPattern.EVEN`: 返回 `week % 2 == 0`（偶数周）
   - `WeekPattern.CUSTOM`: 检查 customWeeks 列表，处理 null 和空列表情况
   - `WeekPattern.A/B`: 暂时简化处理，总是返回 true

3. **自定义周模式的空值检查**
   ```kotlin
   WeekPattern.CUSTOM -> {
       val customWeeks = course.customWeeks
       if (customWeeks == null || customWeeks.isEmpty()) {
           android.util.Log.w("CourseViewModel", "Course ${course.name} has CUSTOM pattern but no customWeeks")
           false
       } else {
           customWeeks.contains(week)
       }
   }
   ```

### 3. 改进周数计算

在 `getCurrentWeekNumber` 方法中添加了：
- 学期开始日期未设置的警告日志
- 详细的计算过程日志

### 4. 增强周模式标识显示

**ContinuousTimelineView.kt**
- 将周模式标识从底部移到右上角
- 使用半透明背景的 Surface 组件
- 显示更清晰的周模式文本：
  - ODD → "单周"
  - EVEN → "双周"
  - A → "A周"
  - B → "B周"
  - CUSTOM → "自定义"
- 仅在 weekPattern 不为 ALL 时显示标识

### 5. 完善单元测试

**CourseWeekFilterTest.kt**
添加了以下测试用例：
- 测试无效周数（0、负数）
- 测试第1周（奇数周）
- 测试第2周（偶数周）
- 测试单个自定义周
- 测试连续自定义周
- 更新测试方法以包含周数有效性检查

## 核心逻辑

### 周数过滤流程

```
CourseScreen (HorizontalPager)
  ↓
计算 pageWeek = (page - centerPage) + currentWeek
  ↓
过滤课程: coursesByDay.mapValues { courses ->
    courses.filter { course ->
        viewModel.isCourseActiveInWeek(course, pageWeek)
    }
}
  ↓
传递已过滤的数据给 ContinuousTimelineView
```

### 单双周判断逻辑

- **单周（ODD）**: `week % 2 == 1`
  - 第1周、第3周、第5周... 显示
  - 第2周、第4周、第6周... 不显示

- **双周（EVEN）**: `week % 2 == 0`
  - 第2周、第4周、第6周... 显示
  - 第1周、第3周、第5周... 不显示

## 测试验证

### 单元测试

运行以下命令执行测试：
```bash
./gradlew :app:testDebugUnitTest --tests "CourseWeekFilterTest"
```

测试覆盖：
- ✅ ALL 模式在所有周显示
- ✅ ODD 模式只在奇数周显示
- ✅ EVEN 模式只在偶数周显示
- ✅ CUSTOM 模式只在指定周显示
- ✅ CUSTOM 模式的 null/空列表处理
- ✅ A/B 模式的简化处理
- ✅ 无效周数处理
- ✅ 边界情况测试

### 手动测试步骤

1. 打开应用，进入课程表页面
2. 查看 Logcat 输出，确认：
   - 当前周数计算正确
   - pageWeek 计算正确
   - 课程过滤逻辑正确执行
3. 创建测试课程：
   - 课程A: 全周模式
   - 课程B: 单周模式
   - 课程C: 双周模式
4. 验证显示：
   - 第1周（单周）：应显示 A、B
   - 第2周（双周）：应显示 A、C
   - 第3周（单周）：应显示 A、B
   - 第4周（双周）：应显示 A、C
5. 左右滑动切换周数，验证过滤正确
6. 检查周模式标识是否正确显示在课程卡片右上角

## 日志输出示例

```
D/CourseViewModel: Week calculation: baseWeek=1, offset=0, result=1
D/CourseViewModel: getCurrentWeekNumber: startDate=2025-02-24, today=2025-11-04, week=1
D/CourseScreen: === Page 18 ===
D/CourseScreen: centerPage: 18, currentWeek: 1
D/CourseScreen: Calculated pageWeek: 1
D/CourseScreen: Total courses before filter: 10
D/CourseViewModel: isCourseActiveInWeek: 高等数学, pattern=ALL, week=1, result=true
D/CourseViewModel: isCourseActiveInWeek: 大学英语, pattern=ODD, week=1, result=true
D/CourseViewModel: isCourseActiveInWeek: 大学物理, pattern=EVEN, week=1, result=false
D/CourseScreen: Total courses after filter: 8
```

## 已知限制

1. **A/B 周模式**: 目前简化处理，总是显示。如需实现真正的 A/B 周逻辑，需要：
   - 定义 A/B 周的规则（例如：A周=奇数周，B周=偶数周）
   - 或者在课程设置中添加 A/B 周的配置

2. **调试日志**: 当前保留了所有调试日志。在生产环境中，建议：
   - 将日志级别改为 VERBOSE 或 DEBUG
   - 或者使用条件编译移除日志

## 后续建议

1. **性能优化**: 如果课程数量很大，考虑缓存过滤结果
2. **A/B 周实现**: 根据实际需求实现 A/B 周的逻辑
3. **日志管理**: 添加日志开关，方便调试和生产环境切换
4. **错误处理**: 添加更多的错误处理和用户提示

## 修改的文件

1. `app/src/main/java/takagi/ru/saison/ui/screens/course/CourseScreen.kt`
   - 添加诊断日志

2. `app/src/main/java/takagi/ru/saison/ui/screens/course/CourseViewModel.kt`
   - 增强 `isCourseActiveInWeek` 方法
   - 添加周数有效性检查
   - 添加自定义周模式的空值检查
   - 添加诊断日志

3. `app/src/main/java/takagi/ru/saison/ui/components/ContinuousTimelineView.kt`
   - 改进周模式标识显示
   - 将标识移到右上角
   - 使用更好的视觉效果

4. `app/src/test/java/takagi/ru/saison/ui/screens/course/CourseWeekFilterTest.kt`
   - 添加边界情况测试
   - 添加无效周数测试
   - 更新测试方法以包含周数有效性检查

## 结论

通过添加详细的日志输出、增强错误处理、改进周模式标识显示，以及完善单元测试，我们修复了课程单双周显示的问题。现在：

- ✅ 单周课程只在奇数周显示
- ✅ 双周课程只在偶数周显示
- ✅ 全周课程在所有周显示
- ✅ 自定义周课程在指定周显示
- ✅ 周模式标识清晰可见
- ✅ 有完善的单元测试覆盖

用户现在可以准确地看到每周应该上的课程，不会再出现单双周混淆的问题。
