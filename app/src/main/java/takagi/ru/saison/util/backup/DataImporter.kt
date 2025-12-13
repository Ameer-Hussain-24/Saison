package takagi.ru.saison.util.backup

import kotlinx.serialization.json.Json
import takagi.ru.saison.domain.model.*
import takagi.ru.saison.domain.model.routine.RoutineTask
import takagi.ru.saison.domain.model.routine.CycleType
import takagi.ru.saison.domain.model.routine.CycleConfig
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class DataImporter @Inject constructor(
    private val json: Json
) {
    companion object {
        private const val TAG = "DataImporter"
        private val dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
        private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
        private val timeFormatter = DateTimeFormatter.ISO_LOCAL_TIME
    }
    
    fun importTasks(jsonString: String): List<Task> {
        return try {
            android.util.Log.d(TAG, "开始解析任务 JSON，长度: ${jsonString.length}")
            val dtos = json.decodeFromString<List<TaskBackupDto>>(jsonString)
            val result = dtos.map { it.toDomain() }
            android.util.Log.d(TAG, "成功解析 ${result.size} 个任务")
            result
        } catch (e: Exception) {
            android.util.Log.e(TAG, "解析任务失败", e)
            android.util.Log.e(TAG, "JSON 内容预览: ${jsonString.take(200)}")
            emptyList()
        }
    }
    
    fun importCourses(jsonString: String): List<Course> {
        return try {
            android.util.Log.d(TAG, "开始解析课程 JSON，长度: ${jsonString.length}")
            val dtos = json.decodeFromString<List<CourseBackupDto>>(jsonString)
            val result = dtos.map { it.toDomain() }
            android.util.Log.d(TAG, "成功解析 ${result.size} 个课程")
            result
        } catch (e: Exception) {
            android.util.Log.e(TAG, "解析课程失败", e)
            emptyList()
        }
    }
    
    fun importEvents(jsonString: String): List<Event> {
        return try {
            android.util.Log.d(TAG, "开始解析事件 JSON，长度: ${jsonString.length}")
            val dtos = json.decodeFromString<List<EventBackupDto>>(jsonString)
            val result = dtos.map { it.toDomain() }
            android.util.Log.d(TAG, "成功解析 ${result.size} 个事件")
            result
        } catch (e: Exception) {
            android.util.Log.e(TAG, "解析事件失败", e)
            emptyList()
        }
    }
    
    fun importRoutines(jsonString: String): List<RoutineTask> {
        return try {
            android.util.Log.d(TAG, "开始解析例程 JSON，长度: ${jsonString.length}")
            val dtos = json.decodeFromString<List<RoutineBackupDto>>(jsonString)
            val result = dtos.map { it.toDomain() }
            android.util.Log.d(TAG, "成功解析 ${result.size} 个例程")
            result
        } catch (e: Exception) {
            android.util.Log.e(TAG, "解析例程失败", e)
            emptyList()
        }
    }
    
    fun importSubscriptions(jsonString: String): List<Subscription> {
        return try {
            android.util.Log.d(TAG, "开始解析订阅 JSON，长度: ${jsonString.length}")
            val dtos = json.decodeFromString<List<SubscriptionBackupDto>>(jsonString)
            val result = dtos.map { it.toDomain() }
            android.util.Log.d(TAG, "成功解析 ${result.size} 个订阅")
            result
        } catch (e: Exception) {
            android.util.Log.e(TAG, "解析订阅失败", e)
            android.util.Log.e(TAG, "JSON 内容预览: ${jsonString.take(200)}")
            emptyList()
        }
    }
    
    fun importValueDays(jsonString: String): List<takagi.ru.saison.data.local.database.entities.ValueDayEntity> {
        return try {
            android.util.Log.d(TAG, "开始解析买断 JSON，长度: ${jsonString.length}")
            val dtos = json.decodeFromString<List<ValueDayBackupDto>>(jsonString)
            val result = dtos.map { it.toDomain() }
            android.util.Log.d(TAG, "成功解析 ${result.size} 个买断")
            result
        } catch (e: Exception) {
            android.util.Log.e(TAG, "解析买断失败", e)
            android.util.Log.e(TAG, "JSON 内容预览: ${jsonString.take(200)}")
            emptyList()
        }
    }
    
    fun importPomodoroSessions(jsonString: String): List<PomodoroSession> {
        return try {
            android.util.Log.d(TAG, "开始解析番茄钟 JSON，长度: ${jsonString.length}")
            val dtos = json.decodeFromString<List<PomodoroBackupDto>>(jsonString)
            val result = dtos.map { it.toDomain() }
            android.util.Log.d(TAG, "成功解析 ${result.size} 个番茄钟")
            result
        } catch (e: Exception) {
            android.util.Log.e(TAG, "解析番茄钟失败", e)
            emptyList()
        }
    }
    
    fun importSemesters(jsonString: String): List<Semester> {
        return try {
            android.util.Log.d(TAG, "开始解析学期 JSON，长度: ${jsonString.length}")
            val dtos = json.decodeFromString<List<SemesterBackupDto>>(jsonString)
            val result = dtos.map { it.toDomain() }
            android.util.Log.d(TAG, "成功解析 ${result.size} 个学期")
            result
        } catch (e: Exception) {
            android.util.Log.e(TAG, "解析学期失败", e)
            android.util.Log.e(TAG, "JSON 内容预览: ${jsonString.take(200)}")
            emptyList()
        }
    }
    
    fun importCategories(jsonString: String): List<CategoryBackupDto> {
        return try {
            android.util.Log.d(TAG, "开始解析分类 JSON，长度: ${jsonString.length}")
            val result = json.decodeFromString<List<CategoryBackupDto>>(jsonString)
            android.util.Log.d(TAG, "成功解析 ${result.size} 个分类")
            result
        } catch (e: Exception) {
            android.util.Log.e(TAG, "解析分类失败", e)
            android.util.Log.e(TAG, "JSON 内容预览: ${jsonString.take(200)}")
            emptyList()
        }
    }
    
    fun importPreferences(jsonString: String): Map<String, String> {
        return try {
            android.util.Log.d(TAG, "开始解析偏好设置 JSON，长度: ${jsonString.length}")
            val result = json.decodeFromString<Map<String, String>>(jsonString)
            android.util.Log.d(TAG, "成功解析 ${result.size} 个偏好设置")
            result
        } catch (e: Exception) {
            android.util.Log.e(TAG, "解析偏好设置失败", e)
            emptyMap()
        }
    }
    
    // DTO to Domain conversion functions
    private fun TaskBackupDto.toDomain() = Task(
        id = id,
        title = title,
        description = description,
        dueDate = dueDate?.let { LocalDateTime.parse(it, dateTimeFormatter) },
        reminderTime = reminderTime?.let { LocalDateTime.parse(it, dateTimeFormatter) },
        location = location,
        priority = Priority.fromValue(priority),
        isCompleted = isCompleted,
        completedAt = completedAt?.let { LocalDateTime.parse(it, dateTimeFormatter) },
        category = if (categoryId != null && categoryName != null) {
            Tag(
                id = categoryId,
                name = categoryName,
                path = categoryName,  // 使用name作为path
                color = 0xFF6200EE.toInt()  // 默认颜色
            )
        } else null,
        pomodoroCount = pomodoroCount,
        estimatedPomodoros = estimatedPomodoros,
        metronomeBpm = metronomeBpm,
        isFavorite = isFavorite,
        sortOrder = sortOrder,
        createdAt = LocalDateTime.parse(createdAt, dateTimeFormatter),
        updatedAt = LocalDateTime.parse(updatedAt, dateTimeFormatter)
    )
    
    private fun CourseBackupDto.toDomain() = Course(
        id = id,
        name = name,
        instructor = instructor,
        location = location,
        color = color,
        semesterId = semesterId,
        dayOfWeek = DayOfWeek.of(dayOfWeek),
        startTime = LocalTime.parse(startTime, timeFormatter),
        endTime = LocalTime.parse(endTime, timeFormatter),
        weekPattern = WeekPattern.fromString(weekPattern),
        customWeeks = customWeeks,
        startDate = LocalDate.parse(startDate, dateFormatter),
        endDate = LocalDate.parse(endDate, dateFormatter),
        notificationMinutes = notificationMinutes,
        autoSilent = autoSilent,
        periodStart = periodStart,
        periodEnd = periodEnd,
        isCustomTime = isCustomTime,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
    
    private fun EventBackupDto.toDomain() = Event(
        id = id,
        title = title,
        description = description,
        eventDate = LocalDateTime.parse(eventDate, dateTimeFormatter),
        category = EventCategory.valueOf(category),
        isCompleted = isCompleted,
        reminderEnabled = reminderEnabled,
        reminderTime = reminderTime?.let { LocalDateTime.parse(it, dateTimeFormatter) },
        createdAt = LocalDateTime.parse(createdAt, dateTimeFormatter),
        updatedAt = LocalDateTime.parse(updatedAt, dateTimeFormatter)
    )
    
    private fun RoutineBackupDto.toDomain() = RoutineTask(
        id = id,
        title = title,
        description = description,
        icon = icon,
        cycleType = CycleType.valueOf(cycleType),
        cycleConfig = parseCycleConfig(cycleConfig),
        durationMinutes = durationMinutes,
        isActive = isActive,
        createdAt = LocalDateTime.parse(createdAt, dateTimeFormatter),
        updatedAt = LocalDateTime.parse(updatedAt, dateTimeFormatter)
    )
    
    private fun SubscriptionBackupDto.toDomain() = Subscription(
        id = id,
        name = name,
        description = description,
        price = price,
        currency = currency,
        billingCycle = BillingCycle.valueOf(billingCycle),
        startDate = LocalDate.parse(startDate, dateFormatter),
        nextBillingDate = LocalDate.parse(nextBillingDate, dateFormatter),
        reminderDaysBefore = reminderDaysBefore,
        isActive = isActive,
        category = category,
        icon = icon,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
    
    private fun ValueDayBackupDto.toDomain() = takagi.ru.saison.data.local.database.entities.ValueDayEntity(
        id = id,
        itemName = itemName,
        purchasePrice = purchasePrice,
        purchaseDate = purchaseDate,
        category = category,
        warrantyEndDate = warrantyEndDate,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
    
    private fun PomodoroBackupDto.toDomain() = PomodoroSession(
        id = id,
        taskId = taskId,
        routineTaskId = routineTaskId,
        startTime = startTime,
        endTime = endTime,
        duration = duration,
        actualDuration = actualDuration,
        isCompleted = isCompleted,
        isBreak = isBreak,
        isLongBreak = isLongBreak,
        isEarlyFinish = isEarlyFinish,
        interruptions = interruptions,
        notes = notes
    )
    
    private fun SemesterBackupDto.toDomain() = Semester(
        id = id,
        name = name,
        startDate = LocalDate.parse(startDate, dateFormatter),
        endDate = LocalDate.parse(endDate, dateFormatter),
        totalWeeks = totalWeeks,
        isArchived = isArchived,
        isDefault = isDefault,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
    
    private fun parseCycleConfig(configString: String): CycleConfig {
        // Parse JSON string to CycleConfig  
        return try {
            json.decodeFromString<CycleConfig>(configString)
        } catch (e: Exception) {
            android.util.Log.e(TAG, "解析 CycleConfig 失败，使用默认值", e)
            CycleConfig.Daily()
        }
    }
}
