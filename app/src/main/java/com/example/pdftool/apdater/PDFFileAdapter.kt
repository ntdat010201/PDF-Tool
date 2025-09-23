package com.example.pdftool.apdater

import android.view.LayoutInflater
import android.view.ViewGroup
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

    fun updateFiles(files: List<ModelFileItem>) {
        this.files = files
        notifyDataSetChanged()
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