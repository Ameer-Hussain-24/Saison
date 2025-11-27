package takagi.ru.saison.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import takagi.ru.saison.data.local.datastore.PreferencesManager
import takagi.ru.saison.notification.*
import javax.inject.Singleton

/**
 * 通知系统依赖注入模块
 */
@Module
@InstallIn(SingletonComponent::class)
object NotificationModule {
    
    @Provides
    @Singleton
    fun provideNotificationChannelManager(
        @ApplicationContext context: Context
    ): NotificationChannelManager {
        return NotificationChannelManager(context)
    }
    
    @Provides
    @Singleton
    fun provideNotificationPermissionManager(
        @ApplicationContext context: Context
    ): NotificationPermissionManager {
        return NotificationPermissionManager(context)
    }
    
    @Provides
    @Singleton
    fun provideNotificationBuilder(
        @ApplicationContext context: Context
    ): NotificationBuilder {
        return NotificationBuilder(context)
    }
    
    @Provides
    @Singleton
    fun provideNotificationScheduler(
        @ApplicationContext context: Context
    ): NotificationScheduler {
        return NotificationScheduler(context)
    }
    
    @Provides
    @Singleton
    fun provideSaisonNotificationManager(
        @ApplicationContext context: Context,
        channelManager: NotificationChannelManager,
        permissionManager: NotificationPermissionManager,
        notificationBuilder: NotificationBuilder,
        scheduler: NotificationScheduler,
        preferencesManager: PreferencesManager
    ): SaisonNotificationManager {
        return SaisonNotificationManager(
            context,
            channelManager,
            permissionManager,
            notificationBuilder,
            scheduler,
            preferencesManager
        )
    }
    
    @Provides
    @Singleton
    fun provideNotificationNavigationHandler(
        @ApplicationContext context: Context
    ): NotificationNavigationHandler {
        return NotificationNavigationHandler(context)
    }
}
