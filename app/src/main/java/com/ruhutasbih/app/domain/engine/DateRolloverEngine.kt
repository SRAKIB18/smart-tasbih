package com.ruhutasbih.app.domain.engine

import com.ruhutasbih.app.data.local.entity.TasbihEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateRolloverEngine {

    fun getTodayDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(Date())
    }

    fun isNewDay(lastDateString: String, todayDateString: String = getTodayDateString()): Boolean {
        if (lastDateString.isBlank()) return false
        return lastDateString != todayDateString
    }

    fun rolloverTasbih(
        tasbih: TasbihEntity,
        todayDateString: String = getTodayDateString()
    ): TasbihEntity {
        if (!isNewDay(tasbih.lastUpdatedDate, todayDateString)) {
            return tasbih.copy(
                lastUpdatedDate = if (tasbih.lastUpdatedDate.isBlank()) todayDateString else tasbih.lastUpdatedDate
            )
        }

        // Calculate current counting state for yesterday
        val currentResult = SmartCountingEngine.calculate(
            dailyTarget = tasbih.dailyTarget,
            previousPending = tasbih.previousPending,
            todayReading = tasbih.todayReading
        )

        // Carry nextDayPending forward as new day's previousPending
        val newPreviousPending = currentResult.nextDayPending

        // Reset todayReading to 0 for the new day
        val newDayResult = SmartCountingEngine.calculate(
            dailyTarget = tasbih.dailyTarget,
            previousPending = newPreviousPending,
            todayReading = 0L
        )

        return tasbih.copy(
            previousPending = newPreviousPending,
            todayReading = 0L,
            dailyCount = newDayResult.dailyCount,
            previousPendingRemaining = newDayResult.previousPendingRemaining,
            nextDayPending = newDayResult.nextDayPending,
            lastUpdatedDate = todayDateString
        )
    }
}