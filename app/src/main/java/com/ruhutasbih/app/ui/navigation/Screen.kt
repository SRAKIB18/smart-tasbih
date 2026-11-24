package com.ruhutasbih.app.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.ui.graphics.vector.ImageVector
import com.ruhutasbih.app.R

sealed class Screen(
    val route: String,
    @param:StringRes val titleRes: Int,
    val icon: ImageVector
) {
    object Home : Screen("home", R.string.nav_home, Icons.Default.Home)
    object TasbihList : Screen("tasbih_list", R.string.nav_tasbih, Icons.AutoMirrored.Filled.LibraryBooks)
    object Counter : Screen("counter/{tasbihId}", R.string.nav_counter, Icons.Default.TouchApp) {
        fun createRoute(tasbihId: String) = "counter/$tasbihId"
    }
    object History : Screen("history", R.string.nav_history, Icons.Default.Analytics)
    object Settings : Screen("settings", R.string.nav_settings, Icons.Default.Settings)

    companion object {
        val bottomNavItems = listOf(Home, TasbihList, History, Settings)
    }
}