# 根本原因已找到并修复

## 问题根源

**CourseConverter.kt 中的导入逻辑问题**

当从ICS文件导入课程时：
- 如果一个课程只有一个VEVENT（单个事件），它会被直接转换
- 在转换过程中，`weekPattern` 被硬编码为 `WeekPattern.ALL`
- 这导致所有导入的课程都显示为"全周"课程
- 因此不管单周还是双周，所有课程都会显示

## 修复内容

### 修改文件
`app/src/main/java/takagi/ru/saison/util/CourseConverter.kt`

### 修改前
```kotlin
if (events.size == 1) {
    // 单个事件，直接转换
    val course = toCourse(
        parsed = events.first(),
        semesterId = semesterId,
        semesterStartDate = semesterStartDate,
        primaryColor = primaryColor,
        existingCourses = courses
    )
    courses.add(course)
}
```

### 修改后
```kotlin
if (events.size == 1) {
    // 单个事件，也需要检测周次模式
    val (weekPattern, customWeeks) = detectWeekPattern(events, semesterStartDate)
    
    val course = toCourse(
        parsed = events.first(),
        semesterId = semesterId,
        semesterStartDate = semesterStartDate,
        primaryColor = primaryColor,
        existingCourses = courses
    ).copy(
        weekPattern = weekPattern,
        customWeeks = customWeeks
    )
    courses.add(course)
}
```

## 修复说明

现在，即使ICS文件中每个课程只有一个VEVENT，系统也会：

1. **调用 `detectWeekPattern` 方法**
   - 分析课程的重复规则（RRULE）
   - 计算课程覆盖的所有周数

2. **自动识别周次模式**
   - 如果所有周数都是奇数（1, 3, 5, 7...）→ 设置为 `WeekPattern.ODD`（单周）
   - 如果所有周数都是偶数（2, 4, 6, 8...）→ 设置为 `WeekPattern.EVEN`（双周）
   - 如果是其他模式 → 设置为 `WeekPattern.CUSTOM`（自定义周）

3. **正确设置 weekPattern 字段**
   - 不再硬编码为 `WeekPattern.ALL`
   - 根据实际情况设置正确的周模式

## 如何验证修复

### 方法1: 重新导入课程（推荐）

1. **删除现有课程**
   - 打开课程表
   - 删除所有课程（或者清除应用数据）

2. **重新导入ICS文件**
   - 点击"导入"按钮
   - 选择您的ICS文件
   - 导入课程

3. **验证结果**
   - 查看第1周（奇数周）：应该只显示单周课程
   - 滑动到第2周（偶数周）：应该只显示双周课程
   - 点击课程查看详情，确认"周模式"字段正确

### 方法2: 手动修改现有课程

如果不想重新导入：

1. **逐个编辑课程**
   - 点击课程卡片
   - 找到"周模式"设置
   - 将应该在单周上的课程改为"单周"
   - 将应该在双周上的课程改为"双周"

2. **验证结果**
   - 查看不同周的课程表
   - 确认单双周显示正确

### 方法3: 查看Logcat日志

导入课程后，查看日志：

```
D/CourseViewModel: isCourseActiveInWeek: [课程名], pattern=[模式], week=[周数], result=[结果]
```

**预期日志示例：**
```
// 第1周（奇数周）
D/CourseViewModel: isCourseActiveInWeek: 高等数学, pattern=ODD, week=1, result=true
D/CourseViewModel: isCourseActiveInWeek: 大学物理, pattern=EVEN, week=1, result=false

// 第2周（偶数周）
D/CourseViewModel: isCourseActiveInWeek: 高等数学, pattern=ODD, week=2, result=false
D/CourseViewModel: isCourseActiveInWeek: 大学物理, pattern=EVEN, week=2, result=true
```

## ICS文件格式说明

### 正确的ICS格式（单双周分开）

如果ICS文件中单双周课程是分开的多个VEVENT：
```
BEGIN:VEVENT
SUMMARY:高等数学
DTSTART:20250901T080000
DTEND:20250901T094000
RRULE:FREQ=WEEKLY;UNTIL=20250915  // 第1周
END:VEVENT

BEGIN:VEVENT
SUMMARY:高等数学
DTSTART:20250915T080000
DTEND:20250915T094000
RRULE:FREQ=WEEKLY;UNTIL=20250929  // 第3周
END:VEVENT
```
→ 系统会识别为单周课程

### 常见的ICS格式（单个VEVENT）

如果ICS文件中每个课程只有一个VEVENT：
```
BEGIN:VEVENT
SUMMARY:高等数学
DTSTART:20250901T080000
DTEND:20250901T094000
RRULE:FREQ=WEEKLY;INTERVAL=2;UNTIL=20251231  // 每两周重复
END:VEVENT
```
→ 修复后，系统会分析RRULE，识别为单周或双周

## 注意事项

1. **需要重新导入**
   - 修复只对新导入的课程生效
   - 已经导入的课程仍然是 `WeekPattern.ALL`
   - 需要删除并重新导入，或手动修改

2. **RRULE 格式要求**
   - ICS文件必须包含正确的RRULE（重复规则）
   - 如果RRULE不正确，可能无法正确识别单双周

3. **备份数据**
   - 重新导入前建议备份现有课程数据
   - 或者先导出为ICS文件

## 总结

**问题：** 导入的课程都被设置为全周模式，导致单双周不分开显示

**原因：** CourseConverter 在处理单个VEVENT时，硬编码 weekPattern 为 ALL

**修复：** 让单个VEVENT也调用 detectWeekPattern 方法，自动识别周次模式

**结果：** 导入的课程会根据实际情况正确设置为单周、双周或自定义周模式
