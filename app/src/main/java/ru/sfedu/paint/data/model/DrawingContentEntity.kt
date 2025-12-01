package ru.sfedu.paint.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "drawing_content",
    foreignKeys = [
        ForeignKey(
            entity = DrawingEntity::class,
            parentColumns = ["id"],
            childColumns = ["drawingId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class DrawingContentEntity(
    @PrimaryKey
    val drawingId: Long,
    val pathsJsonCompressed: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DrawingContentEntity

        if (drawingId != other.drawingId) return false
        if (!pathsJsonCompressed.contentEquals(other.pathsJsonCompressed)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = drawingId.hashCode()
        result = 31 * result + pathsJsonCompressed.contentHashCode()
        return result
    }
}









