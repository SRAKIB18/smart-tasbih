package com.ruhutasbih.app.ui.util

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import java.util.Locale

object LocaleHelper {
    fun setLocale(context: Context, languageCode: String): Context {
        val locale = when (languageCode.lowercase()) {
            "bn", "bangla" -> Locale("bn")
            else -> Locale("en")
        }
        Locale.setDefault(locale)

        val resources = context.resources
        val config = Configuration(resources.configuration)
        config.setLocale(locale)
        @Suppress("DEPRECATION")
        resources.updateConfiguration(config, resources.displayMetrics)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.createConfigurationContext(config)
        } else {
            context
        }
    }
}