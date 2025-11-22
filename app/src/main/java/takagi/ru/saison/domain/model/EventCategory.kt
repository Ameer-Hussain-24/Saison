package takagi.ru.saison.domain.model

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.ui.graphics.vector.ImageVector
import takagi.ru.saison.R

enum class EventCategory(val value: Int) {
    BIRTHDAY(0),      // 生日
    ANNIVERSARY(1),   // 纪念日
    COUNTDOWN(2);     // 倒数日
    
    companion object {
        fun fromValue(value: Int): EventCategory {
            return entries.find { it.value == value } ?: COUNTDOWN
        }
    }
    
    @StringRes
    fun getDisplayNameResId(): Int {
        return when (this) {
            BIRTHDAY -> R.string.event_category_birthday
            ANNIVERSARY -> R.string.event_category_anniversary
            COUNTDOWN -> R.string.event_category_countdown
        }
    }
    
    fun getIcon(): ImageVector {
        return when (this) {
            BIRTHDAY -> Icons.Default.Cake
            ANNIVERSARY -> Icons.Default.Favorite
            COUNTDOWN -> Icons.Default.Event
        }
    }
}
