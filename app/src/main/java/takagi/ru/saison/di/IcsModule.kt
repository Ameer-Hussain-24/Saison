package takagi.ru.saison.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import takagi.ru.saison.data.ics.IcsGenerator
import takagi.ru.saison.data.ics.IcsParser
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object IcsModule {
    
    @Provides
    @Singleton
    fun provideIcsParser(): IcsParser {
        return IcsParser()
    }
    
    @Provides
    @Singleton
    fun provideIcsGenerator(): IcsGenerator {
        return IcsGenerator()
    }
}
