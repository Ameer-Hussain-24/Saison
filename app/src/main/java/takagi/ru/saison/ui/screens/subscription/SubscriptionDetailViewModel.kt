package takagi.ru.saison.ui.screens.subscription

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import takagi.ru.saison.data.local.database.entities.SubscriptionEntity
import takagi.ru.saison.data.repository.SubscriptionRepository
import takagi.ru.saison.util.RenewalCalculator
import takagi.ru.saison.util.SubscriptionStatisticsCalculator
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import javax.inject.Inject

/**
 * 订阅详情页面的ViewModel
 */
@HiltViewModel
class SubscriptionDetailViewModel @Inject constructor(
    private val repository: SubscriptionRepository,
    private val historyRepository: takagi.ru.saison.data.repository.SubscriptionHistoryRepository,
    private val historyManager: takagi.ru.saison.util.SubscriptionHistoryManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    private val subscriptionId: Long = savedStateHandle["subscriptionId"] ?: 0L
    
    // 订阅数据
    private val _subscription = MutableStateFlow<SubscriptionEntity?>(null)
    val subscription: StateFlow<SubscriptionEntity?> = _subscription.asStateFlow()
    
    // 统计信息
    private val _statistics = MutableStateFlow<SubscriptionStatistics?>(null)
    val statistics: StateFlow<SubscriptionStatistics?> = _statistics.asStateFlow()
    
    // 续订选项
    private val _renewalOptions = MutableStateFlow<List<RenewalOption>>(emptyList())
    val renewalOptions: StateFlow<List<RenewalOption>> = _renewalOptions.asStateFlow()
    
    // 历史记录
    val history: StateFlow<List<takagi.ru.saison.data.local.database.entities.SubscriptionHistoryEntity>> = 
        historyRepository.getHistoryForSubscription(subscriptionId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    // UI状态
    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
    
    init {
        loadSubscription()
    }
    
    /**
     * 加载订阅数据
     */
    private fun loadSubscription() {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading
                
                val sub = repository.getSubscriptionById(subscriptionId)
                if (sub == null) {
                    _uiState.value = UiState.Error("订阅不存在")
                    return@launch
                }
                
                _subscription.value = sub
                calculateStatistics(sub)
                generateRenewalOptions(sub)
                
                _uiState.value = UiState.Success
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "加载失败")
            }
        }
    }
    
    /**
     * 计算统计信息
     */
    private fun calculateStatistics(subscription: SubscriptionEntity) {
        viewModelScope.launch {
            val startDate = Instant.ofEpochMilli(subscription.startDate)
                .atZone(ZoneId.systemDefault()).toLocalDate()
            val currentDate = LocalDate.now()
            val nextRenewalDate = Instant.ofEpochMilli(subscription.nextRenewalDate)
                .atZone(ZoneId.systemDefault()).toLocalDate()
            
            val accumulatedDuration = SubscriptionStatisticsCalculator.calculateAccumulatedDuration(
                startDate, currentDate
            )
            
            // 从历史记录中计算跳过的周期数
            val skippedPeriods = calculateSkippedPeriodsFromHistory()
            
            // 使用考虑跳过周期的费用计算方法
            val accumulatedCost = SubscriptionStatisticsCalculator.calculateAccumulatedCostWithSkips(
                startDate, currentDate,
                subscription.price,
                subscription.cycleType,
                subscription.cycleDuration,
                skippedPeriods
            )
            
            val totalMonths = SubscriptionStatisticsCalculator.calculateTotalMonths(startDate, currentDate)
            val totalDays = SubscriptionStatisticsCalculator.calculateTotalDays(startDate, currentDate)
            
            val averageMonthlyCost = SubscriptionStatisticsCalculator.calculateAverageMonthlyCost(
                accumulatedCost, totalMonths
            )
            
            val averageDailyCost = SubscriptionStatisticsCalculator.calculateAverageDailyCost(
                accumulatedCost, totalDays
            )
            
            val renewalCyclesCompleted = SubscriptionStatisticsCalculator.calculateRenewalCyclesCompleted(
                startDate, currentDate,
                subscription.cycleType,
                subscription.cycleDuration
            )
            
            val daysUntilRenewal = ChronoUnit.DAYS.between(currentDate, nextRenewalDate)
            val isOverdue = daysUntilRenewal < 0
            
            _statistics.value = SubscriptionStatistics(
                accumulatedCost = accumulatedCost,
                accumulatedDuration = accumulatedDuration,
                averageMonthlyCost = averageMonthlyCost,
                averageDailyCost = averageDailyCost,
                renewalCyclesCompleted = renewalCyclesCompleted,
                daysUntilRenewal = daysUntilRenewal,
                isOverdue = isOverdue
            )
        }
    }
    
    /**
     * 从历史记录中计算跳过的周期数
     * 根据跳过的实际时长和订阅周期类型计算等效的周期数
     */
    private suspend fun calculateSkippedPeriodsFromHistory(): Int {
        return try {
            val sub = _subscription.value ?: return 0
            val historyList = history.value
            
            // 筛选所有SKIPPED类型的历史记录
            val skippedRecords = historyList.filter { 
                it.operationType == takagi.ru.saison.data.local.database.entities.HistoryOperationType.SKIPPED.name 
            }
            
            var totalSkippedPeriods = 0
            
            // 解析每条跳过记录的metadata，计算等效周期数
            skippedRecords.forEach { record ->
                try {
                    val metadata = record.metadata
                    if (!metadata.isNullOrBlank()) {
                        // 解析JSON metadata获取跳过的时长
                        val json = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
                        val skipMetadata = json.decodeFromString<takagi.ru.saison.util.SkipMetadata>(metadata)
                        
                        // 根据跳过的单位和订阅周期类型计算等效周期数
                        val equivalentPeriods = when (skipMetadata.skipUnit) {
                            "DAYS" -> {
                                when (sub.cycleType) {
                                    "MONTHLY" -> skipMetadata.skipAmount / 30
                                    "QUARTERLY" -> skipMetadata.skipAmount / 90
                                    "YEARLY" -> skipMetadata.skipAmount / 365
                                    else -> 0
                                }
                            }
                            "MONTHS" -> {
                                when (sub.cycleType) {
                                    "MONTHLY" -> skipMetadata.skipAmount
                                    "QUARTERLY" -> skipMetadata.skipAmount / 3
                                    "YEARLY" -> skipMetadata.skipAmount / 12
                                    else -> 0
                                }
                            }
                            "YEARS" -> {
                                when (sub.cycleType) {
                                    "MONTHLY" -> skipMetadata.skipAmount * 12
                                    "QUARTERLY" -> skipMetadata.skipAmount * 4
                                    "YEARLY" -> skipMetadata.skipAmount
                                    else -> 0
                                }
                            }
                            else -> 0
                        }
                        
                        totalSkippedPeriods += equivalentPeriods
                    }
                } catch (e: Exception) {
                    // 如果解析失败，按1个周期计算
                    totalSkippedPeriods += 1
                }
            }
            
            totalSkippedPeriods
        } catch (e: Exception) {
            0
        }
    }
    
    /**
     * 生成续订选项
     */
    private fun generateRenewalOptions(subscription: SubscriptionEntity) {
        val currentDate = LocalDate.now()
        val durationOptions = RenewalCalculator.getRenewalDurationOptions(subscription.cycleType)
        
        val options = durationOptions.map { count ->
            val newDate = RenewalCalculator.calculateNextRenewalDate(
                currentDate,
                subscription.cycleType,
                subscription.cycleDuration,
                count
            )
            
            val totalCost = RenewalCalculator.calculateRenewalCost(
                subscription.price,
                count
            )
            
            val label = when (subscription.cycleType) {
                "MONTHLY" -> "${count}个月"
                "QUARTERLY" -> "${count}个季度"
                "YEARLY" -> "${count}年"
                "CUSTOM" -> "${count}个周期"
                else -> "${count}次"
            }
            
            RenewalOption(
                count = count,
                label = label,
                totalCost = totalCost,
                newExpirationDate = newDate
            )
        }
        
        _renewalOptions.value = options
    }
    
    /**
     * 执行手动续订
     */
    fun performManualRenewal(renewalCount: Int) {
        viewModelScope.launch {
            try {
                val sub = _subscription.value ?: return@launch
                
                val currentDate = LocalDate.now()
                val newRenewalDate = RenewalCalculator.calculateNextRenewalDate(
                    currentDate,
                    sub.cycleType,
                    sub.cycleDuration,
                    renewalCount
                )
                
                val newRenewalTimestamp = newRenewalDate
                    .atStartOfDay(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()
                
                repository.updateRenewalDate(sub.id, newRenewalTimestamp)
                
                // 重新加载数据
                loadSubscription()
            } catch (e: Exception) {
                _uiState.value = UiState.Error("续订失败: ${e.message}")
            }
        }
    }
    
    /**
     * 切换自动续订状态
     */
    fun toggleAutoRenewal() {
        viewModelScope.launch {
            try {
                val sub = _subscription.value ?: return@launch
                val updated = sub.copy(
                    autoRenewal = !sub.autoRenewal,
                    updatedAt = System.currentTimeMillis()
                )
                repository.updateSubscription(updated)
                _subscription.value = updated
            } catch (e: Exception) {
                _uiState.value = UiState.Error("更新失败: ${e.message}")
            }
        }
    }
    
    /**
     * 删除订阅
     */
    fun deleteSubscription(onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                repository.deleteSubscriptionById(subscriptionId)
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = UiState.Error("删除失败: ${e.message}")
            }
        }
    }
    
    /**
     * 刷新数据
     */
    fun refresh() {
        loadSubscription()
    }
    
    /**
     * 中断订阅
     */
    fun pauseSubscription(reason: String? = null, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val sub = _subscription.value ?: return@launch
                repository.pauseSubscription(sub.id, reason)
                historyManager.recordPaused(sub, reason)
                loadSubscription()
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = UiState.Error("中断失败: ${e.message}")
            }
        }
    }
    
    /**
     * 恢复订阅
     */
    fun resumeSubscription(onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val sub = _subscription.value ?: return@launch
                val currentDate = LocalDate.now()
                val newRenewalDate = takagi.ru.saison.util.SubscriptionDateCalculator.calculateNextRenewalDate(
                    currentDate,
                    sub.cycleType,
                    sub.cycleDuration
                )
                val newRenewalTimestamp = newRenewalDate
                    .atStartOfDay(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()
                
                repository.resumeSubscription(sub.id, newRenewalTimestamp)
                
                // 重新加载以获取更新后的订阅
                val updatedSub = repository.getSubscriptionById(sub.id)
                if (updatedSub != null) {
                    historyManager.recordResumed(updatedSub)
                }
                
                loadSubscription()
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = UiState.Error("恢复失败: ${e.message}")
            }
        }
    }
    
    /**
     * 跳过订阅周期
     */
    fun skipSubscription(skipDuration: takagi.ru.saison.domain.model.SkipDuration, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val sub = _subscription.value ?: return@launch
                val oldRenewalDate = sub.nextRenewalDate
                
                repository.skipSubscription(sub.id, skipDuration)
                
                // 重新加载以获取更新后的订阅
                val updatedSub = repository.getSubscriptionById(sub.id)
                if (updatedSub != null) {
                    historyManager.recordSkipped(
                        updatedSub,
                        skipDuration,
                        oldRenewalDate,
                        updatedSub.nextRenewalDate
                    )
                }
                
                loadSubscription()
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = UiState.Error("跳过失败: ${e.message}")
            }
        }
    }
}

/**
 * 订阅统计信息
 */
data class SubscriptionStatistics(
    val accumulatedCost: Double,
    val accumulatedDuration: String,
    val averageMonthlyCost: Double,
    val averageDailyCost: Double,
    val renewalCyclesCompleted: Int,
    val daysUntilRenewal: Long,
    val isOverdue: Boolean
)

/**
 * 续订选项
 */
data class RenewalOption(
    val count: Int,
    val label: String,
    val totalCost: Double,
    val newExpirationDate: LocalDate
)

/**
 * UI状态
 */
sealed class UiState {
    object Loading : UiState()
    object Success : UiState()
    data class Error(val message: String) : UiState()
}
