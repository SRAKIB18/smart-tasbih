package com.ruhutasbih.app.ui.navigation

import android.widget.Toast
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ruhutasbih.app.data.local.AppDatabase
import com.ruhutasbih.app.data.preferences.UserPreferencesRepository
import com.ruhutasbih.app.data.repository.HistoryRepositoryImpl
import com.ruhutasbih.app.data.repository.TasbihRepositoryImpl
import com.ruhutasbih.app.ui.screens.counter.CounterScreen
import com.ruhutasbih.app.ui.screens.counter.CounterViewModel
import com.ruhutasbih.app.ui.screens.history.HistoryScreen
import com.ruhutasbih.app.ui.screens.history.HistoryViewModel
import com.ruhutasbih.app.ui.screens.home.HomeScreen
import com.ruhutasbih.app.ui.screens.home.HomeViewModel
import com.ruhutasbih.app.ui.screens.settings.SettingsScreen
import com.ruhutasbih.app.ui.screens.settings.SettingsViewModel
import com.ruhutasbih.app.ui.screens.tasbih.TasbihListScreen
import com.ruhutasbih.app.ui.util.BackupRestoreHelper
import kotlinx.coroutines.launch

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Initialize Database and Repositories
    val database = remember { AppDatabase.getDatabase(context) }
    val tasbihRepository = remember {
        TasbihRepositoryImpl(
            tasbihDao = database.tasbihDao(),
            dailyProgressDao = database.dailyProgressDao(),
            countHistoryDao = database.countHistoryDao()
        )
    }
    val historyRepository = remember {
        HistoryRepositoryImpl(dailyProgressDao = database.dailyProgressDao())
    }
    val preferencesRepository = remember {
        UserPreferencesRepository(context)
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Hide bottom bar on Counter screen for distraction-free reading
    val showBottomBar = currentRoute in Screen.bottomNavItems.map { it.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    Screen.bottomNavItems.forEach { screen ->
                        val isSelected = currentRoute == screen.route
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = screen.icon,
                                    contentDescription = stringResource(screen.titleRes)
                                )
                            },
                            label = { Text(stringResource(screen.titleRes)) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            // Home Destination
            composable(Screen.Home.route) {
                val homeViewModel: HomeViewModel = viewModel(
                    factory = HomeViewModel.Factory(tasbihRepository)
                )
                HomeScreen(
                    viewModel = homeViewModel,
                    onNavigateToCounter = { tasbihId ->
                        navController.navigate(Screen.Counter.createRoute(tasbihId))
                    },
                    onSaveTasbih = { tasbih ->
                        scope.launch {
                            tasbihRepository.insertTasbih(tasbih)
                        }
                    }
                )
            }

            // Tasbih Library Destination
            composable(Screen.TasbihList.route) {
                val homeViewModel: HomeViewModel = viewModel(
                    factory = HomeViewModel.Factory(tasbihRepository)
                )
                TasbihListScreen(
                    viewModel = homeViewModel,
                    onNavigateToCounter = { tasbihId ->
                        navController.navigate(Screen.Counter.createRoute(tasbihId))
                    },
                    onSaveTasbih = { tasbih ->
                        scope.launch {
                            tasbihRepository.insertTasbih(tasbih)
                        }
                    }
                )
            }

            // Counter Destination
            composable(
                route = Screen.Counter.route,
                arguments = listOf(navArgument("tasbihId") { type = NavType.StringType })
            ) { backStackEntry ->
                val tasbihId = backStackEntry.arguments?.getString("tasbihId") ?: ""
                val counterViewModel: CounterViewModel = viewModel(
                    factory = CounterViewModel.Factory(tasbihId, tasbihRepository)
                )
                CounterScreen(
                    viewModel = counterViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // History Destination
            composable(Screen.History.route) {
                val historyViewModel: HistoryViewModel = viewModel(
                    factory = HistoryViewModel.Factory(historyRepository, tasbihRepository)
                )
                HistoryScreen(viewModel = historyViewModel)
            }

            // Settings Destination
            composable(Screen.Settings.route) {
                val settingsViewModel: SettingsViewModel = viewModel(
                    factory = SettingsViewModel.Factory(
                        preferencesRepository = preferencesRepository,
                        tasbihRepository = tasbihRepository,
                        historyRepository = historyRepository
                    )
                )
                SettingsScreen(
                    viewModel = settingsViewModel,
                    onExportCsv = {
                        scope.launch {
                            val historyList = historyRepository.getAllProgressList()
                            val csvContent = BackupRestoreHelper.exportToCsv(historyList)
                            Toast.makeText(context, "CSV exported (${csvContent.length} bytes)", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onBackupJson = {
                        scope.launch {
                            val tasbihs = database.tasbihDao().getAllTasbihsList()
                            val historyList = historyRepository.getAllProgressList()
                            val jsonContent = BackupRestoreHelper.exportToJson(tasbihs, historyList)
                            Toast.makeText(context, "Backup JSON generated (${jsonContent.length} chars)", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onRestoreJson = {
                        Toast.makeText(context, "Ready to restore backup JSON", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }
}