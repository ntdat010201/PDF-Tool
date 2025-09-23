package com.example.pdftool.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object PermissionHelper {
    
    // Request codes
    const val REQUEST_STORAGE_PERMISSION = 1001
    const val REQUEST_MANAGE_EXTERNAL_STORAGE = 1002
    const val REQUEST_MEDIA_PERMISSIONS = 1003
    
    /**
     * Lấy danh sách permissions cần thiết dựa trên phiên bản Android
     */
    fun getRequiredPermissions(): Array<String> {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                // Android 13+ (API 33+)
                arrayOf(
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.READ_MEDIA_AUDIO
                )
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                // Android 11+ (API 30+)
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            }
            else -> {
                // Android 10 và thấp hơn
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            }
        }
    }
    
    /**
     * Kiểm tra xem tất cả permissions đã được cấp chưa
     */
    fun hasAllPermissions(context: Context): Boolean {
        // Kiểm tra MANAGE_EXTERNAL_STORAGE cho Android 11+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                return false
            }
        }
        
        // Kiểm tra các permissions thông thường
        val requiredPermissions = getRequiredPermissions()
        return requiredPermissions.all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    /**
     * Kiểm tra xem có permissions cơ bản để đọc file không
     */
    fun hasBasicStoragePermission(context: Context): Boolean {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                // Android 13+: Cần READ_MEDIA_* permissions
                ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                // Android 6+: Cần READ_EXTERNAL_STORAGE
                ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
            }
            else -> {
                // Android 5.1 và thấp hơn: Permissions được cấp tự động
                true
            }
        }
    }
    
    /**
     * Xin permissions cơ bản
     */
    fun requestBasicPermissions(activity: Activity) {
        val permissions = getRequiredPermissions()
        ActivityCompat.requestPermissions(activity, permissions, REQUEST_STORAGE_PERMISSION)
    }
    
    /**
     * Xin quyền MANAGE_EXTERNAL_STORAGE cho Android 11+
     */
    fun requestManageExternalStoragePermission(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.data = Uri.parse("package:${activity.packageName}")
                activity.startActivityForResult(intent, REQUEST_MANAGE_EXTERNAL_STORAGE)
            } catch (e: Exception) {
                // Fallback nếu intent không hoạt động
                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                activity.startActivityForResult(intent, REQUEST_MANAGE_EXTERNAL_STORAGE)
            }
        }
    }
    
    /**
     * Xin tất cả permissions cần thiết
     */
    fun requestAllPermissions(activity: Activity) {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                // Android 11+: Xin MANAGE_EXTERNAL_STORAGE trước
                if (!Environment.isExternalStorageManager()) {
                    requestManageExternalStoragePermission(activity)
                } else {
                    requestBasicPermissions(activity)
                }
            }
            else -> {
                // Android 10 và thấp hơn: Xin permissions thông thường
                requestBasicPermissions(activity)
            }
        }
    }
    
    /**
     * Kiểm tra kết quả xin permission
     */
    fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ): Boolean {
        return when (requestCode) {
            REQUEST_STORAGE_PERMISSION, REQUEST_MEDIA_PERMISSIONS -> {
                grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }
            }
            else -> false
        }
    }
    
    /**
     * Kiểm tra xem có nên hiển thị rationale không
     */
    fun shouldShowRequestPermissionRationale(activity: Activity): Boolean {
        val permissions = getRequiredPermissions()
        return permissions.any { permission ->
            ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
        }
    }
    
    /**
     * Mở settings để user cấp permission thủ công
     */
    fun openAppSettings(activity: Activity) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.parse("package:${activity.packageName}")
        activity.startActivity(intent)
    }
    
    /**
     * Lấy thông báo phù hợp cho từng phiên bản Android
     */
    fun getPermissionMessage(context: Context): String {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                "Ứng dụng cần quyền truy cập ảnh, video và âm thanh để hiển thị file PDF. Vui lòng cấp quyền trong cài đặt."
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                "Ứng dụng cần quyền quản lý tất cả file để hiển thị PDF. Vui lòng bật 'Cho phép quản lý tất cả file' trong cài đặt."
            }
            else -> {
                "Ứng dụng cần quyền truy cập bộ nhớ để hiển thị file PDF. Vui lòng cấp quyền."
            }
        }
    }
    
    /**
     * Kiểm tra xem có thể đọc file từ URI không
     */
    fun canReadFromUri(context: Context, uri: Uri): Boolean {
        return try {
            context.contentResolver.openInputStream(uri)?.use { true } ?: false
        } catch (e: Exception) {
            false
        }
    }
}