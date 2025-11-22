package takagi.ru.saison.domain.model

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

data class Course(
    val id: Long = 0,
    val name: String,
    val instructor: String? = null,
    val location: String? = null,
    val color: Int,
    val semesterId: Long = 1, // 所属学期ID，默认为1（默认学期）
    val dayOfWeek: DayOfWeek,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val weekPattern: WeekPattern = WeekPattern.ALL,
    val customWeeks: List<Int>? = null,      // 自定义周数列表（如 [1,3,5,7]）
    val startDate: LocalDate,
    val endDate: LocalDate,
    val notificationMinutes: Int = 10,
    val autoSilent: Boolean = true,
    val periodStart: Int? = null,            // 开始节次
    val periodEnd: Int? = null,              // 结束节次
    val isCustomTime: Boolean = false,       // 是否使用自定义时间
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class WeekPattern {
    ALL,      // Every week
    A,        // A week only
    B,        // B week only
    ODD,      // Odd weeks (1, 3, 5...)
    EVEN,     // Even weeks (2, 4, 6...)
    CUSTOM;   // Custom weeks (user-defined)
    
    companion object {
        fun fromString(value: String): WeekPattern {
            return entries.find { it.name == value } ?: ALL
        }
    }
}
