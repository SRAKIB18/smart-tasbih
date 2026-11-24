package com.ruhutasbih.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasbih_table")
data class TasbihEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val arabicText: String,
    val transliteration: String,
    val meaning: String,
    val dailyTarget: Long,
    val previousPending: Long = 0L,
    val todayReading: Long = 0L,
    val dailyCount: Long = 0L,
    val previousPendingRemaining: Long = 0L,
    val nextDayPending: Long = 0L,
    val lifetimeCount: Long = 0L,
    val lastUpdatedDate: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val isArchived: Boolean = false,
    val isPinned: Boolean = false,
    val category: String = "General",
    val notes: String = ""
)