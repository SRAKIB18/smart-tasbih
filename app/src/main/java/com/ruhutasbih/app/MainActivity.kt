package com.ruhutasbih.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.ruhutasbih.app.data.preferences.UserPreferences
import com.ruhutasbih.app.data.preferences.UserPreferencesRepository
import com.ruhutasbih.app.ui.navigation.AppNavigation
import com.ruhutasbih.app.ui.theme.SmartTasbihTheme
import com.ruhutasbih.app.ui.util.LocaleHelper

class MainActivity : ComponentActivity() {

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

            // Dynamically update LocaleContext when language preference changes
            val localeContext = remember(userPrefs.language) {
                LocaleHelper.setLocale(this@MainActivity, userPrefs.language)
            }

            CompositionLocalProvider(LocalContext provides localeContext) {
                SmartTasbihTheme(darkTheme = isDarkTheme) {
                    AppNavigation()
                }
            }
        }
    }
}