package takagi.ru.saison.data.local.database.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "courses",
    indices = [
        Index("dayOfWeek"),
        Index("startTime"),
        Index("semesterId")
    ]
)
data class CourseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val name: String,
    val instructor: String? = null,
    val location: String? = null,
    val color: Int,
    
    val semesterId: Long = 1, // 所属学期ID，默认为1（默认学期）
    
    val dayOfWeek: Int, // 1=Monday, 7=Sunday
    val startTime: String, // HH:mm format
    val endTime: String,
    
    val weekPattern: String = "ALL", // "ALL", "A", "B", "ODD", "EVEN", "CUSTOM"
    val customWeeks: String? = null, // JSON array of week numbers, e.g. "[1,3,5,7]"
    val startDate: Long,
    val endDate: Long,
    
    val notificationMinutes: Int = 10,
    val autoSilent: Boolean = true,
    
    val periodStart: Int? = null,            // 开始节次
    val periodEnd: Int? = null,              // 结束节次
    val isCustomTime: Boolean = false,       // 是否使用自定义时间
    
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
