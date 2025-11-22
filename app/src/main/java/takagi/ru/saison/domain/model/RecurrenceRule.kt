package takagi.ru.saison.domain.model

import java.time.DayOfWeek
import java.time.LocalDate

data class RecurrenceRule(
    val frequency: Frequency,
    val interval: Int = 1,
    val count: Int? = null,
    val until: LocalDate? = null,
    val byDay: List<DayOfWeek>? = null,
    val byMonthDay: List<Int>? = null
) {
    fun toRRule(): String {
        val parts = mutableListOf<String>()
        
        parts.add("FREQ=${frequency.name}")
        
        if (interval > 1) {
            parts.add("INTERVAL=$interval")
        }
        
        count?.let {
            parts.add("COUNT=$it")
        }
        
        until?.let {
            parts.add("UNTIL=${it.toString().replace("-", "")}")
        }
        
        byDay?.let { days ->
            val dayStr = days.joinToString(",") { day ->
                when (day) {
                    DayOfWeek.MONDAY -> "MO"
                    DayOfWeek.TUESDAY -> "TU"
                    DayOfWeek.WEDNESDAY -> "WE"
                    DayOfWeek.THURSDAY -> "TH"
                    DayOfWeek.FRIDAY -> "FR"
                    DayOfWeek.SATURDAY -> "SA"
                    DayOfWeek.SUNDAY -> "SU"
                }
            }
            parts.add("BYDAY=$dayStr")
        }
        
        byMonthDay?.let { days ->
            parts.add("BYMONTHDAY=${days.joinToString(",")}")
        }
        
        return parts.joinToString(";")
    }
    
    companion object {
        fun fromRRule(rrule: String): RecurrenceRule? {
            try {
                val parts = rrule.split(";").associate { part ->
                    val (key, value) = part.split("=")
                    key to value
                }
                
                val frequency = Frequency.valueOf(parts["FREQ"] ?: return null)
                val interval = parts["INTERVAL"]?.toIntOrNull() ?: 1
                val count = parts["COUNT"]?.toIntOrNull()
                val until = parts["UNTIL"]?.let { LocalDate.parse(it) }
                
                val byDay = parts["BYDAY"]?.split(",")?.mapNotNull { day ->
                    when (day) {
                        "MO" -> DayOfWeek.MONDAY
                        "TU" -> DayOfWeek.TUESDAY
                        "WE" -> DayOfWeek.WEDNESDAY
                        "TH" -> DayOfWeek.THURSDAY
                        "FR" -> DayOfWeek.FRIDAY
                        "SA" -> DayOfWeek.SATURDAY
                        "SU" -> DayOfWeek.SUNDAY
                        else -> null
                    }
                }
                
                val byMonthDay = parts["BYMONTHDAY"]?.split(",")?.mapNotNull { it.toIntOrNull() }
                
                return RecurrenceRule(
                    frequency = frequency,
                    interval = interval,
                    count = count,
                    until = until,
                    byDay = byDay,
                    byMonthDay = byMonthDay
                )
            } catch (e: Exception) {
                return null
            }
        }
    }
}

enum class Frequency {
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY
}
