# 课程周数选择器增强 - 实现进度

## 已完成任务

### ✅ 任务1：扩展数据模型和数据库
- ✅ 在 Course 数据模型中添加 `customWeeks: List<Int>?` 字段
- ✅ 在 WeekPattern 枚举中添加 `CUSTOM` 选项
- ✅ 在 CourseEntity 中添加 `customWeeks: String?` 字段（JSON格式）
- ✅ 创建数据库迁移 MIGRATION_7_8 添加 customWeeks 列
- ✅ 创建 WeekListConverter 工具类用于 List<Int> 与 JSON 的转换
- ✅ 更新 CourseMapper 以支持 customWeeks 字段的转换
- ✅ 在 DatabaseModule 中添加 MIGRATION_7_8
- ✅ 修复 AddCourseSheet 和 CourseCard 中的 when 表达式穷尽性问题

### ✅ 任务2：创建学期设置功能
- ✅ 学期设置功能已存在于 CourseSettings 模型中
- ✅ PreferencesManager 已支持学期设置的存储和读取
- ✅ CourseSettingsRepository 已实现

### ✅ 任务3：实现周数计算工具类
- ✅ 创建 WeekCalculator 类
- ✅ 实现 calculateCurrentWeek 方法
- ✅ 实现 calculateWeeksFromDateRange 方法
- ✅ 实现 getWeekDateRange 方法
- ✅ 实现 isDateInWeeks 方法
- ✅ 创建 WeekCalculatorTest 单元测试（包含18个测试用例）

## 剩余任务

### 任务4：实现周数选择器UI组件
- [ ] 4.1 创建 WeekNumberButton 组件
- [ ] 4.2 创建 WeekNumberGrid 组件
- [ ] 4.3 创建 QuickSelectionBar 组件
- [ ] 4.4 创建 WeekNumberSelectionContent 组件

### 任务5：实现日期模式UI组件
- [ ] 5.1 创建 DateRangeSelectionContent 组件
- [ ] 5.2 实现日期转周数逻辑

### 任务6：创建周数选择器对话框
- [ ] 6.1 创建 WeekSelectorDialog 组件
- [ ] 6.2 实现周数验证逻辑

### 任务7：扩展 CourseViewModel
- [ ] 7.1 添加当前周数计算
- [ ] 7.2 实现课程过滤逻辑
- [ ] 7.3 添加自定义周数课程创建方法

### 任务8：集成到现有组件
- [ ] 8.1 更新 AddCourseSheet
- [ ] 8.2 更新 CourseCard 显示
- [ ] 8.3 创建 CurrentWeekIndicator 组件
- [ ] 8.4 更新 CourseScreen

### 任务9：添加学期设置界面
- [ ] 创建 SemesterSettingsSheet 组件

### 任务10：国际化支持
- [ ] 添加所有相关字符串资源

### 任务11：编写单元测试
- [ ] 测试 isCourseActiveInWeek 方法
- [ ] 测试课程过滤逻辑
- [ ] 测试 WeekSelectionValidator
- [ ] 测试 WeekListConverter

### 任务12：UI测试和优化
- [ ] 测试周数选择器交互
- [ ] 测试快捷模式切换
- [ ] 测试过滤功能
- [ ] 性能优化

## 技术实现细节

### 数据模型变更
```kotlin
// Course.kt
data class Course(
    // ... 现有字段
    val customWeeks: List<Int>? = null,  // 新增：自定义周数列表
)

// WeekPattern.kt
enum class WeekPattern {
    ALL, A, B, ODD, EVEN,
    CUSTOM  // 新增：自定义周数模式
}
```

### 数据库迁移
```kotlin
val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE courses ADD COLUMN customWeeks TEXT DEFAULT NULL")
    }
}
```

### 周数计算工具
```kotlin
class WeekCalculator {
    fun calculateCurrentWeek(semesterStartDate: LocalDate, currentDate: LocalDate): Int
    fun calculateWeeksFromDateRange(...): List<Int>
    fun getWeekDateRange(...): Pair<LocalDate, LocalDate>
    fun isDateInWeeks(...): Boolean
}
```

## 下一步行动

继续实现任务4：周数选择器UI组件。这是用户界面的核心部分，包括：
1. 圆形周数按钮（WeekNumberButton）
2. 网格布局（WeekNumberGrid）
3. 快捷选择栏（QuickSelectionBar）
4. 整体内容组件（WeekNumberSelectionContent）

这些组件将提供直观的周数选择界面，符合 Material Design 3 规范。
