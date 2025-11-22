package takagi.ru.saison.data.local.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 周期性任务数据库实体
 */
@Entity(tableName = "routine_tasks")
data class RoutineTaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "title")
    val title: String,
    
    @ColumnInfo(name = "description")
    val description: String? = null,
    
    @ColumnInfo(name = "icon")
    val icon: String? = null,
    
    @ColumnInfo(name = "cycle_type")
    val cycleType: String,  // CycleType enum name
    
    @ColumnInfo(name = "cycle_config")
    val cycleConfig: String,  // JSON 格式存储
    
    @ColumnInfo(name = "duration_minutes")
    val durationMinutes: Int? = null,  // 活动时长（分钟）
    
    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long,  // Unix timestamp in milliseconds
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long   // Unix timestamp in milliseconds
)
