package com.ruhutasbih.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "count_history_table",
    foreignKeys = [
        ForeignKey(
            entity = TasbihEntity::class,
            parentColumns = ["id"],
            childColumns = ["tasbihId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["tasbihId"]), Index(value = ["date"])]
)
data class CountHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val tasbihId: String,
    val date: String, // "yyyy-MM-dd"
    val countDelta: Long,
    val timestamp: Long = System.currentTimeMillis()
)