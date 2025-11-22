package takagi.ru.saison.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import takagi.ru.saison.data.local.datastore.PreferencesManager
import takagi.ru.saison.data.local.encryption.EncryptionManager
import takagi.ru.saison.data.local.encryption.KeystoreHelper
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {
    // 注意：PreferencesManager, KeystoreHelper 和 EncryptionManager 
    // 已在 DataModule 中提供，这里不再重复定义
}
