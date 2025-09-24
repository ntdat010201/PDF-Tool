package com.example.pdftool.data.repository

import androidx.lifecycle.LiveData
import com.example.pdftool.data.database.dao.RecentFileDao
import com.example.pdftool.data.database.entities.RecentFileEntity
import com.example.pdftool.model.ModelFileItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RecentFileRepository(private val recentFileDao: RecentFileDao) {
    
    fun getAllRecentFiles(): LiveData<List<RecentFileEntity>> {
        return recentFileDao.getAllRecentFiles()
    }
    
    fun getRecentFiles(limit: Int = 20): LiveData<List<RecentFileEntity>> {
        return recentFileDao.getRecentFiles(limit)
    }
    
    suspend fun addRecentFile(file: ModelFileItem) {
        withContext(Dispatchers.IO) {
            val recentFile = RecentFileEntity(
                filePath = file.path,
                fileName = file.name,
                fileSize = file.size,
                lastViewedTime = System.currentTimeMillis(),
                fileUri = file.uri?.toString()
            )
            recentFileDao.insertRecentFile(recentFile)
            
            // Keep only the latest 50 files to prevent database from growing too large
            val count = recentFileDao.getRecentFilesCount()
            if (count > 50) {
                recentFileDao.keepOnlyRecentFiles(50)
            }
        }
    }
    
    suspend fun removeRecentFile(filePath: String) {
        withContext(Dispatchers.IO) {
            recentFileDao.deleteRecentFileByPath(filePath)
        }
    }
    
    suspend fun clearAllRecentFiles() {
        withContext(Dispatchers.IO) {
            recentFileDao.clearAllRecentFiles()
        }
    }
    
    // Convert RecentFileEntity to ModelFileItem for UI
    fun convertToModelFileItem(recentFileEntity: RecentFileEntity): ModelFileItem {
        return ModelFileItem(
            name = recentFileEntity.fileName,
            path = recentFileEntity.filePath,
            type = "pdf", // Default type for PDF files
            lastModified = recentFileEntity.lastViewedTime,
            size = recentFileEntity.fileSize,
            uri = if (recentFileEntity.fileUri != null) 
                android.net.Uri.parse(recentFileEntity.fileUri) else null
        )
    }
}