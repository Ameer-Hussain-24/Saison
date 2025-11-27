package takagi.ru.saison.data.remote.webdav

import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import takagi.ru.saison.domain.model.Task
import takagi.ru.saison.domain.model.TaskDto
import takagi.ru.saison.domain.model.toDomain
import takagi.ru.saison.domain.model.toDto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebDavClient @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val json: Json
) {
    companion object {
        private const val TAG = "WebDavClient"
    }
    
    suspend fun getETag(url: String, credentials: WebDavCredentials): String? {
        return withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url(url)
                    .head()
                    .addHeader("Authorization", credentials.toAuthHeader())
                    .build()
                
                val response = okHttpClient.newCall(request).execute()
                response.header("ETag")
            } catch (e: Exception) {
                null
            }
        }
    }
    
    suspend fun downloadTasks(url: String, credentials: WebDavCredentials): List<Task> {
        return withContext(Dispatchers.IO) {
            val request = Request.Builder()
                .url(url)
                .get()
                .addHeader("Authorization", credentials.toAuthHeader())
                .build()
            
            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                throw WebDavException("Download failed: ${response.code}")
            }
            
            val jsonString = response.body?.string() ?: throw WebDavException("Empty response")
            val taskDtos = json.decodeFromString<List<TaskDto>>(jsonString)
            taskDtos.map { it.toDomain() }
        }
    }
    
    suspend fun uploadTasks(url: String, credentials: WebDavCredentials, tasks: List<Task>) {
        withContext(Dispatchers.IO) {
            val taskDtos = tasks.map { it.toDto() }
            val jsonString = json.encodeToString(taskDtos)
            
            val request = Request.Builder()
                .url(url)
                .put(jsonString.toRequestBody("application/json".toMediaType()))
                .addHeader("Authorization", credentials.toAuthHeader())
                .addHeader("If-Match", "*")
                .build()
            
            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                throw WebDavException("Upload failed: ${response.code}")
            }
        }
    }
    
    suspend fun checkConnection(url: String, credentials: WebDavCredentials): Boolean {
        return try {
            withContext(Dispatchers.IO) {
                val request = Request.Builder()
                    .url(url)
                    .method("PROPFIND", "".toRequestBody())
                    .addHeader("Authorization", credentials.toAuthHeader())
                    .addHeader("Depth", "0")
                    .build()
                
                val response = okHttpClient.newCall(request).execute()
                response.isSuccessful
            }
        } catch (e: Exception) {
            false
        }
    }
    
    suspend fun getLastModified(url: String, credentials: WebDavCredentials): Long? {
        return withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url(url)
                    .head()
                    .addHeader("Authorization", credentials.toAuthHeader())
                    .build()
                
                val response = okHttpClient.newCall(request).execute()
                response.header("Last-Modified")?.let { dateString ->
                    // Parse HTTP date format
                    parseHttpDate(dateString)
                }
            } catch (e: Exception) {
                null
            }
        }
    }
    
    private fun parseHttpDate(dateString: String): Long {
        return try {
            java.text.SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", java.util.Locale.US)
                .parse(dateString)?.time ?: 0L
        } catch (e: Exception) {
            0L
        }
    }
    
    /**
     * 上传文件到 WebDAV 服务器
     */
    suspend fun uploadFile(url: String, credentials: WebDavCredentials, file: java.io.File): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                android.util.Log.d(TAG, "开始上传文件: ${file.name}, 大小: ${file.length()} bytes, URL: $url")
                
                if (!file.exists()) {
                    android.util.Log.e(TAG, "文件不存在: ${file.absolutePath}")
                    return@withContext Result.failure(
                        WebDavUploadException(
                            statusCode = null,
                            responseBody = null,
                            message = "文件不存在: ${file.absolutePath}"
                        )
                    )
                }
                
                val fileBytes = file.readBytes()
                android.util.Log.d(TAG, "文件读取完成: ${fileBytes.size} bytes")
                
                val request = Request.Builder()
                    .url(url)
                    .put(fileBytes.toRequestBody("application/zip".toMediaType()))
                    .addHeader("Authorization", credentials.toAuthHeader())
                    .addHeader("Content-Type", "application/zip")
                    .addHeader("Content-Length", fileBytes.size.toString())
                    .build()
                
                android.util.Log.d(TAG, "发送 PUT 请求到: $url")
                android.util.Log.d(TAG, "请求头: Content-Type=application/zip, Content-Length=${fileBytes.size}")
                
                val startTime = System.currentTimeMillis()
                val response = okHttpClient.newCall(request).execute()
                val duration = System.currentTimeMillis() - startTime
                
                android.util.Log.d(TAG, "收到响应: ${response.code}, 耗时: ${duration}ms")
                
                response.use {
                    if (it.isSuccessful) {
                        android.util.Log.d(TAG, "上传成功")
                        Result.success(Unit)
                    } else {
                        val responseBody = it.body?.string() ?: ""
                        android.util.Log.e(TAG, "上传失败: ${it.code} ${it.message}")
                        android.util.Log.e(TAG, "响应体: $responseBody")
                        
                        val errorMessage = when (it.code) {
                            401, 403 -> "认证失败，请检查用户名和密码"
                            404 -> "目录不存在，请先创建备份目录"
                            405 -> "服务器不支持 PUT 操作"
                            409 -> "资源冲突，可能是目录不存在或文件已存在"
                            413 -> "文件过大，服务器拒绝接收"
                            in 500..599 -> "服务器错误 (${it.code})，请稍后重试"
                            else -> "上传失败: HTTP ${it.code}"
                        }
                        
                        Result.failure(
                            WebDavUploadException(
                                statusCode = it.code,
                                responseBody = responseBody,
                                message = errorMessage
                            )
                        )
                    }
                }
            } catch (e: java.net.UnknownHostException) {
                android.util.Log.e(TAG, "DNS 解析失败", e)
                Result.failure(
                    WebDavUploadException(
                        statusCode = null,
                        responseBody = null,
                        message = "无法解析服务器地址: ${e.message}",
                        cause = e
                    )
                )
            } catch (e: java.net.SocketTimeoutException) {
                android.util.Log.e(TAG, "连接超时", e)
                Result.failure(
                    WebDavUploadException(
                        statusCode = null,
                        responseBody = null,
                        message = "网络超时，请检查网络连接",
                        cause = e
                    )
                )
            } catch (e: java.net.ConnectException) {
                android.util.Log.e(TAG, "连接被拒绝", e)
                Result.failure(
                    WebDavUploadException(
                        statusCode = null,
                        responseBody = null,
                        message = "无法连接到服务器: ${e.message}",
                        cause = e
                    )
                )
            } catch (e: Exception) {
                android.util.Log.e(TAG, "上传异常", e)
                Result.failure(
                    WebDavUploadException(
                        statusCode = null,
                        responseBody = null,
                        message = "上传失败: ${e.message}",
                        cause = e
                    )
                )
            }
        }
    }
    
    /**
     * 下载文件从 WebDAV 服务器
     */
    suspend fun downloadFile(url: String, credentials: WebDavCredentials, outputFile: java.io.File): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url(url)
                    .get()
                    .addHeader("Authorization", credentials.toAuthHeader())
                    .build()
                
                val response = okHttpClient.newCall(request).execute()
                if (response.isSuccessful) {
                    response.body?.byteStream()?.use { input ->
                        outputFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                    true
                } else {
                    false
                }
            } catch (e: Exception) {
                false
            }
        }
    }
    
    /**
     * 列出 WebDAV 目录中的文件
     */
    suspend fun listFiles(url: String, credentials: WebDavCredentials): List<WebDavFileInfo> {
        return withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url(url)
                    .method("PROPFIND", "".toRequestBody())
                    .addHeader("Authorization", credentials.toAuthHeader())
                    .addHeader("Depth", "1")
                    .build()
                
                val response = okHttpClient.newCall(request).execute()
                if (response.isSuccessful) {
                    val responseBody = response.body?.string() ?: return@withContext emptyList()
                    parseWebDavResponse(responseBody, url)
                } else {
                    emptyList()
                }
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
    
    /**
     * 删除 WebDAV 服务器上的文件
     */
    suspend fun deleteFile(url: String, credentials: WebDavCredentials): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url(url)
                    .delete()
                    .addHeader("Authorization", credentials.toAuthHeader())
                    .build()
                
                val response = okHttpClient.newCall(request).execute()
                response.isSuccessful
            } catch (e: Exception) {
                false
            }
        }
    }
    
    /**
     * 测试写权限
     */
    suspend fun testWritePermission(url: String, credentials: WebDavCredentials): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                android.util.Log.d(TAG, "测试写权限: $url")
                
                // 创建一个小测试文件
                val testContent = "test".toByteArray()
                val testUrl = "$url/.saison_test"
                
                val request = Request.Builder()
                    .url(testUrl)
                    .put(testContent.toRequestBody("text/plain".toMediaType()))
                    .addHeader("Authorization", credentials.toAuthHeader())
                    .build()
                
                val response = okHttpClient.newCall(request).execute()
                
                if (response.isSuccessful) {
                    android.util.Log.d(TAG, "写入测试成功")
                    // 清理测试文件
                    deleteFile(testUrl, credentials)
                    Result.success(true)
                } else {
                    android.util.Log.e(TAG, "写入测试失败: ${response.code}")
                    Result.failure(Exception("写入测试失败: ${response.code}"))
                }
            } catch (e: Exception) {
                android.util.Log.e(TAG, "写入测试异常", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * 创建 WebDAV 目录
     */
    suspend fun createDirectory(url: String, credentials: WebDavCredentials): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url(url)
                    .method("MKCOL", "".toRequestBody())
                    .addHeader("Authorization", credentials.toAuthHeader())
                    .build()
                
                val response = okHttpClient.newCall(request).execute()
                response.isSuccessful || response.code == 405 // 405 means already exists
            } catch (e: Exception) {
                false
            }
        }
    }
    
    private fun parseWebDavResponse(xml: String, baseUrl: String): List<WebDavFileInfo> {
        val files = mutableListOf<WebDavFileInfo>()
        try {
            // 简单的 XML 解析 - 查找 href 和 getlastmodified 标签
            val hrefPattern = "<d:href>([^<]+)</d:href>".toRegex()
            val sizePattern = "<d:getcontentlength>([^<]+)</d:getcontentlength>".toRegex()
            val modifiedPattern = "<d:getlastmodified>([^<]+)</d:getlastmodified>".toRegex()
            
            val responses = xml.split("<d:response>")
            for (response in responses) {
                if (response.contains("</d:response>")) {
                    val href = hrefPattern.find(response)?.groupValues?.get(1) ?: continue
                    val size = sizePattern.find(response)?.groupValues?.get(1)?.toLongOrNull() ?: 0L
                    val modified = modifiedPattern.find(response)?.groupValues?.get(1) ?: ""
                    
                    // 跳过目录本身
                    if (href.endsWith("/") && href != baseUrl) continue
                    if (href == baseUrl) continue
                    
                    // 只包含 .zip 文件
                    if (href.endsWith(".zip")) {
                        val fileName = href.substringAfterLast("/")
                        val modifiedTime = parseHttpDate(modified)
                        files.add(WebDavFileInfo(fileName, size, modifiedTime, href))
                    }
                }
            }
        } catch (e: Exception) {
            // 解析失败，返回空列表
        }
        return files
    }
}

data class WebDavFileInfo(
    val name: String,
    val size: Long,
    val modified: Long,
    val url: String
)

data class WebDavCredentials(
    val username: String,
    val password: String
) {
    fun toAuthHeader(): String {
        val credentials = "$username:$password"
        val encoded = Base64.encodeToString(credentials.toByteArray(), Base64.NO_WRAP)
        return "Basic $encoded"
    }
}

class WebDavException(message: String) : Exception(message)

data class WebDavUploadException(
    val statusCode: Int?,
    val responseBody: String?,
    override val message: String,
    override val cause: Throwable? = null
) : Exception(message, cause) {
    override fun toString(): String {
        return buildString {
            append("WebDavUploadException: $message")
            if (statusCode != null) {
                append(" (HTTP $statusCode)")
            }
            if (cause != null) {
                append("\nCaused by: ${cause.javaClass.simpleName}: ${cause.message}")
            }
        }
    }
}
