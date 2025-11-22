package takagi.ru.saison.ui.screens.course

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import takagi.ru.saison.data.local.datastore.PreferencesManager
import takagi.ru.saison.data.repository.CourseRepository
import takagi.ru.saison.data.repository.SemesterRepository
import takagi.ru.saison.domain.model.Course
import takagi.ru.saison.domain.model.CourseSettings
import takagi.ru.saison.domain.repository.CourseSettingsRepository
import takagi.ru.saison.domain.usecase.IcsExportUseCase
import takagi.ru.saison.util.WeekCalculator
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

@OptIn(ExperimentalCoroutinesApi::class)
class CourseViewModelTest {

    private lateinit var viewModel: CourseViewModel
    private val courseRepository: CourseRepository = mockk()
    private val courseSettingsRepository: CourseSettingsRepository = mockk()
    private val weekCalculator: WeekCalculator = mockk()
    private val semesterRepository: SemesterRepository = mockk()
    private val preferencesManager: PreferencesManager = mockk()
    private val icsExportUseCase: IcsExportUseCase = mockk()

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        // Mock default behaviors
        every { courseSettingsRepository.getSettings() } returns flowOf(CourseSettings())
        coEvery { preferencesManager.getCurrentSemesterId() } returns 1L
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `allCourses emits courses from repository`() = runTest {
        val courses = listOf(
            Course(
                id = 1,
                name = "Test Course",
                color = 0,
                dayOfWeek = DayOfWeek.MONDAY,
                startTime = LocalTime.of(8, 0),
                endTime = LocalTime.of(9, 40),
                startDate = LocalDate.now(),
                endDate = LocalDate.now()
            )
        )
        
        // Mock repository to return flow of courses
        every { courseRepository.getCoursesBySemester(1L) } returns flowOf(courses)
        
        // Initialize ViewModel
        viewModel = CourseViewModel(
            courseRepository,
            courseSettingsRepository,
            weekCalculator,
            semesterRepository,
            preferencesManager,
            icsExportUseCase
        )
        
        val result = mutableListOf<List<Course>>()
        val job = launch {
            viewModel.allCourses.collect { result.add(it) }
        }
        
        advanceUntilIdle()
        
        // Check if we have the courses
        // The flow might emit emptyList first (initialValue), then the courses
        assert(result.last() == courses)
        
        job.cancel()
    }
}
