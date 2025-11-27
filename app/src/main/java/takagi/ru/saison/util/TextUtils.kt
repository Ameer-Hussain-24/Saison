package takagi.ru.saison.util

import android.content.Context
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import java.util.Locale

/**
 * Utility functions for handling text display and truncation
 */
object TextUtils {
    
    /**
     * Get abbreviated navigation label based on current locale
     * Falls back to short version for non-Chinese locales
     */
    fun getNavigationLabel(context: Context, fullResId: Int, shortResId: Int): String {
        val locale = context.resources.configuration.locales[0]
        val language = locale.language
        
        // Chinese is already well-optimized, use full labels
        return if (language == "zh") {
            context.getString(fullResId)
        } else {
            // Use abbreviated version for other languages
            context.getString(shortResId)
        }
    }
    
    /**
     * Truncate text to fit within a maximum length
     */
    fun truncateText(text: String, maxLength: Int, ellipsis: String = "..."): String {
        return if (text.length > maxLength) {
            text.take(maxLength - ellipsis.length) + ellipsis
        } else {
            text
        }
    }
    
    /**
     * Get adaptive text size based on text length and locale
     */
    fun getAdaptiveTextSize(
        text: String,
        baseSize: TextUnit,
        locale: Locale = Locale.getDefault()
    ): TextUnit {
        val language = locale.language
        
        // Chinese characters are more compact, can use larger size
        if (language == "zh") {
            return baseSize
        }
        
        // For longer text in other languages, reduce size slightly
        return when {
            text.length > 15 -> baseSize * 0.85f
            text.length > 10 -> baseSize * 0.9f
            else -> baseSize
        }
    }
    
    /**
     * Check if current locale is Chinese
     */
    fun isChineseLocale(context: Context): Boolean {
        val locale = context.resources.configuration.locales[0]
        return locale.language == "zh"
    }
    
    /**
     * Get compact label for UI elements based on locale
     * Returns full text for Chinese, abbreviated for others
     */
    fun getCompactLabel(fullText: String, abbreviatedText: String, context: Context): String {
        return if (isChineseLocale(context)) {
            fullText
        } else {
            abbreviatedText
        }
    }
}
