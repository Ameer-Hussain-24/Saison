package takagi.ru.saison.data.local.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "semesters",
    indices = [
        Index("startDate"),
        Index("isArchived"),
        Index("isDefault")
    ]
)
data class SemesterEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val name: String,
    
    @ColumnInfo(name = "startDate")
    val startDate: Long, // 学期开始日期，存储为时间戳（LocalDate.toEpochDay() * 86400000L）
    
    @ColumnInfo(name = "endDate")
    val endDate: Long, // 学期结束日期，存储为时间戳
    
    val totalWeeks: Int = 18, // 学期总周数
    
    @ColumnInfo(name = "isArchived")
    val isArchived: Boolean = false, // 是否归档
    
    @ColumnInfo(name = "isDefault")
    val isDefault: Boolean = false, // 是否为默认学期
    
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
