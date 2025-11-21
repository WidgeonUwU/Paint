package ru.sfedu.paint.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ru.sfedu.paint.data.dao.DrawingDao
import ru.sfedu.paint.data.model.DrawingEntity

@Database(
    entities = [DrawingEntity::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class PaintDatabase : RoomDatabase() {
    abstract fun drawingDao(): DrawingDao
    
    companion object {
        const val DATABASE_NAME = "paint_database"
    }
}
