# UI集成指南

## 在CourseScreen中添加导入导出功能

### 1. 添加导入功能

在`CourseScreen.kt`的TopBar中添加导入菜单项：

```kotlin
// 在CourseScreen的状态变量中添加
var showImportDialog by remember { mutableStateOf(false) }
val importLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
) { uri: Uri? ->
    uri?.let {
        // 导航到ImportPreviewScreen
        navController.navigate("import_preview?uri=$it&semesterId=$currentSemesterId")
    }
}

// 在TopBar的actions中添加
IconButton(onClick = { importLauncher.launch("text/calendar") }) {
    Icon(Icons.Default.FileUpload, contentDescription = "导入课程表")
}
```

### 2. 添加导出功能

在`CourseScreen.kt`中添加导出对话框：

```kotlin
// 在CourseScreen的状态变量中添加
var showExportDialog by remember { mutableStateOf(false) }
val exportState by viewModel.exportState.collectAsState()

// 在TopBar的actions中添加
IconButton(onClick = { showExportDialog = true }) {
    Icon(Icons.Default.FileDownload, contentDescription = "导出课程表")
}

// 在Scaffold内容区域添加对话框
if (showExportDialog) {
    ExportCoursesDialog(
        onDismiss = { 
            showExportDialog = false
            viewModel.resetExportState()
        },
        onExport = { fileName ->
            viewModel.exportCurrentSemester(fileName)
        },
        isLoading = exportState is ExportState.Loading
    )
}

// 处理导出成功
when (val state = exportState) {
    is ExportState.Success -> {
        ExportSuccessDialog(
            uri = state.uri,
            onDismiss = {
                viewModel.resetExportState()
                showExportDialog = false
            }
        )
    }
    is ExportState.Error -> {
        // 显示错误Toast
        LaunchedEffect(state) {
            // Show error snackbar
        }
    }
    else -> {}
}
```

### 3. 添加导航路由

在`SaisonNavHost.kt`中添加ImportPreviewScreen的路由：

```kotlin
composable(
    route = "import_preview?uri={uri}&semesterId={semesterId}",
    arguments = listOf(
        navArgument("uri") { type = NavType.StringType },
        navArgument("semesterId") { type = NavType.LongType }
    )
) { backStackEntry ->
    val uriString = backStackEntry.arguments?.getString("uri") ?: return@composable
    val uri = Uri.parse(uriString)
    val semesterId = backStackEntry.arguments?.getLong("semesterId") ?: 1L
    val primaryColor = MaterialTheme.colorScheme.primary
    
    ImportPreviewScreen(
        uri = uri,
        semesterId = semesterId,
        primaryColor = primaryColor,
        onNavigateBack = { navController.popBackStack() },
        onImportSuccess = {
            navController.popBackStack()
            // 显示成功Toast
        }
    )
}
```

### 4. 所需导入

在CourseScreen.kt顶部添加：

```kotlin
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import takagi.ru.saison.ui.components.ExportCoursesDialog
import takagi.ru.saison.ui.components.ExportSuccessDialog
```

## 测试步骤

1. **测试导入功能**：
   - 点击导入按钮
   - 选择ICS文件
   - 查看预览界面
   - 选择要导入的课程
   - 确认导入
   - 验证课程是否正确添加到课程表

2. **测试导出功能**：
   - 点击导出按钮
   - 输入文件名
   - 确认导出
   - 验证文件是否生成
   - 测试分享功能

3. **测试错误处理**：
   - 尝试导入无效的ICS文件
   - 尝试导入空文件
   - 验证错误消息是否正确显示

## 注意事项

1. 确保在导入前获取当前学期ID
2. 导入时使用当前主题色进行颜色分配
3. 导出时确保有课程数据
4. 处理文件权限问题
5. 在后台线程执行IO操作
