package ru.sfedu.paint.data.model

import androidx.room.Embedded
import androidx.room.Relation

data class DrawingWithContent(
    @Embedded val drawing: DrawingEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "drawingId"
    )
    val content: DrawingContentEntity?
)










