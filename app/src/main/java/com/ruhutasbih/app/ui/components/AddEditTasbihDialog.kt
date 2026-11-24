package com.ruhutasbih.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ruhutasbih.app.R
import com.ruhutasbih.app.data.local.entity.TasbihEntity

@Composable
fun AddEditTasbihDialog(
    initialTasbih: TasbihEntity? = null,
    onDismiss: () -> Unit,
    onConfirm: (TasbihEntity) -> Unit
) {
    var name by remember { mutableStateOf(initialTasbih?.name ?: "") }
    var arabicText by remember { mutableStateOf(initialTasbih?.arabicText ?: "") }
    var transliteration by remember { mutableStateOf(initialTasbih?.transliteration ?: "") }
    var meaning by remember { mutableStateOf(initialTasbih?.meaning ?: "") }
    var dailyTargetStr by remember { mutableStateOf((initialTasbih?.dailyTarget ?: 100L).toString()) }
    var previousPendingStr by remember { mutableStateOf((initialTasbih?.previousPending ?: 0L).toString()) }
    var category by remember { mutableStateOf(initialTasbih?.category ?: "Daily") }
    var notes by remember { mutableStateOf(initialTasbih?.notes ?: "") }

    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialTasbih == null) stringResource(R.string.add_new_tasbih) else stringResource(R.string.action_edit),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        isError = false
                    },
                    label = { Text(stringResource(R.string.tasbih_name)) },
                    isError = isError,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = arabicText,
                    onValueChange = { arabicText = it },
                    label = { Text(stringResource(R.string.arabic_text)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = transliteration,
                    onValueChange = { transliteration = it },
                    label = { Text(stringResource(R.string.transliteration)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = meaning,
                    onValueChange = { meaning = it },
                    label = { Text(stringResource(R.string.meaning)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = dailyTargetStr,
                    onValueChange = { dailyTargetStr = it.filter { char -> char.isDigit() } },
                    label = { Text(stringResource(R.string.daily_target)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = previousPendingStr,
                    onValueChange = { previousPendingStr = it.filter { char -> char.isDigit() } },
                    label = { Text(stringResource(R.string.previous_pending)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text(stringResource(R.string.category)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(stringResource(R.string.notes)) },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isBlank()) {
                        isError = true
                        return@TextButton
                    }

                    val target = dailyTargetStr.toLongOrNull() ?: 100L
                    val pending = previousPendingStr.toLongOrNull() ?: 0L

                    val id = initialTasbih?.id ?: "TASBIH_${System.currentTimeMillis()}"
                    val updated = TasbihEntity(
                        id = id,
                        name = name.trim(),
                        arabicText = arabicText.trim(),
                        transliteration = transliteration.trim(),
                        meaning = meaning.trim(),
                        dailyTarget = maxOf(1L, target),
                        previousPending = maxOf(0L, pending),
                        todayReading = initialTasbih?.todayReading ?: 0L,
                        lifetimeCount = initialTasbih?.lifetimeCount ?: 0L,
                        createdAt = initialTasbih?.createdAt ?: System.currentTimeMillis(),
                        isArchived = initialTasbih?.isArchived ?: false,
                        isPinned = initialTasbih?.isPinned ?: false,
                        category = category.ifBlank { "General" },
                        notes = notes.trim()
                    )

                    onConfirm(updated)
                }
            ) {
                Text(stringResource(R.string.action_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    )
}