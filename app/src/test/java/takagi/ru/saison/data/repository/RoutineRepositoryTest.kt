package takagi.ru.saison.data.repository

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import takagi.ru.saison.data.local.database.dao.CheckInRecordDao
import takagi.ru.saison.data.local.database.dao.RoutineTaskDao
import takagi.ru.saison.data.local.database.entity.CheckInRecordEntity
import takagi.ru.saison.data.local.database.entity.RoutineTaskEntity
import takagi.ru.saison.domain.model.routine.CycleConfig
import takagi.ru.saison.domain.model.routine.CycleType
import takagi.ru.saison.domain.model.routine.RoutineTask
import takagi.ru.saison.util.CycleCalculator
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime

class RoutineRepositoryTest {
    
    private lateinit var repository: RoutineRepositoryImpl
    private lateinit var routineTaskDao: RoutineTaskDao
    private lateinit var checkInRecordDao: CheckInRecordDao
    private lateinit var cycleCalculator: CycleCalculator
    
    @Before
    fun setup() {
        routineTaskDao = mock()
        checkInRecordDao = mock()
        cycleCalculator = CycleCalculator()
        
        repository = RoutineRepositoryImpl(
            routineTaskDao = routineTaskDao,
            checkInRecordDao = checkInRecordDao,
            cycleCalculator = cycleCalculator
        )
    }
    
    @Test
    fun `create routine task successfully`() = runTest {
        // Given
        val task = createDailyTask()
        val expectedId = 1L
        whenever(routineTaskDao.insert(any())).thenReturn(expectedId)
        
        // When
        val result = repository.createRoutineTask(task)
        
        // Then
        assertEquals(expectedId, result)
        verify(routineTaskDao).insert(any())
    }
    
    @Test
    fun `check in creates record with correct cycle`() = runTest {
        // Given
        val task = createDailyTask().copy(id = 1L)
        val taskEntity = createDailyTaskEntity(id = 1L)
        val expectedRecordId = 100L
        
        whenever(routineTaskDao.getById(1L)).thenReturn(taskEntity)
        whenever(checkInRecordDao.insert(any())).thenReturn(expectedRecordId)
        
        // When
        val result = repository.checkIn(taskId = 1L, note = "Test note")
        
        // Then
        assertNotNull(result)
        assertEquals(expectedRecordId, result.id)
        assertEquals(1L, result.routineTaskId)
        assertEquals("Test note", result.note)
        assertEquals(LocalDate.now(), result.cycleStartDate)
        assertEquals(LocalDate.now(), result.cycleEndDate)
        verify(checkInRecordDao).insert(any())
    }
    
    @Test
    fun `get check in count in current cycle`() = runTest {
        // Given
        val taskId = 1L
        val cycleStart = LocalDate.of(2024, 11, 1)
        val cycleEnd = LocalDate.of(2024, 11, 30)
        val expectedCount = 5
        
        whenever(
            checkInRecordDao.getCountInCycle(
                taskId = taskId,
                cycleStart = cycleStart.toEpochDay(),
                cycleEnd = cycleEnd.toEpochDay()
            )
        ).thenReturn(expectedCount)
        
        // When
        val result = repository.getCheckInCountInCycle(taskId, cycleStart, cycleEnd)
        
        // Then
        assertEquals(expectedCount, result)
    }
    
    @Test
    fun `delete task cascades to check in records`() = runTest {
        // Given
        val taskId = 1L
        
        // When
        repository.deleteRoutineTask(taskId)
        
        // Then
        verify(routineTaskDao).deleteById(taskId)
        // 级联删除由数据库外键约束处理
    }
    
    @Test
    fun `get all routine tasks returns mapped domain models`() = runTest {
        // Given
        val entities = listOf(
            createDailyTaskEntity(id = 1L),
            createWeeklyTaskEntity(id = 2L)
        )
        whenever(routineTaskDao.getAllActive()).thenReturn(flowOf(entities))
        
        // When
        val result = repository.getAllRoutineTasks().first()
        
        // Then
        assertEquals(2, result.size)
        assertEquals(1L, result[0].id)
        assertEquals(2L, result[1].id)
    }
    
    // Helper methods
    
    private fun createDailyTask(): RoutineTask {
        return RoutineTask(
            id = 0,
            title = "每日任务",
            description = null,
            icon = null,
            cycleType = CycleType.DAILY,
            cycleConfig = CycleConfig.Daily(),
            isActive = true,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
    }
    
    private fun createDailyTaskEntity(id: Long): RoutineTaskEntity {
        val now = System.currentTimeMillis()
        return RoutineTaskEntity(
            id = id,
            title = "每日任务",
            description = null,
            icon = null,
            cycleType = "DAILY",
            cycleConfig = """{"type":"daily","time":null}""",
            isActive = true,
            createdAt = now,
            updatedAt = now
        )
    }
    
    private fun createWeeklyTaskEntity(id: Long): RoutineTaskEntity {
        val now = System.currentTimeMillis()
        return RoutineTaskEntity(
            id = id,
            title = "每周任务",
            description = null,
            icon = null,
            cycleType = "WEEKLY",
            cycleConfig = """{"type":"weekly","daysOfWeek":["MONDAY","FRIDAY"]}""",
            isActive = true,
            createdAt = now,
            updatedAt = now
        )
    }
}
