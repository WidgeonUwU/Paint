package ru.sfedu.paint.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.sfedu.paint.data.dao.DrawingDao
import ru.sfedu.paint.data.model.Drawing
import ru.sfedu.paint.data.model.DrawingEntity

class DrawingRepository(private val drawingDao: DrawingDao) {
    fun getAllDrawings(): Flow<List<Drawing>> {
        return drawingDao.getAllDrawingsLightweight().map { entries ->
            entries.map { it.toDomain() }
        }
    }
    
    fun searchDrawings(query: String): Flow<List<Drawing>> {
        return drawingDao.searchDrawingsLightweight("%$query%").map { entries ->
            entries.map { it.toDomain() }
        }
    }
    
    suspend fun insertDrawing(drawing: Drawing): Long {
        return drawingDao.insertDrawing(drawing.toEntity())
    }
    
    suspend fun updateDrawing(drawing: Drawing) {
        drawingDao.updateDrawing(drawing.toEntity())
    }
    
    suspend fun deleteDrawing(drawing: Drawing) {
        drawingDao.deleteDrawing(drawing.toEntity())
    }
    
    suspend fun deleteDrawingById(id: Long) {
        drawingDao.deleteDrawingById(id)
    }
    
    private fun DrawingEntity.toDomain(): Drawing {
        return Drawing(
            id = id,
            name = name,
            createdAt = createdAt,
            width = width,
            height = height,
            backgroundColor = backgroundColor,
            preview = preview
        )
    }
}
