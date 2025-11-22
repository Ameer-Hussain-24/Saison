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
object DataModule {
    
    @Provides
    @Singleton
    fun provideKeystoreHelper(): KeystoreHelper {
        return KeystoreHelper()
    }
    
    @Provides
    @Singleton
    fun provideEncryptionManager(
        keystoreHelper: KeystoreHelper
    ): EncryptionManager {
        return EncryptionManager(keystoreHelper)
    }
    
    @Provides
    @Singleton
    fun providePreferencesManager(
        @ApplicationContext context: Context
    ): PreferencesManager {
        return PreferencesManager(context)
    }
}
