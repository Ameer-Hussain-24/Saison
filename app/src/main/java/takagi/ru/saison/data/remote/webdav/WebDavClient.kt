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
}

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
