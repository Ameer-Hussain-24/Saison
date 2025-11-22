package takagi.ru.saison.data.ics

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import takagi.ru.saison.domain.model.Course
import takagi.ru.saison.domain.model.WeekPattern
import java.io.File
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

/**
 * ICS文件生成器
 */
class IcsGenerator {
    
    companion object {
        private val DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss")
        private val DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd")
    }
    
    /**
     * 生成ICS文件内容
     * @param courses 要导出的课程列表
     * @param semesterName 学期名称（用于PRODID）
     * @return ICS格式的文本内容
     */
    fun generate(courses: List<Course>, semesterName: String): String {
        return generateVCalendar(courses, semesterName)
    }
    
    /**
     * 将ICS内容写入文件
     */
    suspend fun writeToFile(
        context: Context,
        courses: List<Course>,
        semesterName: String,
        fileName: String
    ): Uri {
        try {
            // 创建导出目录
            val documentsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            val exportDir = File(documentsDir, "course_exports")
            if (!exportDir.exists()) {
                exportDir.mkdirs()
            }
            
            // 生成文件名
            val timestamp = System.currentTimeMillis()
            val finalFileName = if (fileName.endsWith(".ics")) {
                fileName
            } else {
                "${fileName}_${timestamp}.ics"
            }
            
            val file = File(exportDir, finalFileName)
            
            // 生成ICS内容
            val icsContent = generate(courses, semesterName)
            
            // 写入文件
            file.writeText(icsContent)
            
            // 返回Uri
            return FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            throw IcsException.IoError("文件保存失败: ${e.message}")
        }
    }
    
    /**
     * 生成VCALENDAR
     */
    private fun generateVCalendar(courses: List<Course>, semesterName: String): String {
        val sb = StringBuilder()
        
        // VCALENDAR头部
        sb.appendLine("BEGIN:VCALENDAR")
        sb.appendLine("VERSION:2.0")
        sb.appendLine("PRODID:-//Saison//$semesterName//EN")
        sb.appendLine("CALSCALE:GREGORIAN")
        sb.appendLine("METHOD:PUBLISH")
        
        // VTIMEZONE
        sb.append(generateVTimezone())
        
        // VEVENT列表
        courses.forEach { course ->
            val events = generateVEvent(course)
            events.forEach { event ->
                sb.append(event)
            }
        }
        
        // VCALENDAR结束
        sb.appendLine("END:VCALENDAR")
        
        return sb.toString()
    }
    
    /**
     * 生成VTIMEZONE
     */
    private fun generateVTimezone(): String {
        return """
BEGIN:VTIMEZONE
TZID:Asia/Shanghai
LAST-MODIFIED:20250410T142247Z
TZURL:https://www.tzurl.org/zoneinfo-outlook/Asia/Shanghai
X-LIC-LOCATION:Asia/Shanghai
BEGIN:STANDARD
TZNAME:CST
TZOFFSETFROM:+0800
TZOFFSETTO:+0800
DTSTART:19700101T000000
END:STANDARD
END:VTIMEZONE

""".trimIndent()
    }
    
    /**
     * 生成VEVENT
     * 对于自定义周次，生成多个VEVENT
     */
    private fun generateVEvent(course: Course): List<String> {
        return when (course.weekPattern) {
            WeekPattern.CUSTOM -> {
                // 自定义周次，生成多个VEVENT
                generateCustomWeekEvents(course)
            }
            else -> {
                // 其他模式，生成单个VEVENT
                listOf(generateSingleVEvent(course, course.startDate, course.endDate))
            }
        }
    }
    
    /**
     * 生成单个VEVENT
     */
    private fun generateSingleVEvent(
        course: Course,
        startDate: LocalDate,
        endDate: LocalDate
    ): String {
        val sb = StringBuilder()
        
        val uid = "Saison-${UUID.randomUUID()}"
        val dtstamp = formatDateTime(LocalDateTime.now())
        
        // 组合日期和时间
        val dtStart = LocalDateTime.of(startDate, course.startTime)
        val dtEnd = LocalDateTime.of(startDate, course.endTime)
        
        sb.appendLine("BEGIN:VEVENT")
        sb.appendLine("DTSTAMP:$dtstamp")
        sb.appendLine("UID:$uid")
        sb.appendLine("SUMMARY:${course.name}")
        
        // DTSTART和DTEND
        sb.appendLine("DTSTART;TZID=Asia/Shanghai:${formatDateTime(dtStart)}")
        sb.appendLine("DTEND;TZID=Asia/Shanghai:${formatDateTime(dtEnd)}")
        
        // RRULE
        val rrule = generateRRule(course, endDate)
        if (rrule != null) {
            sb.appendLine("RRULE:$rrule")
        }
        
        // LOCATION
        if (!course.location.isNullOrBlank()) {
            sb.appendLine("LOCATION:${course.location}")
        }
        
        // DESCRIPTION
        val description = buildDescription(course)
        if (description.isNotBlank()) {
            sb.appendLine("DESCRIPTION:$description")
        }
        
        // VALARM
        if (course.notificationMinutes > 0) {
            sb.appendLine("BEGIN:VALARM")
            sb.appendLine("ACTION:DISPLAY")
            sb.appendLine("TRIGGER;RELATED=START:-PT${course.notificationMinutes}M")
            sb.appendLine("DESCRIPTION:${course.name}${if (!course.location.isNullOrBlank()) "@${course.location}" else ""}\\n")
            sb.appendLine("END:VALARM")
        }
        
        sb.appendLine("END:VEVENT")
        
        return sb.toString()
    }
    
    /**
     * 生成自定义周次的多个VEVENT
     */
    private fun generateCustomWeekEvents(course: Course): List<String> {
        val events = mutableListOf<String>()
        val customWeeks = course.customWeeks ?: return events
        
        // 按周数分组为连续的区间
        val sortedWeeks = customWeeks.sorted()
        val intervals = mutableListOf<Pair<Int, Int>>()
        
        var start = sortedWeeks.first()
        var end = start
        
        for (i in 1 until sortedWeeks.size) {
            if (sortedWeeks[i] == end + 1) {
                end = sortedWeeks[i]
            } else {
                intervals.add(Pair(start, end))
                start = sortedWeeks[i]
                end = start
            }
        }
        intervals.add(Pair(start, end))
        
        // 为每个区间生成VEVENT
        intervals.forEach { (startWeek, endWeek) ->
            val intervalStartDate = course.startDate.plusWeeks((startWeek - 1).toLong())
            val intervalEndDate = course.startDate.plusWeeks(endWeek.toLong())
            
            events.add(generateSingleVEvent(course, intervalStartDate, intervalEndDate))
        }
        
        return events
    }
    
    /**
     * 生成RRULE
     */
    private fun generateRRule(course: Course, endDate: LocalDate): String? {
        val parts = mutableListOf<String>()
        
        // FREQ
        parts.add("FREQ=WEEKLY")
        
        // INTERVAL
        val interval = when (course.weekPattern) {
            WeekPattern.ALL -> 1
            WeekPattern.ODD, WeekPattern.EVEN -> 2
            else -> 1
        }
        parts.add("INTERVAL=$interval")
        
        // UNTIL
        val until = LocalDateTime.of(endDate, course.endTime)
        parts.add("UNTIL=${formatDateTime(until)}Z")
        
        return parts.joinToString(";")
    }
    
    /**
     * 构建DESCRIPTION
     */
    private fun buildDescription(course: Course): String {
        val parts = mutableListOf<String>()
        
        // 节次信息
        if (course.periodStart != null && course.periodEnd != null) {
            if (course.periodStart == course.periodEnd) {
                parts.add("第${course.periodStart}节")
            } else {
                parts.add("第${course.periodStart} - ${course.periodEnd}节")
            }
        }
        
        // 地点
        if (!course.location.isNullOrBlank()) {
            parts.add(course.location)
        }
        
        return parts.joinToString("\\n")
    }
    
    /**
     * 格式化日期时间
     */
    private fun formatDateTime(dateTime: LocalDateTime): String {
        return dateTime.format(DATE_TIME_FORMATTER)
    }
}
