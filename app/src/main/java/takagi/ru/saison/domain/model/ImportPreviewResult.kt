package takagi.ru.saison.domain.model

/**
 * 导入预览结果
 */
data class ImportPreviewResult(
    val courses: List<Course>,
    val generatedPeriods: List<CoursePeriod>? = null,
    val matchingWarnings: List<String> = emptyList(),
    val suggestedSemesterStartDate: java.time.LocalDate? = null
)
