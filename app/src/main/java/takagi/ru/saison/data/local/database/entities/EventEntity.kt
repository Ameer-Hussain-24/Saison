package takagi.ru.saison.data.local.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "events",
    indices = [
        Index("eventDate"),
        Index("category"),
        Index("isCompleted")
    ]
)
data class EventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val title: String,
    val description: String? = null,
    
    @ColumnInfo(name = "eventDate")
    val eventDate: Long, // 存储为时间戳
    
    @ColumnInfo(name = "category")
    val category: Int, // 0=Birthday, 1=Anniversary, 2=Countdown
    
    val isCompleted: Boolean = false,
    val reminderEnabled: Boolean = false,
    val reminderTime: Long? = null,
    
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
