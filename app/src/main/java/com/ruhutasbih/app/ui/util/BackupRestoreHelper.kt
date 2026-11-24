package com.ruhutasbih.app.ui.util

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.ruhutasbih.app.data.local.entity.DailyProgressEntity
import com.ruhutasbih.app.data.local.entity.TasbihEntity

data class AppBackupData(
    val version: Int = 1,
    val timestamp: Long = System.currentTimeMillis(),
    val tasbihs: List<TasbihEntity> = emptyList(),
    val history: List<DailyProgressEntity> = emptyList()
)

object BackupRestoreHelper {

    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    fun exportToJson(tasbihs: List<TasbihEntity>, history: List<DailyProgressEntity>): String {
        val backupData = AppBackupData(
            version = 1,
            timestamp = System.currentTimeMillis(),
            tasbihs = tasbihs,
            history = history
        )
        return gson.toJson(backupData)
    }

    fun importFromJson(jsonString: String): AppBackupData? {
        return try {
            gson.fromJson(jsonString, AppBackupData::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun exportToCsv(history: List<DailyProgressEntity>): String {
        val sb = StringBuilder()
        sb.append("ID,TasbihID,Date,DailyTarget,TodayReading,DailyCount,PendingStart,PendingRemainingEnd,NextDayPending,IsCompleted\n")
        for (item in history) {
            sb.append("${item.id},${item.tasbihId},${item.date},${item.dailyTarget},${item.todayReading},${item.dailyCount},${item.pendingAtStart},${item.pendingRemainingAtEnd},${item.nextDayPending},${item.isCompleted}\n")
        }
        return sb.toString()
    }
}