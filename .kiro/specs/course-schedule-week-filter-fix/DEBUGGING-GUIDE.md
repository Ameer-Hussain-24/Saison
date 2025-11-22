# 单双周显示问题调试指南

## 问题描述

单双周课程没有正确分开显示，所有课程都显示在每一周。

## 可能的原因

### 1. 当前周数计算不正确

**检查方法：**
在CourseScreen中添加日志输出当前周数：
```kotlin
Log.d("CourseScreen", "Current week: $currentWeek, Page week: $pageWeek")
```

**预期结果：**
- 第1周：currentWeek = 1
- 第2周：currentWeek = 2
- 等等

### 2. 课程的weekPattern设置不正确

**检查方法：**
在过滤逻辑中添加日志：
```kotlin
val filteredCoursesByDay = coursesByDay.mapValues { (_, courses) ->
    courses.filter { course ->
        val isActive = viewModel.isCourseActiveInWeek(course, pageWeek)
        Log.d("CourseFilter", "Course: ${course.name}, Week: $pageWeek, Pattern: ${course.weekPattern}, Active: $isActive")
        isActive
    }
}
```

**预期结果：**
- 单周课程在双周应该返回false
- 双周课程在单周应该返回false

### 3. 学期开始日期设置不正确

**检查方法：**
查看CourseSettings中的semesterStartDate：
```kotlin
Log.d("CourseSettings", "Semester start date: ${courseSettings.semesterStartDate}")
```

**预期结果：**
- 应该是学期第一周的周一日期

## 快速修复步骤

### 步骤1：验证isCourseActiveInWeek方法

在CourseViewModel.kt中，确认方法存在且逻辑正确：

```kotlin
fun isCourseActiveInWeek(course: Course, week: Int): Boolean {
    return when (course.weekPattern) {
        WeekPattern.ALL -> true
        WeekPattern.ODD -> week % 2 == 1  // 单周：1, 3, 5, 7...
        WeekPattern.EVEN -> week % 2 == 0 // 双周：2, 4, 6, 8...
        WeekPattern.CUSTOM -> course.customWeeks?.contains(week) ?: false
        WeekPattern.A, WeekPattern.B -> true
    }
}
```

### 步骤2：验证过滤逻辑

在CourseScreen.kt的HorizontalPager中，确认过滤代码存在：

```kotlin
HorizontalPager(state = pagerState) { page ->
    val pageWeek = (page - centerPage) + currentWeek
    
    val filteredCoursesByDay = coursesByDay.mapValues { (_, courses) ->
        courses.filter { course ->
            viewModel.isCourseActiveInWeek(course, pageWeek)
        }
    }.filterValues { it.isNotEmpty() }
    
    // 显示filteredCoursesByDay
}
```

### 步骤3：检查课程数据

确认课程的weekPattern字段设置正确：

1. 打开数据库查看器（如果有）
2. 检查Course表中的weekPattern字段
3. 确认：
   - 单周课程：weekPattern = "ODD"
   - 双周课程：weekPattern = "EVEN"
   - 全部周课程：weekPattern = "ALL"

### 步骤4：重新设置学期开始日期

1. 打开课程设置
2. 点击"学期设置"
3. 设置正确的学期开始日期（第一周的周一）
4. 保存设置

### 步骤5：重新创建测试课程

1. 删除现有的测试课程
2. 创建新的单周课程：
   - 名称：单周测试
   - 周模式：选择"单周"
3. 创建新的双周课程：
   - 名称：双周测试
   - 周模式：选择"双周"
4. 切换周数验证

## 临时调试代码

在CourseScreen.kt中添加以下调试代码：

```kotlin
HorizontalPager(state = pagerState) { page ->
    val pageWeek = (page - centerPage) + currentWeek
    
    // 调试输出
    android.util.Log.d("CourseDebug", "=== Page $page ===")
    android.util.Log.d("CourseDebug", "Center page: $centerPage")
    android.util.Log.d("CourseDebug", "Current week: $currentWeek")
    android.util.Log.d("CourseDebug", "Page week: $pageWeek")
    android.util.Log.d("CourseDebug", "Total courses before filter: ${coursesByDay.values.flatten().size}")
    
    val filteredCoursesByDay = coursesByDay.mapValues { (day, courses) ->
        courses.filter { course ->
            val isActive = viewModel.isCourseActiveInWeek(course, pageWeek)
            android.util.Log.d("CourseDebug", "Course: ${course.name}, Pattern: ${course.weekPattern}, Week: $pageWeek, Active: $isActive")
            isActive
        }
    }.filterValues { it.isNotEmpty() }
    
    android.util.Log.d("CourseDebug", "Total courses after filter: ${filteredCoursesByDay.values.flatten().size}")
    
    // 显示课程表
    if (filteredCoursesByDay.isEmpty()) {
        EmptyCourseList()
    } else {
        ContinuousTimelineView(...)
    }
}
```

## 验证步骤

1. **添加调试代码**
2. **运行应用**
3. **查看Logcat输出**
4. **切换周数**
5. **观察日志变化**

## 预期日志输出

### 第1周（单周）
```
CourseDebug: === Page 18 ===
CourseDebug: Current week: 1
CourseDebug: Page week: 1
CourseDebug: Course: 单周测试, Pattern: ODD, Week: 1, Active: true
CourseDebug: Course: 双周测试, Pattern: EVEN, Week: 1, Active: false
```

### 第2周（双周）
```
CourseDebug: === Page 19 ===
CourseDebug: Current week: 2
CourseDebug: Page week: 2
CourseDebug: Course: 单周测试, Pattern: ODD, Week: 2, Active: false
CourseDebug: Course: 双周测试, Pattern: EVEN, Week: 2, Active: true
```

## 常见问题

### Q: 所有课程都显示在每一周
**A:** 检查课程的weekPattern是否都是"ALL"

### Q: 周数显示不正确
**A:** 重新设置学期开始日期

### Q: 过滤逻辑没有执行
**A:** 确认CourseScreen中的过滤代码存在且正确

### Q: isCourseActiveInWeek方法不存在
**A:** 重新添加该方法到CourseViewModel

## 联系支持

如果以上步骤都无法解决问题，请提供：
1. Logcat日志输出
2. 课程数据截图
3. 当前周数显示
4. 学期开始日期设置

这将帮助我们更快地定位问题。
