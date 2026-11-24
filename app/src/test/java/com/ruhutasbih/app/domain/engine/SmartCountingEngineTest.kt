package com.ruhutasbih.app.domain.engine

import org.junit.Assert.assertEquals
import org.junit.Test

class SmartCountingEngineTest {

    @Test
    fun testExample1() {
        // Daily Target = 100, Previous Pending = 50, Today's Reading = 100
        val result = SmartCountingEngine.calculate(
            dailyTarget = 100L,
            previousPending = 50L,
            todayReading = 100L
        )

        assertEquals(100L, result.dailyCount)
        assertEquals(50L, result.previousPendingRemaining)
        assertEquals(50L, result.nextDayPending)
    }

    @Test
    fun testExample2() {
        // Daily Target = 100, Previous Pending = 0, Today's Reading = 150
        val result = SmartCountingEngine.calculate(
            dailyTarget = 100L,
            previousPending = 0L,
            todayReading = 150L
        )

        assertEquals(150L, result.dailyCount)
        assertEquals(0L, result.previousPendingRemaining)
        assertEquals(0L, result.nextDayPending)
    }

    @Test
    fun testExample3() {
        // Daily Target = 100, Previous Pending = 50, Today's Reading = 200
        val result = SmartCountingEngine.calculate(
            dailyTarget = 100L,
            previousPending = 50L,
            todayReading = 200L
        )

        assertEquals(150L, result.dailyCount)
        assertEquals(0L, result.previousPendingRemaining)
        assertEquals(0L, result.nextDayPending)
    }

    @Test
    fun testExample4() {
        // Daily Target = 100, Previous Pending = 50, Today's Reading = 120
        val result = SmartCountingEngine.calculate(
            dailyTarget = 100L,
            previousPending = 50L,
            todayReading = 120L
        )

        assertEquals(100L, result.dailyCount)
        assertEquals(30L, result.previousPendingRemaining)
        assertEquals(30L, result.nextDayPending)
    }

    @Test
    fun testExample5() {
        // Daily Target = 100, Previous Pending = 50, Today's Reading = 30
        val result = SmartCountingEngine.calculate(
            dailyTarget = 100L,
            previousPending = 50L,
            todayReading = 30L
        )

        assertEquals(30L, result.dailyCount)
        assertEquals(70L, result.todayUncompleted)
        assertEquals(50L, result.previousPendingRemaining)
        assertEquals(120L, result.nextDayPending)
    }
}