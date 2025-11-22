package takagi.ru.saison.domain.model

enum class Priority(val value: Int) {
    LOW(0),
    MEDIUM(1),
    HIGH(2),
    URGENT(3);
    
    companion object {
        fun fromValue(value: Int): Priority {
            return entries.find { it.value == value } ?: MEDIUM
        }
    }
}
