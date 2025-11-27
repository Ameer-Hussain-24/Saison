package takagi.ru.saison.domain.model.plus

import takagi.ru.saison.data.local.datastore.SeasonalTheme

/**
 * Saison Plus 会员专属主题配置
 * 
 * 定义了需要 Saison Plus 会员才能使用的主题列表
 */
object PremiumThemes {
    /**
     * 会员专属主题集合
     * 包含：Saison（四季自动）、樱花、薄荷、琥珀、海洋、日落、森林、极光、雨季、雪
     */
    val themes = setOf(
        SeasonalTheme.AUTO_SEASONAL, // Saison（四季自动切换）
        SeasonalTheme.SAKURA,        // 樱花
        SeasonalTheme.MINT,          // 薄荷
        SeasonalTheme.AMBER,         // 琥珀
        SeasonalTheme.OCEAN,         // 海洋
        SeasonalTheme.SUNSET,        // 日落
        SeasonalTheme.FOREST,        // 森林
        SeasonalTheme.AURORA,        // 极光
        SeasonalTheme.RAIN,          // 雨季
        SeasonalTheme.SNOW           // 雪
    )
    
    /**
     * 检查指定主题是否为会员专属主题
     * 
     * @param theme 要检查的主题
     * @return 如果是会员专属主题返回 true，否则返回 false
     */
    fun isPremiumTheme(theme: SeasonalTheme): Boolean {
        return theme in themes
    }
    
    /**
     * 获取会员专属主题的数量
     */
    val count: Int
        get() = themes.size
}
