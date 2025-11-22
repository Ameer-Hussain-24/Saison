package takagi.ru.saison.util

import androidx.compose.ui.graphics.Color
import takagi.ru.saison.domain.model.Course
import takagi.ru.saison.domain.model.CoursePeriod
import takagi.ru.saison.domain.model.TimeMatchingStrategy
import takagi.ru.saison.domain.model.WeekPattern
import takagi.ru.saison.domain.model.ics.ParsedCourse
import takagi.ru.saison.domain.model.ics.RecurrenceInfo
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * 课程转换器
 * 负责在ParsedCourse和Course之间转换
 */
object CourseConverter {
    
    /**
     * 将ParsedCourse转换为Course（增强版，支持时间匹配策略）
     * @param parsed 解析后的课程数据
     * @param semesterId 目标学期ID
     * @param semesterStartDate 学期开始日期
     * @param primaryColor 主题色（用于颜色分配）
     * @param existingCourses 已存在的课程（用于颜色分配）
     * @param strategy 时间匹配策略
     * @param existingPeriods 现有节次（UseExistingPeriods时使用）
     * @param generatedPeriods 生成的节次（AutoCreatePeriods时使用）
     * @return Course对象
     */
    fun toCourse(
        parsed: ParsedCourse,
        semesterId: Long,
        semesterStartDate: LocalDate,
        primaryColor: Color,
        existingCourses: List<Course>,
        strategy: TimeMatchingStrategy = TimeMatchingStrategy.UseExistingPeriods,
        existingPeriods: List<CoursePeriod> = emptyList(),
        generatedPeriods: List<CoursePeriod> = emptyList()
    ): Course {
        val dayOfWeek = parsed.dtStart.dayOfWeek
        val startTime = parsed.dtStart.toLocalTime()
        val endTime = parsed.dtEnd.toLocalTime()
        val startDate = parsed.dtStart.toLocalDate()
        
        // 根据策略匹配节次
        android.util.Log.d("CourseConverter", "Converting course: ${parsed.summary}")
        android.util.Log.d("CourseConverter", "  Time: $startTime - $endTime")
        android.util.Log.d("CourseConverter", "  Strategy: $strategy")
        
        val (periodStart, periodEnd) = when (strategy) {
            is TimeMatchingStrategy.UseExistingPeriods -> {
                // 使用现有节次配置进行匹配
                android.util.Log.d("CourseConverter", "  Using existing periods (${existingPeriods.size} periods)")
                PeriodMatcher.matchPeriod(
                    startTime = startTime,
                    endTime = endTime,
                    description = parsed.description,
                    existingPeriods = existingPeriods
                )
            }
            is TimeMatchingStrategy.AutoCreatePeriods -> {
                // 使用生成的节次配置进行匹配
                android.util.Log.d("CourseConverter", "  Using generated periods (${generatedPeriods.size} periods)")
                PeriodMatcher.matchPeriod(
                    startTime = startTime,
                    endTime = endTime,
                    description = parsed.description,
                    existingPeriods = generatedPeriods
                )
            }
        }
        
        android.util.Log.d("CourseConverter", "  Matched periods: periodStart=$periodStart, periodEnd=$periodEnd")
        
        val isCustomTime = periodStart == null && periodEnd == null
        android.util.Log.d("CourseConverter", "  isCustomTime=$isCustomTime")
        
        // 当时间匹配失败时，记录警告日志
        if (isCustomTime) {
            android.util.Log.w("CourseConverter", "  WARNING: Failed to match periods for course '${parsed.summary}' at $startTime-$endTime, using custom time")
        }
        
        // 确定结束日期
        val endDate = parsed.rrule?.until?.toLocalDate() ?: startDate
        
        // 分配颜色
        val color = CourseColorAssigner.assignColor(
            existingCourses = existingCourses,
            dayOfWeek = dayOfWeek,
            startTime = startTime,
            endTime = endTime,
            primaryColor = primaryColor
        )
        
        return Course(
            name = parsed.summary.trim(),
            location = parsed.location?.trim(),
            color = color,
            semesterId = semesterId,
            dayOfWeek = dayOfWeek,
            startTime = startTime,
            endTime = endTime,
            weekPattern = WeekPattern.ALL,
            customWeeks = null,
            startDate = startDate,
            endDate = endDate,
            notificationMinutes = parsed.alarmMinutes ?: 10,
            autoSilent = true,
            periodStart = periodStart,
            periodEnd = periodEnd,
            isCustomTime = isCustomTime
        )
    }
    
    /**
     * 分析多个ParsedCourse，识别自定义周次模式
     * 将相同课程的多个VEVENT合并为一个Course
     */
    fun groupAndConvert(
        parsedList: List<ParsedCourse>,
        semesterId: Long,
        semesterStartDate: LocalDate,
        primaryColor: Color,
        strategy: TimeMatchingStrategy = TimeMatchingStrategy.UseExistingPeriods,
        existingPeriods: List<CoursePeriod> = emptyList(),
        generatedPeriods: List<CoursePeriod> = emptyList()
    ): List<Course> {
        // 按课程名称、时间和地点分组
        val grouped = parsedList.groupBy { parsed ->
            Triple(
                parsed.summary.trim(),
                parsed.dtStart.toLocalTime(),
                parsed.location?.trim()
            )
        }
        
        val courses = mutableListOf<Course>()
        
        grouped.forEach { (key, events) ->
            if (events.size == 1) {
                // 单个事件，也需要检测周次模式
                val (weekPattern, customWeeks) = detectWeekPattern(events, semesterStartDate)
                
                val course = toCourse(
                    parsed = events.first(),
                    semesterId = semesterId,
                    semesterStartDate = semesterStartDate,
                    primaryColor = primaryColor,
                    existingCourses = courses,
                    strategy = strategy,
                    existingPeriods = existingPeriods,
                    generatedPeriods = generatedPeriods
                ).copy(
                    weekPattern = weekPattern,
                    customWeeks = customWeeks
                )
                courses.add(course)
            } else {
                // 多个事件，识别周次模式
                val (weekPattern, customWeeks) = detectWeekPattern(events, semesterStartDate)
                
                // 使用第一个事件作为基础
                val baseParsed = events.first()
                val baseCourse = toCourse(
                    parsed = baseParsed,
                    semesterId = semesterId,
                    semesterStartDate = semesterStartDate,
                    primaryColor = primaryColor,
                    existingCourses = courses,
                    strategy = strategy,
                    existingPeriods = existingPeriods,
                    generatedPeriods = generatedPeriods
                )
                
                // 计算整体的开始和结束日期
                val allDates = events.flatMap { event ->
                    listOf(event.dtStart.toLocalDate(), event.rrule?.until?.toLocalDate() ?: event.dtStart.toLocalDate())
                }
                val overallStartDate = allDates.minOrNull() ?: baseCourse.startDate
                val overallEndDate = allDates.maxOrNull() ?: baseCourse.endDate
                
                // 更新课程的周次模式
                val course = baseCourse.copy(
                    weekPattern = weekPattern,
                    customWeeks = customWeeks,
                    startDate = overallStartDate,
                    endDate = overallEndDate
                )
                courses.add(course)
            }
        }
        
        return courses
    }
    
    /**
     * 检测周次模式
     */
    private fun detectWeekPattern(
        events: List<ParsedCourse>,
        semesterStartDate: LocalDate
    ): Pair<WeekPattern, List<Int>?> {
        // 收集所有周数
        val weeks = mutableSetOf<Int>()
        
        events.forEach { event ->
            val startDate = event.dtStart.toLocalDate()
            val endDate = event.rrule?.until?.toLocalDate() ?: startDate
            
            // 计算这个事件覆盖的周数
            var currentDate = startDate
            while (!currentDate.isAfter(endDate)) {
                val weekNumber = calculateWeekNumber(currentDate, semesterStartDate)
                if (weekNumber > 0) {
                    weeks.add(weekNumber)
                }
                currentDate = currentDate.plusWeeks(1)
            }
        }
        
        val sortedWeeks = weeks.sorted()
        
        // 分析模式
        if (sortedWeeks.isEmpty()) {
            return Pair(WeekPattern.ALL, null)
        }
        
        // 检查是否为单双周
        val allOdd = sortedWeeks.all { it % 2 == 1 }
        val allEven = sortedWeeks.all { it % 2 == 0 }
        
        return when {
            allOdd && sortedWeeks.size > 1 -> Pair(WeekPattern.ODD, null)
            allEven && sortedWeeks.size > 1 -> Pair(WeekPattern.EVEN, null)
            else -> Pair(WeekPattern.CUSTOM, sortedWeeks)
        }
    }
    
    /**
     * 计算周数
     * @param date 目标日期
     * @param semesterStartDate 学期开始日期
     * @return 周数（从1开始）
     */
    private fun calculateWeekNumber(date: LocalDate, semesterStartDate: LocalDate): Int {
        val days = ChronoUnit.DAYS.between(semesterStartDate, date)
        return (days / 7).toInt() + 1
    }
    
    /**
     * 从描述中提取节次信息
     * 支持格式: "第X-Y节", "第X节"
     */
    private fun extractPeriodInfo(description: String?): Pair<Int?, Int?> {
        if (description.isNullOrBlank()) {
            return Pair(null, null)
        }
        
        // 匹配 "第X-Y节" 或 "第X - Y节"
        val rangePattern = Regex("""第\s*(\d+)\s*-\s*(\d+)\s*节""")
        val rangeMatch = rangePattern.find(description)
        if (rangeMatch != null) {
            val start = rangeMatch.groupValues[1].toIntOrNull()
            val end = rangeMatch.groupValues[2].toIntOrNull()
            return Pair(start, end)
        }
        
        // 匹配 "第X节"
        val singlePattern = Regex("""第\s*(\d+)\s*节""")
        val singleMatch = singlePattern.find(description)
        if (singleMatch != null) {
            val period = singleMatch.groupValues[1].toIntOrNull()
            return Pair(period, period)
        }
        
        return Pair(null, null)
    }
}
