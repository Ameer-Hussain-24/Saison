package takagi.ru.saison.ui.screens.event

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import takagi.ru.saison.domain.model.Event
import takagi.ru.saison.domain.model.EventCategory
import takagi.ru.saison.domain.repository.EventRepository
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class EventViewModel @Inject constructor(
    private val eventRepository: EventRepository
) : ViewModel() {
    
    private val _selectedCategory = MutableStateFlow<EventCategory?>(null)
    val selectedCategory: StateFlow<EventCategory?> = _selectedCategory.asStateFlow()
    
    private val _uiState = MutableStateFlow<EventUiState>(EventUiState.Loading)
    val uiState: StateFlow<EventUiState> = _uiState.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    val events: StateFlow<List<Event>> = combine(
        eventRepository.getAllEvents(),
        _selectedCategory,
        _searchQuery
    ) { allEvents, category, query ->
        var filteredEvents = allEvents
        
        // 按类别筛选
        if (category != null) {
            filteredEvents = filteredEvents.filter { it.category == category }
        }
        
        // 按搜索关键词筛选
        if (query.isNotBlank()) {
            filteredEvents = filteredEvents.filter { event ->
                event.title.contains(query, ignoreCase = true) ||
                event.description?.contains(query, ignoreCase = true) == true
            }
        }
        
        // 按距离今天的天数排序：即将发生的事件优先（天数越少越靠前）
        filteredEvents.sortedBy { event ->
            val daysUntil = takagi.ru.saison.util.EventDateCalculator.calculateDaysUntilForCategory(
                event.eventDate, 
                event.category
            )
            // 使用绝对值确保今天的事件（0天）排在最前面
            // 然后是1天、2天...以此类推
            kotlin.math.abs(daysUntil)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    init {
        loadEvents()
    }
    
    private fun loadEvents() {
        viewModelScope.launch {
            _uiState.value = EventUiState.Loading
            try {
                events.collect { eventList ->
                    _uiState.value = if (eventList.isEmpty()) {
                        EventUiState.Empty
                    } else {
                        EventUiState.Success
                    }
                }
            } catch (e: Exception) {
                _uiState.value = EventUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
    
    fun setCategory(category: EventCategory?) {
        _selectedCategory.value = category
    }
    
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    fun createEvent(event: Event) {
        viewModelScope.launch {
            try {
                eventRepository.insertEvent(event)
            } catch (e: Exception) {
                _uiState.value = EventUiState.Error(e.message ?: "Failed to create event")
            }
        }
    }
    
    fun updateEvent(event: Event) {
        viewModelScope.launch {
            try {
                eventRepository.updateEvent(event)
            } catch (e: Exception) {
                _uiState.value = EventUiState.Error(e.message ?: "Failed to update event")
            }
        }
    }
    
    fun deleteEvent(eventId: Long) {
        viewModelScope.launch {
            try {
                eventRepository.deleteEvent(eventId)
            } catch (e: Exception) {
                _uiState.value = EventUiState.Error(e.message ?: "Failed to delete event")
            }
        }
    }
    
    fun toggleEventCompletion(eventId: Long) {
        viewModelScope.launch {
            try {
                val event = eventRepository.getEventByIdSync(eventId)
                event?.let {
                    val updatedEvent = it.copy(
                        isCompleted = !it.isCompleted,
                        updatedAt = LocalDateTime.now()
                    )
                    eventRepository.updateEvent(updatedEvent)
                }
            } catch (e: Exception) {
                _uiState.value = EventUiState.Error(e.message ?: "Failed to toggle event completion")
            }
        }
    }
}

sealed class EventUiState {
    object Loading : EventUiState()
    object Empty : EventUiState()
    object Success : EventUiState()
    data class Error(val message: String) : EventUiState()
}
