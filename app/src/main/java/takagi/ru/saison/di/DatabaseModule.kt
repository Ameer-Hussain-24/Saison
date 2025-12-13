package takagi.ru.saison.di

import android.content.Context
import android.util.Log
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import takagi.ru.saison.data.local.database.DatabaseMigrationHelper
import takagi.ru.saison.data.local.database.SaisonDatabase
import takagi.ru.saison.data.local.database.dao.*
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    private const val TAG = "DatabaseModule"
    
    @Provides
    @Singleton
    fun provideSaisonDatabase(
        @ApplicationContext context: Context,
        migrationHelper: DatabaseMigrationHelper
    ): SaisonDatabase {
        return Room.databaseBuilder(
            context,
            SaisonDatabase::class.java,
            SaisonDatabase.DATABASE_NAME
        )
            .addMigrations(
                SaisonDatabase.MIGRATION_1_2,
                SaisonDatabase.MIGRATION_2_3,
                SaisonDatabase.MIGRATION_3_4,
                SaisonDatabase.MIGRATION_4_5,
                SaisonDatabase.MIGRATION_5_6,
                SaisonDatabase.MIGRATION_6_7,
                SaisonDatabase.MIGRATION_7_8,
                SaisonDatabase.MIGRATION_8_9,
                SaisonDatabase.MIGRATION_9_10,
                SaisonDatabase.MIGRATION_10_11,
                SaisonDatabase.MIGRATION_11_12,
                SaisonDatabase.MIGRATION_12_13,
                SaisonDatabase.MIGRATION_13_14,
                SaisonDatabase.MIGRATION_14_15,
                SaisonDatabase.MIGRATION_15_16,
                SaisonDatabase.MIGRATION_16_17
            )
            // ⚠️ 不使用 fallbackToDestructiveMigration() 或 fallbackToDestructiveMigrationOnDowngrade()
            // 这些配置会在迁移失败或版本降级时删除整个数据库，导致用户数据永久丢失
            // 
            // 如果发生迁移错误（包括版本降级），应用会崩溃并提示用户：
            // 1. 通过设置 > 数据库管理 > 恢复备份 来恢复数据
            // 2. 应用在每次打开时都会自动创建备份（见下方回调）
            // 3. 手动备份功能也可用于重要数据保护
            //
            // 这样可以强制保护用户数据，即使在版本降级等异常情况下也不会丢失数据
            
            // 添加回调，在数据库打开时自动创建备份
            .addCallback(object : RoomDatabase.Callback() {
                override fun onOpen(db: SupportSQLiteDatabase) {
                    super.onOpen(db)
                    Log.d(TAG, "数据库已打开，版本: ${db.version}")
                    
                    // 在后台创建自动备份
                    CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
                        try {
                            migrationHelper.createDatabaseBackup("auto").onSuccess {
                                Log.d(TAG, "自动备份创建成功: ${it.name}")
                            }.onFailure {
                                Log.w(TAG, "自动备份创建失败", it)
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "自动备份异常", e)
                        }
                    }
                }
                
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    Log.d(TAG, "数据库首次创建，版本: ${db.version}")
                }
            })
            .build()
    }
    
    @Provides
    fun provideTaskDao(database: SaisonDatabase): TaskDao {
        return database.taskDao()
    }
    
    @Provides
    fun provideTagDao(database: SaisonDatabase): TagDao {
        return database.tagDao()
    }
    
    @Provides
    fun provideCourseDao(database: SaisonDatabase): CourseDao {
        return database.courseDao()
    }
    
    @Provides
    fun providePomodoroDao(database: SaisonDatabase): PomodoroDao {
        return database.pomodoroDao()
    }
    
    @Provides
    fun provideAttachmentDao(database: SaisonDatabase): AttachmentDao {
        return database.attachmentDao()
    }
    
    @Provides
    fun provideEventDao(database: SaisonDatabase): EventDao {
        return database.eventDao()
    }
    
    @Provides
    fun provideRoutineTaskDao(database: SaisonDatabase): RoutineTaskDao {
        return database.routineTaskDao()
    }
    
    @Provides
    fun provideCheckInRecordDao(database: SaisonDatabase): CheckInRecordDao {
        return database.checkInRecordDao()
    }
    
    @Provides
    fun provideSemesterDao(database: SaisonDatabase): SemesterDao {
        return database.semesterDao()
    }
    
    @Provides
    fun provideSubscriptionDao(database: SaisonDatabase): SubscriptionDao {
        return database.subscriptionDao()
    }
    
    @Provides
    fun provideSubscriptionHistoryDao(database: SaisonDatabase): SubscriptionHistoryDao {
        return database.subscriptionHistoryDao()
    }
    
    @Provides
    fun provideCategoryDao(database: SaisonDatabase): CategoryDao {
        return database.categoryDao()
    }
    
    @Provides
    fun provideValueDayDao(database: SaisonDatabase): ValueDayDao {
        return database.valueDayDao()
    }
}
