package takagi.ru.saison.data.local.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = TagEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("categoryId"),
        Index("dueDate"),
        Index("isCompleted"),
        Index("parentTaskId")
    ]
)
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val title: String,
    val description: String? = null,
    val dueDate: Long? = null,
    val reminderTime: Long? = null,
    val location: String? = null,
    
    @ColumnInfo(name = "priority")
    val priority: Int = 1, // 0=Low, 1=Medium, 2=High, 3=Urgent
    
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    
    val categoryId: Long,
    val parentTaskId: Long? = null, // For subtasks
    
    val repeatRule: String? = null, // RRULE format
    val repeatEndDate: Long? = null,
    
    val pomodoroCount: Int = 0,
    val estimatedPomodoros: Int? = null,
    
    val metronomeBpm: Int? = null,
    
    val isFavorite: Boolean = false,
    
    val isEncrypted: Boolean = false,
    val encryptedData: ByteArray? = null,
    
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val syncStatus: Int = 0 // 0=Synced, 1=Pending, 2=Conflict
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TaskEntity

        if (id != other.id) return false
        if (title != other.title) return false
        if (encryptedData != null) {
            if (other.encryptedData == null) return false
            if (!encryptedData.contentEquals(other.encryptedData)) return false
        } else if (other.encryptedData != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + (encryptedData?.contentHashCode() ?: 0)
        return result
    }
}
