package takagi.ru.saison.data.local.database.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tags",
    indices = [
        Index("parentId"),
        Index("path", unique = true)
    ]
)
data class TagEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val name: String,
    val path: String, // e.g., "study/university/math"
    val parentId: Long? = null,
    
    val icon: String? = null, // MaterialIcons name
    val color: Int,
    
    val createdAt: Long = System.currentTimeMillis()
)
