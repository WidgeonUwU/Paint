package ru.sfedu.paint.data.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import ru.sfedu.paint.data.model.DrawingEntity

@Dao
interface DrawingDao {
    @Query("SELECT * FROM drawings ORDER BY createdAt DESC")
    fun getAllDrawingsLightweight(): Flow<List<DrawingEntity>>
    
    @Query("SELECT * FROM drawings WHERE name LIKE :query ORDER BY createdAt DESC")
    fun searchDrawingsLightweight(query: String): Flow<List<DrawingEntity>>
    
    @Query("SELECT * FROM drawings WHERE id = :id")
    suspend fun getDrawingById(id: Long): DrawingEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDrawing(drawing: DrawingEntity): Long
    
    @Update
    suspend fun updateDrawing(drawing: DrawingEntity)
    
    @Delete
    suspend fun deleteDrawing(drawing: DrawingEntity)
    
    @Query("DELETE FROM drawings WHERE id = :id")
    suspend fun deleteDrawingById(id: Long)
}
