package ru.sfedu.paint.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "drawings",
    indices = [Index(value = ["createdAt"], name = "idx_drawings_created_at")]
)
data class DrawingEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val createdAt: Date,
    val width: Int,
    val height: Int,
    val backgroundColor: Long = 0xFFFFFFFF,
    val preview: ByteArray? = null
)


