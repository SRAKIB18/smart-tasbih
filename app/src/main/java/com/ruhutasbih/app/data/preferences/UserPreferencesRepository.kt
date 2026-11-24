package com.ruhutasbih.app.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

data class UserPreferences(
    val themeMode: String = "SYSTEM", // "LIGHT", "DARK", "SYSTEM"
    val language: String = "bn", // "bn", "en"
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val keepScreenAwake: Boolean = false,
    val defaultDailyTarget: Long = 100L,
    val dailyReminderEnabled: Boolean = false,
    val dailyReminderTime: String = "20:00",
    val reminderTimes: Set<String> = setOf("05:30", "18:00", "21:00")
)

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val LANGUAGE = stringPreferencesKey("language")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
        val KEEP_SCREEN_AWAKE = booleanPreferencesKey("keep_screen_awake")
        val DEFAULT_DAILY_TARGET = longPreferencesKey("default_daily_target")
        val DAILY_REMINDER_ENABLED = booleanPreferencesKey("daily_reminder_enabled")
        val DAILY_REMINDER_TIME = stringPreferencesKey("daily_reminder_time")
        val REMINDER_TIMES = stringSetPreferencesKey("reminder_times")
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { preferences ->
        UserPreferences(
            themeMode = preferences[PreferencesKeys.THEME_MODE] ?: "SYSTEM",
            language = preferences[PreferencesKeys.LANGUAGE] ?: "bn",
            soundEnabled = preferences[PreferencesKeys.SOUND_ENABLED] ?: true,
            vibrationEnabled = preferences[PreferencesKeys.VIBRATION_ENABLED] ?: true,
            keepScreenAwake = preferences[PreferencesKeys.KEEP_SCREEN_AWAKE] ?: false,
            defaultDailyTarget = preferences[PreferencesKeys.DEFAULT_DAILY_TARGET] ?: 100L,
            dailyReminderEnabled = preferences[PreferencesKeys.DAILY_REMINDER_ENABLED] ?: false,
            dailyReminderTime = preferences[PreferencesKeys.DAILY_REMINDER_TIME] ?: "20:00",
            reminderTimes = preferences[PreferencesKeys.REMINDER_TIMES] ?: setOf("05:30", "18:00", "21:00")
        )
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = mode
        }
    }

    suspend fun setLanguage(language: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LANGUAGE] = language
        }
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SOUND_ENABLED] = enabled
        }
    }

    suspend fun setVibrationEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.VIBRATION_ENABLED] = enabled
        }
    }

    suspend fun setKeepScreenAwake(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.KEEP_SCREEN_AWAKE] = enabled
        }
    }

    suspend fun setDefaultDailyTarget(target: Long) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DEFAULT_DAILY_TARGET] = target
        }
    }

    suspend fun setDailyReminderEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DAILY_REMINDER_ENABLED] = enabled
        }
    }

    suspend fun setDailyReminderTime(time: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DAILY_REMINDER_TIME] = time
        }
    }

    suspend fun addReminderTime(time: String) {
        context.dataStore.edit { preferences ->
            val current = preferences[PreferencesKeys.REMINDER_TIMES] ?: setOf("05:30", "18:00", "21:00")
            preferences[PreferencesKeys.REMINDER_TIMES] = current + time
        }
    }

    suspend fun removeReminderTime(time: String) {
        context.dataStore.edit { preferences ->
            val current = preferences[PreferencesKeys.REMINDER_TIMES] ?: setOf("05:30", "18:00", "21:00")
            preferences[PreferencesKeys.REMINDER_TIMES] = current - time
        }
    }
}