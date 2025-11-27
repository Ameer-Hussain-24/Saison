package takagi.ru.saison.domain.model.plus

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*

/**
 * Saison Plus功能列表定义
 */
object PlusFeatures {
    /**
     * 获取所有Plus功能列表
     * 仅包含已实现的功能：WebDAV 备份和会员专属主题
     */
    fun getPlaceholderFeatures(): List<PlusFeature> = listOf(
        PlusFeature(
            id = "webdav_backup",
            icon = Icons.Default.Cloud,
            title = "WebDAV 云备份",
            description = "跨设备同步您的数据，支持自动备份和手动恢复",
            isAvailable = true
        ),
        PlusFeature(
            id = "premium_themes",
            icon = Icons.Default.Palette,
            title = "会员专属主题",
            description = "9 个精美主题：樱花、薄荷、琥珀、海洋、日落、森林、极光、雨季(Saison)、雪",
            isAvailable = true
        )
    )
}
