# ICS 导入权限修复 - 最终解决方案

## 问题描述
用户在尝试导入 ICS 文件时遇到 `SecurityException`，错误信息为：
```
Permission Denial: reading com.android.providers.media.MediaDocumentsProvider uri ... 
requires that you obtain access using ACTION_OPEN_DOCUMENT or related APIs
```

## 根本原因
Android 的 `MediaDocumentsProvider` **不支持持久化 URI 权限**。即使调用 `takePersistableUriPermission()` 也会失败。临时 URI 权限只在文件选择器回调的调用栈中有效，一旦导航到另一个界面，权限就会失效。

## 最终解决方案

### 1. 创建临时文件缓存
创建了 `TempFileCache.kt` 用于在导航过程中临时存储文件内容：

```kotlin
object TempFileCache {
    private var cachedContent: String? = null
    
    fun store(content: String) {
        cachedContent = content
    }
    
    fun retrieve(): String? {
        val content = cachedContent
        cachedContent = null // 读取后清除
        return content
    }
}
```

### 2. 在 CourseScreen 中立即读取文件
修改了 `CourseScreen.kt`，在文件选择后立即读取内容（在权限有效期内）：

```kotlin
val coroutineScope = rememberCoroutineScope()
val importLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.OpenDocument()
) { uri: Uri? ->
    uri?.let {
        // 立即读取文件内容（在权限有效期内）
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val content = context.contentResolver.openInputStream(it)?.use { stream ->
                    stream.bufferedReader().use { reader ->
                        reader.readText()
                    }
                }
                
                if (content != null) {
                    // 将内容存储到临时缓存
                    TempFileCache.store(content)
                    
                    // 切换回主线程进行导航
                    withContext(Dispatchers.Main) {
                        val semesterId = currentSemesterId ?: 1L
                        onNavigateToImportPreview(it, semesterId)
                    }
                }
            } catch (e: Exception) {
                Log.e("CourseScreen", "Failed to read file", e)
            }
        }
    }
}
```

### 3. 修改 IcsParser 优先使用缓存
更新了 `IcsParser.kt`，优先从缓存读取内容：

```kotlin
suspend fun parseFromUri(context: Context, uri: Uri): List<ParsedCourse> {
    // 首先尝试从临时缓存读取
    val cachedContent = TempFileCache.retrieve()
    if (cachedContent != null) {
        return parseContent(cachedContent)
    }
    
    // 如果缓存为空，尝试从URI读取（作为后备方案）
    // ...
}
```

## 工作流程

1. 用户点击导入按钮
2. 文件选择器打开
3. 用户选择 ICS 文件
4. **立即**在文件选择器回调中读取文件内容（权限有效）
5. 将内容存储到 `TempFileCache`
6. 导航到 `ImportPreviewScreen`
7. `IcsParser` 从缓存读取内容并解析
8. 显示预览界面

## 为什么这个方案有效

1. **在权限有效期内读取**：文件内容在文件选择器回调中立即读取，此时临时权限仍然有效
2. **避免权限问题**：导航后不再需要访问 URI，因为内容已经在内存中
3. **简单可靠**：不依赖持久化权限（MediaDocumentsProvider 不支持）
4. **内存安全**：读取后立即清除缓存，避免内存泄漏

## 测试步骤
1. 启动应用并导航到课程表界面
2. 点击导入按钮
3. 从文件选择器中选择 ICS 文件（例如 `日历-25下.ics`）
4. 应用应该成功读取文件并显示预览界面
5. 确认导入后，课程应该被正确添加到数据库

## 修改的文件
- `app/src/main/java/takagi/ru/saison/ui/screens/course/CourseScreen.kt`
- `app/src/main/java/takagi/ru/saison/data/ics/IcsParser.kt`
- `app/src/main/java/takagi/ru/saison/util/TempFileCache.kt` (新建)

## 构建状态
✅ 构建成功
✅ 已安装到设备
✅ 准备测试
