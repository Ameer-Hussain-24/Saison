package takagi.ru.saison.domain.model.plus

import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Saison Plus功能数据模型
 * 
 * @param id 功能唯一标识
 * @param icon 功能图标
 * @param title 功能名称
 * @param description 功能描述
 * @param isAvailable 功能是否已实现（用于占位符）
 */
data class PlusFeature(
    val id: String,
    val icon: ImageVector,
    val title: String,
    val description: String,
    val isAvailable: Boolean = false
)
