package takagi.ru.saison.data.local.database.entities.relations

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import takagi.ru.saison.data.local.database.entities.AttachmentEntity
import takagi.ru.saison.data.local.database.entities.TagEntity
import takagi.ru.saison.data.local.database.entities.TaskEntity

data class TaskWithDetails(
    @Embedded val task: TaskEntity,
    
    @Relation(
        parentColumn = "id",
        entityColumn = "parentTaskId"
    )
    val subtasks: List<TaskEntity>,
    
    @Relation(
        parentColumn = "id",
        entityColumn = "taskId"
    )
    val attachments: List<AttachmentEntity>,
    
    @Relation(
        parentColumn = "categoryId",
        entityColumn = "id"
    )
    val category: TagEntity?
)
