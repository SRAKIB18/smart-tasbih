package com.ruhutasbih.app.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ruhutasbih.app.R
import com.ruhutasbih.app.data.local.entity.TasbihEntity
import com.ruhutasbih.app.ui.components.AddEditTasbihDialog
import com.ruhutasbih.app.ui.components.StatCard
import com.ruhutasbih.app.ui.components.TasbihCard
import com.ruhutasbih.app.ui.util.AudioReciter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToCounter: (String) -> Unit,
    onSaveTasbih: (TasbihEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var tasbihToEdit by remember { mutableStateOf<TasbihEntity?>(null) }

    val audioReciter = remember { AudioReciter(context) }
    DisposableEffect(Unit) {
        onDispose {
            audioReciter.release()
        }
    }

    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets.statusBars,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.greeting_title),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        )
                        Text(
                            text = stringResource(R.string.greeting_subtitle),
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    tasbihToEdit = null
                    showAddDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.add_new_tasbih)
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Dashboard Stat Grid (2x2)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatCard(
                            title = stringResource(R.string.stat_today_count),
                            value = uiState.todayTotalReading.toString(),
                            icon = Icons.Default.TouchApp,
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = stringResource(R.string.stat_targets_completed),
                            value = uiState.completedTargetsCount.toString(),
                            icon = Icons.Default.CheckCircle,
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatCard(
                            title = stringResource(R.string.stat_active_tasbihs),
                            value = uiState.activeTasbihsCount.toString(),
                            icon = Icons.AutoMirrored.Filled.FormatListBulleted,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = stringResource(R.string.stat_overall_progress),
                            value = "${(uiState.overallProgress * 100).toInt()}%",
                            icon = Icons.AutoMirrored.Filled.TrendingUp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Search Bar & Filters
            item {
                Column {
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = { viewModel.onSearchQueryChanged(it) },
                        placeholder = { Text(stringResource(R.string.search_placeholder)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search"
                            )
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = uiState.filterMode == "ALL",
                                onClick = { viewModel.onFilterModeChanged("ALL") },
                                label = { Text(stringResource(R.string.filter_all)) }
                            )
                        }
                        item {
                            FilterChip(
                                selected = uiState.filterMode == "PINNED",
                                onClick = { viewModel.onFilterModeChanged("PINNED") },
                                label = { Text(stringResource(R.string.filter_pinned)) }
                            )
                        }
                        item {
                            FilterChip(
                                selected = uiState.filterMode == "ACTIVE",
                                onClick = { viewModel.onFilterModeChanged("ACTIVE") },
                                label = { Text(stringResource(R.string.filter_active)) }
                            )
                        }
                        item {
                            FilterChip(
                                selected = uiState.filterMode == "ARCHIVED",
                                onClick = { viewModel.onFilterModeChanged("ARCHIVED") },
                                label = { Text(stringResource(R.string.filter_archived)) }
                            )
                        }
                    }
                }
            }

            // Section Header
            item {
                Text(
                    text = stringResource(R.string.all_tasbih),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                )
            }

            // Empty State or List
            if (uiState.tasbihList.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.no_history_records),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            } else {
                items(
                    items = uiState.tasbihList,
                    key = { it.id }
                ) { tasbih ->
                    TasbihCard(
                        tasbih = tasbih,
                        onClick = { onNavigateToCounter(tasbih.id) },
                        onPinToggle = { viewModel.togglePin(tasbih.id, tasbih.isPinned) },
                        onArchiveToggle = { viewModel.toggleArchive(tasbih.id, tasbih.isArchived) },
                        onEdit = {
                            tasbihToEdit = tasbih
                            showAddDialog = true
                        },
                        onDuplicate = {
                            val duplicated = tasbih.copy(
                                id = "TASBIH_${System.currentTimeMillis()}",
                                name = "${tasbih.name} (Copy)",
                                todayReading = 0L,
                                lifetimeCount = 0L,
                                createdAt = System.currentTimeMillis()
                            )
                            tasbihToEdit = duplicated
                            showAddDialog = true
                        },
                        onDelete = { viewModel.deleteTasbih(tasbih.id) },
                        onQuickIncrement = { viewModel.quickIncrement(tasbih.id) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddEditTasbihDialog(
            initialTasbih = tasbihToEdit,
            onDismiss = { showAddDialog = false },
            onConfirm = { updatedTasbih ->
                showAddDialog = false
                tasbihToEdit = null
                onSaveTasbih(updatedTasbih)
            }
        )
    }
}