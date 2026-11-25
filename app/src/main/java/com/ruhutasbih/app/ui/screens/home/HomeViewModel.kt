package com.ruhutasbih.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ruhutasbih.app.data.local.entity.TasbihEntity
import com.ruhutasbih.app.domain.engine.StreakResult
import com.ruhutasbih.app.domain.repository.TasbihRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val tasbihList: List<TasbihEntity> = emptyList(),
    val todayTotalReading: Long = 0L,
    val completedTargetsCount: Int = 0,
    val activeTasbihsCount: Int = 0,
    val overallProgress: Float = 0f,
    val searchQuery: String = "",
    val filterMode: String = "ALL", // "ALL", "PINNED", "ACTIVE", "ARCHIVED"
)

class HomeViewModel(
    private val repository: TasbihRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _filterMode = MutableStateFlow("ALL")

    val uiState: StateFlow<HomeUiState> = combine(
        repository.getAllTasbihs(),
        _searchQuery,
        _filterMode
    ) { tasbihs, query, filter ->
        viewModelScope.launch {
            repository.checkAndApplyDateRollover()
        }

        val filteredByMode = when (filter) {
            "PINNED" -> tasbihs.filter { it.isPinned && !it.isArchived }
            "ACTIVE" -> tasbihs.filter { !it.isArchived }
            "ARCHIVED" -> tasbihs.filter { it.isArchived }
            else -> tasbihs.filter { !it.isArchived }
        }

        val finalFiltered = if (query.isBlank()) {
            filteredByMode
        } else {
            filteredByMode.filter {
                it.name.contains(query, ignoreCase = true) ||
                it.transliteration.contains(query, ignoreCase = true) ||
                it.meaning.contains(query, ignoreCase = true) ||
                it.arabicText.contains(query)
            }
        }

        val activeTasbihs = tasbihs.filter { !it.isArchived }
        val todayTotal = activeTasbihs.sumOf { it.todayReading }
        val completedCount = activeTasbihs.count { it.todayReading >= it.dailyTarget }
        val activeCount = activeTasbihs.size

        val totalTargets = activeTasbihs.sumOf { it.dailyTarget }
        val overallProgress = if (totalTargets > 0) {
            (todayTotal.toFloat() / totalTargets.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }

        HomeUiState(
            tasbihList = finalFiltered,
            todayTotalReading = todayTotal,
            completedTargetsCount = completedCount,
            activeTasbihsCount = activeCount,
            overallProgress = overallProgress,
            searchQuery = query,
            filterMode = filter
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

    val streakResult: StateFlow<StreakResult> = repository.getAllTasbihs().map { tasbihs ->
        val active = tasbihs.filter { !it.isArchived }
        val completedCount = active.count { it.todayReading >= it.dailyTarget }
        val streak = if (completedCount > 0) 1 else 0
        StreakResult(
            currentStreak = streak,
            bestStreak = streak,
            totalCompletedDays = streak,
            totalLifetimeCount = active.sumOf { it.lifetimeCount }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = StreakResult()
    )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onFilterModeChanged(filter: String) {
        _filterMode.value = filter
    }

    fun togglePin(id: String, currentPinned: Boolean) {
        viewModelScope.launch {
            repository.setPinnedState(id, !currentPinned)
        }
    }

    fun toggleArchive(id: String, currentArchived: Boolean) {
        viewModelScope.launch {
            repository.setArchivedState(id, !currentArchived)
        }
    }

    fun deleteTasbih(id: String) {
        viewModelScope.launch {
            repository.deleteTasbih(id)
        }
    }

    fun quickIncrement(id: String) {
        viewModelScope.launch {
            repository.incrementCount(id, 1L)
        }
    }

    class Factory(private val repository: TasbihRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HomeViewModel(repository) as T
        }
    }
}