package com.ruhutasbih.app.ui.screens.settings

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTimePickerState
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ruhutasbih.app.R
import com.ruhutasbih.app.ui.components.DeveloperInfoDialog
import com.ruhutasbih.app.ui.util.NotificationHelper
import com.ruhutasbih.app.ui.util.SoundManager
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBackupJson: () -> Unit,
    onRestoreJson: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs by viewModel.userPreferences.collectAsState()
    var showClearHistoryDialog by remember { mutableStateOf(false) }
    var showDeveloperModal by remember { mutableStateOf(false) }
    var showTimePickerModal by remember { mutableStateOf(false) }

    val soundManager = remember { SoundManager(context) }
    DisposableEffect(Unit) {
        onDispose {
            soundManager.release()
        }
    }

    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets.statusBars,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings_title),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        )
                    )
                },
                actions = {
                    IconButton(onClick = { showDeveloperModal = true }) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Developer Profile",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Appearance & Language Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Palette, contentDescription = "Theme")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.sec_appearance),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(stringResource(R.string.theme_mode), style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(6.dp))
                        val themeModes = listOf("LIGHT", "DARK", "SYSTEM")
                        val themeLabels = listOf(
                            stringResource(R.string.theme_light),
                            stringResource(R.string.theme_dark),
                            stringResource(R.string.theme_system)
                        )
                        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                            themeModes.forEachIndexed { index, mode ->
                                SegmentedButton(
                                    selected = prefs.themeMode == mode,
                                    onClick = { viewModel.setThemeMode(mode) },
                                    shape = SegmentedButtonDefaults.itemShape(index = index, count = themeModes.size)
                                ) {
                                    Text(
                                        text = themeLabels[index],
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Language, contentDescription = "Language")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.app_language), style = MaterialTheme.typography.bodyMedium)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        val langModes = listOf("bn", "en")
                        val langLabels = listOf(
                            stringResource(R.string.lang_bangla),
                            stringResource(R.string.lang_english)
                        )
                        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                            langModes.forEachIndexed { index, lang ->
                                SegmentedButton(
                                    selected = prefs.language == lang,
                                    onClick = { viewModel.setLanguage(lang) },
                                    shape = SegmentedButtonDefaults.itemShape(index = index, count = langModes.size)
                                ) {
                                    Text(
                                        text = langLabels[index],
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Counter Preferences Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.TouchApp, contentDescription = "Counter")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.sec_counter_prefs),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(stringResource(R.string.click_sound_label))
                            Switch(
                                checked = prefs.soundEnabled,
                                onCheckedChange = { viewModel.setSoundEnabled(it) }
                            )
                        }

                        if (prefs.soundEnabled) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(R.string.click_sound_type),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            val soundTypes = listOf("BEEP", "WOOD_CLICK", "METALLIC_TINK", "SOFT_TICK", "WATER_DROP", "BELL_CHIME", "DUFF_TAP")
                            val soundLabels = listOf(
                                stringResource(R.string.sound_beep),
                                stringResource(R.string.sound_wood),
                                stringResource(R.string.sound_metallic),
                                stringResource(R.string.sound_soft),
                                stringResource(R.string.sound_water),
                                stringResource(R.string.sound_bell),
                                stringResource(R.string.sound_duff)
                            )
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                itemsIndexed(soundTypes) { idx, typeKey ->
                                    val isSelected = prefs.clickSoundType == typeKey
                                    AssistChip(
                                        onClick = {
                                            viewModel.setClickSoundType(typeKey)
                                            soundManager.playClickSound(typeKey)
                                        },
                                        label = {
                                            Text(
                                                text = soundLabels[idx],
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                            )
                                        },
                                        border = AssistChipDefaults.assistChipBorder(
                                            enabled = true,
                                            borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                                        )
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(stringResource(R.string.haptic_feedback_label))
                            Switch(
                                checked = prefs.vibrationEnabled,
                                onCheckedChange = { viewModel.setVibrationEnabled(it) }
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(stringResource(R.string.keep_screen_awake_label))
                            Switch(
                                checked = prefs.keepScreenAwake,
                                onCheckedChange = { viewModel.setKeepScreenAwake(it) }
                            )
                        }
                    }
                }
            }

            // Powerful Multiple Reminders & Notifications Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Notifications",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.sec_notifications),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }

                            Switch(
                                checked = prefs.dailyReminderEnabled,
                                onCheckedChange = { enabled ->
                                    viewModel.setDailyReminderEnabled(enabled)
                                    if (enabled) {
                                        NotificationHelper.scheduleMultipleReminders(context, prefs.reminderTimes)
                                        Toast.makeText(context, "Dhikr Reminders turned ON", Toast.LENGTH_SHORT).show()
                                    } else {
                                        NotificationHelper.cancelAllReminders(context)
                                        Toast.makeText(context, "Reminders turned OFF", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                        }

                        if (prefs.dailyReminderEnabled) {
                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Scheduled Reminder Times:",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                )

                                TextButton(onClick = { showTimePickerModal = true }) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Add Time",
                                        modifier = Modifier.padding(end = 4.dp)
                                    )
                                    Text("Add Time")
                                }
                            }

                            // Active Reminder Times List
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                prefs.reminderTimes.sorted().forEach { timeStr ->
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.surface,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 14.dp, vertical = 8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.Schedule,
                                                    contentDescription = "Time",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.padding(end = 8.dp)
                                                )
                                                Text(
                                                    text = format12HourTime(timeStr),
                                                    style = MaterialTheme.typography.bodyLarge.copy(
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                )
                                            }

                                            IconButton(
                                                onClick = {
                                                    val updatedSet = prefs.reminderTimes - timeStr
                                                    viewModel.removeReminderTime(timeStr)
                                                    if (prefs.dailyReminderEnabled) {
                                                        NotificationHelper.scheduleMultipleReminders(context, updatedSet)
                                                    }
                                                    Toast.makeText(context, "Removed ${format12HourTime(timeStr)}", Toast.LENGTH_SHORT).show()
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Delete",
                                                    tint = MaterialTheme.colorScheme.error
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Quick Time Presets (Fajr, Maghrib, Night)
                            Text(
                                text = "Add Quick Presets:",
                                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                item {
                                    AssistChip(
                                        onClick = {
                                            viewModel.addReminderTime("05:30")
                                            val updatedSet = prefs.reminderTimes + "05:30"
                                            NotificationHelper.scheduleMultipleReminders(context, updatedSet)
                                            Toast.makeText(context, "Added Fajr (05:30 AM)", Toast.LENGTH_SHORT).show()
                                        },
                                        label = { Text("+ Fajr (05:30 AM)") }
                                    )
                                }
                                item {
                                    AssistChip(
                                        onClick = {
                                            viewModel.addReminderTime("18:00")
                                            val updatedSet = prefs.reminderTimes + "18:00"
                                            NotificationHelper.scheduleMultipleReminders(context, updatedSet)
                                            Toast.makeText(context, "Added Maghrib (06:00 PM)", Toast.LENGTH_SHORT).show()
                                        },
                                        label = { Text("+ Maghrib (06:00 PM)") }
                                    )
                                }
                                item {
                                    AssistChip(
                                        onClick = {
                                            viewModel.addReminderTime("21:00")
                                            val updatedSet = prefs.reminderTimes + "21:00"
                                            NotificationHelper.scheduleMultipleReminders(context, updatedSet)
                                            Toast.makeText(context, "Added Night (09:00 PM)", Toast.LENGTH_SHORT).show()
                                        },
                                        label = { Text("+ Night (09:00 PM)") }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Test Notification Button
                        OutlinedButton(
                            onClick = {
                                NotificationHelper.sendTestNotification(context)
                                Toast.makeText(context, "Test Dhikr Notification Sent!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = "Test Notification",
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Send Test Notification")
                        }
                    }
                }
            }

            // Data Management & Backup Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = "Data Management",
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.sec_data_safety),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        // 1. Backup JSON
                        OutlinedButton(
                            onClick = onBackupJson,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Backup,
                                contentDescription = "Backup JSON",
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.backup_json),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }

                        // 3. Restore JSON
                        OutlinedButton(
                            onClick = onRestoreJson,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Restore,
                                contentDescription = "Restore JSON",
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.restore_json),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }

                        // 4. Clear History
                        OutlinedButton(
                            onClick = { showClearHistoryDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteForever,
                                contentDescription = "Clear History",
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.clear_history),
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }
                    }
                }
            }

            // About Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Info, contentDescription = "About")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.sec_about),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = stringResource(R.string.version_label),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.privacy_policy),
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                }
            }
        }
    }

    if (showTimePickerModal) {
        val timePickerState = rememberTimePickerState(
            initialHour = 20,
            initialMinute = 0,
            is24Hour = false
        )

        AlertDialog(
            onDismissRequest = { showTimePickerModal = false },
            title = { Text("Add Reminder Time") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TimePicker(state = timePickerState)
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val formattedTime = String.format(Locale.US, "%02d:%02d", timePickerState.hour, timePickerState.minute)
                        viewModel.addReminderTime(formattedTime)
                        val updatedSet = prefs.reminderTimes + formattedTime
                        if (prefs.dailyReminderEnabled) {
                            NotificationHelper.scheduleMultipleReminders(context, updatedSet)
                        }
                        showTimePickerModal = false
                        Toast.makeText(context, "Added reminder for ${format12HourTime(formattedTime)}", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text(stringResource(R.string.action_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePickerModal = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }

    if (showDeveloperModal) {
        DeveloperInfoDialog(onDismiss = { showDeveloperModal = false })
    }

    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            title = { Text(stringResource(R.string.confirm_clear_history_title)) },
            text = { Text(stringResource(R.string.confirm_clear_history_msg)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearHistoryDialog = false
                        viewModel.clearAllHistory()
                    }
                ) {
                    Text(stringResource(R.string.action_confirm), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearHistoryDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}

private fun format12HourTime(time24: String): String {
    return try {
        val sdf24 = SimpleDateFormat("HH:mm", Locale.US)
        val sdf12 = SimpleDateFormat("hh:mm a", Locale.US)
        val date = sdf24.parse(time24)
        date?.let { sdf12.format(it) } ?: time24
    } catch (e: Exception) {
        time24
    }
}