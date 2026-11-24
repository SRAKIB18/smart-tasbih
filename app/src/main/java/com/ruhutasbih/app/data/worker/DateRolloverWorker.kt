package com.ruhutasbih.app.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ruhutasbih.app.data.local.AppDatabase
import com.ruhutasbih.app.data.repository.HistoryRepositoryImpl
import com.ruhutasbih.app.data.repository.TasbihRepositoryImpl

class DateRolloverWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = TasbihRepositoryImpl(
            tasbihDao = database.tasbihDao(),
            dailyProgressDao = database.dailyProgressDao(),
            countHistoryDao = database.countHistoryDao()
        )
        repository.checkAndApplyDateRollover()
        return Result.success()
    }
}