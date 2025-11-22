package takagi.ru.saison.data.local.datastore

import takagi.ru.saison.R

/**
 * 主题模式枚举
 * 
 * 定义应用的显示模式选项：
 * - FOLLOW_SYSTEM: 跟随系统设置
 * - LIGHT: 白天模式（浅色主题）
 * - DARK: 夜间模式（深色主题）
 */
enum class ThemeMode(
    val displayNameRes: Int,
    val descriptionRes: Int
) {
    /**
     * 跟随系统 - 根据系统设置自动切换深色/浅色模式
     */
    FOLLOW_SYSTEM(
        displayNameRes = R.string.theme_mode_follow_system,
        descriptionRes = R.string.theme_mode_follow_system_desc
    ),
    
    /**
     * 白天模式 - 始终使用浅色主题
     */
    LIGHT(
        displayNameRes = R.string.theme_mode_light,
        descriptionRes = R.string.theme_mode_light_desc
    ),
    
    /**
     * 夜间模式 - 始终使用深色主题
     */
    DARK(
        displayNameRes = R.string.theme_mode_dark,
        descriptionRes = R.string.theme_mode_dark_desc
    ),
    
    /**
     * 动态时间 - 根据时间段自动切换主题和颜色
     * 优先级高于配色选择
     */
    AUTO_TIME(
        displayNameRes = R.string.theme_mode_auto_time,
        descriptionRes = R.string.theme_mode_auto_time_desc
    );
    
    companion object {
        /**
         * 从字符串反序列化为 ThemeMode
         * 
         * @param value 枚举值的字符串表示
         * @return 对应的 ThemeMode，如果不匹配则返回 FOLLOW_SYSTEM
         */
        fun fromString(value: String): ThemeMode {
            return values().find { it.name == value } ?: FOLLOW_SYSTEM
        }
    }
}
