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
 * 买断筛选模式
 */
enum class ValueDayFilterMode {
    ALL,           // 全部
    WITH_WARRANTY, // 有保修
    WARRANTY_EXPIRED, // 保修已过期
    NO_WARRANTY    // 无保修
}

/**
 * 买断统计数据
 */
data class ValueDayStatistics(
    val totalItems: Int = 0,
    val totalCost: Double = 0.0,
    val averageDailyCost: Double = 0.0
)

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@HiltViewModel
class ValueDayViewModel @Inject constructor(
    private val repository: ValueDayRepository
) : ViewModel() {
    
    // 所有分类
    val categories: StateFlow<List<String>> = repository.getAllCategories()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    // 当前选中的分类（null 表示显示全部）
    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()
    
    // 搜索查询
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    // 筛选模式
    private val _filterMode = MutableStateFlow(ValueDayFilterMode.ALL)
    val filterMode: StateFlow<ValueDayFilterMode> = _filterMode.asStateFlow()
    
    // 根据选中分类、搜索条件和筛选模式显示的买断项目
    val valueDays: StateFlow<List<ValueDayEntity>> = combine(
        selectedCategory,
        searchQuery,
        filterMode
    ) { category, query, mode ->
        Triple(category, query, mode)
    }.flatMapLatest { (category, query, mode) ->
        if (category == null) {
            repository.getAllValueDays()
        } else {
            repository.getValueDaysByCategory(category)
        }
    }.map { items ->
        var filteredItems = items
        
        // 应用搜索过滤
        if (_searchQuery.value.isNotBlank()) {
            filteredItems = filteredItems.filter { it.itemName.contains(_searchQuery.value, ignoreCase = true) }
        }
        
        // 应用筛选模式
        filteredItems = when (_filterMode.value) {
            ValueDayFilterMode.ALL -> filteredItems
            ValueDayFilterMode.WITH_WARRANTY -> filteredItems.filter { 
                it.warrantyEndDate != null && !it.isWarrantyExpired() 
            }
            ValueDayFilterMode.WARRANTY_EXPIRED -> filteredItems.filter { 
                it.warrantyEndDate != null && it.isWarrantyExpired() 
            }
            ValueDayFilterMode.NO_WARRANTY -> filteredItems.filter { it.warrantyEndDate == null }
        }
        
        filteredItems
    }.stateIn(
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
     * 设置分类筛选
     */
    fun setSelectedCategory(category: String?) {
        _selectedCategory.value = category
    }
    
    /**
     * 设置搜索查询
     */
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    /**
     * 设置筛选模式
     */
    fun setFilterMode(mode: ValueDayFilterMode) {
        _filterMode.value = mode
    }
    
    /**
     * 添加分类
     */
    fun addCategory(categoryName: String) {
        // 分类由数据库自动管理，只要有记录使用该分类即可
    }
    
    /**
     * 删除分类
     */
    fun deleteCategory(category: String) {
        viewModelScope.launch {
            // 将该分类下的所有项目改为"未分类"
            val items = repository.getValueDaysByCategory(category).first()
            items.forEach { item ->
                repository.updateValueDay(item.copy(category = "未分类"))
            }
        }
    }
    
    /**
     * 重命名分类
     */
    fun renameCategory(oldName: String, newName: String) {
        viewModelScope.launch {
            // 将该分类下的所有项目改为新分类名
            val items = repository.getValueDaysByCategory(oldName).first()
            items.forEach { item ->
                repository.updateValueDay(item.copy(category = newName))
            }
        }
    }
    
    /**
     * 添加买断项目
     */
    fun addValueDay(
        itemName: String, 
        purchasePrice: Double, 
        purchaseDate: LocalDate,
        category: String = "未分类",
        warrantyEndDate: LocalDate? = null
    ) {
        viewModelScope.launch {
            val entity = ValueDayEntity(
                itemName = itemName,
                purchasePrice = purchasePrice,
                purchaseDate = purchaseDate.toEpochDay(),
                category = category,
                warrantyEndDate = warrantyEndDate?.toEpochDay()
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
