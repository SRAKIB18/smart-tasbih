package com.ruhutasbih.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ruhutasbih.app.data.local.entity.CountHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CountHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: CountHistoryEntity)

    @Query("SELECT * FROM count_history_table WHERE tasbihId = :tasbihId ORDER BY timestamp DESC")
    fun getLogsForTasbih(tasbihId: String): Flow<List<CountHistoryEntity>>

    @Query("SELECT * FROM count_history_table ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastLog(): CountHistoryEntity?

    @Query("DELETE FROM count_history_table WHERE id = :id")
    suspend fun deleteLogById(id: Long)

    @Query("DELETE FROM count_history_table")
    suspend fun clearAll()
}