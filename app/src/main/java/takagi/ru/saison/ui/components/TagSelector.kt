package takagi.ru.saison.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import takagi.ru.saison.domain.model.Tag

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagSelectorBottomSheet(
    tags: List<Tag>,
    selectedTags: List<Tag>,
    onTagSelected: (Tag) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "选择标签",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(tags) { tag ->
                    TagItem(
                        tag = tag,
                        isSelected = selectedTags.contains(tag),
                        onClick = { onTagSelected(tag) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TagItem(
    tag: Tag,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { Text(tag.name) },
        leadingIcon = if (isSelected) {
            { Icon(Icons.Default.Check, contentDescription = null) }
        } else null
    )
}
