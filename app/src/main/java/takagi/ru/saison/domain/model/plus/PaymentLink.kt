package takagi.ru.saison.domain.model.plus

import androidx.compose.ui.graphics.vector.ImageVector

/**
 * 支付链接数据模型
 * 
 * @param platform 支付平台名称（如"Ko-fi"或"爱发电"）
 * @param url 支付链接URL
 * @param icon 平台图标
 */
data class PaymentLink(
    val platform: String,
    val url: String,
    val icon: ImageVector
)
