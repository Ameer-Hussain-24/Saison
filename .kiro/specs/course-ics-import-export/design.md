# 课程表导入导出功能设计文档

## 概述

本功能实现ICS（iCalendar）格式的课程表导入和导出，使用户能够从其他课程表应用迁移数据或将课程表分享到其他日历应用。设计遵循现有的Repository模式和MVVM架构，确保与应用其他功能的一致性。

## 架构

### 整体架构图

```
┌─────────────────────────────────────────────────────────────┐
│                         UI Layer                             │
│  ┌──────────────────┐              ┌──────────────────┐    │
│  │ CourseViewModel  │              │ ImportPreviewVM  │    │
│  └────────┬─────────┘              └────────┬─────────┘    │
│           │                                  │               │
└───────────┼──────────────────────────────────┼──────────────┘
            │                                  │
┌───────────┼──────────────────────────────────┼──────────────┐
│           │        Domain Layer              │               │
│  ┌────────▼─────────┐              ┌────────▼─────────┐    │
│  │ IcsImportUseCase │              │ IcsExportUseCase │    │
│  └────────┬─────────┘              └────────┬─────────┘    │
│           │                                  │               │
└───────────┼──────────────────────────────────┼──────────────┘
            │                                  │
┌───────────┼──────────────────────────────────┼──────────────┐
│           │         Data Layer               │               │
│  ┌────────▼─────────┐              ┌────────▼─────────┐    │
│  │  IcsParser       │              │  IcsGenerator    │    │
│  └────────┬─────────┘              └────────┬─────────┘    │
│           │                                  │               │
│  ┌────────▼──────────────────────────────────▼─────────┐   │
│  │           CourseRepository                          │   │
│  └─────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

### 数据流

**导入流程:**
```
ICS文件 → IcsParser → ParsedCourse列表 → ImportPreviewVM → 
用户确认 → IcsImportUseCase → CourseRepository → 数据库
```

**导出流程:**
```
数据库 → CourseRepository → Course列表 → IcsExportUseCase → 
IcsGenerator → ICS文件 → 文件系统/分享
```

## 组件和接口

### 1. IcsParser (数据解析器)

负责解析ICS文件并转换为中间数据结构。

```kotlin
class IcsParser {
    /**
     * 解析ICS文件内容
     * @param icsContent ICS文件的文本内容
     * @return 解析后的课程列表
     * @throws IcsParseException 解析失败时抛出
     */
    fun parse(icsContent: String): List<ParsedCourse>
    
    /**
     * 从Uri读取并解析ICS文件
     */
    suspend fun parseFromUri(context: Context, uri: Uri): List<ParsedCourse>
    
    private fun parseVEvent(lines: List<String>): ParsedCourse?
    private fun parseRRule(rrule: String): RecurrenceInfo
    private fun extractPeriodInfo(description: String): Pair<Int?, Int?>?
    private fun parseDateTime(dtString: String, tzid: String?): LocalDateTime
}

data class ParsedCourse(
    val summary: String,
    val location: String?,
    val description: String?,
    val dtStart: LocalDateTime,
    val dtEnd: LocalDateTime,
    val rrule: RecurrenceInfo?,
    val alarmMinutes: Int?
)

data class RecurrenceInfo(
    val frequency: String,  // WEEKLY, DAILY, etc.
    val interval: Int,
    val until: LocalDate?,
    val byDay: List<String>?  // MO, TU, WE, etc.
)
```

### 2. IcsGenerator (ICS生成器)

负责将Course对象转换为ICS格式。

```kotlin
class IcsGenerator {
    /**
     * 生成ICS文件内容
     * @param courses 要导出的课程列表
     * @param semesterName 学期名称（用于PRODID）
     * @return ICS格式的文本内容
     */
    fun generate(courses: List<Course>, semesterName: String): String
    
    /**
     * 将ICS内容写入文件
     */
    suspend fun writeToFile(
        context: Context,
        courses: List<Course>,
        semesterName: String,
        fileName: String
    ): Uri
    
    private fun generateVCalendar(courses: List<Course>, semesterName: String): String
    private fun generateVTimezone(): String
    private fun generateVEvent(course: Course): List<String>
    private fun generateRRule(course: Course): String?
    private fun formatDateTime(dateTime: LocalDateTime): String
}
```

### 3. CourseConverter (课程转换器)

负责在ParsedCourse和Course之间转换。

```kotlin
object CourseConverter {
    /**
     * 将ParsedCourse转换为Course
     * @param parsed 解析后的课程数据
     * @param semesterId 目标学期ID
     * @param primaryColor 主题色（用于颜色分配）
     * @param existingCourses 已存在的课程（用于颜色分配）
     * @return Course对象
     */
    fun toCourse(
        parsed: ParsedCourse,
        semesterId: Long,
        primaryColor: Color,
        existingCourses: List<Course>
    ): Course
    
    /**
     * 分析多个ParsedCourse，识别自定义周次模式
     * 将相同课程的多个VEVENT合并为一个Course
     */
    fun groupAndConvert(
        parsedList: List<ParsedCourse>,
        semesterId: Long,
        primaryColor: Color
    ): List<Course>
    
    private fun detectWeekPattern(
        events: List<ParsedCourse>,
        semesterStartDate: LocalDate
    ): Pair<WeekPattern, List<Int>?>
    
    private fun calculateWeekNumber(
        date: LocalDate,
        semesterStartDate: LocalDate
    ): Int
}
```

### 4. IcsImportUseCase (导入用例)

封装导入业务逻辑。

```kotlin
class IcsImportUseCase @Inject constructor(
    private val icsParser: IcsParser,
    private val courseConverter: CourseConverter,
    private val courseRepository: CourseRepository,
    private val semesterRepository: SemesterRepository
) {
    /**
     * 从Uri导入课程
     * @param uri ICS文件的Uri
     * @param targetSemesterId 目标学期ID
     * @return 导入的课程数量
     */
    suspend fun importFromUri(
        context: Context,
        uri: Uri,
        targetSemesterId: Long
    ): Result<Int>
    
    /**
     * 预览导入（不保存到数据库）
     */
    suspend fun previewImport(
        context: Context,
        uri: Uri,
        targetSemesterId: Long
    ): Result<List<Course>>
    
    /**
     * 确认导入选中的课程
     */
    suspend fun confirmImport(
        courses: List<Course>
    ): Result<Int>
}
```

### 5. IcsExportUseCase (导出用例)

封装导出业务逻辑。

```kotlin
class IcsExportUseCase @Inject constructor(
    private val icsGenerator: IcsGenerator,
    private val courseRepository: CourseRepository,
    private val semesterRepository: SemesterRepository
) {
    /**
     * 导出学期的所有课程
     * @param semesterId 学期ID
     * @param fileName 文件名（可选）
     * @return 导出文件的Uri
     */
    suspend fun exportSemester(
        context: Context,
        semesterId: Long,
        fileName: String? = null
    ): Result<Uri>
    
    /**
     * 导出选中的课程
     */
    suspend fun exportCourses(
        context: Context,
        courses: List<Course>,
        fileName: String
    ): Result<Uri>
    
    /**
     * 生成ICS内容（用于分享）
     */
    suspend fun generateIcsContent(
        semesterId: Long
    ): Result<String>
}
```

### 6. ImportPreviewViewModel

管理导入预览界面的状态。

```kotlin
@HiltViewModel
class ImportPreviewViewModel @Inject constructor(
    private val icsImportUseCase: IcsImportUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<ImportPreviewState>(ImportPreviewState.Idle)
    val uiState: StateFlow<ImportPreviewState> = _uiState.asStateFlow()
    
    fun loadPreview(uri: Uri, semesterId: Long)
    fun toggleCourseSelection(courseIndex: Int)
    fun selectAll()
    fun deselectAll()
    fun confirmImport()
}

sealed class ImportPreviewState {
    object Idle : ImportPreviewState()
    object Loading : ImportPreviewState()
    data class Preview(
        val courses: List<Course>,
        val selectedIndices: Set<Int>,
        val duplicateWarnings: List<String>
    ) : ImportPreviewState()
    data class Success(val importedCount: Int) : ImportPreviewState()
    data class Error(val message: String) : ImportPreviewState()
}
```

### 7. UI组件

#### ImportPreviewScreen

```kotlin
@Composable
fun ImportPreviewScreen(
    viewModel: ImportPreviewViewModel,
    onNavigateBack: () -> Unit,
    onImportSuccess: () -> Unit
)

@Composable
private fun CoursePreviewList(
    courses: List<Course>,
    selectedIndices: Set<Int>,
    onToggleSelection: (Int) -> Unit
)

@Composable
private fun CoursePreviewItem(
    course: Course,
    isSelected: Boolean,
    onToggle: () -> Unit
)
```

#### 导出对话框

```kotlin
@Composable
fun ExportCoursesDialog(
    semesterId: Long,
    onDismiss: () -> Unit,
    onExportSuccess: (Uri) -> Unit
)
```

## 数据模型

### ICS文件结构映射

| ICS字段 | Course字段 | 转换逻辑 |
|---------|-----------|----------|
| SUMMARY | name | 直接映射 |
| LOCATION | location | 直接映射，去除尾部空格 |
| DESCRIPTION | periodStart/periodEnd | 正则提取"第X-Y节" |
| DTSTART | startTime, startDate | 提取时间和日期 |
| DTEND | endTime | 提取时间 |
| RRULE:FREQ | weekPattern | WEEKLY → 根据UNTIL判断 |
| RRULE:UNTIL | endDate | 直接映射 |
| RRULE:BYDAY | dayOfWeek | MO→MONDAY等 |
| VALARM:TRIGGER | notificationMinutes | PT20M → 20 |

### 自定义周次识别算法

```
1. 按课程名称和时间分组ParsedCourse
2. 对每组：
   a. 如果只有一个VEVENT且RRULE无限制 → WeekPattern.ALL
   b. 如果有多个VEVENT：
      - 计算每个VEVENT对应的周数范围
      - 合并所有周数到customWeeks列表
      - 设置weekPattern = WeekPattern.CUSTOM
   c. 如果RRULE包含特殊模式（如INTERVAL=2）：
      - 分析是否为单双周 → WeekPattern.ODD/EVEN
3. 返回合并后的Course对象
```

## 错误处理

### 异常类型

```kotlin
sealed class IcsException(message: String) : Exception(message) {
    class ParseError(message: String) : IcsException(message)
    class InvalidFormat(message: String) : IcsException(message)
    class IoError(message: String) : IcsException(message)
    class EmptyFile : IcsException("ICS文件为空或不包含课程数据")
}
```

### 错误处理策略

1. **解析错误**: 记录日志，显示用户友好的错误消息
2. **IO错误**: 重试机制（最多3次），失败后提示用户
3. **格式错误**: 跳过无效的VEVENT，继续处理其他事件
4. **空文件**: 直接提示用户，不执行导入

### 用户反馈

```kotlin
sealed class ImportResult {
    data class Success(val count: Int) : ImportResult()
    data class PartialSuccess(
        val successCount: Int,
        val failedCount: Int,
        val errors: List<String>
    ) : ImportResult()
    data class Failure(val error: String) : ImportResult()
}
```

## 测试策略

### 单元测试

1. **IcsParser测试**
   - 测试标准ICS格式解析
   - 测试各种RRULE模式
   - 测试时区处理
   - 测试异常情况

2. **IcsGenerator测试**
   - 测试生成的ICS格式正确性
   - 测试各种WeekPattern的RRULE生成
   - 测试自定义周次的多VEVENT生成

3. **CourseConverter测试**
   - 测试ParsedCourse到Course的转换
   - 测试自定义周次识别算法
   - 测试颜色分配逻辑

### 集成测试

1. **完整导入流程测试**
   - 使用真实ICS文件测试
   - 验证数据库中的数据正确性

2. **导入导出往返测试**
   - 导出课程 → 导入 → 验证数据一致性

### UI测试

1. 测试文件选择流程
2. 测试预览界面交互
3. 测试成功/失败提示

## 性能优化

### 解析优化

1. **流式解析**: 逐行读取ICS文件，避免一次性加载大文件
2. **并行处理**: 使用协程并行解析多个VEVENT
3. **缓存**: 缓存时区信息，避免重复解析

```kotlin
private val timezoneCache = mutableMapOf<String, ZoneId>()

private fun getZoneId(tzid: String): ZoneId {
    return timezoneCache.getOrPut(tzid) {
        ZoneId.of(tzid)
    }
}
```

### 数据库优化

1. **批量插入**: 使用`insertAll`而非多次`insert`
2. **事务处理**: 将导入操作包装在事务中

```kotlin
suspend fun importCourses(courses: List<Course>) {
    courseDao.withTransaction {
        courseDao.insertAll(courses.map { it.toEntity() })
    }
}
```

### UI优化

1. **分页加载**: 预览界面使用LazyColumn
2. **防抖**: 搜索和过滤操作使用防抖
3. **后台线程**: 所有IO操作在IO调度器执行

## 依赖项

### 新增依赖

```kotlin
// ICS解析库（可选，也可以手动解析）
implementation("org.mnode.ical4j:ical4j:3.2.14")

// 或者使用轻量级的手动解析（推荐）
// 无需额外依赖，使用Kotlin标准库
```

### 现有依赖

- Hilt (依赖注入)
- Room (数据库)
- Kotlin Coroutines (异步处理)
- Compose (UI)

## 文件存储

### 导出文件位置

```kotlin
// 使用Android Storage Access Framework
val documentsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
val exportDir = File(documentsDir, "course_exports")
if (!exportDir.exists()) {
    exportDir.mkdirs()
}

val fileName = "courses_${semesterName}_${timestamp}.ics"
val file = File(exportDir, fileName)
```

### 文件命名规范

```
courses_[学期名称]_[时间戳].ics
例如: courses_2025春季学期_20251103.ics
```

## 国际化

### 字符串资源

```xml
<!-- strings.xml -->
<string name="import_courses">导入课程表</string>
<string name="export_courses">导出课程表</string>
<string name="import_preview_title">导入预览</string>
<string name="import_success">成功导入 %d 门课程</string>
<string name="import_error">导入失败: %s</string>
<string name="export_success">课程表已导出</string>
<string name="export_error">导出失败: %s</string>
<string name="select_all">全选</string>
<string name="deselect_all">取消全选</string>
<string name="duplicate_warning">检测到重复课程</string>
<string name="invalid_ics_format">ICS文件格式无效</string>
<string name="empty_ics_file">文件不包含课程数据</string>
```

## 安全性考虑

1. **文件验证**: 验证文件大小（限制10MB）和格式
2. **输入清理**: 清理课程名称和地点中的特殊字符
3. **权限检查**: 检查文件读写权限
4. **异常捕获**: 捕获所有可能的异常，避免应用崩溃

```kotlin
private fun sanitizeInput(input: String): String {
    return input
        .trim()
        .replace(Regex("[\\r\\n]+"), " ")
        .take(200) // 限制长度
}
```

## 扩展性

### 未来可能的扩展

1. **支持更多格式**: CSV, JSON等
2. **云端同步**: 支持从云端导入课程表
3. **智能识别**: 使用ML识别课程类型
4. **批量编辑**: 导入后批量修改课程属性
5. **模板系统**: 保存和应用课程表模板

### 插件化设计

```kotlin
interface CourseImporter {
    suspend fun import(uri: Uri): List<Course>
    fun supportedFormats(): List<String>
}

class IcsImporter : CourseImporter { ... }
class CsvImporter : CourseImporter { ... }

class ImporterRegistry {
    private val importers = mutableListOf<CourseImporter>()
    
    fun register(importer: CourseImporter)
    fun getImporter(format: String): CourseImporter?
}
```
