package takagi.ru.saison.data.remote.webdav

import takagi.ru.saison.domain.model.Task
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConflictResolver @Inject constructor() {
    
    fun detectConflicts(localTasks: List<Task>, remoteTasks: List<Task>): List<TaskConflict> {
        val conflicts = mutableListOf<TaskConflict>()
        val remoteTasksMap = remoteTasks.associateBy { it.id }
        
        for (localTask in localTasks) {
            val remoteTask = remoteTasksMap[localTask.id]
            if (remoteTask != null) {
                // Both local and remote have the same task
                if (localTask.updatedAt != remoteTask.updatedAt) {
                    // Conflict: both modified
                    conflicts.add(
                        TaskConflict(
                            taskId = localTask.id,
                            localTask = localTask,
                            remoteTask = remoteTask,
                            conflictType = ConflictType.BOTH_MODIFIED
                        )
                    )
                }
            }
        }
        
        return conflicts
    }
    
    fun resolveConflicts(
        conflicts: List<TaskConflict>,
        strategy: ConflictResolutionStrategy = ConflictResolutionStrategy.LATEST_WINS
    ): List<ResolvedConflict> {
        return conflicts.map { conflict ->
            val winner = when (strategy) {
                ConflictResolutionStrategy.LATEST_WINS -> {
                    if (conflict.localTask.updatedAt.isAfter(conflict.remoteTask.updatedAt)) {
                        conflict.localTask
                    } else {
                        conflict.remoteTask
                    }
                }
                ConflictResolutionStrategy.LOCAL_WINS -> conflict.localTask
                ConflictResolutionStrategy.REMOTE_WINS -> conflict.remoteTask
            }
            
            ResolvedConflict(
                taskId = conflict.taskId,
                resolvedTask = winner,
                strategy = strategy
            )
        }
    }
    
    fun mergeTasks(
        localTasks: List<Task>,
        remoteTasks: List<Task>,
        resolvedConflicts: List<ResolvedConflict>
    ): List<Task> {
        val resolvedMap = resolvedConflicts.associateBy { it.taskId }
        val remoteTasksMap = remoteTasks.associateBy { it.id }
        val localTasksMap = localTasks.associateBy { it.id }
        
        val mergedTasks = mutableListOf<Task>()
        
        // Add all local tasks (with resolved conflicts)
        for (localTask in localTasks) {
            val resolved = resolvedMap[localTask.id]
            if (resolved != null) {
                mergedTasks.add(resolved.resolvedTask)
            } else {
                mergedTasks.add(localTask)
            }
        }
        
        // Add remote tasks that don't exist locally
        for (remoteTask in remoteTasks) {
            if (!localTasksMap.containsKey(remoteTask.id)) {
                mergedTasks.add(remoteTask)
            }
        }
        
        return mergedTasks
    }
}

data class TaskConflict(
    val taskId: Long,
    val localTask: Task,
    val remoteTask: Task,
    val conflictType: ConflictType
)

enum class ConflictType {
    BOTH_MODIFIED,
    LOCAL_DELETED,
    REMOTE_DELETED
}

data class ResolvedConflict(
    val taskId: Long,
    val resolvedTask: Task,
    val strategy: ConflictResolutionStrategy
)

enum class ConflictResolutionStrategy {
    LATEST_WINS,
    LOCAL_WINS,
    REMOTE_WINS
}
