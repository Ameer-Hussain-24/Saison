# 设计文档 - 日程活动时长功能

## 概述

为日程（Routine）功能添加"活动时长"字段，允许用户设置每次活动的预期持续时间。这个功能将帮助用户更好地规划时间，例如"每周一跑步30分钟"、"每天阅读1小时"等。活动时长是可选字段，用户可以选择设置或不设置。

## 架构

### 数据层修改

需要在 `RoutineTask` 数据模型中添加 `durationMinutes` 字段来存储活动时长（以分钟为单位）。

### UI层修改

需要修改以下组件：
1. `CreateRoutineSheet` - 添加时长选择器
2. `RoutineCard` - 显示时长信息
3. `RoutineDetailScreen` - 显示和编辑时长

## 组件和接口

### 1. 数据模型修改

#### RoutineTask 扩展
```kotlin
data class RoutineTask(
    val id: Long = 0,
    val title: String,
    val description: String? = null,
    val icon: String? = null,
    val cycleType: CycleType,
    val cycleConfig: CycleConfig,
    val durationMinutes: Int? = null,  // 新增：活动时长（分钟）
    val isActive: Boolean = true,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)
```

**字段说明**:
- `durationMinutes`: 可选字段，存储活动预期持续时间（分钟）
- 值为 `null` 表示未设置时长
- 值为 `0` 也视为未设置
- 最小值：1分钟
- 最大值：1440分钟（24小时）

### 2. 数据库迁移

需要添加数据库迁移脚本，为 `routine_tasks` 表添加 `duration_minutes` 列：

```sql
ALTER TABLE routine_tasks ADD COLUMN duration_minutes INTEGER DEFAULT NULL;
```

### 3. UI 组件

#### DurationPicker 组件

创建一个新的时长选择器组件：

```kotlin
@Composable
fun DurationPicker(
    durationMinutes: Int?,
    onDurationChange: (Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    // 显示小时和分钟选择器
    // 提供快捷选项：15分钟、30分钟、45分钟、1小时、1.5小时、2小时
}
```

**功能特性**:
- 分为小时和分钟两个独立选择器
- 小时范围：0-23
- 分钟范围：0、15、30、45（步进15分钟）
- 提供常用时长的快捷按钮
- 支持清除时长（设置为 null）

#### CreateRoutineSheet 修改

在创建日程的底部弹窗中添加时长选择字段：

```kotlin
@Composable
fun CreateRoutineSheet(
    onDismiss: () -> Unit,
    onSave: (RoutineTask) -> Unit,
    modifier: Modifier = Modifier
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var durationMinutes by remember { mutableStateOf<Int?>(null) }  // 新增
    var showDurationPicker by remember { mutableStateOf(false) }    // 新增
    
    // ... 其他字段
    
    // 时长选择字段
    OutlinedTextField(
        value = durationMinutes?.let { formatDuration(it) } ?: "",
        onValueChange = {},
        label = { Text("活动时长（可选）") },
        readOnly = true,
        trailingIcon = {
            IconButton(onClick = { showDurationPicker = true }) {
                Icon(Icons.Default.Schedule, contentDescription = "选择时长")
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
    
    // 时长选择器对话框
    if (showDurationPicker) {
        DurationPickerDialog(
            initialDuration = durationMinutes,
            onDurationSelected = { 
                durationMinutes = it
                showDurationPicker = false
            },
            onDismiss = { showDurationPicker = false }
        )
    }
}
```

#### RoutineCard 修改

在任务卡片上显示时长信息：

```kotlin
@Composable
fun RoutineCard(
    taskWithStats: RoutineTaskWithStats,
    onCheckIn: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column {
            // 标题和描述
            Text(text = taskWithStats.task.title)
            
            // 时长信息（如果设置了）
            taskWithStats.task.durationMinutes?.let { minutes ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = formatDuration(minutes),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // 打卡按钮等其他内容
        }
    }
}
```

#### RoutineDetailScreen 修改

在详情页面显示和编辑时长：

```kotlin
@Composable
fun RoutineDetailScreen(
    taskId: Long,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RoutineDetailViewModel = hiltViewModel()
) {
    // ... 其他内容
    
    // 时长信息行
    DetailRow(
        icon = Icons.Default.Schedule,
        label = "活动时长",
        value = task.durationMinutes?.let { formatDuration(it) } ?: "未设置",
        onClick = { showDurationPicker = true }
    )
}
```

### 4. 工具函数

#### 时长格式化函数

```kotlin
/**
 * 将分钟数格式化为易读的字符串
 * 例如：30 -> "30分钟"
 *      90 -> "1小时30分钟"
 *      60 -> "1小时"
 */
fun formatDuration(minutes: Int): String {
    if (minutes <= 0) return ""
    
    val hours = minutes / 60
    val mins = minutes % 60
    
    return when {
        hours == 0 -> "${mins}分钟"
        mins == 0 -> "${hours}小时"
        else -> "${hours}小时${mins}分钟"
    }
}

/**
 * 将小时和分钟转换为总分钟数
 */
fun toMinutes(hours: Int, minutes: Int): Int {
    return hours * 60 + minutes
}

/**
 * 将总分钟数拆分为小时和分钟
 */
fun fromMinutes(totalMinutes: Int): Pair<Int, Int> {
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return Pair(hours, minutes)
}
```

## 数据模型

### Duration 数据类（可选）

如果需要更复杂的时长表示，可以创建专门的数据类：

```kotlin
data class Duration(
    val hours: Int = 0,
    val minutes: Int = 0
) {
    val totalMinutes: Int
        get() = hours * 60 + minutes
    
    fun isZero(): Boolean = hours == 0 && minutes == 0
    
    companion object {
        fun fromMinutes(minutes: Int): Duration {
            return Duration(
                hours = minutes / 60,
                minutes = minutes % 60
            )
        }
    }
}
```

但为了简化，建议直接使用 `Int?` 类型存储总分钟数。

## 错误处理

### 输入验证

1. **时长范围验证**:
   - 最小值：1分钟
   - 最大值：1440分钟（24小时）
   - 超出范围时显示错误提示

2. **空值处理**:
   - `null` 值表示未设置时长
   - UI 上显示为"未设置"或不显示时长信息

3. **数据库兼容性**:
   - 旧数据的 `durationMinutes` 字段为 `null`
   - 确保向后兼容，不影响现有功能

## 测试策略

### 单元测试

1. **formatDuration 函数测试**:
   - 测试各种分钟数的格式化输出
   - 测试边界值（0、1、59、60、1440）

2. **toMinutes/fromMinutes 函数测试**:
   - 测试小时和分钟的转换
   - 测试边界值

### UI 测试

1. **CreateRoutineSheet 测试**:
   - 测试时长选择器的显示和隐藏
   - 测试时长值的保存

2. **RoutineCard 测试**:
   - 测试有时长和无时长的显示
   - 测试时长格式化的正确性

3. **DurationPicker 测试**:
   - 测试小时和分钟选择器
   - 测试快捷按钮功能
   - 测试清除功能

## 国际化

需要添加以下字符串资源：

```xml
<!-- 中文 (values-zh-rCN/strings.xml) -->
<string name="routine_duration_label">活动时长</string>
<string name="routine_duration_optional">活动时长（可选）</string>
<string name="routine_duration_not_set">未设置</string>
<string name="routine_duration_hours">小时</string>
<string name="routine_duration_minutes">分钟</string>
<string name="routine_duration_select">选择时长</string>
<string name="routine_duration_clear">清除时长</string>
<string name="routine_duration_quick_15min">15分钟</string>
<string name="routine_duration_quick_30min">30分钟</string>
<string name="routine_duration_quick_45min">45分钟</string>
<string name="routine_duration_quick_1hour">1小时</string>
<string name="routine_duration_quick_1_5hour">1.5小时</string>
<string name="routine_duration_quick_2hour">2小时</string>
<string name="routine_duration_format_minutes">%d分钟</string>
<string name="routine_duration_format_hours">%d小时</string>
<string name="routine_duration_format_hours_minutes">%d小时%d分钟</string>

<!-- 英文 (values/strings.xml) -->
<string name="routine_duration_label">Duration</string>
<string name="routine_duration_optional">Duration (Optional)</string>
<string name="routine_duration_not_set">Not set</string>
<string name="routine_duration_hours">Hours</string>
<string name="routine_duration_minutes">Minutes</string>
<string name="routine_duration_select">Select Duration</string>
<string name="routine_duration_clear">Clear Duration</string>
<string name="routine_duration_quick_15min">15 min</string>
<string name="routine_duration_quick_30min">30 min</string>
<string name="routine_duration_quick_45min">45 min</string>
<string name="routine_duration_quick_1hour">1 hour</string>
<string name="routine_duration_quick_1_5hour">1.5 hours</string>
<string name="routine_duration_quick_2hour">2 hours</string>
<string name="routine_duration_format_minutes">%d min</string>
<string name="routine_duration_format_hours">%d hr</string>
<string name="routine_duration_format_hours_minutes">%d hr %d min</string>
```

## 性能考虑

1. **数据库索引**: `duration_minutes` 字段不需要索引，因为不会用于查询条件
2. **UI 渲染**: 时长格式化函数应该是轻量级的，不会影响列表滚动性能
3. **内存占用**: 使用 `Int?` 而不是自定义对象，减少内存占用

## 依赖关系

- 依赖现有的 `RoutineTask` 数据模型
- 依赖 Room 数据库进行数据持久化
- 依赖 Material 3 组件库提供 UI 组件
- 不引入新的外部依赖
