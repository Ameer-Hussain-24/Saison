# 剩余工作实施指南

## 概述

核心模块（任务和设置）已完全国际化。所有模块的字符串资源已创建完毕，剩余工作是替换其他模块代码中的硬编码文本。

## 已完成 ✅

1. **字符串资源** - 100% 完成
   - 所有模块的字符串资源已创建
   - 4种语言完整翻译（英语、中文、日语、越南语）
   - 135+ 个字符串资源，540+ 个翻译条目

2. **核心模块代码** - 100% 完成
   - TaskListScreen.kt ✅
   - SettingsScreen.kt ✅
   - 已验证编译无误

## 待完成工作

### 1. PomodoroScreen.kt

**需要替换的文本：**

```kotlin
// 添加导入
import androidx.compose.ui.res.stringResource
import takagi.ru.saison.R

// 替换标题
title = { Text("番茄钟") }
// 改为：
title = { Text(stringResource(R.string.pomodoro_title)) }

// 替换设置按钮
contentDescription = "设置"
// 改为：
contentDescription = stringResource(R.string.cd_settings)

// 替换番茄钟计数
text = "$completedPomodoros 个番茄钟"
// 改为：
text = stringResource(R.string.pomodoro_count_format, completedPomodoros)

// 替换按钮文本
Text("开始专注") -> Text(stringResource(R.string.pomodoro_action_start))
Text("暂停") -> Text(stringResource(R.string.pomodoro_action_pause))
Text("继续") -> Text(stringResource(R.string.pomodoro_action_resume))

// 替换统计标题
Text("今日统计") -> Text(stringResource(R.string.pomodoro_stats_today))
label = "完成" -> label = stringResource(R.string.pomodoro_stats_completed)
label = "专注时长" -> label = stringResource(R.string.pomodoro_stats_duration)
label = "中断" -> label = stringResource(R.string.pomodoro_stats_interruptions)

// 替换对话框
title = { Text("番茄钟设置") }
// 改为：
title = { Text(stringResource(R.string.pomodoro_settings_dialog_title)) }

Text("工作时长: $tempWorkDuration 分钟")
// 改为：
Text(stringResource(R.string.pomodoro_work_duration_format, tempWorkDuration))

Text("确定") -> Text(stringResource(R.string.common_action_confirm))
Text("取消") -> Text(stringResource(R.string.action_cancel))
```

### 2. MetronomeScreen.kt

**需要替换的文本：**

```kotlin
// 添加导入
import androidx.compose.ui.res.stringResource
import takagi.ru.saison.R

// 替换标题
title = { Text("节拍器") }
// 改为：
title = { Text(stringResource(R.string.metronome_title)) }

// 替换按钮
contentDescription = "预设" -> stringResource(R.string.metronome_presets)
contentDescription = "保存预设" -> stringResource(R.string.metronome_save_preset)
contentDescription = "设置" -> stringResource(R.string.cd_settings)

// 替换控制按钮
contentDescription = "减少 1" -> stringResource(R.string.metronome_decrease_1)
contentDescription = "减少 5" -> stringResource(R.string.metronome_decrease_5)
contentDescription = "增加 1" -> stringResource(R.string.metronome_increase_1)
contentDescription = "增加 5" -> stringResource(R.string.metronome_increase_5)
```

### 3. CourseScreen.kt

**需要替换的文本：**

```kotlin
// 添加导入
import androidx.compose.ui.res.stringResource
import takagi.ru.saison.R

// 替换标题
text = "课程表" -> stringResource(R.string.course_title)
text = "第 $currentWeek 周" -> stringResource(R.string.course_week_format, currentWeek)

// 替换按钮
text = { Text("添加课程") }
// 改为：
text = { Text(stringResource(R.string.course_action_add)) }

// 替换周模式
"全部" -> stringResource(R.string.course_pattern_all)
"A周" -> stringResource(R.string.course_pattern_a)
"B周" -> stringResource(R.string.course_pattern_b)
"单周" -> stringResource(R.string.course_pattern_odd)
"双周" -> stringResource(R.string.course_pattern_even)

// 替换课程数量
text = "${courses.size} 节课"
// 改为：
text = stringResource(R.string.course_count_format, courses.size)

// 替换空状态
text = "还没有课程" -> stringResource(R.string.course_empty_no_courses)
text = "点击右下角按钮添加课程" -> stringResource(R.string.course_empty_hint)

// 替换星期名称（getDayName 函数）
return when (day) {
    DayOfWeek.MONDAY -> stringResource(R.string.day_monday)
    DayOfWeek.TUESDAY -> stringResource(R.string.day_tuesday)
    // ... 其他星期
}
```

### 4. CourseCard.kt

**需要替换的文本：**

```kotlin
// 添加导入
import androidx.compose.ui.res.stringResource
import takagi.ru.saison.R

// 替换 getWeekPatternLabel 函数
private fun getWeekPatternLabel(pattern: WeekPattern): String {
    // 注意：这个函数不是 Composable，需要改为接收 Context
    // 或者在调用处使用 stringResource
}

// 在 Composable 中使用：
text = when (course.weekPattern) {
    WeekPattern.ALL -> stringResource(R.string.course_pattern_all)
    WeekPattern.A -> stringResource(R.string.course_pattern_a)
    WeekPattern.B -> stringResource(R.string.course_pattern_b)
    WeekPattern.ODD -> stringResource(R.string.course_pattern_odd)
    WeekPattern.EVEN -> stringResource(R.string.course_pattern_even)
}
```

### 5. CalendarScreen.kt

**需要替换的文本：**

```kotlin
// 添加导入
import androidx.compose.ui.res.stringResource
import takagi.ru.saison.R

// 替换 contentDescription
contentDescription = "上一个" -> stringResource(R.string.cd_previous)
contentDescription = "下一个" -> stringResource(R.string.cd_next)
contentDescription = "今天" -> stringResource(R.string.cd_today)

// 替换视图模式
text = when (mode) {
    CalendarViewMode.MONTH -> stringResource(R.string.calendar_view_month)
    CalendarViewMode.WEEK -> stringResource(R.string.calendar_view_week)
    CalendarViewMode.DAY -> stringResource(R.string.calendar_view_day)
    CalendarViewMode.AGENDA -> stringResource(R.string.calendar_view_agenda)
}
```

### 6. TaskDetailScreen.kt

**需要替换的文本：**

```kotlin
// 添加导入
import androidx.compose.ui.res.stringResource
import takagi.ru.saison.R

// 替换标题
title = { Text("任务详情") }
// 改为：
title = { Text(stringResource(R.string.task_detail_title)) }

// 替换按钮
contentDescription = "返回" -> stringResource(R.string.cd_back)
contentDescription = "删除" -> stringResource(R.string.cd_delete)

// 替换字段标签
label = { Text("标题") } -> label = { Text(stringResource(R.string.task_field_title)) }
label = { Text("描述") } -> label = { Text(stringResource(R.string.task_field_description)) }
text = "优先级" -> stringResource(R.string.task_field_priority)
text = "截止日期" -> stringResource(R.string.task_field_due_date)
text = "位置" -> stringResource(R.string.task_field_location)
text = "子任务" -> stringResource(R.string.task_field_subtasks)

// 替换状态文本
text = "未设置" -> stringResource(R.string.task_due_date_not_set)
text = "任务不存在" -> stringResource(R.string.task_error_not_found)
```

### 7. TaskCard.kt

**需要替换的文本：**

```kotlin
// 添加导入
import androidx.compose.ui.res.stringResource
import takagi.ru.saison.R

// 替换 contentDescription
contentDescription = "已完成" -> stringResource(R.string.cd_task_completed)
contentDescription = "未完成" -> stringResource(R.string.cd_task_incomplete)

// 替换 formatDueDate 函数中的文本
// 注意：这个函数不是 Composable，需要改为接收 Context
private fun formatDueDate(dueDate: LocalDateTime, context: Context): String {
    val now = LocalDateTime.now()
    val today = now.toLocalDate()
    val dueDay = dueDate.toLocalDate()
    
    return when {
        dueDay == today -> context.getString(
            R.string.time_today_at,
            dueDate.format(DateTimeFormatter.ofPattern("HH:mm"))
        )
        dueDay == today.plusDays(1) -> context.getString(
            R.string.time_tomorrow_at,
            dueDate.format(DateTimeFormatter.ofPattern("HH:mm"))
        )
        dueDay == today.minusDays(1) -> context.getString(
            R.string.time_yesterday_at,
            dueDate.format(DateTimeFormatter.ofPattern("HH:mm"))
        )
        // ... 其他情况
    }
}

// 在 Composable 中调用时传入 context
val context = LocalContext.current
text = formatDueDate(task.dueDate, context)
```

## 实施步骤

### 对于每个文件：

1. **添加导入**
   ```kotlin
   import androidx.compose.ui.res.stringResource
   import takagi.ru.saison.R
   ```

2. **替换硬编码文本**
   - 在 Composable 函数中使用 `stringResource(R.string.xxx)`
   - 对于非 Composable 函数，传入 Context 并使用 `context.getString(R.string.xxx)`

3. **处理参数化字符串**
   ```kotlin
   // 单参数
   stringResource(R.string.time_minutes_ago, 5)
   
   // 多参数
   stringResource(R.string.settings_pomodoro_duration_subtitle, work, shortBreak, longBreak)
   ```

4. **验证编译**
   ```bash
   ./gradlew assembleDebug
   ```

5. **测试**
   - 在不同语言下测试
   - 验证文本显示正确

## 验证清单

完成每个文件后，检查：

- [ ] 所有硬编码文本已替换
- [ ] 导入已添加
- [ ] 编译无错误
- [ ] 在所有语言下测试通过
- [ ] UI 布局正常
- [ ] ContentDescription 正确

## 预计时间

- PomodoroScreen.kt: 20-30 分钟
- MetronomeScreen.kt: 15-20 分钟
- CourseScreen.kt: 20-30 分钟
- CourseCard.kt: 10-15 分钟
- CalendarScreen.kt: 15-20 分钟
- TaskDetailScreen.kt: 15-20 分钟
- TaskCard.kt: 15-20 分钟

**总计：** 约 2-3 小时

## 注意事项

1. **非 Composable 函数**
   - 不能直接使用 `stringResource()`
   - 需要传入 `Context` 参数
   - 使用 `context.getString()`

2. **LocalContext**
   ```kotlin
   val context = LocalContext.current
   ```

3. **参数顺序**
   - 确保参数化字符串的参数顺序正确
   - 使用 `%1$d`, `%2$s` 等明确指定位置

4. **测试**
   - 每完成一个文件就测试一次
   - 避免积累太多未测试的更改

## 完成后

1. 运行完整测试
2. 在所有语言下验证
3. 更新完成总结文档
4. 标记所有任务为完成

## 参考

- 已完成的示例：TaskListScreen.kt, SettingsScreen.kt
- 字符串资源：app/src/main/res/values/strings.xml
- 设计文档：.kiro/specs/i18n-hardcoded-text/design.md
