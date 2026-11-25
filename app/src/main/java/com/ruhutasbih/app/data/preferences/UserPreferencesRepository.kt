package com.ruhutasbih.app.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

data class UserPreferences(
    val themeMode: String = "SYSTEM", // "LIGHT", "DARK", "SYSTEM"
    val language: String = "bn", // "bn", "en"
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val keepScreenAwake: Boolean = false,
    val defaultDailyTarget: Long = 100L,
    val dailyReminderEnabled: Boolean = false,
    val dailyReminderTime: String = "20:00",
    val reminderTimes: Set<String> = setOf("05:30", "18:00", "21:00"),
    val counterSkin: String = "DIGITAL_LED", // "DIGITAL_LED", "WOODEN_BEADS", "CRYSTAL_BEADS", "METALLIC_RING", etc.
    val clickSoundType: String = "BEEP" // "BEEP", "WOOD_CLICK", "METALLIC_TINK", "SOFT_TICK"
)

class UserPreferencesRepository(context: Context) {

    private val appContext = context.applicationContext

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
        val COUNTER_SKIN = stringPreferencesKey("counter_skin")
        val CLICK_SOUND_TYPE = stringPreferencesKey("click_sound_type")
    }

    val userPreferencesFlow: Flow<UserPreferences> = appContext.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            UserPreferences(
                themeMode = preferences[PreferencesKeys.THEME_MODE] ?: "SYSTEM",
                language = preferences[PreferencesKeys.LANGUAGE] ?: "bn",
                soundEnabled = preferences[PreferencesKeys.SOUND_ENABLED] ?: true,
                vibrationEnabled = preferences[PreferencesKeys.VIBRATION_ENABLED] ?: true,
                keepScreenAwake = preferences[PreferencesKeys.KEEP_SCREEN_AWAKE] ?: false,
                defaultDailyTarget = preferences[PreferencesKeys.DEFAULT_DAILY_TARGET] ?: 100L,
                dailyReminderEnabled = preferences[PreferencesKeys.DAILY_REMINDER_ENABLED] ?: false,
                dailyReminderTime = preferences[PreferencesKeys.DAILY_REMINDER_TIME] ?: "20:00",
                reminderTimes = preferences[PreferencesKeys.REMINDER_TIMES] ?: setOf("05:30", "18:00", "21:00"),
                counterSkin = preferences[PreferencesKeys.COUNTER_SKIN] ?: "DIGITAL_LED",
                clickSoundType = preferences[PreferencesKeys.CLICK_SOUND_TYPE] ?: "BEEP"
            )
        }

    suspend fun setThemeMode(mode: String) {
        appContext.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = mode
        }
    }

    suspend fun setLanguage(language: String) {
        appContext.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LANGUAGE] = language
        }
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        appContext.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SOUND_ENABLED] = enabled
        }
    }

    suspend fun setVibrationEnabled(enabled: Boolean) {
        appContext.dataStore.edit { preferences ->
            preferences[PreferencesKeys.VIBRATION_ENABLED] = enabled
        }
    }

    suspend fun setKeepScreenAwake(enabled: Boolean) {
        appContext.dataStore.edit { preferences ->
            preferences[PreferencesKeys.KEEP_SCREEN_AWAKE] = enabled
        }
    }

    suspend fun setDefaultDailyTarget(target: Long) {
        appContext.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DEFAULT_DAILY_TARGET] = target
        }
    }

    suspend fun setDailyReminderEnabled(enabled: Boolean) {
        appContext.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DAILY_REMINDER_ENABLED] = enabled
        }
    }

    suspend fun setDailyReminderTime(time: String) {
        appContext.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DAILY_REMINDER_TIME] = time
        }
    }

    suspend fun addReminderTime(time: String) {
        appContext.dataStore.edit { preferences ->
            val current = preferences[PreferencesKeys.REMINDER_TIMES] ?: setOf("05:30", "18:00", "21:00")
            preferences[PreferencesKeys.REMINDER_TIMES] = current + time
        }
    }

    suspend fun removeReminderTime(time: String) {
        appContext.dataStore.edit { preferences ->
            val current = preferences[PreferencesKeys.REMINDER_TIMES] ?: setOf("05:30", "18:00", "21:00")
            preferences[PreferencesKeys.REMINDER_TIMES] = current - time
        }
    }

    suspend fun setCounterSkin(skin: String) {
        appContext.dataStore.edit { preferences ->
            preferences[PreferencesKeys.COUNTER_SKIN] = skin
        }
    }

    suspend fun setClickSoundType(soundType: String) {
        appContext.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CLICK_SOUND_TYPE] = soundType
        }
    }
}