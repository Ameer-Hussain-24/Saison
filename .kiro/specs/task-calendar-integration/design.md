# Design Document

## Overview

本设计文档描述如何实现任务和日历视图的数据互通，让日历能够显示任务数据，并改进视图命名的用户体验。

## Architecture

### 当前架构分析

当前系统已经有基础架构：
- `CalendarViewModel` 已经从 `TaskRepository` 获取任务数据
- `loadEvents()` 方法已经将任务转换为 `CalendarEvent`
- 但只显示有 `dueDate` 的任务

### 需要的改进

1. **扩展任务显示逻辑**
   - 当前：只显示有截止日期的任务
   - 改进：也显示已完成但没有截止日期的任务（显示在完成日期）

2. **视图命名改进**
   - 将 `AGENDA` 改为 `LIST`
   - 更新所有相关的UI文本和字符串资源

3. **任务卡片交互**
   - 添加点击导航到任务详情
   - 添加长按快速操作菜单

## Components and Interfaces

### 1. CalendarViewMode 枚举更新

**文件**: `app/src/main/java/takagi/ru/saison/domain/model/CalendarViewMode.kt`

```kotlin
enum class CalendarViewMode {
    MONTH,
    WEEK,
    DAY,
    LIST  // 从 AGENDA 改为 LIST
}
```

### 2. CalendarViewModel 更新

**文件**: `app/src/main/java/takagi/ru/saison/ui/screens/calendar/CalendarViewModel.kt`

**修改点**:
- 更新 `loadEvents()` 方法，添加已完成任务的显示逻辑
- 更新所有 `AGENDA` 引用为 `LIST`
- 添加任务操作方法（完成、删除等）

**新增方法**:
```kotlin
fun toggleTaskCompletion(taskId: Long)
fun deleteTask(taskId: Long)
fun navigateToTaskDetail(taskId: Long): 导航事件
```

### 3. CalendarEvent 数据模型

**当前结构** (无需修改):
```kotlin
data class CalendarEvent(
    val id: Long,
    val title: String,
    val description: String?,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val type: EventType,
    val color: Int,
    val isAllDay: Boolean,
    val location: String? = null,
    val relatedTaskId: Long? = null
)
```

### 4. UI组件更新

#### AgendaView 重命名为 ListView

**文件**: 
- `app/src/main/java/takagi/ru/saison/ui/screens/calendar/AgendaView.kt` → `ListView.kt`

**修改点**:
- 重命名文件和组件
- 添加任务卡片点击处理
- 添加任务卡片长按菜单
- 区分已完成和未完成任务的视觉样式

#### CalendarScreen 更新

**文件**: `app/src/main/java/takagi/ru/saison/ui/screens/calendar/CalendarScreen.kt`

**修改点**:
- 更新视图模式切换按钮文本
- 更新 `AGENDA` 为 `LIST`
- 添加任务详情导航处理

#### MonthView, WeekView, DayView 更新

**修改点**:
- 添加任务卡片点击处理
- 添加已完成任务的视觉标识
- 确保显示完成日期的任务

## Data Models

### Task 数据模型 (已存在)

```kotlin
data class Task(
    val id: Long,
    val title: String,
    val description: String?,
    val dueDate: LocalDateTime?,
    val completedAt: LocalDateTime?,  // 完成时间
    val isCompleted: Boolean,
    val priority: Priority,
    // ... 其他字段
)
```

### CalendarEvent 显示逻辑

**任务显示规则**:
1. 有截止日期的任务 → 显示在截止日期
2. 没有截止日期但已完成 → 显示在完成日期
3. 没有截止日期且未完成 → 不在日历中显示

## Error Handling

### 数据加载错误
- 任务数据加载失败时，显示空状态
- 单个任务解析错误时，跳过该任务继续加载其他任务

### 导航错误
- 任务不存在时，显示错误提示
- 任务已删除时，刷新日历视图

## Testing Strategy

### 单元测试
- `CalendarViewModel.loadEvents()` 的任务显示逻辑
- 已完成任务的日期计算
- 视图模式枚举的更新

### UI测试
- 日历中任务卡片的显示
- 任务卡片点击导航
- 视图模式切换
- 已完成任务的视觉区分

## Implementation Details

### 1. loadEvents() 方法改进

```kotlin
private fun loadEvents(
    startDateTime: LocalDateTime,
    endDateTime: LocalDateTime
): Flow<List<CalendarEvent>> {
    return taskRepository.getTasksByDateRange(startMillis, endMillis).map { tasks ->
        val events = mutableListOf<CalendarEvent>()
        
        tasks.forEach { task ->
            // 情况1: 有截止日期的任务
            if (task.dueDate != null) {
                events.add(createEventFromTask(task, task.dueDate))
            }
            // 情况2: 没有截止日期但已完成的任务
            else if (task.isCompleted && task.completedAt != null) {
                events.add(createEventFromTask(task, task.completedAt))
            }
        }
        
        // 添加节假日...
        
        events.sortedBy { it.startTime }
    }
}

private fun createEventFromTask(task: Task, displayDate: LocalDateTime): CalendarEvent {
    return CalendarEvent(
        id = task.id,
        title = task.title,
        description = task.description,
        startTime = displayDate.minusHours(1),
        endTime = displayDate,
        type = EventType.TASK,
        color = getPriorityColor(task.priority.value),
        isAllDay = false,
        location = task.location,
        relatedTaskId = task.id
    )
}
```

### 2. 字符串资源更新

**文件**: `app/src/main/res/values*/strings.xml`

需要更新的字符串:
- `calendar_view_agenda` → `calendar_view_list`
- "议程" → "列表"
- 所有语言版本都需要更新

### 3. 任务卡片交互

```kotlin
// 在各个视图中添加
TaskCard(
    task = task,
    onClick = { viewModel.navigateToTaskDetail(task.id) },
    onLongClick = { showTaskQuickActions(task) },
    isCompleted = task.isCompleted
)
```

## UI/UX Considerations

### 已完成任务的视觉区分
- 使用半透明度
- 添加完成图标
- 使用删除线样式

### 任务卡片设计
- 显示优先级颜色条
- 显示任务标题
- 显示时间（截止时间或完成时间）
- 显示完成状态图标

### 快速操作菜单
- 标记完成/未完成
- 编辑任务
- 删除任务
- 查看详情

## Migration Strategy

### 枚举值更新
由于 `CalendarViewMode.AGENDA` 改为 `LIST`，需要：
1. 更新所有引用 `AGENDA` 的代码
2. 更新用户偏好设置的存储值
3. 提供迁移逻辑（如果有持久化的视图模式设置）

### 向后兼容
- 如果用户之前保存了 `AGENDA` 模式，自动转换为 `LIST`
- 确保所有视图切换逻辑正常工作
