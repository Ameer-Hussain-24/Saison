# 单双周显示问题 - 快速修复

## 问题确认

单双周课程没有正确分开显示。代码逻辑是正确的，问题可能出在数据或配置上。

## 最可能的原因

### 1. 课程的weekPattern设置为"ALL"

所有课程可能都被设置为"全部周"模式，导致每周都显示。

**解决方法：**
1. 打开应用
2. 点击要修改的课程
3. 在编辑界面中，确认"周模式"设置：
   - 单周课程：选择"单周"
   - 双周课程：选择"双周"
4. 保存课程

### 2. 学期开始日期设置不正确

如果学期开始日期设置错误，当前周数计算就会不准确。

**解决方法：**
1. 打开课程设置（右上角设置图标）
2. 点击"学期设置"（日历图标）
3. 设置正确的学期开始日期（第一周的周一）
4. 保存设置

## 测试步骤

### 步骤1：创建测试课程

1. **创建单周测试课程**
   - 点击"添加课程"
   - 名称：单周测试
   - 选择星期：周一
   - 选择时间：08:00-09:40
   - **周模式：选择"单周"** ← 重要！
   - 保存

2. **创建双周测试课程**
   - 点击"添加课程"
   - 名称：双周测试
   - 选择星期：周一
   - 选择时间：10:00-11:40
   - **周模式：选择"双周"** ← 重要！
   - 保存

### 步骤2：验证周数显示

1. 查看顶部显示的当前周数（例如："第 1 / 18 周"）
2. 确认周数是否正确

### 步骤3：测试过滤

1. **如果当前是第1周（单周）：**
   - 应该只看到"单周测试"课程
   - 不应该看到"双周测试"课程

2. **如果当前是第2周（双周）：**
   - 应该只看到"双周测试"课程
   - 不应该看到"单周测试"课程

3. **左右滑动切换周数：**
   - 滑动到第1周：只显示单周课程
   - 滑动到第2周：只显示双周课程
   - 滑动到第3周：只显示单周课程
   - 滑动到第4周：只显示双周课程

## 如果还是不工作

### 检查代码是否正确

确认以下文件包含正确的代码：

**CourseViewModel.kt** - 应该包含：
```kotlin
fun isCourseActiveInWeek(course: Course, week: Int): Boolean {
    return when (course.weekPattern) {
        WeekPattern.ALL -> true
        WeekPattern.ODD -> week % 2 == 1
        WeekPattern.EVEN -> week % 2 == 0
        WeekPattern.CUSTOM -> course.customWeeks?.contains(week) ?: false
        WeekPattern.A, WeekPattern.B -> true
    }
}
```

**CourseScreen.kt** - HorizontalPager中应该包含：
```kotlin
HorizontalPager(state = pagerState) { page ->
    val pageWeek = (page - centerPage) + currentWeek
    
    val filteredCoursesByDay = coursesByDay.mapValues { (_, courses) ->
        courses.filter { course ->
            viewModel.isCourseActiveInWeek(course, pageWeek)
        }
    }.filterValues { it.isNotEmpty() }
    
    if (filteredCoursesByDay.isEmpty()) {
        EmptyCourseList()
    } else {
        ContinuousTimelineView(
            coursesByDay = filteredCoursesByDay,
            ...
        )
    }
}
```

### 重新编译应用

1. 清理项目：`./gradlew clean`
2. 重新构建：`./gradlew assembleDebug`
3. 重新安装应用
4. 测试功能

## 预期行为

### 正确的行为

- **第1周（单周）：** 只显示weekPattern=ODD的课程
- **第2周（双周）：** 只显示weekPattern=EVEN的课程
- **第3周（单周）：** 只显示weekPattern=ODD的课程
- **第4周（双周）：** 只显示weekPattern=EVEN的课程
- **全部周课程：** 在所有周都显示

### 错误的行为

- 所有课程在每一周都显示
- 单双周课程没有区别

## 需要帮助？

如果按照以上步骤操作后问题仍然存在，请提供以下信息：

1. 当前显示的周数
2. 课程的周模式设置（截图）
3. 学期开始日期设置
4. 是否重新编译了应用

这将帮助我们更快地解决问题。
