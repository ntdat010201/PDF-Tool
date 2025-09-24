package com.example.pdftool.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pdftool.data.repository.FileRepository
import com.example.pdftool.data.repository.RecentFileRepository
import com.example.pdftool.data.database.entities.RecentFileEntity
import com.example.pdftool.model.ModelFileItem
import kotlinx.coroutines.launch
import java.io.File

class FileViewModel(context: Context, private val recentFileRepository: RecentFileRepository) : ViewModel() {
    
    private val fileRepository = FileRepository(context)
    
    // LiveData for PDF files list
    private val _pdfFiles = MutableLiveData<List<ModelFileItem>>()
    val pdfFiles: LiveData<List<ModelFileItem>> = _pdfFiles
    
    // LiveData for loading state
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    // LiveData for error messages
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage
    
    // LiveData for permission status
    private val _hasPermissions = MutableLiveData<Boolean>()
    val hasPermissions: LiveData<Boolean> = _hasPermissions
    
    // LiveData for current sort type
    private val _currentSortType = MutableLiveData<String>()
    val currentSortType: LiveData<String> = _currentSortType
    
    // Store original unsorted files
    private var originalFiles: List<ModelFileItem> = emptyList()
    
    // LiveData for recent files
    private val _recentFiles = MutableLiveData<List<ModelFileItem>>()
    val recentFiles: LiveData<List<ModelFileItem>> = _recentFiles
    
    init {
        checkPermissions()
        loadPDFFiles()
        loadRecentFiles()
    }
    
    private fun loadRecentFiles() {
        viewModelScope.launch {
            recentFileRepository.getRecentFiles().observeForever { recentEntities ->
                val modelItems = recentEntities.map { entity -> 
                    recentFileRepository.convertToModelFileItem(entity) 
                }
                _recentFiles.postValue(modelItems)
            }
        }
    }
    
    /**
     * Load PDF files from repository
     */
    fun loadPDFFiles() {
        _isLoading.value = true
        _errorMessage.value = null
        
        viewModelScope.launch {
            try {
                val files = fileRepository.getPDFFiles()
                originalFiles = files
                
                // Apply default sorting (newest to oldest)
                val sortedFiles = files.sortedByDescending { it.lastModified }
                _currentSortType.value = "nto"
                _pdfFiles.value = sortedFiles
                _isLoading.value = false
            } catch (e: Exception) {
                _errorMessage.value = "Error loading PDF files: ${e.message}"
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Sort files by the specified type
     */
    fun sortFiles(sortType: String) {
        val filesToSort = originalFiles.ifEmpty { _pdfFiles.value ?: emptyList() }
        
        val sortedFiles = when (sortType) {
            "az" -> filesToSort.sortedBy { it.name }
            "za" -> filesToSort.sortedByDescending { it.name }
            "nto" -> filesToSort.sortedByDescending { it.lastModified }
            "otn" -> filesToSort.sortedBy { it.lastModified }
            "bts" -> filesToSort.sortedByDescending { it.size }
            "stb" -> filesToSort.sortedBy { it.size }
            else -> filesToSort
        }
        
        _currentSortType.value = sortType
        _pdfFiles.value = sortedFiles
    }
    
    /**
     * Refresh PDF files list
     */
    fun refreshPDFFiles() {
        loadPDFFiles()
    }
    
    /**
     * Check if permissions are granted
     */
    fun checkPermissions() {
        _hasPermissions.value = fileRepository.hasPermissions()
    }
    
    /**
     * Handle permission granted
     */
    fun onPermissionGranted() {
        checkPermissions()
        if (_hasPermissions.value == true) {
            loadPDFFiles()
        }
    }
    
    /**
     * Handle permission denied
     */
    fun onPermissionDenied() {
        checkPermissions()
        _pdfFiles.value = emptyList()
        _errorMessage.value = "Storage permission is required to access PDF files"
    }

    fun deleteFile(file: ModelFileItem): Boolean {
        val fileToDelete = File(file.path)
        val success = fileToDelete.delete()
        if (success) {
            // Update both original and current lists
            originalFiles = originalFiles.filter { it.path != file.path }
            val updatedList = _pdfFiles.value?.filter { it.path != file.path } ?: emptyList()
            _pdfFiles.value = updatedList
        }
        return success
    }

    fun renameFile(oldFile: ModelFileItem, newFileName: String): Boolean {
        val oldFileObject = File(oldFile.path)
        val fileExtension = oldFileObject.extension
        val newFile = File(oldFileObject.parentFile, "$newFileName.$fileExtension")

        if (newFileName.contains(Regex("[\\\\/:*?\"<>|]"))) {
            return false
        }

        val success = oldFileObject.renameTo(newFile)

        if (success) {
            // Update both original and current lists
            originalFiles = originalFiles.map {
                if (it.path == oldFile.path) {
                    it.copy(name = newFile.name, path = newFile.absolutePath)
                } else {
                    it
                }
            }
            
            val updatedList = _pdfFiles.value?.map {
                if (it.path == oldFile.path) {
                    it.copy(name = newFile.name, path = newFile.absolutePath)
                } else {
                    it
                }
            } ?: emptyList()
            _pdfFiles.value = updatedList
        }
        return success
    }

    /**
     * Add file to recent files when viewed
     */
    fun addRecentFile(file: ModelFileItem) {
        viewModelScope.launch {
            recentFileRepository.addRecentFile(file)
        }
    }

    /**
     * Remove file from recent files
     */
    fun removeRecentFile(filePath: String) {
        viewModelScope.launch {
            recentFileRepository.removeRecentFile(filePath)
        }
    }

    /**
     * Clear all recent files
     */
    fun clearAllRecentFiles() {
        viewModelScope.launch {
            recentFileRepository.clearAllRecentFiles()
        }
    }

    /**
     * Sort recent files by the specified type
     */
    fun sortRecentFiles(sortType: String) {
        val currentRecentFiles = _recentFiles.value ?: emptyList()
        
        val sortedFiles = when (sortType) {
            "az" -> currentRecentFiles.sortedBy { it.name }
            "za" -> currentRecentFiles.sortedByDescending { it.name }
            "nto" -> currentRecentFiles.sortedByDescending { it.lastModified }
            "otn" -> currentRecentFiles.sortedBy { it.lastModified }
            "bts" -> currentRecentFiles.sortedByDescending { it.size }
            "stb" -> currentRecentFiles.sortedBy { it.size }
            else -> currentRecentFiles
        }
        
        _recentFiles.value = sortedFiles
    }

}