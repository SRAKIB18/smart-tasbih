package com.ruhutasbih.app.data.repository

import com.ruhutasbih.app.data.local.dao.CountHistoryDao
import com.ruhutasbih.app.data.local.dao.DailyProgressDao
import com.ruhutasbih.app.data.local.dao.TasbihDao
import com.ruhutasbih.app.data.local.entity.CountHistoryEntity
import com.ruhutasbih.app.data.local.entity.DailyProgressEntity
import com.ruhutasbih.app.data.local.entity.TasbihEntity
import com.ruhutasbih.app.domain.engine.DateRolloverEngine
import com.ruhutasbih.app.domain.engine.SmartCountingEngine
import com.ruhutasbih.app.domain.repository.TasbihRepository
import kotlinx.coroutines.flow.Flow

class TasbihRepositoryImpl(
    private val tasbihDao: TasbihDao,
    private val dailyProgressDao: DailyProgressDao,
    private val countHistoryDao: CountHistoryDao
) : TasbihRepository {

    override fun getAllTasbihs(): Flow<List<TasbihEntity>> = tasbihDao.getAllTasbihs()

    override fun getActiveTasbihs(): Flow<List<TasbihEntity>> = tasbihDao.getActiveTasbihs()

    override fun getArchivedTasbihs(): Flow<List<TasbihEntity>> = tasbihDao.getArchivedTasbihs()

    override fun getTasbihByIdFlow(id: String): Flow<TasbihEntity?> = tasbihDao.getTasbihByIdFlow(id)

    override suspend fun getTasbihById(id: String): TasbihEntity? {
        val tasbih = tasbihDao.getTasbihById(id) ?: return null
        val todayStr = DateRolloverEngine.getTodayDateString()
        if (DateRolloverEngine.isNewDay(tasbih.lastUpdatedDate, todayStr)) {
            val rolledOver = DateRolloverEngine.rolloverTasbih(tasbih, todayStr)
            tasbihDao.updateTasbih(rolledOver)
            syncDailyProgress(rolledOver, todayStr)
            return rolledOver
        }
        return tasbih
    }

    override suspend fun insertTasbih(tasbih: TasbihEntity) {
        val todayStr = DateRolloverEngine.getTodayDateString()
        val calculated = SmartCountingEngine.calculate(
            dailyTarget = tasbih.dailyTarget,
            previousPending = tasbih.previousPending,
            todayReading = tasbih.todayReading
        )
        val updated = tasbih.copy(
            dailyCount = calculated.dailyCount,
            previousPendingRemaining = calculated.previousPendingRemaining,
            nextDayPending = calculated.nextDayPending,
            lastUpdatedDate = if (tasbih.lastUpdatedDate.isBlank()) todayStr else tasbih.lastUpdatedDate
        )
        tasbihDao.insertTasbih(updated)
        syncDailyProgress(updated, todayStr)
    }

    override suspend fun updateTasbih(tasbih: TasbihEntity) {
        val todayStr = DateRolloverEngine.getTodayDateString()
        val calculated = SmartCountingEngine.calculate(
            dailyTarget = tasbih.dailyTarget,
            previousPending = tasbih.previousPending,
            todayReading = tasbih.todayReading
        )
        val updated = tasbih.copy(
            dailyCount = calculated.dailyCount,
            previousPendingRemaining = calculated.previousPendingRemaining,
            nextDayPending = calculated.nextDayPending,
            lastUpdatedDate = if (tasbih.lastUpdatedDate.isBlank()) todayStr else tasbih.lastUpdatedDate
        )
        tasbihDao.updateTasbih(updated)
        syncDailyProgress(updated, todayStr)
    }

    override suspend fun deleteTasbih(id: String) {
        tasbihDao.deleteTasbihById(id)
    }

    override suspend fun setPinnedState(id: String, isPinned: Boolean) {
        tasbihDao.setPinnedState(id, isPinned)
    }

    override suspend fun setArchivedState(id: String, isArchived: Boolean) {
        tasbihDao.setArchivedState(id, isArchived)
    }

    override suspend fun incrementCount(id: String, delta: Long) {
        val rawTasbih = getTasbihById(id) ?: return
        val todayStr = DateRolloverEngine.getTodayDateString()

        val newReading = rawTasbih.todayReading + delta
        val newLifetime = rawTasbih.lifetimeCount + delta

        val calculated = SmartCountingEngine.calculate(
            dailyTarget = rawTasbih.dailyTarget,
            previousPending = rawTasbih.previousPending,
            todayReading = newReading
        )

        val updated = rawTasbih.copy(
            todayReading = newReading,
            dailyCount = calculated.dailyCount,
            previousPendingRemaining = calculated.previousPendingRemaining,
            nextDayPending = calculated.nextDayPending,
            lifetimeCount = newLifetime,
            lastUpdatedDate = todayStr
        )

        tasbihDao.updateTasbih(updated)
        countHistoryDao.insertLog(
            CountHistoryEntity(
                tasbihId = id,
                date = todayStr,
                countDelta = delta
            )
        )
        syncDailyProgress(updated, todayStr)
    }

    override suspend fun undoCount(id: String) {
        val lastLog = countHistoryDao.getLastLog() ?: return
        if (lastLog.tasbihId != id) return

        val rawTasbih = getTasbihById(id) ?: return
        val todayStr = DateRolloverEngine.getTodayDateString()

        val delta = lastLog.countDelta
        val newReading = maxOf(0L, rawTasbih.todayReading - delta)
        val newLifetime = maxOf(0L, rawTasbih.lifetimeCount - delta)

        val calculated = SmartCountingEngine.calculate(
            dailyTarget = rawTasbih.dailyTarget,
            previousPending = rawTasbih.previousPending,
            todayReading = newReading
        )

        val updated = rawTasbih.copy(
            todayReading = newReading,
            dailyCount = calculated.dailyCount,
            previousPendingRemaining = calculated.previousPendingRemaining,
            nextDayPending = calculated.nextDayPending,
            lifetimeCount = newLifetime,
            lastUpdatedDate = todayStr
        )

        tasbihDao.updateTasbih(updated)
        countHistoryDao.deleteLogById(lastLog.id)
        syncDailyProgress(updated, todayStr)
    }

    override suspend fun resetTodayCount(id: String) {
        val rawTasbih = getTasbihById(id) ?: return
        val todayStr = DateRolloverEngine.getTodayDateString()

        val oldReading = rawTasbih.todayReading
        val newLifetime = maxOf(0L, rawTasbih.lifetimeCount - oldReading)

        val calculated = SmartCountingEngine.calculate(
            dailyTarget = rawTasbih.dailyTarget,
            previousPending = rawTasbih.previousPending,
            todayReading = 0L
        )

        val updated = rawTasbih.copy(
            todayReading = 0L,
            dailyCount = calculated.dailyCount,
            previousPendingRemaining = calculated.previousPendingRemaining,
            nextDayPending = calculated.nextDayPending,
            lifetimeCount = newLifetime,
            lastUpdatedDate = todayStr
        )

        tasbihDao.updateTasbih(updated)
        syncDailyProgress(updated, todayStr)
    }

    override suspend fun checkAndApplyDateRollover() {
        val todayStr = DateRolloverEngine.getTodayDateString()
        val allTasbihs = tasbihDao.getAllTasbihsList()
        for (tasbih in allTasbihs) {
            if (DateRolloverEngine.isNewDay(tasbih.lastUpdatedDate, todayStr)) {
                val rolledOver = DateRolloverEngine.rolloverTasbih(tasbih, todayStr)
                tasbihDao.updateTasbih(rolledOver)
                syncDailyProgress(rolledOver, todayStr)
            }
        }
    }

    private suspend fun syncDailyProgress(tasbih: TasbihEntity, dateStr: String) {
        val existingProgress = dailyProgressDao.getProgressForTasbihAndDate(tasbih.id, dateStr)
        val progressEntity = DailyProgressEntity(
            id = existingProgress?.id ?: 0L,
            tasbihId = tasbih.id,
            date = dateStr,
            dailyTarget = tasbih.dailyTarget,
            todayReading = tasbih.todayReading,
            dailyCount = tasbih.dailyCount,
            pendingAtStart = tasbih.previousPending,
            pendingRemainingAtEnd = tasbih.previousPendingRemaining,
            nextDayPending = tasbih.nextDayPending,
            isCompleted = tasbih.todayReading >= tasbih.dailyTarget
        )
        dailyProgressDao.insertOrUpdateProgress(progressEntity)
    }
}