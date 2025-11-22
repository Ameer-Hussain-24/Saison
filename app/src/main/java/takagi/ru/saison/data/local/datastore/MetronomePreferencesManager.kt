package takagi.ru.saison.data.local.datastore

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import takagi.ru.saison.domain.model.MetronomePreset
import javax.inject.Inject
import javax.inject.Singleton

private val Context.metronomeDataStore by preferencesDataStore(name = "metronome_preferences")

@Singleton
class MetronomePreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.metronomeDataStore
    
    companion object {
        private val LAST_BPM = intPreferencesKey("last_bpm")
        private val LAST_TIME_SIGNATURE = intPreferencesKey("last_time_signature")
        private val LAST_VOLUME = floatPreferencesKey("last_volume")
        private val LAST_ACCENT_FIRST_BEAT = booleanPreferencesKey("last_accent_first_beat")
        private val LAST_ENABLE_VIBRATION = booleanPreferencesKey("last_enable_vibration")
        private val SAVED_PRESETS = stringPreferencesKey("saved_presets")
    }
    
    // 保存最后使用的设置
    suspend fun saveLastSettings(
        bpm: Int,
        timeSignature: Int,
        volume: Float,
        accentFirstBeat: Boolean,
        enableVibration: Boolean
    ) {
        dataStore.edit { preferences ->
            preferences[LAST_BPM] = bpm
            preferences[LAST_TIME_SIGNATURE] = timeSignature
            preferences[LAST_VOLUME] = volume
            preferences[LAST_ACCENT_FIRST_BEAT] = accentFirstBeat
            preferences[LAST_ENABLE_VIBRATION] = enableVibration
        }
    }
    
    // 获取最后使用的 BPM
    val lastBpm: Flow<Int> = dataStore.data.map { preferences ->
        preferences[LAST_BPM] ?: 120
    }
    
    // 获取最后使用的拍号
    val lastTimeSignature: Flow<Int> = dataStore.data.map { preferences ->
        preferences[LAST_TIME_SIGNATURE] ?: 4
    }
    
    // 获取最后使用的音量
    val lastVolume: Flow<Float> = dataStore.data.map { preferences ->
        preferences[LAST_VOLUME] ?: 0.8f
    }
    
    // 获取最后使用的重音设置
    val lastAccentFirstBeat: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[LAST_ACCENT_FIRST_BEAT] ?: true
    }
    
    // 获取最后使用的振动设置
    val lastEnableVibration: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[LAST_ENABLE_VIBRATION] ?: true
    }
    
    // 保存预设
    suspend fun savePreset(preset: MetronomePreset) {
        dataStore.edit { preferences ->
            val currentPresets = getPresetsFromString(preferences[SAVED_PRESETS] ?: "")
            val updatedPresets = currentPresets + preset
            preferences[SAVED_PRESETS] = Json.encodeToString(updatedPresets)
        }
    }
    
    // 删除预设
    suspend fun deletePreset(presetId: String) {
        dataStore.edit { preferences ->
            val currentPresets = getPresetsFromString(preferences[SAVED_PRESETS] ?: "")
            val updatedPresets = currentPresets.filter { it.id != presetId }
            preferences[SAVED_PRESETS] = Json.encodeToString(updatedPresets)
        }
    }
    
    // 获取所有预设
    val savedPresets: Flow<List<MetronomePreset>> = dataStore.data.map { preferences ->
        getPresetsFromString(preferences[SAVED_PRESETS] ?: "")
    }
    
    private fun getPresetsFromString(json: String): List<MetronomePreset> {
        return try {
            if (json.isEmpty()) emptyList()
            else Json.decodeFromString<List<MetronomePreset>>(json)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
