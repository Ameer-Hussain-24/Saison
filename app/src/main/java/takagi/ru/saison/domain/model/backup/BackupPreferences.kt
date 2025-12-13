package takagi.ru.saison.domain.model.backup

data class BackupPreferences(
    val includeTasks: Boolean = true,
    val includeCourses: Boolean = true,
    val includeEvents: Boolean = true,
    val includeRoutines: Boolean = true,
    val includeSubscriptions: Boolean = true,
    val includeValueDays: Boolean = true,
    val includePomodoroSessions: Boolean = true,
    val includeSemesters: Boolean = true,
    val includePreferences: Boolean = true
) {
    fun hasAnyEnabled(): Boolean {
        return includeTasks || includeCourses || includeEvents || 
               includeRoutines || includeSubscriptions || includeValueDays ||
               includePomodoroSessions || includeSemesters || includePreferences
    }
}
