package com.example.pdftool.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pdftool.data.repository.FileRepository
import com.example.pdftool.model.ModelFileItem
import kotlinx.coroutines.launch
import java.io.File

class FileViewModel(context: Context) : ViewModel() {
    
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
    
    init {
        checkPermissions()
        loadPDFFiles()
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
                _pdfFiles.value = files
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

}