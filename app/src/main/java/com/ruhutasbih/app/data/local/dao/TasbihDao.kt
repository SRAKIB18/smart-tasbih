package com.ruhutasbih.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ruhutasbih.app.data.local.entity.TasbihEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TasbihDao {

    @Query("SELECT * FROM tasbih_table ORDER BY isPinned DESC, createdAt ASC")
    fun getAllTasbihs(): Flow<List<TasbihEntity>>

    @Query("SELECT * FROM tasbih_table WHERE isArchived = 0 ORDER BY isPinned DESC, createdAt ASC")
    fun getActiveTasbihs(): Flow<List<TasbihEntity>>

    @Query("SELECT * FROM tasbih_table WHERE isArchived = 1 ORDER BY createdAt DESC")
    fun getArchivedTasbihs(): Flow<List<TasbihEntity>>

    @Query("SELECT * FROM tasbih_table WHERE id = :id LIMIT 1")
    fun getTasbihByIdFlow(id: String): Flow<TasbihEntity?>

    @Query("SELECT * FROM tasbih_table WHERE id = :id LIMIT 1")
    suspend fun getTasbihById(id: String): TasbihEntity?

    @Query("SELECT * FROM tasbih_table")
    suspend fun getAllTasbihsList(): List<TasbihEntity>

    @Query("SELECT COUNT(*) FROM tasbih_table")
    suspend fun getTasbihCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasbih(tasbih: TasbihEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tasbihs: List<TasbihEntity>)

    @Update
    suspend fun updateTasbih(tasbih: TasbihEntity)

    @Delete
    suspend fun deleteTasbih(tasbih: TasbihEntity)

    @Query("DELETE FROM tasbih_table WHERE id = :id")
    suspend fun deleteTasbihById(id: String)

    @Query("UPDATE tasbih_table SET isPinned = :isPinned WHERE id = :id")
    suspend fun setPinnedState(id: String, isPinned: Boolean)

    @Query("UPDATE tasbih_table SET isArchived = :isArchived WHERE id = :id")
    suspend fun setArchivedState(id: String, isArchived: Boolean)

    @Query("DELETE FROM tasbih_table")
    suspend fun clearAll()
}