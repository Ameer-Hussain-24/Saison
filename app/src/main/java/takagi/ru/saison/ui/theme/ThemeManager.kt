package takagi.ru.saison.ui.theme

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import takagi.ru.saison.data.local.datastore.PreferencesManager
import takagi.ru.saison.data.local.datastore.SeasonalTheme
import takagi.ru.saison.data.local.datastore.ThemeMode
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ThemeManager @Inject constructor(
    private val preferencesManager: PreferencesManager
) {
    private val _currentTheme = MutableStateFlow(SeasonalTheme.DYNAMIC)
    val currentTheme: StateFlow<SeasonalTheme> = _currentTheme.asStateFlow()
    
    private val _themeMode = MutableStateFlow(ThemeMode.FOLLOW_SYSTEM)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()
    
    private val _useDynamicColor = MutableStateFlow(true)
    val useDynamicColor: StateFlow<Boolean> = _useDynamicColor.asStateFlow()
    
    init {
        // Load saved theme preferences
        CoroutineScope(Dispatchers.IO).launch {
            preferencesManager.themePreferences.collect { prefs ->
                _currentTheme.value = prefs.theme
                _themeMode.value = prefs.themeMode
                _useDynamicColor.value = prefs.useDynamicColor
            }
        }
    }
    
    suspend fun setTheme(theme: SeasonalTheme) {
        _currentTheme.value = theme
        preferencesManager.setTheme(theme)
    }
    
    suspend fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
        preferencesManager.setThemeMode(mode)
    }
    
    suspend fun setUseDynamicColor(enabled: Boolean) {
        _useDynamicColor.value = enabled
        preferencesManager.setUseDynamicColor(enabled)
    }
    
    fun getAllThemes(): List<SeasonalTheme> {
        return SeasonalTheme.entries
    }
}
