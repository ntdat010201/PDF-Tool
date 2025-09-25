package com.example.pdftool.data.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.pdftool.data.database.entities.BookmarkEntity

@Dao
interface BookmarkDao {
    
    @Query("SELECT * FROM bookmarks ORDER BY createdTime DESC")
    fun getAllBookmarks(): LiveData<List<BookmarkEntity>>
    
    @Query("SELECT * FROM bookmarks WHERE filePath = :filePath ORDER BY pageNumber ASC")
    fun getBookmarksByFile(filePath: String): LiveData<List<BookmarkEntity>>
    
    @Query("SELECT * FROM bookmarks WHERE filePath = :filePath AND pageNumber = :pageNumber")
    suspend fun getBookmarkByFileAndPage(filePath: String, pageNumber: Int): BookmarkEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity): Long
    
    @Update
    suspend fun updateBookmark(bookmark: BookmarkEntity)
    
    @Delete
    suspend fun deleteBookmark(bookmark: BookmarkEntity)
    
    @Query("DELETE FROM bookmarks WHERE id = :bookmarkId")
    suspend fun deleteBookmarkById(bookmarkId: Long)
    
    @Query("DELETE FROM bookmarks WHERE filePath = :filePath")
    suspend fun deleteBookmarksByFile(filePath: String)
    
    @Query("DELETE FROM bookmarks WHERE filePath = :filePath AND pageNumber = :pageNumber")
    suspend fun deleteBookmarkByFileAndPage(filePath: String, pageNumber: Int)
    
    @Query("DELETE FROM bookmarks")
    suspend fun clearAllBookmarks()
    
    @Query("SELECT COUNT(*) FROM bookmarks")
    suspend fun getBookmarksCount(): Int
    
    @Query("SELECT COUNT(*) FROM bookmarks WHERE filePath = :filePath")
    suspend fun getBookmarksCountByFile(filePath: String): Int
    
    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE filePath = :filePath AND pageNumber = :pageNumber)")
    suspend fun isPageBookmarked(filePath: String, pageNumber: Int): Boolean
}