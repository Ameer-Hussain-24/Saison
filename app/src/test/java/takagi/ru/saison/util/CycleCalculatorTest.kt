package takagi.ru.saison.util

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import takagi.ru.saison.domain.model.routine.CycleConfig
import takagi.ru.saison.domain.model.routine.CycleType
import takagi.ru.saison.domain.model.routine.RoutineTask
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime

class CycleCalculatorTest {
    
    private lateinit var calculator: CycleCalculator
    
    @Before
    fun setup() {
        calculator = CycleCalculator()
    }
    
    @Test
    fun `daily task is always active`() {
        // Given: 每日任务
        val task = createDailyTask()
        
        // When & Then: 任何日期都应该是活跃的
        val today = LocalDate.of(2024, 11, 1)
        assertTrue(calculator.isInActiveCycle(task, today))
        
        val tomorrow = today.plusDays(1)
        assertTrue(calculator.isInActiveCycle(task, tomorrow))
        
        val nextWeek = today.plusWeeks(1)
        assertTrue(calculator.isInActiveCycle(task, nextWeek))
    }
    
    @Test
    fun `weekly task is active on configured days`() {
        // Given: 每周一、三、五的任务
        val task = createWeeklyTask(listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY))
        
        // When & Then: 周一应该是活跃的
        val monday = LocalDate.of(2024, 11, 4) // 2024-11-04 是周一
        assertTrue(calculator.isInActiveCycle(task, monday))
        
        // When & Then: 周三应该是活跃的
        val wednesday = LocalDate.of(2024, 11, 6)
        assertTrue(calculator.isInActiveCycle(task, wednesday))
        
        // When & Then: 周五应该是活跃的
        val friday = LocalDate.of(2024, 11, 8)
        assertTrue(calculator.isInActiveCycle(task, friday))
        
        // When & Then: 周二不应该是活跃的
        val tuesday = LocalDate.of(2024, 11, 5)
        assertFalse(calculator.isInActiveCycle(task, tuesday))
        
        // When & Then: 周日不应该是活跃的
        val sunday = LocalDate.of(2024, 11, 10)
        assertFalse(calculator.isInActiveCycle(task, sunday))
    }
    
    @Test
    fun `monthly task is active on configured dates`() {
        // Given: 每月 1 日和 15 日的任务
        val task = createMonthlyTask(listOf(1, 15))
        
        // When & Then: 1 号应该是活跃的
        val firstDay = LocalDate.of(2024, 11, 1)
        assertTrue(calculator.isInActiveCycle(task, firstDay))
        
        // When & Then: 15 号应该是活跃的
        val fifteenthDay = LocalDate.of(2024, 11, 15)
        assertTrue(calculator.isInActiveCycle(task, fifteenthDay))
        
        // When & Then: 2 号不应该是活跃的
        val secondDay = LocalDate.of(2024, 11, 2)
        assertFalse(calculator.isInActiveCycle(task, secondDay))
        
        // When & Then: 30 号不应该是活跃的
        val thirtiethDay = LocalDate.of(2024, 11, 30)
        assertFalse(calculator.isInActiveCycle(task, thirtiethDay))
    }
    
    @Test
    fun `get current cycle for daily task`() {
        // Given: 每日任务
        val task = createDailyTask()
        val date = LocalDate.of(2024, 11, 1)
        
        // When: 获取当前周期
        val cycle = calculator.getCurrentCycle(task, date)
        
        // Then: 周期应该是当天
        assertNotNull(cycle)
        assertEquals(date, cycle!!.first)
        assertEquals(date, cycle.second)
    }
    
    @Test
    fun `get current cycle for weekly task`() {
        // Given: 每周任务
        val task = createWeeklyTask(listOf(DayOfWeek.MONDAY))
        val wednesday = LocalDate.of(2024, 11, 6) // 2024-11-06 是周三
        
        // When: 获取当前周期
        val cycle = calculator.getCurrentCycle(task, wednesday)
        
        // Then: 周期应该是当周的周一到周日
        assertNotNull(cycle)
        assertEquals(LocalDate.of(2024, 11, 4), cycle!!.first) // 周一
        assertEquals(LocalDate.of(2024, 11, 10), cycle.second) // 周日
    }
    
    @Test
    fun `get current cycle for monthly task`() {
        // Given: 每月任务
        val task = createMonthlyTask(listOf(1, 15))
        val date = LocalDate.of(2024, 11, 15)
        
        // When: 获取当前周期
        val cycle = calculator.getCurrentCycle(task, date)
        
        // Then: 周期应该是当月 1 日到月末
        assertNotNull(cycle)
        assertEquals(LocalDate.of(2024, 11, 1), cycle!!.first)
        assertEquals(LocalDate.of(2024, 11, 30), cycle.second)
    }
    
    @Test
    fun `get next active date for weekly task`() {
        // Given: 每周一、五的任务
        val task = createWeeklyTask(listOf(DayOfWeek.MONDAY, DayOfWeek.FRIDAY))
        
        // When: 从周二查找下一个活跃日期
        val tuesday = LocalDate.of(2024, 11, 5) // 2024-11-05 是周二
        val nextActive = calculator.getNextActiveDate(task, tuesday)
        
        // Then: 应该是周五
        assertNotNull(nextActive)
        assertEquals(LocalDate.of(2024, 11, 8), nextActive) // 周五
    }
    
    @Test
    fun `get next active date for monthly task in same month`() {
        // Given: 每月 1、15、30 日的任务
        val task = createMonthlyTask(listOf(1, 15, 30))
        
        // When: 从 10 号查找下一个活跃日期
        val date = LocalDate.of(2024, 11, 10)
        val nextActive = calculator.getNextActiveDate(task, date)
        
        // Then: 应该是 15 号
        assertNotNull(nextActive)
        assertEquals(LocalDate.of(2024, 11, 15), nextActive)
    }
    
    @Test
    fun `get next active date for monthly task in next month`() {
        // Given: 每月 1、15 日的任务
        val task = createMonthlyTask(listOf(1, 15))
        
        // When: 从 20 号查找下一个活跃日期
        val date = LocalDate.of(2024, 11, 20)
        val nextActive = calculator.getNextActiveDate(task, date)
        
        // Then: 应该是下个月 1 号
        assertNotNull(nextActive)
        assertEquals(LocalDate.of(2024, 12, 1), nextActive)
    }
    
    @Test
    fun `cycle description formatting for daily task`() {
        // Given: 每日任务
        val task = createDailyTask()
        
        // When: 获取周期描述
        val description = calculator.getCycleDescription(task)
        
        // Then: 应该是"每日"
        assertEquals("每日", description)
    }
    
    @Test
    fun `cycle description formatting for weekly task`() {
        // Given: 每周一、三、五的任务
        val task = createWeeklyTask(listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY))
        
        // When: 获取周期描述
        val description = calculator.getCycleDescription(task)
        
        // Then: 应该包含星期几
        assertEquals("每周一、周三、周五", description)
    }
    
    @Test
    fun `cycle description formatting for monthly task`() {
        // Given: 每月 1、15 日的任务
        val task = createMonthlyTask(listOf(1, 15))
        
        // When: 获取周期描述
        val description = calculator.getCycleDescription(task)
        
        // Then: 应该包含日期
        assertEquals("每月1日、15日", description)
    }
    
    @Test
    fun `inactive task is never active`() {
        // Given: 非活跃的每日任务
        val task = createDailyTask().copy(isActive = false)
        
        // When & Then: 任何日期都不应该是活跃的
        val today = LocalDate.now()
        assertFalse(calculator.isInActiveCycle(task, today))
        
        // When & Then: 获取周期应该返回 null
        val cycle = calculator.getCurrentCycle(task, today)
        assertNull(cycle)
        
        // When & Then: 获取下次活跃日期应该返回 null
        val nextActive = calculator.getNextActiveDate(task, today)
        assertNull(nextActive)
    }
    
    // Helper methods
    
    private fun createDailyTask(): RoutineTask {
        return RoutineTask(
            id = 1,
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
    
    private fun createWeeklyTask(daysOfWeek: List<DayOfWeek>): RoutineTask {
        return RoutineTask(
            id = 2,
            title = "每周任务",
            description = null,
            icon = null,
            cycleType = CycleType.WEEKLY,
            cycleConfig = CycleConfig.Weekly(daysOfWeek),
            isActive = true,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
    }
    
    private fun createMonthlyTask(daysOfMonth: List<Int>): RoutineTask {
        return RoutineTask(
            id = 3,
            title = "每月任务",
            description = null,
            icon = null,
            cycleType = CycleType.MONTHLY,
            cycleConfig = CycleConfig.Monthly(daysOfMonth),
            isActive = true,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
    }
}
