package takagi.ru.saison.ui.screens.valueday

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import takagi.ru.saison.data.local.database.entities.ValueDayEntity
import takagi.ru.saison.data.repository.ValueDayRepository
import java.time.LocalDate
import javax.inject.Inject

/**
 * 买断统计数据
 */
data class ValueDayStatistics(
    val totalItems: Int = 0,
    val totalCost: Double = 0.0,
    val averageDailyCost: Double = 0.0
)

@HiltViewModel
class ValueDayViewModel @Inject constructor(
    private val repository: ValueDayRepository
) : ViewModel() {
    
    // 所有买断项目
    val valueDays: StateFlow<List<ValueDayEntity>> = repository.getAllValueDays()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    // 统计数据
    val statistics: StateFlow<ValueDayStatistics> = valueDays.map { items ->
        calculateStatistics(items)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ValueDayStatistics()
    )
    
    /**
     * 添加买断项目
     */
    fun addValueDay(itemName: String, purchasePrice: Double, purchaseDate: LocalDate) {
        viewModelScope.launch {
            val entity = ValueDayEntity(
                itemName = itemName,
                purchasePrice = purchasePrice,
                purchaseDate = purchaseDate.toEpochDay()
            )
            repository.insertValueDay(entity)
        }
    }
    
    /**
     * 更新买断项目
     */
    fun updateValueDay(entity: ValueDayEntity) {
        viewModelScope.launch {
            repository.updateValueDay(entity)
        }
    }
    
    /**
     * 删除买断项目
     */
    fun deleteValueDay(entity: ValueDayEntity) {
        viewModelScope.launch {
            repository.deleteValueDay(entity)
        }
    }
    
    /**
     * 计算统计数据
     */
    private fun calculateStatistics(items: List<ValueDayEntity>): ValueDayStatistics {
        if (items.isEmpty()) {
            return ValueDayStatistics()
        }
        
        val totalCost = items.sumOf { it.purchasePrice }
        val totalDailyCost = items.sumOf { it.getDailyAverageCost() }
        
        return ValueDayStatistics(
            totalItems = items.size,
            totalCost = totalCost,
            averageDailyCost = totalDailyCost
        )
    }
}
