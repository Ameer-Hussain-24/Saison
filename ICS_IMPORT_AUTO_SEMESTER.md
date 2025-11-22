# ICS 导入自动创建学期功能

## 问题
导入 ICS 文件时，如果目标学期不存在，会显示"学期不存在"错误。

## 解决方案
修改 `IcsImportUseCase.previewImport()` 方法，在学期不存在时自动创建默认学期。

## 实现细节

### 自动创建学期逻辑

```kotlin
// 获取或创建学期
var semester = semesterRepository.getSemesterByIdSync(targetSemesterId)
if (semester == null) {
    android.util.Log.w("IcsImportUseCase", "Semester not found: $targetSemesterId, creating default semester")
    
    // 从解析的课程中获取日期范围
    val dates = parsedCourses.mapNotNull { it.dtStart.toLocalDate() }
    val startDate = dates.minOrNull() ?: java.time.LocalDate.now()
    val endDate = dates.maxOrNull()?.plusMonths(4) ?: startDate.plusMonths(4)
    
    // 创建默认学期
    val newSemester = takagi.ru.saison.domain.model.Semester(
        name = "导入学期 ${java.time.LocalDate.now().year}",
        startDate = startDate,
        endDate = endDate,
        totalWeeks = 18,
        isDefault = true
    )
    
    val semesterId = semesterRepository.insertSemester(newSemester)
    semester = newSemester.copy(id = semesterId)
    android.util.Log.d("IcsImportUseCase", "Created new semester: ${semester.name} (ID: $semesterId)")
}
```

### 学期参数

- **名称**: "导入学期 {当前年份}"（例如："导入学期 2025"）
- **开始日期**: 从 ICS 文件中所有课程的最早日期
- **结束日期**: 从 ICS 文件中所有课程的最晚日期 + 4个月
- **总周数**: 18周（默认值）
- **设为默认**: true

### 工作流程

1. 用户选择 ICS 文件
2. 解析文件内容
3. 检查目标学期是否存在
4. **如果不存在**：
   - 从课程数据中提取日期范围
   - 创建新学期
   - 使用新学期 ID 继续导入
5. **如果存在**：
   - 直接使用现有学期
6. 转换课程数据
7. 显示预览界面

## 优点

1. **用户友好**: 无需手动创建学期即可导入课程
2. **智能日期**: 自动从课程数据中推断学期日期范围
3. **灵活性**: 用户后续可以在学期管理界面修改学期信息

## 修改的文件

- `app/src/main/java/takagi/ru/saison/domain/usecase/IcsImportUseCase.kt`

## 测试场景

1. **首次导入**（无学期）：
   - 自动创建"导入学期 2025"
   - 成功导入课程

2. **已有学期**：
   - 使用现有学期
   - 正常导入

3. **多次导入**：
   - 第一次创建学期
   - 后续导入使用同一学期

## 构建状态
✅ 构建成功
✅ 已安装到设备
✅ 准备测试
