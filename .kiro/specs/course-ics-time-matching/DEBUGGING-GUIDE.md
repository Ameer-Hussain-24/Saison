# 课程导入节次匹配调试指南

## 当前问题

1. **有的课程被拉得很长** - periodEnd匹配错误
2. **下午的课程变成上午第一节** - 时间匹配算法问题

## 根本原因分析

### 问题1：节次范围验证过严

**之前的代码：**
```kotlin
if (start != null && end != null && 
    start > 0 && end > 0 && 
    start <= existingPeriods.size &&  // ❌ 这里限制了节次必须在现有范围内
    end <= existingPeriods.size) {
    return Pair(start, end)
}
```

**问题：**
- ICS文件中的节次配置可能与应用的默认配置不同
- 例如：ICS说第7-8节是15:30-17:00，但应用默认第7-8节是14:50-16:30
- 如果应用只配置了8节课，但ICS文件有第9-10节，就会被拒绝

### 问题2：时间配置不匹配

**ICS文件的节次时间：**
```
外事实务：15:30-17:00（第7-8节）
```

**应用默认配置计算的节次时间：**
```
- 第1节：08:00-08:45
- 第2节：08:55-09:40
- 第3节：09:50-10:35
- 第4节：10:45-11:30
- 午休：11:30-13:00
- 第5节：13:00-13:45
- 第6节：13:55-14:40
- 第7节：14:50-15:35  ❌ 与ICS不匹配
- 第8节：15:45-16:30  ❌ 与ICS不匹配
```

**结论：**
ICS文件的节次时间配置与应用默认配置不同！这是导致匹配错误的根本原因。

## 解决方案

### 修复1：放宽节次范围验证

```kotlin
// 只验证节次是否为正数和顺序正确，不验证是否在现有节次范围内
// 因为ICS文件的节次配置可能与当前设置不同
if (start != null && end != null && start > 0 && end > 0 && start <= end) {
    return Pair(start, end)
}
```

**优点：**
- 允许导入超出当前配置范围的节次
- 信任ICS文件中的节次信息
- 避免因配置不匹配导致的匹配失败

### 修复2：添加调试日志

添加了详细的日志输出，帮助诊断问题：

```kotlin
android.util.Log.d("PeriodMatcher", "=== Matching period for time $startTime-$endTime ===")
android.util.Log.d("PeriodMatcher", "Description: '$description'")
android.util.Log.d("PeriodMatcher", "Extracted period from description: $start-$end")
```

## 调试步骤

### 1. 查看Logcat日志

导入课程后，在Android Studio的Logcat中搜索`PeriodMatcher`标签：

```
PeriodMatcher: === Matching period for time 15:30-17:00 ===
PeriodMatcher: Description: '第7 - 8节\n行m404'
PeriodMatcher: Trying to extract period from description: '第7 - 8节\n行m404'
PeriodMatcher: Range pattern matched: start=7, end=8
PeriodMatcher: Extracted period from description: 7-8 for time 15:30-17:00
```

### 2. 验证节次提取

检查日志中的"Range pattern matched"或"Single pattern matched"：
- ✅ 如果匹配成功，会显示提取的节次
- ❌ 如果显示"No period pattern matched"，说明正则表达式有问题

### 3. 检查时间匹配

如果描述中没有节次信息，会使用时间匹配：
```
PeriodMatcher: No period in description, using time-based matching
PeriodMatcher: Time-based matching result: 1-1
```

### 4. 验证最终结果

在CourseConverter中也会有日志输出，显示最终分配的节次。

## 常见问题

### Q1: 为什么有的课程还是显示错误？

**可能原因：**
1. 描述格式不标准（如"7-8节"而不是"第7-8节"）
2. 时间匹配算法的阈值设置不合适
3. 节次配置与ICS文件差异太大

**解决方法：**
- 查看Logcat日志确认提取结果
- 调整正则表达式支持更多格式
- 考虑使用"自动生成节次"策略

### Q2: 为什么时间匹配不准确？

**原因：**
时间匹配算法使用重叠度计算，如果ICS文件的时间与应用配置差异较大，可能匹配到错误的节次。

**解决方法：**
- 优先使用描述中的节次信息
- 调整CourseSettings使其与ICS文件的时间配置一致
- 使用"自动生成节次"策略从ICS文件生成节次配置

### Q3: 如何处理超出范围的节次？

**例如：** ICS文件有第9-10节，但应用只配置了8节课

**当前行为：**
- 修复后会接受第9-10节
- 课程会被创建，但可能在UI上显示异常

**建议：**
1. 使用"自动生成节次"策略，让应用根据ICS文件生成节次配置
2. 或者手动调整CourseSettings，增加节次数量

## 最佳实践

### 推荐导入流程

1. **首次导入：** 使用"自动生成节次"策略
   - 让应用根据ICS文件自动生成节次配置
   - 确保节次时间与ICS文件完全匹配

2. **后续导入：** 使用"使用现有节次"策略
   - 如果节次配置已经正确设置
   - 新导入的课程会匹配到现有节次

### 验证导入结果

导入后检查：
1. 课程是否显示在正确的节次位置
2. 课程时间是否与ICS文件一致
3. 是否有课程被标记为"自定义时间"

## 下一步改进

### 短期改进
1. ✅ 放宽节次范围验证
2. ✅ 添加详细的调试日志
3. ⏳ 在UI中显示匹配警告

### 中期改进
4. ⏳ 实现TimeMatchingDialog集成
5. ⏳ 支持导入时选择匹配策略
6. ⏳ 在预览界面显示节次匹配结果

### 长期改进
7. ⏳ 智能检测ICS文件的节次配置
8. ⏳ 提供节次配置建议
9. ⏳ 支持多套节次配置（不同学期）

## 相关文件

- `app/src/main/java/takagi/ru/saison/util/PeriodMatcher.kt` - 节次匹配逻辑
- `app/src/main/java/takagi/ru/saison/util/PeriodGenerator.kt` - 节次生成逻辑
- `app/src/main/java/takagi/ru/saison/domain/model/CourseSettings.kt` - 节次配置
- `app/src/main/java/takagi/ru/saison/ui/screens/course/CourseViewModel.kt` - 节次计算

## 总结

主要修复：
1. **放宽节次范围验证** - 允许导入超出当前配置的节次
2. **添加调试日志** - 帮助诊断匹配问题
3. **优先信任描述** - 描述中的节次信息优先于时间匹配

这些修复应该能解决大部分匹配问题。如果还有问题，请查看Logcat日志进行进一步诊断。
