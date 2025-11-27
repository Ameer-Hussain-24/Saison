package takagi.ru.saison.domain.model.backup

data class RestoreSummary(
    val tasksImported: Int = 0,
    val coursesImported: Int = 0,
    val eventsImported: Int = 0,
    val routinesImported: Int = 0,
    val subscriptionsImported: Int = 0,
    val pomodoroSessionsImported: Int = 0,
    val semestersImported: Int = 0
) {
    val totalImported: Int
        get() = tasksImported + coursesImported + eventsImported + 
                routinesImported + subscriptionsImported + 
                pomodoroSessionsImported + semestersImported
}
