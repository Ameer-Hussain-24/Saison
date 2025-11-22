# 调试检查清单

## 请按以下步骤检查

### 1. 查看Logcat日志

在Android Studio的Logcat中搜索以下标签：

#### ImportPreviewScreen日志
```
ImportPreviewScreen: LaunchedEffect triggered: periods.size=?
```
- 如果显示`periods.size=0`，说明CourseSettings未正确加载
- 如果显示`periods.size=8`，说明节次配置正常

#### PeriodMatcher日志
```
PeriodMatcher: === Matching period for time 15:30-17:00 ===
PeriodMatcher: Description: '第7 - 8节\n行m404'
PeriodMatcher: Available periods: ?
PeriodMatcher: Trying to extract period from description: '第7 - 8节\n行m404'
PeriodMatcher: Range pattern matched: start=7, end=8
PeriodMatcher: Extracted period from description: 7-8 for time 15:30-17:00
```

### 2. 检查导入的课程数据

导入后，查看一门课程的详细信息：
- `periodStart`: 应该是7（不是null）
- `periodEnd`: 应该是8（不是null）
- `isCustomTime`: 应该是false
- `startTime`: 15:30
- `endTime`: 17:00

### 3. 可能的问题场景

#### 场景A：periods为空
**症状：** Logcat显示`periods.size=0`

**原因：**
- CourseSettings未初始化
- PreferencesManager返回默认值
- calculatePeriods未被调用

**解决：**
1. 打开应用的课程设置
2. 确认节次配置（应该显示8节课）
3. 如果没有，手动设置并保存
4. 重新导入

#### 场景B：periods不为空但匹配失败
**症状：** Logcat显示`periods.size=8`但`periodStart=null`

**原因：**
- 正则表达式未匹配到描述
- 时间匹配算法失败

**解决：**
查看Logcat中的"Range pattern matched"日志
- 如果没有这行日志，说明正则表达式有问题
- 检查description的实际内容

#### 场景C：匹配成功但UI显示错误
**症状：** Logcat显示匹配成功，但UI还是显示在第1节

**原因：**
- UI渲染逻辑有问题
- 课程数据未正确保存到数据库

**解决：**
检查GridTimetableView或CourseCard的渲染逻辑

### 4. 快速测试

创建一个测试课程：
```kotlin
Course(
    name = "测试课程",
    periodStart = 7,
    periodEnd = 8,
    startTime = LocalTime.of(15, 30),
    endTime = LocalTime.of(17, 0),
    // ... 其他字段
)
```

手动添加这个课程，看看是否显示在第7-8节。如果显示正确，说明UI没问题，问题在导入逻辑。

### 5. 终极解决方案

如果以上都不行，使用**自动生成节次策略**：

修改ImportPreviewScreen：
```kotlin
strategy = TimeMatchingStrategy.AutoCreatePeriods,
existingPeriods = emptyList()
```

这样系统会从ICS文件自动生成节次配置，完全匹配ICS文件的时间。

## 需要提供的信息

如果问题仍然存在，请提供：

1. **Logcat日志**
   - ImportPreviewScreen的日志
   - PeriodMatcher的日志
   - 任何错误或警告信息

2. **课程设置截图**
   - 打开课程设置界面
   - 查看节次配置

3. **导入后的课程数据**
   - 查看一门课程的详细信息
   - 特别是periodStart、periodEnd、isCustomTime字段

4. **ICS文件示例**
   - 提供一个有问题的课程的VEVENT内容

有了这些信息，我可以更准确地定位问题。
