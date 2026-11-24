package com.ruhutasbih.app

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.ruhutasbih.app.data.preferences.UserPreferences
import com.ruhutasbih.app.data.preferences.UserPreferencesRepository
import com.ruhutasbih.app.ui.navigation.AppNavigation
import com.ruhutasbih.app.ui.theme.SmartTasbihTheme
import com.ruhutasbih.app.ui.util.LocaleHelper

class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.setLocale(newBase, "bn"))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val preferencesRepository = UserPreferencesRepository(this)

        setContent {
            val userPrefs by preferencesRepository.userPreferencesFlow.collectAsState(
                initial = UserPreferences()
            )

            val isDarkTheme = when (userPrefs.themeMode) {
                "LIGHT" -> false
                "DARK" -> true
                else -> isSystemInDarkTheme()
            }

            SmartTasbihTheme(darkTheme = isDarkTheme) {
                AppNavigation()
            }
        }
    }
}