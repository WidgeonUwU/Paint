package ru.sfedu.paint.data.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import androidx.room.Transaction
import ru.sfedu.paint.data.model.DrawingContentEntity
import ru.sfedu.paint.data.model.DrawingEntity
import ru.sfedu.paint.data.model.DrawingWithContent

@Dao
interface DrawingDao {
    @Query("SELECT * FROM drawings ORDER BY createdAt DESC")
    fun getAllDrawingsLightweight(): Flow<List<DrawingEntity>>
    
    @Query("SELECT * FROM drawings WHERE name LIKE :query ORDER BY createdAt DESC")
    fun searchDrawingsLightweight(query: String): Flow<List<DrawingEntity>>
    
    @Transaction
    @Query("SELECT * FROM drawings ORDER BY createdAt DESC")
    fun getAllDrawings(): Flow<List<DrawingWithContent>>
    
    @Transaction
    @Query("SELECT * FROM drawings WHERE id = :id")
    suspend fun getDrawingById(id: Long): DrawingWithContent?
    
    @Transaction
    @Query("SELECT * FROM drawings WHERE name LIKE :query ORDER BY createdAt DESC")
    fun searchDrawings(query: String): Flow<List<DrawingWithContent>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDrawing(drawing: DrawingEntity): Long
    
    @Update
    suspend fun updateDrawing(drawing: DrawingEntity)
    
    @Delete
    suspend fun deleteDrawing(drawing: DrawingEntity)
    
    @Query("DELETE FROM drawings WHERE id = :id")
    suspend fun deleteDrawingById(id: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertContent(content: DrawingContentEntity)

    @Query("SELECT drawingId, pathsJsonCompressed FROM drawing_content WHERE drawingId = :id")
    suspend fun getContentById(id: Long): DrawingContentEntity?

    @Query("DELETE FROM drawing_content WHERE drawingId = :id")
    suspend fun deleteContentByDrawingId(id: Long)
}


