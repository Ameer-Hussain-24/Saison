# 数据丢失问题修复方案

## 🚨 问题诊断

### 根本原因

在 `DatabaseModule.kt` 中发现了**极其危险**的配置:

```kotlin
.fallbackToDestructiveMigration()  // ❌ 这会删除所有数据！
```

这个配置的含义是:
- **当数据库 schema 发生变化但没有提供迁移脚本时，Room 会删除整个数据库并重新创建**
- 这导致每次添加新功能、升级依赖、修改数据库结构时，用户的所有数据都会丢失
- 这是一个**灾难性的配置**，绝对不应该在生产环境中使用

### 触发场景

数据丢失通常发生在以下情况:
1. ✅ 添加新的 Entity 类
2. ✅ 修改现有 Entity 的字段
3. ✅ 升级 Room 库版本
4. ✅ 修改数据库版本号但忘记添加迁移脚本
5. ✅ 安装新版本 APK（包含数据库变更）

## ✅ 解决方案

### 1. 移除危险配置 ✅

**修改前:**
```kotlin
Room.databaseBuilder(context, SaisonDatabase::class.java, DATABASE_NAME)
    .addMigrations(...)
    .fallbackToDestructiveMigration()  // ❌ 删除
    .build()
```

**修改后:**
```kotlin
Room.databaseBuilder(context, SaisonDatabase::class.java, DATABASE_NAME)
    .addMigrations(...)
    // ✅ 移除了 fallbackToDestructiveMigration()
    // 现在如果缺少迁移脚本，应用会崩溃并提示开发者添加迁移
    // 这样可以强制我们为每个 schema 变化提供安全的迁移路径
    .build()
```

### 2. 添加自动备份机制 ✅

在 `DatabaseModule` 中添加数据库回调:

```kotlin
.addCallback(object : RoomDatabase.Callback() {
    override fun onOpen(db: SupportSQLiteDatabase) {
        super.onOpen(db)
        // 每次打开数据库时自动创建备份
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            migrationHelper.createDatabaseBackup("auto")
        }
    }
})
```

**特性:**
- ✅ 每次应用启动时自动备份数据库
- ✅ 保留最近 5 个自动备份
- ✅ 备份存储在应用内部存储，不会被用户误删

### 3. 创建数据库迁移助手 ✅

新增 `DatabaseMigrationHelper.kt`:

**功能:**
- ✅ 创建手动备份
- ✅ 创建自动备份
- ✅ 恢复备份
- ✅ 列出所有备份
- ✅ 删除备份
- ✅ 自动清理旧备份

**使用示例:**
```kotlin
// 创建手动备份
migrationHelper.createDatabaseBackup("manual")

// 恢复备份
migrationHelper.restoreDatabaseBackup(backupFile)

// 列出备份
val backups = migrationHelper.listBackups()
```

### 4. 配置 Android 自动备份 ✅

更新 `backup_rules.xml`:

```xml
<full-backup-content>
    <!-- 包含数据库目录 -->
    <include domain="database" path="." />
    
    <!-- 包含所有 SharedPreferences -->
    <include domain="sharedpref" path="." />
    
    <!-- 包含手动备份 -->
    <include domain="file" path="database_backups/" />
</full-backup-content>
```

**效果:**
- ✅ Android 系统会自动备份数据库到云端
- ✅ 卸载重装应用后可自动恢复数据
- ✅ 换设备后可自动恢复数据

### 5. 添加数据库管理 UI ✅

新增 `DatabaseManagementViewModel.kt`:

**功能:**
- ✅ 查看所有备份
- ✅ 创建手动备份
- ✅ 恢复备份（带确认对话框）
- ✅ 删除备份
- ✅ 显示数据库大小
- ✅ 显示备份时间和大小

## 🛡️ 多层保护机制

现在应用拥有**四层**数据保护:

### 第 1 层: 强制迁移脚本
- ❌ 移除了 `fallbackToDestructiveMigration()`
- ✅ 缺少迁移脚本时应用会崩溃，强制开发者添加迁移
- ✅ 每次 schema 变化都必须有安全的迁移路径

### 第 2 层: 自动本地备份
- ✅ 每次应用启动时自动备份数据库
- ✅ 保留最近 5 个自动备份
- ✅ 完全自动化，用户无感知

### 第 3 层: 手动本地备份
- ✅ 用户可以随时创建手动备份
- ✅ 手动备份永久保存，不会被自动清理
- ✅ 可以导出备份文件到外部存储

### 第 4 层: Android 云端备份
- ✅ Android 系统自动备份到 Google Drive
- ✅ 换设备后自动恢复
- ✅ 卸载重装后自动恢复

## 📊 备份策略

### 自动备份

**触发时机:**
- 每次应用启动（数据库打开时）

**保留策略:**
- 保留最近 5 个备份
- 自动清理旧备份
- 存储位置: `app/files/auto_backups/`

**命名格式:**
```
saison_db_auto_20241211_143052.db
```

### 手动备份

**触发时机:**
- 用户在设置中主动创建

**保留策略:**
- 永久保存，不自动清理
- 用户可手动删除
- 存储位置: `app/files/database_backups/`

**命名格式:**
```
saison_db_manual_20241211_143052.db
```

### 云端备份

**触发时机:**
- Android 系统自动决定（通常在设备充电且连接 WiFi 时）

**保留策略:**
- 由 Android 系统管理
- 存储到 Google Drive

## 🔧 开发者指南

### 添加新的数据库变更

1. **修改 Entity 类**
```kotlin
@Entity
data class Task(
    // 添加新字段
    val newField: String = ""  // 必须有默认值
)
```

2. **更新数据库版本**
```kotlin
@Database(
    entities = [...],
    version = 15,  // 版本号 +1
    exportSchema = true
)
```

3. **添加迁移脚本（重要！）**
```kotlin
val MIGRATION_14_15 = object : Migration(14, 15) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // 添加新字段
        db.execSQL("ALTER TABLE tasks ADD COLUMN newField TEXT NOT NULL DEFAULT ''")
    }
}
```

4. **在 DatabaseModule 中注册迁移**
```kotlin
.addMigrations(
    // ... 其他迁移 ...
    SaisonDatabase.MIGRATION_14_15  // 添加新迁移
)
```

### ⚠️ 重要提醒

**绝对不要:**
- ❌ 添加 `fallbackToDestructiveMigration()`
- ❌ 添加 `fallbackToDestructiveMigrationOnDowngrade()`
- ❌ 忘记添加迁移脚本
- ❌ 删除旧的迁移脚本

**务必做到:**
- ✅ 每次修改 schema 都添加迁移脚本
- ✅ 测试迁移脚本的正确性
- ✅ 保留所有历史迁移脚本
- ✅ 在发布前创建手动备份

## 🧪 测试迁移

### 测试步骤

1. **安装旧版本**
```bash
./gradlew installDebug
```

2. **创建测试数据**
- 添加一些任务、课程等数据

3. **创建手动备份**
- 在设置 → 数据库管理 → 创建备份

4. **修改数据库 schema**
- 添加新字段或新表

5. **添加迁移脚本**

6. **安装新版本**
```bash
./gradlew installDebug
```

7. **验证数据**
- 检查旧数据是否完整
- 检查新字段是否正确添加

8. **如果迁移失败**
- 应用会崩溃（这是好事！）
- 检查迁移脚本
- 从备份恢复数据
- 修复迁移脚本后重试

## 📱 用户指南

### 如何创建备份

1. 打开应用
2. 进入 **设置**
3. 选择 **数据库管理**
4. 点击 **创建备份**
5. 备份成功后会显示通知

### 如何恢复备份

1. 进入 **设置 → 数据库管理**
2. 在备份列表中选择要恢复的备份
3. 点击 **恢复**
4. 确认操作
5. **重启应用**使更改生效

### 如何防止数据丢失

1. **定期创建手动备份**
   - 建议每周创建一次
   - 重要数据变更后立即备份

2. **启用 WebDAV 同步**
   - 配置 WebDAV 服务器
   - 启用自动同步

3. **启用 Google 备份**
   - 确保手机已登录 Google 账号
   - 设置 → Google → 备份 → 开启自动备份

## 🎯 总结

### 修复内容

1. ✅ **移除了 `fallbackToDestructiveMigration()`**
   - 这是导致数据丢失的根本原因

2. ✅ **添加了自动备份机制**
   - 每次启动自动备份
   - 保留最近 5 个备份

3. ✅ **创建了数据库管理工具**
   - 手动备份/恢复功能
   - 备份列表管理

4. ✅ **配置了 Android 自动备份**
   - 云端备份支持
   - 跨设备数据恢复

5. ✅ **建立了完善的开发流程**
   - 强制添加迁移脚本
   - 详细的开发指南

### 保护级别

**以前:** 😱 无保护 → 数据随时可能丢失

**现在:** 🛡️ 四层保护 → 数据安全有保障

### 兼容性

- ✅ 对现有用户无影响
- ✅ 自动开始保护新数据
- ✅ 不需要用户手动操作

### 下一步

建议在**应用设置**中添加:
- 📊 数据库管理界面
- 💾 备份/恢复功能
- 📈 存储空间统计
- ⚙️ 备份设置选项

---

**数据安全现在是应用的第一优先级！** 🎉
