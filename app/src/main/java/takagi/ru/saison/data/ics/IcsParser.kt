package takagi.ru.saison.data.ics

import android.content.Context
import android.net.Uri
import takagi.ru.saison.domain.model.ics.ParsedCourse
import takagi.ru.saison.domain.model.ics.RecurrenceInfo
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * ICS文件解析器
 */
class IcsParser {
    
    companion object {
        private const val MAX_FILE_SIZE = 10 * 1024 * 1024 // 10MB
        private val timezoneCache = mutableMapOf<String, ZoneId>()
    }
    
    /**
     * 解析ICS文件内容
     * @param icsContent ICS文件的文本内容
     * @return 解析后的课程列表
     * @throws IcsException 解析失败时抛出
     */
    fun parse(icsContent: String): List<ParsedCourse> {
        if (icsContent.isBlank()) {
            throw IcsException.EmptyFile()
        }
        
        val lines = icsContent.lines()
        val courses = mutableListOf<ParsedCourse>()
        
        var inVEvent = false
        val eventLines = mutableListOf<String>()
        
        for (line in lines) {
            val trimmedLine = line.trim()
            
            when {
                trimmedLine == "BEGIN:VEVENT" -> {
                    inVEvent = true
                    eventLines.clear()
                }
                trimmedLine == "END:VEVENT" -> {
                    inVEvent = false
                    try {
                        val course = parseVEvent(eventLines)
                        if (course != null) {
                            courses.add(course)
                        }
                    } catch (e: Exception) {
                        // 跳过无效的VEVENT，继续处理其他事件
                        android.util.Log.w("IcsParser", "Failed to parse VEVENT: ${e.message}")
                    }
                }
                inVEvent -> {
                    eventLines.add(trimmedLine)
                }
            }
        }
        
        if (courses.isEmpty()) {
            throw IcsException.EmptyFile()
        }
        
        return courses
    }
    
    /**
     * 从Uri读取并解析ICS文件
     * 优先从临时缓存读取内容，如果缓存为空则尝试从URI读取
     */
    suspend fun parseFromUri(context: Context, uri: Uri): List<ParsedCourse> {
        android.util.Log.d("IcsParser", "Attempting to parse ICS from URI: $uri")
        
        // 首先尝试从临时缓存读取
        val cachedContent = takagi.ru.saison.util.TempFileCache.retrieve()
        if (cachedContent != null) {
            android.util.Log.d("IcsParser", "Using cached content, length: ${cachedContent.length}")
            return parseContent(cachedContent)
        }
        
        // 如果缓存为空，尝试从URI读取
        android.util.Log.d("IcsParser", "No cached content, reading from URI")
        android.util.Log.d("IcsParser", "URI scheme: ${uri.scheme}, authority: ${uri.authority}")
        
        try {
            android.util.Log.d("IcsParser", "Opening input stream...")
            val inputStream = context.contentResolver.openInputStream(uri)
            
            if (inputStream == null) {
                throw IcsException.IoError("无法打开文件")
            }
            
            android.util.Log.d("IcsParser", "Input stream opened successfully")
            
            val content = inputStream.use { stream ->
                stream.bufferedReader(Charsets.UTF_8).use { reader ->
                    try {
                        reader.readText()
                    } catch (e: Exception) {
                        android.util.Log.e("IcsParser", "Error reading file content", e)
                        throw IcsException.IoError("读取文件内容失败: ${e.message}")
                    }
                }
            }
            
            android.util.Log.d("IcsParser", "File content read successfully, length: ${content.length}")
            return parseContent(content)
        } catch (e: IcsException) {
            throw e
        } catch (e: SecurityException) {
            android.util.Log.e("IcsParser", "SecurityException when accessing URI", e)
            throw IcsException.IoError("文件访问权限已失效，请重新选择文件")
        } catch (e: java.io.FileNotFoundException) {
            android.util.Log.e("IcsParser", "FileNotFoundException", e)
            throw IcsException.IoError("文件不存在或已被删除")
        } catch (e: Exception) {
            android.util.Log.e("IcsParser", "Unexpected error parsing ICS file from URI: $uri", e)
            throw IcsException.IoError("读取文件失败: ${e.javaClass.simpleName} - ${e.message}")
        }
    }
    
    /**
     * 解析ICS文件内容
     */
    private fun parseContent(content: String): List<ParsedCourse> {
        // 检查内容是否为空
        if (content.isBlank()) {
            throw IcsException.EmptyFile()
        }
        
        // 检查文件大小
        if (content.length > MAX_FILE_SIZE) {
            throw IcsException.InvalidFormat("文件大小超过限制（最大10MB）")
        }
        
        // 验证是否是有效的ICS文件
        if (!content.contains("BEGIN:VCALENDAR")) {
            throw IcsException.InvalidFormat("不是有效的ICS文件格式")
        }
        
        return parse(content)
    }
    
    /**
     * 解析单个VEVENT组件
     */
    private fun parseVEvent(lines: List<String>): ParsedCourse? {
        var summary: String? = null
        var location: String? = null
        var description: String? = null
        var dtStart: LocalDateTime? = null
        var dtEnd: LocalDateTime? = null
        var rrule: RecurrenceInfo? = null
        var alarmMinutes: Int? = null
        var tzid: String? = null
        
        var i = 0
        while (i < lines.size) {
            var line = lines[i]
            
            // 处理多行折叠（以空格或制表符开头的行是上一行的延续）
            while (i + 1 < lines.size && (lines[i + 1].startsWith(" ") || lines[i + 1].startsWith("\t"))) {
                i++
                line += lines[i].substring(1)
            }
            
            when {
                line.startsWith("SUMMARY:") -> {
                    summary = line.substring(8)
                }
                line.startsWith("LOCATION:") -> {
                    location = line.substring(9)
                }
                line.startsWith("DESCRIPTION:") -> {
                    description = line.substring(12).replace("\\n", "\n")
                }
                line.startsWith("DTSTART") -> {
                    val (dateTime, tz) = parseDateTime(line)
                    dtStart = dateTime
                    if (tz != null) tzid = tz
                }
                line.startsWith("DTEND") -> {
                    val (dateTime, _) = parseDateTime(line)
                    dtEnd = dateTime
                }
                line.startsWith("RRULE:") -> {
                    rrule = parseRRule(line.substring(6))
                }
                line.startsWith("BEGIN:VALARM") -> {
                    // 查找TRIGGER行
                    var j = i + 1
                    while (j < lines.size && !lines[j].startsWith("END:VALARM")) {
                        if (lines[j].startsWith("TRIGGER")) {
                            alarmMinutes = parseTrigger(lines[j])
                            break
                        }
                        j++
                    }
                }
            }
            i++
        }
        
        // 验证必需字段
        if (summary == null || dtStart == null || dtEnd == null) {
            return null
        }
        
        // 添加日志记录description字段内容
        android.util.Log.d("IcsParser", "Parsed course: $summary")
        android.util.Log.d("IcsParser", "  Location: $location")
        android.util.Log.d("IcsParser", "  Description: $description")
        android.util.Log.d("IcsParser", "  Start: $dtStart, End: $dtEnd")
        
        return ParsedCourse(
            summary = summary,
            location = location,
            description = description,
            dtStart = dtStart,
            dtEnd = dtEnd,
            rrule = rrule,
            alarmMinutes = alarmMinutes
        )
    }
    
    /**
     * 解析日期时间字段
     * 支持格式: DTSTART:20250827T153000
     *          DTSTART;TZID=Asia/Shanghai:20250827T153000
     */
    private fun parseDateTime(line: String): Pair<LocalDateTime, String?> {
        var tzid: String? = null
        var dateTimeStr: String
        
        if (line.contains(";TZID=")) {
            // 提取时区
            val parts = line.split(":")
            val paramPart = parts[0]
            dateTimeStr = parts.getOrNull(1) ?: throw IcsException.ParseError("Invalid DTSTART format")
            
            val tzidMatch = Regex("TZID=([^;]+)").find(paramPart)
            if (tzidMatch != null) {
                tzid = tzidMatch.groupValues[1]
            }
        } else {
            // 没有时区参数
            dateTimeStr = line.substringAfter(":")
        }
        
        // 移除Z后缀（UTC标记）
        dateTimeStr = dateTimeStr.removeSuffix("Z")
        
        // 解析日期时间
        val formatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss")
        val dateTime = try {
            LocalDateTime.parse(dateTimeStr, formatter)
        } catch (e: Exception) {
            throw IcsException.ParseError("Invalid date time format: $dateTimeStr")
        }
        
        return Pair(dateTime, tzid)
    }
    
    /**
     * 解析RRULE
     * 格式: FREQ=WEEKLY;UNTIL=20250902T160000Z;INTERVAL=1
     */
    private fun parseRRule(rrule: String): RecurrenceInfo {
        val parts = rrule.split(";")
        var frequency = "WEEKLY"
        var interval = 1
        var until: LocalDateTime? = null
        var byDay: List<String>? = null
        
        for (part in parts) {
            val keyValue = part.split("=")
            if (keyValue.size != 2) continue
            
            val key = keyValue[0].trim()
            val value = keyValue[1].trim()
            
            when (key) {
                "FREQ" -> frequency = value
                "INTERVAL" -> interval = value.toIntOrNull() ?: 1
                "UNTIL" -> {
                    // 解析UNTIL日期
                    val dateStr = value.removeSuffix("Z")
                    val formatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss")
                    until = try {
                        LocalDateTime.parse(dateStr, formatter)
                    } catch (e: Exception) {
                        null
                    }
                }
                "BYDAY" -> {
                    byDay = value.split(",")
                }
            }
        }
        
        return RecurrenceInfo(
            frequency = frequency,
            interval = interval,
            until = until,
            byDay = byDay
        )
    }
    
    /**
     * 解析TRIGGER
     * 格式: TRIGGER;RELATED=START:-PT20M
     */
    private fun parseTrigger(line: String): Int? {
        val triggerValue = line.substringAfter(":")
        
        // 解析 -PT20M 格式
        val minutesMatch = Regex("-?PT(\\d+)M").find(triggerValue)
        if (minutesMatch != null) {
            return minutesMatch.groupValues[1].toIntOrNull()
        }
        
        // 解析 -PT1H 格式
        val hoursMatch = Regex("-?PT(\\d+)H").find(triggerValue)
        if (hoursMatch != null) {
            val hours = hoursMatch.groupValues[1].toIntOrNull() ?: return null
            return hours * 60
        }
        
        return null
    }
    
    /**
     * 获取时区ID（带缓存）
     */
    private fun getZoneId(tzid: String): ZoneId {
        return timezoneCache.getOrPut(tzid) {
            try {
                ZoneId.of(tzid)
            } catch (e: Exception) {
                ZoneId.systemDefault()
            }
        }
    }
}
