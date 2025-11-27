package takagi.ru.saison.ui.components

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import takagi.ru.saison.util.TextUtils

/**
 * Text component that automatically handles overflow for multi-language support
 * Uses ellipsis and maxLines to prevent UI deformation
 */
@Composable
fun AdaptiveText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    maxLines: Int = 1,
    overflow: TextOverflow = TextOverflow.Ellipsis,
    adaptiveSize: Boolean = true
) {
    val context = LocalContext.current
    val adaptedStyle = if (adaptiveSize) {
        style.copy(
            fontSize = TextUtils.getAdaptiveTextSize(
                text = text,
                baseSize = style.fontSize,
                locale = context.resources.configuration.locales[0]
            )
        )
    } else {
        style
    }
    
    Text(
        text = text,
        modifier = modifier,
        style = adaptedStyle,
        maxLines = maxLines,
        overflow = overflow
    )
}

/**
 * Text component for navigation labels that uses abbreviated versions for non-Chinese locales
 */
@Composable
fun NavigationLabelText(
    fullTextResId: Int,
    shortTextResId: Int,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    maxLines: Int = 1
) {
    val context = LocalContext.current
    val text = TextUtils.getNavigationLabel(context, fullTextResId, shortTextResId)
    
    AdaptiveText(
        text = text,
        modifier = modifier,
        style = style,
        maxLines = maxLines
    )
}

/**
 * Text component for subtitles that automatically truncates for non-Chinese locales
 */
@Composable
fun SubtitleText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    maxLines: Int = 2
) {
    AdaptiveText(
        text = text,
        modifier = modifier,
        style = style,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis
    )
}
