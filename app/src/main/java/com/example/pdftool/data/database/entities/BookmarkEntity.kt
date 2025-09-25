package com.example.pdftool.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val filePath: String,
    val fileName: String,
    val pageNumber: Int,
    val title: String,
    val note: String? = null,
    val createdTime: Long = System.currentTimeMillis(),
    val fileUri: String? = null
)