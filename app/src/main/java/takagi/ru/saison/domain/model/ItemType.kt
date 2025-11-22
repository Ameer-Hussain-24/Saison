package takagi.ru.saison.domain.model

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Task
import androidx.compose.ui.graphics.vector.ImageVector
import takagi.ru.saison.R

enum class ItemType(val value: Int) {
    TASK(0),
    SCHEDULE(1),
    EVENT(2);
    
    companion object {
        fun fromValue(value: Int): ItemType {
            return entries.find { it.value == value } ?: TASK
        }
    }
    
    @StringRes
    fun getDisplayNameResId(): Int {
        return when (this) {
            TASK -> R.string.item_type_task
            SCHEDULE -> R.string.item_type_schedule
            EVENT -> R.string.item_type_event
        }
    }
    
    fun getIcon(): ImageVector {
        return when (this) {
            TASK -> Icons.Default.Task
            SCHEDULE -> Icons.Default.CalendarMonth
            EVENT -> Icons.Default.Event
        }
    }
}
