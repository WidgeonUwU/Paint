package ru.sfedu.paint.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.sfedu.paint.data.dao.DrawingDao
import ru.sfedu.paint.data.model.Drawing
import ru.sfedu.paint.data.model.DrawingContentEntity
import ru.sfedu.paint.data.model.DrawingEntity
import ru.sfedu.paint.data.model.DrawingWithContent
import ru.sfedu.paint.util.JsonCompression

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
    
    fun getDrawingById(id: Long): Flow<Drawing?> {
        return drawingDao.getAllDrawings().map { entries ->
            entries.find { it.drawing.id == id }?.toDomain()
        }
    }
    
    suspend fun getDrawingWithContentById(id: Long): DrawingWithContent? {
        return drawingDao.getDrawingById(id)
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
        drawingDao.deleteContentByDrawingId(id)
    }

    suspend fun upsertContent(drawingId: Long, pathsJson: String) {
        val compressed = JsonCompression.compress(pathsJson)
        drawingDao.upsertContent(
            DrawingContentEntity(
                drawingId = drawingId,
                pathsJsonCompressed = compressed
            )
        )
    }

    suspend fun getContent(drawingId: Long): DrawingContentEntity? {
        return drawingDao.getContentById(drawingId)
    }
    
    suspend fun getPathsJson(drawingId: Long): String? {
        val content = getContent(drawingId) ?: return null
        return try {
            JsonCompression.decompress(content.pathsJsonCompressed)
        } catch (e: Exception) {
            null
        }
    }

    private fun DrawingWithContent.toDomain(): Drawing {
        return Drawing.from(drawing, content)
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


