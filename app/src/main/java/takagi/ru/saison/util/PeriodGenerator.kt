package takagi.ru.saison.util

import takagi.ru.saison.domain.model.BreakPeriod
import takagi.ru.saison.domain.model.CoursePeriod
import takagi.ru.saison.domain.model.TimeOfDay
import takagi.ru.saison.domain.model.ics.ParsedCourse
import java.time.LocalTime
import java.time.temporal.ChronoUnit

/**
 * 节次生成器
 * 从导入的课程中自动生成节次配置
 */
object PeriodGenerator {
    
    // 午休时间阈值（分钟）
    private const val LUNCH_BREAK_THRESHOLD_MINUTES = 60
    
    /**
     * 从统一配置生成节次列表
     * @param totalPeriods 总节次数
     * @param firstPeriodStartTime 第一节课开始时间
     * @param periodDuration 课程时长（分钟）
     * @param breakDuration 课间休息时长（分钟）
     * @param lunchBreakAfterPeriod 午休在第几节课后（可选）
     * @param lunchBreakDuration 午休时长（分钟）
     * @return Pair<节次列表, 休息时段列表>
     */
    fun generatePeriods(
        totalPeriods: Int,
        firstPeriodStartTime: LocalTime,
        periodDuration: Int,
        breakDuration: Int,
        lunchBreakAfterPeriod: Int? = null,
        lunchBreakDuration: Int = 90
    ): Pair<List<CoursePeriod>, List<BreakPeriod>> {
        val periods = mutableListOf<CoursePeriod>()
        val breakPeriods = mutableListOf<BreakPeriod>()
        var currentTime = firstPeriodStartTime
        
        for (periodNumber in 1..totalPeriods) {
            val startTime = currentTime
            val endTime = currentTime.plusMinutes(periodDuration.toLong())
            
            // 根据时间判断时段
            val timeOfDay = when {
                startTime.hour < 12 -> TimeOfDay.MORNING
                startTime.hour < 18 -> TimeOfDay.AFTERNOON
                else -> TimeOfDay.EVENING
            }
            
            // 判断是否在午休后
            val isAfterLunchBreak = lunchBreakAfterPeriod != null && periodNumber > lunchBreakAfterPeriod
            
            periods.add(
                CoursePeriod(
                    periodNumber = periodNumber,
                    startTime = startTime,
                    endTime = endTime,
                    timeOfDay = timeOfDay,
                    isAfterLunchBreak = isAfterLunchBreak
                )
            )
            
            // 添加休息时间
            currentTime = if (periodNumber < totalPeriods) {
                // 检查是否需要添加午休
                if (lunchBreakAfterPeriod != null && periodNumber == lunchBreakAfterPeriod) {
                    breakPeriods.add(BreakPeriod("午休", afterPeriod = periodNumber))
                    endTime.plusMinutes(lunchBreakDuration.toLong())
                } else {
                    // 普通课间休息
                    endTime.plusMinutes(breakDuration.toLong())
                }
            } else {
                endTime
            }
        }
        
        return Pair(periods, breakPeriods)
    }
    
    /**
     * 从时段配置生成节次列表（已弃用，保留用于向后兼容）
     * @deprecated 使用 generatePeriods(totalPeriods, ...) 代替
     */
    @Deprecated("Use generatePeriods(totalPeriods, ...) instead")
    fun generatePeriodsFromSegments(
        morningPeriods: Int,
        afternoonPeriods: Int,
        eveningPeriods: Int,
        firstPeriodStartTime: LocalTime,
        periodDuration: Int,
        breakDuration: Int,
        lunchBreakDuration: Int,
        dinnerBreakDuration: Int
    ): Pair<List<CoursePeriod>, List<BreakPeriod>> {
        val totalPeriods = morningPeriods + afternoonPeriods + eveningPeriods
        return generatePeriods(
            totalPeriods = totalPeriods,
            firstPeriodStartTime = firstPeriodStartTime,
            periodDuration = periodDuration,
            breakDuration = breakDuration,
            lunchBreakAfterPeriod = if (morningPeriods > 0) morningPeriods else null,
            lunchBreakDuration = lunchBreakDuration
        )
    }
    
    /**
     * 从解析的课程列表生成节次配置
     * @param parsedCourses 解析的课程列表
     * @return 生成的节次列表
     */
    fun generatePeriodsFromCourses(parsedCourses: List<ParsedCourse>): List<CoursePeriod> {
        if (parsedCourses.isEmpty()) {
            return emptyList()
        }
        
        // 1. 提取所有唯一的时间段
        val timeSlots = extractUniqueTimeSlots(parsedCourses)
        
        // 2. 按开始时间排序
        val sortedSlots = timeSlots.sortedBy { it.first }
        
        // 3. 识别午休时间
        val lunchBreakIndex = findLunchBreakIndex(sortedSlots)
        
        // 4. 分配节次编号并创建CoursePeriod对象
        return sortedSlots.mapIndexed { index, (startTime, endTime) ->
            val isAfterLunch = lunchBreakIndex != null && index >= lunchBreakIndex
            val timeOfDay = when {
                startTime.hour < 12 -> TimeOfDay.MORNING
                startTime.hour < 18 -> TimeOfDay.AFTERNOON
                else -> TimeOfDay.EVENING
            }
            
            CoursePeriod(
                periodNumber = index + 1,
                startTime = startTime,
                endTime = endTime,
                timeOfDay = timeOfDay,
                isAfterLunchBreak = isAfterLunch
            )
        }
    }
    
    /**
     * 提取唯一的时间段
     * 合并相同或重叠的时间段
     */
    private fun extractUniqueTimeSlots(parsedCourses: List<ParsedCourse>): List<Pair<LocalTime, LocalTime>> {
        val timeSlots = mutableListOf<Pair<LocalTime, LocalTime>>()
        
        for (course in parsedCourses) {
            val startTime = course.dtStart.toLocalTime()
            val endTime = course.dtEnd.toLocalTime()
            
            // 检查是否已存在相同或相似的时间段
            val existingSlot = timeSlots.find { (existingStart, existingEnd) ->
                isSimilarTimeSlot(startTime, endTime, existingStart, existingEnd)
            }
            
            if (existingSlot == null) {
                timeSlots.add(Pair(startTime, endTime))
            }
        }
        
        // 去重和合并重叠的时间段
        return mergeOverlappingSlots(timeSlots)
    }
    
    /**
     * 判断两个时间段是否相似（允许小的时间差异）
     * 允许5分钟的误差
     */
    private fun isSimilarTimeSlot(
        start1: LocalTime,
        end1: LocalTime,
        start2: LocalTime,
        end2: LocalTime
    ): Boolean {
        val startDiff = Math.abs(ChronoUnit.MINUTES.between(start1, start2))
        val endDiff = Math.abs(ChronoUnit.MINUTES.between(end1, end2))
        
        return startDiff <= 5 && endDiff <= 5
    }
    
    /**
     * 合并重叠的时间段
     */
    private fun mergeOverlappingSlots(
        slots: List<Pair<LocalTime, LocalTime>>
    ): List<Pair<LocalTime, LocalTime>> {
        if (slots.isEmpty()) return emptyList()
        
        val sortedSlots = slots.sortedBy { it.first }
        val merged = mutableListOf<Pair<LocalTime, LocalTime>>()
        
        var current = sortedSlots[0]
        
        for (i in 1 until sortedSlots.size) {
            val next = sortedSlots[i]
            
            // 检查是否重叠或相邻
            if (isOverlappingOrAdjacent(current, next)) {
                // 合并时间段
                current = Pair(
                    minOf(current.first, next.first),
                    maxOf(current.second, next.second)
                )
            } else {
                // 不重叠，保存当前时间段并开始新的
                merged.add(current)
                current = next
            }
        }
        
        // 添加最后一个时间段
        merged.add(current)
        
        return merged
    }
    
    /**
     * 判断两个时间段是否重叠或相邻（允许10分钟的间隔）
     */
    private fun isOverlappingOrAdjacent(
        slot1: Pair<LocalTime, LocalTime>,
        slot2: Pair<LocalTime, LocalTime>
    ): Boolean {
        val (start1, end1) = slot1
        val (start2, end2) = slot2
        
        // 检查是否重叠
        if (start2 <= end1 && start1 <= end2) {
            return true
        }
        
        // 检查是否相邻（间隔小于等于10分钟）
        val gap = ChronoUnit.MINUTES.between(end1, start2)
        return gap in 0..10
    }
    
    /**
     * 查找午休时间的索引
     * 返回午休后第一个节次的索引
     */
    private fun findLunchBreakIndex(sortedSlots: List<Pair<LocalTime, LocalTime>>): Int? {
        if (sortedSlots.size < 2) return null
        
        for (i in 0 until sortedSlots.size - 1) {
            val currentEnd = sortedSlots[i].second
            val nextStart = sortedSlots[i + 1].first
            
            val gapMinutes = ChronoUnit.MINUTES.between(currentEnd, nextStart)
            
            // 如果间隔超过阈值，认为是午休时间
            if (gapMinutes >= LUNCH_BREAK_THRESHOLD_MINUTES) {
                return i + 1
            }
        }
        
        return null
    }
    
    /**
     * 验证生成的节次配置是否合理
     * @param periods 节次列表
     * @return 验证结果和错误信息
     */
    fun validatePeriods(periods: List<CoursePeriod>): Pair<Boolean, String?> {
        if (periods.isEmpty()) {
            return Pair(false, "节次列表为空")
        }
        
        // 检查节次编号是否连续
        val sortedPeriods = periods.sortedBy { it.periodNumber }
        for (i in sortedPeriods.indices) {
            if (sortedPeriods[i].periodNumber != i + 1) {
                return Pair(false, "节次编号不连续")
            }
        }
        
        // 检查时间是否有重叠
        for (i in 0 until sortedPeriods.size - 1) {
            val current = sortedPeriods[i]
            val next = sortedPeriods[i + 1]
            
            if (current.endTime > next.startTime) {
                return Pair(false, "节次 ${current.periodNumber} 和 ${next.periodNumber} 的时间有重叠")
            }
        }
        
        // 检查每个节次的时间是否合理（至少5分钟）
        for (period in periods) {
            val duration = ChronoUnit.MINUTES.between(period.startTime, period.endTime)
            if (duration < 5) {
                return Pair(false, "节次 ${period.periodNumber} 的时长过短（少于5分钟）")
            }
        }
        
        return Pair(true, null)
    }
    
    /**
     * 生成节次配置的摘要信息
     * @param periods 节次列表
     * @return 摘要文本
     */
    fun generateSummary(periods: List<CoursePeriod>): String {
        if (periods.isEmpty()) {
            return "无节次配置"
        }
        
        val totalPeriods = periods.size
        val lunchBreakIndex = periods.indexOfFirst { it.isAfterLunchBreak }
        
        val morningPeriods = if (lunchBreakIndex > 0) lunchBreakIndex else totalPeriods
        val afternoonPeriods = if (lunchBreakIndex > 0) totalPeriods - lunchBreakIndex else 0
        
        return buildString {
            append("共 $totalPeriods 节课")
            if (afternoonPeriods > 0) {
                append("（上午 $morningPeriods 节，下午 $afternoonPeriods 节）")
            }
        }
    }
    
    /**
     * 从课程描述中分析并生成节次配置
     * 优先使用描述中的"第X-Y节"信息，如果没有则回退到基于时间段的生成
     * 增强版：支持混合模式（描述+时间），并优化节次时间计算
     */
    fun generatePeriodsFromDescriptions(parsedCourses: List<ParsedCourse>): List<CoursePeriod> {
        // 使用列表存储每个节次的所有可能时间段，以便后续计算众数或平均值
        val periodTimeSamples = mutableMapOf<Int, MutableList<Pair<LocalTime, LocalTime>>>()
        
        // 扩展的正则匹配，支持更多格式
        val rangePatterns = listOf(
            Regex("""第?\s*(\d+)\s*[-~－—]\s*(\d+)\s*节?"""),
            Regex("""(\d+)\s*[-~－—]\s*(\d+)\s*节""")
        )
        val singlePattern = Regex("""第?\s*(\d+)\s*节""")

        // 1. 收集所有明确的节次信息
        parsedCourses.forEach { course ->
            val description = course.description
            val startTime = course.dtStart.toLocalTime()
            val endTime = course.dtEnd.toLocalTime()
            
            var matched = false
            
            if (!description.isNullOrBlank()) {
                // 尝试匹配 "第X-Y节"
                for (pattern in rangePatterns) {
                    val rangeMatch = pattern.find(description)
                    if (rangeMatch != null) {
                        val startPeriod = rangeMatch.groupValues[1].toInt()
                        val endPeriod = rangeMatch.groupValues[2].toInt()
                        
                        if (startPeriod <= endPeriod) {
                            matched = true
                            // 计算每节课的平均时长（假设课间休息10分钟）
                            val totalMinutes = ChronoUnit.MINUTES.between(startTime, endTime)
                            val numPeriods = endPeriod - startPeriod + 1
                            val totalBreakTime = (numPeriods - 1) * 10 // 假设课间休息10分钟
                            
                            // 如果总时长不够扣除休息时间，尝试减少休息时间假设
                            val breakDuration = if (totalMinutes > totalBreakTime) 10 else 0
                            val periodDuration = (totalMinutes - (numPeriods - 1) * breakDuration) / numPeriods
                            
                            for (i in 0 until numPeriods) {
                                val pStart = startTime.plusMinutes(i * (periodDuration + breakDuration))
                                val pEnd = pStart.plusMinutes(periodDuration)
                                val periodNum = startPeriod + i
                                
                                periodTimeSamples.getOrPut(periodNum) { mutableListOf() }.add(Pair(pStart, pEnd))
                            }
                            break // 匹配成功，跳出循环
                        }
                    }
                }
                
                if (!matched) {
                    // 尝试匹配 "第X节"
                    val singleMatch = singlePattern.find(description)
                    if (singleMatch != null) {
                        val periodNum = singleMatch.groupValues[1].toInt()
                        periodTimeSamples.getOrPut(periodNum) { mutableListOf() }.add(Pair(startTime, endTime))
                        matched = true
                    }
                }
            }
        }

        // 2. 计算每个节次的标准时间（使用众数或平均值）
        val periodMap = mutableMapOf<Int, Pair<LocalTime, LocalTime>>()
        periodTimeSamples.forEach { (periodNum, samples) ->
            // 简单的策略：取出现次数最多的开始时间和结束时间
            // 如果样本太少或分散，取第一个
            val startTimes = samples.map { it.first }
            val endTimes = samples.map { it.second }
            
            val commonStart = startTimes.groupingBy { it }.eachCount().maxByOrNull { it.value }?.key ?: startTimes.first()
            val commonEnd = endTimes.groupingBy { it }.eachCount().maxByOrNull { it.value }?.key ?: endTimes.first()
            
            periodMap[periodNum] = Pair(commonStart, commonEnd)
        }

        // 3. 混合模式：处理那些没有描述但有时间的课程（如物理课）
        // 扫描所有课程，如果发现某个课程的时间段没有被现有的节次覆盖，则尝试创建新节次
        parsedCourses.forEach { course ->
            val startTime = course.dtStart.toLocalTime()
            val endTime = course.dtEnd.toLocalTime()
            
            // 检查是否已被现有节次覆盖
            val isCovered = periodMap.values.any { (pStart, pEnd) ->
                // 允许一定的误差（比如15分钟）
                val startDiff = Math.abs(ChronoUnit.MINUTES.between(startTime, pStart))
                val endDiff = Math.abs(ChronoUnit.MINUTES.between(endTime, pEnd))
                // 或者包含关系
                val isContained = startTime >= pStart && endTime <= pEnd
                
                startDiff <= 15 || isContained
            }
            
            if (!isCovered) {
                // 未覆盖，尝试推断节次
                // 假设它是接在现有最大节次之后的
                // 或者根据时间判断（比如晚上）
                
                // 简单的策略：如果是在晚上（18:00后），且没有对应的节次，则创建
                if (startTime.hour >= 18) {
                    // 查找现有的最大节次
                    val maxPeriod = periodMap.keys.maxOrNull() ?: 0
                    // 假设这是接下来的节次（比如9-10节）
                    // 估算节数：根据时长（每节45分钟+10分钟休息）
                    val duration = ChronoUnit.MINUTES.between(startTime, endTime)
                    val estimatedPeriods = (duration / 55).toInt().coerceAtLeast(1) // 粗略估算
                    
                    // 只有当现有的最大节次小于通常的晚课开始节次（比如9）时，才跳跃编号
                    // 这里简单处理：直接递增
                    // 但为了避免冲突，我们检查一下时间间隔
                    
                    // 如果是物理课（19:00-20:30），通常是9-10节
                    // 如果现有最大是8节（17:00结束），那么19:00开始的很可能是9节
                    
                    var startPeriodNum = maxPeriod + 1
                    // 如果中间有很大空档（比如晚餐），可能需要跳过编号？
                    // 但通常学校的节次是连续编号的，或者晚课有固定编号（如9,10,11）
                    // 这里我们尝试智能一点：如果当前最大是8，且现在是19:00，那很可能是9
                    
                    // 创建新节次
                    val periodDuration = 45L
                    val breakDuration = 10L
                    
                    for (i in 0 until estimatedPeriods) {
                        val pStart = startTime.plusMinutes(i * (periodDuration + breakDuration))
                        val pEnd = pStart.plusMinutes(periodDuration)
                        val periodNum = startPeriodNum + i
                        
                        if (!periodMap.containsKey(periodNum)) {
                            periodMap[periodNum] = Pair(pStart, pEnd)
                        }
                    }
                } else {
                    // 白天的未覆盖课程，可能是特殊的短课或长课
                    // 暂时忽略，以免破坏现有结构，或者可以作为自定义时间处理
                }
            }
        }

        // 4. 如果完全没有描述信息，回退到纯时间生成
        if (periodMap.isEmpty()) {
            return generatePeriodsFromCourses(parsedCourses)
        }

        // 5. 转换为List<CoursePeriod>并排序
        val sortedPeriods = periodMap.entries.sortedBy { it.key }
        
        // 6. 识别午休（最大间隔）
        var maxGap = 0L
        var lunchBreakAfter = 0
        
        for (i in 0 until sortedPeriods.size - 1) {
            val currentEnd = sortedPeriods[i].value.second
            val nextStart = sortedPeriods[i+1].value.first
            val gap = ChronoUnit.MINUTES.between(currentEnd, nextStart)
            
            if (gap > maxGap && gap >= 30) { // 假设午休至少30分钟
                maxGap = gap
                lunchBreakAfter = sortedPeriods[i].key
            }
        }

        return sortedPeriods.map { (periodNum, timeRange) ->
            val (start, end) = timeRange
            val isAfterLunch = lunchBreakAfter > 0 && periodNum > lunchBreakAfter
            
            val timeOfDay = when {
                start.hour < 12 -> TimeOfDay.MORNING
                start.hour < 18 -> TimeOfDay.AFTERNOON
                else -> TimeOfDay.EVENING
            }

            CoursePeriod(
                periodNumber = periodNum,
                startTime = start,
                endTime = end,
                timeOfDay = timeOfDay,
                isAfterLunchBreak = isAfterLunch
            )
        }
    }
    
    /**
     * 将节次列表转换为课程设置
     */
    fun convertToSettings(periods: List<CoursePeriod>): takagi.ru.saison.domain.model.CourseSettings {
        if (periods.isEmpty()) return takagi.ru.saison.domain.model.CourseSettings()
        
        val sortedPeriods = periods.sortedBy { it.periodNumber }
        val firstPeriod = sortedPeriods.first()
        
        // 计算平均课程时长
        val durations = sortedPeriods.map { 
            ChronoUnit.MINUTES.between(it.startTime, it.endTime) 
        }
        // 使用众数作为课程时长，而不是平均值，以避免个别异常值的影响
        val durationCounts = durations.groupingBy { it }.eachCount()
        val commonDuration = durationCounts.maxByOrNull { it.value }?.key?.toInt() ?: 45
        
        // 计算平均课间休息
        val breaks = mutableListOf<Long>()
        var maxBreak = 0L
        var lunchBreakAfter = 0
        
        for (i in 0 until sortedPeriods.size - 1) {
            val currentEnd = sortedPeriods[i].endTime
            val nextStart = sortedPeriods[i+1].startTime
            val gap = ChronoUnit.MINUTES.between(currentEnd, nextStart)
            
            if (gap > maxBreak) {
                maxBreak = gap
                lunchBreakAfter = sortedPeriods[i].periodNumber
            }
            
            // 收集所有休息时间
            breaks.add(gap)
        }
        
        // 识别午休：最大休息时间，且至少30分钟
        val hasLunchBreak = maxBreak >= 30
        val finalLunchBreakAfter = if (hasLunchBreak) lunchBreakAfter else null
        val finalLunchBreakDuration = if (hasLunchBreak) maxBreak.toInt() else 90
        
        // 计算普通课间休息：排除午休后的平均值/众数
        val normalBreaks = if (hasLunchBreak) {
            breaks.filter { it != maxBreak }
        } else {
            breaks
        }
        
        val commonBreak = if (normalBreaks.isNotEmpty()) {
            val breakCounts = normalBreaks.groupingBy { it }.eachCount()
            breakCounts.maxByOrNull { it.value }?.key?.toInt() ?: 10
        } else {
            10
        }
        
        return takagi.ru.saison.domain.model.CourseSettings(
            totalPeriods = sortedPeriods.size,
            periodDuration = commonDuration,
            breakDuration = commonBreak,
            firstPeriodStartTime = firstPeriod.startTime,
            lunchBreakAfterPeriod = finalLunchBreakAfter,
            lunchBreakDuration = finalLunchBreakDuration
        )
    }
}
