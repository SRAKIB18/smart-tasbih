package com.ruhutasbih.app.domain.repository

import com.ruhutasbih.app.data.local.entity.DailyProgressEntity
import kotlinx.coroutines.flow.Flow

interface HistoryRepository {
    fun getProgressForDate(date: String): Flow<List<DailyProgressEntity>>
    fun getHistoryForTasbih(tasbihId: String): Flow<List<DailyProgressEntity>>
    fun getAllProgressFlow(): Flow<List<DailyProgressEntity>>
    suspend fun getAllProgressList(): List<DailyProgressEntity>
    suspend fun clearAllHistory()
}