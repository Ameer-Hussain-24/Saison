package takagi.ru.saison.data.local.datastore

data class ThemePreferences(
    val theme: SeasonalTheme = SeasonalTheme.DYNAMIC,
    val themeMode: ThemeMode = ThemeMode.FOLLOW_SYSTEM,
    val useDynamicColor: Boolean = true
)

enum class SeasonalTheme {
    // 特殊主题
    DYNAMIC,
    AUTO_SEASONAL,  // 四季自动切换
    
    // 季节性主题
    SAKURA,
    MINT,
    AMBER,
    SNOW,
    RAIN,
    MAPLE,
    OCEAN,
    SUNSET,
    FOREST,
    LAVENDER,
    DESERT,
    AURORA
}
