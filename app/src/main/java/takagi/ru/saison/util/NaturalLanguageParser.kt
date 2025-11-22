package takagi.ru.saison.util

import takagi.ru.saison.domain.model.Priority
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NaturalLanguageParser @Inject constructor() {
    
    private val datePatterns = listOf(
        // English patterns
        "today" to { _: MatchResult -> LocalDate.now() },
        "tomorrow" to { _: MatchResult -> LocalDate.now().plusDays(1) },
        "yesterday" to { _: MatchResult -> LocalDate.now().minusDays(1) },
        "next (monday|tuesday|wednesday|thursday|friday|saturday|sunday)" to { match: MatchResult ->
            val dayName = match.groupValues[1]
            val targetDay = parseDayOfWeek(dayName)
            LocalDate.now().with(TemporalAdjusters.next(targetDay))
        },
        "in (\\d+) days?" to { match: MatchResult ->
            val days = match.groupValues[1].toLong()
            LocalDate.now().plusDays(days)
        },
        "in (\\d+) weeks?" to { match: MatchResult ->
            val weeks = match.groupValues[1].toLong()
            LocalDate.now().plusWeeks(weeks)
        },
        "in (\\d+) months?" to { match: MatchResult ->
            val months = match.groupValues[1].toLong()
            LocalDate.now().plusMonths(months)
        },
        // Chinese patterns
        "今天|今日" to { _: MatchResult -> LocalDate.now() },
        "明天|明日" to { _: MatchResult -> LocalDate.now().plusDays(1) },
        "后天" to { _: MatchResult -> LocalDate.now().plusDays(2) },
        "昨天|昨日" to { _: MatchResult -> LocalDate.now().minusDays(1) },
        "下(周一|周二|周三|周四|周五|周六|周日|星期一|星期二|星期三|星期四|星期五|星期六|星期日)" to { match: MatchResult ->
            val dayName = match.groupValues[1]
            val targetDay = parseChineseDayOfWeek(dayName)
            LocalDate.now().with(TemporalAdjusters.next(targetDay))
        },
        "(\\d+)天后" to { match: MatchResult ->
            val days = match.groupValues[1].toLong()
            LocalDate.now().plusDays(days)
        },
        "(\\d+)周后" to { match: MatchResult ->
            val weeks = match.groupValues[1].toLong()
            LocalDate.now().plusWeeks(weeks)
        }
    )
    
    private val timePatterns = listOf(
        // 24-hour format
        "(\\d{1,2}):(\\d{2})" to { match: MatchResult ->
            val hour = match.groupValues[1].toInt()
            val minute = match.groupValues[2].toInt()
            LocalTime.of(hour, minute)
        },
        // 12-hour format
        "(\\d{1,2})\\s*(am|pm)" to { match: MatchResult ->
            var hour = match.groupValues[1].toInt()
            val period = match.groupValues[2].lowercase()
            if (period == "pm" && hour < 12) hour += 12
            if (period == "am" && hour == 12) hour = 0
            LocalTime.of(hour, 0)
        },
        // Chinese time
        "(上午|下午|早上|晚上|中午)(\\d{1,2})(点|时)" to { match: MatchResult ->
            val period = match.groupValues[1]
            var hour = match.groupValues[2].toInt()
            when (period) {
                "下午", "晚上" -> if (hour < 12) hour += 12
                "中午" -> hour = 12
            }
            LocalTime.of(hour, 0)
        },
        // Special times
        "noon|中午" to { _: MatchResult -> LocalTime.of(12, 0) },
        "midnight|午夜" to { _: MatchResult -> LocalTime.of(0, 0) },
        "morning|早上" to { _: MatchResult -> LocalTime.of(9, 0) },
        "afternoon|下午" to { _: MatchResult -> LocalTime.of(14, 0) },
        "evening|晚上" to { _: MatchResult -> LocalTime.of(18, 0) }
    )
    
    private val priorityKeywords = mapOf(
        // English
        "urgent" to Priority.URGENT,
        "important" to Priority.HIGH,
        "asap" to Priority.URGENT,
        "high priority" to Priority.HIGH,
        "low priority" to Priority.LOW,
        // Chinese
        "紧急" to Priority.URGENT,
        "重要" to Priority.HIGH,
        "优先" to Priority.HIGH,
        "不急" to Priority.LOW
    )
    
    fun parse(input: String): ParsedTask {
        var title = input
        var dueDate: LocalDate? = null
        var time: LocalTime? = null
        var priority = Priority.MEDIUM
        val tags = mutableListOf<String>()
        
        // Extract date
        for ((pattern, parser) in datePatterns) {
            val regex = pattern.toRegex(RegexOption.IGNORE_CASE)
            val match = regex.find(input)
            if (match != null) {
                dueDate = parser(match)
                title = title.replace(match.value, "").trim()
                break
            }
        }
        
        // Extract time
        for ((pattern, parser) in timePatterns) {
            val regex = pattern.toRegex(RegexOption.IGNORE_CASE)
            val match = regex.find(input)
            if (match != null) {
                time = parser(match)
                title = title.replace(match.value, "").trim()
                break
            }
        }
        
        // Extract priority
        for ((keyword, pri) in priorityKeywords) {
            if (input.contains(keyword, ignoreCase = true)) {
                priority = pri
                title = title.replace(keyword, "", ignoreCase = true).trim()
            }
        }
        
        // Extract tags
        val tagRegex = "#([\\w\\u4e00-\\u9fa5]+(?:/[\\w\\u4e00-\\u9fa5]+)*)".toRegex()
        tagRegex.findAll(input).forEach { match ->
            tags.add(match.groupValues[1])
            title = title.replace(match.value, "").trim()
        }
        
        // Clean up title
        title = title.replace("\\s+".toRegex(), " ").trim()
        
        return ParsedTask(
            title = title,
            dueDate = dueDate,
            time = time,
            priority = priority,
            tags = tags
        )
    }
    
    private fun parseDayOfWeek(dayName: String): DayOfWeek {
        return when (dayName.lowercase()) {
            "monday" -> DayOfWeek.MONDAY
            "tuesday" -> DayOfWeek.TUESDAY
            "wednesday" -> DayOfWeek.WEDNESDAY
            "thursday" -> DayOfWeek.THURSDAY
            "friday" -> DayOfWeek.FRIDAY
            "saturday" -> DayOfWeek.SATURDAY
            "sunday" -> DayOfWeek.SUNDAY
            else -> DayOfWeek.MONDAY
        }
    }
    
    private fun parseChineseDayOfWeek(dayName: String): DayOfWeek {
        return when {
            dayName.contains("一") -> DayOfWeek.MONDAY
            dayName.contains("二") -> DayOfWeek.TUESDAY
            dayName.contains("三") -> DayOfWeek.WEDNESDAY
            dayName.contains("四") -> DayOfWeek.THURSDAY
            dayName.contains("五") -> DayOfWeek.FRIDAY
            dayName.contains("六") -> DayOfWeek.SATURDAY
            dayName.contains("日") -> DayOfWeek.SUNDAY
            else -> DayOfWeek.MONDAY
        }
    }
}

data class ParsedTask(
    val title: String,
    val dueDate: LocalDate?,
    val time: LocalTime?,
    val priority: Priority,
    val tags: List<String>
)
