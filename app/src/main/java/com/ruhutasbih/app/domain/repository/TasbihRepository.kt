package com.ruhutasbih.app.domain.repository

import com.ruhutasbih.app.data.local.entity.TasbihEntity
import kotlinx.coroutines.flow.Flow

interface TasbihRepository {
    fun getAllTasbihs(): Flow<List<TasbihEntity>>
    fun getActiveTasbihs(): Flow<List<TasbihEntity>>
    fun getArchivedTasbihs(): Flow<List<TasbihEntity>>
    fun getTasbihByIdFlow(id: String): Flow<TasbihEntity?>
    suspend fun getTasbihById(id: String): TasbihEntity?
    suspend fun insertTasbih(tasbih: TasbihEntity)
    suspend fun insertTasbihs(tasbihs: List<TasbihEntity>)
    suspend fun updateTasbih(tasbih: TasbihEntity)
    suspend fun deleteTasbih(id: String)
    suspend fun setPinnedState(id: String, isPinned: Boolean)
    suspend fun setArchivedState(id: String, isArchived: Boolean)
    suspend fun incrementCount(id: String, delta: Long = 1L)
    suspend fun undoCount(id: String)
    suspend fun resetTodayCount(id: String)
    suspend fun checkAndApplyDateRollover()
    suspend fun clearAllTasbihs()
}