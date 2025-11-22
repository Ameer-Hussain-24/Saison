package takagi.ru.saison.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/**
 * 测试PreferencesManager的数据迁移逻辑
 * 
 * 验证从旧的periodsPerDay配置迁移到新的分时段配置
 */
@RunWith(AndroidJUnit4::class)
class PreferencesManagerMigrationTest {
    
    private lateinit var context: Context
    private lateinit var preferencesManager: PreferencesManager
    
    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        preferencesManager = PreferencesManager(context)
        
        // 清理测试数据
        clearDataStore()
    }
    
    @After
    fun tearDown() {
        clearDataStore()
    }
    
    private fun clearDataStore() {
        runBlocking {
            val dataStoreFile = File(context.filesDir, "datastore/saison_preferences.preferences_pb")
            if (dataStoreFile.exists()) {
                dataStoreFile.delete()
            }
        }
    }
    
    @Test
    fun `migration from periodsPerDay 8 to morning 4 afternoon 4 evening 0`() = runBlocking {
        // 模拟旧数据：只有periodsPerDay=8
        val oldPeriodsPerDayKey = intPreferencesKey("course_periods_per_day")
        context.dataStore.edit { preferences ->
            preferences[oldPeriodsPerDayKey] = 8
        }
        
        // 读取设置，应该触发迁移
        val settings = preferencesManager.courseSettings.first()
        
        // 验证迁移结果
        assertEquals(4, settings.morningPeriods)
        assertEquals(4, settings.afternoonPeriods)
        assertEquals(0, settings.eveningPeriods)
        assertEquals(8, settings.totalPeriods)
    }
    
    @Test
    fun `migration from periodsPerDay 10 to morning 4 afternoon 4 evening 2`() = runBlocking {
        // 模拟旧数据：只有periodsPerDay=10
        val oldPeriodsPerDayKey = intPreferencesKey("course_periods_per_day")
        context.dataStore.edit { preferences ->
            preferences[oldPeriodsPerDayKey] = 10
        }
        
        // 读取设置，应该触发迁移
        val settings = preferencesManager.courseSettings.first()
        
        // 验证迁移结果
        assertEquals(4, settings.morningPeriods)
        assertEquals(4, settings.afternoonPeriods)
        assertEquals(2, settings.eveningPeriods)
        assertEquals(10, settings.totalPeriods)
    }
    
    @Test
    fun `migration from periodsPerDay 6 to morning 4 afternoon 2 evening 0`() = runBlocking {
        // 模拟旧数据：只有periodsPerDay=6
        val oldPeriodsPerDayKey = intPreferencesKey("course_periods_per_day")
        context.dataStore.edit { preferences ->
            preferences[oldPeriodsPerDayKey] = 6
        }
        
        // 读取设置，应该触发迁移
        val settings = preferencesManager.courseSettings.first()
        
        // 验证迁移结果
        assertEquals(4, settings.morningPeriods)
        assertEquals(2, settings.afternoonPeriods)
        assertEquals(0, settings.eveningPeriods)
        assertEquals(6, settings.totalPeriods)
    }
    
    @Test
    fun `migration from periodsPerDay 12 to morning 4 afternoon 4 evening 3`() = runBlocking {
        // 模拟旧数据：只有periodsPerDay=12（超过10节）
        val oldPeriodsPerDayKey = intPreferencesKey("course_periods_per_day")
        context.dataStore.edit { preferences ->
            preferences[oldPeriodsPerDayKey] = 12
        }
        
        // 读取设置，应该触发迁移
        val settings = preferencesManager.courseSettings.first()
        
        // 验证迁移结果：晚上节次最多3节
        assertEquals(4, settings.morningPeriods)
        assertEquals(4, settings.afternoonPeriods)
        assertEquals(3, settings.eveningPeriods)
        assertEquals(11, settings.totalPeriods)  // 注意：总数被限制为11
    }
    
    @Test
    fun `no migration when new fields exist`() = runBlocking {
        // 模拟新数据：已经有分时段配置
        val morningKey = intPreferencesKey("course_morning_periods")
        val afternoonKey = intPreferencesKey("course_afternoon_periods")
        val eveningKey = intPreferencesKey("course_evening_periods")
        
        context.dataStore.edit { preferences ->
            preferences[morningKey] = 5
            preferences[afternoonKey] = 3
            preferences[eveningKey] = 1
        }
        
        // 读取设置，不应该触发迁移
        val settings = preferencesManager.courseSettings.first()
        
        // 验证使用新字段的值
        assertEquals(5, settings.morningPeriods)
        assertEquals(3, settings.afternoonPeriods)
        assertEquals(1, settings.eveningPeriods)
        assertEquals(9, settings.totalPeriods)
    }
    
    @Test
    fun `setCourseSettings saves new fields and removes old fields`() = runBlocking {
        // 先设置旧数据
        val oldPeriodsPerDayKey = intPreferencesKey("course_periods_per_day")
        val oldTimelineCompactnessKey = floatPreferencesKey("course_timeline_compactness")
        context.dataStore.edit { preferences ->
            preferences[oldPeriodsPerDayKey] = 8
            preferences[oldTimelineCompactnessKey] = 1.5f
        }
        
        // 保存新设置
        val newSettings = takagi.ru.saison.domain.model.CourseSettings(
            morningPeriods = 4,
            afternoonPeriods = 4,
            eveningPeriods = 1
        )
        preferencesManager.setCourseSettings(newSettings)
        
        // 读取设置
        val settings = preferencesManager.courseSettings.first()
        
        // 验证新字段已保存
        assertEquals(4, settings.morningPeriods)
        assertEquals(4, settings.afternoonPeriods)
        assertEquals(1, settings.eveningPeriods)
        
        // 验证旧字段已被清理（通过检查DataStore）
        val preferences = context.dataStore.data.first()
        assertFalse(preferences.contains(oldPeriodsPerDayKey))
        assertFalse(preferences.contains(oldTimelineCompactnessKey))
    }
    
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "saison_preferences")
}
