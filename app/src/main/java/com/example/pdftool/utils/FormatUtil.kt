package com.example.pdftool.utils
import android.icu.text.SimpleDateFormat
import java.util.*

class FormatUtil {
    companion object {

        fun formatFileSize(sizeInBytes: Long): String {
            val kb = sizeInBytes / 1024
            val mb = kb / 1024
            return when {
                mb > 0 -> "$mb MB"
                kb > 0 -> "$kb KB"
                else -> "$sizeInBytes bytes"
            }
        }

        fun formatFileDate(timestamp: Long): String {
            val date = Date(timestamp)
            val format = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
            return format.format(date)
        }
    }


}