package com.example.pdftool.data.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.pdftool.data.database.entities.RecentFileEntity

@Dao
interface RecentFileDao {
    
    @Query("SELECT * FROM recent_files ORDER BY lastViewedTime DESC")
    fun getAllRecentFiles(): LiveData<List<RecentFileEntity>>
    
    @Query("SELECT * FROM recent_files ORDER BY lastViewedTime DESC LIMIT :limit")
    fun getRecentFiles(limit: Int): LiveData<List<RecentFileEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecentFile(recentFile: RecentFileEntity)
    
    @Delete
    suspend fun deleteRecentFile(recentFile: RecentFileEntity)
    
    @Query("DELETE FROM recent_files WHERE filePath = :filePath")
    suspend fun deleteRecentFileByPath(filePath: String)
    
    @Query("DELETE FROM recent_files")
    suspend fun clearAllRecentFiles()
    
    @Query("SELECT COUNT(*) FROM recent_files")
    suspend fun getRecentFilesCount(): Int
    
    @Query("DELETE FROM recent_files WHERE filePath NOT IN (SELECT filePath FROM recent_files ORDER BY lastViewedTime DESC LIMIT :limit)")
    suspend fun keepOnlyRecentFiles(limit: Int)
}