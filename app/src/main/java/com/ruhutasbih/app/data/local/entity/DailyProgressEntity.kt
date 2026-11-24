package com.ruhutasbih.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "daily_progress_table",
    foreignKeys = [
        ForeignKey(
            entity = TasbihEntity::class,
            parentColumns = ["id"],
            childColumns = ["tasbihId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["tasbihId", "date"], unique = true)]
)
data class DailyProgressEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val tasbihId: String,
    val date: String, // format "yyyy-MM-dd"
    val dailyTarget: Long,
    val todayReading: Long,
    val dailyCount: Long,
    val pendingAtStart: Long,
    val pendingRemainingAtEnd: Long,
    val nextDayPending: Long,
    val isCompleted: Boolean = false
)