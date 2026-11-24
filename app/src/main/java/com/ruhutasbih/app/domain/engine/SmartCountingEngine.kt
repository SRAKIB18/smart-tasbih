package com.ruhutasbih.app.domain.engine

data class CountingResult(
    val dailyTarget: Long,
    val previousPending: Long,
    val todayReading: Long,
    val dailyCount: Long,
    val previousPendingRemaining: Long,
    val todayUncompleted: Long,
    val nextDayPending: Long
)

object SmartCountingEngine {

    fun calculate(
        dailyTarget: Long,
        previousPending: Long,
        todayReading: Long
    ): CountingResult {
        val target = maxOf(1L, dailyTarget)
        val pending = maxOf(0L, previousPending)
        val reading = maxOf(0L, todayReading)

        val dailyBase = minOf(reading, target)
        val surplus = maxOf(0L, reading - target)
        val pendingCompleted = minOf(surplus, pending)
        val remainingSurplus = surplus - pendingCompleted

        val dailyCount = dailyBase + remainingSurplus
        val previousPendingRemaining = pending - pendingCompleted
        val todayUncompleted = maxOf(0L, target - reading)
        val nextDayPending = previousPendingRemaining + todayUncompleted

        return CountingResult(
            dailyTarget = target,
            previousPending = pending,
            todayReading = reading,
            dailyCount = dailyCount,
            previousPendingRemaining = previousPendingRemaining,
            todayUncompleted = todayUncompleted,
            nextDayPending = nextDayPending
        )
    }
}