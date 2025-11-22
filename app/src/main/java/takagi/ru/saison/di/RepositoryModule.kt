package takagi.ru.saison.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import takagi.ru.saison.data.repository.CourseSettingsRepositoryImpl
import takagi.ru.saison.data.repository.EventRepositoryImpl
import takagi.ru.saison.data.repository.RoutineRepository
import takagi.ru.saison.data.repository.RoutineRepositoryImpl
import takagi.ru.saison.data.repository.SemesterRepository
import takagi.ru.saison.data.repository.SemesterRepositoryImpl
import takagi.ru.saison.domain.repository.CourseSettingsRepository
import takagi.ru.saison.domain.repository.EventRepository
import takagi.ru.saison.util.CycleCalculator
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    @Binds
    @Singleton
    abstract fun bindEventRepository(
        eventRepositoryImpl: EventRepositoryImpl
    ): EventRepository
    
    @Binds
    @Singleton
    abstract fun bindRoutineRepository(
        routineRepositoryImpl: RoutineRepositoryImpl
    ): RoutineRepository
    
    @Binds
    @Singleton
    abstract fun bindCourseSettingsRepository(
        courseSettingsRepositoryImpl: CourseSettingsRepositoryImpl
    ): CourseSettingsRepository
    
    @Binds
    @Singleton
    abstract fun bindSemesterRepository(
        semesterRepositoryImpl: SemesterRepositoryImpl
    ): SemesterRepository
    
    companion object {
        @Provides
        @Singleton
        fun provideCycleCalculator(): CycleCalculator {
            return CycleCalculator()
        }
    }
}
