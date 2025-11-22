package takagi.ru.saison.data.local.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 打卡记录数据库实体
 */
@Entity(
    tableName = "check_in_records",
    foreignKeys = [
        ForeignKey(
            entity = RoutineTaskEntity::class,
            parentColumns = ["id"],
            childColumns = ["routine_task_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["routine_task_id"]),
        Index(value = ["check_in_time"]),
        Index(value = ["cycle_start_date", "cycle_end_date"])
    ]
)
data class CheckInRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "routine_task_id")
    val routineTaskId: Long,
    
    @ColumnInfo(name = "check_in_time")
    val checkInTime: Long,  // Unix timestamp in milliseconds
    
    @ColumnInfo(name = "note")
    val note: String? = null,
    
    @ColumnInfo(name = "cycle_start_date")
    val cycleStartDate: Long,  // Unix timestamp (date only)
    
    @ColumnInfo(name = "cycle_end_date")
    val cycleEndDate: Long     // Unix timestamp (date only)
)
