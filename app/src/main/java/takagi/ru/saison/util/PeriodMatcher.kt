package takagi.ru.saison.util

import takagi.ru.saison.domain.model.CoursePeriod
import java.time.LocalTime
import java.time.temporal.ChronoUnit

/**
 * 节次匹配器
 * 负责将时间映射到课程节次
 */
object PeriodMatcher {
    
    /**
     * 根据时间和描述匹配节次
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param description 课程描述（可能包含节次信息）
     * @param existingPeriods 现有节次列表
     * @return 匹配的节次范围 (startPeriod, endPeriod)，如果无法匹配则返回 (null, null)
     */
    fun matchPeriod(
        startTime: LocalTime,
        endTime: LocalTime,
        description: String?,
        existingPeriods: List<CoursePeriod>
    ): Pair<Int?, Int?> {
        android.util.Log.d("PeriodMatcher", "=== Matching period for time $startTime-$endTime ===")
        android.util.Log.d("PeriodMatcher", "Description: '$description'")
        android.util.Log.d("PeriodMatcher", "Available periods: ${existingPeriods.size}")
        
        // 如果没有可用的节次配置，返回null
        if (existingPeriods.isEmpty()) {
            android.util.Log.w("PeriodMatcher", "No existing periods available")
            return Pair(null, null)
        }
        
        // 优先从描述中提取节次信息
        val extractedPeriod = extractPeriodFromDescription(description)
        if (extractedPeriod != null) {
            val (start, end) = extractedPeriod
            // 只验证节次是否为正数和顺序正确，不验证是否在现有节次范围内
            // 因为ICS文件的节次配置可能与当前设置不同
            if (start != null && end != null && start > 0 && end > 0 && start <= end) {
                android.util.Log.d("PeriodMatcher", "Extracted period from description: $start-$end for time $startTime-$endTime")
                return Pair(start, end)
            }
        }
        
        // 如果无法从描述中提取，使用时间重叠算法
        android.util.Log.d("PeriodMatcher", "No period in description, using time-based matching")
        
        // 尝试精确匹配：如果课程时间与某个节次（或连续节次）的时间几乎完全一致
        val exactMatch = findExactMatch(startTime, endTime, existingPeriods)
        if (exactMatch != null) {
            android.util.Log.d("PeriodMatcher", "Found exact time match: ${exactMatch.first}-${exactMatch.second}")
            return exactMatch
        }
        
        val result = findBestMatchingPeriod(startTime, endTime, existingPeriods)
        android.util.Log.d("PeriodMatcher", "Time-based matching result: ${result.first}-${result.second}")
        return result
    }
    
    /**
     * 查找精确的时间匹配
     * 允许5分钟误差
     */
    private fun findExactMatch(
        startTime: LocalTime,
        endTime: LocalTime,
        periods: List<CoursePeriod>
    ): Pair<Int?, Int?>? {
        val sortedPeriods = periods.sortedBy { it.periodNumber }
        
        // 遍历所有可能的连续节次组合
        for (i in sortedPeriods.indices) {
            for (j in i until sortedPeriods.size) {
                val startPeriod = sortedPeriods[i]
                val endPeriod = sortedPeriods[j]
                
                // 检查开始时间
                val startDiff = Math.abs(ChronoUnit.MINUTES.between(startTime, startPeriod.startTime))
                // 检查结束时间
                val endDiff = Math.abs(ChronoUnit.MINUTES.between(endTime, endPeriod.endTime))
                
                if (startDiff <= 5 && endDiff <= 5) {
                    return Pair(startPeriod.periodNumber, endPeriod.periodNumber)
                }
            }
        }
        return null
    }
    
    /**
     * 从描述中提取节次信息
     * 支持格式: "第7-8节", "第7 - 8节", "第7节", "7-8节", "第7~8节", "第7－8节", "第7—8节"
     * @param description 课程描述
     * @return 节次范围 (startPeriod, endPeriod)，如果无法提取则返回null
     */
    fun extractPeriodFromDescription(description: String?): Pair<Int?, Int?>? {
        if (description.isNullOrBlank()) {
            return null
        }
        
        android.util.Log.d("PeriodMatcher", "Trying to extract period from description: '$description'")
        
        // 支持多种连字符格式：- ~ － — (ASCII连字符、波浪号、全角连字符、长破折号)
        // 支持带空格和不带空格的格式
        // 支持带"第"和不带"第"的格式
        val patterns = listOf(
            // 匹配 "第X-Y节" 或 "第X~Y节" 等格式（支持中间有空格）
            Regex("""第?\s*(\d+)\s*[-~－—]\s*(\d+)\s*节?"""),
            // 匹配 "X-Y节" 格式（不带"第"）
            Regex("""(\d+)\s*[-~－—]\s*(\d+)\s*节"""),
            // 匹配 "第X-Y" 格式（不带"节"）
            Regex("""第\s*(\d+)\s*[-~－—]\s*(\d+)""")
        )
        
        for ((index, pattern) in patterns.withIndex()) {
            val match = pattern.find(description)
            if (match != null) {
                val start = match.groupValues[1].toIntOrNull()
                val end = match.groupValues[2].toIntOrNull()
                android.util.Log.d("PeriodMatcher", "Range pattern #$index matched: start=$start, end=$end")
                if (start != null && end != null && start <= end) {
                    return Pair(start, end)
                }
            } else {
                android.util.Log.d("PeriodMatcher", "Range pattern #$index did not match")
            }
        }
        
        // 匹配 "第X节" 格式
        val singlePattern = Regex("""第?\s*(\d+)\s*节""")
        val singleMatch = singlePattern.find(description)
        if (singleMatch != null) {
            val period = singleMatch.groupValues[1].toIntOrNull()
            android.util.Log.d("PeriodMatcher", "Single pattern matched: period=$period")
            if (period != null) {
                return Pair(period, period)
            }
        } else {
            android.util.Log.d("PeriodMatcher", "Single pattern did not match")
        }
        
        android.util.Log.d("PeriodMatcher", "No period pattern matched")
        return null
    }
    
    /**
     * 根据时间查找最匹配的节次
     * 使用时间重叠度算法
     * @param startTime 课程开始时间
     * @param endTime 课程结束时间
     * @param periods 节次列表
     * @return 匹配的节次范围 (startPeriod, endPeriod)，如果无法匹配则返回 (null, null)
     */
    fun findBestMatchingPeriod(
        startTime: LocalTime,
        endTime: LocalTime,
        periods: List<CoursePeriod>
    ): Pair<Int?, Int?> {
        if (periods.isEmpty()) {
            return Pair(null, null)
        }
        
        // 按节次编号排序
        val sortedPeriods = periods.sortedBy { it.periodNumber }
        
        // 查找开始节次：找到与课程开始时间重叠度最高的节次
        val startPeriod = findBestMatchingStartPeriod(startTime, sortedPeriods)
        
        // 查找结束节次：找到与课程结束时间重叠度最高的节次
        val endPeriod = findBestMatchingEndPeriod(endTime, sortedPeriods)
        
        // 如果找到了有效的开始和结束节次
        if (startPeriod != null && endPeriod != null) {
            // 确保开始节次不大于结束节次
            if (startPeriod <= endPeriod) {
                return Pair(startPeriod, endPeriod)
            }
        }
        
        // 如果无法找到合适的匹配，返回null
        return Pair(null, null)
    }
    
    /**
     * 查找与开始时间最匹配的节次
     */
    private fun findBestMatchingStartPeriod(
        startTime: LocalTime,
        sortedPeriods: List<CoursePeriod>
    ): Int? {
        var bestPeriod: Int? = null
        var bestScore = 0.0
        
        for (period in sortedPeriods) {
            val score = calculateStartTimeMatchScore(startTime, period)
            if (score > bestScore) {
                bestScore = score
                bestPeriod = period.periodNumber
            }
        }
        
        // 只有当匹配度超过阈值时才返回结果
        return if (bestScore > 0.3) bestPeriod else null
    }
    
    /**
     * 查找与结束时间最匹配的节次
     */
    private fun findBestMatchingEndPeriod(
        endTime: LocalTime,
        sortedPeriods: List<CoursePeriod>
    ): Int? {
        var bestPeriod: Int? = null
        var bestScore = 0.0
        
        for (period in sortedPeriods) {
            val score = calculateEndTimeMatchScore(endTime, period)
            if (score > bestScore) {
                bestScore = score
                bestPeriod = period.periodNumber
            }
        }
        
        // 只有当匹配度超过阈值时才返回结果
        return if (bestScore > 0.3) bestPeriod else null
    }
    
    /**
     * 计算开始时间的匹配分数
     * 考虑：
     * 1. 课程开始时间与节次开始时间的接近程度
     * 2. 课程开始时间是否在节次时间范围内
     */
    private fun calculateStartTimeMatchScore(
        courseStartTime: LocalTime,
        period: CoursePeriod
    ): Double {
        val periodStart = period.startTime
        val periodEnd = period.endTime
        
        // 如果课程开始时间在节次范围内，给予高分
        if (courseStartTime >= periodStart && courseStartTime <= periodEnd) {
            // 越接近节次开始时间，分数越高
            val totalMinutes = ChronoUnit.MINUTES.between(periodStart, periodEnd).toDouble()
            val offsetMinutes = ChronoUnit.MINUTES.between(periodStart, courseStartTime).toDouble()
            return 1.0 - (offsetMinutes / totalMinutes) * 0.3 // 0.7 到 1.0 之间
        }
        
        // 如果课程开始时间在节次之前或之后，根据距离计算分数
        val minutesToStart = ChronoUnit.MINUTES.between(courseStartTime, periodStart).toDouble()
        val minutesFromEnd = ChronoUnit.MINUTES.between(periodEnd, courseStartTime).toDouble()
        
        // 选择较小的距离
        val minDistance = minOf(Math.abs(minutesToStart), Math.abs(minutesFromEnd))
        
        // 距离越近，分数越高（最多30分钟内有效）
        return if (minDistance <= 30) {
            0.7 * (1.0 - minDistance / 30.0)
        } else {
            0.0
        }
    }
    
    /**
     * 计算结束时间的匹配分数
     * 考虑：
     * 1. 课程结束时间与节次结束时间的接近程度
     * 2. 课程结束时间是否在节次时间范围内
     */
    private fun calculateEndTimeMatchScore(
        courseEndTime: LocalTime,
        period: CoursePeriod
    ): Double {
        val periodStart = period.startTime
        val periodEnd = period.endTime
        
        // 如果课程结束时间在节次范围内，给予高分
        if (courseEndTime >= periodStart && courseEndTime <= periodEnd) {
            // 越接近节次结束时间，分数越高
            val totalMinutes = ChronoUnit.MINUTES.between(periodStart, periodEnd).toDouble()
            val offsetMinutes = ChronoUnit.MINUTES.between(courseEndTime, periodEnd).toDouble()
            return 1.0 - (offsetMinutes / totalMinutes) * 0.3 // 0.7 到 1.0 之间
        }
        
        // 如果课程结束时间在节次之前或之后，根据距离计算分数
        val minutesToStart = ChronoUnit.MINUTES.between(courseEndTime, periodStart).toDouble()
        val minutesFromEnd = ChronoUnit.MINUTES.between(periodEnd, courseEndTime).toDouble()
        
        // 选择较小的距离
        val minDistance = minOf(Math.abs(minutesToStart), Math.abs(minutesFromEnd))
        
        // 距离越近，分数越高（最多30分钟内有效）
        return if (minDistance <= 30) {
            0.7 * (1.0 - minDistance / 30.0)
        } else {
            0.0
        }
    }
    
    /**
     * 计算时间重叠度
     * @param courseStart 课程开始时间
     * @param courseEnd 课程结束时间
     * @param periodStart 节次开始时间
     * @param periodEnd 节次结束时间
     * @return 重叠度 (0.0 到 1.0)
     */
    fun calculateOverlap(
        courseStart: LocalTime,
        courseEnd: LocalTime,
        periodStart: LocalTime,
        periodEnd: LocalTime
    ): Float {
        val overlapStart = maxOf(courseStart, periodStart)
        val overlapEnd = minOf(courseEnd, periodEnd)
        
        if (overlapStart >= overlapEnd) return 0f
        
        val overlapMinutes = ChronoUnit.MINUTES.between(overlapStart, overlapEnd)
        val courseMinutes = ChronoUnit.MINUTES.between(courseStart, courseEnd)
        
        return if (courseMinutes > 0) {
            overlapMinutes.toFloat() / courseMinutes.toFloat()
        } else {
            0f
        }
    }
}
