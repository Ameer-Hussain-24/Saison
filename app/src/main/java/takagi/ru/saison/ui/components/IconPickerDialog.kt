package takagi.ru.saison.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/**
 * 图标选择器对话框
 */
@Composable
fun IconPickerDialog(
    selectedIcon: String,
    onIconSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val icons = remember {
        listOf(
            "CheckCircle" to Icons.Default.CheckCircle,
            "Star" to Icons.Default.Star,
            "Favorite" to Icons.Default.Favorite,
            "Home" to Icons.Default.Home,
            "Work" to Icons.Default.Work,
            "School" to Icons.Default.School,
            "FitnessCenter" to Icons.Default.FitnessCenter,
            "Restaurant" to Icons.Default.Restaurant,
            "LocalCafe" to Icons.Default.LocalCafe,
            "Book" to Icons.Default.Book,
            "MusicNote" to Icons.Default.MusicNote,
            "Brush" to Icons.Default.Brush,
            "DirectionsRun" to Icons.Default.DirectionsRun,
            "SelfImprovement" to Icons.Default.SelfImprovement,
            "Spa" to Icons.Default.Spa,
            "LocalHospital" to Icons.Default.LocalHospital,
            "ShoppingCart" to Icons.Default.ShoppingCart,
            "LocalGroceryStore" to Icons.Default.LocalGroceryStore,
            "Pets" to Icons.Default.Pets,
            "Park" to Icons.Default.Park,
            "BeachAccess" to Icons.Default.BeachAccess,
            "Flight" to Icons.Default.Flight,
            "DirectionsCar" to Icons.Default.DirectionsCar,
            "DirectionsBike" to Icons.Default.DirectionsBike,
            "Lightbulb" to Icons.Default.Lightbulb,
            "Psychology" to Icons.Default.Psychology,
            "Celebration" to Icons.Default.Celebration,
            "Cake" to Icons.Default.Cake,
            "LocalFlorist" to Icons.Default.LocalFlorist,
            "WbSunny" to Icons.Default.WbSunny,
            "Nightlight" to Icons.Default.Nightlight,
            "Cloud" to Icons.Default.Cloud,
            "Umbrella" to Icons.Default.Umbrella,
            "AcUnit" to Icons.Default.AcUnit,
            "Whatshot" to Icons.Default.Whatshot,
            "Water" to Icons.Default.Water
        )
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择图标") },
        text = {
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                contentPadding = PaddingValues(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.height(400.dp)
            ) {
                items(icons) { (name, icon) ->
                    IconItem(
                        icon = icon,
                        isSelected = name == selectedIcon,
                        onClick = { onIconSelected(name) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        }
    )
}

/**
 * 图标项
 */
@Composable
private fun IconItem(
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .size(64.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}
