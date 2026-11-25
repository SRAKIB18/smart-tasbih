package com.ruhutasbih.app.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ruhutasbih.app.data.preferences.UserPreferences
import com.ruhutasbih.app.data.preferences.UserPreferencesRepository
import com.ruhutasbih.app.domain.repository.HistoryRepository
import com.ruhutasbih.app.domain.repository.TasbihRepository
import com.ruhutasbih.app.ui.util.BackupRestoreHelper
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

    fun setClickSoundType(soundType: String) {
        viewModelScope.launch {
            preferencesRepository.setClickSoundType(soundType)
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

    fun setDailyReminderTime(time: String) {
        viewModelScope.launch {
            preferencesRepository.setDailyReminderTime(time)
        }
    }

    fun addReminderTime(time: String) {
        viewModelScope.launch {
            preferencesRepository.addReminderTime(time)
        }
    }

    fun removeReminderTime(time: String) {
        viewModelScope.launch {
            preferencesRepository.removeReminderTime(time)
        }
    }

    fun restoreBackup(jsonString: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val backupData = BackupRestoreHelper.importFromJson(jsonString)
            if (backupData == null) {
                onResult(false, "Invalid or corrupted backup JSON file")
                return@launch
            }

            try {
                if (backupData.tasbihs.isNotEmpty()) {
                    tasbihRepository.insertTasbihs(backupData.tasbihs)
                }
                if (backupData.history.isNotEmpty()) {
                    historyRepository.insertProgressList(backupData.history)
                }
                onResult(true, "Successfully restored ${backupData.tasbihs.size} Tasbihs & ${backupData.history.size} History records!")
            } catch (e: Exception) {
                onResult(false, "Restore failed: ${e.localizedMessage}")
            }
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            historyRepository.clearAllHistory()
        }
    }

    fun resetAllAppData(onComplete: () -> Unit) {
        viewModelScope.launch {
            historyRepository.clearAllHistory()
            tasbihRepository.clearAllTasbihs()
            onComplete()
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