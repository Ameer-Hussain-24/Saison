# 课程导入节次匹配Bug修复

## 问题描述

导入ICS文件后，所有课程都显示在第1节，而不是根据时间正确匹配到对应的节次。

## 根本原因

虽然实现了完整的时间匹配逻辑（PeriodMatcher、PeriodGenerator等），但是**ImportPreviewScreen在调用loadPreview时没有传递节次配置参数**。

### 代码分析

**之前的代码（有问题）：**
```kotlin
@Composable
fun ImportPreviewScreen(
    uri: Uri,
    semesterId: Long,
    primaryColor: Color,
    onNavigateBack: () -> Unit,
    onImportSuccess: () -> Unit,
    viewModel: ImportPreviewViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(uri) {
        // ❌ 没有传递existingPeriods参数，使用默认空列表
        viewModel.loadPreview(uri, semesterId, primaryColor)
    }
}
```

**问题链：**
1. `ImportPreviewScreen`调用`loadPreview`时没有传递`existingPeriods`
2. `loadPreview`使用默认参数`existingPeriods = emptyList()`
3. `IcsImportUseCase.previewImport`收到空的节次列表
4. `CourseConverter.toCourse`调用`PeriodMatcher.matchPeriod`时传入空列表
5. `PeriodMatcher.matchPeriod`因为`existingPeriods.isEmpty()`直接返回`(null, null)`
6. 所有课程的`periodStart`和`periodEnd`都是null
7. 课程显示时默认显示在第1节

## 解决方案

### 修复代码

```kotlin
@Composable
fun ImportPreviewScreen(
    uri: Uri,
    semesterId: Long,
    primaryColor: Color,
    onNavigateBack: () -> Unit,
    onImportSuccess: () -> Unit,
    viewModel: ImportPreviewViewModel = hiltViewModel(),
    courseViewModel: CourseViewModel = hiltViewModel()  // ✅ 添加CourseViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val periods by courseViewModel.periods.collectAsState()  // ✅ 获取节次配置
    
    LaunchedEffect(uri, periods) {
        // ✅ 等待periods加载完成后再调用loadPreview
        if (periods.isNotEmpty()) {
            viewModel.loadPreview(
                uri = uri,
                semesterId = semesterId,
                primaryColor = primaryColor,
                strategy = TimeMatchingStrategy.UseExistingPeriods,  // ✅ 使用现有节次策略
                existingPeriods = periods  // ✅ 传递节次配置
            )
        }
    }
}
```

### 关键改动

1. **添加CourseViewModel依赖**
   - 通过`hiltViewModel()`获取CourseViewModel实例
   - CourseViewModel包含`periods`属性，基于CourseSettings动态计算

2. **获取节次配置**
   - 使用`courseViewModel.periods.collectAsState()`获取当前节次配置
   - periods是一个`StateFlow<List<CoursePeriod>>`

3. **传递节次配置**
   - 在`LaunchedEffect`中添加`periods`依赖
   - 等待`periods.isNotEmpty()`确保节次已加载
   - 将`periods`传递给`loadPreview`方法

4. **指定匹配策略**
   - 明确使用`TimeMatchingStrategy.UseExistingPeriods`
   - 这样PeriodMatcher会使用传入的节次配置进行匹配

## 工作流程

### 修复后的完整流程

```
1. ImportPreviewScreen加载
   ↓
2. CourseViewModel.periods计算节次配置
   （基于CourseSettings: 8节课，每节45分钟等）
   ↓
3. periods加载完成（非空）
   ↓
4. 调用loadPreview并传递periods
   ↓
5. IcsImportUseCase.previewImport收到periods
   ↓
6. CourseConverter.toCourse使用periods
   ↓
7. PeriodMatcher.matchPeriod进行智能匹配
   - 尝试从描述提取节次（"第7-8节"）
   - 或使用时间重叠算法匹配
   ↓
8. 返回正确的(periodStart, periodEnd)
   ↓
9. 课程显示在正确的节次位置
```

### 匹配示例

**示例1：从描述提取**
```
课程：综合英语
描述：第7-8节
时间：13:30-15:10
结果：periodStart=7, periodEnd=8
```

**示例2：时间重叠匹配**
```
课程：计算机...
描述：（无节次信息）
时间：08:00-08:45
节次配置：第1节 08:00-08:45
结果：periodStart=1, periodEnd=1（通过时间重叠匹配）
```

## 测试验证

### 验证步骤

1. **准备测试数据**
   - 确保CourseSettings已配置（8节课，08:00开始等）
   - 准备包含节次信息的ICS文件

2. **导入测试**
   - 导入ICS文件
   - 检查ImportPreviewScreen是否正确显示节次
   - 确认导入后课程显示在正确位置

3. **边界情况测试**
   - 测试无节次信息的课程（应使用时间匹配）
   - 测试跨节次课程（如第7-8节）
   - 测试自定义时间课程（无法匹配的情况）

### 预期结果

- ✅ 有节次描述的课程：精确匹配到对应节次
- ✅ 无节次描述的课程：通过时间重叠算法匹配
- ✅ 无法匹配的课程：标记为自定义时间（isCustomTime=true）
- ✅ 课程表显示：所有课程显示在正确的节次位置

## 相关文件

### 修改的文件
- `app/src/main/java/takagi/ru/saison/ui/screens/course/ImportPreviewScreen.kt`

### 相关实现
- `app/src/main/java/takagi/ru/saison/util/PeriodMatcher.kt` - 节次匹配逻辑
- `app/src/main/java/takagi/ru/saison/util/CourseConverter.kt` - 课程转换逻辑
- `app/src/main/java/takagi/ru/saison/domain/usecase/IcsImportUseCase.kt` - 导入用例
- `app/src/main/java/takagi/ru/saison/ui/screens/course/ImportPreviewViewModel.kt` - ViewModel
- `app/src/main/java/takagi/ru/saison/ui/screens/course/CourseViewModel.kt` - 节次计算

## 总结

这个bug的根本原因是**数据流断裂**：虽然实现了完整的匹配逻辑，但是ImportPreviewScreen没有将节次配置传递给匹配逻辑。

修复方法很简单：
1. 从CourseViewModel获取periods
2. 传递给loadPreview方法
3. 确保在periods加载完成后再调用

这个修复确保了时间匹配功能能够正常工作，课程能够正确显示在对应的节次位置。
