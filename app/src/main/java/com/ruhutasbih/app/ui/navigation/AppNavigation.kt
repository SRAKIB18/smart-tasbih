package com.ruhutasbih.app.ui.navigation

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
            countHistoryDao = database.countHistoryDao(),
            context = context.applicationContext
        )
    }
    val historyRepository = remember {
        HistoryRepositoryImpl(dailyProgressDao = database.dailyProgressDao())
    }
    val preferencesRepository = remember {
        UserPreferencesRepository(context.applicationContext)
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
                    factory = CounterViewModel.Factory(tasbihId, tasbihRepository, preferencesRepository)
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

                // SAF File Launchers

                val createJsonLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.CreateDocument("application/json")
                ) { uri ->
                    if (uri != null) {
                        scope.launch {
                            try {
                                val tasbihs = database.tasbihDao().getAllTasbihsList()
                                val historyList = historyRepository.getAllProgressList()
                                val jsonContent = BackupRestoreHelper.exportToJson(tasbihs, historyList)
                                context.contentResolver.openOutputStream(uri)?.use { os ->
                                    os.write(jsonContent.toByteArray())
                                }
                                Toast.makeText(context, "Saved Backup JSON to storage!", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                Toast.makeText(context, "Backup failed: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }

                val openJsonLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.OpenDocument()
                ) { uri ->
                    if (uri != null) {
                        scope.launch {
                            try {
                                val jsonContent = context.contentResolver.openInputStream(uri)?.use { isStream ->
                                    isStream.bufferedReader().readText()
                                } ?: ""
                                if (jsonContent.isNotBlank()) {
                                    settingsViewModel.restoreBackup(jsonContent) { success, msg ->
                                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                    }
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Restore failed: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }

                val dateStr = SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(Date())

                SettingsScreen(
                    viewModel = settingsViewModel,
                    onBackupJson = {
                        createJsonLauncher.launch("RuhuTasbih_Backup_$dateStr.json")
                    },
                    onRestoreJson = {
                        openJsonLauncher.launch(arrayOf("application/json", "text/plain", "*/*"))
                    }
                )
            }
        }
    }
}