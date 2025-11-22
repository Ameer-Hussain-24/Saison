package takagi.ru.saison.domain.model.routine

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import java.time.DayOfWeek
import java.time.LocalTime

/**
 * CycleConfig JSON 序列化器
 */
object CycleConfigSerializer {
    
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    
    /**
     * 将 CycleConfig 序列化为 JSON 字符串
     */
    fun serialize(config: CycleConfig): String {
        val dto = when (config) {
            is CycleConfig.Daily -> CycleConfigDto(
                type = "daily",
                time = config.time?.toString(),
                daysOfWeek = null,
                daysOfMonth = null,
                rrule = null
            )
            is CycleConfig.Weekly -> CycleConfigDto(
                type = "weekly",
                time = null,
                daysOfWeek = config.daysOfWeek.map { it.name },
                daysOfMonth = null,
                rrule = null
            )
            is CycleConfig.Monthly -> CycleConfigDto(
                type = "monthly",
                time = null,
                daysOfWeek = null,
                daysOfMonth = config.daysOfMonth,
                rrule = null
            )
            is CycleConfig.Custom -> CycleConfigDto(
                type = "custom",
                time = null,
                daysOfWeek = null,
                daysOfMonth = null,
                rrule = config.rrule
            )
        }
        return json.encodeToString(dto)
    }
    
    /**
     * 从 JSON 字符串反序列化为 CycleConfig
     */
    fun deserialize(jsonString: String): CycleConfig {
        val dto = json.decodeFromString<CycleConfigDto>(jsonString)
        return when (dto.type) {
            "daily" -> CycleConfig.Daily(
                time = dto.time?.let { LocalTime.parse(it) }
            )
            "weekly" -> CycleConfig.Weekly(
                daysOfWeek = dto.daysOfWeek?.map { DayOfWeek.valueOf(it) } ?: emptyList()
            )
            "monthly" -> CycleConfig.Monthly(
                daysOfMonth = dto.daysOfMonth ?: emptyList()
            )
            "custom" -> CycleConfig.Custom(
                rrule = dto.rrule ?: ""
            )
            else -> throw IllegalArgumentException("Unknown cycle config type: ${dto.type}")
        }
    }
}

/**
 * 用于序列化的 DTO 类
 */
@Serializable
data class CycleConfigDto(
    val type: String,
    val time: String? = null,
    val daysOfWeek: List<String>? = null,
    val daysOfMonth: List<Int>? = null,
    val rrule: String? = null
)
