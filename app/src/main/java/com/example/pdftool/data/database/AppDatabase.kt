package com.example.pdftool.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.example.pdftool.data.database.dao.RecentFileDao
import com.example.pdftool.data.database.dao.BookmarkDao
import com.example.pdftool.data.database.entities.RecentFileEntity
import com.example.pdftool.data.database.entities.BookmarkEntity

@Database(
    entities = [RecentFileEntity::class, BookmarkEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun recentFileDao(): RecentFileDao
    abstract fun bookmarkDao(): BookmarkDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pdf_tool_database"
                ).fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}