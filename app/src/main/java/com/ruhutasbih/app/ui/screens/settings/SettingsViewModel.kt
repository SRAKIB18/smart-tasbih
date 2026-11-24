package com.ruhutasbih.app.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ruhutasbih.app.data.preferences.UserPreferences
import com.ruhutasbih.app.data.preferences.UserPreferencesRepository
import com.ruhutasbih.app.domain.repository.HistoryRepository
import com.ruhutasbih.app.domain.repository.TasbihRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val preferencesRepository: UserPreferencesRepository,
    private val tasbihRepository: TasbihRepository,
    private val historyRepository: HistoryRepository
) : ViewModel() {

    val userPreferences: StateFlow<UserPreferences> = preferencesRepository.userPreferencesFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UserPreferences()
    )

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            preferencesRepository.setThemeMode(mode)
        }
    }

    fun setLanguage(language: String) {
        viewModelScope.launch {
            preferencesRepository.setLanguage(language)
        }
    }

    fun setSoundEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setSoundEnabled(enabled)
        }
    }

    fun setVibrationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setVibrationEnabled(enabled)
        }
    }

    fun setKeepScreenAwake(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setKeepScreenAwake(enabled)
        }
    }

    fun setDefaultDailyTarget(target: Long) {
        viewModelScope.launch {
            preferencesRepository.setDefaultDailyTarget(target)
        }
    }

    fun setDailyReminderEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setDailyReminderEnabled(enabled)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            historyRepository.clearAllHistory()
        }
    }

    class Factory(
        private val preferencesRepository: UserPreferencesRepository,
        private val tasbihRepository: TasbihRepository,
        private val historyRepository: HistoryRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(preferencesRepository, tasbihRepository, historyRepository) as T
        }
    }
}