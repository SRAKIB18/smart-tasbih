package com.ruhutasbih.app.ui.screens.counter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ruhutasbih.app.data.local.entity.TasbihEntity
import com.ruhutasbih.app.domain.repository.TasbihRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CounterViewModel(
    private val tasbihId: String,
    private val repository: TasbihRepository
) : ViewModel() {

    private val _tasbih = MutableStateFlow<TasbihEntity?>(null)
    val tasbih: StateFlow<TasbihEntity?> = _tasbih.asStateFlow()

    private val _soundEnabled = MutableStateFlow(true)
    val soundEnabled: StateFlow<Boolean> = _soundEnabled.asStateFlow()

    private val _vibrationEnabled = MutableStateFlow(true)
    val vibrationEnabled: StateFlow<Boolean> = _vibrationEnabled.asStateFlow()

    private val _keepScreenAwake = MutableStateFlow(false)
    val keepScreenAwake: StateFlow<Boolean> = _keepScreenAwake.asStateFlow()

    private val _isFullScreen = MutableStateFlow(false)
    val isFullScreen: StateFlow<Boolean> = _isFullScreen.asStateFlow()

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
        _soundEnabled.value = !_soundEnabled.value
    }

    fun toggleVibration() {
        _vibrationEnabled.value = !_vibrationEnabled.value
    }

    fun toggleKeepScreenAwake() {
        _keepScreenAwake.value = !_keepScreenAwake.value
    }

    fun toggleFullScreen() {
        _isFullScreen.value = !_isFullScreen.value
    }

    class Factory(
        private val tasbihId: String,
        private val repository: TasbihRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CounterViewModel(tasbihId, repository) as T
        }
    }
}