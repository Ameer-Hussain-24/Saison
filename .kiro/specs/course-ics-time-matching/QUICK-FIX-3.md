# 第三次修复：移除periods空检查

## 问题
课程又全部挤在第一节

## 根本原因
之前的代码有一个条件检查：
```kotlin
if (periods.isNotEmpty()) {
    viewModel.loadPreview(...)
}
```

**问题：** 如果`periods`一直为空（例如CourseSettings未初始化），`loadPreview`就永远不会被调用！

## 解决方案

### 修改前
```kotlin
LaunchedEffect(uri, periods) {
    // 等待periods加载完成后再调用loadPreview
    if (periods.isNotEmpty()) {  // ❌ 如果periods为空就不调用
        viewModel.loadPreview(...)
    }
}
```

### 修改后
```kotlin
LaunchedEffect(uri, periods) {
    android.util.Log.d("ImportPreviewScreen", "LaunchedEffect triggered: periods.size=${periods.size}")
    // 移除空检查，即使periods为空也调用loadPreview
    viewModel.loadPreview(
        uri = uri,
        semesterId = semesterId,
        primaryColor = primaryColor,
        strategy = TimeMatchingStrategy.UseExistingPeriods,
        existingPeriods = periods  // ✅ 传递periods，即使为空
    )
}
```

## 为什么这样修改

1. **PeriodMatcher已经处理空列表** - 如果`existingPeriods.isEmpty()`，会返回`(null, null)`
2. **Course模型支持null节次** - `periodStart`和`periodEnd`可以为null
3. **UI可以显示自定义时间课程** - `isCustomTime=true`的课程会使用时间显示

## 调试步骤

### 1. 查看Logcat
导入时搜索`ImportPreviewScreen`标签：
```
ImportPreviewScreen: LaunchedEffect triggered: periods.size=0
```
或
```
ImportPreviewScreen: LaunchedEffect triggered: periods.size=8
```

### 2. 查看PeriodMatcher日志
```
PeriodMatcher: === Matching period for time 15:30-17:00 ===
PeriodMatcher: Description: '第7 - 8节\n行m404'
PeriodMatcher: Available periods: 0  // ❌ 如果是0说明periods为空
```

### 3. 检查CourseSettings
如果periods一直为空，可能是CourseSettings未初始化。检查：
- 是否有默认的CourseSettings
- PreferencesManager是否正常工作
- CourseViewModel.calculatePeriods是否被调用

## 预期行为

### 如果periods不为空（正常情况）
```
1. ImportPreviewScreen加载
2. CourseViewModel.periods计算节次（8节课）
3. LaunchedEffect触发，periods.size=8
4. 调用loadPreview并传递8个节次
5. PeriodMatcher匹配节次
6. 课程显示在正确位置 ✅
```

### 如果periods为空（降级处理）
```
1. ImportPreviewScreen加载
2. CourseViewModel.periods为空列表
3. LaunchedEffect触发，periods.size=0
4. 调用loadPreview并传递空列表
5. PeriodMatcher返回(null, null)
6. 课程标记为isCustomTime=true
7. 课程使用时间显示（而不是节次） ⚠️
```

## 下一步

如果问题仍然存在，需要检查：

1. **CourseSettings是否正确初始化**
   ```kotlin
   // 在CourseViewModel中
   val courseSettings: StateFlow<CourseSettings>
   ```

2. **calculatePeriods是否正常工作**
   ```kotlin
   fun calculatePeriods(settings: CourseSettings): List<CoursePeriod>
   ```

3. **PreferencesManager是否返回正确的设置**

## 临时解决方案

如果还是不行，可以尝试：

1. **手动配置CourseSettings**
   - 打开课程设置
   - 确认节次配置（8节课，08:00开始等）
   - 保存设置

2. **使用"自动生成节次"策略**
   - 修改ImportPreviewScreen使用`AutoCreatePeriods`
   - 让系统从ICS文件生成节次配置

3. **清除应用数据重试**
   - 清除应用数据
   - 重新配置CourseSettings
   - 再次导入

## 相关文件
- `app/src/main/java/takagi/ru/saison/ui/screens/course/ImportPreviewScreen.kt`
- `app/src/main/java/takagi/ru/saison/ui/screens/course/CourseViewModel.kt`
- `app/src/main/java/takagi/ru/saison/util/PeriodMatcher.kt`
