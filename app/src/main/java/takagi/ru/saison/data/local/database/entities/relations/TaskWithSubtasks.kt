package takagi.ru.saison.data.local.database.entities.relations

import androidx.room.Embedded
import androidx.room.Relation
import takagi.ru.saison.data.local.database.entities.TaskEntity

data class TaskWithSubtasks(
    @Embedded val task: TaskEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "parentTaskId"
    )
    val subtasks: List<TaskEntity>
)
