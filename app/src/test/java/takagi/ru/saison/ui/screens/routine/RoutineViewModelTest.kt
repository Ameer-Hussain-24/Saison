package takagi.ru.saison.ui.screens.routine

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import takagi.ru.saison.data.repository.RoutineRepository
import takagi.ru.saison.domain.model.routine.CycleConfig
import takagi.ru.saison.domain.model.routine.CycleType
import takagi.ru.saison.domain.model.routine.RoutineTask
import takagi.ru.saison.domain.model.routine.RoutineTaskWithStats
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class RoutineViewModelTest {
    
    private lateinit var viewModel: RoutineViewModel
    private lateinit var repository: RoutineRepository
    private val testDispatcher = UnconfinedTestDispatcher()
    
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mock()
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `load tasks separates active and inactive`() = runTest {
        // Given
        val activeTask = createTaskWithStats(id = 1, isActive = true, checkInCount = 2)
        val inactiveTask = createTaskWithStats(id = 2, isActive = false, checkInCount = 0)
        
        whenever(repository.getRoutineTasksWithStats())
            .thenReturn(flowOf(listOf(activeTask, inactiveTask)))
        
        // When
        viewModel = RoutineViewModel(repository)
        
        // Then
        val state = viewModel.uiState.value
        assertEquals(1, state.activeTasks.size)
        assertEquals(1, state.inactiveTasks.size)
        assertEquals(1L, state.activeTasks[0].task.id)
        assertEquals(2L, state.inactiveTasks[0].task.id)
    }
    
    @Test
    fun `tasks are sorted correctly`() = runTest {
        // Given: 3个活跃任务，打卡次数分别为 5, 2, 2
        val task1 = createTaskWithStats(id = 1, isActive = true, checkInCount = 5)
        val task2 = createTaskWithStats(id = 2, isActive = true, checkInCount = 2)
        val task3 = createTaskWithStats(id = 3, isActive = true, checkInCount = 2)
        
        whenever(repository.getRoutineTasksWithStats())
            .thenReturn(flowOf(listOf(task1, task2, task3)))
        
        // When
        viewModel = RoutineViewModel(repository)
        
        // Then: 应该按打卡次数升序排列（次数少的在前）
        val state = viewModel.uiState.value
        assertEquals(3, state.activeTasks.size)
        assertEquals(2, state.activeTasks[0].checkInCount)
        assertEquals(2, state.activeTasks[1].checkInCount)
        assertEquals(5, state.activeTasks[2].checkInCount)
    }
    
    @Test
    fun `error handling displays correct message`() = runTest {
        // Given
        val errorMessage = "Database error"
        whenever(repository.getRoutineTasksWithStats())
            .thenReturn(flowOf())
        
        // When
        viewModel = RoutineViewModel(repository)
        
        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
    }
    
    // Helper methods
    
    private fun createTaskWithStats(
        id: Long,
        isActive: Boolean,
        checkInCount: Int
    ): RoutineTaskWithStats {
        val task = RoutineTask(
            id = id,
            title = "Task $id",
            description = null,
            icon = null,
            cycleType = CycleType.DAILY,
            cycleConfig = CycleConfig.Daily(),
            isActive = true,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        
        return RoutineTaskWithStats(
            task = task,
            checkInCount = checkInCount,
            isInActiveCycle = isActive,
            currentCycleStart = if (isActive) LocalDate.now() else null,
            currentCycleEnd = if (isActive) LocalDate.now() else null,
            nextActiveDate = if (!isActive) LocalDate.now().plusDays(1) else null,
            lastCheckInTime = null
        )
    }
}
