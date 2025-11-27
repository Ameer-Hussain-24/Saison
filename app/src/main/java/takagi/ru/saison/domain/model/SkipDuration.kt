package takagi.ru.saison.domain.model

data class SkipDuration(
    val amount: Int,
    val unit: SkipUnit
)

enum class SkipUnit {
    DAYS,
    MONTHS,
    YEARS
}
