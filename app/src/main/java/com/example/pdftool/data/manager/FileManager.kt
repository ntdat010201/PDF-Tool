package com.example.pdftool.data.manager

import android.content.Context
import android.os.Environment
import android.util.Log
import com.example.pdftool.model.PDFFile
import java.io.File

class FileManager(private val context: Context) {

    fun getAllPDFFiles(): List<PDFFile> {
        val pdfFiles = mutableListOf<PDFFile>()

        // Scan external storage directories
        val externalStorageDir = Environment.getExternalStorageDirectory()
        if (externalStorageDir != null && externalStorageDir.exists()) {
            scanDirectory(externalStorageDir, pdfFiles)
        }

        // Scan Downloads folder specifically
        val downloadsDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (downloadsDir != null && downloadsDir.exists()) {
            scanDirectory(downloadsDir, pdfFiles)
        }

        // Scan Documents folder
        val documentsDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        if (documentsDir != null && documentsDir.exists()) {
            scanDirectory(documentsDir, pdfFiles)
        }

        return pdfFiles.distinctBy { it.path } // Remove duplicates
    }

    private fun scanDirectory(directory: File, pdfFiles: MutableList<PDFFile>) {
        try {
            directory.listFiles()?.forEach { file ->
                when {
                    file.isFile && file.name.lowercase().endsWith(".pdf") -> {
                        pdfFiles.add(
                            PDFFile(
                                name = file.name,
                                path = file.absolutePath,
                                size = file.length(),
                                lastModified = file.lastModified()
                            )
                        )
                    }

                    file.isDirectory && !file.name.startsWith(".") -> {
                        // Recursively scan subdirectories (but avoid hidden folders)
                        scanDirectory(file, pdfFiles)
                    }
                }
            }
        } catch (e: SecurityException) {
            // Handle permission denied for specific directories
            Log.w(
                "FileManager",
                "Permission denied for directory: ${directory.absolutePath}"
            )
        } catch (e: Exception) {
            // Handle other exceptions
            Log.e(
                "FileManager",
                "Error scanning directory: ${directory.absolutePath}",
                e
            )
        }
    }
}