package com.ruhutasbih.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ruhutasbih.app.data.local.entity.DailyProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyProgressDao {

    @Query("SELECT * FROM daily_progress_table WHERE date = :date")
    fun getProgressForDate(date: String): Flow<List<DailyProgressEntity>>

    @Query("SELECT * FROM daily_progress_table WHERE tasbihId = :tasbihId AND date = :date LIMIT 1")
    suspend fun getProgressForTasbihAndDate(tasbihId: String, date: String): DailyProgressEntity?

    @Query("SELECT * FROM daily_progress_table WHERE tasbihId = :tasbihId ORDER BY date DESC")
    fun getHistoryForTasbih(tasbihId: String): Flow<List<DailyProgressEntity>>

    @Query("SELECT * FROM daily_progress_table ORDER BY date DESC")
    fun getAllProgressListFlow(): Flow<List<DailyProgressEntity>>

    @Query("SELECT * FROM daily_progress_table ORDER BY date DESC")
    suspend fun getAllProgressList(): List<DailyProgressEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProgress(progress: DailyProgressEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(progressList: List<DailyProgressEntity>)

    @Query("DELETE FROM daily_progress_table")
    suspend fun clearAll()
}