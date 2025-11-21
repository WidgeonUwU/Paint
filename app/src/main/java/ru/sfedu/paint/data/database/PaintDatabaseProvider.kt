package ru.sfedu.paint.data.database

import android.content.Context
import androidx.room.Room

object PaintDatabaseProvider {
    @Volatile
    private var INSTANCE: PaintDatabase? = null
    
    fun getDatabase(context: Context): PaintDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                PaintDatabase::class.java,
                PaintDatabase.DATABASE_NAME
            )
                .fallbackToDestructiveMigration()
                .build()
            INSTANCE = instance
            instance
        }
    }
}


