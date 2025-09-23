package com.example.pdftool.data.repository

import android.content.Context
import com.example.pdftool.model.ModelFileItem
import com.example.pdftool.data.manager.FileManager
import com.example.pdftool.utils.PermissionHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FileRepository(private val context: Context) {
    
    private val fileManager = FileManager(context)
    
    /**
     * Get PDF files list
     */
    suspend fun getPDFFiles(): List<ModelFileItem> = withContext(Dispatchers.IO) {
        if (PermissionHelper.hasBasicStoragePermission(context)) {
            val pdfFiles = fileManager.getAllPDFFiles()
            pdfFiles.map { pdfFile ->
                ModelFileItem(
                    name = pdfFile.name,
                    path = pdfFile.path,
                    type = "pdf",
                    lastModified = pdfFile.lastModified,
                    size = pdfFile.size
                )
            }
        } else {
            emptyList()
        }
    }
    
    /**
     * Check if permissions are granted
     */
    fun hasPermissions(): Boolean {
        return PermissionHelper.hasBasicStoragePermission(context)
    }
}