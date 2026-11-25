package com.ruhutasbih.app.ui.screens.counter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ruhutasbih.app.data.local.entity.TasbihEntity
import com.ruhutasbih.app.data.preferences.UserPreferences
import com.ruhutasbih.app.data.preferences.UserPreferencesRepository
import com.ruhutasbih.app.domain.repository.TasbihRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CounterViewModel(
    private val tasbihId: String,
    private val repository: TasbihRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val userPreferences: StateFlow<UserPreferences> = preferencesRepository.userPreferencesFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UserPreferences()
    )

    val soundEnabled: StateFlow<Boolean> = userPreferences
        .map { it.soundEnabled }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    val vibrationEnabled: StateFlow<Boolean> = userPreferences
        .map { it.vibrationEnabled }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    val keepScreenAwake: StateFlow<Boolean> = userPreferences
        .map { it.keepScreenAwake }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    private val _tasbih = MutableStateFlow<TasbihEntity?>(null)
    val tasbih: StateFlow<TasbihEntity?> = _tasbih.asStateFlow()

    private val _isFullScreen = MutableStateFlow(false)
    val isFullScreen: StateFlow<Boolean> = _isFullScreen.asStateFlow()

    private val _isLocked = MutableStateFlow(false)
    val isLocked: StateFlow<Boolean> = _isLocked.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getTasbihByIdFlow(tasbihId).collect {
                _tasbih.value = it
            }
        }
    }

    fun incrementCount(delta: Long = 1L) {
        viewModelScope.launch {
            repository.incrementCount(tasbihId, delta)
        }
    }

    fun undoCount() {
        viewModelScope.launch {
            repository.undoCount(tasbihId)
        }
    }

    fun resetTodayCount() {
        viewModelScope.launch {
            repository.resetTodayCount(tasbihId)
        }
    }

    fun toggleSound() {
        viewModelScope.launch {
            preferencesRepository.setSoundEnabled(!userPreferences.value.soundEnabled)
        }
    }

    fun toggleVibration() {
        viewModelScope.launch {
            preferencesRepository.setVibrationEnabled(!userPreferences.value.vibrationEnabled)
        }
    }

    fun toggleKeepScreenAwake() {
        viewModelScope.launch {
            preferencesRepository.setKeepScreenAwake(!userPreferences.value.keepScreenAwake)
        }
    }

    fun toggleFullScreen() {
        _isFullScreen.value = !_isFullScreen.value
    }

    fun toggleLock() {
        _isLocked.value = !_isLocked.value
    }

    fun setCounterSkin(skin: String) {
        viewModelScope.launch {
            preferencesRepository.setCounterSkin(skin)
        }
    }

    class Factory(
        private val tasbihId: String,
        private val repository: TasbihRepository,
        private val preferencesRepository: UserPreferencesRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CounterViewModel(tasbihId, repository, preferencesRepository) as T
        }
    }
}