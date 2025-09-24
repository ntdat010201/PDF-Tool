package com.example.pdftool.apdater

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.pdftool.R
import com.example.pdftool.databinding.ItemFileBinding
import com.example.pdftool.model.ModelFileItem
import com.example.pdftool.utils.FormatUtil

class PDFFileAdapter(
    private var files: List<ModelFileItem>
) : RecyclerView.Adapter<PDFFileAdapter.FileViewHolder>() {
    var onItemClickMore: ((ModelFileItem) -> Unit)? = null
    var onItemClickItem: ((ModelFileItem) -> Unit)? = null

    fun updateFiles(newFiles: List<ModelFileItem>) {
        val diffCallback = FilesDiffCallback(this.files, newFiles)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        this.files = newFiles
        diffResult.dispatchUpdatesTo(this)
    }

    private class FilesDiffCallback(
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        return FileViewHolder(
            ItemFileBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return files.size
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        val fileItem = files[position]

        holder.name.text = fileItem.name
        holder.iconFile.setImageResource(R.drawable.ic_pdf)
        holder.lastModified.text = FormatUtil.formatFileDate(fileItem.lastModified)
        holder.sizeFile.text = FormatUtil.formatFileSize(fileItem.size)

        holder.more.setOnClickListener {
            onItemClickMore?.invoke(fileItem)
        }

        holder.itemView.setOnClickListener {
            onItemClickItem?.invoke(fileItem)
        }
    }


    inner class FileViewHolder(binding: ItemFileBinding) : RecyclerView.ViewHolder(binding.root) {
        val name = binding.fileName
        val iconFile = binding.fileIcon
        val lastModified = binding.lastModified
        val sizeFile = binding.sizeFile
        val more = binding.moreVert

    }

}