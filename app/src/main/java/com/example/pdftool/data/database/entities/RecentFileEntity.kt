package com.example.pdftool.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recent_files")
data class RecentFileEntity(
    @PrimaryKey
    val filePath: String,
    val fileName: String,
    val fileSize: Long,
    val lastViewedTime: Long,
    val fileUri: String? = null
)