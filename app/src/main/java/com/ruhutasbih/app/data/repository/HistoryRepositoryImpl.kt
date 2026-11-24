package com.ruhutasbih.app.data.repository

import com.ruhutasbih.app.data.local.dao.DailyProgressDao
import com.ruhutasbih.app.data.local.entity.DailyProgressEntity
import com.ruhutasbih.app.domain.repository.HistoryRepository
import kotlinx.coroutines.flow.Flow

class HistoryRepositoryImpl(
    private val dailyProgressDao: DailyProgressDao
) : HistoryRepository {

    override fun getProgressForDate(date: String): Flow<List<DailyProgressEntity>> {
        return dailyProgressDao.getProgressForDate(date)
    }

    override fun getHistoryForTasbih(tasbihId: String): Flow<List<DailyProgressEntity>> {
        return dailyProgressDao.getHistoryForTasbih(tasbihId)
    }

    override fun getAllProgressFlow(): Flow<List<DailyProgressEntity>> {
        return dailyProgressDao.getAllProgressListFlow()
    }

    override suspend fun getAllProgressList(): List<DailyProgressEntity> {
        return dailyProgressDao.getAllProgressList()
    }

    override suspend fun clearAllHistory() {
        dailyProgressDao.clearAll()
    }
}