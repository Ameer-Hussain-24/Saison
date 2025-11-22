# 数据迁移实现总结

## 概述

实现了从旧的 `periodsPerDay` 配置到新的分时段配置（`morningPeriods`, `afternoonPeriods`, `eveningPeriods`）的数据迁移逻辑。

## 实现细节

### 1. 新增 Preference Keys

在 `PreferencesManager.PreferencesKeys` 中添加了新的键：

```kotlin
val COURSE_MORNING_PERIODS = intPreferencesKey("course_morning_periods")
val COURSE_AFTERNOON_PERIODS = intPreferencesKey("course_afternoon_periods")
val COURSE_EVENING_PERIODS = intPreferencesKey("course_evening_periods")
val COURSE_DINNER_BREAK_DURATION = intPreferencesKey("course_dinner_break_duration")
```

保留了旧的键用于迁移：
```kotlin
val COURSE_PERIODS_PER_DAY = intPreferencesKey("course_periods_per_day")  // 保留用于数据迁移
val COURSE_TIMELINE_COMPACTNESS = floatPreferencesKey("course_timeline_compactness")  // 保留用于数据迁移
```

### 2. 迁移逻辑

在 `courseSettings` Flow 中实现了自动迁移：

```kotlin
val courseSettings: Flow<CourseSettings> = dataStore.data
    .map { preferences ->
        val morningPeriods: Int
        val afternoonPeriods: Int
        val eveningPeriods: Int
        
        if (preferences[PreferencesKeys.COURSE_MORNING_PERIODS] != null) {
            // 如果存在新字段，直接使用
            morningPeriods = preferences[PreferencesKeys.COURSE_MORNING_PERIODS] ?: 4
            afternoonPeriods = preferences[PreferencesKeys.COURSE_AFTERNOON_PERIODS] ?: 4
            eveningPeriods = preferences[PreferencesKeys.COURSE_EVENING_PERIODS] ?: 0
        } else {
            // 否则从旧的 periodsPerDay 迁移
            val oldPeriodsPerDay = preferences[PreferencesKeys.COURSE_PERIODS_PER_DAY] ?: 8
            val migrated = migrateOldPeriodsPerDay(oldPeriodsPerDay)
            morningPeriods = migrated.first
            afternoonPeriods = migrated.second
            eveningPeriods = migrated.third
        }
        
        CourseSettings(
            periodsPerDay = morningPeriods + afternoonPeriods + eveningPeriods,
            morningPeriods = morningPeriods,
            afternoonPeriods = afternoonPeriods,
            eveningPeriods = eveningPeriods,
            // ... 其他字段
        )
    }
```

### 3. 迁移规则

`migrateOldPeriodsPerDay()` 方法实现了以下迁移规则：

| 旧的 periodsPerDay | 上午节次 | 下午节次 | 晚上节次 | 说明 |
|-------------------|---------|---------|---------|------|
| ≤ 8               | 4       | 剩余    | 0       | 无晚课 |
| 9-10              | 4       | 4       | 剩余    | 有晚课 |
| > 10              | 4       | 4       | min(剩余, 3) | 晚课最多3节 |

示例：
- `periodsPerDay=6` → `morning=4, afternoon=2, evening=0`
- `periodsPerDay=8` → `morning=4, afternoon=4, evening=0`
- `periodsPerDay=10` → `morning=4, afternoon=4, evening=2`
- `periodsPerDay=12` → `morning=4, afternoon=4, evening=3` (总数变为11)

### 4. 保存新设置时清理旧数据

在 `setCourseSettings()` 方法中，保存新设置时会清理旧字段：

```kotlin
suspend fun setCourseSettings(settings: CourseSettings) {
    dataStore.edit { preferences ->
        // 保存新的分时段配置
        preferences[PreferencesKeys.COURSE_MORNING_PERIODS] = settings.morningPeriods
        preferences[PreferencesKeys.COURSE_AFTERNOON_PERIODS] = settings.afternoonPeriods
        preferences[PreferencesKeys.COURSE_EVENING_PERIODS] = settings.eveningPeriods
        preferences[PreferencesKeys.COURSE_DINNER_BREAK_DURATION] = settings.dinnerBreakDuration
        
        // ... 保存其他字段
        
        // 清理旧数据
        preferences.remove(PreferencesKeys.COURSE_PERIODS_PER_DAY)
        preferences.remove(PreferencesKeys.COURSE_TIMELINE_COMPACTNESS)
    }
}
```

### 5. 向后兼容性

- **读取兼容**：首次读取时自动从旧字段迁移到新字段
- **写入清理**：保存新设置时自动清理旧字段
- **计算属性**：`CourseSettings.totalPeriods` 保持为计算属性，确保总节次数始终正确
- **默认值**：如果既没有旧字段也没有新字段，使用合理的默认值（上午4节，下午4节，晚上0节）

## 测试

创建了 `PreferencesManagerMigrationTest` 测试类，包含以下测试用例：

1. ✅ 从 `periodsPerDay=8` 迁移到 `morning=4, afternoon=4, evening=0`
2. ✅ 从 `periodsPerDay=10` 迁移到 `morning=4, afternoon=4, evening=2`
3. ✅ 从 `periodsPerDay=6` 迁移到 `morning=4, afternoon=2, evening=0`
4. ✅ 从 `periodsPerDay=12` 迁移到 `morning=4, afternoon=4, evening=3`
5. ✅ 当新字段存在时不触发迁移
6. ✅ 保存新设置时清理旧字段

## 迁移流程

```
用户启动应用
    ↓
PreferencesManager 读取 courseSettings
    ↓
检查是否存在新字段 (COURSE_MORNING_PERIODS)
    ↓
    ├─ 存在 → 直接使用新字段
    │
    └─ 不存在 → 读取旧字段 (COURSE_PERIODS_PER_DAY)
                    ↓
                调用 migrateOldPeriodsPerDay()
                    ↓
                返回迁移后的分时段配置
                    ↓
                构造 CourseSettings 对象
                    ↓
                用户修改设置并保存
                    ↓
                setCourseSettings() 保存新字段
                    ↓
                清理旧字段
                    ↓
                迁移完成
```

## 注意事项

1. **迁移是透明的**：用户无需手动操作，首次读取时自动完成
2. **单向迁移**：一旦保存新设置，旧字段将被删除，不可回退
3. **日志记录**：迁移过程会记录日志，便于调试
4. **线程安全**：使用 DataStore 的 Flow API，确保线程安全

## 相关文件

- `app/src/main/java/takagi/ru/saison/data/local/datastore/PreferencesManager.kt` - 迁移逻辑实现
- `app/src/main/java/takagi/ru/saison/domain/model/CourseSettings.kt` - 数据模型
- `app/src/test/java/takagi/ru/saison/data/local/datastore/PreferencesManagerMigrationTest.kt` - 迁移测试
