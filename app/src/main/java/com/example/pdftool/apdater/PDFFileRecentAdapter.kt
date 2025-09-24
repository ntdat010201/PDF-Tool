package com.example.pdftool.apdater

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.pdftool.R
import com.example.pdftool.databinding.ItemFileRecentBinding
import com.example.pdftool.model.ModelFileItem
import com.example.pdftool.utils.FormatUtil
import java.text.SimpleDateFormat
import java.util.*

class PDFFileRecentAdapter(
    private var files: List<ModelFileItem>
) : RecyclerView.Adapter<PDFFileRecentAdapter.RecentFileViewHolder>() {
    var onItemClickMore: ((ModelFileItem) -> Unit)? = null
    var onItemClickItem: ((ModelFileItem) -> Unit)? = null

    fun updateFiles(newFiles: List<ModelFileItem>) {
        val diffCallback = RecentFilesDiffCallback(this.files, newFiles)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        this.files = newFiles
        diffResult.dispatchUpdatesTo(this)
    }

    private class RecentFilesDiffCallback(
        private val oldList: List<ModelFileItem>,
        private val newList: List<ModelFileItem>
    ) : DiffUtil.Callback() {
        
        override fun getOldListSize(): Int = oldList.size
        
        override fun getNewListSize(): Int = newList.size
        
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].path == newList[newItemPosition].path
        }
        
        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldItem = oldList[oldItemPosition]
            val newItem = newList[newItemPosition]
            return oldItem.name == newItem.name && 
                   oldItem.path == newItem.path &&
                   oldItem.size == newItem.size &&
                   oldItem.lastModified == newItem.lastModified
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentFileViewHolder {
        return RecentFileViewHolder(
            ItemFileRecentBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return files.size
    }

    override fun onBindViewHolder(holder: RecentFileViewHolder, position: Int) {
        val fileItem = files[position]

        holder.fileName.text = fileItem.name
        holder.fileIcon.setImageResource(R.drawable.ic_pdf)
        holder.filePath.text = fileItem.path
        
        // Format last viewed time for recent files
        holder.lastViewedTime.text = formatLastViewedTime(fileItem.lastModified)

        holder.moreVert.setOnClickListener {
            onItemClickMore?.invoke(fileItem)
        }

        holder.itemView.setOnClickListener {
            onItemClickItem?.invoke(fileItem)
        }
    }

    private fun formatLastViewedTime(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        
        return when {
            diff < 60 * 1000 -> "Vừa xem"
            diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)} phút trước"
            diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)} giờ trước"
            diff < 7 * 24 * 60 * 60 * 1000 -> "${diff / (24 * 60 * 60 * 1000)} ngày trước"
            else -> {
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                dateFormat.format(Date(timestamp))
            }
        }
    }

    inner class RecentFileViewHolder(binding: ItemFileRecentBinding) : RecyclerView.ViewHolder(binding.root) {
        val fileName = binding.fileName
        val fileIcon = binding.fileIcon
        val lastViewedTime = binding.lastViewedTime
        val filePath = binding.filePath
        val moreVert = binding.moreVert
    }
}