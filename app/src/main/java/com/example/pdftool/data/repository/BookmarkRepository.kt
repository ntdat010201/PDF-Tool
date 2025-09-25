package com.example.pdftool.data.repository

import androidx.lifecycle.LiveData
import com.example.pdftool.data.database.dao.BookmarkDao
import com.example.pdftool.data.database.entities.BookmarkEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BookmarkRepository(private val bookmarkDao: BookmarkDao) {
    
    fun getAllBookmarks(): LiveData<List<BookmarkEntity>> {
        return bookmarkDao.getAllBookmarks()
    }
    
    fun getBookmarksByFile(filePath: String): LiveData<List<BookmarkEntity>> {
        return bookmarkDao.getBookmarksByFile(filePath)
    }
    
    suspend fun getBookmarkByFileAndPage(filePath: String, pageNumber: Int): BookmarkEntity? {
        return withContext(Dispatchers.IO) {
            bookmarkDao.getBookmarkByFileAndPage(filePath, pageNumber)
        }
    }
    
    suspend fun insertBookmark(bookmark: BookmarkEntity): Long {
        return withContext(Dispatchers.IO) {
            bookmarkDao.insertBookmark(bookmark)
        }
    }
    
    suspend fun updateBookmark(bookmark: BookmarkEntity) {
        withContext(Dispatchers.IO) {
            bookmarkDao.updateBookmark(bookmark)
        }
    }
    
    suspend fun deleteBookmark(bookmark: BookmarkEntity) {
        withContext(Dispatchers.IO) {
            bookmarkDao.deleteBookmark(bookmark)
        }
    }
    
    suspend fun deleteBookmarkById(bookmarkId: Long) {
        withContext(Dispatchers.IO) {
            bookmarkDao.deleteBookmarkById(bookmarkId)
        }
    }
    
    suspend fun deleteBookmarksByFile(filePath: String) {
        withContext(Dispatchers.IO) {
            bookmarkDao.deleteBookmarksByFile(filePath)
        }
    }
    
    suspend fun deleteBookmarkByFileAndPage(filePath: String, pageNumber: Int) {
        withContext(Dispatchers.IO) {
            bookmarkDao.deleteBookmarkByFileAndPage(filePath, pageNumber)
        }
    }
    
    suspend fun clearAllBookmarks() {
        withContext(Dispatchers.IO) {
            bookmarkDao.clearAllBookmarks()
        }
    }
    
    suspend fun getBookmarksCount(): Int {
        return withContext(Dispatchers.IO) {
            bookmarkDao.getBookmarksCount()
        }
    }
    
    suspend fun getBookmarksCountByFile(filePath: String): Int {
        return withContext(Dispatchers.IO) {
            bookmarkDao.getBookmarksCountByFile(filePath)
        }
    }
    
    suspend fun isPageBookmarked(filePath: String, pageNumber: Int): Boolean {
        return withContext(Dispatchers.IO) {
            bookmarkDao.isPageBookmarked(filePath, pageNumber)
        }
    }
    
    suspend fun toggleBookmark(filePath: String, fileName: String, pageNumber: Int, title: String, note: String? = null, fileUri: String? = null): Boolean {
        return withContext(Dispatchers.IO) {
            val existingBookmark = bookmarkDao.getBookmarkByFileAndPage(filePath, pageNumber)
            if (existingBookmark != null) {
                // Remove bookmark
                bookmarkDao.deleteBookmark(existingBookmark)
                false
            } else {
                // Add bookmark
                val newBookmark = BookmarkEntity(
                    filePath = filePath,
                    fileName = fileName,
                    pageNumber = pageNumber,
                    title = title,
                    note = note,
                    fileUri = fileUri
                )
                bookmarkDao.insertBookmark(newBookmark)
                true
            }
        }
    }
}