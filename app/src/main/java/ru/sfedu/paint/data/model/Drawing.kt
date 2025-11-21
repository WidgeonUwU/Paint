package ru.sfedu.paint.data.model

import androidx.compose.ui.graphics.Color
import java.util.Date

data class Drawing(
    val id: Long = 0,
    val name: String,
    val createdAt: Date,
    val width: Int,
    val height: Int,
    val backgroundColor: Long = 0xFFFFFFFF,
    val preview: ByteArray? = null
) {
    fun toEntity(): DrawingEntity {
        return DrawingEntity(
            id = id,
            name = name,
            createdAt = createdAt,
            width = width,
            height = height,
            backgroundColor = backgroundColor,
            preview = preview
        )
    }
    
    companion object {
        fun from(entity: DrawingEntity, content: DrawingContentEntity?): Drawing {
            return Drawing(
                id = entity.id,
                name = entity.name,
                createdAt = entity.createdAt,
                width = entity.width,
                height = entity.height,
                backgroundColor = entity.backgroundColor,
                preview = entity.preview
            )
        }
    }
}


